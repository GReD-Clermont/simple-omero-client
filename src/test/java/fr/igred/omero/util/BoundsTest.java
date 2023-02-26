package fr.igred.omero.util;


import fr.igred.omero.BasicTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class BoundsTest extends BasicTest {

    @Test
    void testCoordinatesToString() {
        int x = 1;
        int y = 2;
        int c = 3;
        int z = 4;
        int t = 5;

        Coordinates pos = new Coordinates(x, y, c, z, t);

        String expected = "Coordinates{" +
                          "x=" + x +
                          ", y=" + y +
                          ", c=" + c +
                          ", z=" + z +
                          ", t=" + t +
                          "}";
        assertEquals(expected, pos.toString());
    }


    @Test
    void testBoundsToString() {
        Coordinates start = new Coordinates(1, 2, 3, 4, 5);
        Coordinates end   = new Coordinates(10, 9, 8, 7, 6);
        Coordinates size  = new Coordinates(10, 8, 6, 4, 2);

        Bounds bounds = new Bounds(start, end);
        String expected = "Bounds{" +
                          "start=" + start +
                          ", size=" + size +
                          "}";
        assertEquals(expected, bounds.toString());
    }

}
