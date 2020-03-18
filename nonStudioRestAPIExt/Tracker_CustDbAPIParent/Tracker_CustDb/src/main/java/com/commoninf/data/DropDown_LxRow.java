package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class DropDown_LxRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.DropDown_LxRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("id"),
		new Column ("name"),
		new Column ("display_order"),
		new Column ("isvalid_yn"),
		new Column ("isdefault_yn"),
		new Column ("add_cfg_values")
	};
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public DropDown_LxRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return DropDown_LxRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		DropDown_LxRow.COLS = cols;
	}
}
