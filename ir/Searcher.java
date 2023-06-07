/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, 2017
 */

package ir;
import pagerank.PageRank;

import java.util.*;

import static ir.QueryType.*;

/**
 *  Searches an index for results of a query.
 */
public class Searcher {

    /** The index to be searched by this Searcher. */
    Index index;

    /** The k-gram index to be searched by this Searcher */
    KGramIndex kgIndex;

    PageRank pagerank;

    /** Constructor */
    public Searcher( Index index, KGramIndex kgIndex ) {
        this.index = index;
        this.kgIndex = kgIndex;
        pagerank = new PageRank("./pagerank/linksDavis.txt");
    }



    /** How much the tfidf weigths during ranked query */
    private static final double RANK_WEIGHT = 0.1;

    /**
     *  Searches the index for postings matching the query.
     *  @return A postings list representing the result of the query.
     */

    public PostingsList search( Query query, QueryType queryType, RankingType rankingType, NormalizationType normType ) {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        /*
        for (int queryId = 0; queryId < query.size(); queryId++) {
            System.err.println(query.queryterm.get(queryId).term);
        }*/
        PostingsList result = new PostingsList();
        if (query.size() == 1 && queryType != RANKED_QUERY){
            result = this.index.getPostings(query.queryterm.get(0).term);
        }
        else{
            if (queryType == INTERSECTION_QUERY){
                result = this.index.getPostings(query.queryterm.get(0).term);
                for (int queryId = 1; queryId < query.size(); queryId++) {
                    PostingsList list = this.index.getPostings(query.queryterm.get(queryId).term);
                    result = IntersectionQuery(result, list);
                }
            }
            else if(queryType == PHRASE_QUERY){
                result = this.index.getPostings(query.queryterm.get(0).term);
                for (int queryId = 1; queryId < query.size(); queryId++) {
                    PostingsList list = this.index.getPostings(query.queryterm.get(queryId).term);
                    result = PhraseQuery(result, list);
                }
            }
            else if(queryType == RANKED_QUERY){
                result = rankedQuery(query, rankingType);
            }
        }
        return result;
    }

    private PostingsList IntersectionQuery(PostingsList p1, PostingsList p2){
        PostingsList answer = new PostingsList();
        int p1cnt = 0;
        int p2cnt = 0;
        while (p1cnt < p1.size() && p2cnt < p2.size()){
            if (p1.get(p1cnt).docID == p2.get(p2cnt).docID){
                answer.insertEntry(p1.get(p1cnt));
                p1cnt ++;
                p2cnt ++;
            }else if(p1.get(p1cnt).docID<p2.get(p2cnt).getDocID()) {
                p1cnt ++;
            }else{
                p2cnt++;
            }
        }
        return answer;
    }
    private PostingsList PhraseQuery(PostingsList p1, PostingsList p2){
        PostingsList answer = new PostingsList();
        int p1cnt = 0;
        int p2cnt = 0;
        while (p1cnt < p1.size() && p2cnt < p2.size()){
            if (p1.get(p1cnt).docID == p2.get(p2cnt).docID){
                // System.out.println("--------------docID:" + p1.get(p1cnt).docID + "--------------");
                ArrayList<Integer> p1offsets = p1.get(p1cnt).offsets;
                ArrayList<Integer> p2offsets = p2.get(p2cnt).offsets;
                // System.out.println("p1offsets:" + p1offsets);
                // System.out.println("p2offsets:" + p2offsets);
                int offset1cnt = 0;
                int offset2cnt = 0;
                while (offset1cnt < p1offsets.size() && offset2cnt < p2offsets.size()) {
                    if (p2offsets.get(offset2cnt) - p1offsets.get(offset1cnt) == 1){
                        PostingsEntry entry = new PostingsEntry();
                        entry.docID = p1.get(p1cnt).docID;
                        answer.insertEntry(entry, p2offsets.get(offset2cnt));
                        offset1cnt++;
                        offset2cnt++;
                    }
                    else if(p1offsets.get(offset1cnt) < p2offsets.get(offset2cnt)){
                        offset1cnt++;
                    }
                    else {
                        offset2cnt++;
                    }
                }
                p1cnt ++;
                p2cnt ++;
            }else if(p1.get(p1cnt).docID < p2.get(p2cnt).docID) {
                p1cnt++;
            }else{
                p2cnt++;
            }
        }
        return answer;
    }

