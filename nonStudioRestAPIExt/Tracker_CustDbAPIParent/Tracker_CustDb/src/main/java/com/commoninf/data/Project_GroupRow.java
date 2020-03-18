package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Database;
import com.commoninf.database.Row;
import com.commoninf.json.ProjectJson;
import com.commoninf.logger.CiiLogger;

public class Project_GroupRow extends Row {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Project_GroupRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("group_name"),
		new Column ("indication_full_text"),
		new Column ("registered_yn")
	};
	
	// Define the child_tables for this table
	public static Column[] CHILD_TABLES = {
		// Add all the child tables that project_group uses
		new Column (Project_CoreTable.class),
		new Column (Core_Data_SheetTable.class),
		new Column (User_FunctionTable.class)
	};
	
	
	/**************************************************************************
	 * 
	 */
	public Project_GroupRow() {
		super ();
	}	
	
	/**************************************************************************
	 * 
	 * @param pjson
	 * @param db
	 */
	public Project_GroupRow (ProjectJson pjson, Database db) {
		super ();
		
		for (Column col : getCOLS()) {
			col_vals.put(col.getName(), getCOL_JSON_Mapping(col.getName(), pjson));
		}
		
		// TODO: Eventually do something here for the core_data_sheet table
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public Project_GroupRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return Project_GroupRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		Project_GroupRow.COLS = cols;
	}
	
	/**************************************************************************
	 * 
	 * @param fieldname
	 * @return
	 */
	public Object getCOL_JSON_Mapping (String col_name, ProjectJson json) {
		Object ret_val = null;
		
		if (col_name != null) {
			switch (col_name) {
			case "group_name":
				ret_val = json.getProject_family();
				break;
			case "registered_yn":
				// This is a derived field that needs to be calculated based
				// on all associated projects
				ret_val = "N";
				break ;
			default:
				if (!isAStdCol (col_name)) {
					logger.error ("Unknown column name in ProjectJson: "+col_name);
				}
				ret_val = "";
			}
		}
		
		return ret_val;
	}
}
