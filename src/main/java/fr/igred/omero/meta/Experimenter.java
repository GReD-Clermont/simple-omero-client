/*
 *  Copyright (C) 2020-2023 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero.meta;


import fr.igred.omero.RemoteObject;
import omero.gateway.model.ExperimenterData;

import java.util.List;


/**
 * Interface to handle Experimenters on OMERO.
 */
public interface Experimenter extends RemoteObject {

    /**
     * Returns an {@link ExperimenterData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    ExperimenterData asDataObject();


    /**
     * Returns the first name of the experimenter.
     *
     * @return see above.
     */
    String getFirstName();


    /**
     * Sets the first name of the experimenter.
     *
     * @param firstName The value to set.
     */
    void setFirstName(String firstName);


    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    String getLastName();


    /**
     * Sets the last name of the experimenter.
     *
     * @param lastName The value to set.
     */
    void setLastName(String lastName);


    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    String getUserName();


    /**
     * Returns the e-mail of the experimenter.
     *
     * @return see above.
     */
    String getEmail();


    /**
     * Sets the e-mail of the experimenter.
     *
     * @param email The value to set.
     */
    void setEmail(String email);


    /**
     * Returns the institution where the experimenter works.
     *
     * @return see above.
     */
    String getInstitution();


    /**
     * Sets the institution where the experimenter works.
     *
     * @param institution The value to set.
     */
    void setInstitution(String institution);


    /**
     * Returns the groups the experimenter is a member of.
     *
     * @return See above.
     */
    List<? extends Group> getGroups();


    /**
     * Returns the default Group for this Experimenter
     *
     * @return See above.
     */
    Group getDefaultGroup();


    /**
     * Returns the middle name of the experimenter.
     *
     * @return see above.
     */
    String getMiddleName();


    /**
     * Sets the middle name of the experimenter.
     *
     * @param middleName The value to set.
     */
    void setMiddleName(String middleName);


    /**
     * Returns {@code true} if the experimenter is active, {@code false} otherwise.
     *
     * @return See above.
     */
    boolean isActive();


    /**
     * Checks if supplied group id matches any group to which the current experimenter belongs to.
     *
     * @param groupId The ID of the group.
     *
     * @return boolean {@code true}/{@code false} depending on the matching id found
     */
    boolean isMemberOfGroup(long groupId);


    /**
     * Returns {@code true} if the user is connected via LDAP.
     *
     * @return See above.
     */
    boolean isLDAP();

}
