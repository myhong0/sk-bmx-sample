/*
 * This sample program is provided AS IS and may be used, executed, copied and
 * modified without royalty payment by customer (a) for its own instruction and 
 * study, (b) in order to develop applications designed to run with an IBM 
 * WebSphere product, either for customer's own internal use or for redistribution 
 * by customer, as part of such an application, in customer's own products. "
 * 
 * 5724-J34 (C) COPYRIGHT International Business Machines Corp. 2014
 * All Rights Reserved * Licensed Materials - Property of IBM 
 * 
 */

/**
 * 
 * This Node JS sample uses WebSphere eXtreme Scale REST APIs to put a counter in the Data Cache service
 * Every time this count is accessed, it is incremented by 1 and put back to the data cache
 * 
 */

// Import modules
var WXS = require('./wxs');
var express = require('express');
var app = express(); 

// Constants
var servicePrefix = "DataCache";

// Get environment info
var port = (process.env.VCAP_APP_PORT || 3000); 
var env = JSON.parse(process.env.VCAP_SERVICES);
var serviceEnvKey = null;
var wxsprops   = null;
var envDisplay = null;

// Loop through the environment variables and 
// find out the key to the service in the environment array
for (var aKey in env) {
	if (aKey.lastIndexOf(servicePrefix, 0) === 0) {
		serviceEnvKey = aKey;
		break;
	}
}
	
// If service is found, get credentials from there;
// otherwise, use dummy credentials
if (serviceEnvKey != null && env[serviceEnvKey] != null) {
	wxsprops = env[serviceEnvKey][0]['credentials'];			
} else {
	wxsprops = {"restResource":"","username":"","password":""};
}

//The WXS module provides functions that use WebSphere eXtreme Scale REST APIs 
//to access data cache		
var wxsclient = new WXS(wxsprops);
		
// Prepare service environment data for display
envDisplay = JSON.parse(JSON.stringify(env));
delete envDisplay[serviceEnvKey][0]['credentials'];
			
// Display data
console.log("Environment data...");
console.log(env); 
console.log("Using credentials...");
console.log(wxsprops);

// Define handler for requests
app.get('/', function(req, res) {
	
    var start = process.hrtime();
    
    // Get counter using counterkey
    wxsclient.get('counterkey', function(wxsres) {
        
    	var elapsed = process.hrtime(start)[1] / 1000000;
    	
    	// Update counter
        if (wxsres) {
          wxsres=wxsres+1
        } else {
          wxsres=1
        }
        var start2 = process.hrtime();
        
        // Use 'put' function from WXS module to put the updated counter back to data cache
        wxsclient.put('counterkey', wxsres, function() {
            var elapsed2 = process.hrtime(start2)[1] / 1000000;
            
            // Try to confirm the counter is updated in the data cache
            wxsclient.get('counterkey', function(getwxs) {
                if (wxsres == getwxs)
                	res.send('<pre>WebSphere eXtreme Scale Node JS sample application\nGet time: '+elapsed.toPrecision(3)+' ms\nPut time: '+elapsed2.toPrecision(3)+' ms\nCount: '+wxsres+'\nService Environment\n'+JSON.stringify(envDisplay,null,4));
                else
                    res.send('<pre>Cache failure</pre>',500);
                res.end();
            });
            console.log('get elapsed='+elapsed.toPrecision(3)+' put elapsed='+elapsed2.toPrecision(3));
        });
    });
});

// Define handler to test if server is available
app.get('/ping', function(req, res) {
    res.send('<pre>Express server is up and runing</pre>');
});

app.listen(port);
