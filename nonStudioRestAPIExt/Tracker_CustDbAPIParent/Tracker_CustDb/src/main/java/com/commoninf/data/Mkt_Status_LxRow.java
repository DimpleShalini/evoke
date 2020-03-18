package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class Mkt_Status_LxRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Mkt_Status_LxRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("mkt_status")
	};
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public Mkt_Status_LxRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return Mkt_Status_LxRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		Mkt_Status_LxRow.COLS = cols;
	}
}
