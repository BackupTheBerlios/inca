package org.inca.util.sys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class StreamGobbler extends Thread {
    final private static String _NL = System.getProperty("line.separator");
    private InputStream _is;
    private String _type;

    private OutputStream _os;
    
    private StringBuffer _data;
   
    public StreamGobbler(InputStream is) {
        this._is = is;
        _data = new StringBuffer();
    }
    
    public StringBuffer getData() {
        return _data;
    }

    public void run() {
        try { 
            BufferedReader br = new BufferedReader(new InputStreamReader(_is));
            String line = null;
            
            while ( null != (line = br.readLine() ) ) {
                _data.append(line + _NL);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}