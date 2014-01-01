import RPi.GPIO as GPIO
import threading
import time
import fcntl
import socket
import struct
import os
from tekScopeSql import SqlScope
from telnetclass import TekScope
from FingerPrintScanner import Scanner
from Bluetooth import Bluetooth

#Button Input Variables
GPIO.setmode(GPIO.BCM)
GPIO.setup(2,GPIO.IN)
GPIO.setup(3,GPIO.IN)
GPIO.setup(4,GPIO.IN)
GPIO.setup(17,GPIO.IN)
prevA = 1                                         
prevB = 1
prevC = 1
prevD = 1

#Menu variables
Menu = ["Enroll User", "Delete User", "Validate User", "Update Profile", "Clear Database", "IP"]
Menu_Index = 0
Menu_Active = 0
Window_Width = 1023
Window_Height = 767
menu_time = 0
#Tekscope initialization
netscope = TekScope()
netscope.setHost("192.168.2.10")
netscope.setPort(4000)
netscope.connect()

#Scanner initialization
scanner = Scanner()
scanner.Init()

#SQL scope initialization
sqlscope = SqlScope()

#Bluetooth Initialization
bluetooth = Bluetooth()

def DisplayMessage(message, width, height):
	global menu_time
	print(message)
	menu_time = 0	
	window_x_1 = (Window_Width / 2) - (width / 2)
	window_y_1 = (Window_Height / 2) - (height / 2)
	window_x_2 = window_x_1 + width
	window_y_2 = window_y_1 + height
	netscope.Write('MESSAGE:SHOW "'+message+'"')
	#netscope.Write("MESSAGE:BOX " + str(window_x_1) + "," + str(window_y_1) + "," + str(window_x_2) + "," + str(window_y_2)) 
	#netscope.Write("MESSAGE:BOX " + str(window_x_1) + "," + str(window_y_1))
	netscope.Write("MESSAGE:STATE ON")

def ClearMessage():
	global Menu_Active	
	netscope.Write("MESSAGE:STATE OFF")
	netscope.Write('MESSAGE:SHOW ""')
	Menu_Active = 0

def DisplayMenu():
	index = 0
	string = "USER MENU:\n"
	while index < len(Menu):
		if(Menu_Index == index):
			string += "    " + Menu[index] + "\n"
		else:
			string += Menu[index] + "\n"
		index += 1
	DisplayMessage(string, 150, 60)

def Get_IP(ifname):
	s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	return socket.inet_ntoa(fcntl.ioctl(s.fileno(), 0x8915, struct.pack('256s', ifname[:15]))[20:24])
 
def Initialize():
	DisplayMessage("Finger print scanner initializing", 120, 40)
	time.sleep(2) 
	ip = Get_IP('wlan0') 
	DisplayMessage("IP: " + str(ip), 150, 40) 
	time.sleep(1)	

def Enroll_New_User():
		#Turn the LED on so that the finger can be seen
		scanner.Led_On()
	
		#Ensure that finger is on scanner
		DisplayMessage("Please place your finger on the scanner", 100, 40)
		
		#Wait for finger press
		scanner.Wait_For_Press()
	
		#Get the first unused ID	
		user_id = scanner.Get_Unused_ID()
	
		#EnrollStart with a (not used) ID 
		if(scanner.Enroll_Start(user_id) == -1):
			scanner.Led_Off()	
			return -1	
	
		#2. CaptureFinger
		if(scanner.Capture_Finger() == -1):
			scanner.Led_Off()	
			return -1	
	
		#3. Enroll1 
		if(scanner.Enroll(1) == -1):
			scanner.Led_Off()	
			return -1	
		
		#4. Wait to take off the finger using IsPressFinger 
		#Variable to hold whether a finger is pressed	
		DisplayMessage("Please remove your finger...", 100, 40)
		scanner.Wait_For_Release()	
	
		DisplayMessage("Thanks, now place your finger on the scanner again", 120, 40)
		scanner.Wait_For_Press()	
		
		#5. CaptureFinger 
		if(scanner.Capture_Finger() == -1):
			scanner.Led_Off()
			return -1	
		
		#6. Enroll2 
		if(scanner.Enroll(2) == -1):
			scanner.Led_Off()
			return -1	
		
		#7. Wait to take off the finger using IsPressFinger 
		DisplayMessage("Please remove your finger...", 100, 40)
		scanner.Wait_For_Release()

		DisplayMessage("Thanks, now place your finger on the scanner again", 120, 40)
		scanner.Wait_For_Press()
	
		#8. CaptureFinger 
		if(scanner.Capture_Finger() == -1):
			scanner.Led_Off()
			return -1	
		
		#9. Enroll3 
		if(scanner.Enroll(3) == -1):
			scanner.Led_Off()
			return -1	
		
		#Turn off the LED
		scanner.Led_Off()
		
		DisplayMessage("    Thank you for enrolling.   \n You are now enrolled as user " + str(user_id), 100, 80)
		print("USER ID: " + str(user_id))
		#Get the fingerprint template
		sqlscope.updateFingerprint(str(user_id), scanner.Download_Template(user_id))
		
		ClearMessage()		
		#Upload the current settings	
		sqlscope.upFromScope(str(user_id), "0")
		ClearMessage()	


		return user_id

