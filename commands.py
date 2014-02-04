#
# WebDrive - Selenium 2 WebDriver support for play framework
#
# Copyright (C) 2011 Raghu Kaippully 
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import subprocess
import urllib2
import socket

from play.utils import *

COMMANDS = [ 'webdrive:test' , 'webdrive:remote' ]

HELP = {
    'webdrive:test': 'Run tests using Selenium 2 WebDriver'
}

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")

    if command == 'webdrive:test':
        test(app, args, False)
        
    if command == 'webdrive:remote':
        test(app, args, True)

def test(app, args, remote):
    app.check()

    # If framework-id is not a valid test-id, force it to 'test'
    if not isTestFrameworkId(app.play_env["id"]): 
        app.play_env["id"] = 'test'

    print "~ Running tests with webdriver"
    print "~ Ctrl+C to stop"
    print "~ "

    print "~ Deleting %s" % os.path.normpath(os.path.join(app.path, 'tmp'))
    if os.path.exists(os.path.join(app.path, 'tmp')):
        shutil.rmtree(os.path.join(app.path, 'tmp'))
    print "~"

    # Kill if exists
    http_port = 9000
    protocol = 'http'
    if app.readConf('https.port'):
        http_port = app.readConf('https.port')
        protocol = 'https'
    else:
        http_port = app.readConf('http.port')
    try:
        proxy_handler = urllib2.ProxyHandler({})
        opener = urllib2.build_opener(proxy_handler)
        opener.open('http://localhost:%s/@kill' % http_port)
    except Exception, e:
        pass

    # Run app
    test_result = os.path.join(app.path, 'test-result')
    if os.path.exists(test_result):
        shutil.rmtree(test_result)
    sout = open(os.path.join(app.log_path(), 'system.out'), 'w')
    java_cmd = app.java_cmd(args)
    try:
        play_process = subprocess.Popen(java_cmd, env=os.environ, stdout=sout)
    except OSError:
        print "Could not execute the java executable, please make sure the JAVA_HOME environment variable is set properly (the java executable should reside at JAVA_HOME/bin/java). "
        sys.exit(-1)
    soutint = open(os.path.join(app.log_path(), 'system.out'), 'r')
    while True:
        if play_process.poll():
            print "~"
            print "~ Oops, application has not started?"
            print "~"
            sys.exit(-1)
        line = soutint.readline().strip()
        if line:
            print line
            if line.find('Listening for HTTP') > -1:
                soutint.close()
                break

    # Run WebDriverRunner
    if (remote):
    
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("jenkins.wdstechnology.com", 80))
        ip_address = s.getsockname()[0]
    
        print "~~~~~~~~~~~~~~~~~~ WebDriver out of your house and running free on %s ~~~~~~~~~~ " %(app.readConf('webdrive.remoteUrl'),)
        print "~~~~~~~~~~~~~~~~~~ ET will phone home to %s ~~~~~~~~~~ " %(ip_address,)
    else :
        print "~~~~~~~~~~~~~~~~~~ WebDriver is in your house ~~~~~~~~~~~~~~~~~~ "

    wdcp = app.getClasspath()
    cp_args = ':'.join(wdcp)
    if os.name == 'nt':
        cp_args = ';'.join(wdcp)    
    java_cmd = [app.java_path(), '-classpath', cp_args,
    	'-Dwebdrive.classes=%s' % app.readConf('webdrive.classes'),
    	'-Dwebdrive.timeout=%s' % app.readConf('webdrive.timeout'), 
    	'-Dapplication.url=%s://localhost:%s' % (protocol, http_port),]
    	
    if (remote) :
        java_cmd += ['-Dwebdrive.remoteUrl=%s' % app.readConf('webdrive.remoteUrl'),]
        java_cmd += ['-Dwebdrive.remote.browsers=%s' % app.readConf('webdrive.remote.browsers'),]
        java_cmd += ['-Dwebdrive.this.port=%s' % app.readConf('http.port'),]
        java_cmd += ['-Dwebdrive.this.ipAddress=%s' % ip_address,]
    
    java_cmd += ['play.modules.webdrive.WebDriverRunner',]

   
    try:
        subprocess.call(java_cmd, env=os.environ)
    except OSError:
        print "Could not execute web driver."
        sys.exit(-1)

    print "~"

    # Kill if exists
    http_port = app.readConf('http.port')
    try:
        proxy_handler = urllib2.ProxyHandler({})
        opener = urllib2.build_opener(proxy_handler)
        opener.open('%s://localhost:%s/@kill' % (protocol, http_port))
    except Exception, e:
        pass
 
    if os.path.exists(os.path.join(app.path, 'test-result/result.passed')):
        print "~ All tests passed"
        print "~"
        testspassed = True
    if os.path.exists(os.path.join(app.path, 'test-result/result.failed')):
        print "~ Some tests have failed. See file://%s for results" % test_result
        print "~"
        sys.exit(1)
        