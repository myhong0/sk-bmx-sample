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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.xs.sample.airport.domain.AirportCodes;

/**
 * Servlet for pre-loading data cache with airport information
 * 
 */
public class AirportPreloadServlet extends HttpServlet {
	
	private static final long serialVersionUID = -2032391866949335571L;
	
	private final AirportClient ivAirportClient = new AirportClient();
       
    /**
     * Constructor
     */
    public AirportPreloadServlet() {
        super();
    }

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			 
			// Check if data cache already pre-loaded
			if (!ivAirportClient.isMapPreloaded()) {
			
				// Pre-load data cache with airport information
				AirportCodes apCodes = ivAirportClient.preloadMap();
				
				// Put all the airport codes returned into data cache
				ivAirportClient.addAllCodes(apCodes);
			   
				System.out.println("Data service pre-load completed");
				
			} else {
				System.out.println("Data service already pre-loaded");
			}
		   
		} catch (IOException e) {
			   
			System.err.println("Failed to load data from Airport props file: airport.props" );
			e.printStackTrace();
			   
		} catch (ObjectGridException e) {
			   
			System.err.println("Failed to pre-load data cache with airport information");
			e.printStackTrace();
		}				
	}

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
