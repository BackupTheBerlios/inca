/*
 * Created on Jan 17, 2005 3:57:35 PM
 */
package org.inca.util.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * @author achim
 */
public abstract class TextExtractor {
    private URL url;
    protected StringBuffer content;
    
    final protected static String NL = System.getProperty("line.separator");
    
    public TextExtractor(URL url) throws IOException {
        this.url = url;
        
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        
        content = new StringBuffer();
        String line = null;
        
        while ((line = in.readLine()) != null) {
            content.append(line + NL);
        }
        in.close();
    }

    public abstract StringBuffer getText(URL url);
}