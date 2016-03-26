package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class RankedQuery {

    private static final double prVStfidf = 0.5;

    /**
     * Returns a PostingsList of relevant articles ranked by score
     */
    public static PostingsList rankByScore(ArrayList<PostingsList> lists, int rankingType) {

        PostingsList docScores = null;

        // Get score for each doc
        switch(rankingType) {
            case Index.TF_IDF:
                docScores = RankedQuery.tfidf(lists);
                break;
            case Index.PAGERANK:
                docScores = RankedQuery.pageRank(lists);
                break;
            case Index.COMBINATION:
                docScores = RankedQuery.combination(lists, prVStfidf);
        }

        // Sort PostingsEntries by score now:
        Collections.sort(docScores.list);

        return docScores;
    }

    private static PostingsList tfidf(ArrayList<PostingsList> lists) {
        HashMap<Integer, Double> sum    = new HashMap<Integer, Double>();
        HashMap<Integer, Double> sum2   = new HashMap<Integer, Double>();
        PostingsList docScores = new PostingsList();
        Double tfidf, w, w2;

        // Computing cosine similarity with query
        for(int i = 0 ; i < lists.size() ; i++) {
            for(PostingsEntry pe : lists.get(i).list) {
                tfidf = pe.tfidf(lists.get(i).list.size());

                if((w = sum.get(pe.docID)) == null) {
                    sum.put(pe.docID, tfidf);
                    sum2.put(pe.docID, tfidf * tfidf);
                } else {
                    w2 = sum2.get(pe.docID);
                    sum.put(pe.docID, w + tfidf);
                    sum2.put(pe.docID, w2 + tfidf * tfidf);
                }
            }
        }

        for(HashMap.Entry<Integer, Double> e : sum.entrySet()) {
            PostingsEntry pe = new PostingsEntry();
            pe.docID = e.getKey();
            pe.score = e.getValue() / Math.sqrt(Index.index.size() * sum2.get(e.getKey()));
            docScores.add(pe);
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

    private static PostingsList combination(ArrayList<PostingsList> lists, double a) {
        PostingsList tfidf = RankedQuery.tfidf(lists);

        // Checking values for a
        // a = 0: full TFIDF
        // a = 1: full pagerank
        if(a < 0) {
            a = 0;
        } else if(a > 1) {
            a = 1;
        }

        // Updating scores taking PR into account
        Iterator<PostingsEntry> it = tfidf.list.iterator();
        while(it.hasNext()) {
            it.next().score = a * it.next().score + (1 - a) * HashedIndex.pageRanks.get(it.next().docID);
        }

        return tfidf;
    }

}
