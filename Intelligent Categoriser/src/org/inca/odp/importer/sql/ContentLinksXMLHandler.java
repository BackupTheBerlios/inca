package org.inca.odp.importer.sql;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.inca.main.ApplicationConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author achim
 * extracts links from categories. writes a dmoz-content file
 * listing all categories and creates the odp category
 * structure on the filesystem.
 */
public class ContentLinksXMLHandler extends DefaultHandler {
    private static Configuration config = ApplicationConfiguration.getConfiguration();
    private static Logger logger = Logger.getLogger(ContentLinksXMLHandler.class);

    private String _currentTag = "";
    private String _currentTopicID = "";
    private String _currentLink = "";
    private String _currentDescription = "";
    
    private int _maxCatLen = 0;
    private int _maxURLLen = 0;
    
    private Connection _connection = null;

    /**
     * there are two topic tags: one nested in externalPage and one standing for itself. 
     */
    private boolean _inExternalPage = false;

    private LinkedList _links = null;

    private Pattern _linkEnclosingTags = Pattern.compile(
            "narrow|link|link1|narrow2|related", Pattern.CASE_INSENSITIVE);
    private Pattern _illegalCharacters = Pattern.compile("[\\[\\]|<>]");

    private static int count = 0;

    final private static String NL = System.getProperty("line.separator");
    final private static String FS = System.getProperty("file.separator");
    private long _startTime;

    public ContentLinksXMLHandler() {
        super();
        _links = new LinkedList();
       
        dbConnect();
    }
    
    private void dbConnect() {
        final String DB_URL = config.getString("extractor.dbUrl");
        final String DB_USER = config.getString("extractor.dbUser");
        final String DB_PASSWD = config.getString("extractor.dbPasswd");
        final String DB_DRIVER_CLASS = config.getString("extractor.dbDriverClass");

        try {
            Class.forName(DB_DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            logger.fatal("error loading sql driver.");
            return;
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
        stmt.execute();

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
    
    private void dbInsert(String topicPath) throws SQLException {
       logger.info(">" + topicPath);
        long catId = insertAndGetId("categories", "name", topicPath);
        
        if (_links.size() > 0) {
            for (Iterator iter = _links.iterator(); iter.hasNext();) {
                String link = (String) iter.next();
                if (link.length() > _maxURLLen) {
                    _maxURLLen = link.length();
                }
                
                long linkId = insertAndGetId("links", "url", link);
                Statement stmt = _connection.createStatement();
                stmt.executeUpdate("INSERT INTO cat2link (catId, linkId) VALUES(" + catId + ", " + linkId + ");");
            }
        }
    }

    /**
     * @param resource
     * @return resource with Top replaced by http://dmoz.org
     */
    private String fixResource(String resource) {
        return resource.replaceFirst("Top", "http://dmoz.org");
    }

    private void insertLink(String resource) {
        _links.add(resource);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        _currentTag = qName;
        if (qName.compareToIgnoreCase("topic") == 0 && !_inExternalPage) {
            _currentTopicID = fixResource(attributes.getValue("r:id"));
        }

        if (qName.compareToIgnoreCase("externalpage") == 0) {
            _inExternalPage = true;
        }

        if (_linkEnclosingTags.matcher(qName).matches()) {
            insertLink(attributes.getValue("r:resource"));
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.compareToIgnoreCase("topic") == 0 && !_inExternalPage) {
            URL topic = null;
            try {
                topic = new URL(_currentTopicID);
            } catch (MalformedURLException e1) {
                // TODO Auto-generated catch block
                logger.error(e1);
            }
            
            // get path and filename from tag
            String topicPath = topic.getPath();
            
            // ignore content in World category
            if (topicPath.startsWith("/World") ) {
                _links.clear();

                _currentTopicID = "";
	            _currentLink = "";
	            _currentDescription = "";
	            ++count;
            } else {
                if (topicPath.length() > _maxCatLen) {
                    _maxCatLen = topicPath.length();
                }
                
	            try {
	                dbInsert(topicPath);
	            } catch (SQLException e) {
	                // TODO Auto-generated catch block
	                logger.error(e);
	            }
	            
	            _links.clear();
	            
	            _currentTopicID = "";
	            _currentLink = "";
	            _currentDescription = "";
	            ++count;
            }
        } else if (qName.compareToIgnoreCase("externalPage") == 0) {
            _inExternalPage = false;
        }
    }

    public void startDocument() throws SAXException {
        count = 0;
        _startTime = System.currentTimeMillis();
    }

    public void endDocument() throws SAXException {
        try {
            _connection.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error(e);
        }
        
        long _currentTime = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        long elapsedTime = _currentTime - _startTime;
        logger.info("time: "
                + dateFormat.format(new Date(elapsedTime)));
        
        logger.info(count + " categories processed");
        logger.info("max category name length: " + _maxCatLen);
        logger.info("max url length: " + _maxURLLen);
    }

    public void characters(char[] ch, int start, int length) {
        if (length > 0) {
            String data = new String(ch, start, length);

            if ((_currentTag.compareToIgnoreCase("topic") == 0)
                    && (_currentTopicID.compareToIgnoreCase("") == 0)
                    && _inExternalPage) {
                _currentTopicID = fixResource(data);
            }
//            else if (_currentTag.compareToIgnoreCase("d:description") == 0) {
//                _currentDescription = data;
//            }
        }
    }
}