/**
 * Copyright (c) 2019 Commonwealth Informatics Inc. All rights reserved.
 */
package com.commoninf.rest.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat

import javax.servlet.http.HttpServletRequest

import java.text.FieldPosition

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.commoninf.Tracker_CustDb.Tracker_CustDb
import com.commoninf.database.Row
import com.commoninf.data.High_Lvl_Prod_Type_LxTable
import com.commoninf.data.Substance_Type_LxTable
import com.commoninf.data.Mkt_Status_LxTable
import com.commoninf.data.Reg_Status_LxTable
import com.commoninf.data.Ther_Area_LxTable
import com.commoninf.data.Project_CoreTable
import com.commoninf.json.ProjectJson
import com.commonwealth.audit.AuditEntry

import groovy.json.JsonOutput

public class AuditHelpers {
	
	private static final Logger logger = LoggerFactory.getLogger(AuditHelpers.class)
	private static final String AUDIT_SERVICE_URL = "http://127.0.0.1:8280/auditservice/api/v1/" ; 
	
	public static boolean postAuditEntry(AuditEntry auditEntry) {
		String url = AUDIT_SERVICE_URL;
		boolean success = false
		def postRC
		
		try
		{
			def post = new URL(url).openConnection()
			post.setRequestMethod("POST")
			post.setDoOutput(true)
			post.setRequestProperty("Content-Type", "application/json")
			post.getOutputStream().write(auditEntry.toJson().getBytes("UTF-8"))
			postRC = post.getResponseCode()
			if(postRC.equals(200)) {
				success = true
			} else {
				logger.error("Unable to POST audit trail entry: response=", postRC + "/" + post.getInputStream().getText())
			}
		} catch (Exception e) {
			logger.error("Unable to POST audit trail entry", e)
		}
		return success
	}
	
	public static boolean clearAuditCaches() {
		String url = AUDIT_SERVICE_URL + "cache" ;
		boolean success = false
		def deleteRC
		
		try
		{
			def delete = new URL(url).openConnection()
			delete.setRequestMethod("DELETE")
			delete.setRequestProperty("Content-Type", "application/json")
			deleteRC = delete.getResponseCode()
			if(deleteRC.equals(200)) {
				success = true
			} else {
				logger.error("Unable to clear audit cache: response=", deleteRC + "/" + delete.getInputStream().getText())
			}
		} catch (Exception e) {
			logger.error("Unable to clear audit cache", e)
		}
		return success
	}

	
	public static boolean auditDifference(RestAPIContext context, 
		                                  HttpServletRequest request,
										  long projectFamilyId, 
										  long objectId, 
										  String objectType, 
										  String objectIdentity, 
										  String originalJson, 
										  String updatedJson) {
		AuditEntry auditEntry;
		
		if (originalJson != null)
			auditEntry = AuditHelpers.createChangeAuditEntry(context, request, objectType)
		else 
			auditEntry = AuditHelpers.createCreateAuditEntry(context, request, objectType)
	
		auditEntry.setObjectId(objectId, objectType)
				
		if (projectFamilyId > 0) {
		    auditEntry.setProjectGroupId(projectFamilyId)
		}
		
		if (updatedJson != null) {
			auditEntry.setObjectJson(updatedJson)
		}
		
		if (originalJson != null) {
			auditEntry.setOriginalObjectJson(originalJson)
		}
		
		// Set context preamble
		if (originalJson != null && updatedJson != null) {
			// Change
			auditEntry.setAdditionalInfo("Change details for " + objectIdentity + ":");
		} else if (originalJson != null) {
			// Have original, but no update: delete
			auditEntry.setAdditionalInfo("Deletion details for " + objectIdentity + ":");
		} else {
			// Have update, but not original: Insertion
			auditEntry.setAdditionalInfo("Insertion details for " + objectIdentity + ":");
		}

		return postAuditEntry(auditEntry)
	}
	

	public static boolean auditSecurityFailure(RestAPIContext context, HttpServletRequest request, String additionalDetails, long objectId, String objectType) {
		logger.error(additionalDetails)
		
		AuditEntry auditEntry = createSecurityAuditEntry(context, request)
		auditEntry.setAdditionalInfo(additionalDetails)
		
		if (objectId > 0) {
			auditEntry.setObjectId(objectId, objectType)
		}
				
		return AuditHelpers.postAuditEntry(auditEntry)
	}
	
	public static AuditEntry createSecurityAuditEntry(RestAPIContext context, HttpServletRequest request) {
		AuditEntry auditEntry = new AuditEntry()
		
		auditEntry.setDescription("Permission Failure")
		auditEntry.setIpAddress(request.getRemoteAddr())
		auditEntry.setIgnoreEntry(false)
		auditEntry.setUserId(context.getApiSession().getUserId())
		auditEntry.setUsername(context.getApiSession().getUserName())
		
		return auditEntry
	}
	
	public static AuditEntry createChangeAuditEntry(RestAPIContext context, HttpServletRequest request, String objectType) {
		AuditEntry auditEntry = new AuditEntry()
		
		auditEntry.setDescription(objectType + " Changed")
		auditEntry.setIpAddress(request.getRemoteAddr())
		auditEntry.setIgnoreEntry(false)
		auditEntry.setUserId(context.getApiSession().getUserId())
		auditEntry.setUsername(context.getApiSession().getUserName())
		
		return auditEntry
	}
	
	public static AuditEntry createCreateAuditEntry(RestAPIContext context, HttpServletRequest request, String objectType) {
		AuditEntry auditEntry = new AuditEntry()
		
		auditEntry.setDescription(objectType + " Created")
		auditEntry.setIpAddress(request.getRemoteAddr())
		auditEntry.setIgnoreEntry(false)
		auditEntry.setUserId(context.getApiSession().getUserId())
		auditEntry.setUsername(context.getApiSession().getUserName())
		
		return auditEntry
	}	
	
	public static AuditEntry createImportAuditEntry(RestAPIContext context, HttpServletRequest request, String objectType) {
		AuditEntry auditEntry = new AuditEntry()
		
		auditEntry.setDescription("Import Projects")
		auditEntry.setIpAddress(request.getRemoteAddr())
		auditEntry.setIgnoreEntry(false)
		auditEntry.setObjectId(null, objectType);
		auditEntry.setUserId(context.getApiSession().getUserId())
		auditEntry.setUsername(context.getApiSession().getUserName())
		
		return auditEntry
	}

}
