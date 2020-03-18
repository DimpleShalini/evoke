/**
 * Copyright (c) 2019 Commonwealth Informatics Inc. All rights reserved.
 */
package com.commonwealth.audit

import groovy.json.JsonOutput;
import com.bonitasoft.engine.api.APIAccessor;
import com.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.identity.User;

class AuditEntry implements Serializable {
	public static final String MODULE_NAME="Tracker" ;
	
	public Long startTimestampMs;
	public Long endTimestampMs;
	public String moduleName;
	public Long userId;
	
	public String username;
	public String ipAddress;
	public String sessionId;
	
	public String description;
	public String additionalInfo;
	
	public Long objectId;
	public String objectType;
	
	public String objectJson;
	public String objectOriginalJson;
	
	public String relatedObjectType;
	public Long relatedObjectId;
	
	public Long projectGroupId;
	
	public Boolean ignoreEntry ;
		
	public AuditEntry() {	
		initialize();
	}
	
	public void initialize() {
		this.startTimestampMs = System.currentTimeMillis();
		this.endTimestampMs = this.startTimestampMs;
		this.moduleName = MODULE_NAME;
		this.ipAddress = "127.0.0.1";
		this.sessionId = "Unknown";
		this.ignoreEntry = false ; 
	}

	public void setStartTimestampMs(long startTimestampMs) {
		this.startTimestampMs = startTimestampMs ;
	}
	
	public long getStartTimestampMs() {
		return this.startTimestampMs ; 
	}
	
	public void setEndTimestampMs(long endTimestampMs) {
		this.endTimestampMs = endTimestampMs ;
	}
	
	public long getEndTimestampMs() {
		return this.endTimestampMs;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	
	public String getModuleName() {
		return this.moduleName;
	}

	public void setUserId(Long taskAssigneeId) {
		this.userId = taskAssigneeId;
	}
	
	public Long getUserId() {
		return this.userId ;
	}

	public void setUsername(String username) {
		this.username = username ;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress ;
	}
	
	public String getIpAddress() {
		return this.ipAddress;
	}
	
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId ;
	}
	
	public String getSessionId() {
		return this.sessionId;
	}

    public void setDescription(String description) {
	    this.description = description ;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo ;
	}
	
	public String getAdditionalInfo() {
		return this.additionalInfo ;
	}
	
	public void setObjectId(Long objectId, String objectType) {
		this.objectId = objectId;
		this.objectType = objectType;
	}
	
	public Long getObjectId() {
		return this.objectId;
	}
	
	public String getObjectType() {
		return this.objectType ;
	}
	
	public void setObjectJson(String objectJson) {
		this.objectJson = objectJson;
	}
	
	public String getObjectJson() {
		return this.objectJson ;
	}
	
	public void setOriginalObjectJson(String originalObjectJson) {
		this.objectOriginalJson = originalObjectJson;
	}
	
	public String getOriginalObjectJson() {
		return this.objectOriginalJson; 
	}	
	
	public void setRelatedObjectId(Long relatedObjectId, String relatedObjectType) {
		this.relatedObjectId = relatedObjectId ;
		this.relatedObjectType = relatedObjectType ;	
	}
	
	public Long getRelatedObjectId() {
		return this.relatedObjectId ;
	}
	
	public String getRelatedObjectType() {
		return this.relatedObjectType ;
	}

	public void setIgnoreEntry(Boolean ignoreEntry) {
		this.ignoreEntry = ignoreEntry ;
	}
		
	public Boolean getIgnoreEntry() {
		return this.ignoreEntry ;
	}

	public setProjectGroupId(Long projectGroupId) {
		this.projectGroupId = projectGroupId;
	}

	public Long getProjectGroupId() {
		return this.projectGroupId;
	}

	public String toJson() {
		JsonOutput output = new JsonOutput();
		return output.prettyPrint(JsonOutput.toJson(this));
	}	
}