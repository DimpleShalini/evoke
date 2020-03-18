package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class Ther_Area_LxRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Ther_Area_LxRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("ther_area")
	};
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public Ther_Area_LxRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return Ther_Area_LxRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		Ther_Area_LxRow.COLS = cols;
	}
}
