package com.commoninf.data;

import java.sql.ResultSet;
import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class DataBaseInfoRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.DataBaseInfoRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("parameter"),
		new Column ("setting")
	};
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public DataBaseInfoRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return DataBaseInfoRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		DataBaseInfoRow.COLS = cols;
	}
}
