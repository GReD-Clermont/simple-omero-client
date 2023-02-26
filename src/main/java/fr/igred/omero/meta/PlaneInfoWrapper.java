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
import ome.formats.model.UnitsFactory;
import ome.units.UNITS;
import ome.units.unit.Unit;
import omero.gateway.model.PlaneInfoData;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Time;
import omero.model.TimeI;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ome.formats.model.UnitsFactory.convertTime;


public class PlaneInfoWrapper extends GenericObjectWrapper<PlaneInfoData> {

    /**
     * Constructor of the class PlaneInfoWrapper.
     *
     * @param object The PlaneInfoData to wrap in the PlaneInfoWrapper.
     */
    public PlaneInfoWrapper(PlaneInfoData object) {
        super(object);
    }


    /**
     * Computes the mean time interval from the deltaT in a PlaneInfoWrapper collection.
     *
     * @param planesInfo A collection of PlaneInfoWrappers.
     * @param sizeT      The number of time points for these planes.
     *
     * @return See above.
     */
    public static Time computeMeanTimeInterval(Collection<? extends PlaneInfoWrapper> planesInfo, int sizeT) {
        // planesInfo should be larger than sizeT, unless it is empty
        ome.units.quantity.Time[] deltas = new ome.units.quantity.Time[Math.min(sizeT, planesInfo.size())];

        for (PlaneInfoWrapper plane : planesInfo) {
            int t = plane.getTheT();
            int z = plane.getTheZ();
            int c = plane.getTheC();
            if (c == 0 && z == 0 && t < deltas.length) {
                deltas[t] = convertTime(plane.getDeltaT());
            }
        }
        Unit<ome.units.quantity.Time> unit = UNITS.SECOND;

        ome.units.quantity.Time first = Arrays.stream(deltas).findFirst().orElse(null);
        if (first != null) {
            unit = first.unit();
        }

        double mean  = 0;
        int    count = 0;
        for (int i = 1; i < deltas.length; i++) {
            double delta1 = deltas[i - 1].value(unit).doubleValue();
            double delta2 = deltas[i].value(unit).doubleValue();
            if (!Double.isNaN(delta1) && !Double.isNaN(delta2)) {
                mean += delta2 - delta1;
                count++;
            }
        }
        mean /= count == 0 ? Double.NaN : count;
        return new TimeI(mean, unit);
    }


    /**
     * Computes the mean exposure time for a given channel in a PlaneInfoWrapper collection.
     *
     * @param planesInfo A collection of PlaneInfoWrappers.
     * @param channel    The channel index.
     *
     * @return See above.
     */
    public static Time computeMeanExposureTime(Iterable<? extends PlaneInfoWrapper> planesInfo, int channel) {
        ome.units.quantity.Time t = null;

        Iterator<? extends PlaneInfoWrapper> iterator = planesInfo.iterator();
        while (t == null && iterator.hasNext()) {
            t = convertTime(planesInfo.iterator().next().getExposureTime());
            iterator.next();
        }

        Unit<ome.units.quantity.Time> unit = t == null ? UNITS.SECOND : t.unit();

        double mean  = 0;
        int    count = 0;
        for (PlaneInfoWrapper plane : planesInfo) {
            if (channel == plane.getTheC()) {
                ome.units.quantity.Time time = convertTime(plane.getExposureTime());
                if (time != null) {
                    mean += time.value(unit).doubleValue();
                    count++;
                }
            }
        }
        mean /= count == 0 ? Double.NaN : count;
        return new TimeI(mean, unit);
    }


    /**
     * Retrieves the min value for the specified getter in a PlaneInfoWrapper collection.
     *
     * @param planesInfo A collection of PlaneInfoWrappers.
     * @param getter     The getter function to use.
     * @param unit       The unit to set the results in.
     *
     * @return See above.
     */
    public static Length getMinPosition(Collection<? extends PlaneInfoWrapper> planesInfo,
                                        Function<? super PlaneInfoWrapper, ? extends Length> getter,
                                        Unit<ome.units.quantity.Length> unit) {
        List<Double> positions = planesInfo.stream()
                                           .map(getter)
                                           .map(UnitsFactory::convertLength)
                                           .map(p -> p.value(unit).doubleValue())
                                           .collect(Collectors.toList());
        Double pos = positions.isEmpty() ? 0.0d : Collections.min(positions);
        return new LengthI(pos, unit);
    }


    /**
     * @return See above.
     *
     * @deprecated Returns the PlaneInfoData contained. Use {@link #asDataObject()} instead.
     */
    @Deprecated
    public PlaneInfoData asPlaneInfoData() {
        return data;
    }


    /**
     * Retrieves the plane deltaT.
     *
     * @return See above.
     */
    public Time getDeltaT() {
        return data.getDeltaT();
    }


    /**
     * Retrieves the exposure time.
     *
     * @return See above.
     */
    public Time getExposureTime() {
        return data.getExposureTime();
    }


    /**
     * Retrieves the X stage position.
     *
     * @return See above.
     */
    public Length getPositionX() {
        return data.getPositionX();
    }


    /**
     * Retrieves the Y stage position.
     *
     * @return See above.
     */
    public Length getPositionY() {
        return data.getPositionY();
    }


    /**
     * Retrieves the Z stage position.
     *
     * @return See above.
     */
    public Length getPositionZ() {
        return data.getPositionZ();
    }


    /**
     * Retrieves the plane channel index.
     *
     * @return See above.
     */
    public int getTheC() {
        return data.getTheC();
    }


    /**
     * Retrieves the plane time index.
     *
     * @return See above.
     */
    public int getTheT() {
        return data.getTheT();
    }


    /**
     * Retrieves the plane slice index.
     *
     * @return See above.
     */
    public int getTheZ() {
        return data.getTheZ();
    }

}
