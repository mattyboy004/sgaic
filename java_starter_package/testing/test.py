#!/usr/bin/env python

import os, sys, subprocess, shlex, threading, multiprocessing, simplejson, random
from threadpool import *


try:
	NUM_THREADS = max(multiprocessing.cpu_count() / 2, 1)
except NotImplementedError:
	NUM_THREADS = 1
TIME_LIMIT = 1000 # milliseconds
NUM_ROUNDS = 200


class GameParameters(object):
	def __init__(self, mapfile):
		self.mapfile = mapfile
	

if __name__ == "__main__":
	try: print >> sys.stderr, "Available CPUs:", multiprocessing.cpu_count()
	except NotImplementedError: pass
	print >> sys.stderr, "Number of simultaneous threads:", NUM_THREADS
	print >> sys.stderr
	
	print >> sys.stderr, "Cleaning...",
	os.system("rm -rf ./log.txt")
	print >> sys.stderr, "Done."
	print >> sys.stderr
	
	print >> sys.stderr, "Compiling...",
	subprocess.call('make', cwd='..', stdout=open('/dev/null', 'w'), stderr=open('/dev/null', 'w'))
	print >> sys.stderr, "Done."
	print >> sys.stderr

	games = random.sample([ GameParameters("../maps/map%d.txt" % mapnumber) for mapnumber in range(1, 101) ], 1)
	
	statistics = {'draw': 0, 'p1': 0, 'p2': 0}
	statistics_lock = threading.Lock()

	def playgame(parameters):
		cmdline = """java -jar ../tools/PlayGame.jar %s %d %d ./log.txt 
			"java -cp .. MyBot %s" 
			"java -cp .. MyBot %s" 
			""" % (parameters.mapfile, TIME_LIMIT, NUM_ROUNDS, ' '.join(sys.argv[1:33]), ' '.join(sys.argv[33:]))
		p = subprocess.Popen(shlex.split(cmdline), 
		                     stdout=open('/dev/null', 'w'), 
		                     stderr=subprocess.PIPE)
		outlines = p.stderr.readlines()
		outstr = ''.join(outlines)
		if 'ERROR' in outstr:
			print ERROR
			print outstr
			sys.exit(1)
		status = outlines[-1].strip()
		print >> sys.stderr, status,
		with statistics_lock:
			if 'Player 1' in status:
				statistics['p1'] += 1
			elif 'Player 2' in status:
				statistics['p2'] += 1
			else:
				statistics['draw'] += 1
		
	print >> sys.stderr, 'Playing...'
	pool = ThreadPool(NUM_THREADS)
	[ pool.putRequest(request) for request in makeRequests(playgame, games) ]	
	pool.wait()
	print >> sys.stderr
	print >> sys.stderr
	
	print >> sys.stderr, '|        Statistics         |'
	print >> sys.stderr, '| Wins |  P1  |  P2  | Draw |'
	print >> sys.stderr, '|      | %4d | %4d | %4d |' % (statistics['p1'], statistics['p2'], statistics['draw'])
	print simplejson.dumps({'p1': statistics['p1'], 'p2': statistics['p2'], 'draw': statistics['draw']})
