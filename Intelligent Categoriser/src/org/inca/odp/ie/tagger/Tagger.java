/*
 * Created on Feb 17, 2005 2:56:26 PM
 */
package org.inca.odp.ie.tagger;

import java.io.IOException;

import org.inca.util.CountingHashtable;

/**
 * @author achim
 */
public abstract class Tagger {
    protected StringBuffer _data = null;
    
    // total number of words in documents
    protected int _wordCount;
    
    //number of tagged words
    protected int _taggedWordCount;

    public Tagger(StringBuffer data) {        
        this._data = data;
        _wordCount = 0;
        _taggedWordCount = 0;
    }
    
    abstract public CountingHashtable getTags() throws TaggerException, IOException;
    public int getTaggedWordCount() {
        return _taggedWordCount;
    }

    public int getWordCount() {
        return _wordCount;
    }  
}