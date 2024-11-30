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
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.core.Image;
import omero.gateway.model.RatingAnnotationData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class RatingAnnotationTest extends UserTest {


    @Test
    void testAddRating1() throws Exception {
        Image image = client.getImage(IMAGE1.id);
        int   score = 5;

        RatingAnnotation rating = new RatingAnnotationWrapper(score);
        image.link(client, rating);

        List<RatingAnnotation> ratings = image.getAnnotations(client)
                                              .getElementsOf(RatingAnnotation.class);
        client.delete(ratings);

        assertEquals(1, ratings.size());
        assertEquals(score, ratings.get(0).getRating());
    }


    @Test
    void testAddRating2() throws Exception {
        Image image = client.getImage(IMAGE1.id);
        int   score = 3;

        RatingAnnotation rating = new RatingAnnotationWrapper(new RatingAnnotationData());
        rating.setRating(score);
        image.link(client, rating);

        List<RatingAnnotation> ratings = image.getAnnotations(client)
                                              .getElementsOf(RatingAnnotation.class);
        client.delete(ratings);

        assertEquals(1, ratings.size());
        assertEquals(score, ratings.get(0).getRating());
    }


    @Test
    void testRate1() throws Exception {
        Image image  = client.getImage(IMAGE1.id);
        int   score1 = 4;
        int          score2 = 3;

        image.rate(client, score1);
        int rating1 = image.getMyRating(client);
        image.rate(client, score2);
        int rating2 = image.getMyRating(client);

        List<RatingAnnotation> ratings = image.getAnnotations(client)
                                              .getElementsOf(RatingAnnotation.class);
        client.delete(ratings);

        assertEquals(1, ratings.size());
        assertEquals(score1, rating1);
        assertEquals(score2, rating2);
    }


    @Test
    void testRate2() throws Exception {
        Image image  = client.getImage(IMAGE1.id);
        int   score0 = 1;
        int          score1 = 2;
        int          score2 = 3;

        RatingAnnotation rating1 = new RatingAnnotationWrapper(new RatingAnnotationData());
        rating1.setRating(score0);
        RatingAnnotation rating2 = new RatingAnnotationWrapper(new RatingAnnotationData());
        rating1.setRating(score1);
        image.link(client, rating1);
        image.link(client, rating2);
        int myRating1 = image.getMyRating(client);
        image.rate(client, score2);
        int myRating2 = image.getMyRating(client);

        List<RatingAnnotation> ratings = image.getAnnotations(client)
                                              .getElementsOf(RatingAnnotation.class);
        client.delete(ratings);

        assertEquals(1, ratings.size());
        assertEquals((score0 + score1) / 2, myRating1);
        assertEquals(score2, myRating2);
    }


    @Test
    void testRate3() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);
        int     score1  = 4;
        int            score2  = 3;

        dataset.rate(client, score1);
        int rating1 = dataset.getMyRating(client);
        dataset.rate(client, score2);
        int rating2 = dataset.getMyRating(client);

        List<RatingAnnotation> ratings = dataset.getAnnotations(client)
                                                .getElementsOf(RatingAnnotation.class);
        client.delete(ratings);

        assertEquals(1, ratings.size());
        assertEquals(score1, rating1);
        assertEquals(score2, rating2);
    }


    @Test
    void testGetRatings() throws Exception {
        Dataset                dataset = client.getDataset(DATASET1.id);
        List<RatingAnnotation> ratings = dataset.getRatings(client);
        assertTrue(ratings.isEmpty());
    }

}
