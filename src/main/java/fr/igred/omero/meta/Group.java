/*
 *  Copyright (C) 2020-2025 GReD
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
import omero.gateway.model.GroupData;

import java.util.List;


/**
 * Interface to handle Groups on OMERO.
 */
public interface Group extends RemoteObject {

    /** Indicates that the group is {@code Private} i.e. RW----. */
    int PERMISSIONS_PRIVATE = GroupData.PERMISSIONS_PRIVATE;

    /** Indicates that the group is {@code Group} i.e. RWR---. */
    int PERMISSIONS_GROUP_READ = GroupData.PERMISSIONS_GROUP_READ;

    /** Indicates that the group is {@code Group} i.e. RWRA--. */
    int PERMISSIONS_GROUP_READ_LINK = GroupData.PERMISSIONS_GROUP_READ_LINK;

    /** Indicates that the group is {@code Group} i.e. RWRW--. */
    int PERMISSIONS_GROUP_READ_WRITE = GroupData.PERMISSIONS_GROUP_READ_WRITE;

    /** Indicates that the group is {@code Public} i.e. RWRWR-. */
    int PERMISSIONS_PUBLIC_READ = GroupData.PERMISSIONS_PUBLIC_READ;

    /** Indicates that the group is {@code Public} i.e. RWRWRW. */
    int PERMISSIONS_PUBLIC_READ_WRITE = GroupData.PERMISSIONS_PUBLIC_READ_WRITE;


    /**
     * Returns a {@link GroupData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    GroupData asDataObject();


    /**
     * Returns the name of the group.
     *
     * @return See above.
     */
    String getName();


    /**
     * Sets the name of the group.
     *
     * @param name The name of the group. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setName(String name);


    /**
     * Returns the description of the group.
     *
     * @return See above.
     */
    String getDescription();


    /**
     * Sets the description of the group.
     *
     * @param description The description of the group. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    void setDescription(String description);


    /**
     * Returns the leaders of this group.
     *
     * @return See above.
     */
    List<Experimenter> getLeaders();


    /**
     * Returns the experimenters contained in this group.
     *
     * @return See above.
     */
    List<Experimenter> getExperimenters();


    /**
     * Returns the list of experimenters that are not owners of the group.
     *
     * @return See above.
     */
    List<Experimenter> getMembersOnly();


    /**
     * Returns the permissions level.
     *
     * @return See above.
     */
    int getPermissionsLevel();

}
