package com.commoninf.json;

import java.util.ArrayList;

import com.commoninf.database.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonOut {
	Object object_for_json = null;
	Object[] array_for_json = null;
	ArrayList<Row> rows_for_json = null;
	PageWrapper<Row> paged_rows_for_json = null;
	boolean prettyPrintJson;
	boolean showCreatedUpdatedValidColumns;
	
	/**************************************************************************
	 * 
	 * @param array_for_json
	 */
	public JsonOut () {
		// Make an empty array.  This constructor is only called when there
		// is nothing to return but an empty array.
		this.object_for_json = null;
		this.array_for_json = new ArrayList<Object>().toArray();
		this.prettyPrintJson = false;
		this.showCreatedUpdatedValidColumns = false;
	}

	
	/**************************************************************************
	 * 
	 * @param object_for_json
	 */
	public JsonOut (Object object_for_json) {
		this.object_for_json = object_for_json;
		this.array_for_json = null;
		this.rows_for_json = null;
		this.prettyPrintJson = false;
		this.showCreatedUpdatedValidColumns = false;
	}
	
	/**************************************************************************
	 * 
	 * @param array_for_json
	 */
	public JsonOut (Object[] array_for_json) {
		this.object_for_json = null;
		this.array_for_json = array_for_json;
		this.rows_for_json = null;
		this.prettyPrintJson = false;
		this.showCreatedUpdatedValidColumns = false;
	}
	
	/**************************************************************************
	 * 
	 * @param rows_for_json
	 */
	public JsonOut (ArrayList<Row> rows_for_json) {
		this.object_for_json = null;
		this.rows_for_json = rows_for_json;
		this.array_for_json = null;
		this.prettyPrintJson = false;
		this.showCreatedUpdatedValidColumns = false;
	}
	
    /**************************************************************************
     * @param rows_for_json
     * @param page
     * @param count
     */
    public JsonOut (ArrayList<Row> rows_for_json, int page, int count) {
    	this.object_for_json = null;
    	paged_rows_for_json = new PageWrapper<Row>() ;
    	ArrayList<Row> rows = new ArrayList<Row>() ;
    	
    	paged_rows_for_json.setTotalElements(rows_for_json.size());    	
    	paged_rows_for_json.setPageNumber(page);
    	paged_rows_for_json.setPageSize(count);
    	if (count > 0) {
    		paged_rows_for_json.setTotalPages((int) Math.ceil((double)rows_for_json.size() / count));
    	}
        
        if (page >= 0 && count > 0) {
            int startIndex = page * count ;
            int endIndex = (startIndex + count) ;
            
            if  (startIndex < rows_for_json.size()) {               
                if (endIndex >= rows_for_json.size()) {
                    endIndex = startIndex + (rows_for_json.size() - startIndex) ;
                }
                               
                for (int i=startIndex; i<endIndex; i++) {
                	rows.add(rows_for_json.get(i));
                }
            }
        }
        
        paged_rows_for_json.setValues(rows);
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
		
		if (object_for_json != null) {
			json_builder = getGsonInstance (true);
			json_str = json_builder.toJson(object_for_json);						
		}		
		else if (rows_for_json != null) {
			json_builder = getGsonInstance (true);
			json_str = json_builder.toJson(rows_for_json);			
		}
		else if (array_for_json != null) {
			json_builder = getGsonInstance (false);
			json_str = json_builder.toJson(array_for_json);
		} 
		else if (paged_rows_for_json != null) {
			json_builder = getGsonInstance (false);
			json_str = json_builder.toJson(paged_rows_for_json);
		}
		
		return json_str;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public int getNumRows () {
		int ret_val = 0;
		
		if (rows_for_json != null) {
			ret_val = rows_for_json.size();
		}
		else if (array_for_json != null) {
			ret_val = array_for_json.length;
		}
		
		return ret_val;
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
	
	public class PageWrapper<ElementType> {
	    int pageNumber ;
	    int pageSize ;
	    int totalPages ;
	    long totalElements ;    
	    ArrayList<ElementType> values ;
			
	    public PageWrapper() {        
	    }
	    
	    public int getPageNumber() {
	        return pageNumber;
	    }
	    
	    public void setPageNumber(int pageNumber) {
	        this.pageNumber = pageNumber;
	    }
	    
	    public int getPageSize() {
	        return pageSize;
	    }
	    
	    public void setPageSize(int pageSize) {
	        this.pageSize = pageSize;
	    }
	    
	    public int getTotalPages() {
	        return totalPages;
	    }
	    
	    public void setTotalPages(int totalPages) {
	        this.totalPages = totalPages;
	    }
	    
	    public long getTotalElements() {
	        return totalElements;
	    }
	    
	    public void setTotalElements(long totalElements) {
	        this.totalElements = totalElements;
	    }
	    
	    public ArrayList<ElementType> getValues() {
	        return values;
	    }
	    
	    public void setValues(ArrayList<ElementType> values) {
	        this.values = values;
	    }		
	}
}
