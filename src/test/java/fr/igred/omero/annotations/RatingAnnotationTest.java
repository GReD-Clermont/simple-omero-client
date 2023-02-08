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
import omero.gateway.model.RatingAnnotationData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class RatingAnnotationTest extends UserTest {


    @Test
    void testAddRating1() throws Exception {
        Image image = client.getImage(IMAGE1.id);
        int score = 5;

        RatingAnnotation rating = new RatingAnnotationWrapper(score);
        image.link(client, rating);

        AnnotationList annotations = image.getAnnotations(client);

        List<RatingAnnotation> ratings = annotations.getElementsOf(RatingAnnotation.class);
        client.delete(ratings);

        assertEquals(1, ratings.size());
        assertEquals(score, ratings.get(0).getRating());
    }


    @Test
    void testAddTextualAnnotation2() throws Exception {
        Image image = client.getImage(IMAGE1.id);
        int score = 3;

        RatingAnnotation rating = new RatingAnnotationWrapper(new RatingAnnotationData());
        rating.setRating(score);
        image.link(client, rating);

        AnnotationList annotations = image.getAnnotations(client);

        List<RatingAnnotation> ratings = annotations.getElementsOf(RatingAnnotation.class);
        client.delete(ratings);

        assertEquals(1, ratings.size());
        assertEquals(score, ratings.get(0).getRating());
    }

}