package com.commoninf.data;

import java.sql.ResultSet;

import com.commoninf.database.Column;
import com.commoninf.database.Row;
import com.commoninf.json.UserFunctionJson;
import com.commoninf.logger.CiiLogger;

public class User_FunctionRow extends Row {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.User_FunctionRow");
	
	// Define the columns for this table
	public static Column[] COLS = {
		new Column ("proj_group_id", new Project_GroupTable(), "id", "group_name"),
		new Column ("username"),
		new Column ("user_function_id", new User_Function_LxTable(), "id", "function_name"),
        new Column ("membership_type")
	};
	
	/**************************************************************************
	 * 
	 * * @param ujson
	 */
	public User_FunctionRow (UserFunctionJson ujson) {
		super ();
		
		for (Column col : getCOLS()) {
			col_vals.put(col.getName(), getCOL_JSON_Mapping(col.getName(), ujson));
		}
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public User_FunctionRow (ResultSet rs) {
		super (rs);
	}

	/**************************************************************************
	 * 
	 */
	@Override
	public Column[] getCOLS() {
		return User_FunctionRow.COLS;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public void setCOLS(Column[] cols) {
		User_FunctionRow.COLS = cols;
	}
	
	/**************************************************************************
	 * 
	 * @param fieldname
	 * @return
	 */
	public Object getCOL_JSON_Mapping (String col_name, UserFunctionJson json) {
		Object ret_val = null;
		
		if (col_name != null) {
			switch (col_name) {
			case "id":
			    ret_val = new Long(json.getId()) ;
			    break;
			case "user_function_id":
				ret_val = json.getUser_function();
				break;
			case "proj_group_id":				
				ret_val = json.getProj_group();
				break;
			case "username":
				ret_val = json.getUsername();
				break;
            case "membership_type":
                ret_val = json.getMembership_type();
                break;				
			default:
				if (!isAStdCol (col_name)) {
					logger.error ("Unknown column name in UserFunctionJson: "+col_name);
				}
				ret_val = "";
			}
		}
		
		return ret_val;
	}
}
