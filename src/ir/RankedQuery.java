package ir;

import java.util.ArrayList;
import java.util.HashMap;

public class RankedQuery {

    private static final double prVStfidf = 0.5;

    /**
     * Returns a PostingsList of relevant articles ranked by score
     */
    public static PostingsList rankByScore(ArrayList<PostingsList> lists, int rankingType) {

        HashMap<PostingsEntry, Double> docScores = null;

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
        PostingsList finalList = new PostingsList();
        while(docScores.size() > 0) {
            Double maxVal = 0.0;
            PostingsEntry maxPe = new PostingsEntry();

            for(HashMap.Entry<PostingsEntry, Double> e : docScores.entrySet()) {
                if(e.getValue() > maxVal) {
                    maxVal = e.getValue();
                    maxPe  = e.getKey();
                }
            }

            finalList.list.addFirst(maxPe);
            docScores.remove(maxPe);
        }

        return finalList;
    }

    private static HashMap<PostingsEntry, Double> tfidf(ArrayList<PostingsList> lists) {
        HashMap<PostingsEntry, Double> sum    = new HashMap<PostingsEntry, Double>();
        HashMap<PostingsEntry, Double> sum2   = new HashMap<PostingsEntry, Double>();
        HashMap<PostingsEntry, Double> docScores = new HashMap<PostingsEntry, Double>();
        Double tfidf, w, w2;

        for(int i = 0 ; i < lists.size() ; i++) {
            for(PostingsEntry pe : lists.get(i).list) {
                tfidf = pe.score_tfidf;
                pe.score = tfidf;

                if((w = sum.get(pe)) == null) {
                    sum.put(pe, tfidf);
                    sum2.put(pe, tfidf * tfidf);
                } else {
                    w2 = sum2.get(pe);
                    sum.put(pe, w + tfidf);
                    sum2.put(pe, w2 + tfidf * tfidf);
                }
            }
        }

        for(HashMap.Entry<PostingsEntry, Double> e : sum.entrySet()) {
            docScores.put(e.getKey(), e.getValue() / sum2.get(e.getKey()));
        }

        return docScores;
    }

    private static HashMap<PostingsEntry, Double> pageRank(ArrayList<PostingsList> lists) {
        HashMap<PostingsEntry, Double> pr = new HashMap<PostingsEntry, Double>();
        for(PostingsList pl: lists) {
            for(PostingsEntry pe: pl.list) {
                pe.score = HashedIndex.pageRanks.get(pe.docID);
                pr.put(pe, pe.score);
            }
        }

        System.out.println(pr.size());

        return pr;
    }

    private static HashMap<PostingsEntry, Double> combination(ArrayList<PostingsList> lists, double a) {
        HashMap<PostingsEntry, Double> pr = RankedQuery.pageRank(lists);
        HashMap<PostingsEntry, Double> tfidf = RankedQuery.tfidf(lists);
        HashMap<PostingsEntry, Double> comb = new HashMap<PostingsEntry, Double>();

        // Checking values for a
        // a = 0: full TFIDF
        // a = 1: full pagerank
        if(a < 0) {
            a = 0;
        } else if(a > 1) {
            a = 1;
        }

        for(PostingsEntry pe: pr.keySet()) {
            pe.score = a * tfidf.get(pe) + (1 - a) * pr.get(pe);
            comb.put(pe, pe.score);
        }

        return comb;
    }

}
