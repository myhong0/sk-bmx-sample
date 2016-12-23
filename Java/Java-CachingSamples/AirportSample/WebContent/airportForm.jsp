<%@page %>
<!DOCTYPE html>
<!-- 
	This sample program is provided AS IS and may be used, executed, copied and
	modified without royalty payment by customer (a) for its own instruction and 
	study, (b) in order to develop applications designed to run with an IBM 
	WebSphere product, either for customer's own internal use or for redistribution 
	by customer, as part of such an application, in customer's own products. "

	5724-J34 (C) COPYRIGHT International Business Machines Corp. 2014
	All Rights Reserved * Licensed Materials - Property of IBM
-->
<html>
<head>
  <title>IBM Data Cache service and IBM Session Cache service sample - Airport Entry</title>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
  <link id="themeStyles" rel="stylesheet" href="dojo/dijit/themes/claro/claro.css"/>
  <style type="text/css">
	  @import "css/document.css";
  </style>
  <script type="text/javascript" src="dojo/dojo/dojo.js" djConfig="parseOnLoad: true, isDebug: true"></script>
  <link type='text/css' rel='stylesheet' href="dojo/dojo/resources/dojo.css"></link>
</head>
		
<body class="claro oneui" style="margin: 0; padding: 0;">
<div class="headerLogo">logo</div>
<div class="headerBackground">
<div class="headerTitle">IBM Data Cache service and IBM Session Cache service sample</div>
<div class="headerSubtitle">Airport Entry Sample</div>
</div>
<div class="bodyContent">
<h2>BlueMix Airport</h2>
<p>The Airport sample is provided as an introduction to IBM Data Cache service and IBM Session Cache service functionality 
on BlueMix.  It runs simple create, read, update, and delete (CRUD) functions to IBM Data Cache service and IBM Session 
Cache service in milliseconds.  The sample shows how large amounts of data (in this case, information about thousands of airports worldwide) 
can be stored using IBM Data Cache service and IBM Session Cache service on BlueMix.  When using the sample, 
the time taken to complete any CRUD operation is displayed, demonstrating the speed of accessing the grid.</p>

<ul>
<li>To perform a read operation on an airport already in data cache, enter an airport code (examples: JFK or LAX).
	The dropdown auto-complete feature helps you search for airports already in the grid.</li>
<li>If you do not see the airport you want in the dropdown, 
	click <span class="uicontrol">Insert New</span> to create an entry for a new airport.</li>
<li>Perform a read operation on previously searched airports from the Search History menu, available after the first read operation.</li>
</ul>

<%@include file="searchAirports.jsp" %>

<%
   String code = (String) request.getAttribute("code");
   String city = (String) request.getAttribute("city");
   String country = (String) request.getAttribute("country");
   String numOfTerminals = (String) request.getAttribute("numOfTerminals");
   String numOfGates = (String) request.getAttribute("numOfGates");
   String message = (String) request.getAttribute("message");
   Double elapsedTime = (Double) request.getAttribute("elapsedTime");
   String newEntry = (String)request.getParameter("newEntry");
   
   if (message != null) {
%>
  <div><%=message%></div>
<%
   }
%>

<% if (code != null || newEntry != null) { %>
<% String readonly = ""; %>

<div id="myForm" style="height: 375px;">
<hr />
<% if (newEntry == null) { %>
<iframe width="425" height="350" align="right" frameborder="0" scrolling="no" marginheight="0" marginwidth="0" src="http://maps.google.com/maps?f=q&amp;source=s_q&amp;hl=en&amp;geocode=&amp;q=<%=code %>+airport,+<%=city %>,+<%=country %>&amp;aq=&amp;t=h&amp;ie=UTF8&amp;output=embed"></iframe><br />
<% readonly = "readonly=\"readonly\" style=\"background-color:#ccc;\"";%>
<% } %>
<form>
<h3>Airport Entry</h3>
  <p><label for="code">Code:</label> <input type="text" <%=readonly %>name="code" value="<%=code==null?"":code%>"/></p>
  <p><label for="city">City:</label> <input type="text" name="city" value="<%=city==null?"":city%>"/></p>
  <p><label for="country">Country:</label> <input type="text" name="country" value="<%=country==null?"":country%>"/></p>
  <p><label for="terminals">Number of Terminals:</label> <input type="text" name="numOfTerminals" value="<%=numOfTerminals==null?"":numOfTerminals%>"/></p>
  <p><label for="gates">Number of Gates:</label> <input type="text" name="numOfGates" value="<%=numOfGates==null?"":numOfGates%>"/></p>

<% if (newEntry == null) { %>
<label for="buttons">&nbsp;</label> <input type="submit" value="Update" name="action"/> <input type="submit" value="Delete" name="action"/>
<% }
else { %>
<label for="buttons">&nbsp;</label><input type="submit" value="Insert" name="action"/>
<% } %>
</form>
</div>
<% } %>
<%
   if (elapsedTime != null) {
%>
<hr />
<div>Elapsed Time: <%=elapsedTime%> milliseconds</div>
<%
   }
%>
</div>
<p></p>
</body>
</html>