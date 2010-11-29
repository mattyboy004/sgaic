#!/usr/bin/env python

import os, sys, subprocess, shlex, threading, multiprocessing, simplejson
from threadpool import *


# NUM_THREADS = 1   # must equal to half available cores
try:
	NUM_THREADS = max(multiprocessing.cpu_count() / 2, 1)
except NotImplementedError:
	NUM_THREADS = 1
TIME_LIMIT = 1000 # milliseconds
NUM_ROUNDS = 200
ADVERSARY_TAG = 'v8'


class GameParameters(object):
	def __init__(self, mapfile):
		self.mapfile = mapfile
	

if __name__ == "__main__":
	try: print >> sys.stderr, "Available CPUs:", multiprocessing.cpu_count()
	except NotImplementedError: pass
	print >> sys.stderr, "Number of simultaneous threads:", NUM_THREADS
	print >> sys.stderr
	
	print >> sys.stderr, "Cleaning...",
	os.system("rm -rf ./adversary/MyBot.*")
	os.system("rm -rf ./log.txt")
	print >> sys.stderr, "Done."
	print >> sys.stderr
	
	print >> sys.stderr, "Loading adversary from repository...",
	os.system("hg archive -r %s -I ../MyBot.java ." % ADVERSARY_TAG)
	os.system("rm -rf ./adversary/MyBot.*")
	os.system("mv -f ./java_starter_package/MyBot.java adversary/")
	os.system("rm -rf ./java_starter_package ./.hg_archival.txt")
	print >> sys.stderr, "Done."
	
	print >> sys.stderr, "Compiling...",
	os.system("javac ../*.java")
	os.system("javac ./adversary/*.java")
	print >> sys.stderr, "Done."
	print >> sys.stderr

	games = [ GameParameters("../maps/map%d.txt" % mapnumber) for mapnumber in range(1, 101) ]
	
	statistics = {'draw': 0, 'p1': 0, 'p2': 0}
	statistics_lock = threading.Lock()

	def playgame(parameters):
		cmdline = """java -jar ../tools/PlayGame.jar %s %d %d ./log.txt 
			"java -cp ../ MyBot %s" 
			"java -cp ./adversary MyBot %s" 
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
	pool.wait();
	print >> sys.stderr
	print >> sys.stderr
	
	print >> sys.stderr, '|        Statistics         |'
	print >> sys.stderr, '| Wins |  P1  |  P2  | Draw |'
	print >> sys.stderr, '|      | %4d | %4d | %4d |' % (statistics['p1'], statistics['p2'], statistics['draw'])
	print simplejson.dumps({'p1': statistics['p1'], 'p2': statistics['p2'], 'draw': statistics['draw']})
