/*
 * Created on Dec 16, 2004 3:19:29 PM
 */
package org.inca.odp.importer.sql;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.inca.main.ApplicationConfiguration;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author achim
 */
public class ContentLinks {
    static Configuration config;
    static Logger logger = Logger.getLogger(ContentLinks.class);  
    private final static String mySAXDriver = "gnu.xml.aelfred2.SAXDriver";

    /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
    private static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";

    /**
     * Namespace prefixes feature id
     * (http://xml.org/sax/features/namespace-prefixes).
     */
    private static final String NAMESPACE_PREFIXES_FEATURE_ID = "http://xml.org/sax/features/namespace-prefixes";

    /** Default namespaces support (true). */
    private static final boolean DEFAULT_NAMESPACES = true;

    /** Default namespace prefixes (false). */
    private static final boolean DEFAULT_NAMESPACE_PREFIXES = false;

    public static void main(String[] args) {
        ApplicationConfiguration.initInstance();
        config  = ApplicationConfiguration.getConfiguration();
        
        final String ODP_BASE_DIR = config.getString("odp.baseDir");
        final String ODP_FILE = ODP_BASE_DIR + File.separator + config.getString("odp.contentFile");

        System.setProperty("org.xml.sax.driver", mySAXDriver);

        boolean namespaces = DEFAULT_NAMESPACES;
        boolean namespacePrefixes = DEFAULT_NAMESPACE_PREFIXES;

        XMLReader xr = null;
        try {
            xr = XMLReaderFactory.createXMLReader();
        } catch (SAXException e1) {
            logger.fatal("could not create xml parser.");
            System.exit(0);
        }

        try {
            xr.setFeature(NAMESPACES_FEATURE_ID, namespaces);
        } catch (SAXException e) {
            logger.warn("warning: Parser does not support feature ("
                    + NAMESPACES_FEATURE_ID + ")");
        }
        try {
            xr.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, namespacePrefixes);
        } catch (SAXException e) {
            logger.warn("warning: Parser does not support feature ("
                    + NAMESPACE_PREFIXES_FEATURE_ID + ")");
        }

        ContentLinksXMLHandler xh = null;
        xh = new ContentLinksXMLHandler();
        xr.setContentHandler(xh);

        try {
            // Start parsing
//            xr.parse(new InputSource(new BufferedReader(new InputStreamReader(
//                    new FileInputStream(ODP_FILE), "UTF-8)"))));
            xr.parse(new InputSource(new FileReader(ODP_FILE)));
        } catch (FileNotFoundException e2) {
            logger.fatal("could not open " + ODP_FILE
                    + " for reading.");
        } catch (UnsupportedEncodingException e2) {
            logger.fatal("unsupported input encoding.");
        } catch (IOException e2) {
           logger.fatal("error reading from " + ODP_FILE);
        } catch (SAXException e2) {
            logger.fatal("error parsing " + ODP_FILE);
        }

        logger.info("done.");
    }
}