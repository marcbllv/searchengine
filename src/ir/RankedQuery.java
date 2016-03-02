package ir;

import java.util.ArrayList;
import java.util.HashMap;

public class RankedQuery {

    /**
     * Returns a PostingsList of relevant articles ranked by score
     */
    public static PostingsList rankByScore(ArrayList<PostingsList> lists, int rankingType) {

        HashMap<PostingsEntry, Double> docScores = null;

        // Get score for each doc
        switch(rankingType) {
            case Index.TF_IDF:
                docScores = RankedQuery.tfidfScoring(lists);
                break;
            case Index.PAGERANK:
            case Index.COMBINATION:
                return null;
        }

        // Sort scores by value now:
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

    private static HashMap<PostingsEntry, Double> tfidfScoring(ArrayList<PostingsList> lists) {

        HashMap<PostingsEntry, Double> sum    = new HashMap<PostingsEntry, Double>();
        HashMap<PostingsEntry, Double> sum2   = new HashMap<PostingsEntry, Double>();
        HashMap<PostingsEntry, Double> docScores = new HashMap<PostingsEntry, Double>();
        Double tfidf, w, w2;

        for(int i = 0 ; i < lists.size() ; i++) {
            for(PostingsEntry pe : lists.get(i).list) {
                tfidf = pe.tfidf(lists.get(i).size());
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

}
