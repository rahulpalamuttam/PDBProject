package org.sdsc.pdbproject;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
/**
 * Apache Libraries.
 * Spark Java programming APIs. It contains the
 * RDD classes used for Java, as well as the
 * StorageLevels and SparkContext for java.
 */

import org.apache.spark.api.java.*;

/**
 * SparkConf provides the configuration for a
 * Spark applications.
 * No run time modifications to SparkConf objects!
 */
import org.apache.spark.SparkConf;

/**
 * Provides a set of interfaces to represent functions
 * in Spark's Java API. Users create implementations of
 * these interfaces to pass functions into methods (such as M&R)
 */
import org.apache.spark.api.java.function.*;

/**
 *  Indicates the storage level. I choose to use the MEM_ONLY
 */

/**
 * Library for broadcasting objects to nodes
 */
import org.apache.spark.broadcast.*;
import org.apache.spark.mllib.feature.*;
import org.apache.spark.mllib.linalg.Vector;
import scala.Tuple2;
/**
 * The Main class
 * This is a Spark application that searches through
 * journal articles for PDB IDs.
 */
public class Main {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        String dataSet = args[0];

        // The default 2 line structure for spark programs
        SparkConf conf = new SparkConf().setAppName("pdbproject");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Create and Broadcast the HashTable of unreleased ID's
        // I make sure to check if the serialized object is saved as a file
	PdbHashTable HashTable = ConstructHashTable();

	Broadcast<PdbHashTable> varBroad = sc.broadcast(HashTable);

	// Loads the text files with RDD<filename, text>
	JavaPairRDD<String, String> wholeFile = sc.wholeTextFiles(dataSet).repartition(50);

	// Transforms the basic PairRDD<filename, body> to a JavaRDD<Vector{filename,ID,context}>
	JavaRDD<JournalFeatureVector> fileVector = wholeFile.flatMap(new FeatureExtractor(varBroad));

	JavaRDD<JournalFeatureVector> negativeVector = fileVector.filter(new NegativeFilter());
	
	Word2Vec featuration = new Word2Vec();
	JavaRDD<ArrayList<String>> words = negativeVector.map(new Function<JournalFeatureVector, ArrayList<String>>(){
		public ArrayList<String> call(JournalFeatureVector vect){
		    String[] array = vect.getContext().split(" ");
		    ArrayList<String> ret = new ArrayList<String>();
		    for(String i : array){
			ret.add(i);
		    }
		    return ret;
		}
	    });
	
	HashingTF  hashingTF = new HashingTF();
	JavaRDD<Vector> tf = hashingTF.transform(words);
	// Collects all the key value pairs into a List view
	List<JournalFeatureVector> negativeList = negativeVector.collect();
	List<Vector> tfout = tf.collect();
	// aggregate some countable metrics
	// number of files
	long wholeFileCount = wholeFile.count();
	// number of lines and parititons;
	long vectorLinesCount = fileVector.count();
	int numOfPartitions = fileVector.partitions().size();
	// number of lines filtered
	long filteredVectorCount = negativeVector.count();

	System.out.println(tfout.get(1));
	for (JournalFeatureVector n : negativeList) {
	    System.out.println(n);
	}
	System.out.println("Number of files: " + wholeFileCount);
	System.out.println("Number of line vectors: " + vectorLinesCount);
	System.out.println("Number of partitions: " + numOfPartitions);
	System.out.println("Number of negative vectors: " + filteredVectorCount);

	System.out.println("Hello World!");

    }

    /**
     * A method to construct the hashtable. If it has not been saved as a serialized file,
     * the data is downloaded through the RCSB Rest API using the downloader object.
     * If the file exists, the serialized file is read from stream and the data is loaded
     * back into memory.
     */

    public static PdbHashTable ConstructHashTable(){
	PdbHashTable HashTable = null;
        File HashSerialFile = new File("PDBIDHash.ser");
	if(HashSerialFile.exists()){
	    try {
		InputStream file = new FileInputStream("PDBIDHash.ser");
		InputStream buffer = new BufferedInputStream(file);
		ObjectInput input = new ObjectInputStream(buffer);
		HashTable = (PdbHashTable)input.readObject();
		input.close();
	    } catch (IOException ex) {
		ex.printStackTrace();
	    } catch (ClassNotFoundException uhoh){
	    System.out.println("Some class was not found");
	    }
	} else {
	    // Downloads the PdbId's from the rcsb Rest API sources and parses the XML
	    PdbIdSourceDownloader downloader = new PdbIdSourceDownloader();
	    HashTable = PdbIdSourceDownloader.getPdbHashTable();
	    try {
		OutputStream file = new FileOutputStream("PDBIDHash.ser");
		OutputStream buffer = new BufferedOutputStream(file);
		ObjectOutput output = new ObjectOutputStream(buffer);
		output.writeObject(HashTable);
		output.close();
		buffer.close();
		file.close();
	    } catch (Exception ex){
		ex.printStackTrace();
	    }

	}
	return HashTable;
    }
    /**
     * A basic filter class that filters out Unreleased ID's
     * This is going to be removed later
     */
    public static class NegativeFilter implements Function<JournalFeatureVector, Boolean> {

	/**
	 * @param vect a feature vector to run some queries against
	 * @return whether condition has been met (is it a negative ID)
	 */
	public Boolean call(JournalFeatureVector vect) {
	    return vect.getNegativeIdList().size() > 0 && vect.getRCSBCount() > 0;
	}
    }
}
