#!/usr/bin/env python

import time, subprocess, simplejson, shlex, sys, daemonize, socket, sockutils, pickle

# 
# def execute():
# 	parameters = ' '.join([ request.GET.get(x) for x in map(str, range(64)) ])
# 	p = subprocess.Popen(shlex.split('python test.py %s' % parameters),
# 	                     cwd='../java_starter_package/testing/',
# 	                     stdout=subprocess.PIPE)
# 	out = ' '.join([ x.strip() for x in p.stdout.readlines() ])
# 	print >> sys.stderr, out
# 	if 'ERROR' in out:
# 		return {'error': out}
# 	else:
# 		return simplejson.loads(out)


if __name__ == '__main__':
	# daemonize.daemonize(stdin='/dev/null', stdout='./log/stdout-%s-log.txt'%sys.argv[1], stderr='./log/stderr-%s-log.txt'%sys.argv[1])
	# run(host='0.0.0.0',
	#     port=int(sys.argv[1]))
	
	while True:
		sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

		try:
			sock.settimeout(.5) # times out after .5s
			sock.connect((sys.argv[1], int(sys.argv[2])))
		except IndexError:
			print >> sys.stderr, 'usage: client.py IP PORT'
			sock.close()
			sys.exit(1)
		except:
			sock.close()
			time.sleep(.5)
			continue

		print >> sys.stderr, 'Receiving match...'
		try:
			sock.setblocking(1) # blocks until receives information
			match = pickle.loads(sockutils.recvall(sock))
			print >> sys.stderr, match
			
			p = subprocess.Popen(shlex.split('python test.py %s' % ' '.join(map(str, match['p1'] + match['p2']))),
			                     cwd='../java_starter_package/testing/',
			                     stdout=subprocess.PIPE)
			
			out = ' '.join([ x.strip() for x in p.stdout.readlines() ])
			print >> sys.stderr, out
			
			if 'ERROR' in out:
				msg = {'error': out}
			else:
				msg = simplejson.loads(out)
				
			sock.sendall(pickle.dumps(msg, 2))
		except Exception as ex:
			print >> sys.stderr, 'Match failed. Ex.: (%s, %s)' % (type(ex), ex)
		else:
			print >> sys.stderr, 'Match ok.'
		finally:
			sock.close()
