/*
 * Created on Dec 16, 2004 3:19:29 PM
 */
package org.inca.odp.structure;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author achim
 */
public class Structure2RDF {
    private final static String STRUCTURE_FILE = "/home/achim/Projects/studienarbeit/odp/structure.rdf.u8";

    private final static String RDFSTRUCTURE_FILE = "/home/achim/Projects/studienarbeit/odp/dmoz-structure.rdf";

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

        StructureXMLHandler xh = null;
        try {
            xh = new StructureXMLHandler(RDFSTRUCTURE_FILE);
        } catch (UnsupportedEncodingException e3) {
            System.err.println("unsupported output encoding.");
        } catch (FileNotFoundException e3) {
            System.err.println("could not open file " + RDFSTRUCTURE_FILE
                    + " for writing.");
        }
        xr.setContentHandler(xh);

        try {
            // Start parsing
//            xr.parse(new InputSource(new BufferedReader(new InputStreamReader(
//                    new FileInputStream(STRUCTURE_FILE), "UTF-8)"))));
            xr.parse(new InputSource(new FileReader(STRUCTURE_FILE)));
        } catch (FileNotFoundException e2) {
            System.err.println("could not open " + STRUCTURE_FILE
                    + " for reading.");
        } catch (UnsupportedEncodingException e2) {
            System.err.println("unsupported input encoding.");
        } catch (IOException e2) {
            System.err.println("error reading from " + STRUCTURE_FILE);
        } catch (SAXException e2) {
            System.err.println("error parsing " + STRUCTURE_FILE);
        }

        System.out.println("done.");
    }
}