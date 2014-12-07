package org.sdsc.pdbproject;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Java Packages
 */
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Test class for the list opeartions
 */
public class PdbListTest {
    private static String MAINTEST = "MainTest: ";
    private static String filename = "unreleasedPDBid.csv";
    private static PdbList testList;

    /**
     * Instantiates a new Unrel iD test.
     */
    public PdbListTest() {
        // a) Check if appropriate file is available
        testList = new PdbList(filename);
    }

    /**
     * Check if a null object was returned.
     */
    @Test
    public void testcreatePdbList() {
        assertNotNull("PdbList was not called: ", testList);
    }

    /**
     * Check if the size of list = lines in file - 1
     */
    @Test
    public void testsize() {
        // check if line count matches ID count
        int lines = 0;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException nof) {
            System.out.println(MAINTEST + "Need to add the unreleasedPDBid.csv file");
        }

        try {
            while (reader.readLine() != null) lines++;
        } catch (IOException IO) {
            System.out.println(MAINTEST + "An IO operation error");
        }
        String testa = (MAINTEST + "TestList length =" + testList.size());
        String testb = (" Total Lines =" + lines);
        assertTrue(testa + testb, testList.size() == lines - 1);
    }
}