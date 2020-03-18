package com.commoninf.database;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.commoninf.logger.CiiLogger;

public class Column {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.database.Column");
	
	public static final String FK_ADDITIONAL_COL_NAME_SUFFIX = "_real_id_val";
	
	String name;
	boolean is_foreign_key;
	boolean is_child_table;
	TableBase table;
	Class<?> c_table_class;
	String fk_key_column;
	String ref_column; // If null then the foreign key request will return the whole table
	
	public Column (String name) {
		this.name = name;
		this.is_foreign_key = false;
		this.table = null;
		this.fk_key_column = null;
		this.is_child_table = false;
		this.c_table_class = null;
		this.ref_column = null;
	}
	
	/**************************************************************************
	 * 
	 * @param name
	 * @param obj
	 * @param foreign_key
	 */
	public Column (String name, TableBase table, String fk_column) {
		this.name = name;
		if ((table != null)&&(fk_column != null)) {
			this.is_foreign_key = true;
			this.table = table;
			this.fk_key_column = fk_column;
			this.is_child_table = false;
			this.c_table_class = null;
			this.ref_column = null;
		}
		else {
			this.is_foreign_key = false;
			this.table = null;
			this.fk_key_column = null;
			this.is_child_table = false;
			this.c_table_class = null;
			this.ref_column = null;
		}
	}
	
	/**************************************************************************
	 * 
	 * @param name
	 * @param obj
	 * @param foreign_key
	 */
	public Column (String name, TableBase table, String fk_column, String ref_column) {
		this.name = name;
		if ((table != null)&&(fk_column != null)) {
			this.is_foreign_key = true;
			this.table = table;
			this.fk_key_column = fk_column;
			this.is_child_table = false;
			this.c_table_class = null;
			this.ref_column = ref_column;
		}
		else {
			this.is_foreign_key = false;
			this.table = null;
			this.fk_key_column = null;
			this.is_child_table = false;
			this.c_table_class = null;
			this.ref_column = null;
		}
	}
	
	/**************************************************************************
	 * 
	 * @param table
	 */
	public Column (Class<?> table_class) {
		if (table_class != null) {
			this.name = "";
			this.is_foreign_key = false;
			this.c_table_class = table_class;
			this.fk_key_column = null;
			this.is_child_table = true;
			this.ref_column = null;
		}
		else {
			this.name = "error";
			this.is_foreign_key = false;
			this.table = null;
			this.fk_key_column = null;
			this.is_child_table = false;
			this.c_table_class = null;
			this.ref_column = null;
		}
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**************************************************************************
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public boolean isForeign_key() {
		return is_foreign_key;
	}

	/**************************************************************************
	 * 
	 * @param foreign_key
	 */
	public void setForeign_key(boolean is_foreign_key) {
		this.is_foreign_key = is_foreign_key;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public boolean isChild_table() {
		return is_child_table;
	}

	/**************************************************************************
	 * 
	 * @param child_table
	 */
	public void setChild_table(boolean is_child_table) {
		this.is_child_table = is_child_table;
	}
	
	/**************************************************************************
	 * 
	 */
	public void instantiateChildTable () {
		Constructor<?> child_table_const = null;
		try {
			child_table_const = c_table_class.getConstructor();
		} catch (NoSuchMethodException e) {
			// Quietly ignore
			logger.error("There isn't a zero argument constructor for the class "+c_table_class.getName());
			logger.error(e.getMessage());
		} catch (SecurityException e) {
			// Quietly ignore
			logger.error("There was a security exception when getting the constructor for the class "+c_table_class.getName());
			logger.error(e.getMessage());
		}
		if (child_table_const != null) {
			try {
				table = (TableBase)child_table_const.newInstance();
			} catch (InstantiationException e) {
				// Quietly ignore
				logger.error("InstantiationException for the class "+c_table_class.getName());
				logger.error(e.getMessage());
			} catch (IllegalAccessException e) {
				// Quietly ignore
				logger.error("IllegalAccessException for the class "+c_table_class.getName());
				logger.error(e.getMessage());
			} catch (IllegalArgumentException e) {
				// Quietly ignore
				logger.error("IllegalArgumentException for the class "+c_table_class.getName());
				logger.error(e.getMessage());
			} catch (InvocationTargetException e) {
				// Quietly ignore
				logger.error("InvocationTargetException for the class "+c_table_class.getName());
				logger.error(e.getMessage());
			}
			if (table != null) {
				name = table.getTable_name();
				//logger.info("Created child table named "+name+" for "+c_table_class.getName());
			}
		}
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public TableBase getTable() {
		return table;
	}

	/**************************************************************************
	 * 
	 * @param fk_table
	 */
	public void setTable(TableBase table) {
		this.table = table;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getFk_key_column() {
		return fk_key_column;
	}

	/**************************************************************************
	 * 
	 * @param fk_key_column
	 */
	public void setFk_key_column(String fk_key_column) {
		this.fk_key_column = fk_key_column;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getRef_column() {
		return ref_column;
	}

	/**************************************************************************
	 * 
	 * @param ref_column
	 */
	public void setRef_column(String ref_column) {
		this.ref_column = ref_column;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public Class<?> getC_table_class() {
		return c_table_class;
	}

	/**************************************************************************
	 * 
	 * @param c_table_class
	 */
	public void setC_table_class(Class<?> c_table_class) {
		this.c_table_class = c_table_class;
	}
}
