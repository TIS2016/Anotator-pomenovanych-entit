import socket 

addr = ('localhost',10000)
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(addr)
while True:
    request = input("Request: ")
    if request == 'Close' or request == 'close':
        break
    else:
        s.send(request.encode('ascii'))
    response = s.recv(4096)
    if response:
        print('Received: ', response.decode('ascii'))
    else:
        break
print('Closing')
s.close()
