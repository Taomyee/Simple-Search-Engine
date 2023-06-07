/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  


package ir;

import pagerank.PageRank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {


    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    public HashedIndex(){
        try{
            PageRank.readPageranks("./index/pageranks", pageranks);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     *  Inserts this token in the hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        //
        // YOUR CODE HERE
        //
        PostingsEntry postingsEntry = new PostingsEntry();
        postingsEntry.docID = docID;
        PostingsList list = index.get(token);
        if (list == null){
            list = new PostingsList();
            index.put(token, list);
        }
        list.insertEntry(postingsEntry, offset);
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        // REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        return index.get(token);
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }

    public void printList(String key){
        System.out.println("List of " + key);
        PostingsList p1 = index.get(key);
        for (int i = 0; i < p1.size(); i++){
            int docID = p1.get(i).docID;
            ArrayList<Integer> p1offsets = p1.get(i).offsets;
            System.out.println("docID" + docID);
            System.out.println("offsets:"+ p1offsets);
        }
    }
}
