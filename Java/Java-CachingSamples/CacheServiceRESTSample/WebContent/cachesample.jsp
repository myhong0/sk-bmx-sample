<%-- 

This sample program is provided AS IS and may be used, executed, copied and
modified without royalty payment by customer (a) for its own instruction and 
study, (b) in order to develop applications designed to run with an IBM 
WebSphere product, either for customer's own internal use or for redistribution 
by customer, as part of such an application, in customer's own products. "

5724-J34 (C) COPYRIGHT International Business Machines Corp. 2014
All Rights Reserved * Licensed Materials - Property of IBM

--%>
<%@page import="com.ibm.cachesample.RESTProvider"%>

<%@ page import="java.util.Map"%>

<%@ page import="org.json.JSONArray"%>
<%@ page import="org.json.JSONException"%>
<%@ page import="org.json.JSONObject"%>


<%!
    RESTProvider provider;

	public void jspInit() {
		
		Map<String, String> env = System.getenv();
		String vcap = env.get("VCAP_SERVICES");

		String username;
		String password;
		String restResource;

		username = "";
		password = "";
		restResource = "";

		boolean foundService = false;
		if (vcap == null) {
			System.out.println("No VCAP_SERVICES found" + "<br>");
		} else {
			try {
				JSONObject obj = new JSONObject(vcap);
				String[] names = JSONObject.getNames(obj);
				if (names != null) {
					for (String name : names) {
						if (name.startsWith("DataCache")) {
							JSONArray val = obj.getJSONArray(name);
							JSONObject serviceAttr = val.getJSONObject(0);
							JSONObject credentials = serviceAttr.getJSONObject("credentials");
							username = credentials.getString("username");
							password = credentials.getString("password");
							restResource = credentials.getString("restResource");
							System.out.println("Found configured username: " + username + "<br>");
							System.out.println("Found configured password: " + password + "<br>");
							System.out.println("Found configured resource: " + restResource + "<br>");
							foundService = true;
							break;
						}
					}
				}
			} catch (Exception e) {
			}
		}
		if (!foundService) {
			System.out.println("Did not find WXS service, using defaults<br>");
		}
		provider = new RESTProvider(restResource, username, password, "sampleMap.NONE.P");
	}%>

<%
	provider.threadInit();
	String key = request.getParameter("key");
	String operation = request.getParameter("operation");
	Object retrievedValue;
	if ("get".equals(operation)) {

		retrievedValue = provider.get(key);
		response.getWriter().write(retrievedValue == null ? "null" : retrievedValue.toString());
	
	} else if ("put".equals(operation)) {
	
		String newValue = request.getParameter("value");
		provider.update(key, newValue);
		response.getWriter().write("[PUT]");

	} else if ("delete".equals(operation)) {
		
		provider.remove(key);
		response.getWriter().write("[DELETED]");
	}
%>
