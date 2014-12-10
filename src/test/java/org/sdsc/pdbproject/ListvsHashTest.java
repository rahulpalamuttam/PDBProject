package org.sdsc.pdbproject;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Runs performance tests for comparing
 * a custom HashTable to a list.
 *
 * @author Rahul Palamuttam
 */

public class ListvsHashTest {
    private static PdbHashTable TestHash;
    private static PdbList TestList;
    public String filename = "unreleasedPDBid.csv";
    public long HashTableCreateTime;
    public long ListCreateTime;

    /**
     * Creates a TestList and TestHashTable.
     * Also loads run times for creating the structures.
     */
    public ListvsHashTest() {
        long beforeHash = System.nanoTime();
        TestHash = new PdbHashTable(100);
        long afterHash = System.nanoTime();
        TestList = new PdbList(filename);
        long afterList = System.nanoTime();
        HashTableCreateTime = afterHash - beforeHash;
        ListCreateTime = afterList - beforeHash;
    }

    /**
     * Compares the run time of finding invalid ids.
     * The list structure must iterate through all its entries.
     * The hash table doesn't.
     * Hence the hasTime < listTime.
     */
    @Test
    public void FindUnavailableTest() {
        String random = "0j.;";
        long beforeHash = System.nanoTime();
        TestHash.contains(random);
        long afterHash = System.nanoTime();
        TestList.contains(random);
        long afterList = System.nanoTime();
        long hashTime = afterHash - beforeHash;
        long listTime = afterList - afterHash;
        String assertString = "Nanoseconds to find list[NaN] in Hash: " + hashTime + "\n";
        assertString += "Nanoseconds to find list[NaN] in List: " + listTime + "\n";
        System.out.println(assertString);
        assertTrue(assertString, hashTime < listTime);
    }

    /**
     * Compares the run time of finding the first object in the List.
     * Since it is the first object hashTime should be greater than listTime.
     * This test has inconsistent results (especially with advanced processors).
     */
    @Deprecated
    public void FindFirst() {
        String random = TestList.get(0);
        long beforeHash = System.nanoTime();
        TestHash.contains(random);
        long afterHash = System.nanoTime();
        TestList.contains(random);
        long afterList = System.nanoTime();
        long hashTime = afterHash - beforeHash;
        long listTime = afterList - afterHash;
        String assertString = "Nanoseconds to find list[first] in Hash: " + hashTime + "\n";
        assertString += "Nanoseconds to find list[first] in List: " + listTime + "\n";
        System.out.println(assertString);
        assertTrue(assertString, hashTime > listTime);

    }

    /**
     * Find last object in the list.
     * Since it is the last object in the list,
     * hashTime should be less than the listTime.
     */
    @Test
    public void FindLast() {
        String random = TestList.get(TestList.size() - 1);
        long beforeHash = System.nanoTime();
        TestHash.contains(random);
        long afterHash = System.nanoTime();
        TestList.contains(random);
        long afterList = System.nanoTime();
        long hashTime = afterHash - beforeHash;
        long listTime = afterList - afterHash;
        String assertString = "Nanoseconds to find list[last] in Hash: " + hashTime + "\n";
        assertString += "Nanoseconds to find early list[last] in List: " + listTime + "\n";
        System.out.println(assertString);
        assertTrue(assertString, hashTime < listTime);
    }

    /**
     * Compare search times for finding two-hundred-ninetieth list element.
     */
    @Test
    public void FindTwoNineHundred() {
        String random = TestList.get(2900);
        long beforeHash = System.nanoTime();
        TestHash.contains(random);
        long afterHash = System.nanoTime();
        TestList.contains(random);
        long afterList = System.nanoTime();
        long hashTime = afterHash - beforeHash;
        long listTime = afterList - afterHash;
        String assertString = "Nanoseconds to find list[2900] in Hash: " + hashTime + "\n";
        assertString += "Nanoseconds to find early list[2900] in List: " + listTime + "\n";
        System.out.println(assertString);
        assertTrue(assertString, hashTime < listTime);
    }
}
