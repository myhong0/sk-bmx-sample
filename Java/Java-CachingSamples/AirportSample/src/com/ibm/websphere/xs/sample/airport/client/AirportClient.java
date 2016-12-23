/*

This sample program is provided AS IS and may be used, executed, copied and
modified without royalty payment by customer (a) for its own instruction and 
study, (b) in order to develop applications designed to run with an IBM 
WebSphere product, either for customer's own internal use or for redistribution 
by customer, as part of such an application, in customer's own products. "

5724-J34 (C) COPYRIGHT International Business Machines Corp. 2014
All Rights Reserved * Licensed Materials - Property of IBM
 
 */

package com.ibm.websphere.xs.sample.airport.client;

import java.io.IOException;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ConnectException;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridManager;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.TransactionCallbackException;
import com.ibm.websphere.objectgrid.security.config.ClientSecurityConfiguration;
import com.ibm.websphere.objectgrid.security.config.ClientSecurityConfigurationFactory;
import com.ibm.websphere.objectgrid.security.plugins.builtins.UserPasswordCredentialGenerator;
import com.ibm.websphere.xs.sample.airport.domain.AirportCodes;
import com.ibm.websphere.xs.sample.airport.domain.AirportData;

/**
 * This AirportClient class provides functions
 * to access Data Cache service
 *
 */
public class AirportClient {
   
   private String mapName = null;
   private ObjectGrid ivObjectGrid;
   
   private final static String dataServiceMapName = "sample.NONE.P";
   
   private final static String testGridName 	= "Airports";
   private final static String testCSEndpoints 	= "localhost:2809";
   private final static String testMapName 		= "AirportCodeInfo";
   
   /**
    * Connects to a remote data cache specified by the passed-in string, or
    * using VCAP environment variables if the passed-in string is null.
    * 
    * @param catalogServiceEndpoints
    * @return
    */
   public void connect(String catalogServiceEndpoints) throws ConnectException {
      
	  String username = null;
	  String password = null;
	  String endpoint = catalogServiceEndpoints;
	  String gridName = null;
	  
	  // If there is no CS end points given
      if (endpoint == null) {
    	  
    	  // Try to get connection information from environment
    	  java.util.Map<String, String> env = System.getenv();
    	  String vcap = env.get("VCAP_SERVICES");
    	  
    	  if (vcap != null) {    		  
    		  try {
    			  
    			  JSONObject obj = new JSONObject(vcap);
    			  String[] names = JSONObject.getNames(obj);
    			  
    			  if (names != null) {
    				  
  					for (String name:names) {
  						
                      	if (name.startsWith("DataCache")) {
                      		
               				JSONArray val = obj.getJSONArray(name);
               				JSONObject serviceAttr = val.getJSONObject(0);
               				JSONObject credentials = serviceAttr.getJSONObject("credentials");
               				username = credentials.getString("username");
               				password = credentials.getString("password");             							  
               				endpoint = credentials.getString("catalogEndPoint");
               				gridName = credentials.getString("gridName");  
               				mapName  = dataServiceMapName;

               				break;
               			}                                	                                 	                                 	                                         
                    }
  				 }
    		  } catch (JSONException e) {    			  
    			  System.err.println("Error reading VCAP Variables: " + vcap);
    		  }    		     		   		      		  
    	  } else {
    		  endpoint = testCSEndpoints;
    		  gridName = testGridName;
    		  mapName  = testMapName;
    	  }    	  
      } else {
    	  gridName = testGridName;
    	  mapName  = testMapName;
      }
      
      System.out.println("Endpoints: " + endpoint + ", Gridname: " + gridName + ", MapName: " + mapName);

      // Create an ObjectGridManager instance.
      ObjectGridManager ogm = ObjectGridManagerFactory.getObjectGridManager();
      
      // Create client security configuration objects
      ClientSecurityConfiguration csc = null;
      if (username != null) {
    	  csc = ClientSecurityConfigurationFactory.getClientSecurityConfiguration();
    	  csc.setCredentialGenerator(new UserPasswordCredentialGenerator(username,password));
    	  csc.setSecurityEnabled(true);
      }

      // Obtain a ClientClusterContext by connecting to data cache service's catalog server
      ClientClusterContext ccc = ogm.connect(endpoint, csc, null);

      // Obtain a distributed ObjectGrid using ObjectGridManager and providing
      // the ClientClusterContext.
      ObjectGrid og = ogm.getObjectGrid(ccc, gridName);
      ivObjectGrid = og;           
   }
   