    private PostingsList rankedQuery(Query query, RankingType rankingType){
        PostingsList result;
        if (rankingType == RankingType.TF_IDF){
            result = rankedQueryTFIDF(query);
            return result;
        }
        else if (rankingType == RankingType.PAGERANK){
            result = rankedQueryPagerank(query);
            return result;
        }
        else if (rankingType == RankingType.COMBINATION){
            result = rankedQueryCombined(query);
            return result;
        }
        return null;
    }

    private PostingsList rankedQueryTFIDF(Query query) {
        PostingsList results = new PostingsList();
        int querySize = query.queryterm.size();
        HashMap<Integer, PostingsEntry> scores = new HashMap<>();
        for(int i = 0; i < querySize; i++) {
            String term = query.queryterm.get(i).term;
            PostingsList list = index.getPostings(term);
            for (int j = 0; j < list.size(); j++){ //不同的docID
                PostingsEntry entry = list.get(j);
                double score = tfidf(entry, list); //每个单词在这个文件中的频率以及在所有文件中的频率
                if (!scores.containsKey(entry.docID)) {
                    PostingsEntry newEntry = new PostingsEntry();
                    newEntry.docID = entry.docID;
                    newEntry.score = score;
                    scores.put(entry.docID, newEntry);
                }
                else {//如果不同query进入同一个docID，score会更高
                    scores.get(entry.docID).score += score;
                }
            }
        }
        // Normalize score
        for (int docID: scores.keySet()) {
            PostingsEntry entry = scores.get(docID);
            entry.score /= Index.docLengths.get(entry.docID);
            results.insertEntry(entry);
        }
        results.sortByScore();
        return results;
    }

    private double tfidf(PostingsEntry entry, PostingsList list) {
        int N = index.docNames.size();
        int df = list.size();
        double tf = entry.offsets.size();
        double idf = Math.log((float)N / (float)df);
        return tf * idf;
    }

    private PostingsList rankedQueryPagerank(Query query){
        PostingsList results = new PostingsList();
        int querySize = query.queryterm.size();
        HashSet<Integer> savedDocIDs = new HashSet<>();
        for (int i = 0; i < querySize; i++) {
            String term = query.queryterm.get(i).term;
            PostingsList list = index.getPostings(term);
            for (int j = 0; j < list.size(); j++) {
                PostingsEntry entry = list.get(j);
                if (!savedDocIDs.contains(entry.docID)) {
                    String docName = Index.docNames.get(entry.docID);
                    // System.out.println("docname:" + docName);
                    String strippedDocName = docName.substring(docName.lastIndexOf("\\") + 1);
                    // System.out.println("strippedDOcName: " + strippedDocName);
                    try {
                        entry.score = Index.pageranks.get(strippedDocName);
                        // System.out.println("score: " + entry.score);
                    }catch (NullPointerException e) {
                        System.err.println(strippedDocName);
                    }
                    results.insertEntry(entry);
                    savedDocIDs.add(entry.docID);
                }
            }
        }
        results.sortByScore();
        /** Normalize scores */
        double norm = 0.0;
        for (int i = 0; i < results.size(); i++){
            PostingsEntry entry = results.get(i);
            norm += entry.score;
        }
        for (int i = 0; i < results.size(); i++){
            PostingsEntry entry = results.get(i);
            entry.score /= norm;
        }
        return results;
    }


    private PostingsList rankedQueryCombined(Query query) {
        PostingsList tfidf = rankedQueryTFIDF(query);
        ArrayList<Double> pagerankScores = new ArrayList<>();
        pagerankScores.ensureCapacity(tfidf.size());
        double tfidfNorm = 0.0;
        double pagerankNorm = 0.0;
        for (int i = 0; i < tfidf.size(); i++) {
            PostingsEntry entry = tfidf.get(i);
            tfidfNorm += entry.score;
            String docName = Index.docNames.get(entry.docID);
            String strippedDocName = docName.substring(docName.lastIndexOf("\\") + 1);
            double pagerankScore = index.pageranks.get(strippedDocName);
            pagerankScores.add(pagerankScore);
            pagerankNorm += pagerankScore;
        }

        for (int i = 0; i < tfidf.size(); i++) {
            PostingsEntry entry = tfidf.get(i);
            entry.score = RANK_WEIGHT * entry.score/tfidfNorm + (1.0-RANK_WEIGHT) * pagerankScores.get(i) /pagerankNorm;
        }

        tfidf.sortByScore();

        return tfidf;
    }
}