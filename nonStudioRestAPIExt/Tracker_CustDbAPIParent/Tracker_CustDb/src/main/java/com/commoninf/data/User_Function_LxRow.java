package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class User_Function_LxRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.User_Function_LxRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("function_name")
	};
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public User_Function_LxRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return User_Function_LxRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		User_Function_LxRow.COLS = cols;
	}
}
