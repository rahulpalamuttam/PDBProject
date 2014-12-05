//Written by Rahul Palamuttam
package org.sdsc.pdbproject;

import java.lang.StringBuffer;
import java.util.List;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class is used to store the unreleased ID's
 * in a hashmap. I followed the implementation of the
 * the FNV Hash algorithm and used the same variables.
 * Source : http://www.java2s.com/Code/Java/Development-Class/FNVHash.htm
 * The majority of the unlabeled ID's that will be filtered by the regexp
 * Will not be actual unreleased ID's. The List data structure would take
 * O(n) time for these - which I do not want for irrelevant IDs.
 */

public class UnrelIDHash {
    // Private class to eliminate compiler warnings using generics
    private class StringList extends ArrayList<String>{
	
    }
    private StringList[] HashTable;
    private int[] ListSizes;
    private int HashSize;
    private static final int LARGENUM = 20000000; // 20 million chars
    private static final int MAXLISTSIZE = 500;
    private static final int FNV_32_INIT=0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;

    /*
     * constructor to load unreleased ID's
     */
    public UnrelIDHash(String filename){
	ListSizes = new int[MAXLISTSIZE];
	FileReader file = null;
	BufferedReader reader = null;

	try{
	    file = new FileReader(filename);
	    reader = new BufferedReader(file);
	    
	    // init the array and each of the lists
	    HashSize = getFileSize(reader) * 2;
	    HashTable = new StringList[HashSize];
	    for(int i = 0; i < HashSize; i++) HashTable[i] = new StringList();
	    
	    //Read and add
	    reader.readLine();
	    String line = reader.readLine();
	    while(line != null){
		/* Parsing operation goes here */
		String[] fields = line.split("\",\"");
		line = reader.readLine();
		if(fields.length <= 2){ 
		    continue;
		} else {
		    StringList list = HashTable[HashFunc(fields[1])];
		    list.add(fields[1]);
		}
	    }
	} catch (FileNotFoundException fne){
	    System.out.println("UnrelIDHash: The file " + filename + "is not found");
	    HashTable = null;
	} catch (IOException ioe){
	    System.out.println("UnrelIDHash: Problem reading file " + filename);
	    HashTable = null;
	}
	loadListSizes();
    }

    // based on the FNV Hash algorithm
    private int HashFunc(String value){
	int hash = FNV_32_INIT;
	final int len = value.length();
	for(int i = 0; i < len; i++) {
	    hash *= FNV_32_PRIME;
	    hash += value.charAt(i);
	}
	return Math.abs(hash % HashSize);
	//return value.hashCode() % HashSize;
    }

    private static int getFileSize(BufferedReader reader){
	int lines = 0;
	try{
	    reader.mark(LARGENUM); // 20m chars can be read
	    while (reader.readLine() != null) lines++;
	    reader.reset();
	} catch (IOException IO){
	    System.out.println("IO exception with BufferedReader.mark()");
	}
	return lines;
    }
    
    // loads the respective sizes of the ArrayLists in the HashTable
    private void loadListSizes(){
	for(StringList s: HashTable){
	    ListSizes[s.size()]++;
	}
    }

    public void put(String value){
	int index = HashFunc(value);
	StringList listAtIndex = HashTable[index];
	listAtIndex.add(value);
    }

    // prints the entire hashmap (where values are assigned)
    public String toString(){
	StringBuffer output = new StringBuffer();
	// for(int i = 0; i < HashSize; i++){
	//     if(HashTable[i].size() > 0){
	// 	output.append(i + ": " + HashTable[i] + "\n");
	//     }
	// }
	for(int i = 0; i < MAXLISTSIZE; i++){
	    if(ListSizes[i] > 0){
		output.append("Lists with size " + i + " " + ListSizes[i] + "\n");
	    }
	}
	return output.toString();
    }

    /*
     * Checks to see if a string is contained in the hash table.
     * The input string is converted to uppercase
     */
    public boolean contains(String value){
	int index = HashFunc(value.toUpperCase());
	StringList listAtIndex = HashTable[index];
	return listAtIndex.contains(value);
    }
    
}
