import os, sys
if __name__ == '__main__':
	for i in range(int(sys.argv[3])):
		cmd = 'nohup python ./client.py %s %s > ./log/%d.txt 2>&1 &' % (sys.argv[1], sys.argv[2], i)
		print cmd
		os.system(cmd)
