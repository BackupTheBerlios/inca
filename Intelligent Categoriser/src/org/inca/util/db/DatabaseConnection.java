/*
 * taken from Foafscape by Mark Giereth and adapted for inca.
 */
package org.inca.util.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Encapsulates the JDBC API and provides a wrapper for Connection methods. This
 * class has to be initialized at startup of the application using the
 * initDatabaseConnection method. The DatabaseConnection can only be used by
 * classes in this package.
 * 
 * @author Mark Giereth
 * @authro Achim Stäbler
 */
public class DatabaseConnection {

   /** instance which does the logging. */
   private static Logger logger = Logger.getLogger(DatabaseConnection.class);

   //MySQL:
   //DRIVER = "com.mysql.jdbc.Driver";
   //URL = "jdbc:mysql://localhost/<database>";

   //HSQLDB
   //DRIVER = "org.hsqldb.jdbcDriver";
   //in-process URL = "jdbc:hsqldb:file:<database>";
   //server URL = "jdbc:hsqldb:hsql:/localhost/<database>";

   /** the shared intance. */
   private static DatabaseConnection s_sharedConnection;

   /** The used driver. */
   private static String s_driver;

   /** The used database url. */
   private static String s_url;

   /** The used connection porperties. */
   private static Properties s_properties;
   
   /**
    * Creates the one and only DatabaseConnection instance (Singleton). If the
    * method is called a 2nd time a new DatabaseConnection instance is created.
    * 
    * @param driver name of the driver class
    * @param url the database url
    * @param user database user
    * @üaram password database password
    * @throws SQLException
    * @throws ClassNotFoundException
    */
   public static void initDatabaseConnection(String driver,
                                             String url,
                                             String user,
                                             String password)
         throws SQLException, ClassNotFoundException {

      // store connection properties
      s_driver = driver;
      s_url = url;
      s_properties = new Properties();
      s_properties.put("user", user);
      s_properties.put("password", password);

      // create sharedConnection
      if (s_sharedConnection != null) {
         try {
            s_sharedConnection.closeConnection();
         } catch (SQLException e) {
            logger.warn("Error while closing shared connection. " + e);
         }
      }
      // this might throw an Exception
      s_sharedConnection = new DatabaseConnection(driver, url, s_properties);
   }

   /**
    * Creates the one and only DatabaseConnection instance (Singleton). If the
    * method is called a 2nd time a new DatabaseConnection instance is created.
    * 
    * @param driver name of the driver class
    * @param url the database url
    * @param props database properties
    * @throws SQLException
    * @throws ClassNotFoundException
    */
   public static void initDatabaseConnection(String driver,
                                             String url,
                                             Properties props)
         throws SQLException, ClassNotFoundException {

      // store connection properties
      s_driver = driver;
      s_url = url;
      s_properties = props;

      // create sharedConnection
      if (s_sharedConnection != null) {
         try {
            s_sharedConnection.closeConnection();
         } catch (SQLException e) {
            logger.warn("Error while closing shared connection. " + e);
         }
      }
      // this might throw an Exception
      s_sharedConnection = new DatabaseConnection(driver, url, props);
   }

   /**
    * Returns the shared DatabaseConnection instance (Singleton pattern). <br>
    * NOTE: It must have been initialized using the
    * <code>initDatabaseConnection(String,String,String,String)</code> method.
    * @throws RuntimeException if it has not been initialized.
    */
   public static DatabaseConnection getSharedConnection() {
      if (s_sharedConnection == null) {
         throw new RuntimeException("Database connection is not initialzied");
      }
      return s_sharedConnection;
   }

//   static DatabaseConnection getConnection()
//         throws SQLException, ClassNotFoundException {
//      return new DatabaseConnection(s_driver, s_url, s_properties);
//   }

   /** the JDBC connection. */
   private Connection m_connection;

   /** Private constructor. Connects to the database. */
   private DatabaseConnection(String driver, String url, Properties props)
         throws SQLException, ClassNotFoundException {
      Class.forName(driver);

      //m_connection = DriverManager.getConnection(url, user, pwd);
      m_connection = DriverManager.getConnection(url, props);

      DatabaseMetaData meta = m_connection.getMetaData();
      logger.info("Connected to: " + meta.getDatabaseProductName() + " (version: "
            + meta.getDatabaseProductVersion() + ")\n");
   }

   public void finalize() {
      try {
         m_connection.close();
      } catch (Exception e) {}
   }

   /**
    * Closes the database connection.
    * @throws SQLException
    */
   public void closeConnection()
         throws SQLException {
      // the shared connection can't be closed
      if (!this.equals(s_sharedConnection)) {
         if (m_connection != null) {
            m_connection.close();
         }
      }
   }

   /**
    * Executes a SQL query and returns the result as a List of List of Objects.
    * @param query the SQL query string
    * @return List of rows containing a List of column value objects. May be
    * empty but never null.
    * @throws SQLException
    */
   public List query(String query)
         throws SQLException {
      Statement stmt = m_connection.createStatement();
      ResultSet result = stmt.executeQuery(query);
      return asList(result);
   }

   public List query(String query, int max)
         throws SQLException {
      Statement stmt = m_connection.createStatement();
      ResultSet result = stmt.executeQuery(query);
      return asList(result, max);
   }

