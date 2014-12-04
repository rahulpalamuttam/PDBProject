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

/*Spark Java programming APIs*/

import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.*;
import scala.Tuple2;
/*
 * A filter class that is to be passed to the JavaRDD.filter() function.
 * It takes a string and returns true if it contains the following
 * regular expression. We make sure to find all the matching sequences
 * in a given line. Then it searches for these sequences
 * amoung all the unreleased IDs. 
 */
public class PairRegexpFilter implements Function<Tuple2<String, String>, Boolean> {
    Broadcast<UnrelIDHash> varBroad;
    public PairRegexpFilter(Broadcast<UnrelIDHash> var){
	varBroad = var;
    }
    public Boolean call(Tuple2<String, String> line){
	Pattern pattern = Pattern.compile("[1-9][a-zA-Z0-9]{3}");
	Matcher matcher = pattern.matcher(line._2);
	List<String> matches= new ArrayList<String>();
	// records all the matching sequences in the line
	while(matcher.find()){
	    matches.add(matcher.group());
	}
	    
	if(!matches.isEmpty()){
	    for(String match : matches){
		if(varBroad.value().contains(match.toUpperCase())) return true;
	    }
	}
	return false;
    } 
}
