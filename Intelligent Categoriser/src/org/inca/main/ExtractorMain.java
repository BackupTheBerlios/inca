/*
 * Created on Mar 15, 2005 12:41:58 PM
 */
package org.inca.main;

import java.sql.SQLException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.inca.odp.ie.Extractor;
import org.inca.util.db.DatabaseConnection;

/**
 * @author achim
 */
public class ExtractorMain {
    static Configuration config;
    static Logger logger = Logger.getLogger(ExtractorMain.class);

    public static void main(String[] args) throws SQLException {
        ApplicationConfiguration.initInstance();
        config = ApplicationConfiguration.getConfiguration();

        String DB_URL = config.getString("extractor.dbUrl");
        String DB_USER = config.getString("extractor.dbUser");
        String DB_PASSWD = config.getString("extractor.dbPasswd");
        String DB_DRIVER_CLASS = config.getString("extractor.dbDriverClass");
        
        try {
            DatabaseConnection.initDatabaseConnection(DB_DRIVER_CLASS, DB_URL, DB_USER, DB_PASSWD);
        } catch (SQLException e) { 
            logger.fatal("error connection to database.");
        } catch (ClassNotFoundException e) {
            logger.fatal("error loading sql driver " + DB_DRIVER_CLASS);
        }
        
        Extractor extractor = null;
        
        if (null == args || args.length == 0) {
            extractor = new Extractor(new String[] { "/Arts/%", "/Science/%" });
        } else if (args[0].compareToIgnoreCase("--resume") == 0) {
            extractor = new Extractor(new String[] { "/Arts/%", "/Science/%" });
        }
        extractor.processCategories();
    }
}
