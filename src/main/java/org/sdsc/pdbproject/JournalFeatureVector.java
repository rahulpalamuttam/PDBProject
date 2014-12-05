//Written by Rahul Palamuttam
package org.sdsc.pdbproject;

import java.lang.StringBuffer;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/*
 * This is a custom class used for creating the feature vectors.
 * It must implement the interface Serializable so that copies of
 * the entire object can be distributed along with its fields.
 * NOTE: ALL FIELD MEMBERS MUST BE SERIALIZABLE
 */

public  class JournalFeatureVector implements Serializable{
    String FileName;
    String ContextLine;
    //Contains a List of ID's
    ArrayList<String> NegativeIdList;
    ArrayList<String> PositiveIdList;
    int RCSB_PDB_occurrences; // number of times "RCSB PDB" occurs in the file
    int Protein_Data_Bank_count; // number of times "Protein Data Bank" occurs in the file
    public JournalFeatureVector(int RCSBnum,int P_D_B, String file, String Context, ArrayList<String> NegIds){
	RCSB_PDB_occurrences = RCSBnum;
	Protein_Data_Bank_count = P_D_B;
	FileName = file;
	NegativeIdList = NegIds;
	ContextLine = Context;
    }

    public void changeFileName(String file){
	FileName = file;
    }

    public void changeNegativeIdList(ArrayList<String> idNum){
	NegativeIdList = idNum;
    }
    
    public ArrayList<String> getNegativeIdList(){
	return NegativeIdList;
    }

    public void changeContextLine(String line){
	ContextLine = line;
    }

    public String getContextLine(){
	return ContextLine;
    }

    public int getRCSBCount(){
	return RCSB_PDB_occurrences;
    }

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
