package com.commoninf.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import com.commoninf.database.Database;
import com.commoninf.database.TableBase;
import com.commoninf.logger.CiiLogger;

public class DropDown_LxTable extends TableBase {
	private static final CiiLogger logger = new CiiLogger("com.commoninf.data.DropDown_LxTable");
	
	public static String[] dropDownTables = {
		"topic_category_lx",
		"topic_cds_current_status_lx",
		"topic_rmp_current_status_lx",
		"topic_ib_current_status_lx",
		"topic_dspp_current_status_lx",
		"topic_dsur_current_status_lx",
		"topic_origin_lx",
		"topic_psur_current_status_lx",
		"topic_signal_impact_lx",
		"topic_signal_source_lx",
		"topic_assessment_assessor_lx",
		"action_item_source_lx",
		"ha_request_type_lx",
		"action_item_status_lx",
		"topic_eval_complexity_lx",
		"topic_psur_actions_lx",
		"topic_recent_aggr_lx",
		"topic_special_activities_lx",
		"topic_key_risk_lx",
		"topic_origin_detail_external_lx",
		"topic_origin_detail_internal_lx",
		"topic_foa_eval_complexity_lx",
		"topic_foa_focus_of_anal_lx",
		"topic_dsrc_pre_clin_study_type_lx",
		"topic_dsrc_safety_rep_type_lx",
		"topic_dsrc_eval_period_interval_lx",
		"topic_dsrc_eval_period_cumulative_lx",
		"topic_dsrc_ext_safety_db_lx",
		"topic_cds_section_lx",
		"topic_tme_type_lx"
	};
	
	/**************************************************************************
	 * NOTE:  IF THIS CONSTRUCTOR IS CALLED THEN THE METHOD TableBase.setDb
	 * MUST BE CALLED TO DEFINE THE DATABASE CONNECTION BEFORE ATTEMPTING TO
	 * Get/Set ANYTHING IN THE DATABASE!!!!!  The typical use case for this
	 * constructor would be when defining foreign keys in the Row.COLS
	 * definition
	 */
	public DropDown_LxTable (String tableName) {
		super (null, tableName, DropDown_LxRow.COLS, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		DropDown_LxRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param db
	 * @throws ClassNotFoundException 
	 */
	public DropDown_LxTable (Database db, String tableName) {
		super (db, tableName, DropDown_LxRow.COLS, true, false);
		
		// Update the static Row instance of COLS because the the constructor for
		// TableBase may have added some of the standard rows for id,
		// created by/when, updated by/when and isvalid.
		DropDown_LxRow.COLS = this.col_def;
	}
	
	/**************************************************************************
	 * 
	 * @param rs
	 */
	@Override
	public void readRow(ResultSet rs) throws SQLException {
		table_rows.add(new DropDown_LxRow(rs));
	}
	
	public void getDropDownValues() throws SQLException {
		String [] isvalid_yn = { "Y" };
		
		PreparedStatement ps = null ;
		try {
			ps = getQueryForSpecificRows("isvalid_yn", isvalid_yn);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				readRow(rs);
			}
			rs.close();
		} catch (SQLException e) {
			logger.error("SQL Exception thrown in getDropDownValues");
			logSqlException (e.getMessage());
		} finally {
			if (ps != null) {
				ps.close();
			}
		}		
	}
}
