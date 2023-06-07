import java.io.*;
import java.util.*;
public class test {
    static String term = "hello";
    public static final long TABLESIZE = 611953L;
    static RandomAccessFile dictionaryFile;
    public static long hash(String str){
        long hash = 0;
        for(int i = 0; i < str.length(); i++){
            hash += 31 * hash + str.charAt(i);
        }
        hash = (hash % TABLESIZE);
        return hash;
    }

    public static void main(String[] args){
        long t = hash(term);
        System.out.println(t);
        try{
            dictionaryFile = new RandomAccessFile( "./index/here", "rw" );
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
