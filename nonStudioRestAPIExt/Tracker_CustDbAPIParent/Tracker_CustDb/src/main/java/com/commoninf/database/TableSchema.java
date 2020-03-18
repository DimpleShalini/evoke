package com.commoninf.database;

import java.util.Map;
import java.util.TreeMap;

public class TableSchema {
	int column_cnt;
	int child_table_cnt;
	Map<Integer, Column> column_schema = null;
	Map<Integer, Column> child_table_schema = null;
	
	/**************************************************************************
	 * 
	 */
	public TableSchema () {
		column_cnt = 0;
		child_table_cnt = 0;
		column_schema = new TreeMap<Integer, Column>();
		child_table_schema = new TreeMap<Integer, Column>();
	}
	
	/**************************************************************************
	 * 
	 * @param col
	 */
	public void addColumn (Column col) {
		column_schema.put(column_cnt, col);
		column_cnt++;
	}
	
	/**************************************************************************
	 * 
	 * @param col
	 */
	public void addChild_table (Column child_table) {
		child_table_schema.put(child_table_cnt, child_table);
		child_table_cnt++;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public Map<Integer, Column> getColumn_schema () {
		return column_schema;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public Map<Integer, Column> getChild_table_schema () {
		return child_table_schema;
	}
	
	/**************************************************************************
	 * 
	 * @param col_name
	 * @return
	 */
	public boolean colExists (String col_name) {
		boolean col_found = false;
		
		for (Map.Entry<Integer, Column> entry : column_schema.entrySet()) {
			if (entry.getValue().getName().equals(col_name)) {
				col_found = true;
				break;
			}
		}
		
		return col_found;
	}
}
