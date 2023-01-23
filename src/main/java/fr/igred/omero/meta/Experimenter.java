/*
 *  Copyright (C) 2020-2023 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
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
public interface Experimenter extends RemoteObject<ExperimenterData> {

    /**
     * Returns the first name of the experimenter.
     *
     * @return see above.
     */
    default String getFirstName() {
        return asDataObject().getFirstName();
    }


    /**
     * Sets the first name of the experimenter.
     *
     * @param firstName The value to set.
     */
    default void setFirstName(String firstName) {
        asDataObject().setFirstName(firstName);
    }


    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    default String getLastName() {
        return asDataObject().getLastName();
    }


    /**
     * Sets the last name of the experimenter.
     *
     * @param lastName The value to set.
     */
    default void setLastName(String lastName) {
        asDataObject().setLastName(lastName);
    }


    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    default String getUserName() {
        return asDataObject().getUserName();
    }


    /**
     * Returns the e-mail of the experimenter.
     *
     * @return see above.
     */
    default String getEmail() {
        return asDataObject().getEmail();
    }


    /**
     * Sets the e-mail of the experimenter.
     *
     * @param email The value to set.
     */
    default void setEmail(String email) {
        asDataObject().setEmail(email);
    }


    /**
     * Returns the institution where the experimenter works.
     *
     * @return see above.
     */
    default String getInstitution() {
        return asDataObject().getInstitution();
    }


    /**
     * Sets the institution where the experimenter works.
     *
     * @param institution The value to set.
     */
    default void setInstitution(String institution) {
        asDataObject().setInstitution(institution);
    }


    /**
     * Returns the groups the experimenter is a member of.
     *
     * @return See above.
     */
    List<Group> getGroups();


    /**
     * Returns the default Group for this Experimenter
     *
     * @return See above.
     */
    default GroupWrapper getDefaultGroup() {
        return new GroupWrapper(asDataObject().getDefaultGroup());
    }


    /**
     * Returns the middle name of the experimenter.
     *
     * @return see above.
     */
    default String getMiddleName() {
        return asDataObject().getMiddleName();
    }


    /**
     * Sets the middle name of the experimenter.
     *
     * @param middleName The value to set.
     */
    default void setMiddleName(String middleName) {
        asDataObject().setMiddleName(middleName);
    }


    /**
     * Returns {@code true} if the experimenter is active, {@code false} otherwise.
     *
     * @return See above.
     */
    default boolean isActive() {
        return asDataObject().isActive();
    }


    /**
     * Checks if supplied group id matches any group to which the current experimenter belongs to.
     *
     * @param groupId The id of the group.
     *
     * @return boolean {@code true}/{@code false} depending on the matching id found
     */
    default boolean isMemberOfGroup(long groupId) {
        return asDataObject().isMemberOfGroup(groupId);
    }


    /**
     * Returns {@code true} if the user is connected via LDAP.
     *
     * @return See above.
     */
    default boolean isLDAP() {
        return asDataObject().isLDAP();
    }

}
