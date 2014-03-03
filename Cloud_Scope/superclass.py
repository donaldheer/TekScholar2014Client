#import ConfigParser ((dont use 
import array
import sys
import telnetlib
import time
from sqlclass import sqlsettings
from telnetclass import TekScope

class sqltelscope:
    #class varibles
    netscope = TekScope()
    sqlscope = sqlsettings()
    def telconnect(self): #connect to scope over tenet
        self.netscope.setHost("192.168.2.10")
        self.netscope.setPort(4000)
        self.netscope.connect()
    def sqldlsettings(self,userid): # downoad sql settings from sql to local cfg file
        self.sqlscope.initialize(userid)
        #for output in self.sqlscope.scopesettings:
            #print "%s" % output
    def teluploadset(self): #upload concateintted files to scope over telnet
        print "/n"
    def teldownloadset(self): #retrive settings from scope and save to sql
        print "/n"
        
 
scope = sqltelscope()
#scope.telconnect()
scope.sqldlsettings('ripley')