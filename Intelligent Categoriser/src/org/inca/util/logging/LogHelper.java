/*
 * Created on Dec 1, 2004 7:53:13 PM
 */
package org.inca.util.logging;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author achim
 */

public class LogHelper {
    private static LogHelper _instance = null;
    
    final private static String _configFilename = "log4jconfig.xml";
    
    public static LogHelper getInstance() {
        return _instance;
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

        if (null == configURL) {
            System.out.println("no log4j config foundat at default location.");
            String workDir = System.getProperty("user.dir");
            String homeDir = System.getProperty("user.home");
            String fs = System.getProperty("file.separator");

            String[] locations = { "file://" + workDir + fs + _configFilename,
                    "file://" + homeDir + fs + _configFilename };
            
            for (int i = 0; i < locations.length; i++) {
                String location = locations[i];
                System.out.println("trying " + location);
                
                try {
                    configURL = new URL(location);
                } catch (MalformedURLException e) {
                    continue;
                }

                if (null == configURL) {
                    continue;
                }
            }
            
            if (null == configURL) {
                System.err.println("failed to initialize log4j.");
                System.exit(0);
            }
        }
        System.out.println("using " + configURL.toString() +" as log4j config file.");
        
        // ensure that we use the default xml parser shipped with jdk 1.4
        // common pitfall: using gnujaxp.jar will break when loading an xml 
        // since the dtd referenced therein cannot be resolved
        
        System.setProperty("org.xml.sax.driver", "org.apache.crimson.parser.XMLReaderImpl");
        DOMConfigurator.configure(configURL);
    }        
}