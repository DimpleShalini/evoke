package com.commoninf.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

import javax.naming.InitialContext;

import com.commoninf.logger.CiiLogger;
import com.commoninf.utils.EncryptionUtils;


public class PropertyUtils {
    private static final String APP_CONFIG_DIR_ENV_VAR = "appConfigDir";
    
    private static final CiiLogger logger = new CiiLogger (PropertyUtils.class.getName());
    
    public static void loadProperties(Properties props, String fileName,
            boolean containsSecrets, boolean exitOnError) {
    	loadProperties(props, fileName, containsSecrets, exitOnError, true, false);
    }

    /**
     * Load settings from a properties file.
	 * Don't write the settings to the log file if containsSecrets is true.
	 * 
	 * Load the first properties file found in the following locations:
	 *  1) application config dir (as specified in an environment variable)
	 *  2) class path
     * 
     * @param props
     *            properties object
     * @param fileName
     *            name of the properties file
     * @param containsSecrets
     *            does the properties file contains secrets
	 * @param exitOnError
	 *            should the application exit if there is an error
	 * @param useCache
	 * 			  if false, avoid using the properties caching method and
	 * 			  reread them 
	 * @param okIfNotFound
	 *            It is not an error if not found.  If false, and exitOnError is false, throw an exception.
	 *            
     */
    public static void loadProperties(Properties props, String fileName,
            boolean containsSecrets, boolean exitOnError, boolean useCache, boolean okIfNotFound) {
        InputStream inStream = null;
        String appConfigDir = getAppConfigDir();
        
        // If we have an <application config dir> setting, check there first for the file
        if (!"".equals(appConfigDir)) {
            try {
                String filePath = appConfigDir + File.separator + fileName;
                inStream = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                inStream = null;
            }
        }
        
        try {
            // If we haven't been able to find/open the file, look for it in the classpath
            if (inStream == null) {
                if (useCache) {
                    inStream = PropertyUtils.class.getResourceAsStream(fileName);
                } else {
                    ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
                    URL resURL = ctxLoader.getResource(fileName);
                    if (resURL == null) {
                        // believed to only happen when running unit tests
                        inStream = PropertyUtils.class.getResourceAsStream(fileName);
                    } else {
                        URLConnection resConn = resURL.openConnection();
                        resConn.setUseCaches(false);
                        inStream = resConn.getInputStream();
                    }
                }
            }

            if (inStream != null) {
                props.load(inStream);
                inStream.close();
                inStream = null;
                logger.trace("Loaded properties from '{"+fileName+"}'");
                if (!containsSecrets && CiiLogger.isDebugEnabled()) {
                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    props.list(new PrintStream(outStream, true));
                    logger.debug("\n" + outStream.toString());
                }
            } else {
                String emsg = "Properties file " + fileName + " not found";
                if (exitOnError || !okIfNotFound) {
                    logger.error(emsg);
                } else {
                    logger.info(emsg);
                }
                if (exitOnError) {
                    System.exit(-1);
                }
                if (!okIfNotFound) {
                    throw new RuntimeException(emsg);
                }
            }
        } catch (IOException e) {
            logger.error(String.format("Unable to load the '%s' file", fileName));
            logger.error(e.getMessage());
            if (exitOnError) {
                System.exit(-1);
            }
            throw new RuntimeException("Unable to load the " + fileName + " file:" + e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception e) {
                    logger.warn("Exception when attempting to close inputStream for properties file: " + fileName);
                }
            }
        }
    }
    
    public static Properties ensureSecretsAreSecure(String secretsFileName, String encryptionKey, boolean exitOnError) throws IOException { 
        Properties secrets = new Properties();        
        PropertyUtils.loadProperties(secrets, secretsFileName, true, exitOnError, false, false); 
        
        boolean isSecretsEncrypted = "True".equalsIgnoreCase(secrets.getProperty("encrypted"));
        if (!isSecretsEncrypted) {
            File secretsFile = locateFileInConfigDirOrClasspath(secretsFileName, exitOnError);
            FileWriter fw = new FileWriter(secretsFile);    
            
            for (String secretPropertyName : secrets.stringPropertyNames()) {
                String secretPropertyValue = secrets.getProperty(secretPropertyName);
                String encryptedSecretPropertyValue = null;
                if (secretPropertyName.equals("encrypted")) {
                	encryptedSecretPropertyValue = "True";
                } else  {
                	encryptedSecretPropertyValue = EncryptionUtils.encrypt(secretPropertyValue, encryptionKey);
                }
                fw.write(secretPropertyName + "=" + encryptedSecretPropertyValue);
                    fw.write("\n");
                secrets.setProperty(secretPropertyName, encryptedSecretPropertyValue);
                }                
            fw.close();
            
            if (CiiLogger.isDebugEnabled()) {
                logger.debug("");
                logger.debug("***************************************************************");
                logger.debug(secretsFileName + " has been rewritten with encrypted properties");
                logger.debug("***************************************************************");
                logger.debug("");
            }
        }
        return secrets;
    }
    
    public static void decryptSecretProperties(Properties secrets, String encryptionKey)  { 
        for (String secretPropertyName : secrets.stringPropertyNames()) {
            if (!secretPropertyName.equals("encrypted")) {
                String secretPropertyValue = secrets.getProperty(secretPropertyName);
                secrets.setProperty(secretPropertyName, EncryptionUtils.decrypt(secretPropertyValue, encryptionKey));
            }            
        }
    }
    
    /**
     * Try to get the appConfigDir setting from the application environment
     * 
     * @return the application config dir setting, or "" if the the environment variable is not set
     */
    private static String getAppConfigDir() {
        String appConfigDir = "";
        try {
            appConfigDir = (String) (new InitialContext()).lookup("java:comp/env/" + APP_CONFIG_DIR_ENV_VAR);
            logger.trace(APP_CONFIG_DIR_ENV_VAR + " variable set to " + appConfigDir);
        } catch (Exception e) {
            // If there is an exception, return "" to indicate that the
            // application config dir setting is not available
            logger.trace(APP_CONFIG_DIR_ENV_VAR + " setting is not available");
        }
        return appConfigDir;
    }
    
    private static File locateFileInConfigDirOrClasspath(String fileName, boolean exitOnError) {
        File returnVal = null;
        
        String appConfigDir = getAppConfigDir();

        // If we have an <application config dir> setting, check there first for the file
        if (!"".equals(appConfigDir)) {
            String filePath = appConfigDir + File.separator + fileName;
            File f = new File(filePath);
            if (f.exists()) {
                returnVal = f;
            }
        }

        // If we haven't been able to find/open the file, look for it in the classpath
        if (returnVal == null) {
        	String path;
			try {
				path = PropertyUtils.class.getResource(fileName).getPath();
				path = URLDecoder.decode(path, "UTF-8");
	            returnVal = new File(path);
			} catch (UnsupportedEncodingException e) {
				logger.error("Failure while decoding path for: " + fileName);
				path = null;
			}
        }

        if (returnVal == null || !returnVal.exists()) {
            logger.error("Properties file '{"+fileName+"}' not found");
            if (exitOnError) {
                System.exit(-1);
            }               
            throw new RuntimeException("Properties file " + fileName + " not found");
        }
        
        return returnVal;
    }
}
