/*
 * Created on Feb 15, 2005 2:47:02 PM
 */
package org.inca.odp.ie;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.inca.main.ApplicationConfiguration;
import org.inca.odp.ie.tagger.FrequencyTagFilter;
import org.inca.odp.ie.tagger.TagFilter;
import org.inca.odp.ie.tagger.TaggedLemma;
import org.inca.odp.ie.tagger.Tagger;
import org.inca.odp.ie.tagger.TaggerException;
import org.inca.odp.ie.tagger.TreeTagger;
import org.inca.util.CountingHashtable;
import org.inca.util.logging.LogHelper;
import org.inca.util.net.ConnectionFailedException;
import org.inca.util.net.ResourceNotFoundException;

/**
 * @author achim
 */
public class Extractor {
    static Configuration config;
    static Logger logger;

    private Connection _connection;

    private void dbConnect() {
        String DB_URL = config.getString("extractor.dbUrl");
        String DB_USER = config.getString("extractor.dbUser");
        String DB_PASSWD = config.getString("extractor.dbPasswd");
        String DB_DRIVER_CLASS = config.getString("extractor.dbDriverClass");

        try {
            Class.forName(DB_DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            logger.fatal("error loading sql driver " + DB_DRIVER_CLASS);
        }
        try {
            _connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWD);
        } catch (SQLException e1) {
            logger.fatal("error connection to database.");
        } 
    }
    
    private Vector getLinks(String name) throws SQLException {
        Vector result = new Vector();
        // get links for category name
        String selectStmt = "SELECT url FROM links,cat2link "
            + "INNER JOIN categories ON cat2link.catId=categories.id "
            + "WHERE categories.name=? AND links.id=cat2link.linkId";

        PreparedStatement stmt = _connection.prepareStatement(selectStmt);
        stmt.setEscapeProcessing(true);     
        stmt.setString(1, name);
        stmt.execute();
        
        ResultSet rs = stmt.getResultSet();
        
        while ( rs.next() ) {
            result.add(rs.getString("url"));
        }
        
        return result;
    }
    
    public void go() throws SQLException {
        dbConnect();
        
        // get links for each category
        Statement stmt  = _connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(name) FROM categories");
        
        long numCat = 0;
        if ( rs.next() ) {
            logger.info("processing " + rs.getLong(1) + " categories.");
        } else {
            throw new SQLException("failed to count categories.");
        }
        
        long cat = 0;
        numCat = 15;
        
        int ROWS_PER_QUERY = config.getInt("extractor.rowsPerQuery");
        
        while (cat < numCat) {
            rs = stmt.executeQuery("SELECT name from categories LIMIT " + cat + "," + ROWS_PER_QUERY);
            
            long cc = cat + 1;
            while ( rs.next() ) {
                logger.info("c (" + cc++ + "): " + rs.getString("name"));
                Vector links = getLinks(rs.getString("name"));
                
                if (0 == links.size() ) {
                    continue;
                }

                logger.info("processing " + links.size() + " links.");
                long lc = 1;
	            for (Iterator iter = links.iterator(); iter.hasNext(); ) {
	                URL url = null;
	                String link = (String) iter.next();
	                logger.info("l (" + lc++ + "): " + link);

                    try {
                        url = new URL(link);
                    } catch (MalformedURLException e) {
                        logger.error("malformed url :"   + link);
                        continue;
                    }
                    
                    PlaintextConverter pc = new PlaintextConverter(url);
                    Tagger tagger = null;
                    try {
                        tagger = new TreeTagger(pc.getPlaintext());
                    } catch (ConnectionFailedException e) {
                        logger.error(e.getMessage() );
                        continue;
                    } catch (ResourceNotFoundException e1) {
                        logger.error(e1.getMessage() );
                        continue;
                    } catch (IOException e) {
                        logger.error("error getting plaintext.");
                        continue;
                    }
                    
                    CountingHashtable tags = null;
                    try {
                        tags = tagger.getTags();
                    } catch (TaggerException e) {
                        logger.error(e.getMessage() );
                        continue;
                    } catch (IOException e1) {
                        logger.error(e1.getMessage());
                        continue;
                    }
                    
                    TagFilter tagFilter = new FrequencyTagFilter(tags);
                    List filteredTags = tagFilter.getFilteredTags();
                    
                    if (logger.isDebugEnabled() ) {
	                    for (Iterator i = filteredTags.iterator(); i.hasNext(); ) {
	                        Map.Entry e = (Entry) i.next();
	                        TaggedLemma tl = (TaggedLemma) e.getKey();
	
	                        System.out.println(tl.getLemma() + ": " + tl.getTag() + " (" + e.getValue() + ")");
	                    }
                    }
	            }
            }
            
            cat += ROWS_PER_QUERY;
        }
        
        _connection.close();
        System.out.println("done.");
    }

    public static void main(String[] args) throws SQLException {
        ApplicationConfiguration.initInstance();
        config = ApplicationConfiguration.getConfiguration();
        logger = LogHelper.getLogger();
        new Extractor().go();
    }
}
