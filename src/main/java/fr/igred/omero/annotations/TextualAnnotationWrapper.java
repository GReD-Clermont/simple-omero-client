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


import omero.gateway.model.TextualAnnotationData;


/**
 * Class containing a TextualAnnotationData object.
 * <p> Wraps function calls to the TextualAnnotationData contained.
 */
public class TextualAnnotationWrapper extends AnnotationWrapper<TextualAnnotationData> implements TextualAnnotation {

    /**
     * Constructor of the TextualAnnotationWrapper class.
     *
     * @param object TextualAnnotationData to be wrap.
     */
    public TextualAnnotationWrapper(TextualAnnotationData object) {
        super(object);
    }


    /**
     * Creates a new Textual Annotation with the provided text.
     *
     * @param text Textual Annotation to be contained.
     */
    public TextualAnnotationWrapper(String text) {
        super(new TextualAnnotationData(text));
    }


    /**
     * Returns the text of this annotation.
     *
     * @return See above.
     */
    @Override
    public String getText() {
        return data.getText();
    }


    /**
     * Sets the text.
     *
     * @param text The value to set.
     */
    @Override
    public void setText(String text) {
        data.setText(text);
    }

}
