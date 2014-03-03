import sys
import MySQLdb as mdb
import telnetlib
import time
import datetime
#from sqlclass import sqlsettings
#from telnetclass import TekScope

#db4free
#username:jakegilbert
#password:fergus123

class SqlScope:
	host = "192.168.2.10"
	port = 4000
	
	def __init__(self):
		#self.connectScope()
		self.sqldb = sqlSettings()
	
	def connectScope(self):
		try:
            		self.tn = telnetlib.Telnet(self.host,self.port)
            		return self.tn.read_until("\n>", 1)
        	except:
            		print "ERROR (connectScope): Unable to communicate with device"

	def writeScope(self, command):
		try:
			self.tn.write(command.encode('ascii')+ "\r\n".encode('ascii'))
			time.sleep(.1)
        	except:
            		print "ERROR (writeScope): Unable to communicate with device"
		
	def readScope(self):
		return self.tn.read_until(">", 1).replace("\n\r>", "")

	def closeScope(self):
		self.tn.close()

	def parseSetCommand(self):
		self.connectScope()
		self.writeScope('Set?')
			
		setStr = self.readScope()
		self.writeScope('MESSAGE:SHOW "Saving profile to Cloud Scope Network"')
		self.writeScope("MESSAGE:STATE ON")
		self.closeScope()	
		print(str(setStr + "\n\n\n\n"))	
		setArray = setStr.replace(" :SET *RST;:", "").split(";:")#array of all commands
		print(str(setArray) + "\n\n\n\n")
		finalSetArray = []
 		for index, item in enumerate(setArray):
                        item = item.split(";")
                        _cmd = ''
			print(item)  
                        for _index, _item in enumerate(item):
                                for ind, sub in enumerate(_item.split(":")):
                                        if ind is not (len(_item.split(":")) - 1):
                                                if sub is not ":" + _cmd:
                                                        _cmd = _cmd + ":" + sub
						print(_cmd)
                                _item = _cmd + ":" + _item.split(":")[len(_item.split(":")) - 1]
                                finalSetArray.append(_item.replace("\n\r", ""))
                for index, item in enumerate(finalSetArray):
                        finalSetArray[index] = item.split(" ")
                return finalSetArray		

#PUBLIC
	def uploaddefault(self):
		finalSetArray = parseSetCommand()
		for item in finalSetArray:
			self.sqldb.cur.execute("""
				INSERT INTO `scopedefault` (`id`, `command`, `value`) 
				VALUES (NULL, %s, %s)
			""", (item[0], item[1]))
			self.sqldb.con.commit()

	def concatinateSet(self):#pi->pi
		self.sqldb.cur.execute("SELECT `command` , `value` FROM `scopedefault`")
		sqlArray = self.sqldb.sqlRead()
		setArray = self.parseSetCommand()
		differenceArray = set(set(tuple(map(tuple,setArray)))).difference(sqlArray)
		return differenceArray
#IGNORE
	def dlUserToScope(self, user, profile):
		self.sqldb.cur.execute("""
			SELECT `id`
			FROM `scopeusers`
			WHERE `name` = %s
		""", user)
		#userids = self.sqldb.sqlRead()
		
		#print userids[0][0]
		self.dlToScope(self.sqldb.sqlRead()[0][0], profile)
#Download Settings to scope from specified user
#Arguments:
#	userid: id of user settings to be downloaded
#	profile: profile number to be downloaded

	def dlToScope(self, userid, profile):#pi->scope
		self.sqldb.cur.execute("""
			SELECT `command`, `value`
			FROM `scopesettings`
			WHERE `userprofile` = %s
			AND `userid` = %s
		""", (int(profile), int(str(userid))))
		differenceArray = self.sqldb.sqlRead()
		differenceList = list(differenceArray)
		print(str(differenceList))	
		try:
			differenceList.remove((':', ':SET'))
		except:
			print "Fuck you"
		try:
			differenceList.remove((':', '*RST'))
		except:
			print "No Fuck you"
		try:
			differenceList.remove(('::SET', '*RST'))
		except:
			print "Not in list?" 
		#differenceList.reverse()
		telnetCmd = ''
		for item in differenceList:
			print item
			item = item[0] + " " + item[1]
			telnetCmd = telnetCmd + ";" + item
		self.connectScope()
		self.writeScope('Factory')
		self.writeScope(telnetCmd)
		self.closeScope()
		print telnetCmd
