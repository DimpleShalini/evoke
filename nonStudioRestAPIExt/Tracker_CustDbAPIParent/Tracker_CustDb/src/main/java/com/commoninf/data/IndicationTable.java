package com.commoninf.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.commoninf.database.Database;
import com.commoninf.database.TableBase;
import com.commoninf.logger.CiiLogger;

public class IndicationTable extends TableBase {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.IndicationTable");
	
	/**************************************************************************
	 * NOTE:  IF THIS CONSTRUCTOR IS CALLED THEN THE METHOD TableBase.setDb
	 * MUST BE CALLED TO DEFINE THE DATABASE CONNECTION BEFORE ATTEMPTING TO
	 * Get/Set ANYTHING IN THE DATABASE!!!!!  The typical use case for this
	 * constructor would be when defining foreign keys in the Row.COLS
	 * definition
	 */
	public IndicationTable () {
		super (null, "indication", IndicationRow.COLS, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		IndicationRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 * @throws ClassNotFoundException 
	 */
	public IndicationTable (Database db) {
		super (db, "indication", IndicationRow.COLS, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		IndicationRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	@Override
	public void readRow(ResultSet rs) throws SQLException {
		table_rows.add(new IndicationRow(rs));
	}
	
	/**************************************************************************
	 * 
	 * @throws SQLException
	 */
	public void getAllIndicationsForAProjectCode (String project_code) throws SQLException {
		String [] project_codes = {project_code};
		
		PreparedStatement ps = null ;
		try {
			ps = getQueryForSpecificRows("project_id", project_codes);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				readRow(rs);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("SQL Exception thrown in getAllIndicationsForAProjectCode");
			logSqlException (e.getMessage());
		} finally {
			if (ps != null) {
				ps.close();
			}
		}		
	}
}