   /**
    * Return true if the data cache is pre-loaded with airport information
    * 
    * @return
    */
   public boolean isMapPreloaded() {
	   
	   boolean isPreloaded = false;
	   
	   try {
		   
		   if (ivObjectGrid == null) {
			   connect(null);
		   }
	   
		   Session session = ivObjectGrid.getSession();
		   ObjectMap map = session.getMap(mapName);
	   
		   // Try to get the airport codes from the data cache.
		   // If it is available, the data cache is pre-loaded
		   String allCodeString = (String) map.get(AirportCodes.ALL_AIRPORT_CODE);
		   if (allCodeString != null && !"".equals(allCodeString)) {
			   isPreloaded = true;
		   }
	   
	   } catch (Exception e) {
		   isPreloaded = false;
	   }
	   	   
	   return isPreloaded;
   }
   
   /**
    * pre-load the data cache with airport information. 
    * 
    * @return
    * @throws ObjectGridException
    * @throws IOException
    */
   public AirportCodes preloadMap() throws ObjectGridException, IOException {
	   
	   AirportCodes apCodes = new AirportCodes();
	   
	   Properties ivData = new Properties();
	   ivData.load(this.getClass().getClassLoader().getResourceAsStream("WEB-INF/airport.props"));	   
	   
	   if (ivObjectGrid == null) {
		   connect(null);
	   }
		
	   // Create a session to the specified grid
	   Session sess = ivObjectGrid.getSession();
		
	   // Create a map to load data in to
	   ObjectMap map = sess.getMap(mapName);
	   
	   // Load the airport information into the map, entry by entry
	   for (Object key : ivData.keySet()) {
		   
		   String raw = key + "=" + ivData.getProperty((String) key);
		   AirportData data = AirportData.newAirportData(raw);
		   if (data != null) {
			   try {
				   
				   map.insert(key, data);
				   apCodes.addCode(key.toString());				 			   
				   
			   } catch (ObjectGridException e) {
				   
				   System.err.println("Problem inserting " + raw + " into the map. " + e.getClass().getName());
			   }
		   }
	   }
	   
	  return apCodes;
   }
   
   /**
    * Return the airport information of the given airport code
    * 
    * @param airportCode
    * @return
    * @throws ObjectGridException
    */
   public AirportData getAirportData(String airportCode) throws ObjectGridException {
     
      if (ivObjectGrid == null) {
         connect(null);
      }

      Session session = ivObjectGrid.getSession();
      ObjectMap map = session.getMap(mapName);
      Object o = map.get(airportCode);      

      AirportData data = null;
      if (o instanceof AirportData) {
         data = (AirportData) o;
      }
      
      return data;
   }
   
   /**
    * Add the given airport information to data cache
    * 
    * @param code
    * @param data
    * @throws ObjectGridException
    */
   public void addNewAirport(String code, AirportData data) throws ObjectGridException {      
      
      if (ivObjectGrid == null) {
         connect(null);
      }
      
      Session session = ivObjectGrid.getSession();
      ObjectMap map = session.getMap(mapName);
      
      map.insert(code, data);
   }
   
   /**
    * Update the given airport information in data cache
    * 
    * @param code
    * @param data
    * @throws ObjectGridException
    */
   public void updateAirport(String code, AirportData data) throws ObjectGridException {	   
      
      if (ivObjectGrid == null) {
         connect(null);
      }
      
      Session session = ivObjectGrid.getSession();
      ObjectMap map = session.getMap(mapName);
      
      map.update(code, data);
   }
   
   /**
    * Delete airport information corresponding the given airport code from data cache
    * 
    * @param code
    * @throws ObjectGridException
    */
   public void deleteAirport(String code) throws ObjectGridException {
      
      if (ivObjectGrid == null) {
         connect(null);
      }
      
      Session session = ivObjectGrid.getSession();
      ObjectMap map = session.getMap(mapName);
      map.remove(code);
   }
   
   /**
    * Add all airport codes into data cache as a single entry
    * 
    * @param allCodes
    * @throws ObjectGridException
    */
   public void addAllCodes(AirportCodes allCodes) throws ObjectGridException {	  
		
	  if (ivObjectGrid == null) {
		 connect(null);
	  }
		  
	  Session session = ivObjectGrid.getSession();
	  ObjectMap map = session.getMap(mapName);
		  
	  map.insert(AirportCodes.ALL_AIRPORT_CODE, allCodes.toString());
   }
   
   /**
    * Return all airport codes from data cache
    * 
    * @return
    * @throws TransactionCallbackException
    * @throws ObjectGridException
    */
   public SortedSet<String> getAllCodes() throws TransactionCallbackException, ObjectGridException {
	   
	  if (ivObjectGrid == null) {
	       connect(null);
	  }
	   
	  SortedSet<String> allCodes = null;
	   
	  Session session = ivObjectGrid.getSession();
	  ObjectMap map = session.getMap(mapName);
	   
	  String allCodeString = (String) map.get(AirportCodes.ALL_AIRPORT_CODE);
	  AirportCodes apCode = AirportCodes.load(allCodeString);
	  allCodes = apCode.getAllCodes();
	  if (allCodes == null) {
		  allCodes = new TreeSet<String>();
	  }
	 
	  return allCodes;
   }
}
