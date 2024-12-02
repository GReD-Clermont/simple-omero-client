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

package fr.igred.omero;


import fr.igred.omero.annotations.AnnotationsWrapper;
import fr.igred.omero.containers.DatasetWrapper;
import fr.igred.omero.containers.FolderWrapper;
import fr.igred.omero.containers.ProjectWrapper;
import fr.igred.omero.core.ChannelWrapper;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.core.PixelsWrapper;
import fr.igred.omero.core.PlaneInfoWrapper;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.ShapesWrapper;
import fr.igred.omero.screen.PlateAcquisitionWrapper;
import fr.igred.omero.screen.PlateWrapper;
import fr.igred.omero.screen.ScreenWrapper;
import fr.igred.omero.screen.WellSampleWrapper;
import fr.igred.omero.screen.WellWrapper;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FolderData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlaneInfoData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;

import java.util.function.Function;

import static java.lang.String.format;


/**
 * Utility class to convert DataObjects dynamically.
 */
public enum Wrapper {
    /**
     * Shapes
     */
    SHAPE(ShapeData.class, ShapesWrapper::wrap),
    /**
     * Annotations
     */
    ANNOTATION(AnnotationData.class, AnnotationsWrapper::wrap),
    /**
     * Containers
     */
    FOLDER(FolderData.class, FolderWrapper::new),
    PROJECT(ProjectData.class, ProjectWrapper::new),
    DATASET(DatasetData.class, DatasetWrapper::new),
    /**
     * Screen
     */
    SCREEN(ScreenData.class, ScreenWrapper::new),
    PLATE(PlateData.class, PlateWrapper::new),
    PLATEACQUISITION(PlateAcquisitionData.class, PlateAcquisitionWrapper::new),
    WELLSAMPLE(WellSampleData.class, WellSampleWrapper::new),
    WELL(WellData.class, WellWrapper::new),
    /**
     * Core
     */
    IMAGE(ImageData.class, ImageWrapper::new),
    PIXELS(PixelsData.class, PixelsWrapper::new),
    PLANEINFO(PlaneInfoData.class, PlaneInfoWrapper::new),
    CHANNEL(ChannelData.class, ChannelWrapper::new),
    /**
     * ROI
     */
    ROI(ROIData.class, ROIWrapper::new),
    /**
     * Meta
     */
    EXPERIMENTER(ExperimenterData.class, ExperimenterWrapper::new),
    GROUP(GroupData.class, GroupWrapper::new);

    /**
     * Error message for unknown type.
     */
    private static final String UNKNOWN_TYPE = "Unknown type: %s";

    /**
     * The converter to convert the DataObject to a RemoteObject.
     */
    private final Converter<? extends DataObject, ? extends RemoteObject> converter;


    /**
     * Constructor of the Wrappers enum.
     */
    <T extends DataObject, U extends ObjectWrapper<? extends T>>
    Wrapper(Class<T> klazz, Function<T, U> mapper) {
        converter = new Converter<>(klazz, mapper);
    }

    /**
     * Returns the Wrapper enum from a given class.
     *
     * @param object The object to convert.
     *
     * @return See above.
     */
    private static Wrapper fromObject(DataObject object) {
        for (Wrapper c : values()) {
            if (c.isWrapperFor(object)) {
                return c;
            }
        }
        if (object == null) {
            throw new IllegalArgumentException("Object is null");
        } else {
            String msg = format(UNKNOWN_TYPE, object.getClass().getName());
            throw new IllegalArgumentException(msg);
        }
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
    private static <T extends DataObject, U extends ObjectWrapper<T>>
    Converter<T, U> getConverter(T object) {
        Wrapper c = fromObject(object);
        //noinspection unchecked
        return (Converter<T, U>) c.converter;
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
    public static <T extends DataObject, U extends ObjectWrapper<T>> U wrap(T object) {
        Converter<T, U> converter = getConverter(object);
        return converter.convert(object);
    }

    /**
     * Returns whether the provided object can be wrapped by the current wrapper.
     *
     * @param target The object to compare.
     *
     * @return See above.
     */
    boolean isWrapperFor(DataObject target) {
        return converter.canConvert(target);
    }

    @Override
    public String toString() {
        return format("Wrapper{%s}", name());
    }

    /**
     * Converter class to convert DataObjects to RemoteObjects.
     *
     * @param <T> The DataObject type.
     * @param <U> The RemoteObject type.
     */
    private static class Converter<T extends DataObject, U extends RemoteObject> {

        /**
         * The class of the DataObject.
         */
        private final Class<T> klazz;

        /**
         * The function to convert the DataObject to a RemoteObject.
         */
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
         * Returns whether the provided object can be converted.
         *
         * @param target The object to compare.
         *
         * @return See above.
         */
        boolean canConvert(DataObject target) {
            return this.klazz.isInstance(target);
        }

    }
}
