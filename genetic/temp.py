#!/usr/bin/env python

import pickle


DATA_DIR = 'data/'


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
		
		
	


if __name__ == '__main__':
	
