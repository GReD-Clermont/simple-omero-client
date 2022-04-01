/*
 *  Copyright (C) 2020-2021 GReD
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
import fr.igred.omero.meta.ExperimenterWrapper.SortByLastName;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


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
     * @return GroupData contained.
     */
    public GroupData asGroupData() {
        return data;
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
        Set<ExperimenterData> leaders = data.getLeaders();

        List<ExperimenterWrapper> wrappers = new ArrayList<>(leaders.size());
        for (ExperimenterData leader : leaders) {
            wrappers.add(new ExperimenterWrapper(leader));
        }
        wrappers.sort(new SortByLastName<>());
        return wrappers;
    }


    /**
     * Returns the experimenters contained in this group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getExperimenters() {
        Set<ExperimenterData> experimenters = data.getExperimenters();

        List<ExperimenterWrapper> wrappers = new ArrayList<>(experimenters.size());
        for (ExperimenterData experimenter : experimenters) {
            wrappers.add(new ExperimenterWrapper(experimenter));
        }
        wrappers.sort(new SortByLastName<>());
        return wrappers;
    }


    /**
     * Returns the list of experimenters that are not owners of the group.
     *
     * @return See above.
     */
    public List<ExperimenterWrapper> getMembersOnly() {
        Set<ExperimenterData> members = data.getMembersOnly();

        List<ExperimenterWrapper> wrappers = new ArrayList<>(members.size());
        for (ExperimenterData member : members) {
            wrappers.add(new ExperimenterWrapper(member));
        }
        wrappers.sort(new SortByLastName<>());
        return wrappers;
    }


    /**
     * Returns the permissions level.
     *
     * @return See above.
     */
    public int getPermissionsLevel() {
        return data.getPermissions().getPermissionsLevel();
    }


    /**
     * Class used to sort GroupWrappers.
     */
    public static class SortByName<U extends GroupWrapper> implements Comparator<U> {

        /**
         * Compare 2 GroupWrappers. Compare the names of the GroupWrappers.
         *
         * @param object1 First object to compare.
         * @param object2 Second object to compare.
         *
         * @return <ul><li>-1 if the name of object1 is lower than the id object2.</li>
         * <li>0  if the names are the same.</li>
         * <li>1 if the name of object1 is greater than the id of object2.</li></ul>
         */
        public int compare(U object1, U object2) {
            return object1.getName().compareTo(object2.getName());
        }

    }

}
