package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class Core_Data_SheetRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Core_Data_SheetRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("indication_full_text"),
		new Column ("project_group_id", new Project_GroupTable(), "id", "group_name")
	};
	
	/**************************************************************************
	 * 
	 */
	public Core_Data_SheetRow () {
		super ();
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public Core_Data_SheetRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return Core_Data_SheetRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		Core_Data_SheetRow.COLS = cols;
	}
}
