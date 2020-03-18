package com.commoninf.json;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;

import com.commoninf.database.Row;
import com.commoninf.logger.CiiLogger;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RowGsonAdapter implements JsonSerializer<Row>{
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.json.RowGsonAdapter");
	
	boolean showCreatedUpdatedValidColumns;
	
	/**************************************************************************
	 * 
	 */
	public RowGsonAdapter () {
		super ();
		
		this.showCreatedUpdatedValidColumns = false;
	}
	
	/**************************************************************************
	 * 
	 * @param showCreatedUpdatedValidColumns
	 */
	public RowGsonAdapter (boolean showCreatedUpdatedValidColumns) {
		super ();
		
		this.showCreatedUpdatedValidColumns = showCreatedUpdatedValidColumns;
	}

	@Override
	/**************************************************************************
	 * 
	 * @param src
	 * @param typeOfSrc
	 * @param context
	 */
	public JsonElement serialize(Row src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject main_node = new JsonObject();
		
		Map<String, Object> cols = src.getCol_vals();
		
		// Shows the specific row class
		//logger.info("typeOfSrc="+typeOfSrc.toString());
		
		for (String key : cols.keySet()) {
			if ((!showCreatedUpdatedValidColumns)&&
				(Arrays.asList(Row.getCreatedUpdatedValidColumnNames()).contains(key))) {
				continue;
			}
			Object val = cols.get(key);
			if (val instanceof Integer) {
				main_node.addProperty(key, ((Integer)val).toString());
			}
			else if (val instanceof String) {
				main_node.addProperty(key, (String)val);
			}
			else if (val instanceof Timestamp) {
				main_node.addProperty(key, ((Timestamp)val).toString());
			}
			else {
				final JsonElement array = context.serialize(val);
				main_node.add(key, array);
			}
		}
		
		return main_node;
	}
}
