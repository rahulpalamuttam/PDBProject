package org.sdsc.pdbproject;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;


/**
 * Downloads the PDB IDs from the source URLS.
 * Downloads the current, obsolete, and unreleased IDs.
 * Outputs each PDB ID in some sort of tuple format consisting of
 * <PDB ID, (current|obsolete|unreleased), release/deposition date>
 * Current entries are released entries and are being used
 * Obsolete entries are no longer used
 * Unreleased ID's have yet to be used
 * I used the opensource jacabi xml parsing library for it's brevity in required code.
 *
 * @author Rahul Palamuttam
 * @source http://www.rcsb.org/pdb/rest/customReport.xml?pdbids=*&customReportColumns=structureId,releaseDate,pdbDoi
 * @source http://www.rcsb.org/pdb/test/getObsolete
 * @source http://www.rcsb.org/pdb/test/getUnreleased
 * @source http://xml.jcabi.com/
 */

public class PdbIdSourceDownloader {
    private static String HASHSERIALFILE = "PDBIDHash.ser";
    private static String CURRENT_URL = "http://www.rcsb.org/pdb/rest/customReport.xml?pdbids=*&customReportColumns=structureId,releaseDate,pdbDoi";
    private static String OBSOLETE_URL = "http://www.rcsb.org/pdb/rest/getObsolete";
    private static String UNRELEASED_URL = "http://www.rcsb.org/pdb/rest/getUnreleased";
    private static List<XML> currentRecords;
    private static List<XML> unreleasedRecords;
    private static List<XML> obsoleteRecords;


    /**
     * Helper method to load the current PDBID records information
     * from the XML webpage at the CURRENT_URL.
     */
    private static void loadCurrentRecords() {
        XML document;
        try {
            URL curr = new URL(CURRENT_URL);
            document = new XMLDocument(curr);
            currentRecords = document.nodes("//record");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Helper method to load the Unreleased PDBID records information
     * from the XML webpage at UNRELEASED_URL.
     */
    private static void loadUnreleasedRecords() {
        XML document;
        try {
            URL curr = new URL(UNRELEASED_URL);
            document = new XMLDocument(curr);
            unreleasedRecords = document.nodes("//record");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to load the Obsolete PDBID records information
     * from the XML webpage at the OBSOLETE_URL.
     */
    private static void loadObsoleteRecords() {
        XML document;
        try {
            URL curr = new URL(OBSOLETE_URL);
            document = new XMLDocument(curr);
            obsoleteRecords = document.nodes("//PDB");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets pdb hash table.
     * Constructs a PdbHashTable object based on
     * the XML structure lists. The PDBID's are parsed from the
     * XML structures. If the PdbHashTable serialized object
     * is saved in HASHSERIALFILE then the HashTable is loaded back
     * from the saved file. Otherwise it downloads, parses the XML webpages,
     * and constructs the hashtable.
     *
     * @return the pdb hash table
     */
    public static PdbHashTable getPdbHashTable() {

        PdbHashTable hashTable;
        File SerialObjectFile = new File(HASHSERIALFILE);
        if (SerialObjectFile.exists()) {
            hashTable = loadHashTable();
        } else {
            // Load the records from the URLs
            loadCurrentRecords();
            loadObsoleteRecords();
            loadUnreleasedRecords();
            int tableSize = currentRecords.size() + unreleasedRecords.size() + obsoleteRecords.size();
            hashTable = new PdbHashTable(tableSize);
            // put in the current records
            putCurrentInHashTable(hashTable);
            // put in the obsolete record
            putObsoleteInHashTable(hashTable);
            // put in the unreleased records
            putUnreleasedInHashTable(hashTable);
            writeHashTable(hashTable);
        }
        return hashTable;
    }

    /**
     * Loads the serialized object from HASHSERIALFILE.
     */
    private static PdbHashTable loadHashTable() {
        PdbHashTable HashTable = null;
        try {
            InputStream file = new FileInputStream(HASHSERIALFILE);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            HashTable = (PdbHashTable) input.readObject();
            input.close();
            buffer.close();
            file.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return HashTable;
    }

    /**
     * Writes the serialized hashtable object to HASHSERIALFILE.
     */
    private static void writeHashTable(PdbHashTable HashTable) {
        try {
            OutputStream file = new FileOutputStream(HASHSERIALFILE);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            output.writeObject(HashTable);
            output.close();
            buffer.close();
            file.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Helper function that parses the XML records in the currentRecords list
     * for PDBID's, releaseDate, and the pdbDoi.
     *
     * @param hashTable returns the hashtable with Current ID's
     */
    private static void putCurrentInHashTable(PdbHashTable hashTable) {
        for (XML record : currentRecords) {
            String idName = record.xpath("//dimStructure.structureId/text()").get(0);
            String date = record.xpath("//dimStructure.releaseDate/text()").get(0);
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String doi = record.xpath("//dimStructure.pdbDoi/text()").get(0);
            try {
                Date example = format.parse(date);
                hashTable.put(idName, doi, example);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Helper function that parses the XML records in the obsoleteRecords list
     * for PDBID's. There is no release date or doi associated with these.
     *
     * @param hashTable returns the hashtable with obsolete ID's added
     */
    private static void putObsoleteInHashTable(PdbHashTable hashTable) {
        for (XML record : obsoleteRecords) {
            String idName = record.xpath("//@structureId").get(0);
            hashTable.put(idName, null, null);
        }
    }

    /**
     * Helper function that parses the XML records in the unreleasedRecords list
     * for PDBID's. There is no release date or doi associated with these.
     * @param hashTable returns the hashtable with unreleased ID's added
     */
    private static void putUnreleasedInHashTable(PdbHashTable hashTable) {
        for (XML record : unreleasedRecords) {
            String idName = record.xpath("//@structureId").get(0);
            hashTable.put(idName, null, null);
        }
    }

    /**
     * Deletes the opened file
     */
    public static void deleteFile() {
        try {
            File HashFile = new File(HASHSERIALFILE);
            HashFile.delete();
        } catch (Exception som) {
            som.printStackTrace();
        }
    }



}
