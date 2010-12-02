#!/usr/bin/env python
# -*- coding: utf-8 -*-

import time, subprocess, simplejson, shlex, urllib2, random, DB, threading, Queue
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

# comps_in_use = set()
# comps_in_use_lock = threading.Lock()


def main():
	pop, generation_number = DB.first_pop(POP_SIZE, NPARAMS)
	while True:
		DB.save(pop, generation_number)
		generation_number += 1
		print "Generation number:", generation_number
		best = natural_selection(pop)
		pop = next_generation(best, POP_SIZE)


def natural_selection(pop):
	best = []
	for i in range(SURVIVORS):
		random.shuffle(pop)
		best.append(make_tournament(pop[:TOURNAMENT_SIZE]))
	return best


def make_tournament(rawplayers):
	players = [ {'data': player, 'wins': 0, 'lock': threading.Lock()} for player in rawplayers ]
	matches = [ (players[i], players[j]) for i in range(len(players)) for j in range(len(players)) if j > i ]

	match_queue = Queue.Queue()
	[ match_queue.put(match) for match in matches ]
	
	def worker():
		# print >> sys.stderr, "Starting worker"
		db = DB.connect()
		try:
			while True:
				try:
					match = match_queue.get()
				except:
					break
				
				# print >> sys.stderr, "Got task"
				
				while True:
					cur = db.cursor()
					try:
						cur.execute('select host, port from comps where in_use = 0')
						hosts = cur.fetchall()
						# hosts = [ host for host in cur.fetchall()

						try:
							host, port = random.choice(hosts)
							# print >> sys.stderr, host, port
						except:
							# print >> sys.stderr, "No clients found, sleeping a little bit..."
							db.commit()
							time.sleep(0.5)
							continue
					
						cur.execute('update comps set in_use = 1 where host = ? and port = ?', (host, port))
						db.commit()

						try:
							urllib2.urlopen('http://%s:%s/ping' % (host, port), timeout=.5).read()
							stats = simplejson.loads(urllib2.urlopen('http://%s:%s/execute?%s' % (host, port, params(match[0]['data'] + match[1]['data']))).read())
							with match[0]['lock']:
								match[0]['wins'] += stats['p1']
							with match[1]['lock']:
								match[1]['wins'] += stats['p2']
							match_queue.task_done()
						except:
							# print >> sys.stderr, "Game failed. Retrying..."
							time.sleep(0.5)
							continue
						finally:
							cur.execute('update comps set in_use = 0 where host = ? and port = ?', (host, port))
							db.commit()
					
						print >> sys.stderr, "Game ok, host=%s, port=%s" % (host, port)
						break
					finally:
						cur.close()
		finally:
			db.close()
	
	for i in range(10):
		t = Thread(target=worker)
		t.daemon = True
		t.start()
	
	match_queue.join()
	
	max_wins = max([ player['wins'] for player in players ])
	return [ player['data'] for player in players if player['wins'] == max_wins ][0]


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
