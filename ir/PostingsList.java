/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   Johan Boye, 2017
 */  

package ir;

import java.util.ArrayList;
import java.lang.Comparable;
import java.util.Collections;

public class PostingsList{
    
    /** The postings list */
    private ArrayList<PostingsEntry> list = new ArrayList<PostingsEntry>();


    /** Number of postings in this list. */
    public int size() {
    return list.size();
    }

    /** Returns the ith posting. */
    public PostingsEntry get( int i ) {
    return list.get( i );
    }

    // 
    //  YOUR CODE HERE
    //
    public void insertEntry(PostingsEntry newEntry, int offset){
        // 按docID顺序排序的
        for ( int i = 0; i < size(); i++){
            PostingsEntry insertedEntry = list.get(i);
            if (insertedEntry.docID == newEntry.docID){
                insertedEntry.addOffset(offset);
                return;
            }
            else if(newEntry.docID < insertedEntry.docID){ //比最小的小
                list.add(i, newEntry);
                insertedEntry.addOffset(offset);
                return;
            }
        }
        //比最大的大
        list.add(newEntry);
        newEntry.addOffset(offset);

    }
    public void insertEntry(PostingsEntry newEntry){
        if (size() == 0) list.add(newEntry);
        for ( int i = 0; i < size(); i++) {
            PostingsEntry insertedEntry = list.get(i);
            if (insertedEntry.docID == newEntry.docID){
                return;
            }
            else if(newEntry.docID < insertedEntry.docID){
                list.add(i, newEntry);
                return;
            }
        }
        list.add(newEntry);
    }

    public ArrayList<PostingsEntry> getPostingsList(){
        return this.list;
    }


    public void printList(String key){
        System.out.println("List of " + key);
        for (int i = 0; i < list.size(); i++){
            int docID = list.get(i).docID;
            ArrayList<Integer> p1offsets = list.get(i).offsets;
            System.out.println("docID: " + docID);
            System.out.println("offsets: "+ p1offsets);
        }
    }

    public String toString(){
        StringBuilder test = new StringBuilder();
        for (PostingsEntry pe: this.list){
            test.append(pe.docID);
            for (int os: pe.offsets){
                test.append(",");
                test.append(os);
            }
            test.append(";");
        }
        String testString = test.toString();
        return testString;
    }

    public void sortByScore(){
        Collections.sort(list);
    }
}

