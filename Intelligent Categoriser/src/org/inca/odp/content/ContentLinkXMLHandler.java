package org.inca.odp.content;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author achim extract links from categories. writes a dmoz-content file
 *         listing all categories with links and creates the odp category
 *         structure on filesystem. linklists an entry e of category c are
 *         stored under content/c/sha1(e)
 */
/**
 * @author achim
 */
public class ContentLinkXMLHandler extends DefaultHandler {
    private final static String DMOZ_BASE = "/home/achim/Projects/studienarbeit/odp/content";

    private String _currentTag = "";
    private String _currentTopicID = "";
    private String _currentLink = "";

    
    /**
     * there are two topic tags: one nested in externalPage and one standing for itself. 
     */
    private boolean _inExternalPage = false;

    private StringBuffer _thisTag = null;
    private PrintWriter _writer = null;
    private PrintWriter _indexWriter = null;

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
            String topicFile = "";
            
            // ignore content in World category
            if (topicPath.startsWith("/World") ) {
                return;
            }

            if (_thisTag.length() == 0) {
                // a new category since there are no links
                if (!topicPath.endsWith("/")) {
                    // append trailing / if necessary
                    topicPath = topicPath + "/";
                }
                System.out.println("creating " + topicPath);
                // create a new directory for this category. non-existant parent
                // directories are also created
                (new File(DMOZ_BASE + topicPath)).mkdirs();
            } else {
                // not a category
                int pos = topicPath.lastIndexOf("/");
                topicFile = topicPath.substring(pos + 1);
                topicPath = topicPath.substring(0, pos + 1);

                // make sure all parent directories and this directory exist.
                System.out.println("creating " + topicPath);
                (new File(DMOZ_BASE + topicPath)).mkdirs();

                // create a file with links for this topic.
                // since category names often contain illegal characters, use
                // sha1(filename)
                _md.reset();
                _md.update(topicFile.getBytes());
                BigInteger hash = new BigInteger(1, _md.digest());
                String hashCode = hash.toString(16);
                System.out.println(topicFile + " = " + hashCode);

                try {
                    _writer = new PrintWriter(
                            new BufferedWriter(new OutputStreamWriter(
                                    new FileOutputStream(DMOZ_BASE + topicPath
                                            + hashCode), "UTF-8")));
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // write current topic to content list
                _indexWriter.println(_currentTopicID);
                String temp = _thisTag.toString();
                _writer.print(_thisTag.toString());
                _thisTag.delete(0, _thisTag.length());

                _currentTopicID = "";
                _currentLink = "";
                ++count;
                _writer.close();
            }
        } else if (qName.compareToIgnoreCase("externalPage") == 0) {
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
        }
    }
}