package com.commoninf.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.commoninf.logger.CiiLogger;

public abstract class Row {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.database.Row");
	protected Map<String, Object> col_vals = null;
	
	/**************************************************************************
	 * 
	 */
	public Row () {
		col_vals = new HashMap<String, Object>();
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	public Row (ResultSet rs) {
		col_vals = new HashMap<String, Object>();
		
		for (Column col : getCOLS()) {
			try {
				setColVal(col.getName(), rs.getObject(col.getName()));
				if (col.isForeign_key()) {
					setColVal(col.getName()+Column.FK_ADDITIONAL_COL_NAME_SUFFIX, rs.getObject(col.getName()+Column.FK_ADDITIONAL_COL_NAME_SUFFIX));
				}
			} catch (SQLException e) {
				logger.error("SQL Error while reading table.  "+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**************************************************************************
	 * 
	 * @param col_name
	 * @param col_val
	 */
	public void addDerivedColumn (String col_name, Object col_val) {
		col_vals.put(col_name, col_val);
	}
	
	/**************************************************************************
	 * 
	 * @param table
	 */
	public void addChildTable (TableBase table) {
		col_vals.put(table.getTable_name(), table.getTable_rows());
	}
	
	/**************************************************************************
	 * 
	 * @param fieldName
	 * @return
	 */
	public Object getColVal (String fieldName) {
		return col_vals.get(fieldName);
	}
	
	/**************************************************************************
	 * 
	 * @param fieldName
	 * @param obj
	 */
	public void setColVal (String fieldName, Object obj) {
		col_vals.put(fieldName, obj);
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public Map<String, Object> getCol_vals () {
		return col_vals;
	}
	
	/**************************************************************************
	 * 
	 */
	@Override
	public String toString () {
		String disp_str = "";
		
		int i = 0;
		for (Column col : getCOLS()) {
			if (i!=0) {
				disp_str += ", ";
			}
			disp_str += col_vals.get(col.getName());
			i++;
		}
		
		return disp_str;
	}
	
	/**************************************************************************
	 * 
	 * @param get_cols
	 * @param col_names
	 * @return
	 */
	private static ArrayList<String> getNonExistentColumnNames (Column[] get_cols, String[] col_names) {
		ArrayList<String> columns_names = new ArrayList<String>(Arrays.asList(col_names));
		
		for (int i=0; i<get_cols.length; i++) {
			boolean col_already_exists_in_schema = false;
			int j=0;
			for (j=0; j<columns_names.size(); j++) {
				if (get_cols[i].getName().equals(columns_names.get(j))) {
					// The column already exists in the schema so don't add it
					col_already_exists_in_schema = true;
					break;
				}
			}
			if (col_already_exists_in_schema) {
				columns_names.remove(j);
			}
		}
		
		return columns_names;
	}
	
	/**************************************************************************
	 * 
	 * @param get_cols
	 * @param col_names
	 * @return
	 */
	private static Column[] createNonExistentColumns (Column[] get_cols, ArrayList<String> col_names, boolean prepend) {
		if (col_names.size() != 0) {
			int curr_index = col_names.size();
			get_cols = Arrays.copyOf(get_cols, get_cols.length+col_names.size());
			if (prepend) {
				// Now shift all of the columns up by one and add the id as the first column
				for (int i=get_cols.length-(col_names.size()+1); i>=0; i--) {
					get_cols[i+1] = get_cols[i];
				}
				curr_index = 0;
				for (String name : col_names) {
					get_cols[curr_index] = new Column (name);
					curr_index++;
				}
			}
			else {
				for (String name : col_names) {
					get_cols[get_cols.length-curr_index] = new Column (name);
					curr_index--;
					if (curr_index < 0) {
						logger.error("Indexing error in column array");
						break;
					}
				}
			}
		}
		
		return get_cols;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public static String[] getCreatedUpdatedValidColumnNames () {
		String[] col_names = {"create_timestamp","create_user","update_timestamp","update_user","isvalid_yn"};
		
		return col_names;
	}
	
	/**************************************************************************
	 * 
	 * @param col_name
	 * @return
	 */
	public static boolean isAStdCol (String col_name) {
		boolean ret_val = false;
		
		List<String> list = Arrays.asList(getCreatedUpdatedValidColumnNames());
        
		if ((col_name.equals("id"))||
			(list.contains(col_name))) {
			ret_val = true;
		}
        
		return ret_val;
	}
	
	/**************************************************************************
	 * 
	 * @param get_cols
	 * @return
	 */
	public static Column[] addCreatedUpdatedValidColumns (Column[] get_cols) {
		ArrayList<String> columns_names = getNonExistentColumnNames (get_cols, getCreatedUpdatedValidColumnNames());
		
		return createNonExistentColumns (get_cols, columns_names, false);
	}
	
	/**************************************************************************
	 * 
	 * @param get_cols
	 * @return
	 */
	public static Column[] addIdColumn (Column[] get_cols) {
		String[] col_names = {"id"};
		ArrayList<String> columns_names = getNonExistentColumnNames (get_cols, col_names);
		
		return createNonExistentColumns (get_cols, columns_names, true);
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public abstract Column[] getCOLS ();
	
	/**************************************************************************
	 * 
	 * @param cols
	 */
	public abstract void setCOLS(Column[] cols);
}
