package com.commoninf.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.commoninf.database.Database;
import com.commoninf.database.TableBase;
import com.commoninf.logger.CiiLogger;

public class Project_GroupTable extends TableBase {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Project_GroupTable");
	
	/**************************************************************************
	 * NOTE:  IF THIS CONSTRUCTOR IS CALLED THEN THE METHOD TableBase.setDb
	 * MUST BE CALLED TO DEFINE THE DATABASE CONNECTION BEFORE ATTEMPTING TO
	 * Get/Set ANYTHING IN THE DATABASE!!!!!  The typical use case for this
	 * constructor would be when defining foreign keys in the Row.COLS
	 * definition
	 */
	public Project_GroupTable () {
		super (null, "project_group", Project_GroupRow.COLS, Project_GroupRow.CHILD_TABLES, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		Project_GroupRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 * @throws ClassNotFoundException 
	 */
	public Project_GroupTable (Database db) {
		super (db, "project_group", Project_GroupRow.COLS, Project_GroupRow.CHILD_TABLES, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		Project_GroupRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	@Override
	public void readRow(ResultSet rs) throws SQLException {
		table_rows.add(new Project_GroupRow(rs));
	}
	
	/**************************************************************************
	 * 
	 * @throws SQLException
	 */
	public void getProjectGroupByName (String project_group_name) throws SQLException {
		String [] project_groups = {project_group_name};
		
		PreparedStatement ps = null ;
		try {
			ps = getQueryForSpecificRows("group_name", project_groups);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				readRow(rs);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("SQL Exception thrown in getProjectGroupByName");
			logSqlException (e.getMessage());
		}
		finally {
			if (ps != null) {
				ps.close();
			}
		}
	}
}