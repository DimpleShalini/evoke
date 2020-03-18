package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;

public class FormulationRow extends Row {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.FormulationRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("formulation"),
		new Column ("project_id", new Project_CoreTable(), "id", "project_code"),
		new Column ("display_order")
	};

	/**************************************************************************
	 * 
	 */
	public FormulationRow() {
		super();
	}

	/**************************************************************************
	 * 
	 * @param rs
	 */
	public FormulationRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return FormulationRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		FormulationRow.COLS = cols;
	}
}
