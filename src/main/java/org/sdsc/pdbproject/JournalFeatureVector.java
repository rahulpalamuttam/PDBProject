package org.sdsc.pdbproject;

import java.util.ArrayList;
import java.io.Serializable;

import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.regression.LabeledPoint;

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
    private int PDBCount;
    private String FileName;
    private String context;
    private ArrayList<String> NegativeIdList;
    private ArrayList<String> PositiveIdList;

    public LabeledPoint FeatureLabel() {
        double RCSB = (RCSB_PDB_occurrences > 0) ? 1 : 0;
        double ProtDatBankC = (Protein_Data_Bank_count > 0) ? 1 : 0;
        double PDBC = (PDBCount > 0) ? 1 : 0;
        double neg = (NegativeIdList.size() > 0) ? 1 : 0;
        double pos = (PositiveIdList.size() > 0) ? 1 : 0;
        return new LabeledPoint(pos, Vectors.dense(RCSB, ProtDatBankC, PDBC));
    }

    public Vector FeatureVector() {
        double RCSB = (RCSB_PDB_occurrences > 0) ? 1 : 0;
        double PDBC = (Protein_Data_Bank_count > 0) ? 1 : 0;
        double neg = (NegativeIdList.size() > 0) ? 1 : 0;
        double PDBCounter = (PDBCount > 0) ? 1 : 0;
        return Vectors.dense(RCSB, PDBC, PDBCounter);
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

    public int getRCSBCount() {
        return RCSB_PDB_occurrences;
    }

    public int getProtDatBanCount() {
        return Protein_Data_Bank_count;
    }

    public int getPDBCount() {
        return PDBCount;
    }

    public JournalFeatureVector setPDBCount(int p_d_b) {
        this.PDBCount = p_d_b;
        return this;
    }

    public String toString() {
        StringBuffer output = new StringBuffer();
        int startIndex = context.length() / 2;
        int endIndex = context.length() / 2;
        ArrayList<String> chosen = (NegativeIdList.size() > PositiveIdList.size()) ? NegativeIdList : PositiveIdList;
        if (chosen.size() == 1) {
            startIndex = context.indexOf(chosen.get(0)) - 100;
            if (startIndex < 0) startIndex = 0;
            endIndex = context.indexOf(chosen.get(0)) + 100;
            if (endIndex >= context.length()) endIndex = context.length() - 1;
        } else if (chosen.size() > 1) {
            for (String id : chosen) {
                int idIndex = context.indexOf(id);
                if (idIndex > endIndex) {
                    endIndex = idIndex;
                }
                if (idIndex < startIndex) {
                    startIndex = idIndex;
                }
            }
        } else {
        }
        String line = context.substring(startIndex, endIndex);
        String abbreviated = line.length() > 250 ? line.substring(0, 250) : line;
        output.append(FileName + "\n");
        output.append("Negative ID's:" + NegativeIdList + "\n");
        output.append("Positive ID's:" + PositiveIdList + "\n");
        output.append("Context: " + abbreviated + "\n");
        output.append("RCSB_PDB_occurrences :" + RCSB_PDB_occurrences + "\n");
        output.append("Protein Data Bank occurrences: " + Protein_Data_Bank_count + "\n");
        output.append("PDB Count : " + PDBCount + "\n");
        return output.toString();
    }

}
