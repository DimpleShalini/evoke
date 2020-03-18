package com.commoninf.rest.api

import groovy.sql.Sql
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.postgresql.Driver

public class Database {
	private static final Logger logger = LoggerFactory.getLogger("com.commoninf.rest.api.Database");
	private Connection db_conn = null;
	protected String jdbc_url;
	protected String db_url;
	protected String db_port;
	protected String db_name;
	protected String db_user_name;
	protected String db_password;
	private static String jdbc_class_name;
	private static String jdbc_driver_found_error_str = "";

	/**************************************************************************
	 *
	 */
	public Database(String db_url, String db_port, String db_name, String db_user_name, String db_password) {
		//jdbc_url = appBean.getAppProps ().getProperty ("jdbc_url");
		jdbc_url = "jdbc:postgresql";
		//jdbc_class_name = appBean.getAppProps ().getProperty ("jdbc_class_name");
		jdbc_class_name = "org.postgresql.Driver";
		this.db_url = db_url;
		this.db_port = db_port;
		this.db_name = db_name;
		this.db_user_name = db_user_name;
		this.db_password = db_password;
	}
	
	/**************************************************************************
	 *
	 */
	public static void findJDBCDriver () {
		try {
			Class.forName(jdbc_class_name);
			logger.info ("jdbc class found!");
		} catch (ClassNotFoundException e) {
			logger.info ("jdbc class not found! - "+e.getMessage());
			e.printStackTrace();
		}
	}

	/**************************************************************************
	 *
	 */
	public void getDbConnection() {
		String full_jdbc_url = jdbc_url+"://"+db_url+":"+db_port+"/"+db_name;
		String err_str = "";

		logger.info("Opening db connection to url: "+full_jdbc_url+" for user "+db_user_name);
		this.db_name = db_name;
		try {
			findJDBCDriver ();
			db_conn = DriverManager.getConnection(full_jdbc_url, db_user_name, db_password);
		} catch (SQLException e) {
			db_conn = null;
			logger.info("getDbConnection-exception "+e.getMessage()+" db_user="+db_user_name+": db_password="+db_password);
			e.printStackTrace();
		}
	}
	
	/**************************************************************************
	 * 
	 * @param query_str
	 * @throws SQLException
	 */
	public void runQuery (String query_str) throws SQLException {
		if (db_conn != null) {
			Statement st = db_conn.createStatement();
			ResultSet rs = st.executeQuery(query_str);
			while (rs.next())
			{
				getRow (rs);
			}
			rs.close();
			st.close();
		}
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 * @throws SQLException
	 */
	public void getRow (ResultSet rs) throws SQLException {
		logger.error("The child class must define getRow(ResultSet rs)");
	}
	
	/**************************************************************************
	 * 
	 */
	public void closeConnection () {
		if (db_conn != null) {
			db_conn.close();
			db_conn = null;
		}
	}
}
