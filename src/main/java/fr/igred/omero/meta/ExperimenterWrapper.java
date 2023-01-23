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


import fr.igred.omero.RemoteObjectWrapper;
import omero.gateway.model.ExperimenterData;

import java.util.List;


/**
 * Class containing an ExperimenterData object.
 * <p> Wraps function calls to the ExperimenterData contained.
 */
public class ExperimenterWrapper extends RemoteObjectWrapper<ExperimenterData> implements Experimenter {

    /**
     * Constructor of the class Experimenter.
     *
     * @param dataObject The experimenter contained in the Experimenter.
     */
    public ExperimenterWrapper(ExperimenterData dataObject) {
        super(dataObject);
    }


    /**
     * Returns the groups the experimenter is a member of.
     *
     * @return See above.
     */
    @Override
    public List<Group> getGroups() {
        return wrap(asDataObject().getGroups(), GroupWrapper::new, Group::getName);
    }


}