   /**
    * Executes the given SQL query and returns the query result as ResultSet.
    * Note: the ResultSet has to be closed by the caller.
    * 
    * @param query SQL query
    * @return query result as ResultSet
    * @throws SQLException
    */
   public ResultSet queryResultSet(String query)
         throws SQLException {
      Statement stmt = m_connection.createStatement();
      return stmt.executeQuery(query);
   }

   /**
    * Returns the result as a List of List of Objects.
    * @param result the ResultSet to process
    * @return List of rows containing a List of column value objects. May be
    * empty but never null.
    * @throws SQLException
    */
   public List asList(ResultSet result)
         throws SQLException {
      return asList(result, Integer.MAX_VALUE);
   }

   /**
    * Returns the result as a List of List of Objects.
    * @param result the ResultSet to process
    * @param max maximun count of result items
    * @return List of rows containing a List of column value objects. May be
    * empty but never null.
    * @throws SQLException
    */
   public List asList(ResultSet result, int max)
         throws SQLException {
      if (result != null) {
         List retval = new ArrayList();
         int columnCount = result.getMetaData().getColumnCount();
         // for each row in the result set
         if (columnCount > 0) {
            int rowCount = 0;
            while (result.next() && rowCount < max) {
               rowCount++;
               // create list representing the row items
               List row = new ArrayList(columnCount);
               for (int i = 1; i <= columnCount; i++) {
                  // use default JDBC getObject() method
                  Object obj = result.getObject(i);
                  row.add(obj);
               }
               retval.add(row);
            }
         } // else nothing to do
         result.close(); // we are done
         return retval;
      } else {
         return new ArrayList(0);
      }
   }

   /**
    * Executes a SQL query, which queries exactly one row, and returns the
    * result as a List of List of Objects. If the query specifies more then one
    * row, only the first row is returned.
    * @param query the SQL query string
    * @return List of Objects. The list may be empty but never null!
    * @throws SQLException
    */
   public List querySingleColumn(String query)
         throws SQLException {
      return querySingleColumn(query, Integer.MAX_VALUE);
   }

   public List querySingleColumn(String query, int max)
         throws SQLException {
      Statement stmt = m_connection.createStatement();
      ResultSet result = stmt.executeQuery(query);
      List retval = new ArrayList(); // the return value
      if (result != null) {
         if (result.getMetaData().getColumnCount() >= 1) {
            int rowCount = 0;
            // for each row in the result set
            while (result != null && result.next() && rowCount < max) {
               rowCount++;
               Object obj = result.getObject(1); // exactly one column
               retval.add(obj); // add object to result list
            }
         }
         result.close();
      }
      return retval;
   }

   /**
    * Executes SQL query, which queries exactly one value. If the query
    * specifies more then one column or more then one, only the first column of
    * the first row is returned. If the result is empty <code>null</code> is
    * returned.
    * @param query the SQL query string
    * @return an Object or <code>null</code> if no such object exists
    * @throws SQLException
    */
   public Object querySingleObject(String query)
         throws SQLException {

      Statement stmt = m_connection.createStatement();
      ResultSet result = stmt.executeQuery(query);
      Object retval = null; // the return value
      if (result != null) {
         //if (result.getMetaData().getColumnCount() >= 1) {
         // for each row in the result set
         //if (result.next()) {
         try {
            if (result.next()) {
               retval = result.getObject(1); // exactly one column
            }
         } catch (SQLException e) {
            logger.warn(e.toString());
         }
         //}
         //}
         result.close();
      }
      return retval;
   }

   //
   // transaction methods
   //

   /**
    * @throws java.sql.SQLException
    */
   public void commit()
         throws SQLException {
      m_connection.commit();
   }

   /**
    * @throws java.sql.SQLException
    */
   public void rollback()
         throws SQLException {
      m_connection.rollback();
   }

   /**
    * @return
    * @throws java.sql.SQLException
    */
   public boolean getAutoCommit()
         throws SQLException {
      return m_connection.getAutoCommit();
   }

   /**
    * @param autoCommit
    * @throws java.sql.SQLException
    */
   public void setAutoCommit(boolean autoCommit)
         throws SQLException {
      m_connection.setAutoCommit(autoCommit);
   }
   
   public Statement createStatement() throws SQLException {
       return m_connection.createStatement();
   }

   //
   // prepared statements
   //

   /**
    * Creates a JDBC PreparedStatement and gives direct access to the JDBC
    * interface. Used to handle streams.
    */
   public PreparedStatement createPrepared(String sql)
         throws SQLException {
      return m_connection.prepareStatement(sql);
   }
   
   /**
    * Creates a JDBC PreparedStatement and gives direct access to the JDBC
    * interface.
    */
   public PreparedStatement createPrepared(String sql, int i)
         throws SQLException {
      return m_connection.prepareStatement(sql, i);
   }

   /**
    * Executes a SQL update, insert or delete statement.
    * @param sql the SQL statement
    * @return result as int.
    * @throws SQLException
    * 
    * TODO this method should be package private
    */
   public int execute(String sql)
         throws SQLException {
      //logger.finest("execute: "+sql);
      Statement stmt = m_connection.createStatement();
      return stmt.executeUpdate(sql);
   }
}