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
 * This module provide functions to use WebSphere eXtreme Scale REST APIs to access Data Cache service
 * 
 */

var url = require('url');
var http = require('http');

function WXS(properties) {
    this.wxs = properties;
    this.cookieJar = {};
    this.cookieString = '';
    if (this.wxs.restResource.indexOf('http://') != 0) {
        this.wxs.restResource = 'http://' + this.wxs.restResource;
    }
    this.auth = 'Basic ' + new Buffer(this.wxs.username + ':' + this.wxs.password).toString('base64');
    this.parsed = url.parse(this.wxs.restResource);
    console.log("wxsclient url=" + this.wxs.restResource);
    console.log("wxsclient username=" + this.wxs.username);
}

WXS.prototype = {

    addCookie: function (cookie) {
      idx = cookie.indexOf('=');
      key = cookie.substring(0,idx)
      value = cookie.substring(idx+1);
      console.log('new cookie key='+key+' value='+value);
      newJar = {};
      for (var okey in this.cookieJar) {
          if (okey != key)
            newJar[okey]=this.cookieJar[okey];
      }
      this.cookieJar = newJar;
      this.cookieJar[key]=value;
      cookieString='';
      for (var key in this.cookieJar) {
        if (cookieString == '')
            cookieString=key+'='+this.cookieJar[key];
        else
            cookieString=cookieString+'; '+key+'='+this.cookieJar[key];
      }
      this.cookieString = cookieString;
    },

    processCookies: function(res) {
        wxsclient=this;
        if (Array.isArray(res.headers['set-cookie'])) {
          wxsclient.cookieString = "";
          res.headers['set-cookie'].forEach(function(cook) {
            rcook = cook.split(';')[0];
            wxsclient.addCookie(rcook);
          });
        } else if (res.headers['set-cookie'] != undefined) {
            cook = res.headers['set-cookie'];
            rcook = cook.split(';')[0];
            wxsclient.addCookie(rcook);
        }
    },

    put: function (key, value, callback) {
        var wxsclient = this;
        var post_options = {
            hostname: this.parsed.hostname,
            port: '80',
            path: this.parsed.pathname + '/' + this.wxs.gridName + '/' + encodeURIComponent(key),
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': this.auth,
                'Cookie': this.cookieString
            },
            rejectUnauthorized: false,
            agent: false,
        };
        var post_req = http.request(post_options, function (res) {
            res.setEncoding('utf8');
            res.on('data', function (chunk) {});
            res.on('error', function (c) {
                console.log('post error: ' + c);
            });
            res.on('end', function () {
                wxsclient.processCookies(res);
                callback();
            });
        });
        post_req.write(JSON.stringify(value));
        post_req.end();
    },

    get: function (key, callback) {
        var wxsclient = this;
        var get_options = {
            hostname: this.parsed.hostname,
            port: '80',
            path: this.parsed.pathname + '/' + this.wxs.gridName + '/' + encodeURIComponent(key),
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': this.auth,
                'Cookie': this.cookieString
            },
            rejectUnauthorized: false,
            agent: false,
        };
        var get_req = http.request(get_options, function (res) {
            var resultString = '';
            res.on('data', function (chunk) {        
                resultString += chunk;
            });
            res.on('error', function (c) {
                console.log('get error: ' + error);
            });
            res.on('end', function () {
                wxsclient.processCookies(res);
                if (res.statusCode == 200) {
                    callback(JSON.parse(resultString));
                } else {
                    callback(); //error case
                }
            });
        });
        get_req.end();
    },
    remove: function (key, callback) {
        var wxsclient = this;
        var get_options = {
            hostname: this.parsed.hostname,
            path: this.parsed.pathname + '/' + this.wxs.gridName + '/' + encodeURIComponent(key),
            port: '80',
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': this.auth,
                'Cookie': this.cookieString
            },
            rejectUnauthorized: false,
            agent: false,
        };
        var get_req = http.request(get_options, function (res) {
            var resultString = '';
            res.on('data', function (chunk) {        
                resultString += chunk;
            });
            res.on('error', function (c) {
                console.log('get error: ' + error);
            });
            res.on('end', function () {
                wxsclient.processCookies(res);
                callback();
            });
        });
        get_req.end();
    }
};

module.exports = WXS
