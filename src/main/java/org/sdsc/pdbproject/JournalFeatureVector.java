package org.sdsc.pdbproject;


import java.util.ArrayList;
import java.io.Serializable;

/**
 * o * Custom class that models a feature vector.
 * It must implement the interface Serializable so that copies
 * of the entire project can be distributed along with its fields.
 * NOTE: ALL FIELDS MEMBERS MUST BE SERIALIZABLE
 *
 * @author Rahul Palamuttam
 */
public class JournalFeatureVector implements Serializable {
    private int RCSB_PDB_occurrences;
    private int Protein_Data_Bank_count;
    private String FileName;
    private String context;
    private ArrayList<String> NegativeIdList;
    private ArrayList<String> PositiveIdList;

    public int getRCSBnum() {
        return RCSB_PDB_occurrences;
    }

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

    public ArrayList<String> getNegativeIdList() {
        return NegativeIdList;
    }

    public JournalFeatureVector setNegativeIdList(ArrayList<String> negativeIdList) {
        this.NegativeIdList = negativeIdList;
        return this;
    }

    public ArrayList<String> getPositiveIdList() {
        return PositiveIdList;
    }

    public JournalFeatureVector setPositiveIdList(ArrayList<String> positiveIdList) {
        this.PositiveIdList = positiveIdList;
        return this;
    }

    public String getContext() {
        return context;
    }

    public JournalFeatureVector setContext(String context) {
        this.context = context;
        return this;
    }


    public String toString() {
        StringBuffer output = new StringBuffer();
        int startIndex = context.length() / 2;
        int endIndex = context.length() / 2;
        ArrayList<String> combination = new ArrayList<String>();
        combination.addAll(NegativeIdList);
        combination.addAll(PositiveIdList);
        if (combination.size() == 1) {
            startIndex = context.indexOf(combination.get(0)) - 100;
            if (startIndex < 0) startIndex = 0;
            endIndex = context.indexOf(combination.get(0)) + 100;
            if (endIndex >= context.length()) endIndex = context.length() - 1;
        }
        if (combination.size() > 1) {
            for (String id : combination) {
                int idIndex = context.indexOf(id);
                if (idIndex > endIndex) {
                    endIndex = idIndex;
                }
                if (idIndex < startIndex) {
                    startIndex = idIndex;
                }
            }
        }
        String line = context.substring(startIndex, endIndex);
        String abbreviated = line.length() > 250 ? line.substring(0, 250) : line;
        output.append(FileName + "\n");
        output.append("Negative ID's:" + NegativeIdList + "\n");
        output.append("Positive ID's:" + PositiveIdList + "\n");
        output.append("Context: " + abbreviated + "\n");
        output.append("RCSB_PDB_occurrences :" + RCSB_PDB_occurrences + "\n");
        output.append("Protein Data Bank occurrences: " + Protein_Data_Bank_count + "\n");

        return output.toString();
    }

}
