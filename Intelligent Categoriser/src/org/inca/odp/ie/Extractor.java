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
import java.util.Vector;

import org.inca.util.CollisionableHashtable;

/**
 * @author achim
 */
public class Extractor {
    final private static int ROWS_PER_QUERY = 5;
    final public static String DB_URL = "jdbc:mysql://localhost/odp";
    final public static String DB_USER = "odp";
    final public static String DB_PASSWD = "odp";
    final public static String DB = "MySQL";
    final public static String DB_DRIVER_CLASS = "com.mysql.jdbc.Driver";
    private Connection _connection;
    
    private void dbConnect() {
        try {
            Class.forName(DB_DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("error loading sql driver.");
        }
        try {
            _connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWD);
        } catch (SQLException e1) {
            System.err.println("error connection to database.");
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
            System.out.println(rs.getLong(1));
        } else {
            throw new SQLException("failed to count categories.");
        }
        
        long cat = 0;
        numCat = 15;
        
        while (cat < numCat) {
            rs = stmt.executeQuery("SELECT name from categories LIMIT " + cat + "," + ROWS_PER_QUERY);
            
            while ( rs.next() ) {
                Vector links = getLinks(rs.getString("name"));
            
	            for (Iterator iter = links.iterator(); iter.hasNext();) {
	                URL url = null;
	                String link = (String) iter.next();
                    try {
                        url = new URL(link);
                    } catch (MalformedURLException e) {
                        System.err.println("malformed url :"   + link);
                    }
                    
                    PlaintextConverter pc = new PlaintextConverter(url);
                    Tagger tagger = null;
                    try {
                        tagger = new TreeTagger(pc.getPlaintext());
                    } catch (IOException e1) {
                        System.err.println("error extracting plaintext for " + url);
                    }
                    
                    try {
                        CollisionableHashtable tags = tagger.getTags();
                    } catch (IOException e2) {
                        System.err.println("error tagging " + url);
                    }
	            }
            }
            
            cat += ROWS_PER_QUERY;
        }
        
        _connection.close();
    }
    
    public static void main(String[] args) throws SQLException {
        new Extractor().go();
    }
//    ~/Projects/studienarbeit/TreeTagger/cmd/tt-inca text|grep -v -f removedTags|grep "^[A-Z]" 

}
