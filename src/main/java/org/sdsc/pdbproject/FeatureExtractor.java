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
/**
 * Spark Java Programming APIs
 */
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.*;
import scala.Tuple2;

/**
 * The type Feature extractor.
 */
/*
 * The work force of the text mining operations.
 * This class extracts all the features from the file
 * and the individual lines.
 * It takes the Rdd Tuple2<Filename, filebody> and transforms it
 * to the form JournalFeatureVector<FileName,ContextLine, NegativeIDList, ....>
 */
public class FeatureExtractor implements FlatMapFunction<Tuple2<String, String>, JournalFeatureVector> {
    private Broadcast<UnrelIDHash> HashVar;

	/**
	 * Instantiates a new Feature extractor.
     */
	public FeatureExtractor(){}

	/**
	 * @ Instantiates a new Feature extractor.
	 *
	 * @param v the v
     */
	public FeatureExtractor(Broadcast<UnrelIDHash> v){
	HashVar = v;
    }

    public Iterable<JournalFeatureVector> call(Tuple2<String, String> RDDVect){
	// Make a list of lines from the file body
	List<String> Body = Arrays.asList(RDDVect._2().split("\n"));
	//Extract some features from the entire
	int RCSB_PDB_num = RCSB_PDB_Counter(RDDVect._2());
	int P_D_B_ = Protein_Data_Bank_Counter(RDDVect._2());
	//Make an array of JournalVectors to fill
	JournalFeatureVector[] vect = new JournalFeatureVector[Body.size()];

	//Extract and Load the features into the vectors
	for(int i = 0; i < vect.length; i++){
	    // Load File name and line
	    ArrayList<String> NegativeList = NegativeExtractor(Body.get(i));
	    vect[i] = new JournalFeatureVector(RCSB_PDB_num, P_D_B_, RDDVect._1, Body.get(i), NegativeList);
	}

	// Collect the JournalFeatureVectors into an Iterable List
	List<JournalFeatureVector> vectList = Arrays.asList(vect);
	return vectList;
    }

	/**
	 * Negative extractor.
	 * Extracts the Negative ID's from the line.
	 * @param line the line
	 * @return the array list of Negative ID's
     */
    public ArrayList<String> NegativeExtractor(String line){
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
	}	
	return RecordedInvalid;
    }

	/**
	 * Count the number of times "RCSB PDB" occurs
	 *
	 * @param body the body
	 * @return the int
     */
    public int RCSB_PDB_Counter(String body){
	int numOfOccurrences = 0;
	Pattern patter = Pattern.compile("RCSB PDB");
	Matcher matcher = patter.matcher(body);
	while(matcher.find()){
	    numOfOccurrences++;
	}
	return numOfOccurrences;
    }

	/**
	 *
	 * Count the number of times "Protein Data Bank" occurs
	 * @param body the body
	 * @return the int
     */
//
    public int Protein_Data_Bank_Counter(String body){
	int numOfOccurrences = 0;
	Pattern patter = Pattern.compile("Protein Data Bank");
	Matcher matcher = patter.matcher(body);
	while(matcher.find()){
	    numOfOccurrences++;
	}
	return numOfOccurrences;
    }
}
