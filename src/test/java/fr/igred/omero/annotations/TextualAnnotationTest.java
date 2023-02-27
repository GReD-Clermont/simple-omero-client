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
import fr.igred.omero.core.Image;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fr.igred.omero.RemoteObject.getElementsOf;
import static org.junit.jupiter.api.Assertions.assertEquals;


class TextualAnnotationTest extends UserTest {


    @Test
    void testAddTextualAnnotation1() throws Exception {
        Image image = client.getImage(IMAGE1.id);

        String test = "Test !";

        TextualAnnotation text = new TextualAnnotationWrapper(test);
        image.link(client, text);

        List<Annotation> annotations = image.getAnnotations(client);

        List<TextualAnnotation> texts = getElementsOf(annotations, TextualAnnotationWrapper.class);
        client.delete(texts);

        assertEquals(1, texts.size());
        assertEquals(test, texts.get(0).getText());
    }


    @Test
    void testAddTextualAnnotation2() throws Exception {
        Image image = client.getImage(IMAGE1.id);

        String test = "Test !";

        TextualAnnotation text = new TextualAnnotationWrapper("New");
        text.setText(test);
        image.link(client, text);
        List<Annotation> annotations = image.getAnnotations(client);

        List<TextualAnnotation> texts = getElementsOf(annotations, TextualAnnotationWrapper.class);
        client.delete(texts);

        assertEquals(1, texts.size());
        assertEquals(test, texts.get(0).getText());
    }

}
