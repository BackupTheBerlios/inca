/*
 * Created on Feb 22, 2005 5:44:15 PM
 */
package org.inca.odp.ie.tagger;

public class TaggedLemma implements Comparable {
    private String _lemma;
    private String _tag;

    public TaggedLemma(String lemma, String tag) {
        this._lemma = lemma;
        this._tag = tag;
    }

    public String getLemma() {
        return _lemma;
    }

    public String getTag() {
        return _tag;
    }

    public boolean equals(Object o) {
        if (o instanceof TaggedLemma) {
            TaggedLemma tl = (TaggedLemma) o;
            return _lemma.equals(tl.getLemma() ) && _tag.equals(tl.getTag() );
        } else {
            throw new ClassCastException();
        }
    }

    public int hashCode() {
        return (_lemma + _tag).hashCode();
    }

    public int compareTo(Object o) {
        if (o instanceof TaggedLemma) {
            TaggedLemma tl = (TaggedLemma) o;
            
            int result  = _lemma.compareTo(tl.getLemma() );
            
            if (result == 0) {
                result = _tag.compareTo(tl.getTag() );
            }
            
            if (result > 0) {
                return 1;
            } else if (result < 0) {
                return -1;
            } else {
               	return 0;
            }
        } else {
            throw new ClassCastException();
        }
    }
}