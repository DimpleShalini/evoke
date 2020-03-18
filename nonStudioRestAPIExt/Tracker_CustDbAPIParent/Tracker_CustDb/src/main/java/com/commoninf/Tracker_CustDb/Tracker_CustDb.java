package com.commoninf.Tracker_CustDb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.commoninf.data.Core_Data_SheetTable;
import com.commoninf.data.DataBaseInfoTable;
import com.commoninf.data.DropDown_LxTable;
import com.commoninf.data.FormulationTable;
import com.commoninf.data.Global_Brand_NameTable;
import com.commoninf.data.High_Lvl_Prod_Type_LxTable;
import com.commoninf.data.IndicationTable;
import com.commoninf.data.Mkt_Status_LxTable;
import com.commoninf.data.Project_CoreRow;
import com.commoninf.data.Project_CoreTable;
import com.commoninf.data.Project_GroupRow;
import com.commoninf.data.Project_GroupTable;
import com.commoninf.data.Reg_Status_LxTable;
import com.commoninf.data.Substance_Type_LxTable;
import com.commoninf.data.SubstanceTable;
import com.commoninf.data.Ther_Area_LxTable;
import com.commoninf.data.User_FunctionRow;
import com.commoninf.data.User_FunctionTable;
import com.commoninf.data.User_Function_LxTable;
import com.commoninf.database.Database;
import com.commoninf.database.Row;
import com.commoninf.database.TableBase.sort_order;
import com.commoninf.json.JsonOut;
import com.commoninf.json.ProjectJson;
import com.commoninf.json.UserFunctionJson;
import com.commoninf.logger.CiiLogger;
import com.commoninf.utils.DropDownValue;

public class Tracker_CustDb extends Database {
	private static final CiiLogger logger = new CiiLogger ("com.commoninf.Tracker_CustDb");
	
	/**************************************************************************
	 * This will use the default configuration specified in the JAR's
	 * configuration.properties
	 */
	public Tracker_CustDb () {
		super ("tracker_cust");
	}
	
	/**************************************************************************
	 * 
	 * @param jdbc_url
	 * @param jdbc_class
	 * @param db_url
	 * @param db_port
	 * @param db_name
	 * @param db_user_name
	 * @param db_password
	 */
	public Tracker_CustDb (String jdbc_url, String jdbc_class, String db_url, String db_port, String db_name, String db_user_name, String db_password) {
		super (jdbc_url, jdbc_class, db_url, db_port, db_name, db_user_name, db_password);
	}
	
