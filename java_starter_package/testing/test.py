#!/usr/bin/env python

import os, sys, subprocess, shlex, threading, multiprocessing
from threadpool import *


# NUM_THREADS = 1   # must equal to half available cores
try:
	NUM_THREADS = max(multiprocessing.cpu_count() / 2, 1)
except NotImplementedError:
	NUM_THREADS = 1
TIME_LIMIT = 1000 # milliseconds
NUM_ROUNDS = 200
ADVERSARY_TAG = 'v7'


class GameParameters(object):
	def __init__(self, mapfile):
		self.mapfile = mapfile
	

if __name__ == "__main__":
	try: print "Available CPUs:", multiprocessing.cpu_count()
	except NotImplementedError: pass
	print "Number of simultaneous threads:", NUM_THREADS
	print
	
	print "Loading adversary from repository...",
	sys.stdout.flush()
	os.system("hg archive -r %s -I ../MyBot.java ." % ADVERSARY_TAG)
	os.system("rm -rf ./adversary/MyBot.*")
	os.system("mv -f ./java_starter_package/MyBot.java adversary/")
	os.system("rm -rf ./java_starter_package ./.hg_archival.txt")
	print "Done."
	
	print "Compiling...",
	sys.stdout.flush()
	os.system("javac ../*.java")
	os.system("javac ./adversary/*.java")
	print "Done."
	print

	games = [ GameParameters("../maps/map%d.txt" % mapnumber) for mapnumber in range(1, 101) ]
	
	statistics = {'draw': 0, 'p1': 0, 'p2': 0}
	statistics_lock = threading.Lock()

	def playgame(parameters):
		cmdline = """java -jar ../tools/PlayGame.jar %s %d %d ./log.txt "java -cp ../ MyBot" "java -cp ./adversary MyBot" """ % (parameters.mapfile, TIME_LIMIT, NUM_ROUNDS)
		p = subprocess.Popen(shlex.split(cmdline), 
		                     stdout=open('/dev/null', 'w'), 
		                     stderr=subprocess.PIPE)
		outlines = p.stderr.readlines()
		outstr = ''.join(outlines)
		if 'ERROR' in outstr:
			print outstr
		status = outlines[-1].strip()
		print status,
		sys.stdout.flush()
		with statistics_lock:
			if 'Player 1' in status:
				statistics['p1'] += 1
			elif 'Player 2' in status:
				statistics['p2'] += 1
			else:
				statistics['draw'] += 1
		
	print 'Playing...'
	pool = ThreadPool(NUM_THREADS)
	[ pool.putRequest(request) for request in makeRequests(playgame, games) ]	
	pool.wait();
	print
	print
	
	print "Cleaning...",
	sys.stdout.flush()
	os.system("rm -rf ./adversary/MyBot.*")
	os.system("rm -rf ./log.txt")
	print "Done."
	print
	
	print '|        Statistics         |'
	print '| Wins |  P1  |  P2  | Draw |'
	print '|      | %4d | %4d | %4d |' % (statistics['p1'], statistics['p2'], statistics['draw'])
