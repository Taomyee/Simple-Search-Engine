/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   Johan Boye, KTH, 2018
 */

package ir;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/*
 *   Implements an inverted index as a hashtable on disk.
 *
 *   Both the words (the dictionary) and the data (the postings list) are
 *   stored in RandomAccessFiles that permit fast (almost constant-time)
 *   disk seeks.
 *
 *   When words are read and indexed, they are first put in an ordinary,
 *   main-memory HashMap. When all words are read, the index is committed
 *   to disk.
 */
public class PersistentHashedIndex implements Index {

    /** The directory where the persistent index files are stored. */
    public static final String INDEXDIR = "./index";

    /** The dictionary file name */
    public static final String DICTIONARY_FNAME = "dictionary";

    /** The data file name */
    public static final String DATA_FNAME = "data";

    /** The terms file name */
    public static final String TERMS_FNAME = "terms";

    /** The doc info file name */
    public static final String DOCINFO_FNAME = "docInfo";

    /** The dictionary hash table on disk can fit this many entries. */
    public static final long TABLESIZE = 611953L;
    //public static final long TABLESIZE = 1007939L;

    /** The dictionary hash table is stored in this file. */
    RandomAccessFile dictionaryFile;

    /** The data (the PostingsLists) are stored in this file. */
    RandomAccessFile dataFile;

    /** Pointer to the first free memory cell in the data file. */
    long free = 0L;

    /** The cache as a main-memory hash map. */
    HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();


    // ===================================================================

    /**
     *   A helper class representing one entry in the dictionary hashtable.
     */
    public class Entry {
        //
        //  YOUR CODE HERE
        //
        String keyword;
        int size;
        long address;

        public Entry(String keyword, int size, long address){
            this.keyword = keyword;
            this.size = size;
            this.address = address;
        }
        public Entry(int size, long address){
            this.size = size;
            this.address = address;
        }
        public String getKeyword(){return this.keyword;}
        public int size(){return this.size;}
        public long getAddress(){return this.address;}
    }


    // ==================================================================


