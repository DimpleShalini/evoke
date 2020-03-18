package com.commoninf.rest.api;

import groovy.json.JsonBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.http.HttpHeaders
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

class Index implements RestApiController {

    private static final Logger logger = LoggerFactory.getLogger(Index.class)
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

        // Retrieve db_name parameter
        def db_name = request.getParameter "db_name"
        if (db_name == null) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter db_name is missing"}""")
        }
		
		// Retrieve req parameter
		def req = request.getParameter "req"
		if (req == null) {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter req is missing"}""")
        }
		
        // Here is an example of how you can retrieve configuration parameters from a properties file
        // It is safe to remove this if no configuration is required
        Properties props = loadProperties("configuration.properties", context.resourceProvider)
        String db_url = props[db_name+"_url"]
		String db_port = props[db_name+"_port"]
		String db_realname = props[db_name+"_name"]
		String db_user_name = props[db_name+"_user_name"]
		String db_user_pwd = props[db_name+"_password"]
		
		if (c.equals("test")) {
			// Prepare the result
			def result = [ "p" : p ,"c" : c ,"db_name" : db_name, "req" : req ]
			
			// Send the result as a JSON representation
			// You may use buildPagedResponse if your result is multiple
			return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toString())
		}
		else {
			RestApiResponse response_val;
			
			req_p = Integer.parseInt(p);
			req_c = Integer.parseInt(c);
			
			/*******************************************
			 * Handle investver request
			 */
			if (req.equals("investver")) {
				response_val = getInvestReq (db_url, db_port, db_realname, db_user_name, db_user_pwd);
			}
			
			/*******************************************
			 * Handle prodlist request
			 * This returns the complete list of products
			 */
			else if (req.equals("prodlist")) {
				response_val = getProductList (db_url, db_port, db_realname, db_user_name, db_user_pwd);
			}
			
			/*******************************************
			 * Handle prod request
			 * This returns the product associated with
			 * the request parameter prod_id
			 */
			else if (req.equals("prod")) {
				// Retrieve req parameter
				def prod_id = request.getParameter "prod_id"
				logger.info ("Fetching information for product with and id of <"+prod_id+">");
				if ((prod_id == null)||(prod_id == "")) {
					return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter prod_id is missing which is required when req=prod"}""")
				}
				response_val = getProduct (prod_id, db_url, db_port, db_realname, db_user_name, db_user_pwd);
			}
			
			/*******************************************
			 * Handle an invalid req
			 */
			else {
				def builder_str = "{values:[]}";
				response_val = buildPagedResponse(rspbld, builder_str, req_p, req_c, 0);
			}

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
	
	/**************************************************************************
	 * Specific functions to handle requests
	 */
	
	/**************************************************************************
	 * Handle a request for invest data model version
	 * @param db_url
	 * @param db_port
	 * @param db_realname
	 * @param db_user_name
	 * @param db_user_pwd
	 * @return
	 */
	RestApiResponse getInvestReq (String db_url, String db_port, String db_realname, String db_user_name, String db_user_pwd) {
		InvestDb investdb = new InvestDb(db_url, db_port, db_realname, db_user_name, db_user_pwd);
		
		investdb.fetchAllVersions();
		def versionlist = investdb.getVerList()
		def builder = new JsonBuilder();
		builder versionlist.collect {[parameter:it.getParameter(),setting:it.getSetting()]}
		
		return buildPagedResponse(rspbld, builder.toString(), req_p, req_c, investdb.getVerList().size())
	} 
	
	/**************************************************************************
	 * Handle a request for the product list
	 * @param db_url
	 * @param db_port
	 * @param db_realname
	 * @param db_user_name
	 * @param db_user_pwd
	 * @return
	 */
	RestApiResponse getProductList (String db_url, String db_port, String db_realname, String db_user_name, String db_user_pwd) {
		ProductListDb prodlistdb = new ProductListDb(db_url, db_port, db_realname, db_user_name, db_user_pwd);
		
		prodlistdb.fetchAllProducts();
		def productlist = prodlistdb.getProdList()
		def builder = new JsonBuilder();
		builder productlist.collect {[id:it.getId(),name:it.getName(),project_code:it.getProject_code(),inn:it.getInn(),pharma_form:it.getPharma_form(),indication:it.getIndication(),therap_area:it.getTherap_area()]}
		
		return buildPagedResponse(rspbld, builder.toString(), req_p, req_c, prodlistdb.getProdList().size())
	}
	
	/**************************************************************************
	 * Handle a request for the product list
	 * @param db_url
	 * @param db_port
	 * @param db_realname
	 * @param db_user_name
	 * @param db_user_pwd
	 * @return
	 */
	RestApiResponse getProduct (String id, String db_url, String db_port, String db_realname, String db_user_name, String db_user_pwd) {
		ProductListDb prodlistdb = new ProductListDb(db_url, db_port, db_realname, db_user_name, db_user_pwd);
		
		def val = prodlistdb.fetchProduct(id);
		def value = {}
		if (val != null) {
			value = ["id":val.getId().toString(),"name":val.getName(),"project_code":val.getProject_code(),"inn":val.getInn(),"pharma_form":val.getPharma_form(),"indication":val.getIndication(),"therap_area":val.getTherap_area()]
		}
		
		return buildResponse(rspbld, HttpServletResponse.SC_OK, new JsonBuilder(value).toString())
	}
}
