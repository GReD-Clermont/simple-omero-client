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
import fr.igred.omero.repository.ImageWrapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TextualAnnotationTest extends UserTest {


    @Test
    void testAddTextualAnnotation1() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        String test = "Test !";

        TextualAnnotationWrapper text = new TextualAnnotationWrapper(test);
        image.link(client, text);

        AnnotationList annotations = image.getAnnotations(client);

        List<TextualAnnotationWrapper> texts = annotations.getElementsOf(TextualAnnotationWrapper.class);
        client.delete(texts);

        assertEquals(1, texts.size());
        assertEquals(test, texts.get(0).getText());
    }


    @Test
    void testAddTextualAnnotation2() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        String test = "Test !";

        TextualAnnotationWrapper text = new TextualAnnotationWrapper("New");
        text.setText(test);
        image.link(client, text);
        AnnotationList annotations = image.getAnnotations(client);

        List<TextualAnnotationWrapper> texts = annotations.getElementsOf(TextualAnnotationWrapper.class);
        client.delete(texts);

        assertEquals(1, texts.size());
        assertEquals(test, texts.get(0).getText());
    }

}
