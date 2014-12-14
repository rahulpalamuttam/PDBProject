package org.sdsc.pdbproject;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
/**
 * Apache Libraries.
 * Spark Java programming APIs. It contains the
 * RDD classes used for Java, as well as the
 * StorageLevels and SparkContext for java.
 */

import org.apache.spark.api.java.*;
import org.apache.spark.rdd.RDD;
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
import org.apache.spark.mllib.regression.*;
import org.apache.spark.mllib.classification.*;
import org.apache.spark.mllib.linalg.*;

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
        PdbHashTable HashTable = PdbIdSourceDownloader.getPdbHashTable();

        Broadcast<PdbHashTable> varBroad = sc.broadcast(HashTable);

        // Loads the text files with RDD<filename, text>
        JavaPairRDD<String, String> wholeFile = sc.wholeTextFiles(dataSet).repartition(50);

        // Transforms the basic PairRDD<filename, body> to a JavaRDD<Vector{filename,ID,context}>
        JavaRDD<JournalFeatureVector> fileVector = wholeFile.flatMap(new FeatureExtractor(varBroad));

        JavaRDD<JournalFeatureVector> negativeVector = fileVector.filter(new NegativeFilter());
	JavaRDD<JournalFeatureVector> positiveVector = fileVector.filter(new PositiveFilter());
	JavaRDD<JournalFeatureVector> TrainingVectors = positiveVector.union(negativeVector);
        JavaRDD<LabeledPoint> TrainingSet = TrainingVectors.map(new Function<JournalFeatureVector, LabeledPoint>() {
		public LabeledPoint call(JournalFeatureVector vect) {
		    return vect.FeatureLabel();
		}
	    });
	
        JavaRDD<Vector> DataVectors = fileVector.map(new Function<JournalFeatureVector, Vector>() {
		public Vector call(JournalFeatureVector vect) {
		    return vect.FeatureVector();
		}
	    });
	JavaRDD<Iterable<String>> Features = TrainingVectors.map(new Function<JournalFeatureVector, Iterable<String>>() {
		public Iterable<String> call(JournalFeatureVector vect) {
		    return Arrays.asList(vect.getContext().split(" "));
		}
	    });
	// word2vec model
	Word2Vec mod = new Word2Vec().setVectorSize(1);
	Word2VecModel wordvecmodel = mod.fit(Features);
	
	// Naive Bayes
	NaiveBayes model = new NaiveBayes();
	NaiveBayesModel classifier =  model.train(TrainingSet.rdd());
	RDD<Object> result = classifier.predict(DataVectors.rdd());
	Object resultList = result.collect();

        // Collects all the key value pairs into a List view
        List<JournalFeatureVector> TrainingVectorList = TrainingVectors.collect();
	List<LabeledPoint> TrainingList = TrainingSet.collect();
        
	// aggregate some countable metrics
        // number of files
        long wholeFileCount = wholeFile.count();
        // number of lines and parititons;
        long vectorLinesCount = fileVector.count();
        int numOfPartitions = fileVector.partitions().size();
        // number of lines filtered
        long filteredVectorCount = negativeVector.count();
	
	
        // for (JournalFeatureVector n : TrainingVectorList) {
        //     System.out.println(n);
        // }
	//WordCounts(positiveVector);
	
	int resultCount = 0;
	for(double r : (double[])resultList){
	    if(r == 1) resultCount++;
	}
	System.out.println(resultCount);
	System.out.println(TrainingList.size());
	Tuple2<String, Object>[] synonyms =  wordvecmodel.findSynonyms("PDB", 1000);
	for(Tuple2<String, Object> a : synonyms){
	    String word = a._1;
	    Double num = (Double)(a._2);
	}
	System.out.println("Number of files: " + wholeFileCount);
	System.out.println("Number of line vectors: " + vectorLinesCount);
	System.out.println("Number of partitions: " + numOfPartitions);
	System.out.println("Number of negative vectors: " + filteredVectorCount);
	System.out.println(wordvecmodel.transform("PDB"));
	System.out.println("Hello World!");
    }


    /**
     * A basic filter class that determines the lines with only Negative ID occurrences.
     * Ensures that there are no positive ID's on the line and no references to "PDB" or
     * "Protein Data Bank".
     */
    public static class NegativeFilter implements Function<JournalFeatureVector, Boolean> {

	/**
	 * @param vect a feature vector to run some queries against
	 * @return whether condition has been met (is it a negative ID)
	 */
	public Boolean call(JournalFeatureVector vect) {
	    boolean ret = vect.getNegativeIdList().size() > 0 &&
		vect.getPositiveIdList().size() == 0 &&
		vect.getProtDatBanCount() == 0 &&
		vect.getPDBCount() == 0;
	    return ret;
	}
    }

    /**
     * A basic filter class that determines Positive ID occurrences. It looks at all lines
     * with only Positive ID occurrences and no Negative ID occurrences.
     */
    public static class PositiveFilter implements Function<JournalFeatureVector, Boolean> {

	/**
	 * @param vect a feature vector to run some queries against
	 * @return whether condition has been met (is it a negative ID)
	 */
	public Boolean call(JournalFeatureVector vect) {
	    boolean ret = vect.getPositiveIdList().size()  > 0 &&
		 vect.getPDBCount() > 0  &&
		vect.getNegativeIdList().size() == 0;
	    return ret;
	}
    }

    public static void WordCounts(JavaRDD<JournalFeatureVector> RDDVect){
	double count = RDDVect.count();
	JavaRDD<String> words = RDDVect.flatMap(new FlatMapFunction<JournalFeatureVector, String>() {
		public Iterable<String> call(JournalFeatureVector vect) { 
		    String line = vect.getContext()
		    .replace("the", "");
		    return Arrays.asList(line.split(" "));
		}
	    });
	
	JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
		public Tuple2<String, Integer> call(String s) { return new Tuple2<String, Integer>(s, 1); }
	    });

	JavaPairRDD<String, Integer> counts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
		public Integer call(Integer a, Integer b) {return a + b;}
	    });

	JavaRDD<Tuple2<String, Integer>> reducedCounts = JavaPairRDD.toRDD(counts).toJavaRDD();
	JavaRDD<Tuple2<String, Integer>> sortedCounts = reducedCounts.sortBy(new Sorter(), false,1);
	List<Tuple2<String, Integer>> reducedCountsObject = sortedCounts.collect();
	int j = 1000;
	for(int i = 0; i < reducedCountsObject.size(); i++){
	    Tuple2<String, Integer> temp = reducedCountsObject.get(i);
	    Double frac = temp._2()/count;
	    if(frac > 0.25 && frac < 1.1){
		System.out.println(temp._1() + " " + temp._2());
		j--;
	    }
	    if(j == 0) return;
	}
    }

    public static class Sorter implements Function<Tuple2<String, Integer>, Integer> {
	public Integer call(Tuple2<String, Integer> Tups){
	    return Tups._2;
	}
    }
}
