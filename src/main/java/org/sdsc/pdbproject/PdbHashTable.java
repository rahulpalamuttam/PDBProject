package org.sdsc.pdbproject;

import java.lang.StringBuffer;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.Date;

/**
 * HashTable for storing PDB ID's. It implements Serializable for broadcasting purposes.
 * I followed the implementation of the Fowler-Noll-vo Hash algorithm and used the same
 * variables from the java2s site. The majority of the unlabeled ID's that will be
 * filtered by the regexp will not be actual unreleased ID's. The List data structure
 * would take O(n) time to determine the irrelevant ID's. Run the ListvsHashTest to see
 * the performance gains for yourself! Note that I use nanotime and not a serious java
 * performance analyzer.
 *
 * @author Rahul Palamuttam
 * @source http://www.java2s.com/Code/Java/Development-Class/FNVHash.htm
 */

public class PdbHashTable implements Serializable {
    private static final int MAXLISTSIZE = 500;
    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;
    private PdbIdList[] HashTable;
    private int[] ListSizes;
    private int HashSize;

    /**
     * Constructor to load a new HashTable
     *
     * @param size the size
     */
    public PdbHashTable(int size) {
        ListSizes = new int[MAXLISTSIZE];
        // init the array and each of the lists
        HashSize = size * 5;
        ListSizes[0] = HashSize;
        HashTable = new PdbIdList[HashSize];
        for (int i = 0; i < HashSize; i++) HashTable[i] = new PdbIdList();
    }

    /**
     * Construct a PdbId and put it in.
     *
     * @param id   the id
     * @param doi  the doi
     * @param date the date
     */
    public void put(String id, String doi, Date date) {
        PdbId Pdbid = new PdbId(id, doi, date);
        put(Pdbid);
    }

    /**
     * Put a PdbId
     *
     * @param id the id
     */
    public void put(PdbId id) {
        int Hashcode = HashFunc(id);
        PdbIdList indexList = HashTable[Hashcode];
        // update list size
        int listsize = indexList.size();
        ListSizes[listsize]--;
        ListSizes[listsize + 1]++;
        // insert
        indexList.add(id);
    }

    /**
     * Generates a hash value based on FNV
     *
     * @param id the PdbId to be hashed
     * @return the hashcode
     */
    private int HashFunc(PdbId id) {
        return HashFunc(id.IdName());

    }

    /**
     * Overloaded/helper method that
     *
     * @param value String to be hashed
     * @return the hashed value
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
     * @return return the printed table
     */
    public String printHashTable() {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < HashSize; i++) {
            if (HashTable[i].size() > 0) {
                String line = i + ": " + HashTable[i] + "\n";
                output.append(line);
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
        String upperCaseValue = value.toUpperCase();
        int index = HashFunc(upperCaseValue);
        PdbIdList listAtIndex = HashTable[index];
        return listAtIndex.contains(upperCaseValue);
    }

    /**
     * Checks if a PDB ID is not released.
     * Return false if the PDB ID is current or
     * does not exist.
     *
     * @param value the value
     * @return the boolean
     */
    public boolean isNotReleased(String value) {
        String upperCaseValue = value.toUpperCase();
        int index = HashFunc(upperCaseValue);
        // get list and see if id exists
        PdbIdList listAtIndex = HashTable[index];
        int indexAtList = listAtIndex.indexOf(value);
        if (indexAtList == -1) return false; // nonexistent ID's
        // get id and check if its released
        PdbId IdAtIndex = listAtIndex.get(indexAtList);
        return !(IdAtIndex.isReleased());
    }

    /**
     * Checks if a PDB ID is released.
     * returns false if the PDB ID is not released
     * or does not exist.
     */
    public boolean isReleased(String value) {
        String upperCaseValue = value.toUpperCase();
        int index = HashFunc(upperCaseValue);
        // get list and see if id exists
        PdbIdList listAtIndex = HashTable[index];
        int indexAtList = listAtIndex.indexOf(value);
        if (indexAtList == -1) return false; // nonexistent ID's
        // get id and check if its released
        PdbId IdAtIndex = listAtIndex.get(indexAtList);
        return IdAtIndex.isReleased();
    }

    /**
     * Private class to eliminate compiler warnings using generics
     */
    private class PdbIdList extends ArrayList<PdbId> {
        /**
         * Overrides the contains function of ArrayList.
         * Compares PdbId strings
         *
         * @param value the PdbId to compare to
         */
        public boolean contains(String value) {
            for (PdbId i : this) {
                if (i.IdName().equals(value)) return true;
            }
            return false;
        }

        /**
         * Overrides indexOf. Returns the index if the String value
         * is in the list. Otherwise returns -1.
         *
         * @param value the value
         * @return the int
         */
        public int indexOf(String value) {
            String upperCaseValue = value.toUpperCase();
            int length = this.size() - 1;
            for (; length > -1; length--) {
                if (this.get(length).IdName().equals(upperCaseValue)) break;
            }
            return length;
        }
    }
}
