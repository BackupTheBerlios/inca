/*
 * Created on Feb 17, 2005 3:14:23 PM
 */
package org.inca.odp.ie.tagger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.inca.main.ApplicationConfiguration;
import org.inca.util.CountingHashtable;
import org.inca.util.sys.StreamGobbler;

/**
 * @author achim
 */
public class TreeTagger extends Tagger {
    private static Configuration config = ApplicationConfiguration.getConfiguration();
    private static Logger logger = Logger.getLogger(TreeTagger.class);
    
    private static final Hashtable INTERESTING_TAGS = new Hashtable();
    private final static String _NL = System.getProperty("line.separator");

    static {
        INTERESTING_TAGS.put("CD", new Boolean(true));
        INTERESTING_TAGS.put("FW", new Boolean(true));
        INTERESTING_TAGS.put("JJ", new Boolean(true));
        INTERESTING_TAGS.put("JJR", new Boolean(true));
        INTERESTING_TAGS.put("JJS", new Boolean(true));
        INTERESTING_TAGS.put("NN", new Boolean(true));
        INTERESTING_TAGS.put("NNS", new Boolean(true));
        INTERESTING_TAGS.put("NP", new Boolean(true));
        INTERESTING_TAGS.put("NPS", new Boolean(true));
        INTERESTING_TAGS.put("RB", new Boolean(true));
        INTERESTING_TAGS.put("RBR", new Boolean(true));
        INTERESTING_TAGS.put("RBS", new Boolean(true));
        INTERESTING_TAGS.put("VB", new Boolean(true));
        INTERESTING_TAGS.put("VBD", new Boolean(true));
        INTERESTING_TAGS.put("VBG", new Boolean(true));
        INTERESTING_TAGS.put("VBN", new Boolean(true));
        INTERESTING_TAGS.put("VBP", new Boolean(true));
        INTERESTING_TAGS.put("VBZ", new Boolean(true));
        INTERESTING_TAGS.put("WRB", new Boolean(true));
    }
    
    public TreeTagger(StringBuffer data) {
        super(data);
    }

    public CountingHashtable getTags() throws TaggerException, IOException {
        CountingHashtable tags = new CountingHashtable();
        Process taggerProc = Runtime.getRuntime().exec( new String[] {
                "/bin/sh", "-c", 
                config.getString("tagger.cmd") });
        StreamGobbler errorGobbler = new StreamGobbler(taggerProc.getErrorStream());
        StreamGobbler outputGobbler = new StreamGobbler(taggerProc.getInputStream());
        
        errorGobbler.start();
        outputGobbler.start();
        
        BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(taggerProc.getOutputStream() ) );
        
        bw.write(_data.toString());
        bw.close();
        
        try {
            int exitVal = taggerProc.waitFor();
        } catch (InterruptedException e) {
            throw new TaggerException("error waiting for tagger process.");
        } finally {
            taggerProc.getErrorStream().close();
            taggerProc.getInputStream().close();
            taggerProc.getOutputStream().close();
        }
        
//        errorGobbler.join();
//        outputGobbler.join();
        
        BufferedReader br = new BufferedReader(new StringReader(outputGobbler.getData().toString() ) ); 
        
        _wordCount = 0;
        _taggedWordCount = 0;
        String line;
        while ( null != ( line = br.readLine() )) {
            StringTokenizer st = new StringTokenizer(line, "\t ");
            
            if (2 != st.countTokens() ) {
                throw new IOException("error in TreeTagger output.");
            }
            
            String tag = st.nextToken();
            String lemma = st.nextToken();
            ++_wordCount;
            
            if ( INTERESTING_TAGS.containsKey(tag)) {
                ++_taggedWordCount;
                tags.put(new TaggedLemma(lemma, tag));
            }
        }
        
        br.close();
        
        logger.info("total words: " + _wordCount);
        logger.info("tagged words: " + _taggedWordCount);
        
        return tags;
    }

    public static void main(String[] args) throws TaggerException, IOException {
        ApplicationConfiguration.initInstance();
        File f = new File("/home/achim/Projects/workspace/GateEval/text");
        
        StringBuffer data = new StringBuffer();	
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        
        String line;
        
        while (null != (line = br.readLine())) {
            data.append(line + _NL);
        }
        
        br.close();
        
        TreeTagger tagger = new TreeTagger(data);
        System.out.println("exec treetagger.");

        long startTime = System.currentTimeMillis();
        CountingHashtable ht = tagger.getTags();
        long currentTime = System.currentTimeMillis();
     
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        long elapsedTime = currentTime - startTime;
        System.out.println("time: "
                + dateFormat.format(new Date(elapsedTime)));
        
        System.out.println( ht.size() );
        
        double tagsPerSec = 1000*ht.size()/elapsedTime;
        System.out.println("tags per second: " + tagsPerSec);
        
        TagFilter tagFilter = new FrequencyTagFilter(ht);
        List filteredTags = tagFilter.getFilteredTags();
        
        for (Iterator iter = filteredTags.iterator(); iter.hasNext(); ) {
            Map.Entry e = (Entry) iter.next();
            TaggedLemma tl = (TaggedLemma) e.getKey();

            System.out.println(tl.getLemma() + ": " + tl.getTag() + " (" + e.getValue() + ")");
        }
    }
}
