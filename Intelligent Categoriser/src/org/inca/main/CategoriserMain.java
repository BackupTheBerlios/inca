/*
 * Created on Nov 30, 2004
 */
package org.inca.main;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/**
 * @author achim
 */
public class CategoriserMain {
    static Logger logger = Logger.getLogger(CategoriserMain.class);
    static Configuration config;

    public static void main(String[] args) {
        ApplicationConfiguration.initInstance();
        config  = ApplicationConfiguration.getConfiguration();        
    }  
}