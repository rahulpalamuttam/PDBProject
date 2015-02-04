package org.sdsc.pdbproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PDBPatternFinderTest {

    PDBPatternFinder patternFinder;

    @Before
    public void setUp() throws Exception {
        patternFinder = new PDBPatternFinder();
    }

    @Test
    public void testConcatenatePatterns() throws Exception {
        String one = "hello";
        String two = "bonjour";
        String three = "jsklajdlksa|";
        String four = "|asd;sadklsadsa";
        assertEquals(patternFinder.concatenatePatternStrings(one, two, three, four), one + "|" + two + "|" + three + "|" + four);
    }

    @Test
    public void testFindPattern() throws Exception {
        String text1 = "asdsadaPDB ID:abd2asdsada";
        assertTrue(patternFinder.findPattern("abd2", text1));

    }

    @Test
    public void testAntiFindPattern() throws Exception {
        String text1 = "asdsadaPDB ID:abd42asdsada";
        assertFalse(patternFinder.findPattern("abd2", text1));

    }

    @Test
    public void testFindPattern1() throws Exception {
        String text2 = "asdsahttp://dx.doi.org/10.2210/pdbabd2231k";
        assertTrue(patternFinder.findPattern("abd2", text2));
    }

    @Test
    public void testAntiFindPattern1() throws Exception {
        String text2 = "asdsahttp://dx.doi.org/10.2210/pdbabd2231k";
        assertFalse(patternFinder.findPattern("abd4", text2));
    }

    @Test
    public void testFindPattern2() throws Exception {
        String text3 = "ext-link-type=\"pdb\" xlink:href\"abd2\">jklsadas";
        assertTrue(patternFinder.findPattern("abd2", text3));
    }

    @Test
    public void testAntiFindPattern2() throws Exception {
        String text3 = "ext-link-type=\"pdb\" xlink:href\"abd2\">jklsadas";
        assertFalse(patternFinder.findPattern("abdad2", text3));
    }
}