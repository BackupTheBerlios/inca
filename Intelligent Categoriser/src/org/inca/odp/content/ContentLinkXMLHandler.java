package org.inca.odp.content;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author achim
 * extracts links from categories. writes a dmoz-content file
 * listing all categories and creates the odp category
 * structure on the filesystem.
 */
public class ContentLinkXMLHandler extends DefaultHandler {
    private final static String DMOZ_BASE = "/home/achim/Projects/studienarbeit/odp/content";

    private String _currentTag = "";
    private String _currentTopicID = "";
    private String _currentLink = "";
    private String _currentDescription = "";

    /**
     * there are two topic tags: one nested in externalPage and one standing for itself. 
     */
    private boolean _inExternalPage = false;

    private StringBuffer _thisTag = null;
    private PrintWriter _writer = null;
    private PrintWriter _indexWriter = null;
    private PrintWriter _descriptionWriter = null;

    private MessageDigest _md = null;

    private Pattern _linkEnclosingTags = Pattern.compile(
            "narrow|link|link1|narrow2|related", Pattern.CASE_INSENSITIVE);
    private Pattern _illegalCharacters = Pattern.compile("[\\[\\]|<>]");

    private static int count = 0;

    final private static String NL = System.getProperty("line.separator");
    final private static String FS = System.getProperty("file.separator");

    public ContentLinkXMLHandler() throws UnsupportedEncodingException,
            FileNotFoundException, NoSuchAlgorithmException {
        super();

        _indexWriter = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(
                        "/home/achim/Projects/studienarbeit/odp/dmoz-content"),
                        "UTF-8")));
        _thisTag = new StringBuffer(5 * 1024);
        _md = MessageDigest.getInstance("SHA-1");
    }


    /**
     * @param resource
     * @return resource with Top replaced by http://dmoz.org
     */
    private String fixResource(String resource) {
        return resource.replaceFirst("Top", "http://dmoz.org");
    }

    private void insertLink(String resource) {
        _thisTag.append(resource + NL);
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
                e1.printStackTrace();
            }
            
            // get path and filename from tag
            String topicPath = topic.getPath();
            String topicFile = "links";
            
            // ignore content in World category
            if (topicPath.startsWith("/World") ) {
                return;
            }

            // append trailing slash if necessary
            if (!topicPath.endsWith("/") ) {
                topicPath = topicPath + "/";
            }
            
            // make sure all parent directories and this directory exist.
            (new File(DMOZ_BASE + topicPath)).mkdirs();

            // create a file with links for this topic.
            if (_thisTag.length() > 0) {
                try {
                    _writer = new PrintWriter(
                            new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(DMOZ_BASE + topicPath
                                            + topicFile), "UTF-8")));
                    
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String temp = _thisTag.toString();
                _writer.print(_thisTag.toString());
                _thisTag.delete(0, _thisTag.length());
                _writer.close();
            }

            // write current topic to content list
            _indexWriter.println(_currentTopicID);

            _currentTopicID = "";
            _currentLink = "";
            _currentDescription = "";
            ++count;
        } else if (qName.compareToIgnoreCase("externalPage") == 0) {
//            URL topic = null;
//            try {
//                topic = new URL(_currentTopicID);
//            } catch (MalformedURLException e1) {
//                // TODO Auto-generated catch block
//                e1.printStackTrace();
//            }
//            
//            // get path and filename from tag
//            String topicPath = topic.getPath();
//            String topicFile = "links";
//
//            if (_currentDescription.length() > 0) {
//                try {
//                    _descriptionWriter = new PrintWriter(
//                            new BufferedWriter(new OutputStreamWriter(
//                                    new FileOutputStream(DMOZ_BASE + topicPath
//                                            + "description"), "UTF-8")));
//                } catch (UnsupportedEncodingException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                _descriptionWriter.write(_currentDescription);
//                _descriptionWriter.close();
//            }
            _inExternalPage = false;
        }
    }

    public void startDocument() throws SAXException {
        count = 0;
    }

    public void endDocument() throws SAXException {
        _indexWriter.close();
        System.out.println(count + " processed");
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