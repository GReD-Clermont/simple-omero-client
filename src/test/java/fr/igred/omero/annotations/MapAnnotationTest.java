package fr.igred.omero.annotations;


import fr.igred.omero.UserTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MapAnnotationTest extends UserTest {


    @Test
    void testGetAllMapAnnotations() throws Exception {
        List<MapAnnotation> maps = client.getMapAnnotations();
        assertEquals(2, maps.size());
    }


    @Test
    void testGetSingleMapAnnotation() throws Exception {
        MapAnnotation map = client.getMapAnnotation(4L);
        assertEquals("testKey1", map.getContent().get(0).getKey());
        assertEquals("testValue1", map.getContent().get(0).getValue());
    }


    @Test
    void testGetSingleMapAnnotationByKey() throws Exception {
        List<MapAnnotation> maps = client.getMapAnnotations("testKey1");
        assertEquals(2, maps.size());
        assertEquals("testValue1", maps.get(0).getContent().get(0).getValue());
    }


    @Test
    void testGetSingleMapAnnotationByKeyAndValue() throws Exception {
        List<MapAnnotation> maps = client.getMapAnnotations("testKey1", "testValue1");
        assertEquals(1, maps.size());
    }

}
