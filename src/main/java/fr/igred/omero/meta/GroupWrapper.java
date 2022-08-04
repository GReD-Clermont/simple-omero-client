/*
 *  Copyright (C) 2020-2022 GReD
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


import fr.igred.omero.GenericObjectWrapper;
import omero.gateway.model.GroupData;

import java.util.List;


public class GroupWrapper extends GenericObjectWrapper<GroupData> {

    /** Indicates that the group is {@code Private} i.e. RW----. */
    public static final int PERMISSIONS_PRIVATE = GroupData.PERMISSIONS_PRIVATE;

    /** Indicates that the group is {@code Group} i.e. RWR---. */
    public static final int PERMISSIONS_GROUP_READ = GroupData.PERMISSIONS_GROUP_READ;

    /** Indicates that the group is {@code Group} i.e. RWRA--. */
    public static final int PERMISSIONS_GROUP_READ_LINK = GroupData.PERMISSIONS_GROUP_READ_LINK;

    /** Indicates that the group is {@code Group} i.e. RWRW--. */
    public static final int PERMISSIONS_GROUP_READ_WRITE = GroupData.PERMISSIONS_GROUP_READ_WRITE;

    /** Indicates that the group is {@code Public} i.e. RWRWR-. */
    public static final int PERMISSIONS_PUBLIC_READ = GroupData.PERMISSIONS_PUBLIC_READ;

    /** Indicates that the group is {@code Public} i.e. RWRWRW. */
    public static final int PERMISSIONS_PUBLIC_READ_WRITE = GroupData.PERMISSIONS_PUBLIC_READ_WRITE;


    /**
     * Constructor of the class GroupWrapper.
     *
     * @param group The object contained in the GroupWrapper.
     */
    public GroupWrapper(GroupData group) {
        super(group);
    }


    /**
     * Returns the name of the group.
     *
     * @return See above.
     */
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the group.
     *
     * @param name The name of the group. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Returns the GroupData contained.
     *
     * @return See above.
     */
    public GroupData asGroupData() {
        return data;
    }


    /**
     * Returns the description of the group.
     *
     * @return See above.
     */
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the group.
     *
     * @param description The description of the group. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Returns the leaders of this group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getLeaders() {
        return wrap(data.getLeaders(), ExperimenterWrapper::new, ExperimenterWrapper::getLastName);
    }


    /**
     * Returns the experimenters contained in this group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getExperimenters() {
        return wrap(data.getExperimenters(), ExperimenterWrapper::new, ExperimenterWrapper::getLastName);
    }


    /**
     * Returns the list of experimenters that are not owners of the group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getMembersOnly() {
        return wrap(data.getMembersOnly(), ExperimenterWrapper::new, ExperimenterWrapper::getLastName);
    }


    /**
     * Returns the permissions level.
     *
     * @return See above.
     */
    public int getPermissionsLevel() {
        return data.getPermissions().getPermissionsLevel();
    }

}
