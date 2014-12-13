package org.sdsc.pdbproject;

import org.junit.Test;

import static org.junit.Assert.*;

public class PdbIdSourceDownloaderTest {
    /**
     * Only activate this test if you fear the data extraction step differs
     * between loading the serialized PDBID's and downloading the XML files and constructing
     * the PDBID's.
     *
     * @throws Exception
     */
    @Deprecated
    public void testGetPdbHashTable() throws Exception {
        PdbHashTable TestHash = PdbIdSourceDownloader.getPdbHashTable();
        System.out.println(TestHash);
        PdbHashTable ComparHash = PdbIdSourceDownloader.getPdbHashTable();
        System.out.println(ComparHash);
        PdbIdSourceDownloader.deleteFile();
        String assertString = "Your hashing schemes from downloading from URL and loading from file differ";
        assertTrue(assertString, TestHash.toString().equals(ComparHash.toString()));
    }

    @Test
    public void testGetPdbList() throws Exception {

    }
}