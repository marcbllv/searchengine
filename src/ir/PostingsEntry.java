/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    public double score_tfidf;
    public double score_pagerank;
    public LinkedList<Integer> offsets;

    PostingsEntry() {
        this.offsets = new LinkedList<Integer>();
    }

    PostingsEntry(int docID) {
        this.docID = docID;
        this.offsets = new LinkedList<Integer>();
    }

    PostingsEntry(int docID, double score) {
        this.docID = docID;
        this.offsets = new LinkedList<Integer>();
    }

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
        return Double.compare( other.score, score );
    }

    @Override
    public boolean equals(Object e) {
        return ((PostingsEntry)e).docID == this.docID;
    }

    @Override
    public int hashCode() {
        return this.docID;
    }

    public int size() {
        return this.offsets.size();
    }

    public int get(int i) {
        return this.offsets.get(i);
    }

    public void println() {
        System.out.print(this.docID + ":");
        for(int o : this.offsets) {
            System.out.print(o + ",");
        }
        System.out.println();
    }

    public double tfidf(int docCount) {
        int tf = this.size();
        double idf = Math.log( Index.docIDs.size() / docCount );
        double docLength = Index.docLengths.get(((Integer)this.docID).toString()); 

        return tf * idf / docLength;
    }
}

    
