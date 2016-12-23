/*
 * 
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
package com.ibm.cachesample;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;

public class ConnectionUtils {

	static Logger log = Logger.getLogger(ConnectionUtils.class.getName());
	private static final String CLASS_NAME = ConnectionUtils.class.getCanonicalName();

	public static int postData(URL url, String authString, HashMap<String,String> cookies, byte [] data) throws IOException {
		
		String sourceMethod = "postData";
		int status = 400;
		HttpURLConnection conn =null;
		if (url.getProtocol().equals("https")){
			conn=createSecureConnectionObject(url);			
		} else {
			conn=(HttpURLConnection)url.openConnection();			
		}
		conn.setRequestMethod("POST");
		if (authString != null) {
			conn.setRequestProperty("Authorization", "Basic "+ authString);
		}
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setReadTimeout(5 * 1000);
		setCookies(conn,cookies);

		if ( data != null ) {
			conn.setRequestProperty("Content-Type", "application/octet-stream");
			java.io.OutputStream os = conn.getOutputStream();
			os.write(data);
			os.close();			
		}		
		status = conn.getResponseCode();
		flushData(conn);
		rememberCookies(conn,cookies);
		return status;
	}
		
	public static int deleteData(URL url, String authString,HashMap<String,String> cookies) throws IOException {
		
		String sourceMethod = "postData";
		int status = 400;

		HttpURLConnection conn =null;
		if (url.getProtocol().equals("https")){
			conn=createSecureConnectionObject(url);		
		}
		else {
			conn=(HttpURLConnection) url.openConnection();
		}

		conn.setRequestMethod("DELETE");
		if (authString != null)
			conn.setRequestProperty("Authorization", "Basic "+ authString);
		conn.setUseCaches(false);		
		conn.setReadTimeout(5 * 1000);
		setCookies(conn,cookies);
		status = conn.getResponseCode();
		flushData(conn);
		rememberCookies(conn,cookies);
		return status;
	}
	
	public static byte [] getData(URL url, String authString,HashMap<String,String> cookies) throws IOException {
		
		String sourceMethod = "getDataUrlConnection";
		int status = 400;

		HttpURLConnection conn =null;
		if (url.getProtocol().equals("https")){
			conn=createSecureConnectionObject(url);		
		} else {
			conn=(HttpURLConnection)url.openConnection();
		}
		conn.setRequestMethod("GET");
		if (authString !=null) {
			conn.setRequestProperty("Authorization", "Basic "+ authString);
		}
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setReadTimeout(5 * 1000);
		setCookies(conn,cookies);

		status = conn.getResponseCode();

		byte [] result = null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
		byte b[] = new byte[8192];
		InputStream is;
		if (status == 200)
			is = conn.getInputStream();
		else
			is = conn.getErrorStream();

		
		int i;
		while ( (i = is.read(b,0,b.length)) != -1 ) {
				baos.write(b,0,i);
		}
		if (status == 200)
			result = baos.toByteArray();
		is.close();

		rememberCookies(conn,cookies);
		return result;
	}

	private static void flushData(HttpURLConnection conn) {
		
		try {
		   InputStream is = conn.getInputStream();
		   while (is.read()!=-1);			
		} catch (Exception ex) {			
		}
	}
	
	private static void setCookies(HttpURLConnection conn,HashMap<String,String> cookies) {
		
		if (cookies.size()>0) {
			String cookie="";
			Iterator<String> it = cookies.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				String val = cookies.get(key);
				cookie=cookie+key+"="+val;
				if (it.hasNext())
					cookie+=";";
			}
			conn.setRequestProperty("Cookie",cookie);
		}	
	}

	private static void rememberCookies(HttpURLConnection conn,HashMap<String,String> cookies) {
		
		String header;
		int i=0;
		int k=0;
		while ((header=conn.getHeaderField(i))!=null) {
			String ikey = conn.getHeaderFieldKey(i);
			if (ikey != null && ikey.equals("Set-Cookie")) {
				k++;
				String cooks[] = header.split(";");
				for (int j=0;j<cooks.length;j++) {
					String cook = cooks[j];
					int index=cook.indexOf("=");
					if (index>-1){
						String parts[] = {cook.substring(0,index),cook.substring(index+1,cook.length())};			
						String key = parts[0].trim();
						String val = parts[1].trim();
						if (!key.equals("Path")&&!key.equals("Expires")) {
							//System.out.println("cookie: "+key+" = "+val);
							cookies.put(key,val);
						}						
					}
				}
			}
			i++;
		}

		if (i==0){
			System.err.println("no cookies in header");
		}

	}

	private static HttpURLConnection createSecureConnectionObject(URL url) throws IOException {

		if(log.isLoggable(Level.FINER)) {
			log.entering(CLASS_NAME, "createSecureConnectionObject", url);
		}

		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		try
		{
			conn.setConnectTimeout(75000); 				
			conn.setSSLSocketFactory(getSSLContext().getSocketFactory());
			conn.setHostnameVerifier(getHostnameVerifier());
		}
		catch (Exception e){
			IOException io= new IOException();
			io.initCause(e);
			throw io;
		}
		return conn;
	}

	static SSLContext _sslcontext = null;
	private static synchronized SSLContext getSSLContext() throws KeyManagementException, NoSuchAlgorithmException {
		
		if (_sslcontext == null) {
			_sslcontext = SSLContext.getInstance("TLS");
			_sslcontext.init(null, new AcceptAllTrustManager[]{new AcceptAllTrustManager()}, new SecureRandom());

		}
		return _sslcontext;	      
	}

	private static HostnameVerifier _hostNameVerifier= null;
	private static synchronized HostnameVerifier  getHostnameVerifier() {
		
		if (_hostNameVerifier == null) {
			// install an all accepting Host Verifier
			_hostNameVerifier = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					System.out.println("verifying host="+hostname+" session="+session);
					return true ;
				}
			};
		}
		return _hostNameVerifier;
	}

	public static String getBasicAuthenticationString(String userId, String password) {
		
		String authString = userId+":"+password;		
		
		try {
			return DatatypeConverter.printBase64Binary(authString.getBytes("ISO8859_1"));			
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		}
		return DatatypeConverter.printBase64Binary(authString.getBytes());			
		
	}

	public static class AcceptAllTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
		throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
		throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}
}