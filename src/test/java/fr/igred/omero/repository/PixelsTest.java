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

package fr.igred.omero.repository;


import fr.igred.omero.UserTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


class PixelsTest extends UserTest {


    @Test
    void testGetImageSize() throws Exception {
        final int sizeXY = 512;
        final int sizeC  = 5;
        final int sizeZ  = 3;
        final int sizeT  = 7;

        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();

        assertEquals(sizeXY, pixels.getSizeX());
        assertEquals(sizeXY, pixels.getSizeY());
        assertEquals(sizeC, pixels.getSizeC());
        assertEquals(sizeZ, pixels.getSizeZ());
        assertEquals(sizeT, pixels.getSizeT());
    }


    @Test
    void testGetRawData() throws Exception {
        ImageWrapper     image  = client.getImage(IMAGE1.id);
        PixelsWrapper    pixels = image.getPixels();
        double[][][][][] value  = pixels.getAllPixels(client);

        assertEquals(pixels.getSizeX(), value[0][0][0][0].length);
        assertEquals(pixels.getSizeY(), value[0][0][0].length);
        assertEquals(pixels.getSizeC(), value[0][0].length);
        assertEquals(pixels.getSizeZ(), value[0].length);
        assertEquals(pixels.getSizeT(), value.length);
    }


    @Test
    void testGetRawData2() throws Exception {
        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();
        byte[][][][]  value  = pixels.getRawPixels(client, 1);

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
        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();

        int[] xBound = {0, 2};
        int[] yBound = {0, 2};
        int[] cBound = {0, 2};
        int[] zBound = {0, 2};
        int[] tBound = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBound, yBound, cBound, zBound, tBound);

        assertEquals(3, value[0][0][0][0].length);
        assertEquals(3, value[0][0][0].length);
        assertEquals(3, value[0][0].length);
        assertEquals(3, value[0].length);
        assertEquals(3, value.length);
    }


    @Test
    void testGetRawDataBoundError() throws Exception {
        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();

        final int[] xBound = {511, 513};
        final int[] yBound = {0, 2};
        final int[] cBound = {0, 2};
        final int[] zBound = {0, 2};
        final int[] tBound = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBound, yBound, cBound, zBound, tBound);
        assertNotEquals(xBound[1] - xBound[0] + 1, value[0][0][0][0].length);
    }


    @Test
    void testGetRawDataBoundErrorNegative() throws Exception {
        ImageWrapper  image  = client.getImage(IMAGE1.id);
        PixelsWrapper pixels = image.getPixels();

        int[] xBound = {-1, 1};
        int[] yBound = {0, 2};
        int[] cBound = {0, 2};
        int[] zBound = {0, 2};
        int[] tBound = {0, 2};

        double[][][][][] value = pixels.getAllPixels(client, xBound, yBound, cBound, zBound, tBound);
        assertNotEquals(3, value[0][0][0][0].length);
    }


}
