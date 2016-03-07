/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    /* --------------------------------------------- */


    public PageRank( String filename ) {
        int noOfDocs = readDocs( filename );
        boolean montecarlo = false;

        if(montecarlo) {
            monteCarloPagerank(noOfDocs);
        } else { 
            computePagerank( noOfDocs );
        }
    }


    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
        int fileIndex = 0;
        try {
            System.err.print( "Reading file... " );
            BufferedReader in = new BufferedReader( new FileReader( filename ));
            String line;
            while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
                int index = line.indexOf( ";" );
                String title = line.substring( 0, index );
                Integer fromdoc = docNumber.get( title );
                //  Have we seen this document before?
                if ( fromdoc == null ) {	
                    // This is a previously unseen doc, so add it to the table.
                    fromdoc = fileIndex++;
                    docNumber.put( title, fromdoc );
                    docName[fromdoc] = title;
                }
                // Check all outlinks.
                StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
                while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
                    String otherTitle = tok.nextToken();
                    Integer otherDoc = docNumber.get( otherTitle );
                    if ( otherDoc == null ) {
                        // This is a previousy unseen doc, so add it to the table.
                        otherDoc = fileIndex++;
                        docNumber.put( otherTitle, otherDoc );
                        docName[otherDoc] = otherTitle;
                    }
                    // Set the probability to 0 for now, to indicate that there is
                    // a link from fromdoc to otherDoc.
                    if ( link.get(fromdoc) == null ) {
                        link.put(fromdoc, new Hashtable<Integer,Boolean>());
                    }
                    if ( link.get(fromdoc).get(otherDoc) == null ) {
                        link.get(fromdoc).put( otherDoc, true );
                        out[fromdoc]++;
                    }
                }
            }
            if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
                System.err.print( "stopped reading since documents table is full. " );
            }
            else {
                System.err.print( "done. " );
            }
            // Compute the number of sinks.
            for ( int i=0; i<fileIndex; i++ ) {
                if ( out[i] == 0 )
                    numberOfSinks++;
            }
        }
        catch ( FileNotFoundException e ) {
            System.err.println( "File " + filename + " not found!" );
        }
        catch ( IOException e ) {
            System.err.println( "Error reading file " + filename );
        }
        System.err.println( "Read " + fileIndex + " number of documents" );
        return fileIndex;
    }

    /*
     *   Computes the pagerank of each document.
     */
    void computePagerank( int numberOfDocs ) {
        Double[] p = new Double[numberOfDocs];
        Double[] pPrev = new Double[numberOfDocs];
        p[0] = 1.0;
        pPrev[0] = 1.0;

        double c = 1 - PageRank.BORED;

        for(int i = 1 ; i < numberOfDocs ; i++) {
            p[i] = 0.0;
        }

        for(int n = 0 ; n < PageRank.MAX_NUMBER_OF_ITERATIONS ; n++) {
            System.out.print("It. " + (n+1));
            for(int i = 0 ; i < numberOfDocs ; i++) {
                pPrev[i] = p[i];
            }

            for(int i = 0 ; i < numberOfDocs ; i++) {
                p[i] = 0.0;
                
                for(int j = 0 ; j < numberOfDocs ; j++) {
                    if(this.link.get(j) == null) {
                        p[i] += c * pPrev[j] / numberOfDocs;
                    } else {
                        if(this.link.get(j).get(i) != null) {
                            p[i] += c * pPrev[j] / this.out[j];
                        }
                    }
                }

                p[i] += (1 - c) / numberOfDocs;
            }

            // Test convergence
            double s = 0.0;
            for(int i = 0 ; i < numberOfDocs ; i++) {
                s += (p[i] - pPrev[i]) * (p[i] - pPrev[i]);
            }
            System.out.println(": " + s);

            if(s < PageRank.EPSILON * PageRank.EPSILON) {
                break;
            }
        }

        // Sorting
        ArrayIndexComparator comp = new ArrayIndexComparator(p);
        Integer[] idx = comp.createIndexArray();
        Arrays.sort(idx, comp);

        System.out.println();
        for(int i = 1 ; i <= 25 ; i++) {
            System.out.println(i + ": " + docName[idx[numberOfDocs - i]] + " " + p[idx[numberOfDocs - i]]);
        }
    }


    void monteCarloPagerank( int numberOfDocs ) {
        // Random walk parameters
        int N = 100 * numberOfDocs;
        double c = 1 - PageRank.BORED;
       
        // Various counters for PR computation
        int[] count = new int[numberOfDocs];
        for(int i = 0 ; i < numberOfDocs ; i++) {
            count[i] = 0;
        }
        int pageCount = 0;

        Double[] p = new Double[numberOfDocs];
        for(int i = 0 ; i < numberOfDocs ; i++) {
            p[i] = 0.0;
        }

        Random r = new Random();

        for(int n = 0 ; n < N ; n++) {
            // Starting random walk from page
            int page = r.nextInt(numberOfDocs);
            count[page]++;
            pageCount++;

            // Iterate while not bored
            while(r.nextDouble() > 1 - c) {

                Integer[] reachable = new Integer[0];
                Hashtable<Integer, Boolean> outlinks = new Hashtable<Integer, Boolean>();
                int any;
                Double rateJump;

                if((outlinks = this.link.get(page)) == null) {
                    // Dangling node: end this walk
                    break;
                } else {
                    // Choose amoung random outlinks
                    reachable = this.link.get(page).keySet().toArray(reachable);
                    page = reachable[r.nextInt(reachable.length)];
                }
                count[page]++;
                pageCount++;
            }
        }

        // Normalization
        for(int i = 0 ; i < numberOfDocs ; i++) {
            p[i] = (double)(count[i]) / (double)pageCount;
        }

        // Sorting & displaying results
        ArrayIndexComparator comp = new ArrayIndexComparator(p);
        Integer[] idx = comp.createIndexArray();
        Arrays.sort(idx, comp);

        for(int i = 1 ; i <= 50 ; i++) {
            System.out.println(i + ": " + docName[idx[numberOfDocs - i]] + " " + p[idx[numberOfDocs - i]]);
        }
    }

    private static boolean converged(Double[] p1, Double[] p2, double epsilon) {
        if(p1.length != p2.length) {
            return false;
        }

        double s = 0.0, v2 = 0.0;
        for(int i = 0 ; i < p1.length ; i++) {
            s += (p1[i] - p2[i]) * (p1[i] - p2[i]);
        }

        return s < epsilon * epsilon;
    }

    private class ArrayIndexComparator implements Comparator<Integer> {
        private final Double[] array;

        public ArrayIndexComparator(Double[] a) {
            this.array = a;
        }

        public Integer[] createIndexArray() {
            Integer[] indexes = new Integer[array.length];
            for (int i = 0; i < array.length; i++)
            {
                indexes[i] = i; // Autoboxing
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2) {
            return array[index1].compareTo(array[index2]);
        }
    }

    public static void main( String[] args ) {
        if ( args.length != 1 ) {
            System.err.println( "Please give the name of the link file" );
        }
        else {
            new PageRank( args[0] );
        }
    }
}
