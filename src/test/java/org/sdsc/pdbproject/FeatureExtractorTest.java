package org.sdsc.pdbproject;

import org.junit.Test;

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
        int testCount = testFeatureExtractor.OccurrenceCounter(body, "RCSB PDB");
        assertTrue(assertString, testCount == 1);
    }
}
