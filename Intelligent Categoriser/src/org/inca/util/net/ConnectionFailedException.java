/*
 * Created on Feb 25, 2005 4:22:15 PM
 */
package org.inca.util.net;

import java.io.IOException;

public class ConnectionFailedException extends IOException {
    public ConnectionFailedException(String s) {
        super(s);
    }
}
