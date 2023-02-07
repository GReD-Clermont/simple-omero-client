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


import omero.gateway.model.RatingAnnotationData;


/**
 * Interface to handle Rating Annotations on OMERO.
 */
public interface RatingAnnotation extends Annotation<RatingAnnotationData> {

    /** Indicates the object is not rated. */
    int LEVEL_ZERO = RatingAnnotationData.LEVEL_ZERO;

    /** Indicates the object is rated with one star. */
    int LEVEL_ONE = RatingAnnotationData.LEVEL_ONE;

    /** Indicates the object is rated with two stars. */
    int LEVEL_TWO = RatingAnnotationData.LEVEL_TWO;

    /** Indicates the object is rated with three stars. */
    int LEVEL_THREE = RatingAnnotationData.LEVEL_THREE;

    /** Indicates the object is rated with four stars. */
    int LEVEL_FOUR = RatingAnnotationData.LEVEL_FOUR;

    /** Indicates the object is rated with five stars. */
    int LEVEL_FIVE = RatingAnnotationData.LEVEL_FIVE;


    /**
     * Returns the rating value.
     *
     * @return See above.
     */
    int getRating();


    /**
     * Sets the rating value.
     *
     * @param value The value to set. Must be One of the constants defined by this class.
     */
    void setRating(int value);

}
