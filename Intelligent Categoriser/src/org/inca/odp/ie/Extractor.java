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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.inca.util.net.ConnectionFailedException;
import org.inca.util.net.ResourceNotFoundException;

/**
 * @author achim
 */
public class Extractor {
    static Configuration config;
    static Logger logger = Logger.getLogger(Extractor.class);

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
    
    private long insertAndGetId(String table, String row, String value) throws SQLException{
        long id = 0;
        String insertStmt = "INSERT IGNORE INTO " + table + " (" + row + ") VALUES (?);";
        String selectStmt = "SELECT id FROM " + table + " WHERE " + row + "=?;";
       
        PreparedStatement stmt = _connection.prepareStatement(insertStmt, PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setEscapeProcessing(true);     
        stmt.setString(1, value);
        logger.info(stmt.toString());
        stmt.execute();
        logger.info(stmt.toString());

        ResultSet rs = stmt.getGeneratedKeys();
               
        if ( rs.next() ) {
            id = rs.getLong(1);
        } else {
            stmt = _connection.prepareStatement(selectStmt);
            stmt.setEscapeProcessing(true);
            stmt.setString(1, value);
            stmt.execute();
            
            rs = stmt.getResultSet();
            
            if ( rs.next() ) {
                id = rs.getLong("id");
            } else {
                throw new SQLException("failed to get id from ResultSet");
            }
        }
        return id;
    }
    
    private static String getPlaceholder(int length, String placeholder, String delim) {
        String result = "";
        for (int i = 0; i < length; i++) {
            result += placeholder;
            if (i < length - 1) {
            	result += delim;
            }
        }
        
        return result;
    }
    
    private static String join(String delim, String[] s) {
        String result = "";
        for (int i = 0; i < s.length; i++) {
            result += s[i];
            if ( i < s.length - 1) {
                result += delim;
            }
        }
        
        return result;
    }
    
    private long insertAndGetId(String table, String[] rows, Object[] values) throws SQLException{
        long id = 0;
        String insertStmt = "INSERT IGNORE INTO " + table + " (";
        insertStmt += join(",", rows);
        insertStmt += ") VALUES (";
        insertStmt += getPlaceholder(values.length, "?", ",");
        insertStmt += ");";
        
        String selectStmt = "SELECT id FROM " + table + " WHERE ";       
        selectStmt += join("=? and ", rows) + "=?";

        PreparedStatement stmt = _connection.prepareStatement(insertStmt, PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setEscapeProcessing(true);

        for (int i = 0; i < values.length; i++) {
            stmt.setObject(i + 1, values[i]);
        }

        stmt.execute();

        ResultSet rs = stmt.getGeneratedKeys();
               
        if ( rs.next() ) {
            id = rs.getLong(1);
        } else {
            stmt = _connection.prepareStatement(selectStmt);
            stmt.setEscapeProcessing(true);
            for (int i = 0; i < values.length; i++) {
                stmt.setObject(i + 1, values[i]);    
            }
            
            stmt.execute();           
            rs = stmt.getResultSet();
            
            if ( rs.next() ) {
                id = rs.getLong("id");
            } else {
                logger.fatal(stmt.toString());
                throw new SQLException("failed to get id from ResultSet");
            }
        }
        return id;
    }
    
    private HashMap getLinks(String name) throws SQLException {
        HashMap result = new HashMap();
        // get links for category name
        String selectStmt = "SELECT links.id,url FROM links,cat2link "
            + "INNER JOIN categories ON cat2link.catId=categories.id "
            + "WHERE categories.name=? AND links.id=cat2link.linkId";

        PreparedStatement stmt = _connection.prepareStatement(selectStmt);
        stmt.setEscapeProcessing(true);     
        stmt.setString(1, name);
        stmt.execute();
        
        ResultSet rs = stmt.getResultSet();
        
        while ( rs.next() ) {
            result.put(new Long(rs.getLong("id") ), rs.getString("url"));
        }
        
        return result;
    }

    private void storeTags(long linkId, int wordCount, List filteredTags) throws SQLException {
        for (Iterator i = filteredTags.iterator(); i.hasNext(); ) {
            Map.Entry e = (Entry) i.next();
            TaggedLemma tl = (TaggedLemma) e.getKey();
            int freq = ((Integer) e.getValue()).intValue();
            
            String updateWordCountStmt = "UPDATE links SET wordCount=? WHERE id=?";
            
            PreparedStatement stmt = _connection.prepareStatement(updateWordCountStmt);
            stmt.setEscapeProcessing(true);     
            stmt.setInt(1, wordCount);
            stmt.setLong(2, linkId);
            stmt.execute();
            
            String lemma = tl.getLemma();
            String tag = tl.getTag();
            final int MAX_LEN = config.getInt("extractor.maxWordLength");
            
            if (lemma.length() > MAX_LEN ) {
                logger.warn(tl.getLemma() + ": too long (" + tl.getLemma().length() + ").");
                lemma = lemma.substring(0, MAX_LEN);
            }
            
            long wordId = insertAndGetId("words", new String[] { "word", "tag" }, new Object[] { lemma, tag });
            
            Statement word2linkStmt = _connection.createStatement();
            word2linkStmt.execute("INSERT INTO word2link (wordId, linkId, freq) VALUES(" + wordId + ", " + linkId + ", " + freq + ");");
        }
    }
    
    private void processLinks(HashMap links) throws SQLException {
        logger.info("processing " + links.size() + " links.");
        long lc = 1;
        for (Iterator iter = links.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Entry) iter.next();

            long linkId = ((Long)entry.getKey()).longValue(); 
            String link = (String) entry.getValue();
	    
	        logger.info("l (" + lc++ + "): " + link);

	        URL url = null;
	        try {
	            url = new URL(link);
	        } catch (MalformedURLException e) {
	            logger.error("malformed url :"   + link);
	            continue;
	        }

	        PlaintextConverter pc = new PlaintextConverter(url);
	        Tagger tagger = null;
	        
	        try {
	            tagger = new TreeTagger(pc.getPlaintext() );
	        } catch (ConnectionFailedException e) {
	            logger.error(e);
	            continue;
	        } catch (ResourceNotFoundException e) {
	            logger.error(e);
	            continue;
	        } catch (IOException e) {
	            logger.error("error getting plaintext:");
	            logger.error(e);
	            continue;
	        }
	        
	        CountingHashtable tags = null;
	        try {
	            tags = tagger.getTags();
	        } catch (TaggerException e) {
	            logger.error(e);
	            continue;
	        } catch (IOException e) {
	            logger.error(e);
	            continue;
	        }
	        
	        TagFilter tagFilter = new FrequencyTagFilter(tags);	        
	        storeTags(linkId, tagger.getWordCount(), tagFilter.getFilteredTags() );
        }
    }

    public void go(long startCat) throws SQLException {
        System.setProperty("sun.net.client.defaultConnectTimeout", "5000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        dbConnect();
        
        // get links for each category
        Statement stmt  = _connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(name) FROM categories");
        
        long numCat = 0;
        if ( rs.next() ) {
            numCat = rs.getLong(1);
            logger.info("processing " + numCat + " categories.");
        } else {
            throw new SQLException("failed to count categories.");
        }
        
        long cat = startCat;
        int ROWS_PER_QUERY = config.getInt("extractor.rowsPerQuery");
        
        while (cat < numCat) {
            rs = stmt.executeQuery("SELECT name from categories LIMIT " + cat + "," + ROWS_PER_QUERY);
            
            long cc = cat + 1;
            while ( rs.next() ) {
                logger.info("c (" + cc++ + "): " + rs.getString("name"));
                HashMap links = getLinks(rs.getString("name"));
                
                if (0 == links.size() ) {
                    continue;
                } else {
                    processLinks(links );
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
        
        if (null == args) {
            new Extractor().go(0);
        } else if (args[0].compareToIgnoreCase("--resume") == 0) {
            new Extractor().go(new Long(args[1]).longValue());
        }
    }
}
