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


import omero.gateway.model.RatingAnnotationData;


/**
 * Class containing a RatingAnnotationData object.
 * <p> Wraps function calls to the RatingAnnotationData contained.
 */
public class RatingAnnotationWrapper extends AnnotationWrapper<RatingAnnotationData> implements RatingAnnotation {

    /**
     * Constructor of the RatingAnnotationWrapper class.
     *
     * @param object RatingAnnotationData to wrap.
     */
    public RatingAnnotationWrapper(RatingAnnotationData object) {
        super(object);
    }


    /**
     * Creates a new Textual Annotation with the provided text.
     *
     * @param value The rating value. One of the constants defined by this class.
     */
    public RatingAnnotationWrapper(int value) {
        super(new RatingAnnotationData(value));
    }


    /**
     * Returns the rating value.
     *
     * @return See above.
     */
    @Override
    public int getRating() {
        return data.getRating();
    }


    /**
     * Sets the rating value.
     *
     * @param value The value to set. Must be one of the constants defined by this class.
     */
    @Override
    public void setRating(int value) {
        data.setRating(value);
    }

}
