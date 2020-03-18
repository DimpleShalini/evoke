package com.commoninf.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.function.Consumer;

// import org.postgresql.Driver; // Might need this when running in the REST context

import com.commoninf.logger.CiiLogger;
import com.commoninf.utils.PropertyUtils;

public class Database {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.database.Database");
	private Properties appProps = new Properties();
	private Connection db_conn = null;
	protected String jdbc_url;
	protected String db_url;
	protected String db_port;
	protected String db_name;
	protected String db_user_name;
	protected String db_password;
	private String full_jdbc_url;
	private static String jdbc_class;
	private int nested_open_db_count;
	
	/**************************************************************************
	 * 
	 * @param name
	 */
	public Database (String name) {
		PropertyUtils.loadProperties (appProps, "/configuration.properties", false, false);
		logger.debug("Default info read in from configuration.properties for the database "+name);
		init (  (String)appProps.get(name+"_jdbc_url"),
				(String)appProps.get(name+"_jdbc_class"),
				(String)appProps.get(name+"_url"),
				(String)appProps.get(name+"_port"),
				(String)appProps.get(name+"_name"),
				(String)appProps.get(name+"_user_name"),
				(String)appProps.get(name+"_password"));
	}

	/**************************************************************************
	 * 
	 * @param jdbc_url
	 * @param jdbc_class
	 * @param db_url
	 * @param db_port
	 * @param db_name
	 * @param db_user_name
	 * @param db_password
	 */
	public Database(String jdbc_url, String jdbc_class, String db_url, String db_port, String db_name, String db_user_name, String db_password) {
		logger.debug("Using passed in database configuration.");
		init (jdbc_url, jdbc_class, db_url, db_port, db_name, db_user_name, db_password);
	}
	
	/**************************************************************************
	 * 
	 * @param jdbc_url
	 * @param jdbc_class
	 * @param db_url
	 * @param db_port
	 * @param db_name
	 * @param db_user_name
	 * @param db_password
	 */
	private void init (String jdbc_url, String jdbc_class, String db_url, String db_port, String db_name, String db_user_name, String db_password) {
		this.jdbc_url = jdbc_url;
		this.nested_open_db_count = 0;
		Database.jdbc_class = jdbc_class;
		this.db_url = db_url;
		this.db_port = db_port;
		this.db_name = db_name;
		this.db_user_name = db_user_name;
		this.db_password = db_password;
		this.full_jdbc_url = jdbc_url+"://"+db_url+":"+db_port+"/"+db_name;
		
		logger.debug("Database configuration:"+jdbc_url+", "+jdbc_class+", "+db_url+", "+db_port+", "+db_name+", "+db_user_name);
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public Connection getDb_conn () {
		return db_conn;
	}
	
	/**************************************************************************
	 *
	 */
	public static void findJDBCDriver () {
		try {
			Class.forName(jdbc_class);
			//logger.info ("jdbc class found!");
		} catch (ClassNotFoundException e) {
			logger.error ("jdbc class not found! - "+e.getMessage());
			e.printStackTrace();
		}
	}

	/**************************************************************************
	 *
	 */
	public synchronized void getDbConnection() {
		if (db_conn == null) {
			logger.debug("Opening db connection to url: "+full_jdbc_url+" for user "+db_user_name);
			try {
				findJDBCDriver ();
				db_conn = DriverManager.getConnection(full_jdbc_url, db_user_name, db_password);
				db_conn.setAutoCommit(true);
				nested_open_db_count++;
			} catch (SQLException e) {
				db_conn = null;
				//logger.info("getDbConnection-exception "+e.getMessage()+" db_user="+db_user_name+": db_password="+db_password);
				e.printStackTrace();
			}
		}
		else {
			nested_open_db_count++;
		}
	}
	
	/**************************************************************************
	 * 
	 */
	public synchronized void closeConnection () {
		if (db_conn != null) {
			nested_open_db_count--;
			if (nested_open_db_count <= 0) {
				nested_open_db_count = 0;
				try {
					//db_conn.commit();
					db_conn.close();
					logger.debug("Closed the db connection to url: "+full_jdbc_url+" for user "+db_user_name);
				} catch (SQLException e) {
					logger.error("Unable to commit and close db_conn");
					e.printStackTrace();
				}
				db_conn = null;
			}
		}
		else {
			nested_open_db_count = 0;
		}
	}
	
	/**************************************************************************
	 * 
	 */
	public void rollback () { 
		if (db_conn != null) {
			try {
				db_conn.rollback();
			} catch (SQLException e) {
				logger.error("Unable to rollback db_conn");
				e.printStackTrace();
			}
		}
	}
	
	/**************************************************************************
	 * 
	 * @param update_str
	 * @param set_in_params
	 * @throws SQLException
	 */
	public void runPreparedStatement (String update_str, Consumer<PreparedStatement> set_in_params) throws SQLException {
		getDbConnection();
		
		//logger.info("Prepared statement: "+update_str);
		if (getDb_conn() != null) {
			PreparedStatement statement = getDb_conn().prepareStatement(update_str);
			set_in_params.accept(statement);
			statement.executeBatch ();
		}
		else {
			logger.error("Unable to get a connection to the database in runPreparedStatement.");
		}
		
		closeConnection();
	}
	
	/**************************************************************************
	 * 
	 * @param query_str
	 * @param get_row_cb
	 * @throws SQLException
	 */
	public void runQuery (String query_str, Consumer<ResultSet> get_row_cb) throws SQLException { 
		getDbConnection();
		
		if (getDb_conn() != null) {
			try {
			    Statement st = getDb_conn().createStatement();
			    ResultSet rs;
				rs = st.executeQuery(query_str);
			    while (rs.next()) {
				    get_row_cb.accept(rs);
			    }
			    rs.close();
			    st.close();
			} catch (SQLException e) {
				logger.error("SQL Exception on: "+query_str);
				throw e;
			}
		}
		else {
			logger.error("Unable to get a connection to the database in runQuery.");
		}
		
		closeConnection();
	}
	
    /**************************************************************************
     * 
     * @param query_str
     * @throws SQLException
     */
    public void runUpdate (String update_str) throws SQLException { 
        getDbConnection();
        
        if (getDb_conn() != null) {
            try {
                Statement st = getDb_conn().createStatement();
                st.executeUpdate(update_str);
                st.close();
            } catch (SQLException e) {
                logger.error("SQL Exception on: "+update_str);
                throw e;
            }
        }
        else {
            logger.error("Unable to get a connection to the database in runUpdate.");
        }
        
        closeConnection();
    }
}

