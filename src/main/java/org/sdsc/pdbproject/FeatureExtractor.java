// Written by Rahul Palamuttam
package org.sdsc.pdbproject;
/*
  This is the Regular Expression Filter Function class.
*/

// Java libraries
import java.util.HashSet;
import java.util.regex.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/*Spark Java programming APIs*/

import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.*;
import scala.Tuple2;
/*
 * The work force of the text mining operations.
 * This class extracts all the features from the file
 * and the individual line!
 */
public class FeatureExtractor implements FlatMapFunction<Tuple2<String, String>, JournalFeatureVector> {
    public Iterable<JournalFeatureVector> call(Tuple2<String, String> RDDVect){
	List<String> Body = Arrays.asList(RDDVect._2().split("\n"));
	JournalFeatureVector[] vect = new JournalFeatureVector[Body.size()];
	for(int i = 0; i < vect.length; i++){
	    vect[i] = new JournalFeatureVector(RDDVect._1, Body.get(i));
	}
	List<JournalFeatureVector> vectList = Arrays.asList(vect);
	return vectList;
    }
}
