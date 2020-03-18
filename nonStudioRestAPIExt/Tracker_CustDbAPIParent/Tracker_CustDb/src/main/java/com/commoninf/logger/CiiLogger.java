package com.commoninf.logger;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CiiLogger {
	String classname = "";
	static boolean debugEnabled = false;
	private Logger logger = null;
	
	/**************************************************************************
	 * 
	 * @param classname
	 */
	public CiiLogger (String classname) {
		this.classname = classname;
		if (UseSLF4JGroovyLogger.ON) {
			logger = (Logger)LoggerFactory.getLogger(classname);
		}
	}
	
	/**************************************************************************
	 * 
	 * @param debug_enabled
	 */
	public static void setDebugEnabled (boolean debug_enabled) {
		debugEnabled = debug_enabled;
	}
	
	/**************************************************************************
	 * 
	 * @return
	 */
	public static boolean isDebugEnabled () {
		return debugEnabled;
	}
		
	/**************************************************************************
	 * 
	 * @param type
	 * @param str
	 */
	private void print_txt (String type, String str) {
		Timestamp sys_time = new Timestamp(System.currentTimeMillis());
		String full_str = sys_time+" [" + type + "]: "+ classname + " " + str;
		
		if (UseSLF4JGroovyLogger.ON) {
			switch (type) {
			case "INFO":
				logger.info(str);
				break;
			case "DEBUG":
				logger.debug(str);
				break;
			case "WARN":
				logger.warn(str);
				break;
			case "TRACE":
				logger.trace(str);
				break;
			case "ERROR":
			default:
				logger.error(str);
				break;
			}
		}
		else {
			System.out.println(full_str);
		}
	}
	
	/**************************************************************************
	 * 
	 * @param str
	 */
	public void debug (String str) {
		print_txt ("DEBUG", str);
	}
	
	/**************************************************************************
	 * 
	 * @param str
	 */
	public void error (String str) {
		print_txt ("ERROR", str);
	}
	
	/**************************************************************************
	 * 
	 * @param str
	 */
	public void info (String str) {
		print_txt ("INFO", str);
	}
	
	/**************************************************************************
	 * 
	 * @param str
	 */
	public void trace (String str) {
		print_txt ("TRACE", str);
	}
	
	/**************************************************************************
	 * 
	 * @param str
	 */
	public void warn (String str) {
		print_txt ("WARN", str);
	}
}
