
import MySQLdb as mdb
import ConfigParser
import array
from telnetclass import TekScope
import sys

class sqlsettings(object):
    global scopesettings
    scopesettings = [[],[]]
    userid = "2"
    def __init__(self):
        self.con = mdb.connect("db4free.net","jakegilbert","fergus123","pythonbd");
        self.cur = self.con.cursor()
    def sqlwrite(self,command):
        return self.cur.execute(command)
    def sqlread(self):
        sqlreturn = self.cur.fetchall()
        return sqlreturn
    def dldefault(self): #dowlaod default settings list to config file.used only whenirst time or updating default list
        try:
            self.sqlwrite("SELECT scopedefault.command,scopedefault.value FROM scopedefault")
            self.sqldefault = self.sqlread()
	    return self.sqldefault
            #for defa in self.sqldefault:    
                #print "%s %s" % defa
        except:
            print "unable to download defaults"
    def dlsettings(self, userid): #download settings list from sql and compare to default list for final list, needs paramenter for witch user to get
        try:
            self.sqlwrite("SELECT scopesettings.command, scopedefault.value FROM scopesettings LEFT OUTER JOIN scopedefault ON scopedefault.command = scopesettings.command where scopesettings.userid ='"+ userid +"'")
            self.sqlsetting = self.sqlread()
            return self.sqlsetting
		#for row in self.sqlsetting:    
                #print "%s %s" % row
        except:
            print "unable to download settings"

    def concatinate(self):
        try:
            for y in range(1,len(self.sqldefault)+1):
                defcom = [x for x in self.sqldefault if "command"+str(y) in x]
                setcom = [z for z in self.sqlsetting if "command"+str(y) in z]
                if setcom:
                    global scopesettings
                    scopesettings[y-1].append([setcom[0][0],setcom[0][1]])
                else:
                    scopesettings.append([defcom[0][0],defcom[0][1]])
            #for output in scopesettings:
                #print "%s" % output
            self.clearss()
        except Exception as inst:
            print ("Exception: ", sys.exc_info()[0])
            print inst.args
        self.listclear(defcom,setcom)

    def listclear(self,defcom,setcom):
        del self.sqldefault
        del self.sqlsetting
        del defcom[:]
        del setcom[:]
    
    def clearss(self):
        global scopesettings
        scopesettings =[[],[]]

    def close(self):
        self.con.close()
    
    def uplsettings(self,command,value):
            sql_cmd = "UPDATE `pythonbd`.`scopesettings` SET `value` = '" + value + "' WHERE `scopesettings`.`userid` = '" + self.userid + "' and `scopesettings`.`command` = '" + command + "'"
            print self.sqlwrite(sql_cmd)
            self.con.commit()
        
    def initialize(self,id): #run dldefault,dlsetting,and concatinate to simplify the start up
        self.dldeafult()
        self.setuserid(id)
        self.dlsettings()
        self.concatinate()
            
    def setuserid(self,id):
        self.userid=id
    
    def userexsist(self,id): #checks to see if a user id exisit in the settings and users tables
        self.sqlwrite("SELECT scopeusers.f_print FROM scopeusers WHERE scopeusers.name='" + id +"'")
        returned = self.sqlread()
        if returned <> "()":
            return "user exists"
        else:
            return "user does not exists"
        
sql= sqlsettings()
#sql.initialize('ripley')
sql.uplsettings('command1','hi')
#sql.clearss()
#sql.initialize('ripley')
#sql.userexsist("ripley")
sql.close()
