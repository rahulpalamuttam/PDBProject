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
	// Transform RDD<filename, entire body> -> <filename, line>
	JavaPairRDD<String, String> fileLines = wholeFile.flatMapValues(new Function<String, Iterable<String>>() {
		public Iterable<String> call(String body){
		    return Arrays.asList(body.split("\n"));
		}
	    });

	// Transforms the basic PairRDD<filename, body> to a JavaRDD<Vector{filename,ID,context}>
	JavaRDD<JournalFeatureVector> fileVector = wholeFile.flatMap(new FlatMapFunction<Tuple2<String, String>, JournalFeatureVector>() {
		public Iterable<JournalFeatureVector> call(Tuple2<String, String> RDDVect){
		    List<String> Body = Arrays.asList(RDDVect._2().split("\n"));
		    JournalFeatureVector[] vect = new JournalFeatureVector[Body.size()];
		    for(int i = 0; i < vect.length; i++){
			vect[i] = new JournalFeatureVector(RDDVect._1, Body.get(i));
		    }
		    List<JournalFeatureVector> vectList = Arrays.asList(vect);
		    return vectList;
		}
	    }); 

	//Filter RDD<filename, line> ->  RDD<filename, line with invalid ID>
	// JavaPairRDD<String, String> filtered = fileLines.filter(new Function<Tuple2<String, String>, Boolean>() {
	// 	public Boolean call(Tuple2<String, String> line){
	// 	    Pattern pattern = Pattern.compile("[1-9][a-zA-Z-0-9]{3}");
	// 	    Matcher matcher = pattern.matcher(line._2);
	// 	    List<String> matches = new ArrayList<String>();
	// 	    // records all the matching sequences in the line
	// 	    while(matcher.find()){
	// 		matches.add(matcher.group());
	// 	    }

	// 	    if(!matches.isEmpty()){
	// 		for(String match : matches){
	// 		    if(varBroad.value().contains(match)) return true;
	// 		}
	// 	    }
	// 	    return false;
	// 	}
	//     });

	JavaPairRDD<String, String> filtered = fileLines.filter(new PairRegexpFilter(varBroad));
	JavaRDD<JournalFeatureVector> negativeVector = fileVector.filter(new RegexpFilter(varBroad));
	// Collects all the key value pairs into a List view
	List<Tuple2<String, String>> filteredList = filtered.collect();
	List<JournalFeatureVector> negativeList = negativeVector.collect();
	// aggregate some countable metrics
	// number of files
	long wholeFileCount = wholeFile.count();
	// number of lines and parititons;
	long fileLinesCount = fileLines.count();
	long vectorLinesCount = fileVector.count();
	long mapSize = filteredList.size();
	int numOfPartitions = fileLines.splits().size();
	// number of lines filtered;
	long filteredCount = filtered.count();
	long filteredVectorCount = negativeVector.count();
	

	System.out.println("Number of files: " + wholeFileCount);
	System.out.println("Number of lines: " + fileLinesCount);
	System.out.println("Number of line vectors: " + vectorLinesCount);
	System.out.println("Number of partitions: " + numOfPartitions);
	System.out.println("Number of filtered lines: " + filteredCount);
	System.out.println("number of filtered vectors: " + filteredVectorCount);
	System.out.println("Number of filtered <k,v> pairs: " + mapSize);
	System.out.println("Number of negative vectors: " + filteredVectorCount);
	//System.out.println(filteredList);
	System.out.println(negativeList.get(1));
	System.out.println("Hello World!");
	
    }
}
