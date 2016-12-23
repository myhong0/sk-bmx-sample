/*

This sample program is provided AS IS and may be used, executed, copied and
modified without royalty payment by customer (a) for its own instruction and 
study, (b) in order to develop applications designed to run with an IBM 
WebSphere product, either for customer's own internal use or for redistribution 
by customer, as part of such an application, in customer's own products. "

5724-J34 (C) COPYRIGHT International Business Machines Corp. 2014
All Rights Reserved * Licensed Materials - Property of IBM
 
 */

package com.ibm.websphere.xs.sample.airport.domain;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Store Airport codes
 */
public class AirportCodes {

	 public final static String ALL_AIRPORT_CODE = "allCodesKey";
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	 SortedSet<String> allCodes = new TreeSet();
	 
	 // Code delimiter
	 static private String DELIMITER = ":";
	 
	 /**
	  * Add code
	  * 
	  * @param aCode
	  */
	 public void addCode(String aCode) {
		 
		 if (!allCodes.contains(aCode)) {
			 allCodes.add(aCode);
		 }
	 }

	 /**
	  * Remove code
	  * 
	  * @param aCode
	  */
	 public void removeCode(String aCode) {
		 
		 if (allCodes.contains(aCode)) {
			 allCodes.remove(aCode);
		 }
	 }
	 
	 /**
	  * Get all codes
	  * 
	  * @return
	  */
	 public SortedSet<String> getAllCodes() {
		 
		 return allCodes;
	 }
	 
	 /**
	  * To string
	  * 
	  */
	 public String toString() {
		 
		 String txt = "";
		 
		 if (allCodes != null) {
			 
			 Iterator<String> iter = allCodes.iterator();
			 int c = 1;
			 while (iter.hasNext()) {
				 
				 String aCode = iter.next();
				 if (c == 1) {
					 txt = txt + aCode;
					 c++;
				 } else {
					 txt = txt + DELIMITER + aCode;
				 }
			 }			 
		 }
		 
		 return txt;		 
	 }
	 
	 /**
	  * Load airport codes
	  * 
	  * @param codesString
	  * @return
	  */
	 static public AirportCodes load(String codesString) {
		 
		 AirportCodes apCodes = new AirportCodes();
		 
		 StringTokenizer tokens = new StringTokenizer(codesString, DELIMITER);
		 while (tokens.hasMoreTokens()) {
			 
			 String aCode = tokens.nextToken();
			 apCodes.addCode(aCode);
		 }
		 
		 return apCodes;
	 }
}
