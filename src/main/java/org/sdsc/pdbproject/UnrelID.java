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
 */

public class UnrelID{
    private static List<String> IDLIST;

    /*
     * constructor to load unreleased ID's
     */
    public UnrelID(String filename){
	FileReader file = null;
	BufferedReader reader = null;
	IDLIST = new ArrayList<String>();
	try{
	    file = new FileReader(filename);
	    reader = new BufferedReader(file);
	    
	    reader.readLine();
	    String line = reader.readLine();
	    while(line != null){
		/* Parsing operation goes here */
		String[] fields = line.split("\",\"");
		line = reader.readLine();
		if(fields.length <= 2) continue;
		else IDLIST.add(fields[1]);
	    }
	} catch (FileNotFoundException fne){
	    System.out.println("MAIN: The file " + filename + "is not found");
	    IDLIST = null;
	} catch (IOException ioe){
	    System.out.println("MAIN: Problem reading file " + filename);
	    IDLIST = null;
	}
	
    } 

    public boolean contains(String element){
	return IDLIST.contains(element.toUpperCase());
    }

    public int size(){
	return IDLIST.size();
    }
}
