package org.sdsc.pdbproject;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.*;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class takes the positive vector and negative
 * vectors and classification algorithms provided by
 * Spark Mllib.
 * Created by rahul on 2/2/15.
 */
public class MLClassifier implements Serializable {
    JavaRDD<String> positiveLines;
    JavaRDD<String> negativeLines;
    JavaRDD<String> testpositiveLines;
    JavaRDD<String> testnegativeLines;
    JavaRDD<String> testcompleteLines;
    JavaRDD<String> completeLines;
    HashingTF tf = new HashingTF();

    /**
     * Constructs the classifier with the extracted lines.
     *
     * @param positive
     * @param negative
     * @param testPositive
     * @param testNegative
     */
    MLClassifier(JavaRDD<JournalFeatureVector> positive, JavaRDD<JournalFeatureVector> negative, JavaRDD<JournalFeatureVector> testPositive, JavaRDD<JournalFeatureVector> testNegative) {
        positiveLines = positive.map(new ContextExtractor());
        negativeLines = negative.map(new ContextExtractor());
        testpositiveLines = testPositive.map(new ContextExtractor());
        testnegativeLines = testNegative.map(new ContextExtractor());
        completeLines = negativeLines.union(positiveLines);
        testcompleteLines = testnegativeLines.union(testpositiveLines);
    }

    /**
     * Extracts the string
     */
    public class ContextExtractor implements Function<JournalFeatureVector, String> {
        public String call(JournalFeatureVector vect) {
            return vect.getContext();
        }
    }

    /**
     * Runs the naive bayes classifier from Mllib
     * 1) Build labeled point representations of the negative and positive sets
     * The training set is the union of the negative and positive labeled pointsets
     * 2) Build the same labeled point representations for the test set
     * TODO :: How do we back track from a LabeledPoint to the actual line/data?
     */
    public void Run() {
        // Building training set
        JavaRDD<LabeledPoint> negativePoints = negativeLines.map(new LabeledPointCreater(0.0));
        JavaRDD<LabeledPoint> positivePoints = positiveLines.map(new LabeledPointCreater(1.0));
        JavaRDD<LabeledPoint> training = negativePoints.union(positivePoints);

        // Building test set
        JavaRDD<LabeledPoint> testnegativePoints = testnegativeLines.map(new LabeledPointCreater(0.0));
        JavaRDD<LabeledPoint> testpositivePoints = testpositiveLines.map(new LabeledPointCreater(1.0));
        JavaRDD<LabeledPoint> testing = testnegativePoints.union(testpositivePoints);

        NiaveBayesRun(training, testing);
        SVMRun(training, testing);
    }

    /**
     * Using spark's NaiveBayesModel to train on the training sets.
     * The model is then used to predict
     *
     * @param training
     * @param testing
     */
    private void NiaveBayesRun(JavaRDD<LabeledPoint> training, JavaRDD<LabeledPoint> testing) {
        final NaiveBayesModel model = NaiveBayes.train(training.rdd(), 1.0);

        JavaPairRDD<Double, Double> predictionAndLabel =
                testing.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
                    public Tuple2<Double, Double> call(LabeledPoint p) {
                        // p.label() returns 0 or 1
                        // p.features() returns the vector
                        return new Tuple2<Double, Double>(model.predict(p.features()), p.label());
                    }
                });
        // predicts the accuracy of the classifier
        Double accuracy = predictionAndLabel.filter(new Function<Tuple2<Double, Double>, Boolean>() {
            public Boolean call(Tuple2<Double, Double> pl) {
                return pl._1().equals(pl._2());
            }
        }).count() / (double) testing.count();

        // counts up the negative and positive vectors
        long positiveCount = positiveLines.count();
        long negativeCount = negativeLines.count();
        long totalCount = completeLines.count();
        System.out.println("This is the ML Naive Bayes program");
        System.out.println("Positive : " + positiveCount);
        System.out.println("Negative : " + negativeCount);
        System.out.println("total : " + totalCount);

        System.out.println("The Niave Bayes classifier was this accurate: " + accuracy);
    }

    /**
     * The body of this code was heavily adopted from
     * Spark's tutorials.
     * http://spark.apache.org/docs/1.2.0/mllib-linear-methods.html#linear-support-vector-machines-svms
     * The SVM classifier gives a relative score of negativeness or positiveness.
     * If the score is <0 then it is negative, if it is >0 it is positive.
     * However we need to determine how much the score should vary from 0 for it to be
     * classified vs un-classified.
     *
     * @param training
     * @param testing
     */
    public void SVMRun(JavaRDD<LabeledPoint> training, JavaRDD<LabeledPoint> test) {
        // Run training algorithm to build the model.
        int numIterations = 1;
        final SVMModel model = SVMWithSGD.train(training.rdd(), numIterations);

        // Clear the default threshold.
        model.clearThreshold();
        System.out.println(model.predict(test.first().features()));
        JavaRDD<Tuple2<Double, Double>> predictionAndLabel = test.map(
                new Function<LabeledPoint, Tuple2<Double, Double>>() {
                    public Tuple2<Double, Double> call(LabeledPoint p) {
                        Double score = (model.predict(p.features()) < 0) ? 0.0 : 1.0;
                        return new Tuple2<Double, Double>(score, p.label());
                    }
                }
        );

        // predicts the accuracy of the classifier
        Double accuracy = predictionAndLabel.filter(new Function<Tuple2<Double, Double>, Boolean>() {
            public Boolean call(Tuple2<Double, Double> pl) {
                return pl._1().equals(pl._2());
            }
        }).count() / (double) test.count();

        long positiveCount = positiveLines.count();
        long negativeCount = negativeLines.count();
        long totalCount = completeLines.count();
        System.out.println("This is the SVM program");
        System.out.println("Positive : " + positiveCount);
        System.out.println("Negative : " + negativeCount);
        System.out.println("total : " + totalCount);

        System.out.println("The SVM classifier was this accurate: " + accuracy);
    }

    /**
     * The labeled point creator function takens in pre-determined
     * positive or negative set. The mark is used to denote
     * the label value
     * It then constructs a vector based on the tf scores from using
     * the hashing trick.
     * <p/>
     * In this implementation it transforms a string based on the
     * hashed occurrences of the set of words.
     */
    public class LabeledPointCreater implements Function<String, LabeledPoint> {
        public Double label;

        public LabeledPointCreater(Double mark) {
            label = mark;
        }

        public LabeledPoint call(String context) {
            Set<String> myList = new HashSet<>(Arrays.asList(context.split(" ")));
            return new LabeledPoint(label, tf.transform(myList));
        }
    }
}
