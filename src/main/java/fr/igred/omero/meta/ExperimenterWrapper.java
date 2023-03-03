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


import fr.igred.omero.GenericObjectWrapper;
import omero.gateway.model.ExperimenterData;

import java.util.List;


/**
 * Class containing an ExperimenterData object.
 * <p> Wraps function calls to the ExperimenterData contained.
 */
public class ExperimenterWrapper extends GenericObjectWrapper<ExperimenterData> implements Experimenter {

    /**
     * Constructor of the class ExperimenterWrapper.
     *
     * @param experimenter The ExperimenterData to wrap in the ExperimenterWrapper.
     */
    public ExperimenterWrapper(ExperimenterData experimenter) {
        super(experimenter);
    }


    /**
     * @return See above.
     *
     * @deprecated Returns the ExperimenterData contained. Use {@link #asDataObject()} instead.
     */
    @Deprecated
    public ExperimenterData asExperimenterData() {
        return data;
    }


    /**
     * Returns the first name of the experimenter.
     *
     * @return see above.
     */
    @Override
    public String getFirstName() {
        return data.getFirstName();
    }


    /**
     * Sets the first name of the experimenter.
     *
     * @param firstName The value to set.
     */
    @Override
    public void setFirstName(String firstName) {
        data.setFirstName(firstName);
    }


    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    @Override
    public String getLastName() {
        return data.getLastName();
    }


    /**
     * Sets the last name of the experimenter.
     *
     * @param lastName The value to set.
     */
    @Override
    public void setLastName(String lastName) {
        data.setLastName(lastName);
    }


    /**
     * Returns the last name of the experimenter.
     *
     * @return see above.
     */
    @Override
    public String getUserName() {
        return data.getUserName();
    }


    /**
     * Returns the e-mail of the experimenter.
     *
     * @return see above.
     */
    @Override
    public String getEmail() {
        return data.getEmail();
    }


    /**
     * Sets the e-mail of the experimenter.
     *
     * @param email The value to set.
     */
    @Override
    public void setEmail(String email) {
        data.setEmail(email);
    }


    /**
     * Returns the institution where the experimenter works.
     *
     * @return see above.
     */
    @Override
    public String getInstitution() {
        return data.getInstitution();
    }


    /**
     * Sets the institution where the experimenter works.
     *
     * @param institution The value to set.
     */
    @Override
    public void setInstitution(String institution) {
        data.setInstitution(institution);
    }


    /**
     * Returns the groups the experimenter is a member of.
     *
     * @return See above.
     */
    @Override
    public List<GroupWrapper> getGroups() {
        return wrap(data.getGroups(), GroupWrapper::new, GroupWrapper::getName);
    }


    /**
     * Returns the default Group for this Experimenter
     *
     * @return See above.
     */
    @Override
    public GroupWrapper getDefaultGroup() {
        return new GroupWrapper(data.getDefaultGroup());
    }


    /**
     * Returns the middle name of the experimenter.
     *
     * @return see above.
     */
    @Override
    public String getMiddleName() {
        return data.getMiddleName();
    }


    /**
     * Sets the middle name of the experimenter.
     *
     * @param middleName The value to set.
     */
    @Override
    public void setMiddleName(String middleName) {
        data.setMiddleName(middleName);
    }


    /**
     * Returns {@code true} if the experimenter is active, {@code false} otherwise.
     *
     * @return See above.
     */
    @Override
    public boolean isActive() {
        return data.isActive();
    }


    /**
     * Checks if supplied group id matches any group to which the current experimenter belongs to.
     *
     * @param groupId The id of the group.
     *
     * @return boolean {@code true}/{@code false} depending on the matching id found
     */
    @Override
    public boolean isMemberOfGroup(long groupId) {
        return data.isMemberOfGroup(groupId);
    }


    /**
     * Returns {@code true} if the user is connected via LDAP.
     *
     * @return See above.
     */
    @Override
    public boolean isLDAP() {
        return data.isLDAP();
    }

}
