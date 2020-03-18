package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class IndicationRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.IndicationRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("indication"),
		new Column ("project_id", new Project_CoreTable(), "id", "project_code"),
		new Column ("display_order")
	};
	
	/**************************************************************************
	 * 
	 */
	public IndicationRow() {
		super();
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public IndicationRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return IndicationRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		IndicationRow.COLS = cols;
	}
}