	/**************************************************************************
	 * 
	 * @param str
	 * @return
	 */
	public String echoStringToLog (String str) {
		logger.info(str);
		
		return "String echoed";
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public DataBaseInfoTable getDatabaseInfoTable () {
		DataBaseInfoTable databaseinfo = new DataBaseInfoTable (this);
		
		getDbConnection ();
		
		try {
			databaseinfo.getAllTableRows();
		} catch (SQLException e) {
			databaseinfo.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return databaseinfo;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public Project_CoreTable getProject_CoreTable(String sortField, boolean ascending) {
		Project_CoreTable project_core = new Project_CoreTable (this);
		
		getDbConnection ();
		try {
		    if (sortField != null) {
		        project_core.setSortOrderForAllQuerys(sortField, ascending ? Project_CoreTable.sort_order.ASCENDING : Project_CoreTable.sort_order.DESCENDING);
		    }
			project_core.getAllTableRows();			
			
            for (Row project : project_core.getTable_rows()) {
                // Add the formulation list to the project
                FormulationTable formulations = getFormulationTableByProjectCode ((String)project.getColVal("project_code"));
                if (formulations != null) {
                    project.addChildTable(formulations);
                }
                
                // Add the global brand name list to the project
                Global_Brand_NameTable gbns = getGlobalBrandNameTableByProjectCode ((String)project.getColVal("project_code"));
                if (gbns != null) {
                    project.addChildTable(gbns);
                }

                // Add the indication list to the project
                IndicationTable indications = getIndicationTableByProjectCode ((String)project.getColVal("project_code"));
                if (indications != null) {
                    project.addChildTable(indications);
                }
                
                // Add the substance list to the project
                SubstanceTable substances = getSubstanceTableByProjectCode ((String)project.getColVal("project_code"));
                if (substances != null) {
                    project.addChildTable(substances);
                }
            }            
			
		} catch (SQLException e) {
			project_core.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return project_core;
	}
	
	/**************************************************************************
	 * 
	 * @param project_code
	 * @return
	 */
	public Project_CoreTable getProject_CoreRowByProjectCode (String project_code) {
		Project_CoreTable project_core = new Project_CoreTable (this);
		String[] project_codes = {project_code};
		
		getDbConnection ();
		
		try {
			project_core.getRowsForProjectCodes(project_codes);
		} catch (SQLException e) {
			project_core.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return project_core;
	}
	
	/**************************************************************************
	 * 
	 * @param project_code
	 * @return
	 */
	public Project_CoreTable getProject_CoreRowsByProjectCodes (String[] project_codes) {
		Project_CoreTable project_core = new Project_CoreTable (this);
		
		getDbConnection ();
		
		try {
			project_core.getRowsForProjectCodes(project_codes);
		} catch (SQLException e) {
			project_core.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return project_core;
	}
	
	/**************************************************************************
	 * 
	 * @param project_code
	 * @return
	 */
	public Project_CoreTable getProject_CoreRowsByProjectGroup (String project_group) {
		Project_CoreTable project_core = new Project_CoreTable (this);
		
		getDbConnection ();
		
		try {
			project_core.getAllProjectsForAGroup(project_group);
		} catch (SQLException e) {
			project_core.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return project_core;
	}
	
	
	/**************************************************************************
	 * 
	 * @param id
	 * @return
	 */
	public Project_GroupTable getProject_GroupById (Integer id) {
		Project_GroupTable project_group = new Project_GroupTable (this);
		
		getDbConnection ();		
		
		try {
			project_group.getById(id);
		} catch (SQLException e) {
			project_group.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return project_group;
	}
	
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public Project_CoreTable getProject_CoreRowsForNullProjectGroup () {
		Project_CoreTable project_core = new Project_CoreTable (this);
		
		getDbConnection ();
		
		try {
			project_core.getAllProjectsWithNullProjGroup();
		} catch (SQLException e) {
			project_core.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return project_core;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public User_FunctionTable getUser_FunctionTable () {
		User_FunctionTable user_function = new User_FunctionTable (this);
		
		getDbConnection ();
		
		try {
			user_function.getAllTableRows();
		} catch (SQLException e) {
			user_function.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return user_function;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public User_FunctionTable getUser_FunctionTableByUser (String username) {
		User_FunctionTable user_function = new User_FunctionTable (this);
		
		getDbConnection ();
		
		try {
			user_function.getAllUserFunctionsForAUser(username);
		} catch (SQLException e) {
			user_function.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return user_function;
	}
	
    /**************************************************************************
     * 
     * @return
     */
    public User_Function_LxTable getUser_FunctionLxTable() {
        User_Function_LxTable user_function_lx = new User_Function_LxTable (this);
                
        getDbConnection ();
        
        try {
            user_function_lx.getAllTableRows();
        } catch (SQLException e) {
            user_function_lx.logSqlException (e.getMessage());
            return null;
        }
        finally {
            closeConnection ();
        }
        
        return user_function_lx;    
    }
	

	/**************************************************************************
	 * 
	 * @return
	 */
	public User_FunctionTable getUser_FunctionTableByProjectGroup (String project_group) {
		User_FunctionTable user_function = new User_FunctionTable (this);
		
		getDbConnection ();
		
		try {
			user_function.getAllUserFunctionsForAProjectGroup(project_group);
		} catch (SQLException e) {
			user_function.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return user_function;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public Core_Data_SheetTable getCore_Data_SheetTableByProjectGroup (String project_group) {
		Core_Data_SheetTable cds = new Core_Data_SheetTable (this);
		
		getDbConnection ();
		
		try {
			cds.getAllCoreDataSheetsForAProjectGroup(project_group);
		} catch (SQLException e) {
			cds.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return cds;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public FormulationTable getFormulationTableByProjectCode (String project_code) {
		FormulationTable formulation = new FormulationTable (this);
		
		getDbConnection ();
		
		try {
			formulation.setSortOrderForAllQuerys("display_order", sort_order.ASCENDING);
			formulation.getAllFormulationsForAProjectCode(project_code);
		} catch (SQLException e) {
			formulation.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return formulation;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public IndicationTable getIndicationTableByProjectCode (String project_code) {
		IndicationTable indication = new IndicationTable (this);
		
		getDbConnection ();
		
		try {
			indication.setSortOrderForAllQuerys("display_order", sort_order.ASCENDING);
			indication.getAllIndicationsForAProjectCode(project_code);
		} catch (SQLException e) {
			indication.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return indication;
	}
	
	
	/**************************************************************************
	 * 
	 * @return
	 * @author vvyasabhattu
	 * 
	 */
	public ArrayList<DropDown_LxTable> queryDropDownTables() {
		
		ArrayList<DropDown_LxTable> dropDownData = new ArrayList<DropDown_LxTable>();		
		try{			
			getDbConnection ();			
			for(String tableName: DropDown_LxTable.dropDownTables){
				DropDown_LxTable dropDownTable = new DropDown_LxTable(this, tableName);				
				dropDownTable.setSortOrderForAllQuerys("display_order", sort_order.ASCENDING);
				dropDownTable.getDropDownValues();				
				dropDownData.add(dropDownTable);				
			} 
		}
		catch (SQLException e) {
			logger.error ("SQL Exception message: "+e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
				
		return dropDownData;
	}
		
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public SubstanceTable getSubstanceTableByProjectCode (String project_code) {
		SubstanceTable substance = new SubstanceTable (this);
		
		getDbConnection ();
		
		try {
			substance.setSortOrderForAllQuerys("display_order", sort_order.ASCENDING);
			substance.getAllSubstancesForAProjectCode(project_code);
		} catch (SQLException e) {
			substance.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return substance;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public Global_Brand_NameTable getGlobalBrandNameTableByProjectCode (String project_code) {
		Global_Brand_NameTable gbn = new Global_Brand_NameTable (this);
		
		getDbConnection ();
		
		try {
			gbn.setSortOrderForAllQuerys("display_order", sort_order.ASCENDING);
			gbn.getAllGlobalBrandNamesForAProjectCode(project_code);
		} catch (SQLException e) {
			gbn.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return gbn;
	}
	
	/**************************************************************************
	 *
	 * @return
	 */
	public Project_GroupTable getAllProjectGroups() {
		Project_GroupTable pg = new Project_GroupTable(this);

		getDbConnection ();

		try {
			pg.getAllTableRows();
		} catch (SQLException e) {
			pg.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}

		return pg;
	}
	
	/**************************************************************************
	 * Fetch a project group by name
	 * @return
	 */
	public Project_GroupTable getProject_GroupByName (String name) {
		Project_GroupTable project_group = new Project_GroupTable (this);
		
		getDbConnection ();
		
		try {
			project_group.getProjectGroupByName(name);
		} catch (SQLException e) {
			project_group.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return project_group;
	}	
	
	/**************************************************************************
	 * Fetch a project family/group with derived project list and return it 
	 * as a map including the id (key) and json representation (value)  
	 */
	public Map<Integer, String> getProject_GroupJsonByName (String name) {
		Map<Integer, String> results = null ;
		
		Project_GroupTable project_group = new Project_GroupTable (this);
		Project_CoreTable project_core = new Project_CoreTable (this) ;
		getDbConnection ();
		
		try {
			project_group.getProjectGroupByName(name);
			ArrayList<Row> rows = project_group.getTable_rows() ;
			if (!rows.isEmpty()) {
				Row row = rows.get(0);
				
				project_core.getAllProjectsForAGroup(name);
				ArrayList<Row> coreRows = project_core.getTable_rows() ;
				
				ArrayList<String> projects = new ArrayList<String>(); 
				for (Row coreRow : coreRows) {
					String coreRowName = coreRow.getColVal("project_code").toString() ;
					projects.add(coreRowName) ;
				}				
				row.setColVal("Projects", projects) ;
				
				Integer id = (Integer) row.getColVal("id");
				String json = new JsonOut(row.getCol_vals()).toString();
				results = new LinkedHashMap<Integer, String>();
				results.put(id, json) ;				
			}			
		} catch (SQLException e) {
			project_group.logSqlException (e.getMessage());
		}
		finally {
			closeConnection ();
		}
		
		return results;
	}
	
	/**************************************************************************
	 * Create a new project group with the specified name
	 */
	public Project_GroupTable createProject_Group (String name) {
		Project_GroupTable project_group = new Project_GroupTable (this);
		
		getDbConnection ();
		
		try {
			Project_GroupRow row = new Project_GroupRow();
			row.setColVal("group_name", name) ;
			row.setColVal("registered_yn", "N") ;
			
			project_group.addTableRow(row);
			project_group.insertTableRows(true);
			
		} catch (SQLException e) {
			project_group.logSqlException (e.getMessage());
			return null;
		}
		finally {
			closeConnection ();
		}
		
		return project_group;
	}
	
	/**
	 * Set the project family names for all projects identified by ids 
	 */
	public boolean setProjectProject_Group(String projectFamilyName, ArrayList<Integer> projectIds) {
		boolean success = false ;

		Project_CoreTable ct = new Project_CoreTable(this); 
		try {
			getDbConnection ();
			
		
			// Fetch Project Family row (to make sure it exists)
			ArrayList<Row> projectFamilyRows = getProject_GroupByName(projectFamilyName).getTable_rows() ;
			if (!projectFamilyRows.isEmpty()) {
				
				Integer[] ids = projectIds.toArray(new Integer[0]);
                ct.getRowsForIds(ids);
                
                for (Row projectCore : ct.getTable_rows()) {
                	projectCore.setColVal("proj_group_id", projectFamilyName) ;                	
                }
                
                ct.updateTableRowsById() ;
                success = true ;
			}
		} catch (SQLException e) {
			ct.logSqlException (e.getMessage());
		}
		finally {
			closeConnection ();
		}
	
		return success ;
	}
	
	
	/**
	 * Fetch all of the substance types
	 * @return
	 */
	public Substance_Type_LxTable getAllSubstanceTypes() {
	    Substance_Type_LxTable results = new Substance_Type_LxTable(this);

        getDbConnection ();

        try {
            results.getAllTableRows();
        } catch (SQLException e) {
            results.logSqlException (e.getMessage());
            return null;
        }
        finally {
            closeConnection ();
        }

        return results;
	}
	
	/**
     * Fetch all of the product types
     * @return
     */
    public High_Lvl_Prod_Type_LxTable getAllProductTypes() {
        High_Lvl_Prod_Type_LxTable results = new High_Lvl_Prod_Type_LxTable(this);

        getDbConnection ();

        try {
            results.getAllTableRows();
        } catch (SQLException e) {
            results.logSqlException (e.getMessage());
            return null;
        }
        finally {
            closeConnection ();
        }

        return results;
    }
    
    /**
     * Fetch all of the market statuses
     * @return
     */
    public Mkt_Status_LxTable getAllMarketingStatuses() {
        Mkt_Status_LxTable results = new Mkt_Status_LxTable(this);

        getDbConnection ();

        try {
            results.getAllTableRows();
        } catch (SQLException e) {
            results.logSqlException (e.getMessage());
            return null;
        }
        finally {
            closeConnection ();
        }

        return results;
    }
    
    /**
     * Fetch all of the Registration statuses
     * @return
     */
    public Reg_Status_LxTable getAllRegistrationStatuses() {
        Reg_Status_LxTable results = new Reg_Status_LxTable(this);

        getDbConnection ();

        try {
            results.getAllTableRows();
        } catch (SQLException e) {
            results.logSqlException (e.getMessage());
            return null;
        }
        finally {
            closeConnection ();
        }

        return results;
    }
    
    /**
     * Fetch all of the Therapeutic areas
     * @return
     */
    public Ther_Area_LxTable getAllTherapeuticAreas() {
        Ther_Area_LxTable results = new Ther_Area_LxTable(this);

        getDbConnection ();

        try {
            results.getAllTableRows();
        } catch (SQLException e) {
            results.logSqlException (e.getMessage());
            return null;
        }
        finally {
            closeConnection ();
        }

        return results;
    }    
    

	/**************************************************************************
	 * 
	 * @return
	 */
	public JsonOut buildJsonForDataBaseInfo () {
		JsonOut json_out = null;
		
		getDbConnection ();
		
		DataBaseInfoTable db_rows = getDatabaseInfoTable ();
		
		if (db_rows != null) {
    		closeConnection ();
    		json_out = new JsonOut (db_rows.getTable_rows());
    	}
		else {
			json_out = new JsonOut();
		}
		
		return json_out;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public JsonOut buildJsonForUsersProjectGroups (String username) {
		JsonOut json_out = null;
		
		getDbConnection ();
		
		// Get all of the project groups and roles for the project groups for a user name.
		User_FunctionTable users_proj = getUser_FunctionTableByUser (username);
		
		if (users_proj != null) {
			for (Row pg_row : users_proj.getTable_rows()) {
				// Add the registered_yn value for each project group
				Project_GroupTable pc = getProject_GroupByName ((String)pg_row.getColVal("proj_group_id"));
				// It is ok to simply use the first index here because the
				// result set should always be only one project_group.  If
				// it isn't then the database is corrupt.
				pg_row.addDerivedColumn("registered_yn", pc.getTable_rows().get(0).getColVal("registered_yn"));
				
				// Add the CDS to the project group
				Core_Data_SheetTable cds = getCore_Data_SheetTableByProjectGroup ((String)pg_row.getColVal("proj_group_id"));
				if (cds != null) {
					pg_row.addChildTable(cds);
				}
				
				// Add the projects to the project group
				Project_CoreTable projects = getProject_CoreRowsByProjectGroup ((String)pg_row.getColVal("proj_group_id"));
				if (projects != null) {
					for (Row project : projects.getTable_rows()) {
						// Add the formulation list to the project
						FormulationTable formulations = getFormulationTableByProjectCode ((String)project.getColVal("project_code"));
						if (formulations != null) {
							project.addChildTable(formulations);
						}
						
						// Add the global brand name list to the project
						Global_Brand_NameTable gbns = getGlobalBrandNameTableByProjectCode ((String)project.getColVal("project_code"));
						if (gbns != null) {
							project.addChildTable(gbns);
						}

						// Add the indication list to the project
						IndicationTable indications = getIndicationTableByProjectCode ((String)project.getColVal("project_code"));
						if (indications != null) {
							project.addChildTable(indications);
						}
						
						// Add the substance list to the project
						SubstanceTable substances = getSubstanceTableByProjectCode ((String)project.getColVal("project_code"));
						if (substances != null) {
							project.addChildTable(substances);
						}
					}
					pg_row.addChildTable(projects);
				}
			}
			
			closeConnection ();
			
			json_out = new JsonOut (users_proj.getTable_rows());
		}
		else {
			json_out = new JsonOut();
		}
		
		return json_out;
	}
	
    /**************************************************************************
     * 
     * @return
     */
    public JsonOut buildJsonForUserFunctionLx() {
        JsonOut json_out = null;
        
        User_Function_LxTable table = getUser_FunctionLxTable() ;
        
        json_out = new JsonOut (table.getTable_rows());
        
        return json_out;
    }
    
    
    public JsonOut buildJsonForProjectGroup(String group, int page, int count) {
        JsonOut json_out = null;

        getDbConnection ();
        
        // Add the projects to the project group
        Project_CoreTable projects ;
        if (group == null) {
            projects = getProject_CoreRowsForNullProjectGroup();
        } else {
            projects = getProject_CoreRowsByProjectGroup(group);    
        }

                     
        if (projects != null) {
            for (Row project : projects.getTable_rows()) {
                // Add the formulation list to the project
                FormulationTable formulations = getFormulationTableByProjectCode ((String)project.getColVal("project_code"));
                if (formulations != null) {
                    project.addChildTable(formulations);
                }

                // Add the global brand name list to the project
                Global_Brand_NameTable gbns = getGlobalBrandNameTableByProjectCode ((String)project.getColVal("project_code"));
                if (gbns != null) {
                    project.addChildTable(gbns);
                }

                // Add the indication list to the project
                IndicationTable indications = getIndicationTableByProjectCode ((String)project.getColVal("project_code"));
                if (indications != null) {
                    project.addChildTable(indications);
                }

                // Add the substance list to the project
                SubstanceTable substances = getSubstanceTableByProjectCode ((String)project.getColVal("project_code"));
                if (substances != null) {
                    project.addChildTable(substances);
                }
            }
        }

        closeConnection ();

        json_out = new JsonOut (projects.getTable_rows(), page, count);
        return json_out ;
    }
    

    public JsonOut buildJsonForProjectGroupId(Integer id) {
        JsonOut json_out = null;

        getDbConnection ();
                
        Project_GroupTable project_groups = getProject_GroupById(id);
		if (project_groups != null && project_groups.getTable_rows().size() > 0) {			
        		        
			// Add the projects to the project group
			for (Row pg_row : project_groups.getTable_rows()) {
											
				String projectGroupId = (String) pg_row.getColVal("group_name") ;

				// Add the CDS to the project group
				Core_Data_SheetTable cds = getCore_Data_SheetTableByProjectGroup(projectGroupId);
				if (cds != null) {
					pg_row.addChildTable(cds);
				}				

				// Add the projects to the project group
				Project_CoreTable projects = getProject_CoreRowsByProjectGroup(projectGroupId);				
				if (projects != null) {
					for (Row project : projects.getTable_rows()) {
						// Add the formulation list to the project
						FormulationTable formulations = getFormulationTableByProjectCode ((String)project.getColVal("project_code"));
						if (formulations != null) {
							project.addChildTable(formulations);
						}

						// Add the global brand name list to the project
						Global_Brand_NameTable gbns = getGlobalBrandNameTableByProjectCode ((String)project.getColVal("project_code"));
						if (gbns != null) {
							project.addChildTable(gbns);
						}

						// Add the indication list to the project
						IndicationTable indications = getIndicationTableByProjectCode ((String)project.getColVal("project_code"));
						if (indications != null) {
							project.addChildTable(indications);
						}

						// Add the substance list to the project
						SubstanceTable substances = getSubstanceTableByProjectCode ((String)project.getColVal("project_code"));
						if (substances != null) {
							project.addChildTable(substances);
						}
					}
					pg_row.addChildTable(projects);
				}
			}
		}

        closeConnection ();

        json_out = new JsonOut (project_groups.getTable_rows());
        return json_out ;
    }    
	
	/**************************************************************************
	 *
	 * @return
	 */
	public JsonOut buildJsonForProjectGroups () {
		JsonOut json_out = null;

		getDbConnection ();

		Project_GroupTable project_groups = getAllProjectGroups() ;
		if (project_groups != null) {
						
			for (Row pg_row : project_groups.getTable_rows()) {				
				String projectGroupId = (String) pg_row.getColVal("group_name") ;
				
				// Add Key Users
				// TODO: Only load user functions we care about
				User_FunctionTable userFunctionTable = getUser_FunctionTableByProjectGroup(projectGroupId) ;
				Map<String, String> keyRoles = new LinkedHashMap<String, String>() ;
				if (userFunctionTable != null) {
					for (Row userFunction: userFunctionTable.getTable_rows()) {
						String userFunctionName = (String) userFunction.getColVal("user_function_id") ;
						if (userFunctionName == null)
							userFunctionName = "" ;
						String userName = (String) userFunction.getColVal("username");
						if (userFunctionName.equalsIgnoreCase("Safety Lead") || userFunctionName.equalsIgnoreCase("TA Head Patient Safety")) {
							keyRoles.put(userFunctionName.replace(" ", "_").toLowerCase(), userName);
						}
					}
				}
				pg_row.addDerivedColumn("key_roles", keyRoles) ;

				// Add the CDS to the project group
				Core_Data_SheetTable cds = getCore_Data_SheetTableByProjectGroup(projectGroupId);
				if (cds != null) {
					pg_row.addChildTable(cds);
				}				

				// Add the projects to the project group
				Project_CoreTable projects = getProject_CoreRowsByProjectGroup(projectGroupId);				
				if (projects != null) {
					for (Row project : projects.getTable_rows()) {
						// Add the formulation list to the project
						FormulationTable formulations = getFormulationTableByProjectCode ((String)project.getColVal("project_code"));
						if (formulations != null) {
							project.addChildTable(formulations);
						}

						// Add the global brand name list to the project
						Global_Brand_NameTable gbns = getGlobalBrandNameTableByProjectCode ((String)project.getColVal("project_code"));
						if (gbns != null) {
							project.addChildTable(gbns);
						}

						// Add the indication list to the project
						IndicationTable indications = getIndicationTableByProjectCode ((String)project.getColVal("project_code"));
						if (indications != null) {
							project.addChildTable(indications);
						}

						// Add the substance list to the project
						SubstanceTable substances = getSubstanceTableByProjectCode ((String)project.getColVal("project_code"));
						if (substances != null) {
							project.addChildTable(substances);
						}
					}
					pg_row.addChildTable(projects);
				}
			}

			closeConnection ();

			json_out = new JsonOut (project_groups.getTable_rows());
		}
		else {
			json_out = new JsonOut();
		}

		return json_out;
	}
	
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public JsonOut buildJsonForProjectGroupsUsers (String project_group) {
		JsonOut json_out = null;
		getDbConnection ();
		
		try {			
			// Get all of the users and their roles for a project group.
			User_FunctionTable projs_users = getUser_FunctionTableByProjectGroup (project_group);
			if (projs_users != null) {			
				json_out = new JsonOut (projs_users.getTable_rows());
			}
			else {
				json_out = new JsonOut();
			}
		} finally {
			closeConnection ();
		}
		
		return json_out;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public JsonOut buildJsonForProjectGroupsUsersById (String project_group_id) {
		JsonOut json_out = null;
		getDbConnection ();
				
		try {
			Project_GroupTable project_groups = getProject_GroupById(Integer.parseInt(project_group_id));
			if (project_groups != null && project_groups.getTable_rows().size() > 0) {
				Row pg_row = project_groups.getTable_rows().get(0);	// Must have one element, should have no more than one.
				
				// Get all of the users and their roles for a project group.
				User_FunctionTable projs_users = getUser_FunctionTableByProjectGroup ((String) pg_row.getColVal("group_name"));
				if (projs_users != null) {
					json_out = new JsonOut (projs_users.getTable_rows());
				}
				else {
					json_out = new JsonOut();
				}
			} else {
				json_out = new JsonOut();
			}
		} finally {
			closeConnection ();
		}
		
		return json_out;
	}	
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public boolean updateProjectGroup(String project_group, String newProjectGroupName, String indicationFullText) {
		boolean error = false ;
		int count = 0 ;
		Project_GroupTable searchTable = new Project_GroupTable (this);
		Project_GroupTable updateTable = new Project_GroupTable (this);

		try {
			getDbConnection ();	

			// Update row(s) -- should only be one
			searchTable.getProjectGroupByName(project_group) ;
			for (Row projectGroupRow : searchTable.getTable_rows()) {
				if (newProjectGroupName != null) {
				    projectGroupRow.setColVal("group_name", newProjectGroupName) ;
				}
				if (indicationFullText != null) {
				    projectGroupRow.setColVal("indication_full_text", indicationFullText);
				}
				updateTable.addTableRow(projectGroupRow);
				count++ ;
			}
			
			if (count == 1) {
				updateTable.updateTableRowsById();
			} else {
				logger.error("Refusing to update project group " + project_group + ", with instance count of " + count);
				error = true ;
			}		
		} catch (SQLException e) {
		    error = true ;
		    updateTable.logSqlException (e.getMessage());                
		}
		finally {
			closeConnection ();
		}		
		
		return !error ;
	}
	
	
	/**************************************************************************
	 * 
	 * @param json_obj
	 */
	public boolean insertUser_FunctionTableFromJson (ArrayList<UserFunctionJson> json_obj) {
		// Translate the json output into the table
		User_FunctionTable uf_table = null;
		boolean error = false;
		
		getDbConnection ();
		
		for (UserFunctionJson uf : json_obj) {
			User_FunctionRow urow = null;
			
			if (!uf.isEdit()) {
				if (uf_table == null) {
					uf_table = new User_FunctionTable (this);
				}
				urow = new User_FunctionRow (uf);
				uf_table.addTableRow(urow);
			}
			else {
				logger.error ("UserFunctionJson has edit set to true when trying to do an insert.");
				error = true;
			}
			//logger.info(urow.toString());
		}
		
		/*
		 * Now actually do all of the updates/creations
		 */
		if (!error) {
			try {
				uf_table.insertTableRows(true);				
			} catch (SQLException e) {
			    error = true ;
				uf_table.logSqlException (e.getMessage());                
			}
			finally {
				closeConnection ();
			}
		}
		else {
			closeConnection ();
		}
		
		return !error ;
	}
	
	/**************************************************************************
	 * 
	 * @param json_obj
	 */
	public boolean updateUser_FunctionTableFromJson (ArrayList<UserFunctionJson> json_obj) {
		// Translate the json output into the table 
		User_FunctionTable uf_original_table = null;
		User_FunctionTable uf_update_table = null;
		boolean error = false;
		
		getDbConnection ();
		
		for (UserFunctionJson uf : json_obj) {
			User_FunctionRow urow = null;
			
			if (uf.isEdit()) {
				if (uf_update_table == null) {
					uf_update_table = new User_FunctionTable (this);
				}
				urow = new User_FunctionRow (uf);
				uf_update_table.addTableRow(urow);
			}
			else {
				if (uf_original_table == null) {
					uf_original_table = new User_FunctionTable (this);
				}
				urow = new User_FunctionRow (uf);
				uf_original_table.addTableRow(urow);
			}
			//logger.info(urow.toString());
		}
		
		/*
		 * Now actually do all of the updates/creations
		 */
		try {
			uf_update_table.updateTableRows(uf_original_table);
		} catch (SQLException e) {
		    error = true ;
			uf_update_table.logSqlException (e.getMessage());
		}
		finally {
			closeConnection ();
		}
		
		return !error ;
	}
	
    /**************************************************************************
     * Combined update and insert user function based on id within the 
     * records.   If the id is > 0, it is an update, if <= 0, it is an insert
     * 
     * @param json_obj
     */
    public boolean upsertUser_FunctionTableFromJson (ArrayList<UserFunctionJson> json_obj) {        
        // Translate the json output into the table 
        User_FunctionTable uf_update_table = null;
        User_FunctionTable uf_insert_table = null;
        boolean error = false;
        
        getDbConnection ();
        
        for (UserFunctionJson uf : json_obj) {
            User_FunctionRow newRow = new User_FunctionRow (uf) ;
            if (uf.getId() > 0) {
            	newRow.setColVal("id", uf.getId()) ;
                // Add to update list
                if (uf_update_table == null) {
                    uf_update_table = new User_FunctionTable (this);
                }
                uf_update_table.addTableRow(newRow);                
            } else {
                // Add to insert list
                if (uf_insert_table == null) {
                    uf_insert_table = new User_FunctionTable (this);
                }
                uf_insert_table.addTableRow(newRow);                                
            }
        }
        
        /*
         * Now actually do all of the updates/creations
         */
        try {
            // Updates first incase we are inserting something replaced by an update
            if (uf_update_table != null) {
                uf_update_table.updateTableRowsById();
            }
            
            // Then inserts
            if (uf_insert_table != null) {
                uf_insert_table.insertTableRows(true);
            }
            
        } catch (SQLException e) {
            error = true ;
            if (uf_update_table != null)
            	uf_update_table.logSqlException (e.getMessage());
            else if (uf_insert_table != null)
            	uf_insert_table.logSqlException (e.getMessage());
        }
        finally {
            closeConnection ();
        }
        
        return !error ;
    }
    
    
    /**
     * Delete the designated list of IDs
     * 
     * @param ids
     * @return
     */
    public boolean deleteUser_FunctionTableByIds (Long ids[]) {
        boolean error = false;
        User_FunctionTable update_table = null;
        
        getDbConnection ();
        
        try 
        {
            update_table = new User_FunctionTable (this);
            update_table.deleteByIds(ids);
        } catch (SQLException e) {
            error = true ;
            update_table.logSqlException (e.getMessage());
        }
        finally {
            closeConnection ();
        }
        
        return !error ;
    }
	
	/**************************************************************************
	 * 
	 * @param json_obj
	 */
	public void insertProject_GroupTableFromJson (ArrayList<ProjectJson> json_obj) {
		// Translate the json output into the table 
		Project_GroupTable pg_table = null;
		
		getDbConnection ();
		
		for (ProjectJson pg : json_obj) {
			if (pg.getProject_family() != null && !pg.getProject_family().isEmpty()) {
				Project_GroupRow pgrow = null;			

				if (pg_table == null) {
					pg_table = new Project_GroupTable (this);
				}
				pgrow = new Project_GroupRow (pg, this);
				pg_table.addTableRow(pgrow);
			}
		}
		
		/*
		 * Now actually do all of the inserts
		 */
		try {
			if (pg_table != null) {
				pg_table.insertTableRows(true);
			}
		} catch (SQLException e) {
			pg_table.logSqlException (e.getMessage());
		}
		finally {
			if (pg_table != null) {
				pg_table.clearChildTables();
			}
			closeConnection ();
		}
	}
	
	/**************************************************************************
	 * 
	 * @param json_obj
	 */
	public boolean insertProject_CoreTableFromJson (ArrayList<ProjectJson> json_obj) {		
		boolean error = false;
		
		// Translate the json output into the table 
		Project_CoreTable pc_table = null;		
				
		getDbConnection ();
		
		// We always want to first update the project group table since this
		// list of projects may include a new group. Any new ones will be
		// needed as foreign key when we write the project_core table
		insertProject_GroupTableFromJson (json_obj);
			
		for (ProjectJson pc : json_obj) {
			if (pc_table == null) {
				pc_table = new Project_CoreTable (this);
			}
			Project_CoreRow pcrow = new Project_CoreRow (pc, this);
			pc_table.addTableRow(pcrow);

			//logger.info(pcrow.toString());
		}
				
		/*
		 * Now actually do all of the updates/creations
		 */
		try {
			if (pc_table != null) {
				pc_table.insertTableRows(true);
			}
		} catch (SQLException e) {
			error = true;
			pc_table.logSqlException (e.getMessage());
		}
		finally {
			// We are done with the child tables so clear all rows in them
			pc_table.clearChildTables();
			closeConnection ();
		}
		
		return !error;
	}
	
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public boolean updateProject_GroupRegistration(String projectGroupName) {
		boolean error = false;
		boolean registered = false ;
		
		// Translate the json output into the table 
		Project_CoreTable pc_table = new Project_CoreTable (this);
		Project_GroupTable pg_table = new Project_GroupTable(this);
        Project_GroupTable pg_table_updated = null;

        getDbConnection ();
		
		try {
			pc_table.getAllProjectsForAGroup(projectGroupName);
			ArrayList<Row> table_rows = pc_table.getTable_rows();
			
			for (Row row : table_rows) {
				String registrationStatus = (String)row.getColVal("reg_status_id") ;
				if (registrationStatus != null && registrationStatus.compareToIgnoreCase("Approved") == 0) {
					registered = true ;
					break ;
				}				
			}
			
			pg_table.getProjectGroupByName(projectGroupName);
			ArrayList<Row> pg_table_rows = pg_table.getTable_rows();
			for (Row row : pg_table_rows) {
				String expectedValue = registered ? "Y" : "N";
				String registered_yn = (String) row.getColVal("registered_yn");
				if (expectedValue.compareToIgnoreCase(registered_yn) != 0) {					
					row.setColVal("registered_yn", expectedValue);
					if (pg_table_updated == null) {
						pg_table_updated = new Project_GroupTable(this);						
					}
					pg_table_updated.addTableRow(row);
				}
			}
			
			if (pg_table_updated != null) {
				pg_table_updated.updateTableRowsById();
			}
			
		} catch (SQLException e) {
			error = true;
			pg_table.logSqlException (e.getMessage());
		}
		finally {
			closeConnection ();
		}

		return !error ;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public Map<String, DropDownValue>  getDropDownsData () {
		return getDropDownsData (false);
	}
	
	/**************************************************************************
	 * 
	 * @return
	 * @author vvyasabhattu
	 * Queries all the tables for dropdown values
	 */
	public Map<String, DropDownValue>  getDropDownsData (boolean inc_ids) {
		
		//ArrayList<DropDownsJson> jsonData = new ArrayList<DropDownsJson>();		
		ArrayList<DropDown_LxTable> dropDownTablesData = queryDropDownTables();		
		
		Map<String,DropDownValue> dropDownMap = new LinkedHashMap<>();
		if(dropDownTablesData != null){
			for(DropDown_LxTable dropDownTable: dropDownTablesData){
				boolean is_any_add_cfg_values_not_null = false;
				ArrayList<Row> table_rows = dropDownTable.getTable_rows();
				List<String> values = new ArrayList<>();
				List<String> defaults = new ArrayList<>();
				List<String> add_cfg_vals = new ArrayList<>();
				List<Integer> cl_ids = new ArrayList<>();
				List<Integer> display_orders = new ArrayList<>();
				for (Row row : table_rows) {
					values.add(row.getColVal("name").toString());
					if(row.getColVal("isdefault_yn").toString().equalsIgnoreCase("Y")){
						defaults.add(row.getColVal("name").toString());
					}
					Object add_cfg_val = row.getColVal("add_cfg_values");
					if (add_cfg_val == null) {
						add_cfg_vals.add((String)add_cfg_val);
					}
					else {
						is_any_add_cfg_values_not_null = true;
						add_cfg_vals.add(add_cfg_val.toString());
					}
					cl_ids.add((Integer)row.getColVal("id"));
					display_orders.add((Integer)row.getColVal("display_order"));
				}
				DropDownValue dropDownValue = new DropDownValue();
				dropDownValue.setValues(values);
				dropDownValue.setDefaults(defaults);
				dropDownValue.setDisplay_orders(display_orders);				
				// Only add the add_cfg_values if at least one row is not null
				if (is_any_add_cfg_values_not_null) {
					dropDownValue.setAdd_cfg_values(add_cfg_vals);
				}
				// Only add the cl_ids if requested to
				if (inc_ids) {
					dropDownValue.setCl_ids(cl_ids);
				}
				
				dropDownMap.put(dropDownTable.getTable_name(), dropDownValue);
			}
		}
		return dropDownMap;
	}
}
