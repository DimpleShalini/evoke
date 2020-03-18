package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class SubstanceRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.SubstanceRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("substance_name"),
		new Column ("project_id", new Project_CoreTable(), "id", "project_code"),
		new Column ("sub_type_id", new Substance_Type_LxTable(), "id", "substance_type"),
		new Column ("display_order")
	};
	
	/**************************************************************************
	 * 
	 */
	public SubstanceRow () {
		super ();
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public SubstanceRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return SubstanceRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		SubstanceRow.COLS = cols;
	}
}
