package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class Reg_Status_LxRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Reg_Status_LxRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("reg_status")
	};
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public Reg_Status_LxRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return Reg_Status_LxRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		Reg_Status_LxRow.COLS = cols;
	}
}
