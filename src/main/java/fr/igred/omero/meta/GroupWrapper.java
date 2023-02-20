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


import fr.igred.omero.ObjectWrapper;
import omero.gateway.model.GroupData;

import java.util.List;


/**
 * Class containing a GroupData object.
 * <p> Wraps function calls to the GroupData contained.
 */
public class GroupWrapper extends ObjectWrapper<GroupData> implements Group {


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
    @Override
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
    @Override
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Returns the description of the group.
     *
     * @return See above.
     */
    @Override
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
    @Override
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Returns the leaders of this group.
     *
     * @return See above.
     */
    @Override
    public List<Experimenter> getLeaders() {
        return wrap(data.getLeaders(), ExperimenterWrapper::new, Experimenter::getLastName);
    }


    /**
     * Returns the experimenters contained in this group.
     *
     * @return See above.
     */
    @Override
    public List<Experimenter> getExperimenters() {
        return wrap(data.getExperimenters(), ExperimenterWrapper::new, Experimenter::getLastName);
    }


    /**
     * Returns the list of experimenters that are not owners of the group.
     *
     * @return See above.
     */
    @Override
    public List<Experimenter> getMembersOnly() {
        return wrap(data.getMembersOnly(), ExperimenterWrapper::new, Experimenter::getLastName);
    }


    /**
     * Returns the permissions level.
     *
     * @return See above.
     */
    @Override
    public int getPermissionsLevel() {
        return data.getPermissions().getPermissionsLevel();
    }

}
