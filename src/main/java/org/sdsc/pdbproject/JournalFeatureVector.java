package org.sdsc.pdbproject;

import java.lang.StringBuffer;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;


/**
 * Custom class that models a feature vector.
 * It must implement the interface Serializable so that copies
 * of the entire project can be distributed along with its fields.
 * NOTE: ALL FIELDS MEMBERS MUST BE SERIALIZABLE
 *
 * @author Rahul Palamuttam
 */
public  class JournalFeatureVector implements Serializable{
    /**
     * The File name.
     */
    String FileName;
    /**
     * The Context line.
     */
    String ContextLine;
    /**
     * The Negative id list.
     */
    ArrayList<String> NegativeIdList;
    /**
     * The Positive id list.
     */
    ArrayList<String> PositiveIdList;
    /**
     * The RCSB _ pDB _ occurrences.
     */
    int RCSB_PDB_occurrences; // number of times "RCSB PDB" occurs in the file
    /**
     * The Protein _ data _ bank _ count.
     */
    int Protein_Data_Bank_count; // number of times "Protein Data Bank" occurs in the file

    /**
     * Instantiates a new Journal feature vector.
     *
     * @param RCSBnum The number of times "RCSB PDB" occurs
     * @param P_D_B The number of times "Protein Data Bank"
     * @param file The name of the file
     * @param Context The line
     * @param NegIds A list of the Negative ID's
     */
    public JournalFeatureVector(int RCSBnum,int P_D_B, String file, String Context, ArrayList<String> NegIds){
	RCSB_PDB_occurrences = RCSBnum;
	Protein_Data_Bank_count = P_D_B;
	FileName = file;
	NegativeIdList = NegIds;
	ContextLine = Context;
    }

    /**
     * Change file name.
     *
     * @param file the file
     */
    public void changeFileName(String file){
	FileName = file;
    }

    /**
     * Change negative id list.
     *
     * @param idNum the id num
     */
    public void changeNegativeIdList(ArrayList<String> idNum){
	NegativeIdList = idNum;
    }

    /**
     * Get negative id list.
     *
     * @return array list
     */
    public ArrayList<String> getNegativeIdList(){
	return NegativeIdList;
    }

    /**
     * Change context line.
     *
     * @param line the line
     */
    public void changeContextLine(String line){
	ContextLine = line;
    }

    /**
     * Get context line.
     *
     * @return the string
     */
    public String getContextLine(){
	return ContextLine;
    }

    /**
     * Get rCSB count.
     *
     * @return the int
     */
    public int getRCSBCount(){
	return RCSB_PDB_occurrences;
    }

    /**
     * Get protein _ data _ bank count.
     *
     * @return the int
     */
    public int getProtein_Data_BankCount(){
	return Protein_Data_Bank_count;
    }

    public String toString(){
	StringBuffer output = new StringBuffer();
	output.append(FileName + "||" + NegativeIdList + "||" + ContextLine.substring(0, 50) +
		      "||" + RCSB_PDB_occurrences + "||" + Protein_Data_Bank_count);
	return output.toString();
    }
}
