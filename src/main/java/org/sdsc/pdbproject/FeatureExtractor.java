package org.sdsc.pdbproject;

/**
 * java  libraries
 */

import java.io.Serializable;
import java.util.*;
import java.util.regex.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
/**
 * Stanford parsing library
 */
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.Annotation;
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
public class FeatureExtractor implements FlatMapFunction<Tuple2<String, String>, JournalFeatureVector>, Serializable {
    private Broadcast<PdbHashTable> HashVar;
    private Broadcast<StanfordCoreNLP> StanfordCoreVar;

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
    public FeatureExtractor(Broadcast<PdbHashTable> BroadcastHash, Broadcast<StanfordCoreNLP> StanfCore) {
        HashVar = BroadcastHash;
        StanfordCoreVar = StanfCore;
    }

    /**
     * The call function describes what the Map task needs to do.
     * The function extracts data from the text and creates feature
     * vectors for each line of text.
     * TODO :: We need to do something when the date can't be parsed
     * @param RDDVect A two tuple of <File name, File body>
     * @return Iterable structure of feature vectors
     */
    public Iterable<JournalFeatureVector> call(Tuple2<String, String> RDDVect) {
        // Date published extraction from title of file

        Date date = null;
        try {
            date = DateParse(RDDVect._1());
        } catch (Exception e) {
            return new ArrayList<JournalFeatureVector>();
        }
        // Make a list of lines from the file body
        List<String> Body = SplitIntoSentences(RDDVect._2());
        //Extract some features from the entire file
        int RCSB_PDB_num = RCSB_PDB_Counter(RDDVect._2());
        int P_D_B_ = Protein_Data_Bank_Counter(RDDVect._2());
        //Make an array of JournalVectors to fill for each line
        JournalFeatureVector[] vect = new JournalFeatureVector[Body.size()];

        //Extract and Load the features into the vectors
        for (int i = 0; i < vect.length; i++) {
            // Load File name and line
            Tuple2<ArrayList<String>, ArrayList<String>> PosNeg = Extractor(Body.get(i), date);
            ArrayList<String> NegativeList = PosNeg._1();
            ArrayList<String> PositiveList = PosNeg._2();
            vect[i] = new JournalFeatureVector()
                    .setRCSBnum(RCSB_PDB_num)
                    .setP_D_B(P_D_B_)
                    .setFileName(RDDVect._1())
                    .setContext(Body.get(i))
                    .setNegativeIdList(NegativeList)
                    .setPositiveIdList(PositiveList);
        }

        // Collect the JournalFeatureVectors into a List
        return Arrays.asList(vect);
    }

    /**
     * Parses the String for the Date regular expression found in Article Names.
     * Example : _2008_Jun_1_
     * TODO: There are quite a few dates that cannot be parsed
     * @param String to parse
     * @return the date object
     */
    public Date DateParse(String dateString) throws Exception {
        Pattern pattern = Pattern.compile("_[1-2][0-9]{3}_(([A-Z][a-z]{2})|([1-9]|10|11|12))_[0-9]{1,2}");
        Matcher matcher = pattern.matcher(dateString);
        String datefields = null;
        if (matcher.find()) {
            datefields = matcher.group();
        } else {
            throw new Exception("matcher could not find date in string " + dateString);
        }

        // this right here is a hack
        DateFormat format = new SimpleDateFormat("_yyyy_MMM_dd");
        DateFormat secondFormat = new SimpleDateFormat("_yyyy_M_dd");
        Date ret = null;
        for (int i = 0; i < 100; i++) {
            try {
                DateFormat sample = null;
                if (i == 0) sample = format;
                if (i == 1) sample = secondFormat;
                ret = sample.parse(datefields);
            } catch (Exception e) {
                if (i > 1) {
                    System.out.println(i);
                    throw e;
                }
                continue;
            }
            break;
        }
        return ret;
    }

    /**
     * Negative extractor.
     * Extracts the Negative ID's from the line.
     * The date is absolutely necessary to extract NegativeID's.
     * We need deposition dates.
     *
     * @param date the journal publish date
     * @param line the line
     * @return tuple of two arraylists <RecordedInvalid, RecordedValid>
     */
    public Tuple2<ArrayList<String>, ArrayList<String>> Extractor(String line, Date date) {

        Pattern pattern = Pattern.compile("([1-9][a-z0-9]{3})|([1-9][A-Z0-9]{3})");
        Matcher matcher = pattern.matcher(line);
        Set<String> matches = new HashSet<String>();
        // records all the matching sequences in the line
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        ArrayList<String> RecordedValid = new ArrayList<String>();
        ArrayList<String> RecordedInvalid = new ArrayList<String>();
        if (date == null)
            return new Tuple2(RecordedInvalid, RecordedValid); // if we don't know the date just return an empty inist
        if (!matches.isEmpty()) {
            // Hash it is important to have the smaller array iterated over first
            for (String match : matches) {
                if (HashVar.value().isNotReleased(match, date)) {
                    RecordedInvalid.add(match);
                }
                if (HashVar.value().isReleased(match, date)) {
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
    public int RCSB_PDB_Counter(String body) {
        int numOfOccurrences = 0;
        Pattern patter = Pattern.compile("PDB");
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

    /**
     * Takes in a string body and returns the sentences in the body.
     *
     * @param body
     * @return
     */
    public List<String> SplitIntoSentences(String body) {
        //StanfordCoreNLP pipeline = StanfordCoreVar.value();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, cleanxml");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(body);
        pipeline.annotate(annotation);
        //    pipeline.xmlPrint(annotation, xmlOut);
        // An Annotation is a Map and you can get and use the // various analyses individually. For instance, this
        // gets the parse tree of the 1st sentence in the text.
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        ArrayList<String> result = new ArrayList<>();
        if (sentences != null)
            for (CoreMap sentence : sentences) {
                result.add(sentence.toString());
            }
        return result;
    }
}
