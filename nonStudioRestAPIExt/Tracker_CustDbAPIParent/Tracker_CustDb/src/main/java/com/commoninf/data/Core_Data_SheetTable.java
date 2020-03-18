package com.commoninf.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.commoninf.database.Database;
import com.commoninf.database.TableBase;
import com.commoninf.logger.CiiLogger;

public class Core_Data_SheetTable extends TableBase {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.User_FunctionTable");
	
	/**************************************************************************
	 * NOTE:  IF THIS CONSTRUCTOR IS CALLED THEN THE METHOD TableBase.setDb
	 * MUST BE CALLED TO DEFINE THE DATABASE CONNECTION BEFORE ATTEMPTING TO
	 * Get/Set ANYTHING IN THE DATABASE!!!!!  The typical use case for this
	 * constructor would be when defining foreign keys in the Row.COLS
	 * definition
	 */
	public Core_Data_SheetTable () {
		super (null, "core_data_sheet", Core_Data_SheetRow.COLS, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		Core_Data_SheetRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 * @throws ClassNotFoundException 
	 */
	public Core_Data_SheetTable (Database db) {
		super (db, "core_data_sheet", Core_Data_SheetRow.COLS, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		Core_Data_SheetRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	@Override
	public void readRow(ResultSet rs) throws SQLException {
		table_rows.add(new Core_Data_SheetRow(rs));
	}
	
	/**************************************************************************
	 * 
	 * @throws SQLException
	 */
	public void getAllCoreDataSheetsForAProjectGroup (String project_group) throws SQLException {
		String [] project_groups = {project_group};
		
		PreparedStatement ps = null ;
		try {
			ps = getQueryForSpecificRows("project_group_id", project_groups);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				readRow(rs);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("SQL Exception thrown in getAllCoreDataSheetsForAProjectGroup");
			logSqlException (e.getMessage());
		} finally {
			if (ps != null) {
				ps.close();
			}
		}		
	}
}
