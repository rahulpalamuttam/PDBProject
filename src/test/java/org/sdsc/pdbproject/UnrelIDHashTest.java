package org.sdsc.pdbproject;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for UnrelIDHash table functionality
 *
 * @author Rahul Palamuttam
 */
public class UnrelIDHashTest {
    private static String filename = "unreleasedPDBid.csv";
    private static UnrelIDHash TestHash;

    /**
     * Instantiates a new Unrel iD hash test.
     */
    public UnrelIDHashTest() {
        TestHash = new UnrelIDHash(filename);
    }

    /**
     * Tests if the Hash table was created.
     */
    @Test
    public void testcreateUnreleasedHash() {
        String assertString = "UnrelIDHash was not called: ";
        assertNotNull(assertString, TestHash);
    }

    /**
     * Check if toString works.
     */
    @Test
    public void testtoString() {
        String assertString = "UnredIDHash.toString returned null: ";
        String testString = TestHash.toString();
        System.out.println(testString);
        assertNotNull(assertString, testString);
    }


    /**
     * Check if contains works.
     */
    @Test
    public void testcontains() {
        String assertString = "UnrelIDHash.testcontains() said: ";
        String random = "sdfs";
        String existing = "1ujh";
        Boolean notTrue = TestHash.contains(random);
        Boolean isTrue = TestHash.contains(existing);
        assertFalse(assertString + random + " exists", notTrue);
        assertTrue(assertString + random + "does not exist", isTrue);

    }
}
