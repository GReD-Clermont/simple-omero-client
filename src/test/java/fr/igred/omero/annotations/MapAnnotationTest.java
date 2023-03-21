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

package fr.igred.omero.annotations;


import fr.igred.omero.UserTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


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


    @Test
    void testNoSuchMapAnnotation() {
        assertThrows(NoSuchElementException.class, () -> client.getMapAnnotation(-1L));
    }

}
