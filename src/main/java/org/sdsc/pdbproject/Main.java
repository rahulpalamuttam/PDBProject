package org.sdsc.pdbproject;

/**
 * Apache Libraries.
 * Spark Java programming APIs. It contains the
 * RDD classes used for Java, as well as the
 * StorageLevels and SparkContext for java.
 */

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
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
import org.apache.spark.rdd.SequenceFileRDDFunctions;
import scala.Tuple2;
import scala.Tuple4;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
        try {
            System.setOut(new PrintStream(new File("Output.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String dataSet = args[0];
        Runtime runtime = Runtime.getRuntime();
        long megabytes = runtime.maxMemory() / (1024 * 1024);
        System.out.println("Max used up memory" + megabytes);
        // The default 2 line structure for spark programs
        SparkConf conf = new SparkConf()
                .setAppName("pdbproject")
                        //.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                .setMaster(args[2])
                .set("spark.storage.memoryFraction", "0.75")
                .set("spark.executor.memory", "25g")
                .set("spark.driver.memory", "20g")
                .set("spark.driver.maxResultSize", "20g");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Create and Broadcast the HashTable of unreleased ID's
        // I make sure to check if the serialized object is saved as a file
        PdbHashTable HashTable = PdbIdSourceDownloader.getPdbHashTable();
        // stanford Sentence parser initializatio and broadcast
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, cleanxml");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Broadcast<PdbHashTable> varBroad = sc.broadcast(HashTable);
        //Broadcast<StanfordCoreNLP> StanfordNLP = sc.broadcast(pipeline);
        // Loads the text files with RDD<filename, text>
        JavaPairRDD<String, String> wholeFile = sc.objectFile(args[1]).mapToPair(new PairFunction<Object, String, String>() {
            @Override
            public Tuple2<String, String> call(Object o) throws Exception {
                return (Tuple2<String, String>) o;
            }
        }).repartition(250);


        // test set - this set does not include the training set - achieved by the subtract function
        //JavaPairRDD<String, String> testFile = sc.wholeTextFiles(dataSet)
        //        .subtract(wholeFile)
        //        .repartition(100);

        // Transforms the basic PairRDD<filename, body> to a JavaRDD<Vector{filename,ID,context}>
        JavaRDD<JournalFeatureVector> wholeFileVector = wholeFile.flatMap(new FeatureExtractor(varBroad, null));
        JavaRDD<JournalFeatureVector> trainingVector = wholeFileVector.sample(false, 0.5, (long) 5);
        // test vector
        JavaRDD<JournalFeatureVector> testVector = wholeFileVector.subtract(trainingVector);
        // extracts the training negative and positive vectors
        JavaRDD<JournalFeatureVector> negativeVector = trainingVector.filter(new NegativeFilter());
        JavaRDD<JournalFeatureVector> positiveVector = trainingVector.filter(new PositiveFilter());
        JavaRDD<String> negativeLines = negativeVector.map(new ContextExtractor()).repartition(1);
        JavaRDD<String> positiveLines = positiveVector.map(new ContextExtractor()).repartition(1);
        // extracts the test negative and positive vectors
        JavaRDD<JournalFeatureVector> testNegVector = testVector.filter(new NegativeFilter());
        JavaRDD<JournalFeatureVector> testPosVector = testVector.filter(new PositiveFilter());

        // number of lines filtered
        // training set
        long filteredVectorCount = negativeVector.count();
        long positiveVectorCount = positiveVector.count();
        // test set
        long testNegVectorCount = testNegVector.count();
        long testPosVectorCount = testPosVector.count();

        /**
         * Since we have more positive vectors than negative vectors
         * we set the sampling ratio to get a proportionally equal amount
         * of the positive vectors
         */
        JavaRDD<JournalFeatureVector> testpositiveVector = positiveVector.sample(false, ((double) filteredVectorCount / (double) positiveVectorCount), 1);
        long testPositiveCount = testpositiveVector.count();


        MLClassifier mlClassifier = new MLClassifier(testpositiveVector, negativeVector, testPosVector, testNegVector);


        // number of files
        long wholeFileCount = wholeFile.count();
        // number of lines and parititons;
        long vectorLinesCount = trainingVector.count();
        int numOfPartitions = trainingVector.partitions().size();

        mlClassifier.Run();
        //complete.saveAsTextFile("csvfile");
        negativeLines.saveAsTextFile("NegativeLines.txt");
        positiveLines.saveAsTextFile("PositiveLines.txt");
        System.out.println("Free Memory:" + runtime.maxMemory() / (1024 * 1024));
        System.out.println("Number of files: " + wholeFileCount);
        System.out.println("Number of line vectors: " + vectorLinesCount);
        System.out.println("Number of partitions: " + numOfPartitions);
        System.out.println("Number of negative vectors: " + filteredVectorCount);
        System.out.println("Number of filtered positive vectors: " + testPositiveCount);
        System.out.println("Number of positive vectors: " + positiveVectorCount);
        System.out.println("Number of test negative vectors: " + testNegVectorCount);
        System.out.println("Number of test positive vectors: " + testPosVectorCount);
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
            PDBPatternFinder patternFinder = new PDBPatternFinder();
            List<String> negatives = vect.getNegativeIdList();
            String context = vect.getContext();
            for (String id : negatives) {
                boolean found = patternFinder.findPattern(id, context);
                if (found == true) return false;
            }
            return vect.getNegativeIdList().size() > 0 && vect.getPositiveIdList().size() == 0;
        }
    }

    /**
     * The positive filter checks for any occurrences of positive ID's
     * regardless if is has been unreleased or not.
     */
    public static class PositiveFilter implements Function<JournalFeatureVector, Boolean> {
        public Boolean call(JournalFeatureVector vect) {
            PDBPatternFinder patternFinder = new PDBPatternFinder();
            List<String> complete = new ArrayList<String>();
            complete.addAll(vect.getPositiveIdList());
            complete.addAll(vect.getNegativeIdList());
            String context = vect.getContext();
            // if the pattern is found just return true
            for (String id : complete) {
                if (patternFinder.findPattern(id, context)) {
                    return true;
                }
            }

            // otherwise just check if all found ID's are positive
            return vect.getPositiveIdList().size() > 0 &&
                    vect.getNegativeIdList().size() == 0;
        }
    }

    /**
     * public static class
     */
    public static class wholeContextExtractor implements Function<JournalFeatureVector, Tuple4<String, String, String, String>> {
        public Tuple4 call(JournalFeatureVector vect) {
            String context = vect.getContext();
            String negativeList = vect.getNegativeIdList().toString();
            String positiveList = vect.getPositiveIdList().toString();
            String fileName = vect.getFileName();
            Tuple4<String, String, String, String> ret = new Tuple4<>(fileName, negativeList, positiveList, context);
            return ret;
        }
    }

    public static class ContextExtractor implements Function<JournalFeatureVector, String> {
        public String call(JournalFeatureVector vect) {
            return vect.getContext();
        }
    }
}
