package org.sdsc.pdbproject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * (?=X) indicates a zero width positive look ahead
 * Created by rahul on 2/3/15.
 */
public class PDBPatternFinder {
    String idPattern = "PDB ID:";
    String doiPattern = "http://dx.doi.org/10.2210/pdb";
    String linkPattern = "ext-link-type=\"pdb\" xlink:href\"";
    String PDBpattern = "PDB code: |PDB reference |PDB accession number ";
    String PDBbeforepattern = "(?<=.pdb)";
    String PDBafterpattern = "(?=PDB){0-40}";


    public String concatenatePatternStrings(String... strings) {
        StringBuilder build = new StringBuilder();

        for (String s : strings) {
            build.append(s + "|");
        }

        return build.toString().substring(0, build.lastIndexOf("|"));
    }

    public boolean findPattern(String pdbId, String context) {
        String patternCompileString = concatenatePatternStrings(idPattern, doiPattern, linkPattern);
        String braced = "(" + patternCompileString + pdbId + ")";
        Pattern pattern = Pattern.compile(braced);
        Matcher matcher = pattern.matcher(context);
        if (matcher.find()) {
            System.out.println(matcher.group());
            return true;
        }

        return false;
    }

}
