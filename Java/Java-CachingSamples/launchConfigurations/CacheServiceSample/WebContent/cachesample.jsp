<%-- 

This sample program is provided AS IS and may be used, executed, copied and
modified without royalty payment by customer (a) for its own instruction and 
study, (b) in order to develop applications designed to run with an IBM 
WebSphere product, either for customer's own internal use or for redistribution 
by customer, as part of such an application, in customer's own products. "

5724-J34 (C) COPYRIGHT International Business Machines Corp. 2014
All Rights Reserved * Licensed Materials - Property of IBM

--%>
<%@ page import="java.util.Map" %>

<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="com.ibm.websphere.objectgrid.*" %>
<%@ page import="com.ibm.websphere.objectgrid.security.config.*" %>
<%@ page import="com.ibm.websphere.objectgrid.security.plugins.builtins.*" %>
<%!
Session ogSession;

public void jspInit() {
	
 		Map<String, String> env = System.getenv();
 		String vcap=env.get("VCAP_SERVICES");
 		
 		String username=null;
 		String password=null;
 		String endpoint=null;
 		String gridName=null;
 			
        boolean foundService=false;
        if(vcap==null) {
        	System.out.println("No VCAP_SERVICES found");
        } else {
            try {
            	JSONObject obj = new JSONObject(vcap);
                String[] names=JSONObject.getNames(obj);
                if (names!=null) {
					for (String name:names) {
                    	if (name.startsWith("DataCache")) {
             				JSONArray val = obj.getJSONArray(name);
             				JSONObject serviceAttr = val.getJSONObject(0);
             				JSONObject credentials = serviceAttr.getJSONObject("credentials");
             				username = credentials.getString("username");
             				password = credentials.getString("password");             							  
             				endpoint=credentials.getString("catalogEndPoint");
             				gridName= credentials.getString("gridName");
             				System.out.println("Found configured username: " + username);
             				System.out.println("Found configured password: " + password);
             				System.out.println("Found configured endpoint: " + endpoint);
             				System.out.println("Found configured gridname: " + gridName);		
             				foundService = true;
             				break;
             			}                                	                                 	                                 	                                         
                    }
				}
			} catch(Exception e) {}
 		}
        
 		if(!foundService) {
   			System.out.println("Did not find WXS service, using defaults");
 		}
 		
 		try {
 			
			ObjectGridManager ogm = ObjectGridManagerFactory
					.getObjectGridManager();
			ClientSecurityConfiguration csc=null;
			csc=ClientSecurityConfigurationFactory.getClientSecurityConfiguration();
			csc.setCredentialGenerator(new UserPasswordCredentialGenerator(username,password));
			csc.setSecurityEnabled(true);
			
			ClientClusterContext ccc = ogm
					.connect(endpoint, csc, null);
	
			ObjectGrid clientGrid = ogm.getObjectGrid(ccc, gridName);
			ogSession = clientGrid.getSession();
			
 		} catch(Exception e) {
 			System.out.println("Failed to connect to grid!");
 			e.printStackTrace();
 		}
}
%>

<%
		try {
			request.setCharacterEncoding("UTF-8");
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");
				
			// Use the getMap() method to return a ObjectMap obejct.
			// Once we have this object, we will be able to perform
			// key-value operations on the map.
			ObjectMap map=ogSession.getMap("sample.NONE.P");
			
		    String key = request.getParameter("key");			
			String operation=request.getParameter("operation");
			Object retrievedValue;
			if("get".equals(operation)) {
			
			  // Use the get() method from the ObjectMap to 
			  // retrive corresponding value using the given key.
			  retrievedValue=map.get(key);
			  response.getWriter().write(retrievedValue==null?"null":retrievedValue.toString());
			 
			} else if("put".equals(operation)) {
			  String newValue = request.getParameter("value");
			  
			  // Use the upsert() method from the ObjectMap to 
			  // update or insert key-value pair into the map.
			  // If key-value pair already exist, update operation
			  // will be performed instead of insert.
			  map.upsert(key,newValue);
			  
			  response.getWriter().write("[PUT]");
			  
			} else if("delete".equals(operation)) {
				
			  // Use the remove() method from the Object Map to 
			  // remove key-value pair from the map.
			  map.remove(key);			  
			  
			  response.getWriter().write("[DELETED]");			  
			}
			
		} catch(Exception e) {
			System.out.println("Failed to perform operation on map");
			e.printStackTrace();
		}
 %>
 