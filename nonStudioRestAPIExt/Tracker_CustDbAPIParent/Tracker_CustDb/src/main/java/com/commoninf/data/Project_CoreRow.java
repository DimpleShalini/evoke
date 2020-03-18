package com.commoninf.data;

import java.sql.ResultSet;
import java.util.ArrayList;

import com.commoninf.database.Column;
import com.commoninf.database.Database;
import com.commoninf.database.Row;
import com.commoninf.json.ProjectJson;
import com.commoninf.logger.CiiLogger;

public class Project_CoreRow extends Row {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.Project_CoreRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("project_code"),
		new Column ("name_derived"),
		new Column ("corporate_div"),
		new Column ("substance_display"),
		new Column ("formulation_display"),
		new Column ("indication_display"),
		new Column ("high_lvl_prod_type_id", new High_Lvl_Prod_Type_LxTable(), "id", "prod_type"),
		new Column ("reg_status_id", new Reg_Status_LxTable(), "id", "reg_status"),
		new Column ("compound_code"),
		new Column ("mkt_status_id", new Mkt_Status_LxTable(), "id", "mkt_status"),
		new Column ("ther_area_id", new Ther_Area_LxTable(), "id", "ther_area"),
		new Column ("proj_complexity"),
		new Column ("complexity_class"),
		new Column ("duty_of_care"),
		new Column ("enabled_yn"),
		new Column ("reason_disabled"),
		new Column ("proj_group_id", new Project_GroupTable(), "id", "group_name"),
	};
	
	// Define the child_tables for this table
	public static Column[] CHILD_TABLES = {
		// Add all the child tables that project_core uses
		new Column (SubstanceTable.class),
		new Column (FormulationTable.class),
		new Column (Global_Brand_NameTable.class),
		new Column (IndicationTable.class)
	};
	
	/**************************************************************************
	 * 
	 * @param pjson
	 * @param db
	 */
	public Project_CoreRow (ProjectJson pjson, Database db) {
		super ();
		
		for (Column col : getCOLS()) {
			col_vals.put(col.getName(), getCOL_JSON_Mapping(col.getName(), pjson));
		}
		
		for (Column col : CHILD_TABLES) {
			ArrayList<String> child_table_vals = getChildTable_JSON_Mapping (col.getC_table_class(), pjson);
			if (child_table_vals.size() > 0) {
				Row t_row = null;
				if (col.getTable() == null) {
					col.instantiateChildTable();
					col.getTable().setDb(db);
				}
				// Put values into child table
				switch (col.getC_table_class().getSimpleName()) {
				case "SubstanceTable":
					for (String val : child_table_vals) {
						t_row = new SubstanceRow ();
						t_row.setColVal("sub_type_id", pjson.getSubstance_name_sources());
						t_row.setColVal("substance_name", val);
						t_row.setColVal("project_id", getCOL_JSON_Mapping("project_code", pjson));
						col.getTable().addTableRow(t_row);
					}
					break;
				case "FormulationTable":
					for (String val : child_table_vals) {
						t_row = new FormulationRow ();
						t_row.setColVal("formulation", val);
						t_row.setColVal("project_id", getCOL_JSON_Mapping("project_code", pjson));
						col.getTable().addTableRow(t_row);
					}
					break;
				case "Global_Brand_NameTable":
					int gbn_cnt = 0;
					for (String val : child_table_vals) {
						t_row = new Global_Brand_NameRow ();
						t_row.setColVal("preferred_flag_yn", gbn_cnt==0?"Y":"N");
						t_row.setColVal("global_brand_name", val);
						t_row.setColVal("project_id", getCOL_JSON_Mapping("project_code", pjson));
						col.getTable().addTableRow(t_row);
						gbn_cnt++;
					}
					break;
				case "IndicationTable":
					for (String val : child_table_vals) {
						t_row = new IndicationRow ();
						t_row.setColVal("indication", val);
						t_row.setColVal("project_id", getCOL_JSON_Mapping("project_code", pjson));
						col.getTable().addTableRow(t_row);
					}
					break;
				default:
					break;
				}
			}
		}
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public Project_CoreRow (ResultSet rs) {
		super (rs);
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return Project_CoreRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		DataBaseInfoRow.COLS = cols;
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
			case "project_code":
				ret_val = json.getProject_code();
				break;
			case "name_derived":
				ret_val = json.getProject_code()+":"+json.getIndication_short_form();
				break;
			case "corporate_div":
				ret_val = json.getDivision();
				break;
			case "substance_display":
				ret_val = json.getInn();
				break;
			case "formulation_display":
				ret_val = json.getFormulation();
				break;
			case "indication_display":
				ret_val = json.getIndication_short_form();
				break;
			case "high_lvl_prod_type_id":
				ret_val = json.getProduct_type();
				break;
			case "reg_status_id":
				ret_val = json.getRegistration_status();
				break;
			case "compound_code":
				ret_val = json.getCompound_code();
				break;
			case "mkt_status_id":
				ret_val = json.getMarketing_status();
				break;
			case "ther_area_id":
				ret_val = json.getTherapeutic_area();
				break;
			case "proj_complexity":
				ret_val = json.getProduct_complexity_classification();
				break;
			case "complexity_class":
				ret_val = json.getComplexity_classification();
				break;
			case "duty_of_care":
				ret_val = json.getDuty_of_care();
				break;
			case "enabled_yn":
				ret_val = json.getEnabled();
				if (ret_val.equals("")) {
					ret_val = "Y";  // Default to enabled
				}
				break;
			case "reason_disabled":
				ret_val = json.getReason_disabled();
				break;
			case "proj_group_id":
				ret_val = json.getProject_family();
				break;
			default:
				if (!isAStdCol (col_name)) {
					logger.error ("Unknown column name in ProjectJson: "+col_name);
				}
				ret_val = "";
			}
		}
		
		return ret_val;
	}
	
	/**************************************************************************
	 * 
	 * @param child_table_class
	 * @param json
	 * @return
	 */
	public ArrayList<String> getChildTable_JSON_Mapping (Class<?> child_table_class, ProjectJson json) {
		ArrayList<String> ret_val = null;
		
		if (child_table_class != null) {
			String table_class_name = child_table_class.getSimpleName();
			switch (table_class_name) {
			case "SubstanceTable":
				ret_val = json.getInn_array();
				break;
			case "FormulationTable":
				ret_val = json.getFormulation_array();
				break;
			case "Global_Brand_NameTable":
				ret_val = json.getGlobal_brand_name_array();
				break;
			case "IndicationTable":
				ret_val = json.getIndication_short_form_array();
				break;
			default:
				logger.error ("Unknown child table in ProjectJson: "+table_class_name);
				ret_val = new ArrayList<String>();
			}
		}
		
		return ret_val;
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public String toString () {
		String ret_str = super.toString();
		
		ret_str += ", Child Tables:";
		
		int table_cnt = 0;
		for (Column col : CHILD_TABLES) {
			if (col.getTable() != null) {
				if (table_cnt != 0) {
					ret_str += ", ";
				}
				table_cnt++;
				ret_str += "[";
				int row_cnt = 0;
				for (Row curr_row : col.getTable().getTable_rows()) {
					if (row_cnt != 0) {
						ret_str += ", ";
					}
					row_cnt++;
					ret_str += (row_cnt+":(");
					ret_str += curr_row.toString();
					ret_str += ")";
				}
				ret_str += "]";
			}
		}
		return ret_str;
	}
}
