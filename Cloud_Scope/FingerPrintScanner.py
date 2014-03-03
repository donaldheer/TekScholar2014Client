#*****Finger Print Scanner Driver*****
#*****Brandon Fry*****

import serial
from array import *
import sys
import time
import telnetlib
from telnetclass import TekScope

class Scanner:
	ser = serial.Serial(port='/dev/ttyUSB0', baudrate=9600, timeout=5)
	#print(int(ser.timeout()))
	#tekscope = TekScope()
	#tekscope.setHost("192.168.2.10")
	#tekscope.setPort(4000)
	#tekscope.connect()
	#tekscope.Write('MESSAGE.SHOW "HELLO FROM FINGERPRINTSCANNER"')
	#tekscope.Write("MESSAGE:STATE ON")
			
	#Hex Values for Commands
	#Commands taken from the fingerprint scanner datasheet: http://dlnmh9ip6v2uc.cloudfront.net/datasheets/Sensors/Biometric/GT-511C3_datasheet_V1%201_20130411[4].pdf
	OPEN = 0x01 #initialization
	CLOSE = 0X02 #TERMINATION
	USB_INTERNAL_CHECK = 0X03 #CHECK IF THE CONNECTED USB DEVICE IS VALID
	CHANGE_BUADRATE = 0X04 #CHANGE USART BUAD RATE
	SET_IAP_MODE = 0X05 #ENTER IAP MODE. IN THIS MODE, FW UPGRADE IS AVAILABLE
	CMOS_LED = 0X12 #CONTROL CMOS LED
	GET_ENROLLED_COUNT = 0X20 # GET ENROLLED FINGERPRINT COUNT
	CHECK_ENROLLED = 0X21 #CHECK WHETHER THE SPECIFIED ID IS ALREADY ENROLLED
	ENROLL_START = 0X22 #START AN ENROLLMENT
	ENROLL1 = 0X23 #MAKE 1ST TEMPLATE FOR ENROLLMENT
	ENROLL2 = 0X24 #MAKE 2ND TEMPLATE FOR ENROLLMENT
	ENROLL3 = 0X25 #Make 3rd template for an enrollment, merge three templates into one template, save merged template to the database
	IS_PRESS_FINGER = 0X26 #CHECK IF A FINGER IS PLACED ON THE SENSOR
	DELETE_ID = 0X40 #DELETE THE FINGERPRINT WITH THE SPECIFIED ID
	DELETE_ALL = 0X41 #DELETE ALL FINGERPRINTS FROM THE DATABASE
	VERIFY = 0X50 #1:1 VERIFICATION OF THE CAPTURE FINGERPRINT IMAGE WITH THE SPECIFIED ID
	IDENTIFY = 0X51 #1:N IDENTIFICATION OF THE CAPTURE FINGERPRINT IMAGE WITH THE DATABASE
	VERIFY_TEMPLATE = 0X52 #1:1 VERIFICATION OF A FINGERPRINT TEMPLATE WITH THE SPECIFIED ID
	IDENTIFY_TEMPLATE = 0X53 #1:N IDENTIFICATION OF A FINGERPRINT TEMPLATE WITH THE DATABASE
	CAPTURE_FINGER = 0X60 #CAPTURE A FINGERPRINT IMAGE (256X256) FROM THE SENSOR
	MAKE_TEMPLATE = 0X61 #MAKE TEMPLATE FOR TRANSMISSION
	GET_IMAGE = 0X62 #DOWNLOAD THE CAPTURED FINGERPRINT IMAGE (256X256)
	GET_RAW_IMAGE = 0X63 #CAPTURE & DOWNLOAD RAW FINGERPRINT IMAGE (320X240)
	GET_TEMPLATE = 0X70 #DOWNLOAD THE TEMPLATE OF THE SPECIFIED ID
	SET_TEMPLATE = 0X71 #UPLOAD THE TEMPLATE OF THE SPECIFIED ID
	GET_DATABASE_START = 0X72 #START DATABASE DOWNLOAD, OBSOLETE
	GET_DATABASE_END = 0X73 #END DATABASE DOWNLOAD, OBSOLETE
	ACK = 0X30 #self.ACKNOWLEDGE
	NACK = 0X31 #NON-self.ACKNOWLEDGE

	#ERROR CODES
	#When response packet is Non-acknowledge, Parameter represents an error code as below

	NACK_TIMEOUT = 0X1001 #OBSOLETE, CAPTURE TIMEOUT
	NACK_INVALID_BAUDRATE = 0X1002 #OBSOLETE, INVALID SERIAL BAUD RATE
	NACK_INVALID_POS = 0X1003 #THE SPECIFIED ID IS NOT BETWEEN 0~199
	NACK_IS_NOT_USED = 0X1004 #THE SPECIFIED ID IS NOT USED
	NACK_IS_ALREADY_USED = 0X1005 #THE SPECIFIED ID IS ALREADY USED
	NACK_COMM_ERR = 0X1006 #COMMUNICATION ERROR
	NACK_VERIFY_FAILED = 0X1007 #1:1 VERIFICATION FAILURE
	NACK_IDENTIFY_FAILED = 0X1008 #1:N VERIFICATION FAILURE
	NACK_DB_IS_FULL = 0X1009 #THE DATABASE IS FULL
	NACK_DB_IS_EMPTY = 0X100A #THE DATABASE IS EMPTY
	NACK_TURN_ERR = 0X100B #OBSOLETE, INVALID ORDER OF THE ENROLLMENT (THE ORDER WAS NOT AS: ENROLLSTART->ENROLL1->ENROLL2->ENROLL3)
	NACK_BAD_FINGER = 0X100C #TOO BAD FINGERPRINT
	NACK_ENROLL_FAILED = 0X100D #ENROLLMENT FAILURE
	NACK_IS_NOT_SUPPORTED = 0X100E #THE SPECIFIED COMMAND IS NOT SUPPORTED
	NACK_DEV_ERR = 0X100F #OBSOLETE, THE CAPTURING IS CANCELED
	NACK_INVALID_PARAM = 0X1011 #INVALID PARAMETER
	NACK_FINGER_IS_NOT_PRESSED = 0X1012 #FINGER IS NOT PRESSED
	# Duplicated ID 0-199: There is duplicated fingerprint (while enrollment or setting template), this error describes just duplicated ID
	
	DEVICE_ID = 0X0001 #DEFAULT DEVICE IS = 0X0001, ALWAYS FIXED
	DEV_ID_LOW = (DEVICE_ID &  0XFF)#LOW BYTE OF THE DEVICE ID. MUST MATCH THE VALUE FOR self.DEVICE_ID
	DEV_ID_HIGH = ((DEVICE_ID & 0XFF00)>> 8 )#HIGH BYTE OF THE DEVICE ID. MUST MATCH THE VALUE FOR self.DEVICE_ID

	START_CODE1 = 0X55 #COMMAND/RESPONSE START CODE1
	START_CODE2 = 0XAA #COMMAND/RESPONSE START CODE2
	DATA_START1 =0X5A #DATA START CODE1
	DATA_START2 = 0XA5 #DATA START CODE2

	PREV_NACK = 0 #Holds a variable to spcify if the previously recieved data is a NACK code or parameter
	
	def Scope_Print(self, msg):
		print(str(msg))
		
	def Adjust_Buad(self):
	        self.Send_Command(self.DEVICE_ID, self.CHANGE_BUADRATE, 0x2580)
	
	#command is a word long, parameter is a double word
	def Send_Command(self, devID, command, parameter):
		self.ser.write(chr(self.START_CODE1)) #send command start code1
		self.ser.write(chr(self.START_CODE2)) #send command start code2
		#send device id
		self.ser.write(chr((devID &  0x00FF))) #send low byte of command
		self.ser.write(chr(((devID & 0xFF00) >> 8))) #send high byte of device id
		#send parameter
		self.ser.write(chr((parameter & 0xFF))) #send first byte of parameter
		self.ser.write(chr(((parameter & 0xFF00) >> 8))) #send second byte of parameter
		self.ser.write(chr(((parameter & 0xFF0000) >> 16))) #send third byte of parameter
		self.ser.write(chr(((parameter & 0xFF000000) >> 24))) #send last byte of parameter
		#send command
		self.ser.write(chr((command &  0x00FF))) #send low byte of command
		self.ser.write(chr(((command & 0xFF00) >> 8))) #send high byte of device id
		#send CheckSum
		checkSum = self.START_CODE1 + self.START_CODE2 + self.DEV_ID_LOW + self.DEV_ID_HIGH + (parameter & 0xFF) + ((parameter & 0xFF00) >> 8) + ((parameter & 0xFF0000) >> 16) + ((parameter & 0xFF000000) >> 24) + (command &  0x00FF) + ((command & 0xFF00) >> 8)
		#checkSum = 256 + command + (parameter & 0xFF) + ((parameter & 0xFF00) >> 8)
		self.ser.write(chr((checkSum & 0xFF))) #send low byte of Check Sum Value
		self.ser.write(chr(((checkSum & 0xFF00) >> 8))) #send high byte of Check Sum Value
		time.sleep(.1) #testing to see if the data is coming too fast)
		self.ser.flush()
	
	#Send data packet to finger print scanner
	def Send_Data(self, devID, data):
		wsum = 0
		#Send data start ID
		self.ser.write(chr(self.DATA_START1))
		self.ser.write(chr(self.DATA_START2))
		#Send device id
		self.ser.write(chr((devID &  0x00FF))) #send low byte of command
		self.ser.write(chr(((devID & 0xFF00) >> 8))) #send high byte of device id
		#Send data
		for x in range(0, len(data)):
			wsum += int(data[x])	
			self.ser.write(chr(data[x]))
		#Send checksum
		checksum = (self.DATA_START1 + self.DATA_START2 + self.DEVICE_ID + wsum) 
		self.ser.write(chr(checksum & 0xFF))
		self.ser.write(chr((checksum & 0xFF00) >> 8))
		time.sleep(.1)
		self.ser.flush()
	
	#read in response packet from the self.serial port
	def Recieve_Response(self):
		self.PREV_NACK = 0
		receivedByte = self.ser.read()
		while (receivedByte != chr(self.START_CODE1)): #clear out excess data until the beginning of a response packet is found
			print("STUCK GETTING DATA " + receivedByte)	
			receivedByte = self.ser.read()
		receivedByte = self.ser.read() #read in the second start code
		if (receivedByte == chr(self.START_CODE2)): #checks for bad data
			receivedByte = self.ser.read()
			if (receivedByte == chr(self.DEV_ID_LOW)): #check low byte of Device ID
				receivedByte = self.ser.read()
				if (receivedByte == chr(self.DEV_ID_HIGH)): #check high byte of Device ID
					#Get the parameter DWORD	
					parameter = 0 
					parameter += int(ord(self.ser.read()))
					parameter += int(ord(self.ser.read())) << 8
					parameter += int(ord(self.ser.read())) << 16
					parameter += int(ord(self.ser.read())) << 24
					#Get the acknowledgement word
					response = 0
					response += int(ord(self.ser.read()))
					response += int(ord(self.ser.read())) << 8
					#Confirm the response
					#print(response)
					if (response != self.ACK):
						self.PREV_NACK = 1	
						return parameter	
					#Get the checksum
					check_sum = 0
					check_sum += int(ord(self.ser.read()))
					check_sum += int(ord(self.ser.read())) << 8
					#Confirm that the checksum is correct
					if (check_sum == (self.START_CODE1 + self.START_CODE2 + self.DEV_ID_LOW + self.DEV_ID_HIGH + parameter + response)):
						return parameter	
	
	def Recieve_Data(self, bytes_to_read):
		readsum = 0
		receivedByte = self.ser.read()
		while (receivedByte != chr(self.DATA_START1)): #clear out excess data until the beginning of a response packet is found
			print("STUCK GETTING DATA " + recievedByte)	
			receivedByte = self.ser.read()
		receivedByte = self.ser.read() #read in the second start code
		if (receivedByte == chr(self.DATA_START2)): #checks for bad data
			receivedByte = self.ser.read()
			if (receivedByte == chr(self.DEV_ID_LOW)): #check low byte of Device ID
				receivedByte = self.ser.read()
				if (receivedByte == chr(self.DEV_ID_HIGH)): #check high byte of Device ID
					data = []
					for x in range(0, bytes_to_read):	
						val = self.ser.read()
						readsum += int(ord(val))
						data.append(val)
					#Get the checksum
					check_sum = 0
					check_sum += int(ord(self.ser.read()))
					check_sum += int(ord(self.ser.read())) << 8
				
					#Confirm that the checksum is correct
					if (check_sum == (self.DATA_START1 + self.DATA_START2 + self.DEV_ID_LOW + self.DEV_ID_HIGH + readsum)):
						return data	
	
	#initialize communication with the fingerprint scanner.
	def Init_Scanner_Serial(self):
		self.Send_Command(self.DEVICE_ID, self.OPEN, 0) # Parameter = 0: don't get extra device info. Parameter != 0: get extra device info
		self.Recieve_Response()
	
	#terminates self.serial communication with the fingerprint scanner.
	def Close_Scanner_Serial(self):
		self.Send_Command(self.DEVICE_ID, CLOSE, 0)
		self.ser.close()
	
	#LedOn
	def Led_On(self):
		#Turn the light on
		self.Send_Command(self.DEVICE_ID,self.CMOS_LED,1)
		self.Recieve_Response()
	
	#LedOff
	def Led_Off(self):
		#Turn the light off
		self.Send_Command(self.DEVICE_ID,self.CMOS_LED,0)
		self.Recieve_Response()
	
	def Get_Unused_ID(self):
		#Get first unused ID	
		user_id = 0
		while(user_id < 200):
			self.Send_Command(self.DEVICE_ID, self.CHECK_ENROLLED, user_id)
			if(self.Recieve_Response() == self.NACK_IS_NOT_USED):	
				break
			user_id += 1
		return user_id
	
	#Wait for finger press
	def Wait_For_Press(self):
		while(True):
			self.Send_Command(self.DEVICE_ID, self.IS_PRESS_FINGER, 0)
			if(self.Recieve_Response() == 0):
				break
	
	#Wait for finger to release
	def Wait_For_Release(self):
		while(True):
			self.Send_Command(self.DEVICE_ID, self.IS_PRESS_FINGER, 0)
			if(self.Recieve_Response() != 0):
				break
	
	#Start uself.ser enrollment
	def Enroll_Start(self, user_id):
		self.Send_Command(self.DEVICE_ID, self.ENROLL_START, user_id)	
		recieved = self.Recieve_Response()
		if(recieved == self.NACK_DB_IS_FULL):
			self.Scope_Print("Database is full. Please remove some uself.sers")
			return -1
		if(recieved == self.NACK_INVALID_POS):
			self.Scope_Print("Invalid Position. Please try again")
			return -1
		if(recieved == self.NACK_IS_ALREADY_USED):
			self.Scope_Print("ID is already in use")
			return -1
	
	#Capture finger image
	def Capture_Finger(self):
		self.Send_Command(self.DEVICE_ID, self.CAPTURE_FINGER, 0)
		if(self.Recieve_Response() == self.NACK_FINGER_IS_NOT_PRESSED):
			self.Scope_Print("Finger is not pressed. Please try again")
			return -1
	
	#Enroll Finger print
	def Enroll(self, val):
		if(val == 1):		
			self.Send_Command(self.DEVICE_ID, self.ENROLL1, 0)
		elif(val == 2):
			self.Send_Command(self.DEVICE_ID, self.ENROLL2, 0)
		elif(val == 3):
			self.Send_Command(self.DEVICE_ID, self.ENROLL3, 0)
		else:
			self.Scope_Print("Invalid Parameter")
			return -1
		
		recieved = self.Recieve_Response()
		if(recieved == self.NACK_ENROLL_FAILED):
			self.Scope_Print("Failed to enroll finger. Please try again")
			return -1
		if(recieved == self.NACK_BAD_FINGER):
			self.Scope_Print("Bad finger read. Please try again")
			return -1
		if(self.PREV_NACK == 1):
			self.Scope_Print("Duplicate finger print as user: " + str(recieved))
			return -1
	
	#Identify a fingerprint
	def Identify(self):
		self.Send_Command(self.DEVICE_ID, self.IDENTIFY, 0)
		recieved = self.Recieve_Response()
		if(recieved == self.NACK_DB_IS_EMPTY):
			self.Scope_Print("Database is empty")
			return -1
		if(recieved == self.NACK_IDENTIFY_FAILED):
			self.Scope_Print("Unidentified User")
			return -1
		return recieved	
	
	#Delete a uself.ser by fingerprint
	def Delete_ID(self):
		user_id = self.Identify_User()
		self.Send_Command(self.DEVICE_ID, self.DELETE_ID, user_id) 
		if(self.Recieve_Response() == self.NACK_INVALID_POS):
			self.Scope_Print("Unable to delete ID")
			return -1
		self.Scope_Print("User ID " + str(user_id) + " has been deleted")
		return user_id
	
	#Delete all users
	def Delete_DB(self):
		self.Send_Command(self.DEVICE_ID, self.DELETE_ALL, 0)
		if(self.Recieve_Response() == self.NACK_DB_IS_EMPTY):
			self.Scope_Print("Database is already empty")
			return -1
		self.Scope_Print("Database has been cleared")
	
	
	#Enroll a new user
	def Enroll_New_User(self):
		#Turn the LED on so that the finger can be seen
		self.Led_On()
	
		#Ensure that finger is on scanner
		self.Scope_Print("Please place your finger on the scanner")
		
		#Wait for finger press
		self.Wait_For_Press()
	
		#Get the first unused ID	
		user_id = self.Get_Unused_ID()
	
		#EnrollStart with a (not used) ID 
		if(self.Enroll_Start(user_id) == -1):
			self.Led_Off()	
			return -1	
	
		#2. CaptureFinger
		if(self.Capture_Finger() == -1):
			self.Led_Off()	
			return -1	
	
		#3. Enroll1 
		if(self.Enroll(1) == -1):
			self.Led_Off()	
			return -1	
		
		#4. Wait to take off the finger using IsPressFinger 
		#Variable to hold whether a finger is pressed	
		self.Scope_Print("Please remove your finger...")
		self.Wait_For_Release()	
	
		self.Scope_Print("Thanks, now place your finger on the scanner again")
		self.Wait_For_Press()	
		
		#5. CaptureFinger 
		if(self.Capture_Finger() == -1):
			self.Led_Off()
			return -1	
		
		#6. Enroll2 
		if(self.Enroll(2) == -1):
			self.Led_Off()
			return -1	
		
		#7. Wait to take off the finger using IsPressFinger 
		self.Scope_Print("Please remove your finger...")
		self.Wait_For_Release()

		self.Scope_Print("Thanks, now place your finger on the scanner again")
		self.Wait_For_Press()
	
		#8. CaptureFinger 
		if(self.Capture_Finger() == -1):
			self.Led_Off()
			return -1	
		
		#9. Enroll3 
		if(self.Enroll(3) == -1):
			self.Led_Off()
			return -1	
		
		#Turn off the LED
		self.Led_Off()
		
		self.Scope_Print("Thank you for enrolling. You are now enrolled as user " + str(user_id))
		return user_id
	
	def Identify_User(self):
		#Turn the light on 
		self.Led_On()	
		
		#Ensure that finger is on scanner
		self.Scope_Print("Please place finger on scanner")	
		self.Wait_For_Press()
	
		#CaptureFinger 
		if(self.Capture_Finger() == -1):
			self.Led_Off()
			return -1	
	
		#Identify the uself.ser
		user_id = self.Identify()
		if(user_id == -1):
			self.Led_Off()
			return -1	
		
		#Turn the light on 
		self.Led_Off()	
	
		return user_id 
	
	def Download_Template(self, user_id):
		#user_id = self.Identify_User()
		self.Send_Command(self.DEVICE_ID, self.GET_TEMPLATE, user_id)
		recieved = self.Recieve_Response()
		if(recieved == self.NACK_INVALID_POS):
			self.Scope_Print("Invalid position in database")
			return -1
		if(recieved == self.NACK_IS_NOT_USED):
			self.Scope_Print("Invalid ID for template")
			return -1
		data = self.Recieve_Data(498)
		strdata = ''.join(map(str,data))
		print(strdata)	
		return strdata
 		#open the file to write to
		#f = open(file_name, 'w+')
		#for x in range(0, 498):
		#	f.write(str(data[x]) + " ")
		
	def Upload_Template(self, user_id, finger_print):
		#f = open(file_name, 'r')
		self.Send_Command(self.DEVICE_ID, self.SET_TEMPLATE, user_id)
		if(self.Recieve_Response() == self.NACK_INVALID_POS):
			self.Scope_Print("Invalid position in database")
			return -1
		print(finger_print)
		self.Send_Data(self.DEVICE_ID, finger_print)
		recieved = self.Recieve_Response()
		if(recieved == self.NACK_COMM_ERR):
			self.Scope_Print("Communication Error")
			return -1
		if(recieved == self.NACK_DEV_ERR):
			self.Scope_Print("Devce Error")
			return -1
	
	def Init(self):
		#Initialize the scanner
		self.Init_Scanner_Serial()

	def Test(self):
		self.Scope_Print("BEGINING TEST IN FINGERPRINT SCANNER")	
		while(True):
			input = raw_input('"e" to enroll\n"i" to identify\n"du" to delete user\n"d" to delete database\n"g" to get template\n"u" to upload template\n"q" to quit')
			while ((input != "e") & (input != "q") & (input != "i") & (input != "du") & (input != "d") & (input != "g") & (input != "u")):
				input = raw_input('"e" to enroll\n"i" to identify\n"du" to delete user\n"d" to delete database\n"g" to get template\n"u" to upload template\n"q" to quit')
			
			if input == "q":
				self.Scope_Print("Exiting...")
				sys.exit()
			elif input == "e":
				self.Scope_Print("Enrolling new user")	
				self.Enroll_New_User()
			elif input == "i":
				self.Scope_Print("Identifying user")
				self.Scope_Print("User ID: " + str(self.Identify_User()))
			elif input == "du":
				self.Scope_Print("Removing ID")
				self.Delete_ID()
			elif input == "d":
				self.Scope_Print("Deleting Database")
				self.Delete_DB()
			elif input == "g":
				self.Scope_Print("Getting Template")
				self.Download_Template('output.txt')
			elif input == "u":
				self.Scope_Print("Uploading Template")
				self.Upload_Template('output.txt')
			self.Scope_Print("Done")
	
		self.Close_Serial()

