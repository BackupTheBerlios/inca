/*
 * Created on Feb 17, 2005 3:31:38 PM
 */
package org.inca.odp.ie;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.inca.odp.ie.datasources.EntityMapper;
import org.inca.odp.ie.datasources.PlaintextExtractor;
import org.inca.odp.ie.datasources.XMLPlaintextExtractor;
import org.inca.util.logging.LogHelper;
import org.inca.util.net.HTTPReader;
import org.inca.util.net.ResourceNotFoundException;

import com.quiotix.html.parser.ParseException;

/**
 * @author achim
 */
public class PlaintextConverter {
    private static Logger logger = LogHelper.getLogger();
    private URL _url = null;
    private Pattern _whitespace = Pattern.compile("\\s+");
    
    private static final Hashtable MIMETYPE_MAPPING = new Hashtable();
    
    static {
        MIMETYPE_MAPPING.put("text/html", XMLPlaintextExtractor.class);
        MIMETYPE_MAPPING.put("text/xml", XMLPlaintextExtractor.class);
    }
        
    public PlaintextConverter(URL url) {
    	this._url = url;
	}
    
    private PlaintextExtractor getInstanceForContentType(String contentType,
            String data) throws NoSuchMethodException,
            IllegalArgumentException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        if (MIMETYPE_MAPPING.containsKey(contentType)) {
            Class c = (Class) MIMETYPE_MAPPING.get(contentType);
            Constructor constructor = c
                    .getConstructor(new Class[] { String.class });

            return (PlaintextExtractor) constructor
                    .newInstance(new Object[] { data });
        } else {
            throw new InstantiationException("unsupported mimetype: "
                    + contentType);
        }
    }

    public StringBuffer getPlaintext() throws IOException, ResourceNotFoundException {
        HTTPReader reader = new HTTPReader(_url);

        String contentType = reader.guessContentType();        
        String data = reader.read();
        
        Matcher matcher = _whitespace.matcher(data);
        data = matcher.replaceAll(" ");
        
        data = EntityMapper.mapEntities(data);

        PlaintextExtractor pe = null;
/* TODO: uncomment this to use java reflection api instead of hard-coded
 * mime types
 		try {
            pc = getInstanceForContentType(contentType, data);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            System.err.println("unsupported mimetype: " + contentType);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
  */
        if (null == contentType) {
            logger.warn("error guessing content type, trying xhtml");
            pe = new XMLPlaintextExtractor(data);
        } else if (contentType.compareToIgnoreCase("text/html") == 0
                || contentType.compareToIgnoreCase("text/xml") == 0
                || contentType.compareToIgnoreCase("application/xhtml+xml") == 0
                || contentType.compareToIgnoreCase("application/xml") == 0) {
            pe = new XMLPlaintextExtractor(data);
        } else {
            logger.warn("unsupported mime type: " + contentType + ". trying xhtml.");
            pe = new XMLPlaintextExtractor(data);
        }

        return pe.getPlaintext();
    }
    
    public static void main(String[] args) throws ParseException, IOException, ResourceNotFoundException {
        //"http://www.cnn.com/2005/SHOWBIZ/TV/01/23/carson.obit/"
        PlaintextConverter pc = new PlaintextConverter(new URL(
                "http://www.cnn.com/2005/SHOWBIZ/TV/01/23/carson.obit/"));
        StringBuffer text = pc.getPlaintext();
        System.out.println(text.toString());
    }
}
