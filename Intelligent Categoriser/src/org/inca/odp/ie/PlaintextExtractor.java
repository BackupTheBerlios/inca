/*
 * Created on Feb 21, 2005 6:12:15 PM
 */
package org.inca.odp.ie;

import java.io.IOException;

/**
 * @author achim
 */
public abstract class PlaintextExtractor {
    protected String _data;
    public PlaintextExtractor(String data) {
        this._data = data;   
    }

    public abstract StringBuffer getPlaintext() throws IOException;
}
