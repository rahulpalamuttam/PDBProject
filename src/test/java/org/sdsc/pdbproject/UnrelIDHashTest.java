//Written by Rahul Palamuttam
package org.sdsc.pdbproject;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class is used to test the UnrelIDHash table functionality
 */
public class UnrelIDHashTest {
    private static String TESTER="UnrelIDHashTest: ";
    private static String filename = "unreleasedPDBid.csv";
    private static UnrelIDHash TestHash;
    
    // Check if appropriate files are available
    public UnrelIDHashTest() {
	TestHash = new UnrelIDHash(filename);
    }

    //Check if constructor worked.
    @Test
    public void testcreateUnreleasedHash(){
	String assertString = "UnrelIDHash was not called: ";
	assertNotNull(assertString, TestHash);
    }

    //Check if toString works
    @Test
    public void testtoString(){
	String assertString = "UnredIDHash.toString returned null: ";
	String testString = null;
	testString = TestHash.toString();
	System.out.println(testString);
	assertNotNull(assertString, testString);
    }
    
    //Check if contains works
    @Test
    public void testcontains(){
	String assertString = "UnrelIDHash.testcontains() returned true";
	String random = "sdfs";
	Boolean notTrue = TestHash.contains(random);
	assertFalse(assertString, notTrue);
	
    }
}
