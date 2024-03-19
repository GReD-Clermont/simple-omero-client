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

import java.util.List;

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
        assertEquals("testKey1", map.getContent().get(0).name);
        assertEquals("testValue1", map.getContent().get(0).value);
    }


    @Test
    void testGetSingleMapAnnotationByKey() throws Exception {
        List<MapAnnotationWrapper> maps = client.getMapAnnotations("testKey1");
        assertEquals(2, maps.size());
        assertEquals("testValue1", maps.get(0).getContent().get(0).value);
    }


    @Test
    void testGetSingleMapAnnotationByKeyAndValue() throws Exception {
        String key = "testKey1";
        String val = "testValue1";

        List<MapAnnotationWrapper> maps = client.getMapAnnotations(key, val);
        assertEquals(1, maps.size());
    }

}
