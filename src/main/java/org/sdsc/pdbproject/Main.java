package org.sdsc.pdbproject;


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

import java.util.List;
import java.util.regex.Pattern;

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
        String dataSet = "../Samples/SmallSet";
        Runtime runtime = Runtime.getRuntime();
        long megabytes = runtime.maxMemory() / (1024 * 1024);
        System.out.println("Max used up memory" + megabytes);
        // The default 2 line structure for spark programs
        SparkConf conf = new SparkConf()
                .setAppName("pdbproject")
                .setMaster("local")
                .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .set("spark.storage.memoryFration", "0.75")
                .set("spark.executor.memory", "7g")
                .set("spark.driver.memory", "5g")
                .set("spark.driver.maxResultSize", "2g");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Create and Broadcast the HashTable of unreleased ID's
        // I make sure to check if the serialized object is saved as a file
        PdbHashTable HashTable = PdbIdSourceDownloader.getPdbHashTable();

        Broadcast<PdbHashTable> varBroad = sc.broadcast(HashTable);

        // Loads the text files with RDD<filename, text>
        JavaPairRDD<String, String> wholeFile = sc.wholeTextFiles(dataSet).sample(false, 0.5, 1).repartition(100);

        // Transforms the basic PairRDD<filename, body> to a JavaRDD<Vector{filename,ID,context}>
        JavaRDD<JournalFeatureVector> fileVector = wholeFile.flatMap(new FeatureExtractor(varBroad));

        JavaRDD<JournalFeatureVector> negativeVector = fileVector.filter(new NegativeFilter());
        JavaRDD<JournalFeatureVector> positiveVector = fileVector.filter(new PositiveFilter());

        // Collects all the key value pairs into a List view.
        //List<JournalFeatureVector> negativeList = negativeVector.collect();
        //List<JournalFeatureVector> positiveList = positiveVector.collect();

        // aggregate some countable metrics

        MLClassifier mlClassifier = new MLClassifier(positiveVector, negativeVector);
        mlClassifier.Run();
        // number of files
        long wholeFileCount = wholeFile.count();
        // number of lines and parititons;
        long vectorLinesCount = fileVector.count();
        int numOfPartitions = fileVector.partitions().size();
        // number of lines filtered
        long filteredVectorCount = negativeVector.count();
        long positiveVectorCount = positiveVector.count();

        //complete.saveAsTextFile("csvfile");
        System.out.println("Free Memory:" + runtime.maxMemory() / (1024 * 1024));
        System.out.println("Number of files: " + wholeFileCount);
        System.out.println("Number of line vectors: " + vectorLinesCount);
        System.out.println("Number of partitions: " + numOfPartitions);
        System.out.println("Number of negative vectors: " + filteredVectorCount);
        System.out.println("Number of positive vectors: " + positiveVectorCount);
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
            List<String> negatives = vect.getNegativeIdList();
            for (String id : negatives) {

            }
            return vect.getNegativeIdList().size() > 0 && vect.getPositiveIdList().size() == 0;

        }
    }

    public static class PositiveFilter implements Function<JournalFeatureVector, Boolean> {
        public Boolean call(JournalFeatureVector vect) {
            return vect.getPositiveIdList().size() > 0 &&
                    vect.getNegativeIdList().size() == 0 &&
                    vect.getRCSBnum() > 0;
        }
    }


}
