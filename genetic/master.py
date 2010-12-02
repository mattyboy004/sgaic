#!/usr/bin/env python
# -*- coding: utf-8 -*-

import time, subprocess, simplejson, shlex, urllib2, random, DB, threading, Queue, socket, sockutils
from bottle import *
from daemonize import daemonize
from threading import Thread
import pickle


DATA_DIR = 'data/'
POP_SIZE = 50
SURVIVORS = 15
TOURNAMENT_SIZE = 6
NPARAMS = 32
ALPHA = 0.03
SELECTION_PRESSURE = 0.7 # p for tournament selection


def main():
	global server_socket
	server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	try:
		server_socket.bind(('0.0.0.0', int(sys.argv[1])))
		server_socket.listen(5)
		
		pop, generation_number = DB.first_pop(POP_SIZE, NPARAMS)
		while True:
			DB.save(pop, generation_number)
			generation_number += 1
			print "Generation number:", generation_number
			best = natural_selection(pop)
			pop = next_generation(best, POP_SIZE)
	finally:
		server_socket.close()


def natural_selection(pop):
	best = []
	tournaments = [ build_tournament(random.sample(pop, TOURNAMENT_SIZE)) for i in range(SURVIVORS) ]
	total_matches = sum([ tournament['matches'] for tournament in tournaments ], [])
	dispatch_matches(total_matches)
	
	for tournament in tournaments:
		players = reversed(sorted(tournament['players'], lambda x: x['wins']))
		for player in players:
			if random.random() < SELECTION_PRESSURE:
				best.append(player['data'])
				break
		else:
			best.append(tournament['players'][-1]['data'])
			
	return best
	
	
def dispatch_matches(matches):
	# worker target
	def worker(sock_info, match, outstanding_queue):
		client_sock, (client_host, client_port) = sock_info
	
		print >> sys.stderr, "Dispatching game to (%s, %s)" % (client_host, client_port)
		# print >> sys.stderr, match
		
		try:
			client_sock.sendall(pickle.dumps({'p1': match[0]['data'], 'p2': match[1]['data']}, 2))
			client_sock.shutdown(socket.SHUT_WR) # done sending, will only receive now
			match_result = pickle.loads(sockutils.recvall(client_sock))
			if 'error' in match_result:
				raise RuntimeError('Client-side game error.')
		except Exception as ex:
			print >> sys.stderr, "Game failed. (%s, %s). Exc.: (%s, %s)" % (client_host, client_port, type(ex), ex.args)
			outstanding_queue.put(match)
		else:
			print >> sys.stderr, "Game ok. (%s, %s)" % (client_host, client_port)
			with match[0]['lock']:
				match[0]['wins'] += match_result['p1']
			with match[1]['lock']:
				match[1]['wins'] += match_result['p2']
		finally:
			client_sock.close()

	# initial queue
	outstanding_queue = Queue.Queue()
	[ outstanding_queue.put(match) for match in matches ]

	# outstanding_queue stores initial and failed games.
	# current_queue stores the currently processed queue.
	while not outstanding_queue.empty():
		current_queue = outstanding_queue
		outstanding_queue = Queue.Queue()
		threads = []

		while not current_queue.empty():
			sock_info = server_socket.accept()
			match = current_queue.get()
			thr = Thread(target=worker, args=(sock_info, match, outstanding_queue))
			thr.daemon = True
			thr.start()
			threads.append(thr)

		[ thr.join() for thr in threads ]
	

def build_tournament(rawplayers):
	# tournament matches
	players = [ {'data': player, 'wins': 0, 'lock': threading.Lock()} for player in rawplayers ]
	matches = [ (players[i], players[j]) for i in range(len(players)) for j in range(len(players)) if j > i ]

	return {'players': players, 'matches': matches}


def get_champ(wins):
	return wins.index(max(wins))


def make_game(comp, port, i, j, pi, pj, wins, winslock):
	stats = simplejson.loads(urllib2.urlopen('http://%s:%s/execute?%s&%s' % (comp, port, params(pi, 0), params(pj, NPARAMS)))\
	                         .read())
	with winslock:
		wins[i] += stats['p1']
		wins[j] += stats['p2']
	DB.release(comp, port)
	
	
def params(player, init=0):
	return '&'.join([ '%d=%f' % (x[0] + init, x[1]) for x in enumerate(player) ])
	# ans = ''
	# for i in range(NPARAMS):
	# 	ans += str(init + i) + '=' + str(player[i]) + '&'
	# ans = ans[:-1]
	# return ans


def crossover(popa,popb):
	popx,popy = popa[:],popb[:]
	for i, val in enumerate(popb):
		if random.randint(1,2) == 1:	
			popy[i] = popx[i]
			popx[i] = val
			
	return popx,popy


def mutation(pop):
	pos = random.randint(0,len(pop)-1)
	pop[pos] = get_random_param()	

		
def fuck(popa,popb):
	popx,popy = crossover(popa,popb)
	
	if random.random() < ALPHA:
		mutation(popx)
	if random.random() < ALPHA:
		mutation(popy)
		
	return [popx,popy]		


def next_generation(original,number):
	next = []
	for i in range(number):
		next += fuck(random.choice(original),random.choice(original))
	random.shuffle(next)
	return next[:number]


def get_random_param():
	return 4*random.random() - 2

if __name__ == '__main__':
	# daemonize(stdin='/dev/null', stdout='./log/stdout-master-log.txt', stderr='./log/stderr-master-log.txt')
	main()
