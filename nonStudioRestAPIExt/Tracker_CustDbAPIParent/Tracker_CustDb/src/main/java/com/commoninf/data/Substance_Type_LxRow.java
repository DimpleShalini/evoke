package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class Substance_Type_LxRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Substance_Type_LxRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("substance_type"),
		new Column ("default_yn")
	};
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public Substance_Type_LxRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return Substance_Type_LxRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		Substance_Type_LxRow.COLS = cols;
	}
}
