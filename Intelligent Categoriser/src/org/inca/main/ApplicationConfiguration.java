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

        if (configLocation.equals("")) {
            logger
                .info("no configuration file provided. attempting to guess one");

            // try via URL first
            logger.info("trying getResource()");
            URL configURL = ApplicationConfiguration.class
                .getResource(_configFilename);

            factory.setConfigurationURL(configURL);

            try {
                _configuration = factory.getConfiguration();

            } catch (ConfigurationException e) {
                logger.error("no config at " + configURL);

                String workDir = System.getProperty("user.dir");
                String homeDir = System.getProperty("user.home");
                String fs = System.getProperty("file.separator");

                String[] locations = { "file://" + workDir + fs + _configFilename,
                        "file://" + homeDir + fs + _configFilename };

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
            }
        } else {
            URL configURL = getClass().getResource(_configFilename);
            factory.setConfigurationURL(configURL);

            try {
                _configuration = factory.getConfiguration();
            } catch (ConfigurationException e) {
                logger.fatal("error loading configuration from " + configURL);
            }
        }
    }

    public static Configuration getConfiguration() {
        return _configuration;
    }
}