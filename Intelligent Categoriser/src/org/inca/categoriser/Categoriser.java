/*
 * Created on Mar 10, 2005 12:53:36 PM
 */
package org.inca.categoriser;

import java.io.IOException;
import java.net.URL;

import org.inca.odp.ie.tagger.TaggerException;
import org.inca.util.CountingHashtable;
import org.inca.util.net.ResourceNotFoundException;

/**
 * @author achim
 */
public abstract class Categoriser {
    protected URL _url;
    protected StringBuffer _data;
    protected CountingHashtable _tags;
    
    public Categoriser(URL url) {
        this._url = url;
    }
    
    abstract public String getCategory() throws ResourceNotFoundException, TaggerException, IOException; 
}
