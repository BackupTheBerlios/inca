/*
 * Created on Feb 21, 2005 8:51:03 PM
 */
package org.inca.odp.ie;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.sun.rsasign.d;

/**
 * @author achim
 */
public class EntityMapper {
    private static Hashtable entities = new Hashtable();
    
    static {
        // see http://www.htmlhelp.com/reference/html40/entities/latin1.html
        entities.put(Pattern.compile("&nbsp;"), " ");
        entities.put(Pattern.compile("&auml;"), "ae");
        entities.put(Pattern.compile("&ouml;"), "oe");
        entities.put(Pattern.compile("&uuml;"), "ue");
        entities.put(Pattern.compile("&Auml;"), "Ae");
        entities.put(Pattern.compile("&Ouml;"), "Oe");
        entities.put(Pattern.compile("&Uuml;"), "Ue");
        entities.put(Pattern.compile("&uuml;"), "ue");
        entities.put(Pattern.compile("&copy;"), "(c)");
        entities.put(Pattern.compile("&reg;"), "(r)");
        entities.put(Pattern.compile("&uuml;"), "ue");
        entities.put(Pattern.compile("&szlig;"), "ss");
//        entities.put(Pattern.compile("&quot;"), "\"");
//        entities.put(Pattern.compile("&amp;"), "&");
//        entities.put(Pattern.compile("&lt;"), "<");
//        entities.put(Pattern.compile("&gt;"), ">");
        
        // all other entities are removed
        entities.put(Pattern.compile("&[a-zA-Z0-9]{2,}?;"), " ");
    }
    
    public static String mapEntities(String data) {
        for (Iterator iter = entities.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry e = (Entry) iter.next();
            Pattern p = (Pattern) e.getKey();
            data = p.matcher(data).replaceAll((String) e.getValue());
        }
        
        return data;
    }
}

