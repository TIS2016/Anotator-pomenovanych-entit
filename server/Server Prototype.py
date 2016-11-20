import select
import socket
import Queue
from table import table as table

class server:

    def __init__(self):
        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.s.setblocking(0)
        self.s.bind(('localhost', 10000))
        self.s.listen(5)
        self.inputs,self.outputs = [self.s],[]
        self.message_queues = {}
        self.login_list = {}
        self.mainloop()

    def mainloop(self):
        while self.inputs:
            print("Waiting for the next event...")
            readable, writable, exceptional = select.select(self.inputs, self.outputs, self.inputs)
            for s in readable:
                if s is self.s:
                    # "readable" socket je pripraven=y akceptovat spojenie
                    connection, client_address = s.accept()
                    print('New connection from {}'.format(client_address))
                    connection.setblocking(0)
                    self.inputs.append(connection)
                    self.message_queues[connection] = Queue.Queue()
                else:
                    data = s.recv(1024)
                    if data:
                        data = data.decode('ascii')
                        # readable socket ma data
                        print('Received {} from {}'.format(data, s.getpeername()))
                        reply = self.parse(data,s)
                        self.message_queues[s].put(reply.encode('ascii'))
                        # Pridaj output kanal pre odpoved
                        if s not in self.outputs:
                            self.outputs.append(s)
                    else:
                        # Empty resuklt = strata spojenia
                        print('Closing ', client_address, ' after reading no data')
                        # Prestan cakat na input
                        if s in self.outputs:
                            self.outputs.remove(s)
                        self.inputs.remove(s)
                        s.close()
                        del self.message_queues[s]
            for s in writable:
                try:
                    next_msg = self.message_queues[s].get_nowait()
                except Queue.Empty:
                    # Necakaju spravy, tak prestan kontrolovat zapisovatelnost
                    print('Output queue for ', s.getpeername(), ' is empty')
                    self.outputs.remove(s)
                else:
                    print('Sending "{}" to {}'.format(next_msg, s.getpeername()))
                    s.send(next_msg)
            for s in exceptional:
                print('Handling exceptional condition for ', s.getpeername())
                # Prestan cakat na input
                self.inputs.remove(s)
                if s in self.outputs:
                    self.outputs.remove(s)
                s.close()
                del self.message_queues[s]

    def parse(self,string,sock_source):
        def eq(a,b):
            return a==b
        def gr(a,b):
            return a>b
        def goe(a,b):
            return a>=b
        request = string.split('|||')
        reply = ''
        if request[0] == 'login':
            t = table()
            t.import_table('DB/users.txt')
            result = t.get_rows_where([(eq,'Username',request[1]),(eq,'Password',request[2])])
            if result == []:
                return 'Invalid credentials'
            elif len(result) > 1:
                return 'Error, multiple accounts'
            else:
                user = result[0]
                self.login_list[sock_source.getpeername()] = user
                return 'Login, successful, {}. Welcome!'.format(user['Username'])
        elif request[0] == 'loginlist':
            ret = ''
            for key in self.login_list:
                ret = ret + '{} at {}\n'.format(self.login_list[key],key)
            return ret
        else:
            return 'Unknown request: "{}"'.format(string)

s = server()










































