package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class Global_Brand_NameRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Global_Brand_NameRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("global_brand_name"),
		new Column ("project_id", new Project_CoreTable(), "id", "project_code"),
		new Column ("display_order"),
		new Column ("preferred_flag_yn")
	};

	/**************************************************************************
	 * 
	 */
	public Global_Brand_NameRow() {
		super();
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public Global_Brand_NameRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return Global_Brand_NameRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		Global_Brand_NameRow.COLS = cols;
	}
}
