package com.commoninf.json;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.commoninf.logger.CiiLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonIn {
	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.json.JsonIn");
	private ArrayList<UserFunctionJson> user_functions;
	private ArrayList<ProjectJson> project_core;
	
	/**************************************************************************
	 * 
	 */
	public JsonIn () {
		user_functions = null;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	private Gson getGsonInstance () {
		GsonBuilder gson_b = null;
		Gson gson = null;
		
		gson_b = new GsonBuilder();
		
		gson = gson_b.create();
		
		return gson;
	}
	
	/**************************************************************************
	 * 
	 * @param json
	 * @return
	 */
	public ArrayList<UserFunctionJson> userFunctionsFromJson (String json) {
		Gson json_deconstructor;
		Type arrayType = new TypeToken<ArrayList<UserFunctionJson>>() {}.getType();
		
		json_deconstructor = getGsonInstance ();
		user_functions = json_deconstructor.fromJson(json, arrayType);
		
		return user_functions;
	}
	
	/**************************************************************************
	 * 
	 * @param json
	 * @return
	 */
	public ArrayList<ProjectJson> projectCoreFromJson (String json) {
		Gson json_deconstructor;
		Type arrayType = new TypeToken<ArrayList<ProjectJson>>() {}.getType();
		
		json_deconstructor = getGsonInstance ();
		project_core = json_deconstructor.fromJson(json, arrayType);
		
		return project_core;
	}
}
