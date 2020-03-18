package com.commoninf.utils;

import java.util.List;

public class DropDownValue {
	private List<String> values;
	private List<String> defaults;
	private List<String> add_cfg_values;
	private List<Integer> cl_ids;
	private List<Integer> display_orders;

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public List<String> getDefaults() {
		return defaults;
	}

	public void setDefaults(List<String> defaults) {
		this.defaults = defaults;
	}

	public List<String> getAdd_cfg_values() {
		return add_cfg_values;
	}

	public void setAdd_cfg_values(List<String> add_cfg_values) {
		this.add_cfg_values = add_cfg_values;
	}
	
	public List<Integer> getCl_ids() {
		return cl_ids;
	}

	public void setCl_ids(List<Integer> cl_ids) {
		this.cl_ids = cl_ids;
	}
	
	public List<Integer> getDisplay_orders() {
		return display_orders;
	}

	public void setDisplay_orders(List<Integer> display_orders) {
		this.display_orders = display_orders;
	}
}