def Refresh_Database():
	scanner.Delete_DB()
	data = sqlscope.updateLocalDatabase()  
	for x in data:
		print x[0]
		print map(ord, list(x[1]))
		scanner.Upload_Template(int(x[0]), map(ord, list(x[1])))
	
#*** MAIN ***

Initialize()
while True:
#	Refresh_Database()
#	Up Button
	if GPIO.input(2) != prevA:
		Menu_Active = 1	
		if GPIO.input(2) == 0:
			if(Menu_Index == len(Menu) - 1):
				Menu_Index = 0
			else:
				Menu_Index += 1
			DisplayMenu()	
			#DisplayMssage(Menu[Menu_Index])

		prevA = GPIO.input(2)
#	#Down Button
	elif GPIO.input(3) != prevB:
		Menu_Active = 1
		if GPIO.input(3) == 0:
			if(Menu_Index == 0):
				Menu_Index = len(Menu) - 1 
			else:
				Menu_Index -= 1
			DisplayMenu()	
			#DisplayMessage(Menu[Menu_Index])	
	
		prevB = GPIO.input(3)
#	#Select Button
	elif GPIO.input(4) != prevC:
		print(Menu_Active)	
		if GPIO.input(4) == 0:
			#Select Button Pressed
			if(Menu_Active == 0):
				Menu_Active = 1
				DisplayMenu()
			else:
				Refresh_Database()
				if(Menu_Index == 0):	
					user_id = Enroll_New_User()	
					if(user_id == -1):
						DisplayMessage("Failed to enroll new user", 100, 40)
										
				elif(Menu_Index == 1):
					DisplayMessage("Scan finger to remove", 100, 40)
					user_id = scanner.Delete_ID()
					if(user_id == -1):
						DisplayMessage("Failed to delete user", 100, 40)
					else:
						DisplayMessage("User " + str(user_id) + " has been removed", 150, 40)

				elif(Menu_Index == 2): 
					DisplayMessage("Scan finger to identify", 100, 40)
					user_id = scanner.Identify_User() 
					if(user_id == -1): 
						DisplayMessage("Failed to identify user", 100, 40) 
					else: 
						DisplayMessage("Identified as user " + str(user_id), 150, 40)
			
				elif(Menu_Index == 3):
					DisplayMessage("Scan finger to identify", 100, 40)
					user_id = scanner.Identify_User()
					if(user_id == -1):
						DisplayMessage("Failed to identify user", 100, 40)
					else:
						DisplayMessage("Update user settings", 100, 40)	
						ClearMessage()	
						#Upload the current settings	
						sqlscope.upFromScope(str(user_id), "0")
						ClearMessage()	
				
				elif(Menu_Index == 4):
					DisplayMessage("Killing fingerprint database", 100, 40)
					scanner.Delete_DB()

				elif(Menu_Index == 5):
					ip = Get_IP('wlan0') 
					DisplayMessage("IP: " + str(ip), 150, 40) 
				
				Menu_Active = 0

		prevC = GPIO.input(4)

#	#Scan Button
	elif GPIO.input(17) != prevD:
		if GPIO.input(17) == 0:
			#Button D was pressed
			Refresh_Database()
			DisplayMessage("Please scan your finger...", 100, 40)
			user_id = scanner.Identify_User()		
			if(user_id == -1):
				DisplayMessage("Invalid fingerprint", 100, 40)
			else:
				DisplayMessage("Your user ID is " + str(user_id), 100, 40)
				netscope.close()	
				sqlscope.dlToScope(str(user_id), "0")
				netscope.connect()
		else:
			prevD = GPIO.input(17)
	
	#Watch for inactivity
	else:
		time.sleep(0.1)
		if(menu_time == 30):
			menu_time += 1
			ClearMessage()
		elif(menu_time < 30):
			menu_time += 1

	#Check for bluetooth commands and pipe them to scope
	val = bluetooth.Recieve_Data()
	if str(val) != "":
		netscope.Write(str(val))

	#Secret button commands
	#Restart device
	shutdown_time = 5 
	while(GPIO.input(3) == 0 and GPIO.input(2) == 0):
		DisplayMessage("Hold to shut down in " + str(shutdown_time), 100, 40)
		time.sleep(1)
		shutdown_time -= 1
		if(shutdown_time == 0):
			DisplayMessage("Rebooting...", 100, 40)
			time.sleep(1)
			ClearMessage()	
			os.system("reboot")
			
