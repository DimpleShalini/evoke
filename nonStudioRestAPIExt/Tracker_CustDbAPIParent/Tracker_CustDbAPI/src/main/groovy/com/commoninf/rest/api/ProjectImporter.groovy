/**
 * Copyright (c) 2019 Commonwealth Informatics Inc. All rights reserved.
 */
package com.commoninf.rest.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.text.FieldPosition

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import com.bonitasoft.web.extension.rest.RestAPIContext
import com.commoninf.Tracker_CustDb.Tracker_CustDb
import com.commoninf.database.Row
import com.commoninf.data.High_Lvl_Prod_Type_LxTable
import com.commoninf.data.Substance_Type_LxTable
import com.commoninf.data.Mkt_Status_LxTable
import com.commoninf.data.Reg_Status_LxTable
import com.commoninf.data.Ther_Area_LxTable
import com.commoninf.data.Project_CoreTable
import com.commoninf.json.ProjectJson

import groovy.json.JsonOutput

/**
 * Import Projects and create Therapies from a CSV file.  Please call validate() and then 
 * process().  Results can be obtained using getLog(), getWarnings(), and getErrors().
 */
public class ProjectImporter {
	
	/*
	 * Constants for CVS column header names
	 */
    public static final String COLUMN_COMPOUND_CODE = "Compound Code"
    public static final String COLUMN_PROJECT_CODE = "Project Code"
    public static final String COLUMN_THERAPY_NAME = "Therapy Name"    
    public static final String COLUMN_BRAND_NAMES = "Brand Name"
    public static final String COLUMN_INNS = "Substance(s) or INN(s)"
    public static final String COLUMN_SUBSTANCE_SOURCE = "Substance name sources"
    public static final String COLUMN_MARKETING_STATUS = "Marketing Status"
    public static final String COLUMN_REGISTRATION_STATUS = "Registration Status"
    public static final String COLUMN_INDICATION_SHORT_FORMS = "Indication Short Form"
    public static final String COLUMN_FORMULATIONS = "Formulation"
    public static final String COLUMN_THERAPEUTIC_AREA = "Therapeutic Area"
    public static final String COLUMN_DIVISION = "Corporate Division"
    public static final String COLUMN_PRODUCT_TYPE = "Type of Product"
    public static final String COLUMN_PRODUCT_CLASSIFICATION = "Product Category Classification"
    public static final String COLUMN_DUTY_OF_CARE = "Duty of Care"
    public static final String COLUMN_COMPLEXITY = "Complexity Classification"
	
	/**
	 * List of required headers -- these must be present
	 */
    public static final String[] REQUIRED_HEADERS = [ 
            COLUMN_COMPOUND_CODE, 
            COLUMN_PROJECT_CODE,
            COLUMN_THERAPY_NAME,
            COLUMN_BRAND_NAMES,
            COLUMN_INNS,
            COLUMN_SUBSTANCE_SOURCE,
            COLUMN_MARKETING_STATUS,
            COLUMN_REGISTRATION_STATUS,
            COLUMN_INDICATION_SHORT_FORMS,
            COLUMN_FORMULATIONS,
            COLUMN_THERAPEUTIC_AREA,
            COLUMN_DIVISION,
            COLUMN_PRODUCT_TYPE,
            COLUMN_PRODUCT_CLASSIFICATION,
            COLUMN_DUTY_OF_CARE,
            COLUMN_COMPLEXITY 
    ]

	/**
	 * Columns where a value is required
	 */
    public static final String[] REQUIRED_FIELDS = [ 
            COLUMN_PROJECT_CODE 
    ]

	/**
	 * Columns where multiple values are allows (delimited by ProjectImporter.SEPARATOR)
	 */
    public static final String[] ALLOWS_MULTIPLE_FIELD_VALUES = [ 
            COLUMN_BRAND_NAMES, 
            COLUMN_INNS, 
            COLUMN_INDICATION_SHORT_FORMS, 
            COLUMN_FORMULATIONS 
    ]
	
