/*
 * Created on Feb 21, 2005 6:14:24 PM
 */
package org.inca.odp.ie.datasources;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.log4j.Logger;

public class HTMLPlaintextExtractor extends PlaintextExtractor {
    private static Logger logger = Logger.getLogger(HTMLPlaintextExtractor.class);
    public HTMLPlaintextExtractor(String data) {
        super(data);
    }

    private class HTMLHandler extends ParserCallback {
	    private Hashtable _ignoredTags = new Hashtable();
	    private Hashtable _textTags = new Hashtable();
	    private StringBuffer _text = null;
	    private boolean _inBody = false;
	    private boolean _ignoredTag = false;
	    private HTML.Tag _currentIgnoredTag = null;
	    private HTML.Tag _currentTag;
	
	    private int _depth = 0;
	
	    public HTMLHandler() {
	        super();
	        _text = new StringBuffer();
	        _ignoredTags.put(HTML.Tag.A, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.APPLET, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.CODE, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.COMMENT, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.FORM, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.MENU, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.OBJECT, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.SELECT, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.SCRIPT, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.STYLE, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.AREA, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.INPUT, new Boolean(true));
	        _ignoredTags.put(HTML.Tag.MAP, new Boolean(true));
	        
	        _textTags.put(HTML.Tag.H1, new Boolean(true));
	        _textTags.put(HTML.Tag.H2, new Boolean(true));
	        _textTags.put(HTML.Tag.H3, new Boolean(true));
	        _textTags.put(HTML.Tag.H4, new Boolean(true));
	        _textTags.put(HTML.Tag.H5, new Boolean(true));
	        _textTags.put(HTML.Tag.H6, new Boolean(true));
	        _textTags.put(HTML.Tag.B, new Boolean(true));
	        _textTags.put(HTML.Tag.BIG, new Boolean(true));
	        _textTags.put(HTML.Tag.BLOCKQUOTE, new Boolean(true));
	        _textTags.put(HTML.Tag.CAPTION, new Boolean(true));
	        _textTags.put(HTML.Tag.CITE, new Boolean(true));
	        _textTags.put(HTML.Tag.DD, new Boolean(true));
	        _textTags.put(HTML.Tag.DFN, new Boolean(true));
	        _textTags.put(HTML.Tag.DT, new Boolean(true));
	        _textTags.put(HTML.Tag.EM, new Boolean(true));
	        _textTags.put(HTML.Tag.LI, new Boolean(true));
	        _textTags.put(HTML.Tag.P, new Boolean(true));
	        _textTags.put(HTML.Tag.PRE, new Boolean(true));
	        _textTags.put(HTML.Tag.SAMP, new Boolean(true));
	        _textTags.put(HTML.Tag.STRONG, new Boolean(true));
	        _textTags.put(HTML.Tag.TD, new Boolean(true));
	        _textTags.put(HTML.Tag.TH, new Boolean(true));
	        _textTags.put(HTML.Tag.TT, new Boolean(true));
	        
	    }
	    
	    private void printIndent(String s) {
	        for (int i = 0; i < _depth; i++) {
	            logger.debug(" ");
	        }
	        logger.debug(s);
	    }
	    
	    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
	        _currentTag = t;
	        if (t.equals(HTML.Tag.BODY) ) {
	            _inBody = true;
	        }
	        
	        if (_ignoredTags.containsKey(t) && !_ignoredTag) {
	            _currentIgnoredTag = t;
	            _ignoredTag = true;
	            if (logger.isDebugEnabled() ) {
	                printIndent("ignored node: " + t.toString());
	            }
	        } else if ( _ignoredTag ){
	            if (logger.isDebugEnabled() ) {
	                printIndent("ignored: " + t.toString());
	            }
	        } else {
	            if (logger.isDebugEnabled() ) {
	                printIndent("tag: " + t.toString() );
	            }
	        }
	        _depth++;            
	    }
	    
	    public void handleEndTag(HTML.Tag t, int pos) {
	        _depth--;
	        if (t.equals(HTML.Tag.BODY) ) {
	            _inBody = false;
	        }
	
	        if ( t.equals(_currentIgnoredTag) && _ignoredTag ) {              
	            if (logger.isDebugEnabled() ) {
	                printIndent("end ignored node: " + t.toString() );
	            }
	            _ignoredTag = false;
	            _currentIgnoredTag = null;
	        } else if ( _ignoredTag ) {
	            if (logger.isDebugEnabled() ) {
	                printIndent("end ignored: " + t.toString());
	            }
	        } else {
	            if (logger.isDebugEnabled() ) {
	                printIndent("end tag: " + t.toString() );
	            }
	        }
	    }
	    
	    public void handleText(char[] data, int pos) {
	        if ( !_ignoredTag ) {
	            if (logger.isDebugEnabled() ) {
	                printIndent("text: " + new String(data));
	                printIndent("pos: " + pos);
	            }
	            _text.append(data);
	        }
	    }
	    
	    public StringBuffer getPlaintext() {
	        return _text;
	    }
    }

    public StringBuffer getPlaintext() throws IOException {
        HTMLHandler handler = new HTMLHandler();
        new ParserDelegator().parse(new StringReader(_data), handler, false);
        
        return handler.getPlaintext();
    }
}