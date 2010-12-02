	
def recvall(sock):
	msg = ''
	while True:
		chunk = sock.recv(4096)
		if chunk == '':
			break
		msg += chunk
	return msg
	
