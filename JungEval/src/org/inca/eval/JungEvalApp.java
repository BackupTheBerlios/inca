package org.inca.eval;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.Dictionary;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.utils.UserData;

/**
 * @author achim
 */
public class JungEvalApp {
    public static final String[] KEYWORDS = { "show", "host", "comedian",
            "guest", "business", "year", "world", "television",
            "something=2", "morning=2" };
    public static final int[] FREQ = { 17, 10, 4, 3, 3, 2, 2, 2, 2, 2 };

    public static void main(String[] args) {
        JungEvalApp app = new JungEvalApp();
        
        app.go();
    }
    
    public void go() {
        initWordNet();
        
        List words = lookupWords();
        List hypernyms = findHypernyms(words);
        
        Iterator hypernymsIt = hypernyms.iterator();
        for (Iterator iter = words.iterator(); iter.hasNext();) {
            
            IndexWord word = (IndexWord) iter.next();
            System.out.println("hypernyms of " + word + ": ");
            PointerTargetNodeList hypernymsW = (PointerTargetNodeList)hypernymsIt.next();
            hypernymsW.print();
        }
        
        DirectedSparseGraph dag = new DirectedSparseGraph();
        
        // add each keyword to graph
        for (int i = 0; i < KEYWORDS.length; i++) {
            Vertex v = new SparseVertex();
            v.setUserDatum("name", KEYWORDS[i], UserData.SHARED);
            dag.addVertex(v);
        }
    }
    
    public void initWordNet() {
        try {
			// initialize JWNL (this must be done before JWNL can be used)
			JWNL.initialize(new FileInputStream("/home/achim/Projects/studienarbeit/jwnl/file_properties.xml"));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
    }
    
    public List lookupWords() {
        List result = new Vector(KEYWORDS.length);
        for (int i = 0; i < KEYWORDS.length; i++) {
            try {                
                result.add(Dictionary.getInstance().lookupIndexWord(POS.NOUN, KEYWORDS[i]));
            } catch (JWNLException e) {
                System.out.println("lookup failed: " + KEYWORDS[i]);
            }
        }
        
        return result;
    }
    
    public List findHypernyms(List words) {
        List result = new LinkedList();
        for (Iterator iter = words.iterator(); iter.hasNext();) {
            IndexWord word = (IndexWord) iter.next();

            try {
                PointerTargetNodeList hypernyms = PointerUtils.getInstance().getDirectHypernyms(word.getSense(1));
                result.add(hypernyms);
            } catch (JWNLException e) {
                System.out.println("hypernym lookup failed: " + word);
            }
        }
        
        return result;
    }
}