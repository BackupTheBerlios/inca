/*
 * Created on Jan 17, 2005 3:57:58 PM
 */
package org.inca.util.text;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author achim
 */
public class HTMLTextExtractor extends TextExtractor {

    public HTMLTextExtractor(URL url) throws IOException {
        super(url);
    }

    public StringBuffer getText(URL url) {
        Pattern brTag = Pattern.compile("<br>|<br\\w?/>");
        Pattern htmlTags = Pattern.compile("<[^>]*>|</[^>]*>");
        
        Matcher matcher = brTag.matcher(content);
        matcher.replaceAll(NL);

        matcher = htmlTags.matcher(content);
        matcher.replaceAll("");

        return content;
    }
}