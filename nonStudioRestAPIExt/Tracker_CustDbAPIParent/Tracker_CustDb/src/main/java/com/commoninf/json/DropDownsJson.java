package com.commoninf.json;

import com.commoninf.database.Row;
import com.commoninf.utils.DropDownValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DropDownsJson {
	
	String dropDownKey = null;
	DropDownValue dropDownValue = null;
	boolean prettyPrintJson;
	boolean showCreatedUpdatedValidColumns;
	
	/**************************************************************************
	 * 
	 * @param array_for_json
	 */
	public DropDownsJson () {
		// Make an empty array.  This constructor is only called when there
		// is nothing to return but an empty array.
		this.prettyPrintJson = false;
		this.showCreatedUpdatedValidColumns = false;
	}
	
	/**************************************************************************
	 * 
	 * @param array_for_json
	 */
	public DropDownsJson (Object[] array_for_json) {
		this.dropDownValue = null;
		this.prettyPrintJson = false;
		this.showCreatedUpdatedValidColumns = false;
	}
	
	/**************************************************************************
	 * 
	 * @param array_for_json
	 */
	public DropDownsJson (String key, DropDownValue dropDownValue) {
		this.dropDownKey = key;
		this.dropDownValue = dropDownValue;
		this.prettyPrintJson = false;
		this.showCreatedUpdatedValidColumns = false;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	@Override
	public String toString () {
		String json_str = "[]";
		Gson json_builder;
		
		if (dropDownValue != null) {
			json_builder = getGsonInstance (true);
			json_str = json_builder.toJson(dropDownValue);
		}
		
		return "{\""+dropDownKey +"\":"+json_str+"}";
	}
	
	
	/**************************************************************************
	 * 
	 * @param use_complex_map_serialization
	 * @return
	 */
	private Gson getGsonInstance (boolean use_complex_map_serialization) {
		GsonBuilder gson_b = null;
		Gson gson = null;
		
		if (use_complex_map_serialization) {
			gson_b = new GsonBuilder().registerTypeHierarchyAdapter(Row.class, new RowGsonAdapter(showCreatedUpdatedValidColumns));
			gson_b.enableComplexMapKeySerialization();
		}
		else {
			gson_b = new GsonBuilder();
		}
		
		if (gson_b != null) {
			if (prettyPrintJson) {
				gson = gson_b.setPrettyPrinting().create();
			}
			else {
				gson = gson_b.create();
			}
		}
		
		return gson;
	}
	
	/**************************************************************************
	 * 
	 * @param prettyPrintJson
	 */
	public void setPrettyPrintJson (boolean prettyPrintJson) {
		this.prettyPrintJson = prettyPrintJson;
	}
	
	/**************************************************************************
	 * 
	 * @param showCreatedUpdatedValidColumns
	 */
	public void setShowCreatedUpdatedValidColumns (boolean showCreatedUpdatedValidColumns) {
		this.showCreatedUpdatedValidColumns = showCreatedUpdatedValidColumns;
	}
}
