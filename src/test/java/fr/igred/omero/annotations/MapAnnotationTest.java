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

package fr.igred.omero.annotations;


import fr.igred.omero.UserTest;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MapAnnotationTest extends UserTest {


    @Test
    void testGetAllMapAnnotations() throws Exception {
        List<MapAnnotationWrapper> maps = client.getMapAnnotations();
        assertEquals(2, maps.size());
    }


    @Test
    void testGetSingleMapAnnotation() throws Exception {
        MapAnnotationWrapper map = client.getMapAnnotation(4L);
        assertEquals("testKey1", map.getContent().get(0).getKey());
        assertEquals("testValue1", map.getContent().get(0).getValue());
    }


    @Test
    void testGetSingleMapAnnotationByKey() throws Exception {
        List<MapAnnotationWrapper> maps = client.getMapAnnotations("testKey1");
        assertEquals(2, maps.size());
        assertEquals("testValue1", maps.get(0).getContent().get(0).getValue());
    }


    @Test
    void testGetSingleMapAnnotationByKeyAndValue() throws Exception {
        String key = "testKey1";
        String val = "testValue1";

        List<MapAnnotationWrapper> maps = client.getMapAnnotations(key, val);
        assertEquals(1, maps.size());
    }


    @Test
    void testGetContentAsMap() throws Exception {
        MapAnnotationWrapper      map     = client.getMapAnnotation(4L);
        Map<String, List<String>> content = map.getContentAsMap();
        assertEquals("testValue1", content.get("testKey1").get(0));
        assertEquals("20", content.get("testKey2").get(0));
    }


    @Test
    void testGetContentAsString() throws Exception {
        MapAnnotationWrapper map = client.getMapAnnotation(4L);

        String expected = "testKey1=testValue1;testKey2=20";
        assertEquals(expected, map.getContentAsString());
    }


    @Test
    void testSetContent() throws Exception {
        MapAnnotationWrapper        map     = client.getMapAnnotation(4L);
        List<Entry<String, String>> content = map.getContent();

        Collection<Entry<String, String>> pairs = new ArrayList<>(2);
        pairs.add(new SimpleEntry<>("testKey2", "testValue2"));
        pairs.add(new SimpleEntry<>("testKey3", "testValue3"));

        map.setContent(pairs);
        map.saveAndUpdate(client);

        int    size     = map.getContent().size();
        String expected = "testKey2=testValue2;testKey3=testValue3";
        String actual   = map.getContentAsString();

        map.setContent(content);
        map.saveAndUpdate(client);

        assertEquals(2, size);
        assertEquals(expected, actual);
        assertEquals("testKey1", map.getContent().get(0).getKey());
        assertEquals("testValue1", map.getContent().get(0).getValue());
    }

}
