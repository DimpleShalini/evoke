package com.commoninf.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.commoninf.database.Database;
import com.commoninf.database.TableBase;
import com.commoninf.logger.CiiLogger;

public class Substance_Type_LxTable extends TableBase {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Substance_Type_LxTable");
	
	/**************************************************************************
	 * NOTE:  IF THIS CONSTRUCTOR IS CALLED THEN THE METHOD TableBase.setDb
	 * MUST BE CALLED TO DEFINE THE DATABASE CONNECTION BEFORE ATTEMPTING TO
	 * Get/Set ANYTHING IN THE DATABASE!!!!!  The typical use case for this
	 * constructor would be when defining foreign keys in the Row.COLS
	 * definition
	 */
	public Substance_Type_LxTable () {
		super (null, "substance_type_lx", Substance_Type_LxRow.COLS, true, true);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		Substance_Type_LxRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 * @throws ClassNotFoundException 
	 */
	public Substance_Type_LxTable (Database db) {
		super (db, "substance_type_lx", Substance_Type_LxRow.COLS, true, true);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		Substance_Type_LxRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	@Override
	public void readRow(ResultSet rs) throws SQLException {
		table_rows.add(new Substance_Type_LxRow(rs));
	}
}
