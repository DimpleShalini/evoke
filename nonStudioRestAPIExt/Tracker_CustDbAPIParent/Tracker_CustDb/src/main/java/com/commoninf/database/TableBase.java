package com.commoninf.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import com.commoninf.logger.CiiLogger;

public abstract class TableBase {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.database.TableBase");
	protected Database db;
	protected Column[] col_def = null;
	protected Column[] child_tables = null;
	protected TableSchema schema = null;
	protected ArrayList<Row> table_rows = null;
	protected String table_name = null;
	protected boolean table_has_created_updated_isvalid_columns = false;
	protected boolean table_has_id_column = false;
	private sort_order currSortOrder;
	private boolean sortIsForAllQuerys;
	private String currSortColumn;
	public static enum sort_order {ASCENDING, DESCENDING};

	/**************************************************************************
	 * 
	 * @param db
	 * @param table_name
	 * @param col_def
	 * @param child_tables
	 * @param table_has_id_column
	 * @param table_has_created_updated_isvalid_columns
	 */
	public TableBase (Database db, String table_name, Column[] col_def, Column[] child_tables, boolean table_has_id_column, boolean table_has_created_updated_isvalid_columns) {
		init (db, table_name, col_def, child_tables, table_has_id_column, table_has_created_updated_isvalid_columns);
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 * @param table_name
	 * @param table_has_created_updated_isvalid_columns
	 */
	public TableBase (Database db, String table_name, Column[] col_def, boolean table_has_id_column, boolean table_has_created_updated_isvalid_columns) {
		init (db, table_name, col_def, null, table_has_id_column, table_has_created_updated_isvalid_columns);
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 * @param table_name
	 */
	public TableBase (Database db, String table_name, Column[] col_def) {
		init (db, table_name, col_def, null, false, false);
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 * @param table_name
	 * @param table_has_created_updated_isvalid_columns
	 */
	private void init (Database db, String table_name, Column[] col_def, Column[] child_tables, boolean table_has_id_column, boolean table_has_created_updated_isvalid_columns) {
		this.db = db;
		this.sortIsForAllQuerys = false;
		this.currSortOrder = null;
		this.currSortColumn = null;
		schema = new TableSchema ();
		table_rows = new ArrayList<Row> ();
		this.table_name = table_name;
		this.table_has_created_updated_isvalid_columns = table_has_created_updated_isvalid_columns;
		this.table_has_id_column = table_has_id_column;
		this.col_def = col_def;
		this.child_tables = child_tables;

		defineTableSchema ();
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public Database getDb () {
		return db;
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 */
	public void setDb (Database db) {
		// Update any foreign key column's tables with the new db connection
		if (schema != null) {
			schema.getColumn_schema().forEach ((k,v)->{
				if (v.isForeign_key()) {
					v.getTable().setDb(db);
				}
			});
		}
		this.db = db;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public String getTable_name () {
		return table_name;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public ArrayList<Row> getTable_rows () {
		return table_rows;
	}
	
	/**************************************************************************
	 * 
	 * @param column_name
	 * @param so
	 */
	public void setSortOrderForNextQuery (String column_name, sort_order so) {
		sortIsForAllQuerys = false;
		currSortOrder = so;
		currSortColumn = column_name;
	}
	
	/**************************************************************************
	 * 
	 * @param column_name
	 * @param so
	 */
	public void setSortOrderForAllQuerys (String column_name, sort_order so) {
		sortIsForAllQuerys = true;
		currSortOrder = so;
		currSortColumn = column_name;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public String getQueryStringForAllRows () {
		return getQueryStringForAllRows (null);
	}
	
	/**************************************************************************
	 * 
	 * @param null_col
	 * @return
	 */
	public String getQueryStringForAllRows (String null_col) {
		int entry_cnt = 0;
		ArrayList<String> join_strs = new ArrayList<String>();
		String query_str = "SELECT ";
		
		for (Map.Entry<Integer, Column> entry : schema.getColumn_schema().entrySet()) {
			if (entry_cnt != 0) {
				query_str += ", ";
			}
			
			if ((null_col != null)&&(null_col.equals(entry.getValue().getName()))) {
				query_str += (table_name+"."+entry.getValue().getName());
			}
			else {
				if (entry.getValue().isForeign_key()) {
					query_str += (entry.getValue().getTable().getTable_name()+"_alias."+entry.getValue().getRef_column()+" AS "+entry.getValue().getName());
					query_str += ", ";
					query_str += (table_name+"."+entry.getValue().getName()+" AS "+entry.getValue().getName()+Column.FK_ADDITIONAL_COL_NAME_SUFFIX);
					join_strs.add("LEFT JOIN ("+entry.getValue().getTable().getQueryStringForAllRows(null_col)+") "+entry.getValue().getTable().getTable_name()+"_alias"+" ON "+table_name+"."+entry.getValue().getName()+"="+entry.getValue().getTable().getTable_name()+"_alias."+entry.getValue().getFk_key_column());
				}
				else {
					query_str += (table_name+"."+entry.getValue().getName());
				}
			}
			entry_cnt++;
		}
		
		query_str += "\n";
		
		query_str += "FROM "+table_name;
		
		for (String join_str : join_strs) {
			query_str += ("\n"+join_str);
		}
		
		if ((currSortOrder != null)&&(currSortColumn != null)) {
			query_str += "\n";
			query_str += ("ORDER BY "+currSortColumn+(currSortOrder==sort_order.ASCENDING?" ASC":" DESC"));
			
			if (!sortIsForAllQuerys) {
				currSortOrder = null;
				currSortColumn = null;
			}
		}

		// logger.info(query_str);
		
		return query_str;
	}
	
	/**************************************************************************
	 * 
	 * @param id
	 * @return
	 */
	public String getQueryStringForNullRows (String col_name) {
		String query_str = "SELECT * FROM ("+getQueryStringForAllRows(col_name)+") "+table_name+"_trans_alias \nWHERE";

		query_str += (" "+table_name+"_trans_alias."+col_name+" is null");
		
		//logger.info(query_str);
		
		return query_str;
	}
	
	
	
	/**************************************************************************
	 * 
	 * @param id
	 * @return
	 */
	public PreparedStatement getQueryForSpecificRows (String col_name, Object[] col_val) throws SQLException {		 
		String query_str = "SELECT * FROM ("+getQueryStringForAllRows()+") "+table_name+"_trans_alias \nWHERE";		
		for (int i=0; i<col_val.length; i++) {
			if (i!=0) {
				query_str += " OR";
			}
			query_str += (" "+table_name+"_trans_alias."+col_name+"=?");
		}
		
		// logger.info(query_str);
		
		PreparedStatement statement = db.getDb_conn().prepareStatement(query_str);		
		for (int i=0; i<col_val.length; i++) {
			if (col_val[i] instanceof Long)
				statement.setLong(i+1, (Long) col_val[i]);			
			else if (col_val[i] instanceof Integer)
				statement.setInt(i+1, (Integer) col_val[i]);
			else
				statement.setString(i+1, col_val[i].toString());
		}
		
		return statement;
	}
	
	
    /**************************************************************************
     * 
     * @param id
     * @return
     */
    public PreparedStatement getDeleteForSpecificRows (String col_name, Object[] col_val) throws SQLException {
        String query_str = "DELETE FROM "+table_name+"\nWHERE";
        
        for (int i=0; i<col_val.length; i++) {
            if (i!=0) {
                query_str += " OR";
            }
            query_str += (" "+table_name+"."+col_name+"=?");
        }
        
        // logger.info(query_str);
        
		PreparedStatement statement = db.getDb_conn().prepareStatement(query_str);		
		for (int i=0; i<col_val.length; i++) {
			if (col_val[i] instanceof Long)
				statement.setLong(i+1, (Long) col_val[i]);
			else if (col_val[i] instanceof Integer)
				statement.setInt(i+1, (Integer) col_val[i]);
			else
				statement.setString(i+1, col_val[i].toString());
		}
		
		return statement;
    }
    
	
	/**************************************************************************
	 * 
	 * @throws SQLException
	 */
	public void getAllTableRows () throws SQLException {
		String query_str = getQueryStringForAllRows();
		
		db.runQuery (query_str, (ResultSet rs)->{
			try {
				readRow(rs);
			} catch (SQLException e) {
				logger.error("SQL Exception in TableBase.getAllTableRows.");
				logger.error("Exception message: "+e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	private String getUpdateSQLForPreparedStatement () {
		String ret_val = "UPDATE "+table_name+" SET\n";
		String where_clause = "WHERE ";
		
		int col_cnt = 0;
		for (Map.Entry<Integer, Column> entry : schema.getColumn_schema().entrySet()) {
			String col_name = entry.getValue().getName();
			if ((!col_name.equals("display_order"))&&
				(!Row.isAStdCol(col_name))) {
				if (col_cnt != 0) {
					ret_val += ",\n";
					where_clause += " AND\n";
				}
				// This is where all of the table specific columns will land
				if (entry.getValue().isForeign_key()) {
					ret_val += (col_name+"= (SELECT "+entry.getValue().getFk_key_column()+" FROM "+entry.getValue().getTable().getTable_name()+" "+entry.getValue().getTable().getTable_name()+"_alias WHERE "+entry.getValue().getTable().getTable_name()+"_alias."+entry.getValue().getRef_column()+"=?)");
					where_clause += (col_name+"= (SELECT "+entry.getValue().getFk_key_column()+" FROM "+entry.getValue().getTable().getTable_name()+" "+entry.getValue().getTable().getTable_name()+"_alias WHERE "+entry.getValue().getTable().getTable_name()+"_alias."+entry.getValue().getRef_column()+"=?)");
				}
				else {
					ret_val += (col_name+"= ?");
					where_clause += (col_name+"= ?");
				}
				col_cnt++;
			}
			else {
				if (col_name.equals("update_user")) {
					if (col_cnt != 0) {
						ret_val += ",\n";
					}
					ret_val += (col_name+"= 1");
					col_cnt++;
				}
				else if (col_name.equals("update_timestamp")){
					if (col_cnt != 0) {
						ret_val += ",\n";
					}
					ret_val += (col_name+"=now()");
					col_cnt++;
				}
				// All others shouldn't get added to the update
			}
		}

		ret_val += ("\n"+where_clause+";");
		
		//logger.info("Update query string = "+ret_val);
		
		return ret_val;
	}
	
    /**************************************************************************
     * Create an prepared update statement for an update by primary key/id. 
     * 
     * @return
     */
    private String getUpdateByIdSQLForPreparedStatement () {
        String ret_val = "UPDATE "+table_name+" SET\n";
        String where_clause = "WHERE (id=?)";
        
        int col_cnt = 0;
        for (Map.Entry<Integer, Column> entry : schema.getColumn_schema().entrySet()) {
            String col_name = entry.getValue().getName();
            if ((!col_name.equals("display_order"))&&
                (!Row.isAStdCol(col_name))) {
                if (col_cnt != 0) {
                    ret_val += ",\n";
                }
                // This is where all of the table specific columns will land
                if (entry.getValue().isForeign_key()) {
                    ret_val += (col_name+"= (SELECT "+entry.getValue().getFk_key_column()+" FROM "+entry.getValue().getTable().getTable_name()+" "+entry.getValue().getTable().getTable_name()+"_alias WHERE "+entry.getValue().getTable().getTable_name()+"_alias."+entry.getValue().getRef_column()+"=?)");
                }
                else {
                    ret_val += (col_name+"= ?");                    
                }                
                col_cnt++;
            }
            else {
                if (col_name.equals("update_user")) {
                    if (col_cnt != 0) {
                        ret_val += ",\n";
                    }
                    ret_val += (col_name+"= 1");
                    col_cnt++;
                }
                else if (col_name.equals("update_timestamp")){
                    if (col_cnt != 0) {
                        ret_val += ",\n";
                    }
                    ret_val += (col_name+"=now()");
                    col_cnt++;
                }
                // All others shouldn't get added to the update
            }
        }

        ret_val += ("\n"+where_clause+";");
        
        //logger.info("Update query string = "+ret_val);
                
        return ret_val;
    }	
	
	/**************************************************************************
	 * 
	 * @throws SQLException
	 */
	public void updateTableRows (TableBase orig_table) throws SQLException {
		String query_str = getUpdateSQLForPreparedStatement ();
		
		db.runPreparedStatement(query_str, (PreparedStatement stmt)->{
			try {
				setINParamsForUpdate (stmt, orig_table);
			} catch (SQLException e) {
				logger.error("SQL Exception in TableBase.updateTableRows.");
				logger.error("Exception message: "+e.getMessage());
				e.printStackTrace();
			}
		});
	}
	
    /**************************************************************************
     * Update rows give the current rows by id
     * 
     * @throws SQLException
     */	
    public void updateTableRowsById() throws SQLException {
        String query_str = getUpdateByIdSQLForPreparedStatement ();
        db.runPreparedStatement(query_str, (PreparedStatement stmt)->{
            try {
                setINParamsByIdForUpdate (stmt);
            } catch (SQLException e) {
                logger.error("SQL Exception in TableBase.updateTableRows.");
                logger.error("Exception message: "+e.getMessage());
                e.printStackTrace();
            }
        });
    }
	
	
	/**************************************************************************
	 * 
	 * @param ignore_unique_constraint_violations
	 * @return
	 */
	private String getInsertSQLForPreparedStatement (boolean ignore_unique_constraint_violations) {
		int entry_cnt = 0;
		ArrayList<String> value_strs = new ArrayList<String>();
		String query_str = "INSERT INTO "+table_name+"(";
		
		for (Map.Entry<Integer, Column> entry : schema.getColumn_schema().entrySet()) {
			String col_name = entry.getValue().getName();
			String val_str = "";
			if (entry_cnt != 0) {
				query_str += ", ";
				val_str += ", ";
			}
			
			val_str += "\n";
			
			query_str += col_name;
			
			if (col_name.equals("id")) {
				val_str += ("nextval(\'"+table_name+"_id_seq\'::regclass)");
			}
			else if (Row.isAStdCol(col_name)) {
				if (col_name.contains("_user")) {
					val_str += "1";
				}
				else if (col_name.contains("_timestamp")) {
					val_str += "now()";
				}
				else {
					// Must be the isvalid_yn column
					val_str += "\'Y\'";
				}
			}
			else if (col_name.equals("display_order")) {
				val_str += ("nextval(\'"+table_name+"_display_order_seq\'::regclass)");
			}
			else {
				// This is where all of the table specific columns will land
				if (entry.getValue().isForeign_key()) {
					val_str += ("(SELECT "+entry.getValue().getFk_key_column()+" FROM "+entry.getValue().getTable().getTable_name()+" "+entry.getValue().getTable().getTable_name()+"_alias WHERE "+entry.getValue().getTable().getTable_name()+"_alias."+entry.getValue().getRef_column()+"=?)");
				}
				else {
					val_str += "?";
				}
			}
			value_strs.add(val_str);
			entry_cnt++;
		}
		
		query_str += ")\n";
		
		query_str += "VALUES (";
		
		for (String value_str : value_strs) {
			query_str += value_str;
		}
		
		query_str += "\n)";
		
		if (ignore_unique_constraint_violations) {
			query_str += "ON CONFLICT ON CONSTRAINT "+table_name+"_unique_id\n";
			query_str += "DO NOTHING";
			// Decrement the unused sequence id
			//query_str += "DO SELECT setval(\'"+table_name+"_id_seq\', currval(\'"+table_name+"_id_seq\'))";
		}
		
		query_str += ";";
		
		//logger.info(query_str);
		
		return query_str;
	}
	
	/**************************************************************************
	 * 
	 * @throws SQLException
	 */
	public void insertTableRows (boolean ignore_unique_constraint_violations) throws SQLException {
		String query_str = getInsertSQLForPreparedStatement(ignore_unique_constraint_violations);
		
		db.runPreparedStatement(query_str, (PreparedStatement stmt)->{
			try {
				setINParams (stmt);
			} catch (SQLException e) {
				logger.error("SQL Exception in TableBase.insertTableRows.");
				logger.error("Exception message: "+e.getMessage());
				e.printStackTrace();
			}
		});
		
		if (child_tables != null) {
			for (Column col : child_tables) {
				if (col.getTable() != null) {
					col.getTable().insertTableRows(ignore_unique_constraint_violations);
				}
			}
		}
	}
	
	/**************************************************************************
	 * 
	 * @param msg
	 */
	public void logSqlException (String e_msg) {
		logger.error("SQL Exception accessing the "+table_name+" table");
		logger.error ("SQL Exception message: "+e_msg);
	}
	
	/**************************************************************************
	 * 
	 */
	public void defineTableSchema () {
		if (table_has_id_column) {
			col_def = Row.addIdColumn(col_def);
		}
		if (table_has_created_updated_isvalid_columns) {
			col_def = Row.addCreatedUpdatedValidColumns(col_def);

		}
		for (Column col : col_def) {
			if (col.isForeign_key()) {
				TableBase tb = col.getTable();
				if ((tb != null)&&(tb.getDb() == null)) {
					tb.setDb(db);
				}
			}
			schema.addColumn(col);
		}
		
		// Add the child tables to the schema
		addChildTablesToSchema ();
	}
	
	/**************************************************************************
	 * 
	 */
	public void addChildTablesToSchema () {
		if (child_tables != null) {
			for (Column ct : child_tables) {
				TableBase tb = ct.getTable();
				if ((tb != null)&&(tb.getDb() == null)) {
					tb.setDb(db);
				}
				schema.addChild_table(ct);
			}
		}
	}
	
	/**************************************************************************
	 * 
	 * @param child_tables
	 */
	public void addChildTablesToSchema (Column[] child_tables) {
		// Add the child tables to the schema
		this.child_tables = child_tables;
		
		addChildTablesToSchema ();
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public TableSchema getSchema () {
		return schema;
	}
	
	/**************************************************************************
	 * 
	 * @param row
	 */
	public void addTableRow (Row row) {
		table_rows.add(row);
	}
	
	/**************************************************************************
	 * 
	 */
	public void clearTableRows () {
		table_rows = new ArrayList<Row> ();
	}
	
	/**************************************************************************
	 * 
	 */
	public void clearChildTables () {
		if (child_tables != null) {
			for (Column ct : child_tables) {
				TableBase tb = ct.getTable();
				if (tb != null) {
					tb.clearTableRows();
				}
			}
		}
	}
	
	/**************************************************************************
	 * Fetch row by ID
	 * @throws SQLException
	 */
	public void getById(Integer id) throws SQLException {
		Integer [] ids = {id};
		
		
		PreparedStatement ps = null ;
		try {
			ps = getQueryForSpecificRows("id", ids) ;
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				readRow(rs);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("SQL Exception thrown in getById");
			logSqlException (e.getMessage());
		}
		finally {
			if (ps != null) {
				ps.close();
			}
		}
	}	
	
	
	/**************************************************************************
	 * 
	 * @param stmt
	 * @throws SQLException
	 */
	protected void setINParamsForUpdate (PreparedStatement stmt, TableBase orig_table) throws SQLException {
		//logger.info ("Calling setINParamsForUpdate for "+table_name+", table_rows length="+table_rows.size());
		int row_index = 0;
		for (Row table_row : table_rows) {
			int col_cnt = 1;
			for (Map.Entry<Integer, Column> entry : schema.getColumn_schema().entrySet()) {
				String col_name = entry.getValue().getName();
				if ((!col_name.equals("display_order"))&&
					(!Row.isAStdCol(col_name))) {
					stmt.setString(col_cnt, (String)table_row.getColVal(col_name));
					//logger.info("Setting "+col_name+" to "+(String)table_row.getColVal(col_name));
					col_cnt++;
				}
				/*else {
					logger.info("Skipping row named: "+entry.getValue().getName());
				}*/
			}
			
			// Fill in the values for the WHERE clause
			for (Map.Entry<Integer, Column> entry : orig_table.getSchema().getColumn_schema().entrySet()) {
				String col_name = entry.getValue().getName();
				if ((!col_name.equals("display_order"))&&
					(!Row.isAStdCol(col_name))) {
					stmt.setString(col_cnt, (String)orig_table.getTable_rows().get(row_index).getColVal(col_name));
					//logger.info("Setting "+col_name+" to "+(String)orig_table.getTable_rows().get(row_index).getColVal(col_name));
					col_cnt++;
				}
				/*else {
					logger.info("Skipping row named: "+entry.getValue().getName());
				}*/
			}
			
			stmt.addBatch();
			row_index++;
		}
	}
	
    /**************************************************************************
     * Given a prepared statement, fill in all of the data and where clause
     * identifiers given current row data.
     * 
     * @param stmt
     * @throws SQLException
     */
    protected void setINParamsByIdForUpdate (PreparedStatement stmt) throws SQLException {
        //logger.info ("Calling setINParamsForUpdate for "+table_name+", table_rows length="+table_rows.size());
        for (Row table_row : table_rows) {
            int col_cnt = 1;
            for (Map.Entry<Integer, Column> entry : schema.getColumn_schema().entrySet()) {
                String col_name = entry.getValue().getName();
                if ((!col_name.equals("display_order"))&&
                    (!Row.isAStdCol(col_name))) {
                    stmt.setString(col_cnt, (String)table_row.getColVal(col_name));
                    // logger.info("Setting "+col_name+" to "+(String)table_row.getColVal(col_name));
                    col_cnt++;
                }
                /*else {
                    logger.info("Skipping row named: "+entry.getValue().getName());
                }*/
            }
                        
            // Fill in the values for the WHERE clause
            for (Map.Entry<Integer, Column> entry : schema.getColumn_schema().entrySet()) {
                String col_name = entry.getValue().getName();
                if (col_name.compareToIgnoreCase("id") == 0) {
                	Object id = table_row.getColVal(col_name) ;
                	if (id instanceof Long)
                		stmt.setLong(col_cnt, (Long) id);
                	else
                		stmt.setLong(col_cnt, (Integer) id);
                    col_cnt++;
                    break ;
                }
            }
            
            stmt.addBatch();
        }
    }	
	
	/**************************************************************************
	 * 
	 * @param stmt
	 * @throws SQLException
	 */
	protected void setINParams (PreparedStatement stmt) throws SQLException {
		//logger.info ("Calling setINParams for "+table_name+", table_rows length="+table_rows.size());
		for (Row table_row : table_rows) {
			int col_cnt = 1;
			for (Map.Entry<Integer, Column> entry : schema.getColumn_schema().entrySet()) {
				if ((!Row.isAStdCol(entry.getValue().getName()))&&(!entry.getValue().getName().equals("display_order"))) {
					stmt.setString(col_cnt, (String)table_row.getColVal(entry.getValue().getName()));
					//logger.info("Setting "+entry.getValue().getName()+" to "+(String)table_row.getColVal(entry.getValue().getName()));
					col_cnt++;
				}
				//else {
				//	logger.info("Skipping std row named: "+entry.getValue().getName());
				//}
			}
			stmt.addBatch();
		}
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 * @throws SQLException
	 */
	abstract public void readRow (ResultSet rs) throws SQLException;
}
