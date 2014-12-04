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
 * This class is used to test the performance times
 * of the UnrelIDHash vs the UnrelIDList
 */

public class ListvsHashTest {
    public String TESTER="UnrelIDHashTest: ";
    public String filename = "unreleasedPDBid.csv";
    private static UnrelIDHash TestHash;
    private static UnrelID TestList;
    
    // load the two datastructures
    public ListvsHashTest(){
	long beforeHash = System.nanoTime();
	TestHash = new UnrelIDHash(filename);
	long afterHash = System.nanoTime();
	TestList = new UnrelID(filename);
	long afterList = System.nanoTime();
	// System.out.println("Nanoseconds taken to create HashObject: " + 
	// 		   (afterHash - beforeHash));
	// System.out.println("Nanoseconds taken to create ListObject: " +
	// 		   (afterList - afterHash));
    }

    // Compares the run time of finding an invalid object
    @Test
    public void FindUnavailable(){
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

    // Compares the run time of finding the first object in the List
    @Test
    public void FindEarlyAvailable(){
	String random = "1uJh";
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

    // Compares the run time of finding the Last object in the List
    @Test
    public void FindLast(){
	String random = "4WM8";
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

    // Compares the run time of finding the 5th object in the List
    @Test
    public void FindTwoNinety(){
	String random = "4D1R";
	long beforeHash = System.nanoTime();
	TestHash.contains(random);
	long afterHash = System.nanoTime();
	TestList.contains(random);
	long afterList = System.nanoTime();
	long hashTime = afterHash - beforeHash;
	long listTime = afterList - afterHash;
	String assertString = "Nanoseconds to find list[290] in Hash: " + hashTime + "\n";
	assertString += "Nanoseconds to find early list[290] in List: " + listTime + "\n";
	System.out.println(assertString);
	assertTrue(assertString, hashTime < listTime);
    }
}
