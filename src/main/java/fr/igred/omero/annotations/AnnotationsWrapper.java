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


import omero.gateway.model.AnnotationData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TextualAnnotationData;

import java.util.function.Function;

import static java.lang.String.format;


/**
 * Utility class to convert DataObjects dynamically.
 */
public enum AnnotationsWrapper {
    /** Annotations */
    FILEANNOTATION(FileAnnotationData.class, FileAnnotationWrapper::new),
    MAPANNOTATION(MapAnnotationData.class, MapAnnotationWrapper::new),
    TAGANNOTATION(TagAnnotationData.class, TagAnnotationWrapper::new),
    RATINGANNOTATION(RatingAnnotationData.class, RatingAnnotationWrapper::new),
    TEXTUALANNOTATION(TextualAnnotationData.class, TextualAnnotationWrapper::new);

    /** Error message for unknown type. */
    private static final String UNKNOWN_TYPE = "Unknown type: %s";

    /** The converter to convert the DataObject to a RemoteObject. */
    private final AnnotationConverter<? extends AnnotationData, ? extends AnnotationWrapper<?>> converter;


    /** Constructor of the Wrappers enum. */
    <T extends AnnotationData, U extends AnnotationWrapper<T>>
    AnnotationsWrapper(Class<T> klazz, Function<T, U> mapper) {
        converter = new AnnotationConverter<>(klazz, mapper);
    }


    /**
     * Returns the Wrapper enum from a given class.
     *
     * @param klazz The class to get the Wrapper enum from.
     *
     * @return See above.
     */
    private static AnnotationsWrapper fromClass(Class<? extends AnnotationData> klazz) {
        for (AnnotationsWrapper c : values()) {
            if (c.converter.getKlazz().equals(klazz)) {
                return c;
            }
        }
        String msg = format(UNKNOWN_TYPE, klazz.getName());
        throw new IllegalArgumentException(msg);
    }


    /**
     * Returns the converter to convert the DataObject to a RemoteObject.
     *
     * @param object The DataObject to convert.
     * @param <T>    The DataObject type.
     * @param <U>    The RemoteObject type.
     *
     * @return See above.
     */
    private static <T extends AnnotationData, U extends AnnotationWrapper<T>>
    AnnotationConverter<T, U> getConverter(T object) {
        AnnotationsWrapper c = fromClass(object.getClass());
        //noinspection unchecked
        return (AnnotationConverter<T, U>) c.converter;
    }


    /**
     * Converts (wraps) a ShapeData object to a Shape object.
     *
     * @param object The object to convert.
     * @param <T>    The ShapeData type.
     * @param <U>    The Shape type.
     *
     * @return See above.
     */
    public static <T extends AnnotationData, U extends AnnotationWrapper<T>> U wrap(T object) {
        AnnotationConverter<T, U> converter = getConverter(object);
        return converter.convert(object);
    }


    @Override
    public String toString() {
        return format("AnnotationsWrapper{%s}", name());
    }


    /**
     * AnnotationConverter class to convert ShapeData to Shapes.
     *
     * @param <T> The DataObject type.
     * @param <U> The RemoteObject type.
     */
    private static class AnnotationConverter<T extends AnnotationData, U extends AnnotationWrapper<T>> {

        /** The class of the DataObject. */
        private final Class<T> klazz;

        /** The function to convert the DataObject to a RemoteObject. */
        private final Function<? super T, ? extends U> mapper;


        /**
         * Constructor of the AnnotationConverter class.
         *
         * @param klazz  The class of the DataObject.
         * @param mapper The function to convert the DataObject to a RemoteObject.
         */
        AnnotationConverter(Class<T> klazz, Function<? super T, ? extends U> mapper) {
            this.mapper = mapper;
            this.klazz  = klazz;
        }


        /**
         * Converts the DataObject to a RemoteObject.
         *
         * @param object The DataObject to convert.
         *
         * @return See above.
         */
        U convert(T object) {
            return mapper.apply(object);
        }


        /**
         * Returns the class of the DataObject.
         *
         * @return See above.
         */
        Class<T> getKlazz() {
            return klazz;
        }

    }
}
