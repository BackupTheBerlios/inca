package org.inca.util;

import java.util.Enumeration;
import java.util.Hashtable;
import org.inca.odp.ie.tagger.TaggedLemma;

public class CountingHashtable extends Hashtable {
    public CountingHashtable() {
        super();
    }
    
    public void put(Object key) {
	    Integer freq = (Integer) get(key);
	    if (null == freq) {
	        // no entry yet
	        put(key, new Integer(1));
	    } else {
	        put(key, new Integer(freq.intValue() + 1));
	    }
    }
    
    public int getCount(Object key) {
        Integer freq = (Integer) super.get(key);
        
        if (null == freq) {
            return 0;
        } else {
            return freq.intValue();
        }
    }
    
    public static void main(String[] args) {
        CountingHashtable ht = new CountingHashtable();
        ht.put(new TaggedLemma("foo", "bar"));
        ht.put(new TaggedLemma("foo", "bar"));
        
        Enumeration keys = ht.keys();
		while (keys.hasMoreElements()) {
		    TaggedLemma tl = (TaggedLemma) keys.nextElement();
			System.out.println(tl.getLemma() + ": " + tl.getTag() + " (" + ht.getCount(tl) + ")");
		} 
    }
}