package org.sdsc.pdbproject;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Tests if all the functions in the FeatureExtractor are
 * working.
 *
 * @author Rahul Palamuttam
 */
public class FeatureExtractorTest {
    private FeatureExtractor testFeatureExtractor;

    /**
     * Instantiates a new Feature extractor for testing.
     */
    public FeatureExtractorTest() {
        testFeatureExtractor = new FeatureExtractor();
    }

    /**
     * Tests the RCSB_PDB_Counter method
     */
    @Test
    public void RCSB_PDB_CounterTest() {
        String assertString = "The RCSB_PDB_CounterTest is fault";
        String body = "Hello my name is RCSB PDB";
        int testCount = testFeatureExtractor.RCSB_PDB_Counter(body);
        assertTrue(assertString, testCount == 1);
    }

    @Test
    public void testDateParse() throws Exception {
        Date date = testFeatureExtractor.DateParse("Nucleic_Acids_Res_2006_Dec_5_34(22)_6708-6717.nxml");
        String assertString = date.toString();
        System.out.println(assertString);
        assertTrue(assertString, date != null);
        date = testFeatureExtractor.DateParse(" Nucleic_Acids_Res_2005_Jul_27_33(13)_4106-4116.nxml");
        assertString = date.toString();
        System.out.println(assertString);
        assertTrue(assertString, date != null);
        date = testFeatureExtractor.DateParse("/Users/rahulpalamuttam/Research_UCSD_Protein/POSITIVE_DATASET/NXML_FILES/J_Struct_Biol_2014_Mar_185(3)_427-439.nxml\n");
        assertString = date.toString();
        System.out.println(assertString);
        assertTrue(assertString, date != null);
    }
}