#IGNORE
	def upUserFromScope(self, user, profile):
		self.sqldb.cur.execute("""
			SELECT `id`
                        FROM `scopeusers`
                        WHERE `name` = %s
                """, user)
		self.upFromScope(self.sqldb.sqlRead()[0][0], profile)

#Upload Settings from scope from specified user
#Arguments:
#       userid: id of user settings to be uploaded
#       profile: profile number to be uploaded
	
	def upFromScope(self, userid, profile):
		differenceArray = list(self.concatinateSet())
		self.sqldb.cur.execute("""
			DELETE FROM `scopesettings`
			WHERE `userprofile` = %s
			AND `userid` = %s
		""", (int(profile), int(userid)))
		self.sqldb.con.commit()
		for item in differenceArray:
			print item
			try:
				self.sqldb.cur.execute("""
					INSERT INTO `scopesettings` (`id`, `userid`, `command`, `value`, `userprofile`)
					VALUES (NULL, %s, %s, %s, %s)
				""", (userid, item[0], item[1], profile))
			except:
				differenceArray.remove(item)
				print "one of those stupid commands popped up and this was the easy way out, hehe"
			self.sqldb.con.commit()
	
#One time use to populate user database
	def  populateUsers(self):
		for number in range(0,200):
			self.sqldb.cur.execute("""
				INSERT INTO `scopeusers`
				(`id`, `userid`, `f_print`, `timestamp`)
				VALUES (NULL, %s, %s, %s)
			""", ((number), "0", datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')))
			self.sqldb.con.commit()

#UpdateFingerprint
#Arguments:
#	userid: id location for fingerprint to be saved
#	fingerprint: fingerprint template
   	def updateFingerprint(self, userid, fingerprint):
		print userid
		print fingerprint
		self.sqldb.cur.execute("""
				UPDATE `scopeusers`
				SET `f_print` = %s, `timestamp` = %s
				WHERE `userid` = %s
		""", (fingerprint, datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'), userid))
		self.sqldb.con.commit()		

#UpdateLocalDatabase: returns an array of all fingerprints and ids  updated since the given timestamp
#Arguments
#	timestamp (use: datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
#Returns:
#	tuple of fingerprint id's
#		access like this:
#		userid = updatedArray[0][0]
#		fingerprint = updatedArray[0][1]
	def updateLocalDatabase(self):
		self.sqldb.cur.execute("""
				SELECT `userid`, `f_print`
				FROM `scopeusers`
				WHERE `f_print` <> %s
		""", "0")
		updatedArray = self.sqldb.sqlRead()
		return updatedArray

#Clear Fingerprints
	def clearFingerprintDatabase(self):
		self.sqldb.cur.execute("""
				UPDATE `scopeusers`
				SET `f_print` = %s
				WHERE `f_print` <> %s
		""", ("0", "0"))
		self.sqldb.con.commit()
				
	def figerprintfind():
		print "fingerprintfind"
    #this is to find a user by a a fingerprint useing brandons library
    
#place all commands into sql database
    
class sqlSettings:
	def __init__(self):
        	self.con = mdb.connect(host="engr-db.engr.oregonstate.edu",port=3307,user="collaboratory",passwd="WizyZu6K",db="collaboratory");
		self.cur = self.con.cursor()
        def sqlRead(self):
        	sqlreturn = self.cur.fetchall()
        	return sqlreturn
		
 
