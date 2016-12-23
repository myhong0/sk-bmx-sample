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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;

public class RESTProvider  {
	
	protected String resource;
	protected String username;
	protected String password;
	protected String gridName;
	protected String mapName;
	protected String clientProps;
	protected String traceSpec;

	private String authString = null;
	private ThreadLocal<HashMap<String,String>> cookies = new ThreadLocal<HashMap<String,String>>();
	private ThreadLocal<Integer> timeToLive = new ThreadLocal<Integer>();

	public RESTProvider(String resource,String username,String password,String mapName) {
		
		this.resource = resource;
		this.username = username;
		this.password = password;
		this.mapName = mapName;
		authString = ConnectionUtils.getBasicAuthenticationString(username, password);
	}

	public void threadInit() {
		
 	    if (cookies.get() == null) {
			cookies.set(new HashMap<String,String>());
 	    }
		timeToLive.set(new Integer(-1));
	}
	
	public Object get(String key) throws Exception {
		
		URL url = getCacheURL(key);
		byte result[] = ConnectionUtils.getData(url,authString,cookies.get());
		if (result != null){
			ObjectInputStream ois=new ObjectInputStream(new ByteArrayInputStream(result));
			ois.readShort();
			return ois.readObject();			
		}
		return null;
	}

	public void insert(String key, Object value) throws Exception {
		
		URL url = getCacheURL(key);
		if (timeToLive.get() > 0)
			url = new URL(url.toExternalForm()+"?ttl="+timeToLive.get());
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		ObjectOutputStream oos=new ObjectOutputStream(baos);
		oos.writeShort(0);
		oos.writeObject(value);
        byte[] result = baos.toByteArray();
		int status = ConnectionUtils.postData(url,authString,cookies.get(),result);
		if (status != 200 && status!=201 && status != 204) {
			throw new IllegalStateException("unexpected status inserting key="+key+" status="+status);
		}
	}

	public void update(String key, Object value) throws Exception {
		
        insert(key,value);
    }
	
	public void remove(String key) throws Exception {
		
		URL url = getCacheURL(key);
		int status = ConnectionUtils.deleteData(url,authString,cookies.get());
		if (status != 204 && status!=200) {
			throw new IllegalStateException("unexpected status inserting key="+key+" status="+status);
		}
	}

	public boolean isConnected() {
		
		return true;
	}
	
	private URL getCacheURL(String key) throws Exception {
		
		return new URL(resource+"/"+mapName+"/"+key);
	}
	
	public void setTimeToLive(int ttl) throws Exception {
		
		timeToLive.set(new Integer(ttl));
	}
}

