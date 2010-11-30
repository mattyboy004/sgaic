#!/usr/bin/env python

import time, subprocess, simplejson, shlex, urllib2
from bottle import *
from daemonize import daemonize


def main():
	generation_number = 0
	
	while True:
		generation_number += 1
		print "Generation number:", generation_number
		


if __name__ = '__main__':
	daemonize(stdin='/dev/null', stdout='stdout-log.txt', stderr='stderr-log.txt')
	main()
