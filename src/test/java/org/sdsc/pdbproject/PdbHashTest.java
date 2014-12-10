package org.sdsc.pdbproject;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test class for PdbHashTable table functionality
 *
 * @author Rahul Palamuttam
 */
public class PdbHashTest {
    private static PdbHashTable TestHash;
    private String RANDOM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    /**
     * Instantiates a new Unrel iD hash test.
     */
    public PdbHashTest() {
        //PdbIdSourceDownloader downloader = new PdbIdSourceDownloader();
        //TestHash = downloader.getPdbHashTable();
        TestHash = new PdbHashTable(700000);
        //random String generation
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 36; j++) {
                for (int k = 0; k < 36; k++) {
                    for (int l = 0; l < 36; l++) {
                        String a = "" + i;
                        String b = RANDOM.substring(j, j + 1);
                        String c = RANDOM.substring(k, k + 1);
                        String d = RANDOM.substring(l, l + 1);
                        TestHash.put(a + b + c + d, null, null);
                    }
                }
            }
        }
    }

    /**
     * Tests if the Hash table was created.
     */
    @Test
    public void testcreateUnreleasedHash() {
        String assertString = "PdbHashTable was not called: ";
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
     * Check if contains is functional.
     */
    @Test
    public void testcontains() {
        String assertString = "PdbHashTable.testcontains() said: ";
        String random = "sdfs";
        String existing = "5fsd";
        Boolean notTrue = TestHash.contains(random);
        Boolean isTrue = TestHash.contains(existing);
        assertFalse(assertString + random + " does not exist", notTrue);
        assertTrue(assertString + existing + " does exist", isTrue);

    }


}
