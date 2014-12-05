// Written by Rahul Palamuttam
package org.sdsc.pdbproject;

/* This is the Main class of the program.
 */

// Java libraries
import java.io.Serializable;
import java.util.List;
import java.util.Arrays;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//Apache libraries
import org.apache.commons.collections.map.LinkedMap;
/* Spark Java programming APIs. It contains the 
 * RDD classes used for Java, as well as the
 * StorageLevels and SparkContext for java.
 */
import org.apache.spark.api.java.*;

/* SparkConf provides the configuration for a
 * Spark applications.
 * No run time modifications to SparkConf objects!
 */
import org.apache.spark.SparkConf;

/* Provides a set of interfaces to represent functions
 * in Spark's Java API. Users create implementations of
 * these interfaces to pass functions into methods (such as M&R)
 */
import org.apache.spark.api.java.function.*;

/* Indicates the storage level. I choose to use the MEM_ONLY */
import org.apache.spark.storage.StorageLevel;

/* This allows me to cache in objects to nodes */
import org.apache.spark.broadcast.*;
// A Tuple of two elements
import scala.Tuple2;

/*
 * @Arg0 = Input Directory for UNLABELED dataset
 * @Arg2 = Input Directory for Unreleased IDs
 */

public class Main 
{

    /*
     * A very basic filter class that removes any vector with no NegativeID's
     * This is going to be removed later.
     */ 
    public static class NegativeFilter implements Function<JournalFeatureVector, Boolean> {
	public Boolean call(JournalFeatureVector vect){
	    if(vect.getNegativeIdList().size() == 0 || vect.getRCSBCount() == 0){
		return false;
	    }
	    return true;
	} 
    }

    public static void main(String[] args)
    {
	String dataSet = args[0];
	String fileUnreleased = args[1];

	// The default 2 line structure for spark programs
	SparkConf conf = new SparkConf().setAppName("pdbproject");
	JavaSparkContext sc = new JavaSparkContext(conf);

	// Create and Broadcast the HashTable of unreleased ID's
	UnrelIDHash HashTable = new UnrelIDHash(fileUnreleased);
        Broadcast<UnrelIDHash> varBroad = sc.broadcast(HashTable);

	// Loads the text files with RDD<filename, text>
	JavaPairRDD<String, String> wholeFile = sc.wholeTextFiles(dataSet).repartition(50);

	// Transforms the basic PairRDD<filename, body> to a JavaRDD<Vector{filename,ID,context}>
	JavaRDD<JournalFeatureVector> fileVector = wholeFile.flatMap(new FeatureExtractor(varBroad));

	JavaRDD<JournalFeatureVector> negativeVector = fileVector.filter(new NegativeFilter());
	
	// Collects all the key value pairs into a List view
	List<JournalFeatureVector> negativeList = negativeVector.collect();
	
	// aggregate some countable metrics
	// number of files
	long wholeFileCount = wholeFile.count();
	// number of lines and parititons;
	long vectorLinesCount = fileVector.count();
	int numOfPartitions = fileVector.splits().size();
	// number of lines filtered
	long filteredVectorCount = negativeVector.count();
	
	for(JournalFeatureVector n : negativeList){
	    System.out.println(n);
	}
	System.out.println("Number of files: " + wholeFileCount);
	System.out.println("Number of line vectors: " + vectorLinesCount);
	System.out.println("Number of partitions: " + numOfPartitions);
	System.out.println("Number of negative vectors: " + filteredVectorCount);
	
	System.out.println("Hello World!");
	
    }
}
