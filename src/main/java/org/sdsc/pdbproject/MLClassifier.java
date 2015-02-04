package org.sdsc.pdbproject;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.*;
import org.apache.spark.mllib.classification.NaiveBayes;
import org.apache.spark.mllib.classification.NaiveBayesModel;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.regression.LabeledPoint;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * This class takes the positive vector and negative
 * vectors and classification algorithms provided by
 * Spark Mllib.
 * Created by rahul on 2/2/15.
 */
public class MLClassifier implements Serializable {
    JavaRDD<String> positiveLines;
    JavaRDD<String> negativeLines;
    JavaRDD<String> completeLines;
    HashingTF tf = new HashingTF();
    MLClassifier(JavaRDD<JournalFeatureVector> positive, JavaRDD<JournalFeatureVector> negative) {
        positiveLines = positive.map(new ContextExtractor());
        negativeLines = negative.map(new ContextExtractor());
        completeLines = negativeLines.union(positiveLines);

    }


    public class ContextExtractor implements Function<JournalFeatureVector, String> {
        public String call(JournalFeatureVector vect) {
            return vect.getContext();
        }
    }

    public void Run() {
        JavaRDD<LabeledPoint> negativePoints = negativeLines.map(new LabeledPointCreater(0.0));
        JavaRDD<LabeledPoint> positivePoints = positiveLines.map(new LabeledPointCreater(1.0));
        JavaRDD<LabeledPoint> training = negativePoints.union(positivePoints);

        final NaiveBayesModel model = NaiveBayes.train(training.rdd(), 1.0);

        JavaPairRDD<Double, Double> predictionAndLabel =
                training.mapToPair(new PairFunction<LabeledPoint, Double, Double>() {
                    public Tuple2<Double, Double> call(LabeledPoint p) {
                        return new Tuple2<Double, Double>(model.predict(p.features()), p.label());
                    }
                });

        Double accuracy = predictionAndLabel.filter(new Function<Tuple2<Double, Double>, Boolean>() {
            public Boolean call(Tuple2<Double, Double> pl) {
                return pl._1().equals(pl._2());
            }
        }).count() / (double) training.count();

        long positiveCount = positiveLines.count();
        long negativeCount = negativeLines.count();
        long totalCount = completeLines.count();
        System.out.println("This is the ML program");
        System.out.println("Positive : " + positiveCount);
        System.out.println("Negative : " + negativeCount);
        System.out.println("total : " + totalCount);

        System.out.println("The classifier was this accurate: " + accuracy);
    }

    public class LabeledPointCreater implements Function<String, LabeledPoint> {
        public Double label;

        public LabeledPointCreater(Double mark) {
            label = mark;
        }

        public LabeledPoint call(String context) {
            List<String> myList = Arrays.asList(context.split(" "));
            return new LabeledPoint(label, tf.transform(myList));
        }
    }
}
