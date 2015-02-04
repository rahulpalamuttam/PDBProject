package org.sdsc.pdbproject;

import org.apache.spark.api.java.JavaRDD;

/**
 * This class takes the positive vector and negative
 * vectors and classification algorithms provided by
 * Spark Mllib.
 * Created by rahul on 2/2/15.
 */
public class MLClassifier {
    JavaRDD<JournalFeatureVector> PositiveJournalVector;
    JavaRDD<JournalFeatureVector> NegativeJournalVector;
    JavaRDD<JournalFeatureVector> TrainingJournalVector;

    MLClassifier(JavaRDD<JournalFeatureVector> positive, JavaRDD<JournalFeatureVector> negative) {
        PositiveJournalVector = positive;
        NegativeJournalVector = negative;
        TrainingJournalVector = positive.union(negative);
    }

    public void Run() {

    }
}
