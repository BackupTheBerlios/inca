/*
 * Created on Feb 21, 2005 6:14:41 PM
 */
package org.inca.odp.ie.datasources;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLPlaintextExtractor extends PlaintextExtractor {
    private static final Logger logger = Logger.getLogger(XMLPlaintextExtractor.class);
    private final static String AELFRED_SAX_DRIVER  = "gnu.xml.aelfred2.SAXDriver";

    public XMLPlaintextExtractor(String data) {
        super(data);
    }

	private class XMLHandler extends DefaultHandler {
        private StringBuffer _text;
        private Hashtable _ignoredTags = new Hashtable();

        private boolean _inBody = false;
        private boolean _ignoredTag = false;
        private String _currentIgnoredTag;
        private String _currentTag;
        
        private int _depth;
        public XMLHandler() {
            super();
            _text = new StringBuffer();
            
//            _ignoredTags.put("a", new Boolean(true));
            _ignoredTags.put("applet", new Boolean(true));
            _ignoredTags.put("code", new Boolean(true));
            _ignoredTags.put("comment", new Boolean(true));
            _ignoredTags.put("form", new Boolean(true));
            _ignoredTags.put("menu", new Boolean(true));
            _ignoredTags.put("object", new Boolean(true));
            _ignoredTags.put("select", new Boolean(true));
            _ignoredTags.put("script", new Boolean(true));
            _ignoredTags.put("style", new Boolean(true));
            _ignoredTags.put("area", new Boolean(true));
            _ignoredTags.put("input", new Boolean(true));
            _ignoredTags.put("map", new Boolean(true));
            
            _depth = 0;
        }

        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            _currentTag = qName;
            if (qName.compareToIgnoreCase("body") == 0) {
                _inBody = true;
            }
            
            if (_ignoredTags.containsKey(qName) && !_ignoredTag) {
                _currentIgnoredTag = qName;
                _ignoredTag = true;
                
                if (logger.isDebugEnabled() )
                    logger.debug("ignored node: " + qName);
            } else if ( _ignoredTag ){
                if (logger.isDebugEnabled() )
                    logger.debug("ignored: " + qName);
            } else {
                if (logger.isDebugEnabled() )
                    logger.debug("tag: " + qName );
            }
            _depth++;
        }

        public void endElement(String uri, String localName, String qName)
        	throws SAXException {
            _depth--;
            if (qName.compareToIgnoreCase("body") == 0 ) {
                _inBody = false;
            }

            if ( _currentIgnoredTag != null && ( qName.compareToIgnoreCase(_currentIgnoredTag) == 0) && _ignoredTag ) {
                if (logger.isDebugEnabled() )
                    logger.debug("end ignored node: " + qName );
                _ignoredTag = false;
                _currentIgnoredTag = null;
            } else if ( _ignoredTag ) {
                if (logger.isDebugEnabled() )
                    logger.debug("end ignored: " + qName);
            } else {
                if (logger.isDebugEnabled() )
                    logger.debug("end tag: " + qName );
            }
        }

        public void unparsedEntityDecl(String name, String publicId,
            String systemId, String notationName) throws SAXException {
            logger.debug("unparsed entity declaration: " + name);
        }
        
        public void skippedEntity(String name) throws SAXException {
            logger.debug("skipped entity: " + name);
        }
        
        public void warning(SAXParseException e) throws SAXException {
            logger.debug(e.getMessage());
        }

        public void characters(char[] ch, int start, int length) {
            if ( length > 0 && !_ignoredTag ) {
                String data = new String(ch, start, length);
                if (logger.isDebugEnabled() ) {
                    logger.debug("text node:" + data);
                }
                _text.append(data);
            }
        }
        
        public StringBuffer getPlaintext() {
            return _text;
        }
	}

	private boolean isXHTML() {
	    int lookAhead = Math.min(_data.length(), 511);
        if (_data.substring(0, lookAhead).indexOf("doctype") != -1
                && _data.substring(0, 511).indexOf("xhtml") != -1) {
            // assume document is xhtml, use sax parser to get plaintext
            return true;
        } else {
            return false;
        }
    }

	public StringBuffer getPlaintext() throws IOException {
        XMLReader xr = null;

        if ( isXHTML () ) {
            System.setProperty("org.xml.sax.driver", AELFRED_SAX_DRIVER);
            try {
                xr = XMLReaderFactory.createXMLReader();
            } catch (SAXException e1) {
                logger.fatal("could not create xml parser.");
            }
        } else {
            xr = new Parser();
        }
        
        XMLHandler xh = new XMLHandler();
        xr.setContentHandler(xh);
/*        try {
            xr.setProperty("http://xml.org/sax/properties/lexical-handler", xh);
        } catch (SAXNotRecognizedException e1) {
            e1.printStackTrace();
        } catch (SAXNotSupportedException e1) {
            e1.printStackTrace();
        }*/
        
        try {
            xr.parse(new InputSource(new StringReader(_data)));
        } catch (SAXException e) {
            logger.warn("parsing as xhtml failed. trying html");
            
            xr = new Parser();
            xr.setContentHandler(xh);
            
            try {
                xr.parse(new InputSource(new StringReader(_data)));
            } catch (SAXException e2) {
                logger.error("parsing as html failed. giving up.");
            }
        }
        
        return xh.getPlaintext();
	}
}

