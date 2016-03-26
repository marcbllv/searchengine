/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.Comparator;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.Files;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    private boolean scanFiles = false;

    /** How many files in case of saving the index on hard drive */
    public static final int INDEX_SAVE_N_FILES = 1000;

    public static HashMap<Integer, Double> pageRanks = new HashMap<Integer, Double>();

    public int countDoc = 0;

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {

        if(docID > 100000)
            return;
        
        if(docID % 100 == 0)
            System.out.println(docID);

        PostingsList pl;

        if(( pl = index.get(token)) == null) {
            pl = new PostingsList();
        }

        pl.add(docID, offset);

        Index.index.put(token, pl);
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	    return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        return index.get(token);
    }

    /**
     * Compute tfidf for all PostingsEntry in the index
     */
    public static void computeAllTfidf() {
        int i = 0;
        for(PostingsList pl: Index.index.values()) {
            for(PostingsEntry pe: pl.list) {
                pe.score_tfidf = pe.tfidf(pl.list.size());
            }
            i++;
        }
    }

    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
        ArrayList<PostingsList> responses = new ArrayList<PostingsList>(query.size());

        responses = new ArrayList<PostingsList>(query.size());
        PostingsList l;
        for(int i = 0 ; i < query.size() ; i++) {
            if((l = index.get(query.terms.get(i))) == null) {
                l = new PostingsList();
            }
            responses.add(l);
        }

        switch(queryType) {
            case Index.INTERSECTION_QUERY:
            case Index.PHRASE_QUERY:
                return IntersectionQuery.intersectResponses(responses, queryType);
            case Index.RANKED_QUERY:
                return RankedQuery.rankByScore(responses, rankingType);
        }

        return null;
    }

    public void cleanup() {

        int nFiles = 100;

        PrintWriter writer;

        try {
            FileWriter f;
            PostingsList pl;
            PostingsEntry pe;
            int s;
            int sOffsets;

            // Saving document names
            f = new FileWriter("savedindex/__docnames__");
            writer = new PrintWriter(new BufferedWriter(f));
            for(Map.Entry<String, String> e : Index.docIDs.entrySet()) {
                writer.print(e.getKey());
                writer.print("|");
                writer.println(e.getValue());
            }
            writer.close();

            // Saving document lengths
            f = new FileWriter("savedindex/__doclengths__");
            writer = new PrintWriter(new BufferedWriter(f));
            for(Map.Entry<String, Integer> e : Index.docLengths.entrySet()) {
                writer.print(e.getKey());
                writer.print("|");
                writer.println(e.getValue());
            }
            writer.close();

            // Saving postings lists & scores
            for(Map.Entry<String, PostingsList> e : Index.index.entrySet()) {
                String key = e.getKey();
                PostingsList val = e.getValue();

                int h = key.hashCode();
                String fileName = ((Integer)Math.abs(h % INDEX_SAVE_N_FILES)).toString();
                f = new FileWriter("savedindex/" + fileName, true);
                writer = new PrintWriter(new BufferedWriter(f));

                s = val.size();
                writer.print(key + "|");
                for(int k = 0 ; k < s ; k++) {
                    pe = val.get(k);
                    writer.print(pe.docID + ":");
                    writer.print(pe.score_tfidf + ":");

                    sOffsets = pe.size();
                    for(int j = 0 ; j < sOffsets ; j++) {
                        writer.print(pe.get(j));

                        if(j < sOffsets - 1) {
                            writer.print(",");
                        }
                    }

                    if(k < s - 1) {
                        writer.print(";");
                    }
                }
                writer.println();
                writer.close();
            }


        } catch(Exception e) {
            System.err.println(e.getMessage());
        }

    }

    public void scanFilesTrue() {
        this.scanFiles = true;
    }
}
