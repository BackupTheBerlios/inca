/*
 * Created on Feb 21, 2005 6:14:41 PM
 */
package org.inca.odp.ie;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

class XMLPlaintextExtractor extends PlaintextExtractor {
    private final static String AELFRED_SAX_DRIVER  = "gnu.xml.aelfred2.SAXDriver";

    public XMLPlaintextExtractor(String data) {
        super(data);
    }

	class XMLHandler extends DefaultHandler {
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
	            
	            _ignoredTags.put("a", new Boolean(true));
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
	        }
	        
	        private void printIndent(String s) {
	            for (int i = 0; i < _depth; i++) {
	//                System.out.print(" ");
	            }
	            //System.out.println(s);
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
	                printIndent("ignored node: " + qName);
	            } else if ( _ignoredTag ){
	                printIndent("ignored: " + qName);
	            } else {
	                printIndent("tag: " + qName );
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
	                printIndent("end ignored node: " + qName );
	                _ignoredTag = false;
	                _currentIgnoredTag = null;
	            } else if ( _ignoredTag ) {
	                printIndent("end ignored: " + qName);
	            } else {
	                printIndent("end tag: " + qName );
	            }
	        }
	
	        public void characters(char[] ch, int start, int length) {
	            if ( length > 0 && !_ignoredTag ) {
	                String data = new String(ch, start, length);
	                printIndent("text node.");
	                //printIndent("text: " + new String(ch));
	                _text.append(data);
	            }
	        }

        public StringBuffer getPlaintext() {
            return _text;
        }
    }

	private boolean isXHTML() throws IOException {
        if (_data.substring(0, 511).indexOf("doctype") != -1
                && _data.substring(0, 511).indexOf("xhtml") != -1) {
            // assume document is xhtml, use sax parser to get plaintext
            return true;
        } else {
            return false;
        }
    }

	public StringBuffer getPlaintext() throws IOException {
	    StringBuffer text;
        XMLReader xr = null;
        if ( isXHTML () ) {
            System.setProperty("org.xml.sax.driver", AELFRED_SAX_DRIVER);
            try {
                xr = XMLReaderFactory.createXMLReader();
            } catch (SAXException e1) {
                System.err.println("could not create xml parser.");
            }
        } else {
            xr = new Parser();
        }
        
        XMLHandler xh = new XMLHandler();
        xr.setContentHandler(xh);
        
        try {
            xr.parse(new InputSource(new StringReader(_data)));
        } catch (SAXException e) {
            // TODO: fall back to parsing as HTML
            e.printStackTrace();
        }
        
        return xh.getPlaintext();
	}
}