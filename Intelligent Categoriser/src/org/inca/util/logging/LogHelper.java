/*
 * Created on Dec 1, 2004 7:53:13 PM
 */
package org.inca.util.logging;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author achim
 */

public class LogHelper {
    private static LogHelper _instance = null;
    private static Logger _logger = null;
    
    final private static String _configFilename = "log4jconfig.xml";
    
    public static LogHelper getInstance() {
        return _instance;
    }

    public static Logger getLogger() {
        return _logger;
    }
    
    public static void initInstance() {
        if (null == _instance) {
            _instance = new LogHelper(_configFilename);
        }
    }
    
    public static void initInstance(String configLocation) {
        if (null == _instance) {
            _instance = new LogHelper(configLocation);
        }
    }
    
    protected LogHelper(String configLocation) {
        URL configURL = LogHelper.class.getResource(configLocation);
    
        System.out.println("using " + configURL.toString() +" as log4j config file.");
        
        // ensure that we use the default xml parser shipped with jdk 1.4
        // common pitfall: using gnujaxp.jar
        
        System.setProperty("org.xml.sax.driver", "org.apache.crimson.parser.XMLReaderImpl");
        DOMConfigurator.configure(configURL);

        _logger = Logger.getLogger(LogHelper.class);
    }        
}