/*
 * Created on Feb 17, 2005 2:56:26 PM
 */
package org.inca.odp.ie;

import java.io.IOException;

import org.inca.util.CollisionableHashtable;

/**
 * @author achim
 */
public abstract class Tagger {
    protected StringBuffer _data = null;

    public Tagger(StringBuffer data) {
        this._data = data;
    }
    
    abstract public CollisionableHashtable getTags() throws IOException;
}
