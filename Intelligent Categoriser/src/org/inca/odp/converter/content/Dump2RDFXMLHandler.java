/*
 * Created on Dec 16, 2004 3:22:00 PM
 */
package org.inca.odp.converter.content;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.inca.odp.CountryCodeMapper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author achim
 */
public class Dump2RDFXMLHandler extends DefaultHandler {
    private String _currentTag = "";
    private String _currentAbout = "";
    private String _currentTopicID = "";
    private String _currentTopicTitle = "";
    private String _currentCatID = "";
    private boolean _inExternalPage = false;

    private StringBuffer _thisTag = null;
    private PrintWriter _writer = null;
    private static int count = 0;
    
    final private static String NL = System.getProperty("line.separator");

    final private static String RDF_HEADER = "<?xml version=\"1.0\"? encoding='UTF-8'>" + NL
            + "<rdf:RDF" + NL
            + "  xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + NL
            + "  xmlns:dc=\"http://purl.org/metadata/dublin_core#\">";

    public Dump2RDFXMLHandler(String filename) throws UnsupportedEncodingException,
            FileNotFoundException {
        super();
        _writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                filename), "UTF-8")));
        _thisTag = new StringBuffer(5 * 1024);
    }
    
    private String fixResource(String resource) {
        return resource.replaceFirst("Top", "http://dmoz.org");
    }

    private void insertRelated(String resource) {
        
        _thisTag.append("<dmoz:related rdf:resource=\"" + fixResource(resource) + "\" />" + NL);
    }

    private void insertNarrow(String resource) {
        _thisTag.append("<dmoz:narrow rdf:resource=\"" + fixResource(resource) + "\" />" + NL);
    }
    
    private void insertLink(String resource) {
        _thisTag.append("<dmoz:link rdf:resource=\"" + fixResource(resource) + "\" />" + NL);
    }

    private void insertSeeAlso(String resource, String language) {
        _thisTag.append("<rdfs:seeAlso xml-lang=\"" + language
                + "\" rdf:resource=\"" + fixResource(resource) + "\" />" + NL);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        _currentTag = qName;
        if (qName.compareToIgnoreCase("topic") == 0 && !_inExternalPage) {
            _currentTopicID = attributes.getValue("r:id");
            _currentTopicID = _currentTopicID.replaceFirst("Top", "http://dmoz.org");
        }
        
        if (qName.compareToIgnoreCase("externalpage") == 0) {
            _inExternalPage = true;
            _currentAbout = attributes.getValue("about");
        }

        if (qName.compareToIgnoreCase("narrow") == 0) {
            insertNarrow(attributes.getValue("r:resource"));
        }
        
        if ((qName.compareToIgnoreCase("link") == 0)
                || (qName.compareTo("link1") == 0)) {
            insertLink(attributes.getValue("r:resource"));
        }

        if (qName.compareToIgnoreCase("narrow2") == 0) {
            insertNarrow(attributes.getValue("r:resource"));
        }

        if (qName.compareToIgnoreCase("related") == 0) {
            insertRelated(attributes.getValue("r:resource"));
        }

        if (qName.compareToIgnoreCase("altlang") == 0) {
            String combinedResource = attributes.getValue("r:resource");

            // split resource by ':'
            int colonIndex = combinedResource.indexOf(':');

            String language = combinedResource.substring(0, colonIndex);
            String resource = combinedResource.substring(colonIndex + 1);
            String countryCode = CountryCodeMapper.getCountryCode(language);
            
            if (null == countryCode) {
                countryCode = "unknown";
            }
            insertSeeAlso(resource, countryCode);
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.compareToIgnoreCase("topic") == 0 && !_inExternalPage) {
            _thisTag.insert(0, NL + "<rdf:Description about=\"" + _currentTopicID
                    + "\">" + NL);
//            _thisTag.append("<dc:title>" + _currentTopicTitle + "</dc:title>" + NL);
            _thisTag.append("<dmoz:catid>" + _currentCatID + "</dmoz:catid>" + NL);
            _thisTag.append("</rdf:Description>" + NL);

            String temp = _thisTag.toString();
            _writer.print(_thisTag.toString());
            _thisTag.delete(0, _thisTag.length());

            _currentCatID = "";
            _currentTopicID = "";
            _currentTopicTitle = "";
            _currentAbout = "";
            ++count;
            System.out.print("\r" + count + " tags processed");
        } else if (qName.compareToIgnoreCase("externalpage") == 0) {
            _thisTag.insert(0, NL + "<rdf:Description about=\"" + _currentAbout
                    + "\">" + NL);
            _thisTag.append("<dmoz:topic>" + _currentTopicID + "</dmoz:topic>" + NL);
            _thisTag.append("<dc:title>" + _currentTopicTitle + "</dc:title>" + NL);
            _thisTag.append("</rdf:Description>" + NL);
            
            String temp = _thisTag.toString();
            _writer.print(_thisTag.toString());
            _thisTag.delete(0, _thisTag.length());

            _currentCatID = "";
            _currentTopicID = "";
            _currentTopicTitle = "";
            _currentAbout = "";
            _inExternalPage = false;
            ++count;
            System.out.print("\r" + count + " tags processed");
        }        
    }

    public void startDocument() throws SAXException {
        _writer.print(RDF_HEADER);
        count = 0;
    }
    
    public void endDocument() throws SAXException {
        _writer.print("</rdf:RDF>" + NL);
        _writer.close();
        System.out.println("\n");
    }

    public void characters(char[] ch, int start, int length) {
        if (length > 0) {
	        String data = new String(ch, start, length);
	        
	        if ((_currentTag.compareToIgnoreCase("catid") == 0)
	                && (_currentCatID.compareToIgnoreCase("") == 0)) {
	            _currentCatID = data;
	        }
	        
	        if ((_currentTag.compareToIgnoreCase("d:title") == 0)
	                && (_currentTopicTitle.compareToIgnoreCase("") == 0)) {
	            _currentTopicTitle = data;
	        }
	        
	        if ((_currentTag.compareToIgnoreCase("topic") == 0)
                    && (_currentTopicID.compareToIgnoreCase("") == 0) && _inExternalPage) {
                _currentTopicID = data;
                _currentTopicID = _currentTopicID.replaceFirst("Top",
                        "http://dmoz.org");
            }
	        
        //        if ( (_currentTag.compareToIgnoreCase("lastUpdate") == 0)
        //                && (currentlastUpdate.compareToIgnoreCase("") == 0)) {
        //            currentlastUpdate = data;
        //        }
        }
    }
}