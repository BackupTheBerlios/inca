package org.inca.main;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.log4j.Logger;
import org.inca.util.logging.LogHelper;

/**
 * @author achim
 */

public class ApplicationConfiguration {
    private static ApplicationConfiguration _instance = null;

    final private static String _configFilename = "config.xml";

    private static Configuration _configuration = null;
    private static Logger logger = Logger.getLogger(ApplicationConfiguration.class);

    public static ApplicationConfiguration getInstance() {
        if (null == _instance) {
            throw new RuntimeException(
                "ApplicationConfiguration has not been initalized.");
        }
        return _instance;
    }

    public static void initInstance() {
        if (null == _instance) {
            _instance = new ApplicationConfiguration(_configFilename);
        }
    }
    public static void initInstance(String configLocation) {
        if (null == _instance) {
            _instance = new ApplicationConfiguration(configLocation);
        }
    }

    protected ApplicationConfiguration(String configLocation) {
        System.out.println("setting up logging.");
        LogHelper.initInstance();

        ConfigurationFactory factory = new ConfigurationFactory();
        
        URL configURL = getClass().getResource(configLocation);
        
        if (null == configURL) {
            factory.setConfigurationFileName(configLocation);

            try {
                _configuration = factory.getConfiguration();
            } catch (ConfigurationException e) {
                logger.error("error loading configuration from " + configURL);
                autoSetup(factory);
            }
        } else {
            factory.setConfigurationURL(configURL);
            try {
                _configuration = factory.getConfiguration();
            } catch (ConfigurationException e) {
                logger.error("error loading configuration from " + configURL);
                autoSetup(factory);
            }
        }
    }
    
    private void autoSetup(ConfigurationFactory factory) {
        URL configURL = getClass().getResource(_configFilename);

	    if (null == configURL) {
	        String workDir = System.getProperty("user.dir");
	        String homeDir = System.getProperty("user.home");
	        String fs = System.getProperty("file.separator");
	
	        String[] locations = { workDir + fs + _configFilename,
	                			   homeDir + fs + _configFilename };
	
	        for (int i = 0; i < locations.length; i++) {
	            String location = locations[i];
	            logger.info("trying " + location);

	            factory.setConfigurationFileName(location);
	            try {
	                _configuration = factory.getConfiguration();
	                return;
	            } catch (ConfigurationException f) {
	                logger.error("no config at " + location);
	                continue;
	            }
	        }
	        
	        logger.fatal("failed to load configuration.");
	    } else {
		    factory.setConfigurationURL(configURL);

		    try {
		        _configuration = factory.getConfiguration();	
		    } catch (ConfigurationException e) {
		        logger.fatal("no config at " + configURL);
		    }
	    }
    }

    public static Configuration getConfiguration() {
        return _configuration;
    }
}