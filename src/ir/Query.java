/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;

public class Query {

    public static final double alpha = 1.0;
    public static final double beta  = 0.8;
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
	StringTokenizer tok = new StringTokenizer( queryString );
	while ( tok.hasMoreTokens() ) {
	    terms.add( tok.nextToken() );
	    weights.add( new Double(1) );
	}    
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
	Query queryCopy = new Query();
	queryCopy.terms = (LinkedList<String>) terms.clone();
	queryCopy.weights = (LinkedList<Double>) weights.clone();
	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
        ArrayList<Double> relevantSum = new ArrayList<Double>(this.size());
        Double s;
        int relevantCount = 0;

        for(int i = 0 ; i < 10 ; i++) {
            relevantCount += docIsRelevant[i] ? 1 : 0;
        }

        for(int i = 0 ; i < 10 ; i++) {
            if(results.get(i) == null) {
                break; // less than 10 results
            }

            if(docIsRelevant[i]) {
                for(int j = 0 ; j < this.size() ; j++) {
                    this.weights.set(j, Query.alpha * this.weights.get(j) + Query.beta * results.get(i).tfidf_vect[j] / relevantCount);
                }
            }
        }
    }
}

    