	/**
	 * Maximum field lengths for each column.   When multiple fields are allowed, the max is 
	 * applied to the the individual value. 
	 */
	private static final Map<String, Integer> MAX_FIELD_LENGTHS = new HashMap<String, Integer>() {
		{
			put(COLUMN_COMPOUND_CODE, 60)
			put(COLUMN_PROJECT_CODE, 60)
			put(COLUMN_THERAPY_NAME, 60)
			put(COLUMN_BRAND_NAMES, 60)
			put(COLUMN_INNS, 2000)
			put(COLUMN_SUBSTANCE_SOURCE, 60)
			put(COLUMN_MARKETING_STATUS, 60)
			put(COLUMN_REGISTRATION_STATUS, 60)
			put(COLUMN_INDICATION_SHORT_FORMS, 2000)
			put(COLUMN_FORMULATIONS, 200)
			put(COLUMN_THERAPEUTIC_AREA, 60)
			put(COLUMN_DIVISION, 200)
			put(COLUMN_PRODUCT_TYPE, 60)
			put(COLUMN_PRODUCT_CLASSIFICATION, 60)
			put(COLUMN_DUTY_OF_CARE, 60)
			put(COLUMN_COMPLEXITY, 60)
		}
	}

	
	/**
	 * When display a field that is too long, truncate at this length
	 */
	public static final int MAX_VALUE_DISPLAY_LENGTH = 80
	
	/**
	 * Separate for multiple values
	 */
    public static final String SEPARATOR = "|"
	public static final String SEPARATOR_REGEX = "\\|"
	
