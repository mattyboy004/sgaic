# -*- coding: utf-8 -*-
import master, os, glob, pickle, time, sqlite3


def save(pop, n):
	with open("./generations/%05dgen.bot" % n, "w") as f:
		pickle.dump(pop, f)


def first_pop(pop_size, nparam):
	if db_has_data():
		return db_pop(pop_size, nparam)
	return random_pop(pop_size, nparam), 0


def db_has_data():
	return len(glob.glob("./generations/*")) != 0


def db_pop(pop_size, nparam):
	gen = sorted(glob.glob("./generations/*"))[-1]
	ngen = int(gen[-12:-7])
	with open(gen, 'r') as f:
		return pickle.load(f), ngen
	

def random_pop(pop_size, nparam):
	pop = []
	init = [-1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0.8, 0, 0, 0, 0, 1, 0, 0, 0, 0.8, 0, 0]
	pop.append(init)
	for i in range(pop_size - 1):
		new = []
		for j in range(nparam):
			new.append(master.get_random_param())
		pop.append(new)
	return pop


def connect():
	return sqlite3.connect('./comps.db')