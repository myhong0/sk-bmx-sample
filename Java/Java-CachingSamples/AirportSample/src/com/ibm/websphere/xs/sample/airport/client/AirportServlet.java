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
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.websphere.objectgrid.ConnectException;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.plugins.TransactionCallbackException;
import com.ibm.websphere.xs.sample.airport.domain.AirportData;

/**
 * Entry servlet for airport sample.
 * 
 */
public class AirportServlet extends HttpServlet {
	
   private static final long serialVersionUID = -8795528585001526072L;

   private final AirportClient ivAirportClient = new AirportClient();

   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	  
	  // Create or retrieve session
      HttpSession session = req.getSession(true);
      
      // Get parameter from the request to determine action to perform
      String action = req.getParameter("action");
      String code = req.getParameter("code");
      if (action == null && code != null) {
         action = "search";
      }
      
      AirportData data = null;
      String message = null;
      
      long startTime = System.nanoTime();
      
      if (code == null) {
    	  
    	 System.out.println("doGet:  No code specified - expected if this is the first hit/refresh/new session/etc.");
         
      } else if (code != null) {
    	  
    	 // Perform actions accordingly
         if ("delete".equalsIgnoreCase(action)) {
            if (delete(req, resp, code)) {
               message = "Airport code has been deleted: " + code;
               code = null;
            } else {
               message = "Could not delete airport code: " + code + ".  Check logs for more details.";
            }
         } else if ("insert".equalsIgnoreCase(action)) {
            try {
               data = update(req, resp, code);
            } catch (Exception ex) {
               message = "Insert failed.  Check logs for more details.";
            }
         } else if ("update".equalsIgnoreCase(action)) {
            try {
               data = update(req, resp, code);
            } catch (Exception ex) {
               message = "Update failed.  Check logs for more details.";
            }
         } else if ("search".equalsIgnoreCase(action)) {
            data = search(req, resp, session, code);
            if (data == null) {
               message = "Airport not found for code: " + code;
               code = null;
            }
         } else  {
        	System.out.println("doGet:  Unknown action.");
         }

         req.setAttribute("code", code);
      }

      // Calculate roughly how long does the data cache operation take 
      double elapsedTime = (System.nanoTime() - startTime) / 1000000; // convert to milliseconds (nano / 1Million = ms)
      req.setAttribute("elapsedTime", elapsedTime);

      if (message != null) {
         req.setAttribute("message", message);
      }

      // Prepare to send back the airport information, if available
      if (data != null) {
         req.setAttribute("city", data.getCity());
         req.setAttribute("country", data.getCountry());
         req.setAttribute("numOfTerminals", ""+data.getNumOfTerminals());
         req.setAttribute("numOfGates", ""+data.getNumOfGates());
      }

      @SuppressWarnings("unchecked")
	  SortedSet<String> allCodes = (SortedSet<String>) session.getAttribute("allCodes");
	  
      // If the list of airport codes are not available in the session, 
      // retrieve it from data cache and set it in session.
      // List of airport codes stored in session is persisted using Session Cache service
      if (allCodes == null) {
           
      	  try {
      		  
      		  allCodes = ivAirportClient.getAllCodes();
      		  
      	  } catch (ConnectException e) {

      		  e.printStackTrace();
      		  
      	  } catch (TransactionCallbackException e) {

      		  e.printStackTrace();
      		  
      	  } catch (ObjectGridException e) {

      		  e.printStackTrace();
      	  }
      	  
      	  session.setAttribute("allCodes", allCodes);
      }
      
      RequestDispatcher rd = req.getRequestDispatcher("airportForm.jsp");
      rd.forward(req, resp);      
   }
   
   @Override
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      // Delegate to doGet
      doGet(req, resp);
   }
   
   /**
    * Search for airport information with the given airport code
    * 
    * @param req
    * @param resp
    * @param session
    * @param code
    * @return
    */
   private AirportData search(HttpServletRequest req, HttpServletResponse resp, HttpSession session, String code) {
      
	  // Try to retrieve the previously searched airport codes from session.
	  // Previously searched airport codes in session are persisted using Session Cache service
      @SuppressWarnings("unchecked")
	  SortedSet<String> prevSearches = (SortedSet<String>) session.getAttribute("prevSearches");
      if (prevSearches == null) {
         prevSearches = new TreeSet<String>();
         session.setAttribute("prevSearches", prevSearches);
      }

      // Record the search history
      try {
    	 
         prevSearches.add(code);
         
      } catch (Throwable t) {
    	  
    	 System.err.println("search:  Caught exception adding \"" + code + "\" to previous searches set. " + t.getClass().getName());
      }
      
      // Search for airport information
      AirportData data;
      try {
    	  
         data = ivAirportClient.getAirportData(code);
         
      } catch (ObjectGridException ex) {
         
         System.out.println("search:  Warning.  Caught exception during search.  " + ex.getClass().getName() );
         data = null;
      }
     
      return data;
   }

   /**
    * Update airport information
    * 
    * @param req
    * @param resp
    * @param code
    * @return
    * @throws Exception
    */
   private AirportData update(HttpServletRequest req, HttpServletResponse resp, String code) throws Exception {

      AirportData data;
      
      // Find the airport information corresponding to the given code from data cache
      try {
    	  
         data = ivAirportClient.getAirportData(code);
         
      } catch (ObjectGridException ex) {
         
         System.out.println("update:  Warning.  Caught exception during search.  " + ex.getClass().getName() );
         data = null;
      }
      
      // Determine new airport information should be inserted into data cache,
      // or just update existing airport information
      boolean insert = false;
      if (data == null) {
         // key does not exist - must do an insert
         data = new AirportData();
         insert = true;
      }

      try {
    	  
         data.setCity(req.getParameter("city"));
         data.setCountry(req.getParameter("country"));
         data.setNumOfTerminals(Integer.parseInt(req.getParameter("numOfTerminals")));
         data.setNumOfGates(Integer.parseInt(req.getParameter("numOfGates")));

         if (insert) { 
            ivAirportClient.addNewAirport(code, data);
         } else {
            ivAirportClient.updateAirport(code, data);
         }
            
      } catch (NumberFormatException ex) {

         throw new Exception("Invalid inputs - number of gates/terminals must be integers.");
         
      } catch (ObjectGridException ex) {
         
         throw new Exception("Failed to update/insert entry", ex);
      }
      
      return data;
   }

   /**
    * Delete airport information corresponding to the given airport code
    * 
    * @param req
    * @param resp
    * @param code
    * @return
    */
   private boolean delete(HttpServletRequest req, HttpServletResponse resp, String code) {
      
      boolean success;
      
      try {
    	  
         ivAirportClient.deleteAirport(code);
         success = true;
         
      } catch (ObjectGridException ex) {
    	         
         System.out.println("delete:  Warning.  Exception occurred during delete.  " + ex.getClass().getName() );
         success = false;
         
      }
            
      return success;
   }
}
