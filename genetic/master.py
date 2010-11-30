#!/usr/bin/env python

import time, subprocess, simplejson, shlex, urllib2
from bottle import *
from daemonize import daemonize

import pickle


DATA_DIR = 'data/'

def main():
	generation_number = 0
	
	while True:
		generation_number += 1
		print "Generation number:", generation_number
		

class Population(object):
	
	
	def __init__(self, population, generation_number):
		self.population, self.generation_number = population, generation_number
		
	
	def store(self):
		"""Consolidates generation data permanently."""
		fout = open(DATA_DIR + 'generation_%d.dat' % self.generation_number, 'w')
		pickle.dump(self, fout)
		
		
	@staticmethod
	def load(generation_number):
		"""Loads generation data from permanent store."""
		fin = open(DATA_DIR + 'generation_%d.dat' % generation_number, 'r')
		return pickle.load(fin)


if __name__ = '__main__':
	daemonize(stdin='/dev/null', stdout='stdout-log.txt', stderr='stderr-log.txt')
	main()
