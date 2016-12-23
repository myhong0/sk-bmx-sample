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

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Object to store Airport data
 * 
 */
public class AirportData implements Serializable {
	
   private static final long serialVersionUID = -8743162287583194783L;

   private String code;
   private String city;
   private String country;
   private int numOfTerminals;
   private int numOfGates;

   /**
    * Creates a new AirportData object from the raw string containing the following data (separated by colon):
    * city
    * country
    * number of terminals - integer
    * number of gates - integer
    * If the raw string is invalid, this method will log a warning and return a null.
    * @param raw
    * @return
    */
   static public AirportData newAirportData(String raw) {
	   
      AirportData data = new AirportData();
      
      String object = "";
      try {
    	  
    	  String code = raw.substring(0, 3);
    	  // the third character is an equals sign
          object = raw.substring(4,raw.length());
          StringTokenizer st = new StringTokenizer(object, ":");
          
    	  data.setCode(code);
          data.setCity(st.nextToken());
          data.setCountry(st.nextToken());
          data.setNumOfTerminals(Integer.parseInt(st.nextToken()));
          data.setNumOfGates(Integer.parseInt(st.nextToken()));
          
      } catch (NoSuchElementException ex) {
         System.err.println("ERROR: Invalid airport data - data: " + object);
         data = null;
         
      } catch (NumberFormatException ex) {
         System.err.println("ERROR: Invalid airport data - data: " + object);
         data = null;
      }
      
      return data;
   }

   public String getCity() {
      return city;
   }
   public void setCity(String city) {
      this.city = city;
   }
   public String getCountry() {
      return country;
   }
   public void setCountry(String country) {
      this.country = country;
   }
   public int getNumOfTerminals() {
      return numOfTerminals;
   }
   public void setNumOfTerminals(int numOfTerminals) {
      this.numOfTerminals = numOfTerminals;
   }
   public int getNumOfGates() {
      return numOfGates;
   }
   public void setNumOfGates(int numOfGates) {
      this.numOfGates = numOfGates;
   }
   public String getCode() {
	   return code;
   }
   public void setCode(String code) {
	   this.code = code;
   }
   
   @Override
   public String toString() {
      return code + "=" + city + ":" + country + ":" + numOfTerminals + ":" + numOfGates;
   }
}
