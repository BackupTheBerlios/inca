/*
 * Created on Feb 15, 2005 2:47:02 PM
 */
package org.inca.odp.content.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author achim
 */
public class NounExtractor {
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
    
    private void extract(String name) throws SQLException {
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
            System.out.println(rs.getString("url"));
        }
    }
    
    public void go() throws SQLException {
        dbConnect();
        
        // for each category, get links and extract nouns from link
        // count categories first.
        Statement stmt  = _connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(name) FROM categories");
        
        long numCat = 0;
        if ( rs.next() ) {
            System.out.println(rs.getLong(1));
        } else {
            throw new SQLException("failed to count categories.");
        }
        
        long cat = 0;
        long catPerQ = 5;
        numCat = 15;
        
        while (cat < numCat) {
            rs = stmt.executeQuery("SELECT name from categories LIMIT " + cat + "," + catPerQ);
            
            while ( rs.next() ) {
                extract(rs.getString("name"));
            }
            
            cat += catPerQ;
        }
        
        _connection.close();
    }
    
    public static void main(String[] args) throws SQLException {
        new NounExtractor().go();
    }
//    SELECT linkId FROM cat2link INNER JOIN categories ON cat2link.catId=categories.id WHERE categories.name='/Arts/Performing_Arts/Acting/Actors_and_Actresses/B/Brosnan,_Pierce'; 
//    SELECT url FROM links,cat2link INNER JOIN categories ON cat2link.catId=categories.id WHERE categories.name='/Arts/Performing_Arts/Acting/Actors_and_Actresses/B/Brosnan,_Pierce' AND links.id=cat2link.linkId
}
