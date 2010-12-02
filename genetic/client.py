#!/usr/bin/env python

import time, subprocess, simplejson, shlex, sys, daemonize
from bottle import *


debug(True)	


@route('/execute')
def execute():
	parameters = ' '.join([ request.GET.get(x) for x in map(str, range(64)) ])
	p = subprocess.Popen(shlex.split('python test.py %s' % parameters),
	                     cwd='../java_starter_package/testing/',
	                     stdout=subprocess.PIPE)
	out = ' '.join([ x.strip() for x in p.stdout.readlines() ])
	print >> sys.stderr, out
	if 'ERROR' in out:
		return {'error': out}
	else:
		return simplejson.loads(out)


if __name__ == '__main__':
	daemonize.daemonize(stdin='/dev/null', stdout='./log/stdout-log.txt', stderr='./log/stderr-log.txt')
	run(host='0.0.0.0',
	    port=int(sys.argv[1]),
	    reloader=True)
