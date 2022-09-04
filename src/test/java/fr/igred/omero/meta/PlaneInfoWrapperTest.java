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

        Length positionX = getMinPosition(planes, PlaneInfoWrapper::getPositionX, UNITS.NANOMETER);
        assertEquals(100000, positionX.getValue());
        assertEquals("nm", positionX.getSymbol());
    }

}