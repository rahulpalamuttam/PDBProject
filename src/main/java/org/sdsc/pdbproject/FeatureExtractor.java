package org.sdsc.pdbproject;

/**
 * java  libraries
 */

import java.util.*;
import java.util.regex.*;

/**
 * Spark Programming Libraries
 */
import org.apache.spark.api.java.function.*;
import org.apache.spark.broadcast.*;
import scala.Tuple2;


/**
 * The work force of the text mining operations.
 * This map class extracts all the features from the file and the individual lines.
 * It maps Tuple2<File, Body> -> JournalFeatureVector<F1,F2,...>
 *
 * @param Tuple2<String:String> The input to the Mapper consists of file name and body
 * @param JournalFeatureVector  the output of the Mapper
 * @author Rahul Palamuttam
 */
public class FeatureExtractor implements FlatMapFunction<Tuple2<String, String>, JournalFeatureVector> {
    private Broadcast<PdbHashTable> HashVar;

    /**
     * Instantiates a new Feature extractor.
     */
    public FeatureExtractor() {
    }

    /**
     * Instantiates a new Feature extractor with a hashtable
     * to look up valid invalid ids.
     *
     * @param BroadcastHash the broadcasted hashtable
     */
    public FeatureExtractor(Broadcast<PdbHashTable> BroadcastHash) {
        HashVar = BroadcastHash;
    }

    /**
     * The call function describes what the Map task needs to do.
     * The function extracts data from the text and creates feature
     * vectors for each line of text.
     *
     * @param RDDVect A two tuple of <File name, File body>
     * @return Iterable structure of feature vectors
     */
    public Iterable<JournalFeatureVector> call(Tuple2<String, String> RDDVect) {
        // Make a list of lines from the file body
        List<String> Body = Arrays.asList(RDDVect._2().split("\n"));
        //Extract some features from the entire file
        int RCSB_PDB_num = OccurrenceCounter(RDDVect._2(), "RCSB PDB");
        int P_D_B_ = OccurrenceCounter(RDDVect._2(), "Protein Data Bank");
	int PDBCount = OccurrenceCounter(RDDVect._2(), "PDB");
        //Make an array of JournalVectors to fill for each line
        JournalFeatureVector[] vect = new JournalFeatureVector[Body.size()];

        //Extract and Load the features into the vectors
        for (int i = 0; i < vect.length; i++) {
            // Load File name and line
            Tuple2<ArrayList<String>,ArrayList<String>> PosNeg = Extractor(Body.get(i));
	    ArrayList<String> NegativeList = PosNeg._1();
	    ArrayList<String> PositiveList = PosNeg._2();
            vect[i] = new JournalFeatureVector()
                    .setRCSBnum(RCSB_PDB_num)
                    .setP_D_B(P_D_B_)
                    .setFileName(RDDVect._1)
                    .setContext(Body.get(i))
		    .setNegativeIdList(NegativeList)
		.setPositiveIdList(PositiveList)
		.setPDBCount(PDBCount);
        }

        // Collect the JournalFeatureVectors into a List
        return Arrays.asList(vect);
    }

    /**
     * Negative extractor.
     * Extracts the Negative ID's from the line.
     *
     * @param line the line
     * @return the Tuple containing lists <Negative, Positive>
     */
    public Tuple2<ArrayList<String>, ArrayList<String>> Extractor(String line) {
        Pattern pattern = Pattern.compile("[1-9][a-zA-Z0-9]{3}\\b");
        Matcher matcher = pattern.matcher(line);
        Set<String> matches = new HashSet<String>();
        // records all the matching sequences in the line
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        ArrayList<String> RecordedInvalid = new ArrayList<String>();
	ArrayList<String> RecordedValid = new ArrayList<String>();
        if (!matches.isEmpty()) {
            // Hash it is important to have the smaller array iterated over first
            for (String match : matches) {
                if (HashVar.value().isNotReleased(match)) {
                    RecordedInvalid.add(match);
                }
		if (HashVar.value().isReleased(match)) {
		    RecordedValid.add(match);
		}
            }
        }
        return new Tuple2(RecordedInvalid, RecordedValid);
    }

    /**
     * Count the number of times "RCSB PDB" occurs
     *
     * @param body the body
     * @return the int
     */
    public int OccurrenceCounter(String body, String matchstring) {
        int numOfOccurrences = 0;
        Pattern patter = Pattern.compile(matchstring);
        Matcher matcher = patter.matcher(body);
        while (matcher.find()) {
            numOfOccurrences++;
        }
        return numOfOccurrences;
    }

}    
