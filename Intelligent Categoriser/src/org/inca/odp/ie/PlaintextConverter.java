/*
 * Created on Feb 17, 2005 3:31:38 PM
 */
package org.inca.odp.ie;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.inca.util.net.HTTPReader;

import com.quiotix.html.parser.ParseException;

/**
 * @author achim
 */
public class PlaintextConverter {    
    private URL _url = null;
    private Pattern _whitespace = Pattern.compile("\\s+");
    
    private static final Hashtable MimetypeMapping = new Hashtable();
    
    static {
        MimetypeMapping.put("text/html", XMLPlaintextExtractor.class);
        MimetypeMapping.put("text/xml", XMLPlaintextExtractor.class);
    }
        
    public PlaintextConverter(URL url) {
    	this._url = url;
	}
    
    private PlaintextExtractor getInstanceForContentType(String contentType, String data) throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (MimetypeMapping.containsKey(contentType) ) {
            Class c = (Class) MimetypeMapping.get(contentType);
            Constructor constructor = c.getConstructor(new Class[] { String.class } );
            
            return (PlaintextExtractor)constructor.newInstance(new Object[] { data } );
        } else {
            throw new InstantiationException("unsupported mimetype: " + contentType);
        }
    }

    public StringBuffer getPlaintext() throws IOException {
        String contentType = URLConnection.guessContentTypeFromStream( new BufferedInputStream(_url.openStream()));
        HTTPReader reader = new HTTPReader(_url);
        
        String data = reader.read();
        Matcher matcher = _whitespace.matcher(data);
        data = matcher.replaceAll(" ");
        
        data = EntityMapper.mapEntities(data);

        PlaintextExtractor pc = null;
//        try {
//            pc = getInstanceForContentType(contentType, data);
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            System.err.println("unsupported mimetype: " + contentType);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
        
        if (contentType.compareToIgnoreCase("text/html") == 0
                || contentType.compareToIgnoreCase("text/xml") == 0) {
            pc = new XMLPlaintextExtractor(data);
        } else {
            throw new IOException("unsupported mime type: " + contentType);
        }

        return pc.getPlaintext();
    }
    
    public static void main(String[] args) throws MalformedURLException,
            ParseException, IOException {
        //"http://www.cnn.com/2005/SHOWBIZ/TV/01/23/carson.obit/"
        PlaintextConverter pc = new PlaintextConverter(new URL(
                "http://www.cnn.com/2005/SHOWBIZ/TV/01/23/carson.obit/"));
        StringBuffer text = pc.getPlaintext();
        System.out.println(text.toString());
    }
}
