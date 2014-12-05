//Written by Rahul Palamuttam
package org.sdsc.pdbproject;

import org.junit.Test;
import static org.junit.Assert.*;
/*
 * Tests if all the functions in the FeatureExtractor are
 * working appropriatley.
 */
public class FeatureExtractorTest {
    private FeatureExtractor testFeatureExtractor;
    public FeatureExtractorTest(){
	testFeatureExtractor = new FeatureExtractor();
    }
    
    //Tests the RCSB_PDB_Counter method
    @Test
    public void RCSB_PDB_CounterTest(){
	String assertString = "The RCSB_PDB_CounterTest is fault";
	String body = "Hello my name is RCSB PDB";
	int testCount = testFeatureExtractor.RCSB_PDB_Counter(body);
	assertTrue(assertString, testCount == 1);
    }
}
