package pagerank;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class PageRank {

	/**
	 *   Maximal number of documents. We're assuming here that we
	 *   don't have more docs than we can keep in main memory.
	 */
	final static int MAX_NUMBER_OF_DOCS = 2000000;

	/**
	 *   Mapping from document names to document numbers.
	 */
	HashMap<String,Integer> docNumber = new HashMap<String,Integer>();

	/**
	 *   Mapping from document numbers to document names
	 */
	String[] docName = new String[MAX_NUMBER_OF_DOCS];

	/**
	 *   A memory-efficient representation of the transition matrix.
	 *   The outlinks are represented as a HashMap, whose keys are
	 *   the numbers of the documents linked from.<p>
	 *
	 *   The value corresponding to key i is a HashMap whose keys are
	 *   all the numbers of documents j that i links to.<p>
	 *
	 *   If there are no outlinks from i, then the value corresponding
	 *   key i is null.
	 */
	HashMap<Integer,HashMap<Integer,Boolean>> link = new HashMap<Integer,HashMap<Integer,Boolean>>();

	public ArrayList<Page> results = new ArrayList<>();

	/**
	 *   The number of outlinks from each node.
	 */
	int[] out = new int[MAX_NUMBER_OF_DOCS];

	/**
	 *   The probability that the surfer will be bored, stop
	 *   following links, and take a random jump somewhere.
	 */
	final static double BORED = 0.15;

	/**
	 *   Convergence criterion: Transition probabilities do not
	 *   change more that EPSILON from one iteration to another.
	 */
	final static double EPSILON = 0.00001;

	public class Page implements Comparable<Page> {
		public int docID = 0;
		public double value = 0.0;
		public String docName = "";

		public Page(int docID, double value, String docName) {
			this.docID = docID;
			this.value = value;
			this.docName = docName;
		}

		@Override
		public int compareTo(Page other) {
			return Double.compare(value, other.value);
		}
	}


	/* --------------------------------------------- */


	public PageRank( String filename ) {
		int noOfDocs = readDocs( filename );
		iterate( noOfDocs, 1000 );
	}


	/* --------------------------------------------- */


	/**
	 *   Reads the documents and fills the data structures.
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
						link.put(fromdoc, new HashMap<Integer,Boolean>());
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


	/* --------------------------------------------- */


	/*
	 *   Chooses a probability vector a, and repeatedly computes
	 *   aP, aP^2, aP^3... until aP^i = aP^(i+1).
	 */
	void iterate( int numberOfDocs, int maxIterations ) {
		// YOUR CODE HERE
		HashMap<String, Double> G = new HashMap<String, Double>();
		Matrix A_pre;
		Matrix A = new Matrix(1, numberOfDocs);
		A.matrix[0][0] = 1.0;
		double err;
		for (int k = 0; k < maxIterations; k++){
			System.out.println("Iteration: " + k);
			A_pre = A;
			Matrix B = new Matrix(1, numberOfDocs);
			for (int i = 0; i < numberOfDocs; i++){ // G row
				HashMap<Integer, Boolean> h = link.get(i);
				if (h == null) {
					//System.err.println(out[i]);
					for (int j = 0; j < numberOfDocs; j++) {
						B.matrix[0][j] += A_pre.matrix[0][i] / numberOfDocs;
					}
				}
				else {
					for (int j = 0; j < numberOfDocs; j++) { // G column
						if (h.get(j) != null) {
							B.matrix[0][j] += A_pre.matrix[0][i] * ((1.0 - BORED) / (double) out[i] + BORED / numberOfDocs);
						} else {
							//System.err.println(out[i]);
							B.matrix[0][j] += A_pre.matrix[0][i] * BORED / numberOfDocs;
						}
					}
				}
			}
			A = B;
			Matrix.normalize(A);
			err = Matrix.distance(A_pre, A);
			System.out.println(err);
			if (err <= EPSILON) {
				System.out.println("Iteration: " + k);
				break;
			}
		}
		try {
			writePageranks("pagerank/davisTitles.txt", docName, A.matrix[0]);
		}catch(IOException e){
			e.printStackTrace();
		}
		for (int i = 0; i < A.column; i++) {
			results.add(new Page(i, A.matrix[0][i], docName[i]));
		}
		Collections.sort(results, Collections.reverseOrder());
		for (int i = 0; i < 30; i++) {
			Page page = results.get(i);
			System.err.format(page.docName + ": %.5f%n", page.value);
		}
	}
	/*
	void displayResults(Matrix a) {
		ArrayList<Page> results = new ArrayList<>();
		for (int i = 0; i < a.column; i++) {
			results.add(new Page(i, a.matrix[0][i], docName[i]));
		}
		Collections.sort(results, Collections.reverseOrder());
		for (int i = 0; i < 30; i++) {
			Page page = results.get(i);
			System.err.format(page.docName + ": %.5f%n", page.value);
		}
	}
	*/

	public static void writePageranks(String filepath, String[] docNames, double[] a) throws IOException {
		HashMap<String, String> realDocNames = new HashMap<>();
		File file = new File(filepath);
		FileReader freader = new FileReader(file);
		try (BufferedReader br = new BufferedReader(freader)) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(";");
				realDocNames.put(data[0], data[1]);
			}
		}
		freader.close();
		FileOutputStream fout = new FileOutputStream("index/pageranks");
		for (int i = 0; i < a.length; i++) {
			String docName = realDocNames.get(docNames[i]);
			String docInfoEntry = docName + ";" + a[i] + "\n";
			fout.write(docInfoEntry.getBytes());
		}
		fout.close();
	}

	public static void readPageranks(String filepath, HashMap<String, Double> map) throws IOException {
		File file = new File(filepath);
		FileReader freader = new FileReader(file);
		try (BufferedReader br = new BufferedReader(freader)) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split(";");
				map.put(data[0], Double.parseDouble(data[1]));
			}
		}
		freader.close();
	}


	/* --------------------------------------------- */


	public static void main( String[] args ) {
		if ( args.length != 1 ) {
			System.err.println( "Please give the name of the link file" );
		}
		else {
			new PageRank( args[0] );
		}
	}
}