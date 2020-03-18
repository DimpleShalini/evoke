/**
 * Copyright (c) 2019 Commonwealth Informatics Inc. All rights reserved.
 */
package com.commoninf.rest.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import groovy.json.JsonBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController
import com.commoninf.Tracker_CustDb.Tracker_CustDb
import com.commoninf.json.JsonOut
import com.commoninf.database.Row ;

class Tracker_CustGet implements RestApiController {

    private static final Logger logger = LoggerFactory.getLogger(Tracker_CustGet.class)
	private int req_p = 0;
	private int req_c = 0;
	private RestApiResponseBuilder rspbld = null;

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        // To retrieve query parameters use the request.getParameter(..) method.
        // Be careful, parameter values are always returned as String values

		rspbld = responseBuilder;
		
        // Retrieve p parameter
        def p = request.getParameter "p"
        if (p == null) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter p is missing"}""")
        }

        // Retrieve c parameter
        def c = request.getParameter "c"
        if (c == null) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter c is missing"}""")
        }

        // Retrieve q parameter
        def query = request.getParameter "q"
        if (query == null) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter q is missing"}""")
        }
		
		// Retrieve f parameter
		def filter = request.getParameter "f"
		if (((query=="u_by_p")||(query=="u_by_pid")||(query=="p_by_u"))&&((filter == null)||(filter==""))) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter f is missing"}""")
        }
		
		if (c.equals("test")) {
			// Prepare the result
			def result = [ "p" : p ,"c" : c ,"q" : query, "f" : filter ]
			
			// Send the result as a JSON representation
			// You may use buildPagedResponse if your result is multiple
			return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toString())
		}
		else {
			RestApiResponse response_val;
			JsonOut json_out = null;
			
			req_p = Integer.parseInt(p);
			req_c = Integer.parseInt(c);
			// logger.debug("Starting the Tracker_CustDb instance for GET");
			Tracker_CustDb the_db = DbUtils.initTracker_CustDbConfig(context);
			
			/*******************************************
			 * Handle db_inf request
			 * This returns the databaseinfo table, which
			 * contains the data model version
			 */
			if (query.equals("db_inf")) {
				json_out = the_db.buildJsonForDataBaseInfo();
				json_out.setPrettyPrintJson(true);
				response_val = buildPagedResponse(rspbld, json_out.toString(), req_p, req_c, json_out.getNumRows());
			}
			
			/*******************************************
			 * Handle u_by_p request
			 * This returns the list of users for a
			 * product family
			 */
			else if (query.equals("u_by_p")) {
				json_out = the_db.buildJsonForProjectGroupsUsers(filter);
				json_out.setPrettyPrintJson(true);
				response_val = buildPagedResponse(rspbld, json_out.toString(), req_p, req_c, json_out.getNumRows());
			}
			
			/*******************************************
			 * Handle u_by_pid request
			 * This returns the list of users for a
			 * product family by id
			 */
			else if (query.equals("u_by_pid")) {
				json_out = the_db.buildJsonForProjectGroupsUsersById(filter);
				json_out.setPrettyPrintJson(true);
				response_val = buildPagedResponse(rspbld, json_out.toString(), req_p, req_c, json_out.getNumRows());
			}

			
			/*******************************************
			 * Handle p_by_u request
			 * This returns the list of products a user
			 * is assigned to
			 */
			else if (query.equals("p_by_u")) {
				json_out = the_db.buildJsonForUsersProjectGroups(filter);
				json_out.setPrettyPrintJson(true);
				response_val = buildPagedResponse(rspbld, json_out.toString(), req_p, req_c, json_out.getNumRows());
			}
			
			/*******************************************
			 * Handle p request
			 * This returns the list of all products
			 */
			else if (query.equals("p")) {
				json_out = the_db.buildJsonForProjectGroups();
				json_out.setPrettyPrintJson(true);
				response_val = buildPagedResponse(rspbld, json_out.toString(), req_p, req_c, json_out.getNumRows());
			}

			/*******************************************
			 * Handle tcl request
			 * This returns topic code lists
			 */
			else if (query.equals("tcl")) {
				def getDropDownsData = the_db.getDropDownsData();
			  
				GsonBuilder builder = new GsonBuilder(); 
				builder.setPrettyPrinting(); 
				Gson gson = builder.create(); 
				def jsonString = gson.toJson(getDropDownsData); 
				// logger.info ("jsonString...."+ jsonString);
				response_val = buildPagedResponse(rspbld, jsonString, req_p, req_c, getDropDownsData.size());
			}
			
			/*******************************************
			 * Handle tcl_wi request
			 * This returns topic code lists
			 */
			else if (query.equals("tcl_wi")) {
				def getDropDownsData = the_db.getDropDownsData(true);
			  
				GsonBuilder builder = new GsonBuilder();
				builder.setPrettyPrinting();
				Gson gson = builder.create();
				def jsonString = gson.toJson(getDropDownsData);
				// logger.info ("jsonString...."+ jsonString);
				response_val = buildPagedResponse(rspbld, jsonString, req_p, req_c, getDropDownsData.size());
			}
			
			/*******************************************
			 * Handle uflx request
			 * This returns all of the possible user functions / roles
			 */
			else if (query.equals("uflx")) {
				json_out = the_db.buildJsonForUserFunctionLx() ;
				json_out.setPrettyPrintJson(true);
				response_val = buildPagedResponse(rspbld, json_out.toString(), req_p, req_c, json_out.getNumRows());
			}

			/*******************************************
			 * Handle projects request
			 * This returns all of the projects
			 */
            else if (query.equals("projects")) {
                def sortField = request.getParameter "sf";
                def sortOrder = request.getParameter "so";

                // TODO: If implement server side sorting, need to translate sortField into schema
                                
                json_out = new JsonOut(the_db.getProject_CoreTable("project_group_alias.group_name", sortOrder && sortOrder == "1").getTable_rows(), req_p, req_c);
                json_out.setPrettyPrintJson(true);
                response_val = buildPagedResponse(rspbld, json_out.toString(), req_p, req_c, json_out.getNumRows());
            } 
            else if (query.equals("project")) {
                if (!filter || filter == '') {
                    filter = null;
                }

                json_out = the_db.buildJsonForProjectGroup(filter, req_p, req_c) ;
                json_out.setPrettyPrintJson(true);
                response_val = buildPagedResponse(rspbld, json_out.toString(), req_p, req_c, json_out.getNumRows());
            } 
			/**
			 * Fetch a single project group by ID
			 */
			else if (query.equals("pgid")) {
				
				Integer id ; 
				try {
					id = Integer.valueOf(filter) ;					
				} catch (NumberFormatException nfe) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter f is invalid"}""")
				}

                json_out = the_db.buildJsonForProjectGroupId(id) ;
                json_out.setPrettyPrintJson(true);
				
				if (json_out.getNumRows() == 0) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_NOT_FOUND, null) ;
				}
				else {
					response_val = buildPagedResponse(rspbld, json_out.toString(), req_p, req_c, json_out.getNumRows());
				}
            }


			/*******************************************
			 * Handle an invalid req
			 */
			else {
				response_val = buildPagedResponse(rspbld, "[]", req_p, req_c, 0);
			}
			
			// logger.info ("Stopping the Tracker_CustDb instance for GET");

			return response_val
		}
    }

    /**
     * Build an HTTP response.
     *
     * @param  responseBuilder the Rest API response builder
     * @param  httpStatus the status of the response
     * @param  body the response body
     * @return a RestAPIResponse
     */
    RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
        return responseBuilder.with {
            withResponseStatus(httpStatus)
            withResponse(body)
            build()
        }
    }

    /**
     * Returns a paged result like Bonita BPM REST APIs.
     * Build a response with a content-range.
     *
     * @param  responseBuilder the Rest API response builder
     * @param  body the response body
     * @param  p the page index
     * @param  c the number of result per page
     * @param  total the total number of results
     * @return a RestAPIResponse
     */
    RestApiResponse buildPagedResponse(RestApiResponseBuilder responseBuilder, Serializable body, int p, int c, long total) {
	    return responseBuilder.with {
	        withContentRange(p,c,total)
	        withResponse(body)
	        build()
	    }
    }

    /**
     * Load a property file into a java.util.Properties
     */
    Properties loadProperties(String fileName, ResourceProvider resourceProvider) {
        Properties props = new Properties()
        resourceProvider.getResourceAsStream(fileName).withStream { InputStream s ->
            props.load s
        }
        props
    }
}
