package org.sdsc.pdbproject;

import java.io.Serializable;
import java.util.Date;

/**
 * Class meant to store information about PdbIds.
 * Currently stores <IDNAME, date released, doi>.
 * For PDB ID's that are obsolete or unreleased they
 * will have a null date and doi field.
 *
 * @author Rahul Palamuttam
 */
public class PdbId implements Serializable {
    private String IdName;
    private String doi;
    private Date dateReleased;

    /**
     * Instantiates a new Pdb Id.
     *
     * @param Id        the id
     * @param doiString the doi string
     * @param date      the date
     */
    public PdbId(String Id, String doiString, Date date) {
        IdName = Id;
        doi = doiString;
        dateReleased = date;
    }

    /**
     * Checks if the PdbId is current.
     * It is not current if a release date has not been set.
     * That it is not released or it has been obsolete.
     *
     * @return whether it is Released
     */
    public boolean isReleased() {
        return dateReleased != null;
    }

    /**
     * Overloaded method. Checks if the PdbId is current
     * given the date. If the input date is after the release date
     * then the PdbId has been released for that date.
     * If the release date is null then it also means an unreleased Id.
     *
     * @param date the date to check if it was released
     * @return whether it has been released at the date
     */
    public boolean isReleased(Date date) {
        if (dateReleased == null) return false;
        return date.after(dateReleased);
    }

    public String IdName() {
        return IdName;
    }

    public String toString() {
        return IdName + dateReleased + doi;
    }

}
