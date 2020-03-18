package com.commoninf.json;

import com.commoninf.logger.CiiLogger;

/******************************************************************************
 * This class is used to de-serialize the JSON that is used to add/import/edit
 * user functions to the database.
 */
public class UserFunctionJson {

	@SuppressWarnings("unused")
	private static final CiiLogger logger = new CiiLogger("com.commoninf.json.UserFunctionJson");
	private boolean edit;
	private long id;
	private String user_function;
	private String membership_type; 
	private String proj_group;
	private String username;
	
	/**************************************************************************
	 * 
	 */
	public UserFunctionJson () {
		edit = false;
		id = 0 ;
		user_function = "";
		proj_group = "";
		username = "";
		membership_type = "";
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public boolean isEdit() {
		return edit;
	}

	/**************************************************************************
	 * 
	 * @param edit
	 */
	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getUser_function() {
		return user_function.trim();
	}
	
    /**************************************************************************
     * 
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**************************************************************************
     * 
     * @return
     */
    public long getId() {
        return this.id;
    }
    
    /**************************************************************************
     * 
     * @param user_function
     */
    public void setUser_function(String user_function) {
        this.user_function = user_function;
    }    
    
    /**************************************************************************
     * 
     * @return
     */
    public String getMembership_type() {
        return membership_type.trim();
    }
    
    /**************************************************************************
     * 
     * @param membership_type
     */
    public void setMembership_type(String membership_type) {
        this.membership_type = membership_type;
    }

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getProj_group() {
		return proj_group.trim();
	}

	/**************************************************************************
	 * 
	 * @param proj_group
	 */
	public void setProj_group(String proj_group) {
		this.proj_group = proj_group;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getUsername() {
		return username.trim();
	}

	/**************************************************************************
	 * 
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}
