/*
 * Created on Mar 10, 2005 12:57:40 PM
 */
package org.inca.categoriser;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.inca.odp.ie.PlaintextConverter;
import org.inca.odp.ie.tagger.FrequencyTagFilter;
import org.inca.odp.ie.tagger.TagFilter;
import org.inca.odp.ie.tagger.TaggedLemma;
import org.inca.odp.ie.tagger.Tagger;
import org.inca.odp.ie.tagger.TaggerException;
import org.inca.odp.ie.tagger.TreeTagger;
import org.inca.util.db.DatabaseConnection;
import org.inca.util.net.ResourceNotFoundException;

/**
 * @author achim
 */
public class DefaultCategoriser extends Categoriser {
    public DefaultCategoriser(URL url) {
        super(url);
    }

    private void getCategoriesForWord(String word) throws SQLException {
        DatabaseConnection connection = DatabaseConnection.getSharedConnection();

        String selectStmt = "SELECT url,freq,wordCount FROM links,word2link "
            + "INNER JOIN words ON word2link.wordId=words.id "
            + "WHERE words.word=? AND links.id=word2link.linkId "
            + "ORDER BY wordCount/freq";
        
        PreparedStatement stmt = connection.createPrepared(selectStmt);
        stmt.setEscapeProcessing(true);     
        stmt.setString(1, word);
        stmt.execute();
                
        ResultSet rs = stmt.getResultSet();
        
        while ( rs.next() ) {
//            _wordFreq.put(word, new Integer(rs.getInt("freq") ) );
        }
    }

    public String getCategory() throws ResourceNotFoundException,
            TaggerException, IOException {
        PlaintextConverter pc = new PlaintextConverter(_url);
        Tagger tagger = new TreeTagger(pc.getPlaintext());        
        _tags = tagger.getTags();
        TagFilter tagFilter = new FrequencyTagFilter(_tags);
        List filteredTags = tagFilter.getFilteredTags();

        for (Iterator iter = filteredTags.iterator(); iter.hasNext();) {
            Map.Entry e = (Map.Entry) iter.next();

            TaggedLemma tl = (TaggedLemma) e.getKey();
            int freq = ((Integer) e.getValue() ).intValue();
        }

        return null;
    }
}