package org.bonitasoft.rest.context

import java.util.logging.Logger

import groovy.json.JsonSlurper;

class RestContextAbbreviateData {
	private static Logger logger = Logger.getLogger("org.bonitasoft.rest.context.RestContextAbbreviateData");
	private String path;
	private List<String> includedDataAttr;
	
	/**************************************************************************
	 * 
	 * @param init_json
	 */
	public RestContextAbbreviateData (String init_json) {
		def slurper = new JsonSlurper()
		def attrs = slurper.parseText(init_json);
		
		path = attrs.path;
		includedDataAttr = new ArrayList<String>();
		attrs.data.each {
			def curr_name;
			includedDataAttr.push(it);
		};
		
		/*logger.info("The path is: "+path);
		for (String n: includedDataAttr) {
			logger.info("Data of Interest: "+n);
		}*/
	}
	
	/**************************************************************************
	 * 
	 * @param path
	 * @return
	 */
	boolean isPathOfInterest (String path) {
		boolean interestingPath = false;
		
		if (this.path.equals(path)) {
			//logger.info ("Found the correct path");
			interestingPath = true;
		}
		
		return interestingPath;
	}
	
	/**************************************************************************
	 * 
	 * @param path
	 * @param name
	 * @return
	 */
	public boolean isDataOfInterest (String name) {
		boolean interestingData = false;
		
		for (String attr_name: includedDataAttr) {
			if (attr_name.toLowerCase().equals(name.toLowerCase())) {
				interestingData = true;
				break;
			}
		}
		
		return interestingData;
	}
}
