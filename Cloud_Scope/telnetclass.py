import sys
import telnetlib
import time

class TekScope:
	host = "192.168.2.10"
	port = 4000

	def setHost(self, newHost):
		self.host = newHost

	def setPort(self, newPort):
		self.port = newPort

	def connect(self):
		try:
			self.tn = telnetlib.Telnet(self.host, self.port)
			self.tn.read_until("\n>",2)
		except:
			print "ERROR: Unable to communicate with device"
			self.connect()
		
	def read3(self):
		message = ">"
		while (message == "") | (message == ">") | (message == ''):
			message = self.tn.read_very_eager()
		print message
		self.tn.read_until(">")
	
	def storetosql(self):
		print "/n"
	
	def close(self):
		self.tn.close
	
	def Write(self,stuff):
		try:
			self.tn.write(stuff.encode('ascii') + "\r\n".encode('ascii'))
			time.sleep(.1)
		except:
			print "ERROR: Unable to communicate with device"
		
#telscope = TekScope()
#telscope.setPort(4000)
#telscope.setHost("192.168.2.10")
#telscope.connect()
#telscope.read()
#telscope.Write("*idn?")

#telscope.Write('MESSAGE:SHOW "HELLO"')
#telscope.Write("MESSAGE:STATE ON")
#telscope.read3()
#telscope.close()
