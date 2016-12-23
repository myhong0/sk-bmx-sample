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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
 
/**
 * This is a Session Cache Service sample that run in BlueMix.
 * 
 * This sample servlet allows access of session information, and insert and retrieve data into session. 
 * 
 */
@WebServlet("/SessionCacheSampleServlet")
public class SessionCacheSampleServlet extends HttpServlet {
    
	private static final long serialVersionUID = -2684635794916004762L;
	
	/**
	 * Title of the returning page
	 */
	private final String sampleName = "Session Cache Service sample";
	
	/**
	 * Constants
	 */
	private String servletName 					= "SessionCacheSampleServlet";
	private String attr_accessCount				= "accessCount";
	private String attr_wxs_request				= "wxs_request";
	private String attr_wxs_session_data		= "wxs_session_data";
	private String attr_wxs_session_invalidate	= "wxs_session_invalidate";
	
	
    /**
     * Constructors
     */
    public SessionCacheSampleServlet() {
        super();
    }

    /**
	 * Handle GET request
	 * 
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Get the current session associated with the request.  If it is not available, create one
   		// In the background, this function will leverage on the Session Cache service to store / retrieve
    	// session data from the grid
        HttpSession session = request.getSession(true);        
      
        // Determine heading
        String heading = "";
        IntWrapper accessCount = (IntWrapper) session.getAttribute(attr_accessCount);        
        if (accessCount == null) {
            accessCount = new IntWrapper(0);
            heading = "Welcome, Newcomer.";
        } else {
            heading = "Welcome back.";
            accessCount.incrementValue();
        }
        session.setAttribute(attr_accessCount, accessCount);

        // Create page title
        out.println(createHeadWithTitle(sampleName));
        
        // Determine the action:
        //   wxs_session_data 			- Return session data
        //   wxs_session_invalidate		- Invalidate current session 
        String wxsRequest = request.getParameter(attr_wxs_request);
        if (attr_wxs_session_data.equalsIgnoreCase(wxsRequest)) {        	
            out.println("<h2>" + heading + "</h2>\n");           
            out.println(getSessionData(session));
        } else if (attr_wxs_session_invalidate.equalsIgnoreCase(wxsRequest)) {
            out.println(invalidateSession(session));
        } else {
            out.println(getSelectionPage());
        }

        out.println("<FORM><INPUT TYPE=\"button\" VALUE=\"Main Page\" onClick=\"history.go(-1);\"> </FORM>");
        out.println("</BODY>\n");
        out.println("</HTML>");
    }

    /**
	 * Handle POST request
	 * 
	 */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
        doGet(request, response);
    }

    /**
     * Return selection page
     * 
     * @return
     */
    private String getSelectionPage() {
    	
    	String html = "<form ACTION=\"" + servletName + "\" METHOD=\"POST\">\n" +
        			  "<input type=\"hidden\" name=\"" + attr_wxs_request + "\" value=\"" + attr_wxs_session_data + "\" />\n" +
        			  "<INPUT TYPE=\"SUBMIT\" VALUE=\"Retrieve Session Data\">\n" +
        			  "</form>\n" + 
        		
        			  "<form ACTION=\"" + servletName + "\" METHOD=\"POST\">\n" +
        			  "<input type=\"hidden\" name=\"" + attr_wxs_request + "\" value=\"" + attr_wxs_session_invalidate + "\" />\n" +
        			  "<INPUT TYPE=\"SUBMIT\" VALUE=\"Invalidate Session\">\n" +
        			  "</form>\n";        	        		
        
    	return html;
    }
    

    /**
     * Return session data
     * 
     * @param session
     * @return
     */
    private String getSessionData(HttpSession session) {    	
    	
        String response = "<H2>Information on your session:</H2>\n" +
        				  "<TABLE BORDER=1>\n" +
        				  "<TR>  <TH>Info Type</TH> 				<TH>Value</TH>								    	 	    </TR>\n" + 
        				  "<TR>	 <TD>ID</TD>         		 		<TD>" + session.getId() +							 "</TD> </TR>\n" + 
        				  "<TR>	 <TD>isNew</TD>      		 		<TD>" + session.isNew() + 							 "</TD> </TR>\n" + 
        				  "<TR>	 <TD>Creation time</TD>	            <TD id='createtime'></TD> </TR>\n" + 
        				  "<TR>	 <TD>Time of Last Access</TD> 		<TD id='accesstime'></TD> </TR>\n" + 
        				  "<TR>	 <TD>Max Inactive Interval</TD>		<TD>" + session.getMaxInactiveInterval() +			 "</TD> </TR>\n" +
        				  "<TR>	 <TD>Number of Previous Accesses</TD><TD><B>" + session.getAttribute(attr_accessCount) + "</B></TD> </TR>\n" +
        				  "</TABLE>\n" + 
						  "<script> var d = new Date(" + session.getCreationTime() +  "); document.getElementById('createtime').innerHTML = d.toLocaleString(); " + 
						  "var d2 = new Date(" + session.getLastAccessedTime() + "); document.getElementById('accesstime').innerHTML = d2.toLocaleString();</script>\n";
        
        return response;
    }

    /**
     * Invalidate session
     * 
     * @return
     */
    private String invalidateSession(HttpSession session) {
    	
    	session.invalidate();
    	
        String response = "<h2>" + "Session invalidated. Please visit again!" + "</h2>\n";
        return response;
    }
    
    /**
     * Create HTML head section with the given title
     * 
     * @param title
     * @return
     */
    private String createHeadWithTitle(String title) {
    	
    	String html = 	"<html><head>" +
    					"<title>" + title + "</title>\n" +
    					"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n" +
    					"</head>\n" +
    					"<body bgcolor=\"lightblue\" style=\"margin: 0; padding: 0;\">\n" +
    					"<h2>" + title + "</h2>\n";
    	
        return html;
    }
}
