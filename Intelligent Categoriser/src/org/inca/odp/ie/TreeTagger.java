/*
 * Created on Feb 17, 2005 3:14:23 PM
 */
package org.inca.odp.ie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import org.inca.util.CollisionableHashtable;

/**
 * @author achim
 */
public class TreeTagger extends Tagger {
    
    public TreeTagger(StringBuffer data) {
        super(data);
    }

    public CollisionableHashtable getTags() throws IOException {
        CollisionableHashtable tags = new CollisionableHashtable();
        Process taggerProc = Runtime.getRuntime().exec( new String[] {
                "/bin/sh", "-c", 
                "/home/achim/Projects/studienarbeit/TreeTagger/cmd/tt-inca" });
        
        OutputStream os = taggerProc.getOutputStream();
        BufferedReader br = new BufferedReader( new InputStreamReader(taggerProc.getInputStream() ) );
        new BufferedWriter(new OutputStreamWriter(os)).write(_data.toString());
        
        String line;
        while ( null != ( line = br.readLine() )) {
            StringTokenizer st = new StringTokenizer(line, "\t ");
            
            if (2 != st.countTokens() ) {
                throw new IOException("error in TreeTagger output.");
            }
            
            String tag = st.nextToken();
            String lemma = st.nextToken();
            
            tags.put(lemma, tag);
        }
        
        return tags;
    }
}