    /**
     *  Constructor. Opens the dictionary file and the data file.
     *  If these files don't exist, they will be created.
     */
    public PersistentHashedIndex() {
        try {
            dictionaryFile = new RandomAccessFile( INDEXDIR + "/" + DICTIONARY_FNAME, "rw" );
            dataFile = new RandomAccessFile( INDEXDIR + "/" + DATA_FNAME, "rw" );
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        try {
            readDocInfo();
        } catch ( FileNotFoundException e ) {
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     *  Writes data to the data file at a specified place.
     *
     *  @return The number of bytes written.
     */
    int writeData( String dataString, long ptr ) {
        try {
            dataFile.seek( ptr );
            byte[] data = dataString.getBytes();
            dataFile.write( data );
            return data.length;
        } catch ( IOException e ) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     *  Reads data from the data file
     */
    String readData( long ptr, int size ) {
        try {
            dataFile.seek( ptr );
            byte[] data = new byte[size];
            dataFile.readFully( data );
            return new String(data);
        } catch ( IOException e ) {
            e.printStackTrace();
            return null;
        }
    }


    // ==================================================================
    //
    //  Reading and writing to the dictionary file.

    /*
     *  Writes an entry to the dictionary hash table file.
     *
     *  @param entry The key of this entry is assumed to have a fixed length
     *  @param ptr   The place in the dictionary file to store the entry
     */
    void writeEntry( Entry entry, long ptr ) {
        //
        //  YOUR CODE HERE
        //
        try{
            //char + int + long = 2 + 4 + 8 = 14 bytes
            ptr *= 14;
            dictionaryFile.seek(ptr);
            dictionaryFile.writeChar(entry.getKeyword().charAt(0));
            dictionaryFile.writeInt(entry.size());
            dictionaryFile.writeLong(entry.getAddress());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Reads an entry from the dictionary file.
     *
     *  @param ptr The place in the dictionary file where to start reading.
     */
    Entry readEntry(String token, long ptr ) {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        try{
            ptr *= 14;
            dictionaryFile.seek(ptr);
            if(dictionaryFile.readChar() == (token.charAt(0))){
                int size = Math.abs(dictionaryFile.readInt());
                long address = dictionaryFile.readLong();
                Entry e = new Entry(token, size, address);
                return e;
            }
            else{return null;}
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // ==================================================================

    /**
     *  Writes the document names and document lengths to file.
     *
     * @throws IOException  { exception_description }
     */
    private void writeDocInfo() throws IOException {
        FileOutputStream fout = new FileOutputStream( INDEXDIR + "/docInfo" );
        for ( Map.Entry<Integer,String> entry : docNames.entrySet() ) {
            Integer key = entry.getKey();
            String docInfoEntry = key + ";" + entry.getValue() + ";" + docLengths.get(key) + "\n";
            fout.write( docInfoEntry.getBytes() );
        }
        fout.close();
    }


    /**
     *  Reads the document names and document lengths from file, and
     *  put them in the appropriate data structures.
     *
     * @throws     IOException  { exception_description }
     */
    private void readDocInfo() throws IOException {
        File file = new File( INDEXDIR + "/docInfo" );
        FileReader freader = new FileReader(file);
        try ( BufferedReader br = new BufferedReader(freader) ) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(";");
                docNames.put( Integer.valueOf(data[0]), data[1] );
                docLengths.put( Integer.valueOf(data[0]), Integer.valueOf(data[2]) );
            }
        }
        freader.close();
    }


    /**
     *  Write the index to files.
     */
    public void writeIndex() {
        int collisions = 0;
        try {
            // Write the 'docNames' and 'docLengths' hash maps to a file
            writeDocInfo();

            // Write the dictionary and the postings list
            //
            //  YOUR CODE HERE
            //
            long ptr = 0L;
            ArrayList<Long> hashes = new ArrayList<Long>();
            String listString;
            for (String token: this.index.keySet()){
                PostingsList postingsList = this.index.get(token);
                //String encodedPostings = encode(postingsList);
                listString = postingsList.toString();
                // listString = token + ":" + listString; // prepend the word
                System.out.println(listString);
                long hash = hashString(token);
                if (hashes.contains(hash)){
                    collisions++;
                }
                else{
                    hashes.add(hash);
                }
                int size = writeData(listString, ptr);
                Entry entry = new Entry(token, size, ptr);
                writeEntry(entry, hash);
                ptr += size;
            }

        } catch ( IOException e ) {
            e.printStackTrace();
        }
        System.err.println( collisions + " collisions." );
    }


    // ==================================================================


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        //
        //  REPLACE THE STATEMENT BELOW WITH YOUR CODE
        //
        long hash = hashString(token);
        Entry entry = readEntry(token, hash);
        if (entry == null){
            return null;
        }
        String data = readData(entry.getAddress(), entry.size());
        // System.out.println("data:"+data);
        PostingsList postingsList = new PostingsList();
        //String dataSplit = data.split(":")[1]; // this will contain a postingListObj
        String[] dataSplit2 = data.split(";");
        for (int i = 0; i <= dataSplit2.length-1; i++){
            String[] splittedPostings = dataSplit2[i].split(",");
            String docID = splittedPostings[0];
            PostingsEntry postingsEntry = new PostingsEntry();
            postingsEntry.docID = Integer.parseInt(docID);
            for (int j = 1; j <= splittedPostings.length-1; j++){
                String offsetVal = splittedPostings[j];
                offsetVal = offsetVal.replace(" ", "");
                postingsList.insertEntry(postingsEntry, Integer.parseInt(offsetVal));
            }
        }
        return postingsList;
    }


    /**
     *  Inserts this token in the main-memory hashtable.
     */
    public void insert( String token, int docID, int offset ) {
        //
        //  YOUR CODE HERE
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
     *  Write index to file after indexing is done.
     */
    public void cleanup() {
        System.err.println( index.keySet().size() + " unique words" );
        System.err.print( "Writing index to disk..." );
        writeIndex();
        System.err.println( "done!" );
    }

    public long hashString(String str){
        str = new StringBuilder(str).reverse().toString();
        long hash = 7;
        for(int i = 0; i < str.length(); i++){
            hash = 31 * hash + str.charAt(i);//(hash << 4) ^ (hash >> 8) ^ str.charAt(i);
            hash = hash % TABLESIZE;
        }
        return hash;
    }


    /*
    public static String encode(PostingsList pl){
        StringBuilder sb = new StringBuilder();
        ArrayList<PostingsEntry> list = pl.getPostingsList();
        for(PostingsEntry entry: list){
            sb.append(entry.docID);
            for(int pos: entry.offsets){
                sb.append(":");
                sb.append(pos);
            }
            sb.append(",");
        }
        System.out.println(sb.toString());
        return sb.toString();
    }

    public static PostingsList decode(String str){
        //go through list and create objects
        PostingsList pl = new PostingsList();

        String[] split = str.split(",");
        for(int i = 0; i < split.length; i++){
            String[] e = split[i].split((":"));
            PostingsEntry entry = new PostingsEntry();
            entry.docID = Integer.parseInt(e[0]);
            for(int j = 1; j < e.length -1; j ++){
                entry.offsets.add(Integer.parseInt(e[j]));
            }
            pl.getPostingsList().add(entry);
        }
        return pl;
    }
    */
}
