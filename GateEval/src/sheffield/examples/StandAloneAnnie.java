package sheffield.examples;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.GateConstants;
import gate.Node;
import gate.ProcessingResource;
import gate.annotation.NodeImpl;
import gate.creole.ANNIEConstants;
import gate.creole.ANNIETransducer;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.Out;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class StandAloneAnnie {

    private SerialAnalyserController annieController;

    public void initAnnie() throws GateException, MalformedURLException {
        Out.prln("Initialising ANNIE...");

        annieController = (SerialAnalyserController) Factory.createResource(
                "gate.creole.SerialAnalyserController",
                Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_"
                        + Gate.genSym());

        for (int i = 0; i < ANNIEConstants.PR_NAMES.length; i++) {
            FeatureMap params = Factory.newFeatureMap();

/*
            if (ANNIEConstants.PR_NAMES[i]
                    .equals("gate.creole.ANNIETransducer")) {
                Out.prln("set jape rule url");
                params.put(ANNIETransducer.TRANSD_GRAMMAR_URL_PARAMETER_NAME,
                           new URL("file:///home/achim/Projects/workspace/GateEval/src/nouns.jape"));
            }
*/
            ProcessingResource pr = (ProcessingResource) Factory
                    .createResource(ANNIEConstants.PR_NAMES[i], params);

            annieController.add(pr);
        }

        Out.prln("...ANNIE loaded");
    }

    public void setCorpus(Corpus corpus) {
        annieController.setCorpus(corpus);
    }

    public void execute() throws GateException {
        Out.prln("Running ANNIE...");
        annieController.execute();
        Out.prln("...ANNIE complete");
    }

    public static void main(String args[]) throws GateException, IOException {
        System.setProperty("gate.home",
                "/home/achim/Projects/studienarbeit/gate");
        Out.prln("Initialising GATE...");
        Gate.init();
        Out.prln("...GATE initialised");

        StandAloneAnnie annie = new StandAloneAnnie();
        annie.initAnnie();

        Corpus corpus = (Corpus) Factory
                .createResource("gate.corpora.CorpusImpl");
        for (int i = 0; i < args.length; i++) {
            URL u = new URL(args[i]);
            FeatureMap params = Factory.newFeatureMap();
            params.put(Document.DOCUMENT_URL_PARAMETER_NAME, u);
            params.put(Document.DOCUMENT_PRESERVE_CONTENT_PARAMETER_NAME,
                    new Boolean(true));
            params.put(Document.DOCUMENT_REPOSITIONING_PARAMETER_NAME,
                    new Boolean(true));
            Out.prln("Creating doc for " + u);
            Document doc = (Document) Factory.createResource(
                    "gate.corpora.DocumentImpl", params);
            corpus.add(doc);
        }

        annie.setCorpus(corpus);
        annie.execute();

        Iterator iter = corpus.iterator();

        while (iter.hasNext()) {
            Document doc = (Document) iter.next();
            AnnotationSet defaultAnnotSet = doc.getAnnotations();
            
            String originalContent = (String) doc.getFeatures().get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
            
            Set annotTypesRequired = new HashSet();
            annotTypesRequired.add("Person");
            AnnotationSet people = defaultAnnotSet.get(annotTypesRequired);

            FeatureMap features = Factory.newFeatureMap();
            features.put("category", "NN");
            
            for (Iterator iterator = people.iterator(); iterator.hasNext();) {
                Annotation element = (Annotation) iterator.next();
                FeatureMap f = element.getFeatures();
                
                Node startNode = element.getStartNode();
                Node endNode = (NodeImpl) element.getEndNode();
               
                String person = originalContent.substring((int)startNode.getOffset().longValue(), (int)endNode.getOffset().longValue());
                System.out.println(person);
            }

            AnnotationSet annotSet = defaultAnnotSet.get("Token", features);
            Hashtable nounFrequency = new Hashtable();

            for (Iterator iterator = annotSet.iterator(); iterator.hasNext();) {
                Annotation element = (Annotation) iterator.next();
                FeatureMap annotFeatures = element.getFeatures();
                String noun = (String) annotFeatures.get("string");

                Integer freq = (Integer) nounFrequency.get(noun);
                if (null == freq) {
                    nounFrequency.put(noun, new Integer(1));
                } else {
                    nounFrequency.put(noun, new Integer(freq.intValue() + 1));
                }
            }

            SortedSet entries = new TreeSet(
                    annie.new ReverseEntryValueComparator());
            entries.addAll(nounFrequency.entrySet());

            Out.prln(entries);
            
//            String xmlDoc = doc.toXml(annotSet, true);
//            Out.print(xmlDoc);

            //            FeatureMap features = doc.getFeatures();
            //            String originalContent = (String) features
            //                    .get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
            //            RepositioningInfo info = (RepositioningInfo) features
            //                    .get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);
        }
    }

    public class ReverseEntryValueComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return compare((Map.Entry) o2, (Map.Entry) o1);
        }

        public int compare(Map.Entry e1, Map.Entry e2) {
            int cf = ((Comparable) e1.getValue()).compareTo(e2.getValue());
            if (cf == 0) {
                cf = ((Comparable) e1.getKey()).compareTo(e2.getKey());
            }
            return cf;
        }
    }
}