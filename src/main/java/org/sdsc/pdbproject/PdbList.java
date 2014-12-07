//Written by Rahul Palamuttam
package org.sdsc.pdbproject;

import java.util.List;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class is used to store the unreleased ID's.
 * The goal is to refactor this to use a hashmap instead
 * If you want to use it in clustered interface this needs to be fixed!
 * I NEED TO IMPLEMENT SERIALIZABLE!
 */

public class PdbList {
    private static List<String> IDLIST;

    /**
     * Instantiates a list of unreleased IDs.
     *
     * @param filename the filename
     */
    public PdbList(String filename) {
        FileReader file;
        BufferedReader reader;
        IDLIST = new ArrayList<String>();
        try {
            file = new FileReader(filename);
            reader = new BufferedReader(file);

            reader.readLine();
            String line = reader.readLine();
            while (line != null) {
                String[] fields = line.split("\",\"");
                line = reader.readLine();
                if (fields.length > 2) IDLIST.add(fields[1]);
            }
        } catch (FileNotFoundException fne) {
            System.out.println("MAIN: The file " + filename + "is not found");
            IDLIST = null;
        } catch (IOException ioe) {
            System.out.println("MAIN: Problem reading file " + filename);
            IDLIST = null;
        }

    }

    /**
     * Checks if the ID exists in the list
     *
     * @param element the String to search for
     * @return the boolean
     */
    public boolean contains(String element) {
        return IDLIST.contains(element.toUpperCase());
    }

    public String get(int index) {
        return IDLIST.get(index);
    }

    public int size() {
        return IDLIST.size();
    }
}
