package org.sdsc.pdbproject;

import java.util.*;
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
        PdbHashTable HashTable = PdbIdSourceDownloader.getPdbHashTable();

        Broadcast<PdbHashTable> varBroad = sc.broadcast(HashTable);

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
        int numOfPartitions = fileVector.partitions().size();
        // number of lines filtered
        long filteredVectorCount = negativeVector.count();


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
     * A basic filter class that filters out Unreleased ID's
     * This is going to be removed later
     */
    public static class NegativeFilter implements Function<JournalFeatureVector, Boolean> {

        /**
         * @param vect a feature vector to run some queries against
         * @return whether condition has been met (is it a negative ID)
         */
        public Boolean call(JournalFeatureVector vect) {
	    boolean ret = false;
	    ret = vect.getNegativeIdList().size() > 0;
            return ret;
        }
    }
}
