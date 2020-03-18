package com.commoninf.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.commoninf.database.Database;
import com.commoninf.database.TableBase;
import com.commoninf.logger.CiiLogger;

public class User_FunctionTable extends TableBase {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.User_FunctionTable");
	
	/**************************************************************************
	 * NOTE:  IF THIS CONSTRUCTOR IS CALLED THEN THE METHOD TableBase.setDb
	 * MUST BE CALLED TO DEFINE THE DATABASE CONNECTION BEFORE ATTEMPTING TO
	 * Get/Set ANYTHING IN THE DATABASE!!!!!  The typical use case for this
	 * constructor would be when defining foreign keys in the Row.COLS
	 * definition
	 */
	public User_FunctionTable () {
		super (null, "user_function", User_FunctionRow.COLS, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		User_FunctionRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 * @throws ClassNotFoundException 
	 */
	public User_FunctionTable (Database db) {
		super (db, "user_function", User_FunctionRow.COLS, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		User_FunctionRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	@Override
	public void readRow(ResultSet rs) throws SQLException {
		table_rows.add(new User_FunctionRow(rs));
	}
	
	/**************************************************************************
	 * 
	 * @throws SQLException
	 */
	public void getAllUserFunctionsForAUser (String username) throws SQLException {
		String [] usernames = {username};
		String query_str;
		
		if (username.equals("all_users")) {
			query_str = getQueryStringForAllRows ();
			db.runQuery (query_str, (ResultSet rs)->{
				try {
					readRow(rs);
				} catch (SQLException e) {
					logger.error("SQL Exception thrown in getAllUserFunctionsForAUser");
					logSqlException (e.getMessage());
				}
			});
		}
		else {
			PreparedStatement ps = null ;
			try {
				ps = getQueryForSpecificRows("username", usernames);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					readRow(rs);
				}
				rs.close();
			} catch (SQLException e) {
				logger.error("SQL Exception thrown in getAllUserFunctionsForAUser");
				logSqlException (e.getMessage());
			}
			finally {
				if (ps != null) {
					ps.close();
				}
			}
		}		
	}
	
    /**************************************************************************
     * 
     * @throws SQLException
     */
    public void deleteByIds(Long ids[]) throws SQLException {
    	PreparedStatement ps = null ;
		try {
			ps = getDeleteForSpecificRows("id", ids) ;
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL Exception thrown in deleteByIds");
			logSqlException (e.getMessage());
		}
		finally {
			if (ps != null) {
				ps.close();
			}
		}
    }	
	
	/**************************************************************************
	 * 
	 * @throws SQLException
	 */
	public void getAllUserFunctionsForAProjectGroup (String project_group) throws SQLException {
		String [] project_groups = {project_group};
				
		PreparedStatement ps = null ;
		try {
			ps = getQueryForSpecificRows("proj_group_id", project_groups);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				readRow(rs);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("SQL Exception thrown in getAllUserFunctionsForAProjectGroup");
			logSqlException (e.getMessage());
		}
		finally {
			if (ps != null) {
				ps.close();
			}
		}
	}
}
