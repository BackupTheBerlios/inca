/*
 * Created on Feb 17, 2005 3:31:38 PM
 */
package org.inca.odp.ie;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    private static final Logger logger = Logger.getLogger(PlaintextConverter.class);
    private List _urlList = null;
    private Pattern _br = Pattern.compile("<br\\s*/?>", Pattern.CASE_INSENSITIVE);
    private Pattern _whitespace = Pattern.compile("[ \\t\\n\\x0B\\f\\r\\u0085\\u2028\\u2029]+");
    private Pattern _sentenceDelim = Pattern.compile("([.?!])([A-Z\"\'])");
    
    private static final Hashtable MIMETYPE_MAPPING = new Hashtable();
    
    static {
        MIMETYPE_MAPPING.put("text/html", XMLPlaintextExtractor.class);
        MIMETYPE_MAPPING.put("text/xml", XMLPlaintextExtractor.class);
    }
        
    public PlaintextConverter(URL url) {
    	_urlList = new LinkedList();
    	_urlList.add(url);
	}
    
    public PlaintextConverter(List urlList) {
    	this._urlList = urlList;
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
        StringBuffer result = new StringBuffer();

        for (Iterator iter = _urlList.iterator(); iter.hasNext();) {
            URL url = (URL) iter.next();
	        HTTPReader reader = new HTTPReader(url);
	
	        String contentType = reader.guessContentType();        
	        String data = reader.read();
	        
	        Matcher matcher = _br.matcher(data);
	        data = matcher.replaceAll("\n");
	
	        matcher = _whitespace.matcher(data);
	        data = matcher.replaceAll(" ");
	        
	        if (logger.isDebugEnabled() ) {
	            logger.debug(data);
	        }
	        
	        StringBuffer tmp = new StringBuffer();
	        matcher = _sentenceDelim.matcher(data);
	        while (matcher.find() ) {
	            matcher.appendReplacement(tmp, matcher.group(1) + " " + matcher.group(2));
	        }
	        
	        matcher.appendTail(tmp);
	        
	        data = EntityMapper.mapEntities(tmp.toString());
	
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
	        
	        result.append(pe.getPlaintext());
        }
        
        return result;
    }
    
    public static void main(String[] args) throws ParseException, IOException, ResourceNotFoundException {
        LogHelper.initInstance();
        //"http://www.cnn.com/2005/SHOWBIZ/TV/01/23/carson.obit/"
        PlaintextConverter pc = new PlaintextConverter(new URL(
                "http://www.coobics.com/classifieds/html/gp5.html"));        
        StringBuffer text = pc.getPlaintext();
        System.out.println(text.toString());
    }
}
