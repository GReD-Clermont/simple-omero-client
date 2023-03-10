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


import omero.gateway.model.TextualAnnotationData;


/**
 * Interface to handle Textual Annotations on OMERO.
 */
public interface TextualAnnotation extends Annotation {

    /**
     * Returns a {@link TextualAnnotationData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    TextualAnnotationData asDataObject();


    /**
     * Returns the text of this annotation.
     *
     * @return See above.
     */
    String getText();


    /**
     * Sets the text.
     *
     * @param text The value to set.
     */
    void setText(String text);

}