/*
 * Created on Feb 22, 2005 5:01:24 PM
 */
package org.inca.odp.ie.tagger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.inca.main.ApplicationConfiguration;
import org.inca.util.CountingHashtable;

/**
 * @author achim
 */
public class FrequencyTagFilter extends TagFilter {
    private static Configuration config = ApplicationConfiguration.getConfiguration();
    public FrequencyTagFilter(CountingHashtable tags) {
        super(tags);
    }

    private class ReverseEntryValueComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return compare((Map.Entry) o2, (Map.Entry) o1);
        }

        public int compare(Map.Entry e1, Map.Entry e2) {
            int cf = ((Comparable) e1.getValue()).compareTo(e2.getValue());
            if (cf == 0) {
                cf = ((Comparable) e1.getKey()).compareTo(e2.getKey());
            }
            return cf;
        }
    }

    public List getFilteredTags() {
        List sortedTags = new ArrayList(_tags.entrySet() );
        Collections.sort(sortedTags, new ReverseEntryValueComparator() );
        
        int max = Math.min(sortedTags.size(), config.getInt("tagger.tagsPerDoc"));
        return sortedTags.subList(0, max);        
    }
}
