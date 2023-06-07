/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;
import java.nio.charset.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *  A class for representing a query as a list of words, each of which has
 *  an associated weight.
 */
public class Query {

    /**
     *  Help class to represent one query term, with its associated weight. 
     */
    class QueryTerm {
        String term;
        double weight;
        QueryTerm( String t, double w ) {
            term = t;
            weight = w;
        }
    }

    /** 
     *  Representation of the query as a list of terms with associated weights.
     *  In assignments 1 and 2, the weight of each term will always be 1.
     */
    public ArrayList<QueryTerm> queryterm = new ArrayList<QueryTerm>();

    /**  
     *  Relevance feedback constant alpha (= weight of original query terms). 
     *  Should be between 0 and 1.
     *  (only used in assignment 3).
     */
    double alpha = 0.2;
    /**  
     *  Relevance feedback constant beta (= weight of query terms obtained by
     *  feedback from the user). 
     *  (only used in assignment 3).
     */
    double beta = 1 - alpha;
    
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
            queryterm.add( new QueryTerm(tok.nextToken(), 1.0) );
        }    
    }
    
    
    /**
     *  Returns the number of terms
     */
    public int size() {
        return queryterm.size();
    }
    
    
    /**
     *  Returns the Manhattan query length
     */
    public double length() {
        double len = 0;
        for ( QueryTerm t : queryterm ) {
            len += t.weight; 
        }
        return len;
    }

    /**
     *  Returns a copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        for ( QueryTerm t : queryterm ) {
            queryCopy.queryterm.add( new QueryTerm(t.term, t.weight) );
        }
        return queryCopy;
    }

    public void normalize() {

        for (int i = 0; i < size(); i++) {
            queryterm.get(i).weight /= size();
        }
    }

    /**
     *  Expands the Query using Relevance Feedback
     *
     *  @param results The results of the previous query.
     *  @param docIsRelevant A boolean array representing which query results the user deemed relevant.
     *  @param engine The search engine object
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Engine engine ) {
        //
        //  YOUR CODE HERE
        //

        // System.out.println(results);

        normalize();
        // get relevance index
        ArrayList<Integer> relevantIndices = new ArrayList<>();
        for (int i = 0; i < docIsRelevant.length; i++) {
            if (docIsRelevant[i]) relevantIndices.add(i);
        }

        // no relevance feedback
        if (relevantIndices.isEmpty()) return;

        HashMap<String, Integer> tknToIdx = new HashMap<>();
        // System.out.println("size:"+size());
        // Multiply q0 by alpha and its idf
        for (int i = 0; i < size(); i++) {
            // System.out.println("term"+queryterm.get(i).term);
            queryterm.get(i).weight *= alpha;
            // queryterm.get(i).weight *= Math.log( engine.index.docLengths.size() * 1.0 / engine.index.getPostings(queryterm.get(i).term).size());
            tknToIdx.put(queryterm.get(i).term, i);
        }
        try {
            for (int i: relevantIndices) {

                ArrayList<QueryTerm> di = new ArrayList<>();

                // Read each file and get each token
                int docID = results.get(i).docID;
                String fileName = engine.index.docNames.get(docID);
                // System.out.println("filename"+fileName);
                Reader reader = new InputStreamReader( new FileInputStream(new File(fileName)), StandardCharsets.UTF_8 );
                Tokenizer tok = new Tokenizer( reader, true, false, true, engine.patterns_file );
                ArrayList<String> tokens = new ArrayList<String>();
                HashMap<String, Integer> tokenCnt = new HashMap<String, Integer>();
                // For each relevant document, the file is read and tokens are extracted using a Tokenizer.
                // Each token is stored in the di ArrayList along with a weight of 1.0.
                while ( tok.hasMoreTokens() ) {
                    String token = tok.nextToken();
                    // System.out.println("token"+token);
                    di.add(new QueryTerm(token, 0.0));
                    if (!tokens.contains(token)) {
                        tokens.add(token);
                        tokenCnt.put(token, 1);
                    } else {
                        int occ = tokenCnt.get(token);
                        occ++;
                        tokenCnt.put(token, occ);
                    }
                }

                // Calculate score for each token
                for (int j = 0; j < tokens.size(); j++) {
                    String token = tokens.get(j);
                    int df = engine.index.getPostings(token).size();
                    double idf = Math.log( engine.index.docNames.size() / (double)df);
                    int tf =  tokenCnt.get(token);
                    double tf_idf = idf * tf / engine.index.docLengths.get(docID);
                    di.get(j).weight += tf_idf;
                    di.get(j).weight *= (beta / relevantIndices.size());
                }

                // Filter tokens and merge scores for same tokens
                for (int j = 0; j < di.size(); j++) {
                    QueryTerm qt = di.get(j);
                    // System.out.println(j + " " + qt.term);
                    if (!tknToIdx.containsKey(qt.term)) {
                        queryterm.add(qt);
                        tknToIdx.put(qt.term, size() - 1);
                    } else {
                        queryterm.get(tknToIdx.get(qt.term)).weight += qt.weight;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


