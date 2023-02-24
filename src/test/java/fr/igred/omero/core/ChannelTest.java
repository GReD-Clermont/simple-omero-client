package fr.igred.omero.core;


import fr.igred.omero.BasicTest;
import omero.gateway.model.ChannelData;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ChannelTest extends BasicTest {


    @Test
    void testChannelNoRGBA() {
        ChannelWrapper channel = new ChannelWrapper(new ChannelData(0));
        assertEquals(Color.WHITE, channel.getColor());
    }

}
