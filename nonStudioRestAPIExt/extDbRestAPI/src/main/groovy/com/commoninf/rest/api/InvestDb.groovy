package com.commoninf.rest.api

import java.sql.ResultSet
import java.sql.SQLException
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InvestDb extends Database {
	public class Versions {
		private String parameter;
		private String setting;

		public Versions(String parameter, String setting) {
			this.parameter = parameter;
			this.setting = setting;
		}
		public String getParameter() {
			return parameter;
		}

		public void setParameter(String parameter) {
			this.parameter = parameter;
		}

		public String getSetting() {
			return setting;
		}

		public void setSetting(String setting) {
			this.setting = setting;
		}
	};
	
	private static final Logger logger = LoggerFactory.getLogger("com.commoninf.rest.api.InvestDb");
	private ArrayList<Versions> verList;

	/**************************************************************************
	 * 
	 * @param db_url
	 * @param db_port
	 * @param db_name
	 * @param db_user_name
	 * @param db_password
	 */
	public InvestDb(String db_url, String db_port, String db_name, String db_user_name, String db_password) {
		super (db_url, db_port, db_name, db_user_name, db_password);
		
		verList = new ArrayList<Versions> ();
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public ArrayList<Versions> getVerList () {
		return verList;
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public void getRow (ResultSet rs) throws SQLException {
		int col_cnt = 1;
		String parameter = rs.getString (col_cnt++);
		String setting = rs.getString (col_cnt++);
		
		Versions ver = new Versions (parameter, setting);
		verList.add (ver);
	}

	/**************************************************************************
	 * 
	 */
	public void fetchAllVersions () {
		verList = new ArrayList<Versions> ();

		getDbConnection ();
		try {
			runQuery ("SELECT parameter, setting FROM\r\n" + "databaseinfo");
		}
		catch (SQLException e) {
			verList.add(new Versions ("Error", "SQL Error while fetching all versions: " + e.getMessage ()))
			logger.info ("SQL Error while fetching all versions: " + e.getMessage ());
			e.printStackTrace ();
		}
		finally {
			closeConnection ();
		}
	}
}
