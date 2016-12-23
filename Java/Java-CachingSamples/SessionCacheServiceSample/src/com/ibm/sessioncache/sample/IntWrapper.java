/*

This sample program is provided AS IS and may be used, executed, copied and
modified without royalty payment by customer (a) for its own instruction and 
study, (b) in order to develop applications designed to run with an IBM 
WebSphere product, either for customer's own internal use or for redistribution 
by customer, as part of such an application, in customer's own products. "

5724-J34 (C) COPYRIGHT International Business Machines Corp. 2014
All Rights Reserved * Licensed Materials - Property of IBM
 
 */

package com.ibm.sessioncache.sample;

import java.io.Serializable;

/**
 * This class serves as a wrapper for an int.
 * 
 * This class is used for testing that needs to check the system behavior when 
 * an application classloader only class is placed in the session.
 * 
 */
public class IntWrapper implements Serializable {
	
	private static final long serialVersionUID = 2982830170534040881L;	
	private int _i = 0;

	/**
	 * Constructor.
	 */
	public IntWrapper(int i) {
		_i = i;
	}
	
	/**
	 * Return int value
	 * 
	 * @return
	 */
	public int intValue() {
		return _i;
	}

	/**
	 * Increment the value by 1
	 * 
	 */
	public void incrementValue() {
		_i = _i+1;		
	}
	
	/**
	 * Return the value in String
	 * 
	 */
	public String toString() {
		return "("+_i+")";		
	}
}