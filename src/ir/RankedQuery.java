package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class RankedQuery {

    // balancing PR and TFIDF. Choose 1.0 for PR only / 0.0 for tfidf only
    private static final double prVStfidf = 0.95;

    public static HashMap<Integer, Double> docNorms;

    /**
     * Returns a PostingsList of relevant articles ranked by score
     */
    public static PostingsList rankByScore(Query query, ArrayList<PostingsList> lists, int rankingType) {

        PostingsList docScores = null;

        // Get score for each doc
        switch(rankingType) {
            case Index.TF_IDF:
                docScores = RankedQuery.tfidf(query, lists);
                break;
            case Index.PAGERANK:
                docScores = RankedQuery.pageRank(lists);
                break;
            case Index.COMBINATION:
                docScores = RankedQuery.combination(query, lists, prVStfidf);
        }

        // Sort PostingsEntries by score now:
        Collections.sort(docScores.list);

        return docScores;
    }

    private static PostingsList tfidf(Query query, ArrayList<PostingsList> lists) {
        HashMap<Integer, PostingsEntry> sum = new HashMap<Integer, PostingsEntry>();
        PostingsList docScores = new PostingsList();
        Double tfidf;
        PostingsEntry p;

        query.print();

        // Computing cosine similarity with respect to query terms weights
        for(int i = 0 ; i < lists.size() ; i++) {
            for(PostingsEntry pe : lists.get(i).list) {
                tfidf = pe.tfidf(lists.get(i).list.size());

                if((p = sum.get(pe.docID)) == null) {
                    p = new PostingsEntry();
                    p.docID = pe.docID;
                    p.tfidf_vect = new double[query.size()];
                    p.tfidf_vect[i] = tfidf;

                    p.score += tfidf * query.weights.get(i);

                    sum.put(pe.docID, p);
                } else {
                    p.tfidf_vect[i] = tfidf;
                }
            }
        }

        for(HashMap.Entry<Integer, PostingsEntry> e : sum.entrySet()) {
            e.getValue().score = e.getValue().score / (Math.sqrt(lists.size()) * RankedQuery.docNorms.get(e.getValue().docID));
            docScores.add(e.getValue());
        }

        return docScores;
    }

    private static PostingsList pageRank(ArrayList<PostingsList> lists) {
        PostingsList pr = new PostingsList();
        for(PostingsList pl: lists) {
            for(PostingsEntry pe: pl.list) {
                try {
                    pe.score = HashedIndex.pageRanks.get(pe.docID);
                } catch (Exception e) {
                    System.err.println("No page rank found for article " + pe.docID);
                    pe.score = 0.0;
                }
                pr.add(pe);
            }
        }

        return pr;
    }

    private static PostingsList combination(Query query, ArrayList<PostingsList> lists, double a) {
        PostingsList tfidf = RankedQuery.tfidf(query, lists);

        // Checking values for a
        if(a < 0) {
            a = 0;
        } else if(a > 1) {
            a = 1;
        }

        // Updating scores taking PR into account
        Iterator<PostingsEntry> it = tfidf.list.iterator();
        while(it.hasNext()) {
            it.next().score = (1 - a) * it.next().score + a * HashedIndex.pageRanks.get(it.next().docID);
        }

        return tfidf;
    }

}
