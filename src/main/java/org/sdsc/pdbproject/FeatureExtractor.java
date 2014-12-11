package org.sdsc.pdbproject;

/**
 * Java libraries
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
 * @author Rahul Palamuttam
 * @param Tuple2<String:String> The input to the Mapper consists of file name and body
 * @param JournalFeatureVector the output of the Mapper
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
        int RCSB_PDB_num = RCSB_PDB_Counter(RDDVect._2());
        int P_D_B_ = Protein_Data_Bank_Counter(RDDVect._2());
        //Make an array of JournalVectors to fill for each line
        JournalFeatureVector[] vect = new JournalFeatureVector[Body.size()];

        //Extract and Load the features into the vectors
        for (int i = 0; i < vect.length; i++) {
            // Load File name and line
            ArrayList<String> NegativeList = NegativeExtractor(Body.get(i));
            vect[i] = new JournalFeatureVector(RCSB_PDB_num, P_D_B_, RDDVect._1, Body.get(i), NegativeList);
        }

        // Collect the JournalFeatureVectors into a List
        return Arrays.asList(vect);
    }

    /**
     * Negative extractor.
     * Extracts the Negative ID's from the line.
     *
     * @param line the line
     * @return the array list of Negative ID's
     */
    public ArrayList<String> NegativeExtractor(String line) {
        Pattern pattern = Pattern.compile("[1-9][a-zA-Z0-9]{3}");
        Matcher matcher = pattern.matcher(line);
        Set<String> matches = new HashSet<String>();
        // records all the matching sequences in the line
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        ArrayList<String> RecordedInvalid = new ArrayList<String>();
        if (!matches.isEmpty()) {
            // Hash it is important to have the smaller array iterated over first
            for (String match : matches) {
                if (HashVar.value().isNotReleased(match)) RecordedInvalid.add(match);
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
    public int RCSB_PDB_Counter(String body) {
        int numOfOccurrences = 0;
        Pattern patter = Pattern.compile("RCSB PDB");
        Matcher matcher = patter.matcher(body);
        while (matcher.find()) {
            numOfOccurrences++;
        }
        return numOfOccurrences;
    }

    /**
     * Count the number of times "Protein Data Bank" occurs
     *
     * @param body the body
     * @return the int
     */
    public int Protein_Data_Bank_Counter(String body) {
        int numOfOccurrences = 0;
        Pattern patter = Pattern.compile("Protein Data Bank");
        Matcher matcher = patter.matcher(body);
        while (matcher.find()) {
            numOfOccurrences++;
        }
        return numOfOccurrences;
    }
}
