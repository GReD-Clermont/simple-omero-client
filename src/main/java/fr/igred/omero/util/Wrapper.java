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

package fr.igred.omero.util;


import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.RatingAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.annotations.TextualAnnotationWrapper;
import fr.igred.omero.containers.DatasetWrapper;
import fr.igred.omero.containers.FolderWrapper;
import fr.igred.omero.containers.ProjectWrapper;
import fr.igred.omero.core.ChannelWrapper;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.core.PixelsWrapper;
import fr.igred.omero.core.PlaneInfoWrapper;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import fr.igred.omero.roi.EllipseWrapper;
import fr.igred.omero.roi.LineWrapper;
import fr.igred.omero.roi.MaskWrapper;
import fr.igred.omero.roi.PointWrapper;
import fr.igred.omero.roi.PolygonWrapper;
import fr.igred.omero.roi.PolylineWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import fr.igred.omero.roi.Shape;
import fr.igred.omero.roi.TextWrapper;
import fr.igred.omero.screen.PlateAcquisitionWrapper;
import fr.igred.omero.screen.PlateWrapper;
import fr.igred.omero.screen.ScreenWrapper;
import fr.igred.omero.screen.WellSampleWrapper;
import fr.igred.omero.screen.WellWrapper;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.EllipseData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FolderData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LineData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlaneInfoData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.PointData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TextData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;

import java.util.function.Function;

import static java.lang.String.format;


/**
 * Utility class to convert DataObjects dynamically.
 */
public enum Wrapper {
    /** Containers */
    FOLDER(FolderData.class, FolderWrapper::new),
    PROJECT(ProjectData.class, ProjectWrapper::new),
    DATASET(DatasetData.class, DatasetWrapper::new),
    /** Screen */
    SCREEN(ScreenData.class, ScreenWrapper::new),
    PLATE(PlateData.class, PlateWrapper::new),
    PLATEACQUISITION(PlateAcquisitionData.class, PlateAcquisitionWrapper::new),
    WELLSAMPLE(WellSampleData.class, WellSampleWrapper::new),
    WELL(WellData.class, WellWrapper::new),
    /** Core */
    IMAGE(ImageData.class, ImageWrapper::new),
    PIXELS(PixelsData.class, PixelsWrapper::new),
    PLANEINFO(PlaneInfoData.class, PlaneInfoWrapper::new),
    CHANNEL(ChannelData.class, ChannelWrapper::new),
    /** ROI */
    ROI(ROIData.class, ROIWrapper::new),
    /** Shapes */
    RECTANGLE(RectangleData.class, RectangleWrapper::new),
    POLYGON(PolygonData.class, PolygonWrapper::new),
    POLYLINE(PolylineData.class, PolylineWrapper::new),
    ELLIPSE(EllipseData.class, EllipseWrapper::new),
    POINT(PointData.class, PointWrapper::new),
    LINE(LineData.class, LineWrapper::new),
    TEXT(TextData.class, TextWrapper::new),
    MASK(MaskData.class, MaskWrapper::new),
    /** Annotations */
    FILEANNOTATION(FileAnnotationData.class, FileAnnotationWrapper::new),
    MAPANNOTATION(MapAnnotationData.class, MapAnnotationWrapper::new),
    TAGANNOTATION(TagAnnotationData.class, TagAnnotationWrapper::new),
    RATINGANNOTATION(RatingAnnotationData.class, RatingAnnotationWrapper::new),
    TEXTUALANNOTATION(TextualAnnotationData.class, TextualAnnotationWrapper::new),
    /** Meta */
    EXPERIMENTER(ExperimenterData.class, ExperimenterWrapper::new),
    GROUP(GroupData.class, GroupWrapper::new);

    /** Error message for unknown type. */
    private static final String UNKNOWN_TYPE = "Unknown type: %s";

    /** The converter to convert the DataObject to a RemoteObject. */
    private final Converter<? extends DataObject, ? extends RemoteObject> converter;


    /** Constructor of the Wrappers enum. */
    <T extends DataObject, U extends RemoteObject> Wrapper(Class<T> klazz, Function<T, U> mapper) {
        converter = new Converter<>(klazz, mapper);
    }


    /**
     * Returns the Wrapper enum from a given class.
     *
     * @param klazz The class to get the Wrapper enum from.
     *
     * @return See above.
     */
    private static Wrapper fromClass(Class<? extends DataObject> klazz) {
        for (Wrapper c : values()) {
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
    private static <T extends DataObject, U extends RemoteObject>
    Converter<T, U> getConverter(T object) {
        Wrapper c = fromClass(object.getClass());
        //noinspection unchecked
        return (Converter<T, U>) c.converter;
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
    public static <T extends ShapeData, U extends Shape> U wrap(T object) {
        Converter<T, U> converter = getConverter(object);
        return converter.convert(object);
    }


    /**
     * Converts (wraps) an AnnotationData object to an Annotation object.
     *
     * @param object The object to convert.
     * @param <T>    The AnnotationData type.
     *
     * @return See above.
     */
    public static <T extends AnnotationData> Annotation wrap(T object) {
        Converter<T, Annotation> converter = getConverter(object);
        return converter.convert(object);
    }


    /**
     * Converts (wraps) a DataObject to a ObjectWrapper.
     *
     * @param object The object to convert.
     * @param <T>    The DataObject type.
     * @param <U>    The RemoteObject type.
     *
     * @return See above.
     */
    public static <T extends DataObject, U extends RemoteObject> U wrap(T object) {
        Converter<T, U> converter = getConverter(object);
        return converter.convert(object);
    }


    /**
     * Converter class to convert DataObjects to RemoteObjects.
     *
     * @param <T> The DataObject type.
     * @param <U> The RemoteObject type.
     */
    private static class Converter<T extends DataObject, U extends RemoteObject> {

        /** The class of the DataObject. */
        private final Class<T> klazz;

        /** The function to convert the DataObject to a RemoteObject. */
        private final Function<? super T, ? extends U> mapper;


        /**
         * Constructor of the Converter class.
         *
         * @param klazz  The class of the DataObject.
         * @param mapper The function to convert the DataObject to a RemoteObject.
         */
        Converter(Class<T> klazz, Function<? super T, ? extends U> mapper) {
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
