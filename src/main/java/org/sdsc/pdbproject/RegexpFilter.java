package org.sdsc.pdbproject;


// Java libraries
import java.util.regex.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Spark Java programming APIs
 */

import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.*;

/**
 * A filter class that is to be passed to the JavaRDD.filter() function.
 * It takes a feature vector and returns true if it the line contains a
 * negative PDB ID. We make sure to find all the matching sequences
 * in a given line. Then it searches for these sequences
 * among all the unreleased IDs.
 *
 * @param JournalFeatureVector the feature vector
 * @param Boolean whether the
 */
public class RegexpFilter implements Function<JournalFeatureVector, Boolean> {
	private Broadcast<PdbHashTable> HashVar;

	public RegexpFilter(Broadcast<PdbHashTable> v) {
		HashVar = v;
	}

	public Boolean call(JournalFeatureVector vect){
		String line = vect.getContext();
	Pattern pattern = Pattern.compile("[1-9][a-zA-Z0-9]{3}");
	Matcher matcher = pattern.matcher(line);
	List<String> matches= new ArrayList<String>();
	// records all the matching sequences in the line
	while(matcher.find()){
	    matches.add(matcher.group());
	}    
	ArrayList<String> RecordedInvalid = new ArrayList<String>();
	if(!matches.isEmpty()){
	    // Hash it is important to have the smaller array iterated over first
	    for(String match : matches){
		if(HashVar.value().contains(match.toUpperCase())) RecordedInvalid.add(match);
	    }
		vect.setNegativeIdList(RecordedInvalid);
		if(!RecordedInvalid.isEmpty()) return true;
	}
	return false;
    } 
}
