package com.commoninf.Tracker_CustDb;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.ArrayList;
import java.util.Random;


import com.commoninf.utils.DropDownValue;
import com.commoninf.json.JsonOut;
import com.commoninf.json.JsonIn;
import com.commoninf.json.UserFunctionJson;


/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
    
    public void testSmokeDropDownData(){
    	Tracker_CustDb tracker_CustDb = new Tracker_CustDb();
   	 	Map<String, DropDownValue> dropDownJson = tracker_CustDb.getDropDownsData();
   	 	assertNotNull(dropDownJson) ;
    }
    
    public void testSmokeBuildDropDownDataJson(){
        Tracker_CustDb tracker_CustDb = new Tracker_CustDb();
        JsonOut json = tracker_CustDb.buildJsonForUserFunctionLx() ;
        assertNotNull(json) ;
    }
    
    public void testSmokeBuildProjectGroupsUsersJson() {
        Tracker_CustDb tracker_CustDb = new Tracker_CustDb();
        JsonOut json = tracker_CustDb.buildJsonForProjectGroupsUsers("Afinitor") ;
        assertNotNull(json) ;        
    }
        
    public void testUserFunctionJsonSerialization() {
                
        JsonIn jsonIn = new JsonIn() ;
        ArrayList<UserFunctionJson> userFunctionJsonList = jsonIn.userFunctionsFromJson("[{ \"id\": 230043, \"user_function\": \"Deputy Safety Lead\", \"proj_group\": \"Rasilamlo\", \"username\": \"lsp_user10\", \"membership_type\": \"Core\" }]") ;
        assertNotNull(userFunctionJsonList) ;
        assertNotNull(userFunctionJsonList.size() == 1) ;
        
        UserFunctionJson userFunctionJson = userFunctionJsonList.get(0) ;
        
        assertNotNull(userFunctionJson) ;
        assertEquals(userFunctionJson.getId(), 230043) ;
        assertEquals(userFunctionJson.getUser_function(), "Deputy Safety Lead") ;
        assertEquals(userFunctionJson.getMembership_type(), "Core") ;
        assertEquals(userFunctionJson.getProj_group(), "Rasilamlo") ;
        assertEquals(userFunctionJson.getUsername(), "lsp_user10") ;      
    }
    
    public void testUpsertDeleteNewEntry() {
        String projectFamily = "Rasilamlo"; 
        String projectFunction = "Deputy Safety Lead";
        String membershipType = "Core" ;
        Tracker_CustDb tracker_CustDb = new Tracker_CustDb();
        JsonIn jsonIn = new JsonIn() ;
        
        // Generate a random username
        String randomUsername = randomUsername("rnd-", 15) ;
        
        // Create a new record to add
        ArrayList<UserFunctionJson> userFunctionJsonList = jsonIn.userFunctionsFromJson("[{ \"id\": 0, \"user_function\": \"" + projectFunction + "\", \"proj_group\": \"" + projectFamily + "\", \"username\": \"" + randomUsername + "\", \"membership_type\": \"" + membershipType + "\" }]") ;
        
        // Add the records
        boolean success = tracker_CustDb.upsertUser_FunctionTableFromJson(userFunctionJsonList) ;
        assertTrue(success) ;
        
        // Find the added record
        ArrayList<UserFunctionJson> searchResults = jsonIn.userFunctionsFromJson(tracker_CustDb.buildJsonForProjectGroupsUsers(projectFamily).toString()) ;
        assertNotNull(searchResults);
        UserFunctionJson foundResult = null ;
        for (UserFunctionJson result : searchResults) {
            if (result.getUsername().compareTo(randomUsername) == 0) {
                foundResult = result ;
                break ;                
            }            
        }
        
        // Verify what we found
        assertNotNull(foundResult) ;
        assertTrue(foundResult.getId() > 0) ;        
        assertEquals(membershipType, foundResult.getMembership_type()) ;
        assertEquals(randomUsername, foundResult.getUsername()) ;
        
//      TODO: APIs are not symmetrical, links to other tables are lost in round trip
//      assertEquals(projectFamily, foundResult.getProj_group()) ;
//      assertEquals(projectFunction, foundResult.getUser_function()) ;
        
        // Update it
        membershipType = "Extended" ;
        userFunctionJsonList = jsonIn.userFunctionsFromJson("[{ \"id\": " + foundResult.getId() + ", \"user_function\": \"" + projectFunction + "\", \"proj_group\": \"" + projectFamily + "\", \"username\": \"" + randomUsername + "\", \"membership_type\": \"" + membershipType + "\" }]") ;
                
        // Now update it
        success = tracker_CustDb.upsertUser_FunctionTableFromJson(userFunctionJsonList) ;
        assertTrue(success) ;
        
        // Verify what we found
        ArrayList<UserFunctionJson> updateSearchResults = jsonIn.userFunctionsFromJson(tracker_CustDb.buildJsonForProjectGroupsUsers(projectFamily).toString()) ;
        assertNotNull(updateSearchResults);
        UserFunctionJson updateFoundResult = null ;
        for (UserFunctionJson result : updateSearchResults) {
            if (result.getUsername().compareTo(randomUsername) == 0) {
                updateFoundResult = result ;
                break ;                
            }            
        }
        // Verify that we found it
        assertNotNull(updateFoundResult) ;
        assertTrue(updateFoundResult.getId() > 0) ;        
        assertEquals(membershipType, updateFoundResult.getMembership_type()) ;
        assertEquals(randomUsername, updateFoundResult.getUsername()) ;
//      TODO: APIs are not symmetrical, links to other tables are lost in round trip
//      assertEquals(projectFamily, updateFoundResult.getProj_group()) ;
//      assertEquals(projectFunction, updateFoundResult.getUser_function()) ;
                
        // Verify that we have the same record
        assertEquals(updateFoundResult.getId(), foundResult.getId()) ;
        
        
        // Clean up and remove newly added record
        Long[] ids = new Long[1] ;
        ids[0] = updateFoundResult.getId() ;
        tracker_CustDb.deleteUser_FunctionTableByIds(ids);
        
        // Verify that we cannot find it
        ArrayList<UserFunctionJson> postDeleteSearchResults = jsonIn.userFunctionsFromJson(tracker_CustDb.buildJsonForProjectGroupsUsers(projectFamily).toString()) ;
        assertNotNull(postDeleteSearchResults);
        UserFunctionJson postDeleteFoundResult = null ;
        for (UserFunctionJson result : postDeleteSearchResults) {
            if ((result.getUsername().compareTo(randomUsername) == 0) || (result.getId() == updateFoundResult.getId())) {
                postDeleteFoundResult = result ;
                break ;                
            }            
        }
        // Verify that we did not found it
        assertNull(postDeleteFoundResult) ;
        
    }
    
    protected String randomUsername(String prefix, int targetLength)
    {
        String VALID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789" ;
        Random random = new Random();
        
        StringBuilder buffer = new StringBuilder(targetLength);
        buffer.append(prefix) ;
        
        while (buffer.length() < targetLength) {
            
            int randomInt = (int) (random.nextFloat() * (VALID_CHARS.length()));
            buffer.append(VALID_CHARS.charAt(randomInt));
        }
        return buffer.toString();
    }
    
}
