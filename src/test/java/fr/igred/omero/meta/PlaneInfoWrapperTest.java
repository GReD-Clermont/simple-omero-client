/*
 *  Copyright (C) 2020-2024 GReD
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


import fr.igred.omero.UserTest;
import fr.igred.omero.repository.PixelsWrapper;
import ome.units.UNITS;
import omero.model.Length;
import omero.model.Time;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fr.igred.omero.meta.PlaneInfoWrapper.computeMeanExposureTime;
import static fr.igred.omero.meta.PlaneInfoWrapper.computeMeanTimeInterval;
import static fr.igred.omero.meta.PlaneInfoWrapper.getMinPosition;
import static org.junit.jupiter.api.Assertions.assertEquals;


class PlaneInfoWrapperTest extends UserTest {

    @Test
    void testComputeMeanTimeInterval() throws Exception {
        PixelsWrapper pixels = client.getImage(IMAGE1.id).getPixels();
        pixels.loadPlanesInfo(client);
        List<PlaneInfoWrapper> planes = pixels.getPlanesInfo();

        Time time = computeMeanTimeInterval(planes, pixels.getSizeT());
        assertEquals(150, time.getValue());
        assertEquals("ms", time.getSymbol());
        assertEquals(time.getValue(), pixels.getMeanTimeInterval().getValue());
    }


    @Test
    void testComputeMeanExposureTime() throws Exception {
        PixelsWrapper pixels = client.getImage(IMAGE1.id).getPixels();
        pixels.loadPlanesInfo(client);
        List<PlaneInfoWrapper> planes = pixels.getPlanesInfo();

        Time time = computeMeanExposureTime(planes, 0);
        assertEquals(25, time.getValue());
        assertEquals("ms", time.getSymbol());
        assertEquals(time.getValue(), pixels.getMeanExposureTime(0).getValue());
    }


    @Test
    void testGetMinPosition() throws Exception {
        PixelsWrapper pixels = client.getImage(IMAGE1.id).getPixels();
        pixels.loadPlanesInfo(client);
        List<PlaneInfoWrapper> planes = pixels.getPlanesInfo();

        Length positionX = getMinPosition(planes,
                                          PlaneInfoWrapper::getPositionX,
                                          UNITS.NANOMETER);
        assertEquals(100000, positionX.getValue());
        assertEquals("nm", positionX.getSymbol());
    }

}