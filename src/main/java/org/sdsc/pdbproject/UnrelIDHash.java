package org.sdsc.pdbproject;

import java.lang.StringBuffer;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

/**
 * HashTable for storing PDB ID's. It implements Serializable for broadcasting purposes.
 * I followed the implementation of the Fowler-Noll-vo Hash algorithm and used the same
 * variables from the java2s site. The majority of the unlabeled ID's that will be
 * filtered by the regexp will not be actual unreleased ID's. The List data structure
 * would take O(n) time to determine the irrelevant ID's. Run the ListvsHashTest to see
 * the performance gains for yourself!
 *
 * @author Rahul Palamuttam
 * @source http://www.java2s.com/Code/Java/Development-Class/FNVHash.htm
 */

public class UnrelIDHash implements Serializable {

    /**
     * Private class to eliminate compiler warnings using generics
     */
    private class StringList extends ArrayList<String> {

    }

    private StringList[] HashTable;
    private int[] ListSizes;
    private int HashSize;
    private static final int LARGENUM = 20000000; // 20 million chars
    private static final int MAXLISTSIZE = 500;
    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;

    /**
     * Constructor to load a new HashTable
     *
     * @param filename the filename
     */
    public UnrelIDHash(String filename) {
        ListSizes = new int[MAXLISTSIZE];
        FileReader file = null;
        BufferedReader reader = null;
        try {
            file = new FileReader(filename);
            reader = new BufferedReader(file);

            // init the array and each of the lists
            HashSize = getFileSize(reader) * 2;
            HashTable = new StringList[HashSize];
            for (int i = 0; i < HashSize; i++) HashTable[i] = new StringList();

            //Read and add
            reader.readLine();
            String line = reader.readLine();
            while (line != null) {
                String[] fields = line.split("\",\"");
                line = reader.readLine();
                if (fields.length <= 2) {
                    continue;
                } else {
                    StringList list = HashTable[HashFunc(fields[1])];
                    list.add(fields[1]);
                }
            }
        } catch (FileNotFoundException fne) {
            System.out.println("UnrelIDHash: The file " + filename + "is not found");
            HashTable = null;
        } catch (IOException ioe) {
            System.out.println("UnrelIDHash: Problem reading file " + filename);
            HashTable = null;
        }
        loadListSizes();
    }

    /**
     * Generates a hash value based on FNV
     *
     * @param value the String to be hashed
     * @return the hashcode
     */
    private int HashFunc(String value) {
        int hash = FNV_32_INIT;
        final int len = value.length();
        for (int i = 0; i < len; i++) {
            hash *= FNV_32_PRIME;
            hash += value.charAt(i);
        }
        return Math.abs(hash % HashSize);
    }

    /**
     * @param reader Opened file
     * @return the number of lines in the file
     */
    private static int getFileSize(BufferedReader reader) {
        int lines = 0;
        try {
            reader.mark(LARGENUM); // 20m chars can be read
            while (reader.readLine() != null) lines++;
            reader.reset();
        } catch (IOException IO) {
            System.out.println("IO exception with BufferedReader.mark()");
        }
        return lines;
    }

    /**
     * Loads the respective sizes of the Lists in the HashTable
     */
    private void loadListSizes() {
        for (StringList s : HashTable) {
            ListSizes[s.size()]++;
        }
    }

    /**
     * Put void.
     *
     * @param value the value
     */
    public void put(String value) {
        int index = HashFunc(value);
        StringList listAtIndex = HashTable[index];
        listAtIndex.add(value);
    }

    /**
     * prints the HashTable list sizes (where values are assigned)
     */
    public String toString() {
        StringBuffer output = new StringBuffer();

        for (int i = 0; i < MAXLISTSIZE; i++) {
            if (ListSizes[i] > 0) {
                output.append("Lists with size " + i + " " + ListSizes[i] + "\n");
            }
        }
        return output.toString();
    }

    /**
     * Prints the entire table by printing the list at each index.
     *
     * @return
     */
    public String printTable() {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < HashSize; i++) {
            if (HashTable[i].size() > 0) {
                output.append(i + ": " + HashTable[i] + "\n");
            }
        }
        return output.toString();
    }

    /**
     * Checks to see if a string is contained in the HashTableable.
     * The input string is converted to uppercase.
     *
     * @param value the value
     * @return the boolean
     */
    public boolean contains(String value) {
        int index = HashFunc(value.toUpperCase());
        StringList listAtIndex = HashTable[index];
        return listAtIndex.contains(value);
    }

}
