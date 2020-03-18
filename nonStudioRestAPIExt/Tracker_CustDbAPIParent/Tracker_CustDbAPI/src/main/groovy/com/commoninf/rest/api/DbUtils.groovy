/**
 * Copyright (c) 2019 Commonwealth Informatics Inc. All rights reserved.
 */
package com.commoninf.rest.api

import org.bonitasoft.web.extension.ResourceProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.commoninf.Tracker_CustDb.Tracker_CustDb

class DbUtils {
	private static final Logger logger = LoggerFactory.getLogger(DbUtils.class);
	
	public static groovyProps = new Properties ();
	
	/**************************************************************************
	 * Load a property file into a java.util.Properties
	 * @param fileName
	 * @param resourceProvider
	 * @return
	 */
	private static Properties loadProperties(String fileName, ResourceProvider resourceProvider) {
		Properties props = new Properties()
		resourceProvider.getResourceAsStream(fileName).withStream { InputStream s ->
			props.load s
		}
		return props;
	}
	
	/**************************************************************************
	 * 
	 * @param context
	 * @return
	 */
	public static Tracker_CustDb initTracker_CustDbConfig (RestAPIContext context) {
		Tracker_CustDb the_db = null;
		
		groovyProps = loadProperties("configuration.properties", context.resourceProvider);
		//PropertyUtils.loadProperties (groovyProps, "/configuration.properties", false, false);
		//logger.info("test_prop="+(String)groovyProps.get("test_prop"));
		String jdbc_url = (String)groovyProps.get("tracker_cust_jdbc_url");
		String jdbc_class = (String)groovyProps.get("tracker_cust_jdbc_class");
		String url = (String)groovyProps.get("tracker_cust_url");
		String port = (String)groovyProps.get("tracker_cust_port");
		String name = (String)groovyProps.get("tracker_cust_name");
		String user_name = (String)groovyProps.get("tracker_cust_user_name");
		String password = (String)groovyProps.get("tracker_cust_password");
		
		// If any of the properties are not defined then use the default
		if ((jdbc_url == null)||
			(jdbc_class == null)||
			(url == null)||
			(port == null)||
			(name == null)||
			(user_name == null)||
			(password == null)) {
			the_db = new Tracker_CustDb();
		}
		else {
			the_db = new Tracker_CustDb(jdbc_url, jdbc_class, url, port, name, user_name, password);
		}
		
		return the_db;
	}
}
