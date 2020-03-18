/**
 * Copyright (c) 2019 Commonwealth Informatics Inc. All rights reserved.
 */
package com.commoninf.rest.api

import java.io.Serializable

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import java.lang.Long
import java.net.Authenticator.RequestorType

import org.apache.http.HttpHeaders
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.bonitasoft.web.extension.rest.RestApiController

import com.commoninf.Tracker_CustDb.Tracker_CustDb
import com.commoninf.database.Row
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import com.commonwealth.audit.AuditEntry
import com.commoninf.rest.api.AuditHelpers

import com.commoninf.json.JsonIn

import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.FileItem

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import java.io.InputStreamReader
import java.io.Reader

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.ArrayList
\
class Tracker_CustPost  implements RestApiController {

	private static final Logger logger = LoggerFactory.getLogger(Tracker_CustPost.class)

	@Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
				
        // Retrieve f parameter
        def query = request.getParameter "q"
        if (query == null) {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"error" : "the parameter q is missing"}""")
        }	
		
		if (context.getApiClient()) {			
			if (query.compareToIgnoreCase("u_ift") == 0) {
                return handleIndicationFullTextUpdate(request, responseBuilder, context)
			}
			if (query.compareToIgnoreCase("pgcreate") == 0) {
				return handleCreateProjectGroup(request, responseBuilder, context)
			}
			if (query.compareToIgnoreCase("pgassociate") == 0) {
				return handleAddProjectsToProjectGroup(request, responseBuilder, context)
			}
			if (query.compareToIgnoreCase("pgrename") == 0) {
				return handleRenameProjectGroup(request, responseBuilder, context)
			}

			else if (query.compareToIgnoreCase("u_uf") == 0) {
				return handleUserFunctionUpdate(request, responseBuilder, context)
			}
            else if (query.compareToIgnoreCase("import") == 0) {
                return handleProjectImport(request, responseBuilder, context)
            } 
            else {
				return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"Error" : "Unknown request"}""")
			}						
		}
		
		return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"Error" : "Unknown request"}""")
	}
	
	
	RestApiResponse handleIndicationFullTextUpdate(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def projectFamily = request.getParameter "f"		
		String indicationFullText = request.reader.readLines().join("\n");		
		
		def username = context.getApiSession().getUserName()
		logger.info("Requesting update " + projectFamily + " indication full text by" + username) ;
		
		Tracker_CustDb the_db = DbUtils.initTracker_CustDbConfig(context);
		
        // Get User Permissions and User Functions
        def permissions = fetchUserPermissions(request, username)
        def userFunctions = new JsonSlurper().parseText(the_db.buildJsonForProjectGroupsUsers(projectFamily).toString()) 
        
        // Determine our User function (Role) within project group
        def functionRoles = []
        for (Object userFunction: userFunctions) {
            if (userFunction.username && userFunction.username.compareToIgnoreCase(userFunction.username) == 0) {
				if (!functionRoles.contains(userFunction.user_function_id)) {
					functionRoles << userFunction.user_function_id
				}
            }
        }
        
        // Fail if we could not load prerequisites needed for security checks 				
        if ( (permissions == null || permissions.user_permissions == null || userFunctions == null || functionRoles == null)) {
            AuditHelpers.auditSecurityFailure(context, request, "unable to load prerequisite reference data for therapy update", 0, "Therapy")
            return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
        }
				
		// High level security check: must be the business admin / load tech or safety/deputy safety lead 
		if (!(	permissions.user_permissions.indexOf('CAN_LOAD_THERAPIES') >= 0 ||
				permissions.user_permissions.indexOf('CAN_MANAGE_THERAPIES') >= 0 ||
				functionRoles.contains('Safety Lead') ||
				functionRoles.contains('Deputy Safety Lead'))) {
			AuditHelpers.auditSecurityFailure(context, request, "user failed permission pre-check for therapy update", 0, "Therapy")
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
		}
		
		ArrayList<Row> originalRows = the_db.getProject_GroupByName(projectFamily).getTable_rows() ;
		if (!originalRows.isEmpty()) {
			Row originalRow = originalRows.get(0);
			String originalJson = new JsonBuilder(originalRow.getCol_vals()).toString()
			if (the_db.updateProjectGroup(projectFamily, null, indicationFullText)) {
				ArrayList<Row> newRows = the_db.getProject_GroupById(originalRow.getColVal("id")).getTable_rows() ;
				Row newRow = newRows.get(0);
				String newJson = new JsonBuilder(newRow.getCol_vals()).toString()
				
				long id = Long.parseLong(originalRow.getColVal("id").toString()) ;
				
				AuditHelpers.auditDifference(context, request, id, id, "Therapy", projectFamily, originalJson, newJson); 			
												
				return buildResponse(responseBuilder, HttpServletResponse.SC_NO_CONTENT, null)
			} else
				return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"Error" : "Unknown error"}""")
		} else {
			return buildResponse(responseBuilder, HttpServletResponse.SC_NOT_FOUND,"""{"Error" : "Not Found"}""")
		}
	}
	
	
	RestApiResponse handleRenameProjectGroup(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def projectFamily = request.getParameter "f"
		String newProjectFamily = request.reader.readLines().join("\n");
		
		def username = context.getApiSession().getUserName()
		logger.info("Requesting rename of " + projectFamily + " to " + newProjectFamily + " by " + username) ;
		
		Tracker_CustDb the_db = DbUtils.initTracker_CustDbConfig(context);
		
		// Get User Permissions and User Functions
		def permissions = fetchUserPermissions(request, username)
		def userFunctions = new JsonSlurper().parseText(the_db.buildJsonForProjectGroupsUsers(projectFamily).toString())
		
		// Determine our User function (Role) within project group
		def functionRoles = []
		for (Object userFunction: userFunctions) {
			if (userFunction.username && userFunction.username.compareToIgnoreCase(userFunction.username) == 0) {
				if (!functionRoles.contains(userFunction.user_function_id)) {
					functionRoles << userFunction.user_function_id
				}
			}
		}
		
		// Fail if we could not load prerequisites needed for security checks
		if ( (permissions == null || permissions.user_permissions == null || userFunctions == null || functionRoles == null)) {
			AuditHelpers.auditSecurityFailure(context, request, "unable to load prerequisite reference data for therapy update", 0, "Therapy")
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
		}
				
		// High level security check: must be the business admin / load tech or safety/deputy safety lead
		if (!(	permissions.user_permissions.indexOf('CAN_LOAD_THERAPIES') >= 0 ||
				permissions.user_permissions.indexOf('CAN_MANAGE_THERAPIES') >= 0 ||
				functionRoles.contains('Safety Lead') ||
				functionRoles.contains('Deputy Safety Lead'))) {
			AuditHelpers.auditSecurityFailure(context, request, "user failed permission pre-check for therapy update", 0, "Therapy")
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
		}
		
		ArrayList<Row> originalRows = the_db.getProject_GroupByName(projectFamily).getTable_rows() ;
		if (!originalRows.isEmpty()) {
			Row originalRow = originalRows.get(0);
			String originalJson = new JsonBuilder(originalRow.getCol_vals()).toString()
			if (the_db.updateProjectGroup(projectFamily, newProjectFamily, null)) {
				ArrayList<Row> newRows = the_db.getProject_GroupById(originalRow.getColVal("id")).getTable_rows() ;
				Row newRow = newRows.get(0);
				String newJson = new JsonBuilder(newRow.getCol_vals()).toString()
				
				long id = Long.parseLong(originalRow.getColVal("id").toString()) ;
				
				AuditHelpers.auditDifference(context, request, id, id, "Therapy", projectFamily, originalJson, newJson);
				AuditHelpers.clearAuditCaches() ;
												
				return buildResponse(responseBuilder, HttpServletResponse.SC_NO_CONTENT, null)
			} else
				return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"Error" : "Unknown error"}""")
		} else {
			return buildResponse(responseBuilder, HttpServletResponse.SC_NOT_FOUND,"""{"Error" : "Not Found"}""")
		}
	}
	
	RestApiResponse handleCreateProjectGroup(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def projectFamily = request.getParameter "f"
		
		def username = context.getApiSession().getUserName()
		logger.info("Create " + projectFamily + " project group by " + username) ;
		
		Tracker_CustDb the_db = DbUtils.initTracker_CustDbConfig(context);
		
		// Get User Permissions
		def permissions = fetchUserPermissions(request, username)
				
		// Fail if we could not load prerequisites needed for security checks
		if (permissions == null || permissions.user_permissions == null) {
			AuditHelpers.auditSecurityFailure(context, request, "unable to load prerequisite reference data for create therapy", 0, "Therapy")
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
		}
		
		// High level security check: must be the business admin / load tech or safety/deputy safety lead
		if (!(	permissions.user_permissions.indexOf('CAN_LOAD_THERAPIES') >= 0 ||
				permissions.user_permissions.indexOf('CAN_MANAGE_THERAPIES') >= 0)) {
			AuditHelpers.auditSecurityFailure(context, request, "user failed permission pre-check for create therapy", 0, "Therapy")
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
		}
		
		the_db.createProject_Group(projectFamily);				
		ArrayList<Row> newRows = the_db.getProject_GroupByName(projectFamily).getTable_rows() ;
		if (!newRows.isEmpty()) {
			Row newRow = newRows.get(0);
			String newJson = new JsonBuilder(newRow.getCol_vals()).toString()
			
			long id = Long.parseLong(newRow.getColVal("id").toString()) ;			
			AuditHelpers.auditDifference(context, request, id, id, "Therapy", projectFamily, null, newJson);
			AuditHelpers.clearAuditCaches() ;
												
			return buildResponse(responseBuilder, HttpServletResponse.SC_NO_CONTENT, null)
		} else {
			return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"Error" : "Unknown error"}""")
		}
	}
	
	RestApiResponse handleAddProjectsToProjectGroup(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def projectFamily = request.getParameter "f"
		def postRequest = new JsonSlurper().parseText(request.reader.readLines().join("\n"))
		
		def username = context.getApiSession().getUserName()
		logger.info("Associate " + projectFamily + " project group with projects by " + username) ;
		
		Tracker_CustDb the_db = DbUtils.initTracker_CustDbConfig(context);
		
		// Get User Permissions
		def permissions = fetchUserPermissions(request, username)
				
		// Fail if we could not load prerequisites needed for security checks
		if (permissions == null || permissions.user_permissions == null) {
			AuditHelpers.auditSecurityFailure(context, request, "unable to load prerequisite reference data for create therapy", 0, "Therapy")
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
		}
		
		// High level security check: must be the business admin / load tech or safety/deputy safety lead
		if (!(	permissions.user_permissions.indexOf('CAN_LOAD_THERAPIES') >= 0 ||
				permissions.user_permissions.indexOf('CAN_MANAGE_THERAPIES') >= 0)) {
			AuditHelpers.auditSecurityFailure(context, request, "user failed permission pre-check for create therapy", 0, "Therapy")
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
		}
				
		Map<Integer, String> originalJsonMap = the_db.getProject_GroupJsonByName(projectFamily);
		if (originalJsonMap != null) { 												
			if (the_db.setProjectProject_Group(projectFamily, postRequest)) {
				the_db.updateProject_GroupRegistration(projectFamily);
				Map<Integer, String> newJsonMap = the_db.getProject_GroupJsonByName(projectFamily);				
				long id = Long.parseLong(originalJsonMap.keySet().first().toString()) ;								
				AuditHelpers.auditDifference(context, request, id, id, "Therapy", projectFamily, originalJsonMap.values().first(), newJsonMap.values().first());
	
			} else {
				return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"Error" : "Unknown error"}""")
			}
		} else {
			return buildResponse(responseBuilder, HttpServletResponse.SC_NOT_FOUND,"""{"Error" : "Not Found"}""")
		}

		return buildResponse(responseBuilder, HttpServletResponse.SC_NO_CONTENT, null)
	}
	
	
	
	
	
    RestApiResponse handleUserFunctionUpdate(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        // Parse the POST request and turn it into an object
        def postRequest = new JsonSlurper().parseText(request.reader.readLines().join("\n"))
                        
        def username = context.getApiSession().getUserName()
        logger.info("Requesting update of user functions by " + username + ": " + postRequest.toString())
        
        Tracker_CustDb the_db = DbUtils.initTracker_CustDbConfig(context);
        
        // Get User Permissions and User Functions
        def permissions = fetchUserPermissions(request, username)
        def userFunctions = new JsonSlurper().parseText(the_db.buildJsonForProjectGroupsUsers(postRequest.proj_group).toString()) 
        
        // Determine our User function (Role) within project group
        def functionRoles = []
        for (Object userFunction: userFunctions) {
            if (userFunction.username && userFunction.username.compareToIgnoreCase(userFunction.username) == 0) {
				if (!functionRoles.contains(userFunction.user_function_id)) {
					functionRoles << userFunction.user_function_id
				}
            }
        }
        
        // Fail if we could not load prerequisites needed for security checks 				
        if ( (permissions == null || permissions.user_permissions == null || userFunctions == null || functionRoles == null)) {
            AuditHelpers.auditSecurityFailure(context, request, "unable to load prerequisite reference data for function update", 0, "User Function")
            return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
        }

        // High level security check: must have ability to set one of the roles or be the safety/deputy safety lead or TA				
        if (!(	permissions.user_permissions.indexOf('CAN_ASSIGN_THERAPY_SAFETY_LEAD') >= 0 ||
                permissions.user_permissions.indexOf('CAN_ASSIGN_THERAPY_DEPUTY_SAFETY_LEAD') >= 0 ||
                permissions.user_permissions.indexOf('CAN_ASSIGN_THERAPY_THERAPEUTIC_AREAHEAD') >= 0 ||
                permissions.user_permissions.indexOf('CAN_ASSIGN_THERAPY_DEPUTY_THERAPEUTIC_AREAHEAD') >= 0 ||
                functionRoles.contains('Safety Lead') ||
                functionRoles.contains('Deputy Safety Lead') ||
                functionRoles.contains('TA Head Patient Safety') ||
                functionRoles.contains('Deputy TA Head Patient Safety'))) {
            AuditHelpers.auditSecurityFailure(context, request, "user failed permission pre-check", 0, "User Function")
            return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
        }
                
        // Build a map of 'valid' ids and functions for later checks
        Map<String, String> idFunctions = new HashMap<String, String>()		
        for (Object userFunction: userFunctions) {
            idFunctions.put(userFunction.id, userFunction.user_function_id)
        }
        
        // Validate delete Ids: must be a valid id and we must have access to change that id
        if (postRequest.deleteIds && postRequest.deleteIds.size() > 0) {
            for (Long id: postRequest.deleteIds) {
                if (!canDelete(id, idFunctions, permissions.user_permissions)) {
                    AuditHelpers.auditSecurityFailure(context, request, "user " + username + "/" + functionRoles + "/" + postRequest.proj_group + " does not have permissions to delete record " + id, id, "User Function")
                    return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
                }
            }
        }
        
        // Validate Updates: must be a valid id (if existing) and we must have access to both old/new ids.
        if (postRequest.updates && postRequest.updates.size() > 0) {
            for (Object obj: postRequest.updates) {
                if (obj.proj_group.compareToIgnoreCase(postRequest.proj_group) != 0) {
                    AuditHelpers.auditSecurityFailure(context, request, "user " + username + "/" + functionRoles + "/" + postRequest.proj_group + " requested a change of an inconsistent project group: " + obj.proj_group, obj.id, "User Function")
                    return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
                }
                
                if (!canEdit(obj.id, obj.user_function, idFunctions, permissions.user_permissions)) {
                    AuditHelpers.auditSecurityFailure(context, request, "user " + username + "/" + functionRoles + "/" + postRequest.proj_group + " does not have permissions to add/edit record " + id, id, "User Function")
                    return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
                }
            }
        }				
        
        // Real work: Delete records			
        if (postRequest.deleteIds && postRequest.deleteIds.size() > 0) {
            def elements = new Long[postRequest.deleteIds.size()]
            for (int i=0; i<postRequest.deleteIds.size(); i++) {
                elements[i] = postRequest.deleteIds[i]
            }
            the_db.deleteUser_FunctionTableByIds(elements)					
        }
        
        // Real work: Update/Add new records
        if (postRequest.updates && postRequest.updates.size() > 0) {
            JsonIn jsonIn = new JsonIn()
            the_db.upsertUser_FunctionTableFromJson(jsonIn.userFunctionsFromJson(new JsonBuilder(postRequest.updates).toString()))
        }
        
        def newUserFunctions = new JsonSlurper().parseText(the_db.buildJsonForProjectGroupsUsers(postRequest.proj_group).toString())
        
        auditUserFunctionDifferences(context, request, userFunctions, newUserFunctions)				
        
        return buildResponse(responseBuilder, HttpServletResponse.SC_NO_CONTENT, null)				
    }

	
	/**
	 * Is the user able/entitled to edit the designated id and/or add a new entry?
	 *
	 * @param id
	 * @param idFunctions
	 * @param permissions
	 * @return
	 */
	boolean canEdit(Long id,  String userFunction, Map<String, String> idFunctions, ArrayList<String> permissions) {
		boolean allowed = false
		
		// logger.info("canEdit id " + id + ", userFunction" + userFunction)				
		if (id == 0) {
			// Must have a valid function type
			allowed = isEntitledUserFunction(userFunction, permissions)						
		} else {
			// id must exist for this group and the from/to function types must be valid						
			String role = idFunctions.get(Long.toString(id))			
			allowed = 
				role != null &&
				isEntitledUserFunction(role, permissions) &&
				isEntitledUserFunction(userFunction, permissions) 
		}
		
		return allowed 		
	}
	
	/**
	 * Is the user able/entitled to delete the designated id?
	 * 
	 * @param id
	 * @param idFunctions
	 * @param permissions
	 * @return
	 */
	boolean canDelete(Long id, Map<String, String> idFunctions, ArrayList<String> permissions) {
		boolean allowed = false
		
		// id must exist for this group and the function types must be valid					
		String role = idFunctions.get(Long.toString(id))
		allowed = role != null && isEntitledUserFunction(role, permissions)
		
		return allowed
	}
	
	
	/**
	 * Is the user entitled to make changes to the designated user function/role?
	 * 
	 * @param userFunction
	 * @param permissions
	 * @return
	 */
	boolean isEntitledUserFunction(String userFunction, ArrayList<String> permissions) {
		boolean valid = true
		
		if (userFunction.compareToIgnoreCase("Safety Lead") == 0) {
			valid = permissions.indexOf('CAN_ASSIGN_THERAPY_SAFETY_LEAD') >= 0
		} else if (userFunction.compareToIgnoreCase("Deputy Safety Lead") == 0) {
			valid = permissions.indexOf('CAN_ASSIGN_THERAPY_DEPUTY_SAFETY_LEAD') >= 0
		} else if (userFunction.compareToIgnoreCase("TA Head Patient Safety") == 0) {
			valid = permissions.indexOf('CAN_ASSIGN_THERAPY_THERAPEUTIC_AREAHEAD') >= 0
		} else if (userFunction.compareToIgnoreCase("Deputy TA Head Patient Safety") == 0) {
			valid = permissions.indexOf('CAN_ASSIGN_THERAPY_DEPUTY_THERAPEUTIC_AREAHEAD') >= 0
		}
		
		return valid		
	}
		
	/**
	 * Fetch user permissions from the permissionByUser REST extension, using 
	 * the http session found in the request to this REST extension
	 * 
	 * @param request
	 * @param username
	 * @return
	 */
	Object fetchUserPermissions(HttpServletRequest request, String username) {
/*
        TrustManager trustManger = 
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {  }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

            } ;

        TrustManager[] trustAllCerts = new TrustManager[1] ;
        trustAllCerts[0] = trustManger ; 

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
*/
		def permissions = null
		
		String url = request.getScheme() + "://" + request.getLocalName() + ":" + request.getLocalPort() +
				request.getContextPath() + "/API/extension/permissionsByUser?userName=" +
				URLEncoder.encode(username, "UTF-8") +
				"&p=0&c=100"
				
//		logger.info("fetchUserPermissions via " + url);
		
        def get = new URL(url).openConnection()
		get.setRequestProperty("cookie", request.getHeader("cookie"))
		def getRC = get.getResponseCode()
		if (getRC.equals(200)) {
			String payload = get.getInputStream().getText()
			permissions = new JsonSlurper().parseText(payload)
		} else {
			logger.error("Unable to fetch permissions from " + url + ", rc=" + getRC + ": " + get.getInputStream().getText())
		}
		return permissions
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
	
	AuditEntry createChangeAuditEntry(RestAPIContext context, HttpServletRequest request) {
		AuditEntry auditEntry = new AuditEntry()
		
		auditEntry.setDescription("Team Membership Changed")
		auditEntry.setIpAddress(request.getRemoteAddr())
		auditEntry.setIgnoreEntry(false)
		auditEntry.setUserId(context.getApiSession().getUserId())
		auditEntry.setUsername(context.getApiSession().getUserName())
		
		return auditEntry
	}

	boolean auditProjectFamilyImport(RestAPIContext context, HttpServletRequest request, String additionalDetails) {
		AuditEntry auditEntry = AuditHelpers.createImportAuditEntry(context, request, "Therapy") ;
		auditEntry.setAdditionalInfo(additionalDetails)
		return AuditHelpers.postAuditEntry(auditEntry)
	}
	
	boolean auditUserFunctionDifferences(RestAPIContext context, HttpServletRequest request, Object original, Object updated) {
		Set<Object> updatedRecorded = new HashSet<String>()
		Map<Object, Object> updatedMap = new HashMap<Object, Object>()
				
		for (Object userFunction: updated) {
			updatedMap.put(userFunction.id, userFunction)
		}
		
		// Start by iterating over all the originals
		for (Object originalFunction: original) {
			Object updatedFunction = updatedMap.get(originalFunction.id)
			updatedRecorded.add(originalFunction.id)
			
			String originalJson = new JsonBuilder(originalFunction).toString()
			String updatedJson = null
			if (updatedFunction != null)
				updatedJson = new JsonBuilder(updatedFunction).toString()
				
			if (updatedFunction == null || originalJson.compareTo(updatedJson) != 0) {
				AuditHelpers.auditDifference(context, request, Long.parseLong(originalFunction.proj_group_id_real_id_val), Long.parseLong(originalFunction.id), "User Function", 
						originalFunction.proj_group_id + " / " + originalFunction.user_function_id, originalJson, updatedJson);				
			}
		}
		
		// Next iterate over all updates that are left
		for (Object updatedFunction: updated) {
			if (!updatedRecorded.contains(updatedFunction.id)) {
				AuditHelpers.auditDifference(context, request, Long.parseLong(updatedFunction.proj_group_id_real_id_val), Long.parseLong(updatedFunction.id), "User Function",
					updatedFunction.proj_group_id + " / " + updatedFunction.user_function_id, null, new JsonBuilder(updatedFunction).toString());
			}
		}		
	}

    RestApiResponse handleProjectImport(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
		def username = context.getApiSession().getUserName()
		
		// Get User Permissions
		def permissions = fetchUserPermissions(request, username)
				
		// Fail if we could not load prerequisites needed for security checks
		if (permissions == null || permissions.user_permissions == null) {
			AuditHelpers.auditSecurityFailure(context, request, "unable to load prerequisite reference data for therapy", 0, "Therapy")
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
		}

		// Actual security check: must have CAN_LOAD_THERAPIES permissions
		if (!(	permissions.user_permissions.indexOf('CAN_LOAD_THERAPIES') >= 0 ) ) {
			AuditHelpers.auditSecurityFailure(context, request, "user failed permission pre-check for import", 0, "Therapy")
			return buildResponse(responseBuilder, HttpServletResponse.SC_FORBIDDEN, null)
		}
		
        List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request)
        FileItem importData = null
        for (FileItem item : items) {
            if (!item.isFormField() && item.getFieldName().compareToIgnoreCase("importData") == 0) {
                importData = item
                break
            }
        }

        if (importData != null) {
            Map<String, Object> results = new HashMap<String, Object>()
            
            try {
                Reader reader = new InputStreamReader(importData.getInputStream())
				CSVParser csvParser = null ;
				
                csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim())
				
				ProjectImporter projectImporter = new ProjectImporter(csvParser, importData.getName(), context)
				boolean success = projectImporter.validate()
				if (success) {
					success = projectImporter.process()
				}
				String logContent = projectImporter.getLog()
			
				results.put("log", logContent)
				results.put("success", new Boolean(success))
				results.put("warnings", new Integer(projectImporter.getWarnings()))
				results.put("errors", new Integer(projectImporter.getErrors()))
				results.put("filename", importData.getName())

				logger.info(logContent)
				
				auditProjectFamilyImport(context, request, logContent) ;
			
            } catch (Exception e) {
				
				String logContent = "Unexpected error: " + e.toString() ; 
				
				results.put("log", logContent)
				results.put("success", new Boolean(false))
				results.put("warnings", new Integer(0))
				results.put("errors", new Integer(1))
				results.put("filename", importData.getName())
			
				logger.error("Error during project import", e) ;
				logger.error(logContent)
				
				auditProjectFamilyImport(context, request, logContent) ;
            }

            return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(results).toString())
        }
        else {
            return buildResponse(responseBuilder, HttpServletResponse.SC_BAD_REQUEST,"""{"Error" : "Invalid request; missing importData"}""")
        }
    }        
}
