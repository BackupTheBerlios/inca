/*
 * Created on Feb 17, 2005 3:31:38 PM
 */
package org.inca.odp.ie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author achim
 */
public class PlaintextConverter {
    private final static String mySAXDriver = "gnu.xml.aelfred2.SAXDriver";
    protected final static String NL = System.getProperty("line.separator");
    private class HTMLHandler extends ParserCallback {
        private StringBuffer _text = null;
        private boolean _inBody = false;
        
        public HTMLHandler() {
            _text = new StringBuffer();
        }
        
        public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
            if (t.equals(HTML.Tag.BODY) ) {
                _inBody = true;
            }
        }
        
        public void handleEndTag(HTML.Tag t, int pos) {
            if (t.equals(HTML.Tag.BODY) ) {
                _inBody = false;
            }
        }
        
        public void handleText(char[] data, int pos) {
            if ( _inBody ) {
                _text.append(data);
                _text.append(NL);
            }
        }
        
        public StringBuffer getPlaintext() {
            return _text;
        }
    }

    private class XMLHandler extends DefaultHandler{
        private StringBuffer _text;
        public XMLHandler() {
            super();
            _text = new StringBuffer();
        }
        public void characters(char[] ch, int start, int length) {
            _text.append(ch);
            _text.append(NL);
        }

        public StringBuffer getPlaintext() {
            return _text;
        }
    }
    private URL _url = null;
    
    public PlaintextConverter(URL url) {
    	this._url = url;
	}
    
    private boolean isXHTML() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(_url.openStream()));
        if ( in.markSupported() ) {
            in.mark(256);
            
            String line = in.readLine().toLowerCase();

            in.reset();
            
            if ( line.indexOf("doctype") != -1 && line.indexOf("xhtml") != -1) {
                // assume document is xhtml, use sax parser to get plaintext
                return true;
            } else {           
                return false;
            }
        } else {
            throw new IOException("mark not supported in BufferedReader.");
        }
    }
    
    private StringBuffer parseAsXML() throws IOException {
        System.setProperty("org.xml.sax.driver", mySAXDriver);
        XMLReader xr = null;
        try {
            xr = XMLReaderFactory.createXMLReader();
        } catch (SAXException e1) {
            System.err.println("could not create xml parser.");
        }
        
        XMLHandler xh = new XMLHandler();
        xr.setContentHandler(xh);
        
        if (_url.getProtocol().compareToIgnoreCase("http") == 0) {
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) _url.openConnection();
            try {
                xr.parse(new InputSource(connection.getInputStream()));
            } catch (SAXException e) {
                // fall back to parsing as HTML
                return parseAsHTML();
            }
            
            return xh.getPlaintext();
        } else {
            return null;
        }
    }
    
    private StringBuffer parseAsHTML() throws IOException {
        if (_url.getProtocol().compareToIgnoreCase("http") == 0) {
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) _url.openConnection();

            ParserDelegator parser = new ParserDelegator();
            HTMLHandler callback = new HTMLHandler();
            parser.parse(new InputStreamReader(connection.getInputStream() ), callback, false);
            
            return callback.getPlaintext();
        } else {
            return null;
        }
    }
    
    public StringBuffer getPlaintext() throws IOException {
        if ( isXHTML () ) {
            // attempt to parse with SAX
            return parseAsXML();
        } else {
            return parseAsHTML();
        }
    }
    
    public static void main(String [] args) throws IOException {
        PlaintextConverter pc = new PlaintextConverter( new URL("http://wordpress.org/"));
        
        System.out.println(pc.getPlaintext().toString());
    }
}
