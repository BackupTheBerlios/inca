/*
 * Created on Feb 22, 2005 4:48:44 PM
 */
package org.inca.odp.ie.tagger;

import java.util.List;

import org.inca.util.CountingHashtable;

/**
 * @author achim
 */
public abstract class TagFilter {
    protected CountingHashtable _tags;

    public TagFilter(CountingHashtable tags) {
        this._tags = tags;
    }
    
    public abstract List getFilteredTags();
}
