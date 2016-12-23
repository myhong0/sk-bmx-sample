/*jshint node:true*/
var cfenv = require('cfenv');
var appEnv = cfenv.getAppEnv();
var dbCreds =  appEnv.getServiceCreds('laurenlandscapesdb');

var nano, prints;

if (dbCreds) {
	console.log('URL is ' + dbCreds.url);
	nano = require('nano')(dbCreds.url);
	prints = nano.use('prints');
} else {

	console.log('NO DB!');
}
	

// This application uses express as it's web server
// for more info, see: http://expressjs.com
var express = require('express');


var websiteTitle = require('./websitetitle');


// create a new express server
var app = express();
app.use(express.static('public'));
// serve the files out of ./public as our main files
app.set('view engine', 'jade');

app.get('/', function (req, res) {
	if (dbCreds) {
			prints.get('inventory', function(err, body) {
				if (!err) {
  					res.render('home', {title: websiteTitle.getTitle(), prints: body.landscapes});
  				} else {
  					console.error(err);
  				}
    		});
	} else  {
				res.render('home', {title: websiteTitle.getTitle(), prints: []});
	}
  	
	});

app.get('/printdisp', function (req, res) {
	prints.get('inventory', function(err, body) {
			if (!err) {
				var curprint = {};
				for(i=0, len=body.landscapes.length; i < len; i++ )  {
					if (body.landscapes[i].id == parseInt(req.query.id)) {
						curprint = body.landscapes[i];
						break;
					}
				} 
  				res.render('printdisp', {title: websiteTitle.getTitle(), print: curprint});
  			} else {
  				console.error(err);
  			}
    });
});



// start server on the specified port and binding host
app.listen(appEnv.port, appEnv.bind, function() {

	// print a message when the server starts listening
  console.log("server starting on " + appEnv.url);
});
