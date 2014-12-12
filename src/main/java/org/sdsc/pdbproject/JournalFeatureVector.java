package org.sdsc.pdbproject;

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
public class JournalFeatureVector implements Serializable{
    private int RCSB_PDB_occurrences;
    private int Protein_Data_Bank_count;
    private String FileName;
    private String context;
    private ArrayList<String> NegativeIdList;

    public JournalFeatureVector setRCSBnum(int rcsBnum) {
        this.RCSB_PDB_occurrences = rcsBnum;
        return this;
    }

    public JournalFeatureVector setP_D_B(int p_d_b) {
        this.Protein_Data_Bank_count = p_d_b;
        return this;
    }

    public JournalFeatureVector setFileName(String fileName) {
        this.FileName = fileName;
        return this;
    }

    public JournalFeatureVector setContext(String context) {
        this.context = context;
        return this;
    }

    public JournalFeatureVector setNegativeIdList(ArrayList<String> negativeIdList) {
        this.NegativeIdList = negativeIdList;
        return this;
    }

    public ArrayList<String> getNegativeIdList(){
        return NegativeIdList;
    }

    public String getContext(){
        return context;
    }

    public int getRCSBCount(){
        return RCSB_PDB_occurrences;
    }
    public String toString(){
        StringBuffer output = new StringBuffer();
        String line = (context.length() > 50)? context.substring(0,50) : context;
        output.append(FileName + "||" + NegativeIdList + "||" + line +
                "||" + RCSB_PDB_occurrences + "||" + Protein_Data_Bank_count);
        return output.toString();
    }

}
