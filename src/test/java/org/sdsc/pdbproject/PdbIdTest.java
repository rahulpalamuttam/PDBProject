package org.sdsc.pdbproject;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Tests the methods that are part of the PdbId class
 *
 * @author Rahul Palamuttam
 */
public class PdbIdTest {

    /**
     * Tests the date isReleased() function.
     * Makes sure that the function returns false for
     * null fields.
     *
     * @throws Exception
     */
    @Test
    public void testIsReleased() throws Exception {
        String assertStringa = "Fields are filled so should be released";
        String assertStringb = "fields are not filled so should be false";
        String id = "1UJH";
        String date = "1989-03-31";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date example = format.parse(date);
        String doi = "asdsada";
        PdbId rand = new PdbId(id, doi, example);
        PdbId unrnd = new PdbId(id, null, null);
        assertTrue(assertStringa, rand.isReleased());
        assertFalse(assertStringb, unrnd.isReleased());
    }

    /**
     * Tests the date isReleased(date) function.
     * Makes sure that the function returns false for
     * dates before the release date of the PdbId.
     *
     * @throws Exception
     */
    @Test
    public void testIsReleased1() throws Exception {
        String assertStringb = "fields are not filled so should be false";
        String id = "1UJH";
        String date = "1989-03-31";
        String beforeDate = "1989-03-30";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date example = format.parse(date);
        Date beforeExample = format.parse(beforeDate);
        String doi = "asdsada";
        PdbId rand = new PdbId(id, doi, example);
        PdbId unrnd = new PdbId(id, null, null);
        String assertStringa = "Pdb release " + example + " is After " + beforeExample;
        assertFalse(assertStringa, rand.isReleased(beforeExample));
        assertFalse(assertStringb, unrnd.isReleased());
    }
}