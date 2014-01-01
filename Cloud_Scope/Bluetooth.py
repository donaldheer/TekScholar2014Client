#*****Finger Print Scanner Driver*****
#*****Brandon Fry*****

import serial
from array import *
import sys
import time
import telnetlib
from telnetclass import TekScope

class Bluetooth:
#	GET THE CORRECT USB DEVICE
#	ser = serial.Serial(port='/dev/ttyUSB0', baudrate=9600, timeout=5)

	#Send data packet to Bluetooth 
	def Send_Data(self, devID, data):
		for x in range(0, len(data)):
			self.ser.write(chr(data[x]))
		time.sleep(.1)
		self.ser.flush()

	def Recieve_Data(self):
		#IS THIS BLOCKING?	
		val = self.ser.readline()
		return val 

