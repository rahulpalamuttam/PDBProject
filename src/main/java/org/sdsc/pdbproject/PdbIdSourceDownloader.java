package org.sdsc.pdbproject;

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
    private static String CURRENT_URL = "http://www.rcsb.org/pdb/rest/customReport.xml?pdbids=*&customReportColumns=structureId,releaseDate,pdbDoi";
    private static String OBSOLETE_URL = "http://www.rcsb.org/pdb/rest/getObsolete";
    private static String UNRELEASED_URL = "http://www.rcsb.org/pdb/rest/getUnreleased";
    private static List<XML> currentRecords;
    private static List<XML> unreleasedRecords;
    private static List<XML> obsoleteRecords;

    public PdbIdSourceDownloader() {
        loadCurrentRecords();
        loadObsoleteRecords();
        loadUnreleasedRecords();
    }

    private static void loadCurrentRecords() {
        XML document;
        try {
            URL curr = new URL(CURRENT_URL);
            document = new XMLDocument(curr);
            currentRecords = document.nodes("//record");
        } catch (Exception e) {

        }

    }

    private static void loadUnreleasedRecords() {
        XML document;
        try {
            URL curr = new URL(UNRELEASED_URL);
            document = new XMLDocument(curr);
            unreleasedRecords = document.nodes("//record");
        } catch (Exception e) {

        }

    }

    private static void loadObsoleteRecords() {
        XML document;
        try {
            URL curr = new URL(OBSOLETE_URL);
            document = new XMLDocument(curr);
            obsoleteRecords = document.nodes("//PDB");
        } catch (Exception e) {

        }

    }

    public static PdbHashTable getPdbHashTable() {
        int tableSize = currentRecords.size() + unreleasedRecords.size() + obsoleteRecords.size();
        PdbHashTable hashTable = new PdbHashTable(tableSize);
        // load in the current records
        putCurrentInHashTable(hashTable);
        // load in the obsolete record
        putObsoleteInHashTable(hashTable);
        // load in the unreleased records
        putUnreleasedInHashTable(hashTable);
        return hashTable;
    }

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
            }
        }
    }

    private static void putObsoleteInHashTable(PdbHashTable hashTable) {
        for (XML record : obsoleteRecords) {
            String idName = record.xpath("//@structureId").get(0);
            hashTable.put(idName, null, null);
        }
    }

    private static void putUnreleasedInHashTable(PdbHashTable hashTable) {
        for (XML record : unreleasedRecords) {
            String idName = record.xpath("//@structureId").get(0);
            hashTable.put(idName, null, null);
        }
    }

    public static PdbList getPdbList() {

        return null;
    }


}