	/**
	 * Separate for display purpose (stored in data model)
	 */		
	public static final String DISPLAY_SEPARATOR = " + "

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectImporter.class)
	
    String filename
    CSVParser csvParser
    List<CSVRecord> csvRecords
    StringBuffer logBuffer = new StringBuffer()
    int warnings = 0
    int errors = 0
	
    Tracker_CustDb theDb	
    Map<String, Integer> productTypeMap = new HashMap<String, Integer>() 
    Map<String, Integer> substanceTypeMap = new HashMap<String, Integer>()
    Map<String, Integer> marketingStatusMap = new HashMap<String, Integer>()
    Map<String, Integer> registrationStatusMap = new HashMap<String, Integer>()
    Map<String, Integer> therapeuticAreaMap = new HashMap<String, Integer>()
    Set<String> existingProjectCodes = new HashSet<String>()
		
	
	/**
	 * Construct a Project Importer
	 * 
	 * @param csvParser Preconfigured CSV Parser (defaults are fine, reloaded with data)
	 * @param filename Used only for logging
	 * @param context Required for database initialization
	 */
    public ProjectImporter(CSVParser csvParser, String filename, RestAPIContext context) {
        this.csvParser = csvParser
        this.filename = filename
        theDb = DbUtils.initTracker_CustDbConfig(context)
		
		initializeCodeLists()
		initializeExistingProjectCodes()
    }

	
	/**
	 * Validate the inbound data based on hard code business rules 
	 *  - CVS Headers Exist
	 *  - Required Fields Exist
	 *  - Field Lengths are within bounds
	 *  - Project Codes are unique (existing and within itself)
	 *  - Code Lists are consistent (post mapping)
	 *  - Multiple values (separated by SEPARATOR) are only where expected
	 *   
	 * @return true if validate, otherwise false
	 */
    public boolean validate() {

        long startMS = System.currentTimeMillis()
        info("Starting project import validate process for " + filename)
		
        boolean overallSuccess = false

        if (    validateHeaders() &&
                validateMandatoryFields() && 
                validateFieldLengths() &&
                validateProjectCodeUniqueness() &&
                validateCodeListConsistency() &&
                validateCardinality()) {

            overallSuccess = true
        }

        info("Project import validation process " + (overallSuccess ? "completed" : "failed") + " with " + 
                warnings + pluralize(warnings, " warning") + " and " +
                errors + pluralize(errors, " error" )+ 
                " in " + (System.currentTimeMillis() - startMS) + "ms")

        return overallSuccess
    }
	

	/**
	 * Process the CSV import.   This assumes that validate has been run, but it does not enforce
	 * it.
	 * 
	 * @return true if successful, otherwise false
	 */
    public boolean process() {

        long startMS = System.currentTimeMillis()
        boolean overallSuccess = false

        info("Starting project import process for " + filename)
		
		this.theDb.getDbConnection()		
		try {
			Set<String> uniqueProjectFamilies = new HashSet<String>() 
					
			// Convert into an ProjectJson array for import
			ArrayList<ProjectJson> projects = new ArrayList<ProjectJson>() 
			for (CSVRecord csvRecord : csvRecords) {
				ProjectJson project = new ProjectJson()			
												
				project.setProject_code(trim(csvRecord.get(COLUMN_PROJECT_CODE)))
				project.setProject_family(trim(csvRecord.get(COLUMN_THERAPY_NAME)))
				project.setCompound_code(trim(csvRecord.get(COLUMN_COMPOUND_CODE)))
				project.setSubstance_name_sources(mapCodeListValue(substanceTypeMap, trimAndDefault(csvRecord.get(COLUMN_SUBSTANCE_SOURCE), "INN"), ""))
				project.setDivision(trim(csvRecord.get(COLUMN_DIVISION)))
				project.setProduct_type(mapCodeListValue(productTypeMap, trim(csvRecord.get(COLUMN_PRODUCT_TYPE)), ""))
				project.setMarketing_status(mapCodeListValue(marketingStatusMap, trim(csvRecord.get(COLUMN_MARKETING_STATUS)), ""))
				project.setRegistration_status(mapCodeListValue(registrationStatusMap, trim(csvRecord.get(COLUMN_REGISTRATION_STATUS)), ""))
				project.setTherapeutic_area(mapCodeListValue(therapeuticAreaMap, trim(csvRecord.get(COLUMN_THERAPEUTIC_AREA)), ""))
				project.setProduct_complexity_classification(trim(csvRecord.get(COLUMN_PRODUCT_CLASSIFICATION)))
				project.setComplexity_classification(trim(csvRecord.get(COLUMN_COMPLEXITY)))
				project.setDuty_of_care(trim(csvRecord.get(COLUMN_DUTY_OF_CARE)))
				project.setEnabled("Y")
				project.setReason_disabled("")
								
				project.setGlobal_brand_name(toMultiValueDisplayString(csvRecord.get(COLUMN_BRAND_NAMES)))		
				project.setGlobal_brand_name_array(toArray(csvRecord.get(COLUMN_BRAND_NAMES)))
	
				project.setInn(toMultiValueDisplayString(csvRecord.get(COLUMN_INNS)))		
				project.setInn_array(toArray(csvRecord.get(COLUMN_INNS)))
	
				project.setIndication_short_form(toMultiValueDisplayString(csvRecord.get(COLUMN_INDICATION_SHORT_FORMS)))
				project.setIndication_short_form_array(toArray(csvRecord.get(COLUMN_INDICATION_SHORT_FORMS)))
				
				project.setFormulation(toMultiValueDisplayString(csvRecord.get(COLUMN_FORMULATIONS)))
				project.setFormulation_array(toArray(csvRecord.get(COLUMN_FORMULATIONS)))
				
				String logEntry = "Selecting '" + project.getProject_code() + "' for import - therapy '" + (project.getProject_family() ? project.getProject_family() : "") + 
					"', therapeutic area '" + (project.getTherapeutic_area() ? project.getTherapeutic_area() : "") + "'" 
				info(logEntry)
				
				projects.push(project)
				
				// Track unique project families so that we can update the registered_yn flag
				String projectFamily = project.getProject_family()
				if (projectFamily && !projectFamily.isEmpty()) {
				    uniqueProjectFamilies.add(projectFamily)
				}
			}
			
			info("Importing " + projects.size() + " record" + (projects.size() == 1 ? "" : "s"))
			
			overallSuccess = this.theDb.insertProject_CoreTableFromJson(projects)
			
			if (overallSuccess)	{
				info("Checking " + uniqueProjectFamilies.size() + " project " + (projects.size() == 1 ? "family" : "families"))
				for (String projectFamily : uniqueProjectFamilies) {				
					overallSuccess = this.theDb.updateProject_GroupRegistration(projectFamily)
					info("Checked '" + projectFamily + "' registration status")
					if (!overallSuccess) {
						break
					}
				}
			} else {
                error("An unexpected error occured while importing projects")
			}
			
	        info("Project import process " + (overallSuccess ? "completed" : "failed") + " in " + (System.currentTimeMillis() - startMS) + "ms")
		}
		catch (Exception e) {
			error("Unexpected error: " + e.getMessage())
			throw e
		}
		finally {
			this.theDb.closeConnection()
		}
		
		return overallSuccess
    }
	
	
	/**
	 * @return the log file including all info messages, errors, and warnings
	 */
	public String getLog() {
		return logBuffer.toString()
	}

	
	/**
	 * @return the number of detected warnings
	 */
	public int getWarnings() {
		return warnings
	}

	
	/**
	 * @return the number of detected errors
	 */
	public int getErrors() {
		return errors
	}
	
	
	/**
	 * @return trimmed version of the string, with an embedded null check
	 */
	protected String trim(String value) {
		if (value == null) {
			return null	
		}		
		value = value.trim()		
		return value
	}
	
	
	/**
	 * @return trimmed version of the string or defaultValue if null
	 */
	protected trimAndDefault(String value, String defaultValue) {
		if (value == null) {
			return defaultValue
		}
		value = value.trim()
		
		if (value.isEmpty())
			return defaultValue
				
		return value
	}
	
	
	/**
	 * @return Display string where the CSV separator is displayed with the display separator (| -> +) 
	 */
	protected String toMultiValueDisplayString(String value) {
		if (value != null) {		
			return trim(value.replaceAll(SEPARATOR_REGEX, DISPLAY_SEPARATOR))
		} else {
			return null
		}		
	}
	
	
	/**
	 * @return an array of strings where the string is split on the separator and the results are
	 *         trimmed and empty strings removed. 
	 */
	protected ArrayList<String> toArray(String value) {
		ArrayList<String> results = new ArrayList<String>()
		
		if (value) {
			String[] values = value.split(SEPARATOR_REGEX)
			for (String newValue : values) {
				newValue = trim(newValue)
				if (!newValue.isEmpty()) {
					results.add(newValue)	
				}
			}			
		}
		
		return results 		
	}


    /**
     * Validate that required headers are included and note/warn any unexpected headers
     */ 
    private boolean validateHeaders() {
        long startMS = System.currentTimeMillis()
        boolean valid = true
        Map<String, Integer> headerMap = csvParser.getHeaderMap()
        Set<String> expectedHeaders = new TreeSet<String>()
        Set<String> normalizedHeaderMap = new TreeSet<String>()

        // Make map keys all lower case
        for (String key : headerMap.keySet()) {
            normalizedHeaderMap.add(key.toLowerCase())
        }

        // Make sure that all of the required headers exist
        for (String header: REQUIRED_HEADERS) {
            if (!normalizedHeaderMap.contains(header.toLowerCase())) {
                error("Import file missing required column '" + header + "'")
                valid = false
            }
            expectedHeaders.add(header.toLowerCase())
        }

        // Look for extraneous headers (using non-normalized headers for error reporting)
        for (String key : headerMap.keySet()) {
            if (!expectedHeaders.contains(key.toLowerCase())) {
                warning("Ignoring unexpected column '" + key + "'")
            }
        }

        info("Header validation " + (valid ? "completed successfully" : "failed") + " in " + (System.currentTimeMillis() - startMS) + "ms")

        return valid
    }
	

    /**
     * Confirm that mandatory fields are present
     */
    private boolean validateMandatoryFields() {
        long startMS = System.currentTimeMillis()
        boolean valid = true

        if (csvRecords == null) {
            csvRecords = csvParser.getRecords()
        }

        for (CSVRecord csvRecord : csvRecords) {
            for (String requiredField : REQUIRED_FIELDS) {
                String value = csvRecord.get(requiredField)
                if (value == null || value.length() == 0) {
                    error("Required column '" + requiredField + "' is missing in record number " + csvRecord.getRecordNumber())
                    valid = false
                }
            }
        }

        info("Mandatory field validation " + (valid ? "completed successfully" : "failed") + " in " + (System.currentTimeMillis() - startMS) + "ms")

        return valid
    }
	

    /**
     * Confirmed defined field lengths
     */
    private boolean validateFieldLengths() {
        long startMS = System.currentTimeMillis()
        boolean valid = true

        if (csvRecords == null) {
            csvRecords = csvParser.getRecords()
        }

		// Iterate over each CSV record and then each field where we have a configured max length
        for (CSVRecord csvRecord : csvRecords) {
			for (String field : MAX_FIELD_LENGTHS.keySet()) {
				int length = MAX_FIELD_LENGTHS.get(field)
				String value = csvRecord.get(field)
				
				if (containsIgnoreCase(ALLOWS_MULTIPLE_FIELD_VALUES, field)) {
					ArrayList<String> values = toArray(value)					
					for (String individualValue : values) {
						if (individualValue != null && individualValue.length() > length) {						
							if (individualValue.length() > MAX_VALUE_DISPLAY_LENGTH) {
								individualValue = individualValue.substring(0, MAX_VALUE_DISPLAY_LENGTH) + "..."
							}
							error("'" + field + "' column value '" + individualValue + "' exceeds " + length + " per entry character limit in record number " + csvRecord.getRecordNumber())
							valid = false
						}
					}				
				} else {
					if (value != null && value.length() > length) {					
						// Cap how much of the value is logged
						if (value.length() > MAX_VALUE_DISPLAY_LENGTH) {
							value = value.substring(0, MAX_VALUE_DISPLAY_LENGTH) + "..."
						}
						
						error("'" + field + "' column value '" + value + "' exceeds " + length + " character limit in record number " + csvRecord.getRecordNumber())
						valid = false
					}
				}
			} 
        }

        info("Field length validation " + (valid ? "completed successfully" : "failed") + " in " + (System.currentTimeMillis() - startMS) + "ms")

        return valid
    }

	
	/**
	 * Verify the project codes don't already exist and that we don't have dups within the import records
	 */
    private boolean validateProjectCodeUniqueness() {
        long startMS = System.currentTimeMillis()
        boolean valid = true

        Set<String> csvProjectCodes = new HashSet<String>() 

        if (csvRecords == null) {
            csvRecords = csvParser.getRecords()
        }

        for (CSVRecord csvRecord : csvRecords) {
            String projectCode = csvRecord.get(COLUMN_PROJECT_CODE)
            String normalizedProjectCode = projectCode.toLowerCase()  

            // Check for database project codes
            if (existingProjectCodes.contains(normalizedProjectCode)) {
                error("'" + COLUMN_PROJECT_CODE + "' column contains an existing value: '" + projectCode + "' in record number " + csvRecord.getRecordNumber())
                valid = false
            }
            // Next check project codes within the CSV file
            else if (csvProjectCodes.contains(normalizedProjectCode)) {
                error("'" + COLUMN_PROJECT_CODE + "' column contains a duplicate value: '" + projectCode + "' in record number " + csvRecord.getRecordNumber())
                valid = false
            }
            else {
                csvProjectCodes.add(normalizedProjectCode)  
            }
        }
  
        info("Project Code uniqueness validation " + (valid ? "completed successfully" : "failed") + " in " + (System.currentTimeMillis() - startMS) + "ms")

        return valid
    }


	/**
	 * Validates that fields with code list values have valid values -- ignoring case. 
	 */
    private boolean validateCodeListConsistency() {
        long startMS = System.currentTimeMillis()
        boolean valid = true

         if (csvRecords == null) {
            csvRecords = csvParser.getRecords()
        }

        // Begin checks
        for (CSVRecord csvRecord : csvRecords) {
            String value

            // Check COLUMN_PRODUCT_TYPE
            value = trim(csvRecord.get(COLUMN_PRODUCT_TYPE))
            if (value != null && value.length() > 0) {
				if (mapCodeListValue(productTypeMap, value, null) == null) {				
                    error("'" + COLUMN_PRODUCT_TYPE + "' column contains an unexpected value: '" + value + "' in record number " + csvRecord.getRecordNumber())
                    valid = false
                }
            }

            // Check COLUMN_SUBSTANCE_SOURCE
            value = trim(csvRecord.get(COLUMN_SUBSTANCE_SOURCE))
            if (value != null && value.length() > 0) {
				if (mapCodeListValue(substanceTypeMap, value, null) == null) {
                    error("'" + COLUMN_SUBSTANCE_SOURCE + "' column contains an unexpected value: '" + value + "' in record number " + csvRecord.getRecordNumber())
                    valid = false
                }
            }

            // Check COLUMN_MARKETING_STATUS
            value = trim(csvRecord.get(COLUMN_MARKETING_STATUS))
            if (value != null && value.length() > 0) {
				if (mapCodeListValue(marketingStatusMap, value, null) == null) {
                    error("'" + COLUMN_MARKETING_STATUS + "' column contains an unexpected value: '" + value + "' in record number " + csvRecord.getRecordNumber())
                    valid = false
                }
            }

            // Check COLUMN_REGISTRATION_STATUS
            value = trim(csvRecord.get(COLUMN_REGISTRATION_STATUS))
            if (value != null && value.length() > 0) {
				if (mapCodeListValue(registrationStatusMap, value, null) == null) {
                    error("'" + COLUMN_REGISTRATION_STATUS + "' column contains an unexpected value: '" + value + "' in record number " + csvRecord.getRecordNumber())
                    valid = false
                }
            }

            // Check COLUMN_THERAPEUTIC_AREA
            value = trim(csvRecord.get(COLUMN_THERAPEUTIC_AREA))
            if (value != null && value.length() > 0) {
				if (mapCodeListValue(therapeuticAreaMap, value, null) == null) {
                    error("'" + COLUMN_THERAPEUTIC_AREA + "' column contains an unexpected value: '" + value + "' in record number " + csvRecord.getRecordNumber())
                    valid = false
                }
            }
        }

        info("Code list consistency validation " + (valid ? "completed successfully" : "failed") + " in " + (System.currentTimeMillis() - startMS) + "ms")

        return valid
    }
	

	/**
	 * Verify that only columns designated to have multiple values do infact have multiple values.
	 */
    private boolean validateCardinality() {
        long startMS = System.currentTimeMillis()
        boolean valid = true

        if (csvRecords == null) {
            csvRecords = csvParser.getRecords()
        }

        for (CSVRecord csvRecord : csvRecords) {
            for (String header: REQUIRED_HEADERS) {
                if (!containsIgnoreCase(ALLOWS_MULTIPLE_FIELD_VALUES, header)) {
                    String value = csvRecord.get(header)
                    if (value != null && value.indexOf(SEPARATOR) >= 0) {
                        error("'" + header + "' column is single value, but value '" + value + "' includes one or more separators in record number " + csvRecord.getRecordNumber())
                        valid = false
                    }
                } 
            }
        }

        info("Field cardinality validation " + (valid ? "completed successfully" : "failed") + " in " + (System.currentTimeMillis() - startMS) + "ms")

        return valid
    }

	
	/**
	 * Initialize project code lists
	 */
    private void initializeExistingProjectCodes() {
        
        Project_CoreTable coreTable = theDb.getProject_CoreTable(null, true)
        for (Row row : coreTable.getTable_rows()) {
            String value = row.getColVal("project_code")
            if (value != null) {
                existingProjectCodes.add(value.toLowerCase())
            }
        }
    }
	
	
	/**
	 * Load code list values
	 */
    private void initializeCodeLists() {		
		/**
		 * Load all of the data and dump into maps for quick lookups
		 */
        High_Lvl_Prod_Type_LxTable productTypeTable = theDb.getAllProductTypes()
        for (Row row : productTypeTable.getTable_rows()) {
            productTypeMap.put(row.getColVal("prod_type").trim(), row.getColVal("id"))            
        }

        Substance_Type_LxTable substanceTypeTable = theDb.getAllSubstanceTypes()
        for (Row row : substanceTypeTable.getTable_rows()) {
            substanceTypeMap.put(row.getColVal("substance_type").trim(), row.getColVal("id"))
        }

        Mkt_Status_LxTable marketingStatusTable = theDb.getAllMarketingStatuses()
        for (Row row : marketingStatusTable.getTable_rows()) {
            marketingStatusMap.put(row.getColVal("mkt_status").trim(), row.getColVal("id"))
        }

        Reg_Status_LxTable registrationStatusTable = theDb.getAllRegistrationStatuses()
        for (Row row : registrationStatusTable.getTable_rows()) {
            registrationStatusMap.put(row.getColVal("reg_status").trim(), row.getColVal("id"))
        }

        Ther_Area_LxTable therapeuticAreaTable = theDb.getAllTherapeuticAreas()
        for (Row row : therapeuticAreaTable.getTable_rows()) {
            therapeuticAreaMap.put(row.getColVal("ther_area").trim(), row.getColVal("id"))
        }
    }	
	
	
	/**
	 * Map a code list candidate to the actual code list value using a case insensitive lookup 
	 */
	private String mapCodeListValue(Map<String, Integer> codeList, candidateKey, String defaultValue) {
		String mappedKey = defaultValue
		
		for (String checkKey : codeList.keySet()) {
			if (checkKey.compareToIgnoreCase(candidateKey) == 0) {
				mappedKey = checkKey
				break
			}
		}
		
		return mappedKey
			
	}
	

	/**
	 * Append "s" to the supplied string if the count does not equal 1  
	 */
    private pluralize(int count, String base) {
        if (count == 1)
            return base
        else
            return base + "s"
    }

	
	/*
	 * Capture info log information
	 */
    private void info(String entry) {
        logBuffer.append("info: ")
        logBuffer.append(entry)
        logBuffer.append("\n")
    }

	
	/*
	 * Capture warning log information
	 */
    private void warning(String entry) {
        logBuffer.append("warning: ")
        logBuffer.append(entry)
        logBuffer.append("\n")
        warnings = warnings + 1
    }
	

	/*
	 * Capture error log information
	 */
    private void error(String entry) {
        logBuffer.append("error: ")
        logBuffer.append(entry)
        logBuffer.append("\n")
        errors = errors + 1
    }
	

	/**
	 * Case insensitive test of string in array 
	 */
    private boolean containsIgnoreCase(final String[] array, final String v) {
        boolean result = false

        for (String i : array) {
            if (v.compareToIgnoreCase(i) == 0) {
                result = true
                break
            }
        }

        return result
    }
}
