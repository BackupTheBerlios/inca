/*
 * Created on Dec 16, 2004 3:19:29 PM
 */
package org.inca.odp.content;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author achim
 */
public class ContentLinkExtractor {
    private final static String ODP_FILE = "/home/achim/Projects/studienarbeit/odp/content.rdf.u8";
    private final static String mySAXDriver = "gnu.xml.aelfred2.SAXDriver";

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    private static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /**
     * Namespace prefixes feature id
     * (http://xml.org/sax/features/namespace-prefixes).
     */
    private static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";

    /** Default namespaces support (true). */
    protected static final boolean DEFAULT_NAMESPACES = true;

    /** Default namespace prefixes (false). */
    protected static final boolean DEFAULT_NAMESPACE_PREFIXES = false;

    public static void main(String[] args) {
        System.setProperty("org.xml.sax.driver", mySAXDriver);

        boolean namespaces = DEFAULT_NAMESPACES;
        boolean namespacePrefixes = DEFAULT_NAMESPACE_PREFIXES;

        XMLReader xr = null;
        try {
            xr = XMLReaderFactory.createXMLReader();
        } catch (SAXException e1) {
            System.err.println("could not create xml parser.");
        }

        try {
            xr.setFeature(NAMESPACES_FEATURE_ID, namespaces);
        } catch (SAXException e) {
            System.err.println("warning: Parser does not support feature ("
                    + NAMESPACES_FEATURE_ID + ")");
        }
        try {
            xr.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, namespacePrefixes);
        } catch (SAXException e) {
            System.err.println("warning: Parser does not support feature ("
                    + NAMESPACE_PREFIXES_FEATURE_ID + ")");
        }

        ContentLinkXMLHandler xh = null;
        try {
            try {
                xh = new ContentLinkXMLHandler();
            } catch (FileNotFoundException e4) {
                // TODO Auto-generated catch block
                e4.printStackTrace();
            } catch (NoSuchAlgorithmException e4) {
                // TODO Auto-generated catch block
                e4.printStackTrace();
            }
        } catch (UnsupportedEncodingException e3) {
            System.err.println("unsupported output encoding.");
        }
        xr.setContentHandler(xh);

        try {
            // Start parsing
//            xr.parse(new InputSource(new BufferedReader(new InputStreamReader(
//                    new FileInputStream(ODP_FILE), "UTF-8)"))));
            xr.parse(new InputSource(new FileReader(ODP_FILE)));
        } catch (FileNotFoundException e2) {
            System.err.println("could not open " + ODP_FILE
                    + " for reading.");
        } catch (UnsupportedEncodingException e2) {
            System.err.println("unsupported input encoding.");
        } catch (IOException e2) {
            System.err.println("error reading from " + ODP_FILE);
        } catch (SAXException e2) {
            System.err.println("error parsing " + ODP_FILE);
        }

        System.out.println("done.");
    }
}