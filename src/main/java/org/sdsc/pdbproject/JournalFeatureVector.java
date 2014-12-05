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
    ArrayList<String> Id;
    String ContextLine;
    public JournalFeatureVector(String file, String Context){
	FileName = file;
	Id = null;
	ContextLine = Context;
    }

    public void changeFileName(String file){
	FileName = file;
    }

    public void changeId(ArrayList<String> idNum){
	Id = idNum;
    }

    public void changeContextLine(String line){
	ContextLine = line;
    }

    public String getContextLine(){
	return ContextLine;
    }

    public String toString(){
	StringBuffer output = new StringBuffer();
	output.append(FileName + "||" + Id + "||" + ContextLine.substring(0, 50));
	return output.toString();
    }
}
