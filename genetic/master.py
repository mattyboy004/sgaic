#!/usr/bin/env python
# -*- coding: utf-8 -*-

import time, subprocess, simplejson, shlex, urllib2, random, DB
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
winners = []


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


def make_tournament(players):
	N = len(players)
	games = []
	for i in range(N):
		for j in range(i + 1, N):
			comp, port = DB.find_available()
			DB.lock(comp, port)
			try: 
				game = Thread(target = make_game, args = (comp, port, i, j))
				game.start()
				games.append(game)
			except:
				# o que fazer se um jogo der pau?
				pass
	for game in games:
		game.join()
	best = get_champ()
	#zerar winners
	return best


def get_champ():
	# achar quem é o melhor usando o array winners
	return []


def make_game(comp, port, i, j):
	# complementar método
	print comp, port, i, j
	# info = urllib2.urlopen('http://' + comp + ':' + port + '/execute?' + 
	#					params(player[i],0) + params(player[j],NPARAMS))
	# colocar informação de vitória no winner
	DB.release(comp, port)

def params(player, init):
	ans = ''
	for i in range(NPARAMS):
		ans += str(init + i) + '=' + player[i] + '&'
	ans = ans[:-1]
	return ans

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
	# daemonize(stdin='/dev/null', stdout='./log/stdout-log.txt', stderr='./log/stderr-log.txt')
	main()
