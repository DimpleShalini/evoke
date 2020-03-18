package com.commoninf.json;

import java.util.ArrayList;

/******************************************************************************
 * This class is used to de-serialize the JSON that is used to add/import
 * projects to the database.
 */
public class ProjectJson {
	private String project_code;
	private String global_brand_name;
	private String project_family;
	private String compound_code;
	private String inn;
	private String substance_name_sources;
	private String division;
	private String product_type;
	private String marketing_status;
	private String registration_status;
	private String indication_short_form;
	private String formulation;
	private String therapeutic_area;
	private String product_complexity_classification;
	private String complexity_classification;
	private String duty_of_care;
	private String enabled;
	private String reason_disabled;
	private ArrayList<String> global_brand_name_array;
	private ArrayList<String> inn_array;
	private ArrayList<String> indication_short_form_array;
	private ArrayList<String> formulation_array;
	
	/**************************************************************************
	 * 
	 */
	public ProjectJson () {
		project_code = "";
		global_brand_name = "";
		project_family = "";
		compound_code = "";
		inn = "";
		substance_name_sources = "";
		division = "";
		product_type = "";
		marketing_status = "";
		registration_status = "";
		indication_short_form = "";
		formulation = "";
		therapeutic_area = "";
		product_complexity_classification = "";
		complexity_classification = "";
		duty_of_care = "";
		enabled = "";
		reason_disabled = "";
		global_brand_name_array = new ArrayList<String>();
		inn_array = new ArrayList<String>();
		indication_short_form_array = new ArrayList<String>();
		formulation_array = new ArrayList<String>();
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getProject_code() {
		return project_code.trim();
	}

	/**************************************************************************
	 * 
	 * @param project_code
	 */
	public void setProject_code(String project_code) {
		this.project_code = project_code;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getGlobal_brand_name() {
		return global_brand_name.trim();
	}

	/**************************************************************************
	 * 
	 * @param global_brand_name
	 */
	public void setGlobal_brand_name(String global_brand_name) {
		this.global_brand_name = global_brand_name;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getProject_family() {
		return project_family.trim();
	}

	/**************************************************************************
	 * 
	 * @param project_family
	 */
	public void setProject_family(String project_family) {
		this.project_family = project_family.trim();
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getCompound_code() {
		return compound_code.trim();
	}

	/**************************************************************************
	 * 
	 * @param compound_code
	 */
	public void setCompound_code(String compound_code) {
		this.compound_code = compound_code;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getInn() {
		return inn.trim();
	}

	/**************************************************************************
	 * 
	 * @param inn
	 */
	public void setInn(String inn) {
		this.inn = inn;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getSubstance_name_sources() {
		return substance_name_sources.trim();
	}

	/**************************************************************************
	 * 
	 * @param substance_name_sources
	 */
	public void setSubstance_name_sources(String substance_name_sources) {
		this.substance_name_sources = substance_name_sources;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getDivision() {
		return division.trim();
	}

	/**************************************************************************
	 * 
	 * @param division
	 */
	public void setDivision(String division) {
		this.division = division;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getProduct_type() {
		return product_type.trim();
	}

	/**************************************************************************
	 * 
	 * @param product_type
	 */
	public void setProduct_type(String product_type) {
		this.product_type = product_type;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getMarketing_status() {
		return marketing_status.trim();
	}

	/**************************************************************************
	 * 
	 * @param marketing_status
	 */
	public void setMarketing_status(String marketing_status) {
		this.marketing_status = marketing_status;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getRegistration_status() {
		return registration_status.trim();
	}

	/**************************************************************************
	 * 
	 * @param registration_status
	 */
	public void setRegistration_status(String registration_status) {
		this.registration_status = registration_status;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getIndication_short_form() {
		return indication_short_form.trim();
	}

	/**************************************************************************
	 * 
	 * @param indication_short_form
	 */
	public void setIndication_short_form(String indication_short_form) {
		this.indication_short_form = indication_short_form;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getFormulation() {
		return formulation.trim();
	}

	/**************************************************************************
	 * 
	 * @param formulation
	 */
	public void setFormulation(String formulation) {
		this.formulation = formulation;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getTherapeutic_area() {
		return therapeutic_area.trim();
	}

	/**************************************************************************
	 * 
	 * @param therapeutic_area
	 */
	public void setTherapeutic_area(String therapeutic_area) {
		this.therapeutic_area = therapeutic_area;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getProduct_complexity_classification() {
		return product_complexity_classification.trim();
	}

	/**************************************************************************
	 * 
	 * @param product_complexity_classification
	 */
	public void setProduct_complexity_classification(String product_complexity_classification) {
		this.product_complexity_classification = product_complexity_classification;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getComplexity_classification() {
		return complexity_classification.trim();
	}

	/**************************************************************************
	 * 
	 * @param complexity_classification
	 */
	public void setComplexity_classification(String complexity_classification) {
		this.complexity_classification = complexity_classification;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getDuty_of_care() {
		return duty_of_care.trim();
	}

	/**************************************************************************
	 * 
	 * @param duty_of_care
	 */
	public void setDuty_of_care(String duty_of_care) {
		this.duty_of_care = duty_of_care;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getEnabled() {
		return enabled.trim();
	}

	/**************************************************************************
	 * 
	 * @param enabled
	 */
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public String getReason_disabled() {
		return reason_disabled.trim();
	}

	/**************************************************************************
	 * 
	 * @param reason_disabled
	 */
	public void setReason_disabled(String reason_disabled) {
		this.reason_disabled = reason_disabled;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public ArrayList<String> getGlobal_brand_name_array() {
		// No need to trim the strings in the array because these values are
		// derived during import on the client.  The code running on the client
		// is expected to trim leading and trailing white space.
		return global_brand_name_array;
	}

	/**************************************************************************
	 * 
	 * @param global_brand_name_array
	 */
	public void setGlobal_brand_name_array(ArrayList<String> global_brand_name_array) {
		this.global_brand_name_array = global_brand_name_array;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public ArrayList<String> getInn_array() {
		// No need to trim the strings in the array because these values are
		// derived during import on the client.  The code running on the client
		// is expected to trim leading and trailing white space.
		return inn_array;
	}

	/**************************************************************************
	 * 
	 * @param inn_array
	 */
	public void setInn_array(ArrayList<String> inn_array) {
		this.inn_array = inn_array;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public ArrayList<String> getIndication_short_form_array() {
		// No need to trim the strings in the array because these values are
		// derived during import on the client.  The code running on the client
		// is expected to trim leading and trailing white space.
		return indication_short_form_array;
	}

	/**************************************************************************
	 * 
	 * @param indication_short_form_array
	 */
	public void setIndication_short_form_array(ArrayList<String> indication_short_form_array) {
		this.indication_short_form_array = indication_short_form_array;
	}

	/**************************************************************************
	 * 
	 * @return
	 */
	public ArrayList<String> getFormulation_array() {
		// No need to trim the strings in the array because these values are
		// derived during import on the client.  The code running on the client
		// is expected to trim leading and trailing white space.
		return formulation_array;
	}

	/**************************************************************************
	 * 
	 * @param formulation_array
	 */
	public void setFormulation_array(ArrayList<String> formulation_array) {
		this.formulation_array = formulation_array;
	}

}
