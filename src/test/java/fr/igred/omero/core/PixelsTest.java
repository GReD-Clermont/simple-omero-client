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

package fr.igred.omero.core;


import fr.igred.omero.UserTest;
import fr.igred.omero.util.Coordinates;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class PixelsTest extends UserTest {


    @Test
    void testGetResolutions() throws Exception {
        final int sizeX = 512;
        final int sizeY = 512;

        Image                 image       = client.getImage(IMAGE1.id);
        Pixels                pixels      = image.getPixels();
        List<ResolutionLevel> resolutions = pixels.getResolutionLevels(client);

        assertEquals(3, resolutions.size());
        assertEquals(2, resolutions.get(2).getLevel());
        assertEquals(sizeX / 4, resolutions.get(2).getSizeX());
        assertEquals(sizeY / 4, resolutions.get(2).getSizeY());
    }


    @Test
    void testGetImageSize() throws Exception {
        final int sizeXY = 512;
        final int sizeC  = 5;
        final int sizeZ  = 3;
        final int sizeT  = 7;

        Image  image  = client.getImage(IMAGE1.id);
        Pixels pixels = image.getPixels();

        assertEquals(sizeXY, pixels.getSizeX());
        assertEquals(sizeXY, pixels.getSizeY());
        assertEquals(sizeC, pixels.getSizeC());
        assertEquals(sizeZ, pixels.getSizeZ());
        assertEquals(sizeT, pixels.getSizeT());
    }


    @Test
    void testGetRawData() throws Exception {
        Image            image  = client.getImage(IMAGE1.id);
        Pixels           pixels = image.getPixels();
        double[][][][][] value  = pixels.getAllPixels(client);

        assertEquals(pixels.getSizeX(), value[0][0][0][0].length);
        assertEquals(pixels.getSizeY(), value[0][0][0].length);
        assertEquals(pixels.getSizeC(), value[0][0].length);
        assertEquals(pixels.getSizeZ(), value[0].length);
        assertEquals(pixels.getSizeT(), value.length);
    }


    @Test
    void testGetRawData2() throws Exception {
        Image        image  = client.getImage(IMAGE1.id);
        Pixels       pixels = image.getPixels();
        byte[][][][] value  = pixels.getRawPixels(client, 1);

        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        int sizeZ = pixels.getSizeZ();
        int sizeC = pixels.getSizeC();
        int sizeT = pixels.getSizeT();

        assertEquals(sizeX * sizeY, value[0][0][0].length);
        assertEquals(sizeC, value[0][0].length);
        assertEquals(sizeZ, value[0].length);
        assertEquals(sizeT, value.length);
    }


    @Test
    void testGetRawDataBound() throws Exception {
        Image  image  = client.getImage(IMAGE1.id);
        Pixels pixels = image.getPixels();

        int[] xBounds = {0, 2};
        int[] yBounds = {0, 2};
        int[] cBounds = {0, 2};
        int[] zBounds = {0, 2};
        int[] tBounds = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBounds, yBounds, cBounds, zBounds, tBounds);

        assertEquals(3, value[0][0][0][0].length);
        assertEquals(3, value[0][0][0].length);
        assertEquals(3, value[0][0].length);
        assertEquals(3, value[0].length);
        assertEquals(3, value.length);
    }


    @Test
    void testGetRawDataBoundSubRes1() throws Exception {
        Image  image  = client.getImage(IMAGE1.id);
        Pixels pixels = image.getPixels();

        int[] xBounds = {-1, 2};
        int[] yBounds = {0, 512};
        int[] cBounds = {0, 2};
        int[] zBounds = {0, 2};
        int[] tBounds = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBounds, yBounds, cBounds, zBounds, tBounds, 1);

        assertEquals(3, value[0][0][0][0].length);
        assertEquals(256, value[0][0][0].length);
        assertEquals(3, value[0][0].length);
        assertEquals(3, value[0].length);
        assertEquals(3, value.length);
    }


    @Test
    void testGetRawDataBoundSubRes2() throws Exception {
        Image  image  = client.getImage(IMAGE1.id);
        Pixels pixels = image.getPixels();

        int[] xBounds = {-1, 2};
        int[] yBounds = {0, 512};
        int[] cBounds = {0, 2};
        int[] zBounds = {0, 2};
        int[] tBounds = {0, 2};

        byte[][][][] value = pixels.getRawPixels(client, xBounds, yBounds, cBounds, zBounds, tBounds, 1, 1);

        assertEquals(3 * 256, value[0][0][0].length);
        assertEquals(3, value[0][0].length);
        assertEquals(3, value[0].length);
        assertEquals(3, value.length);
    }


    @Test
    void testGetRawDataSubRes1() throws Exception {
        Image  image = client.getImage(IMAGE1.id);
        Pixels pix   = image.getPixels();

        PixelsWrapper pixels = new PixelsWrapper(pix.asDataObject());

        int level = 1;

        Coordinates size  = pixels.getSize(client, level);
        int         sizeX = size.getX();
        int         sizeY = size.getY();
        int         sizeZ = size.getZ();
        int         sizeC = size.getC();
        int         sizeT = size.getT();

        double[][][][][] value = pixels.getAllPixels(client, level);

        assertEquals(sizeX, value[0][0][0][0].length);
        assertEquals(sizeY, value[0][0][0].length);
        assertEquals(sizeC, value[0][0].length);
        assertEquals(sizeZ, value[0].length);
        assertEquals(sizeT, value.length);
    }


    @Test
    void testGetRawDataSubRes2() throws Exception {
        Image  image = client.getImage(IMAGE1.id);
        Pixels pix   = image.getPixels();

        PixelsWrapper pixels = new PixelsWrapper(pix.asDataObject());

        int level = 1;

        Coordinates size  = pixels.getSize(client, level);
        int         sizeX = size.getX();
        int         sizeY = size.getY();
        int         sizeZ = size.getZ();
        int         sizeC = size.getC();
        int         sizeT = size.getT();

        byte[][][][] value = pixels.getRawPixels(client, 1, level);

        assertEquals(sizeX * sizeY, value[0][0][0].length);
        assertEquals(sizeC, value[0][0].length);
        assertEquals(sizeZ, value[0].length);
        assertEquals(sizeT, value.length);
    }


    @Test
    void testGetRawDataBoundError() throws Exception {
        Image  image  = client.getImage(IMAGE1.id);
        Pixels pixels = image.getPixels();

        int[] xBounds = {511, 513};
        int[] yBounds = {525, 2};
        int[] cBounds = {-1, -1};
        int[] zBounds = {0, 2};
        int[] tBounds = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBounds, yBounds, cBounds, zBounds, tBounds);
        assertNotEquals(xBounds[1] - xBounds[0] + 1, value[0][0][0][0].length);
    }


    @Test
    void testGetRawDataBoundErrorNegative() throws Exception {
        Image  image  = client.getImage(IMAGE1.id);
        Pixels pixels = image.getPixels();

        int[] xBounds = {-1, 1};
        int[] yBounds = {0, 2};
        int[] cBounds = {0, 2};
        int[] zBounds = {0, 2};
        int[] tBounds = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBounds, yBounds, cBounds, zBounds, tBounds);
        assertNotEquals(3, value[0][0][0][0].length);
    }


}
