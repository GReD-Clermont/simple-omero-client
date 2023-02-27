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

package fr.igred.omero.util;


import fr.igred.omero.Annotatable;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.RepositoryObject;
import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.roi.Shape;
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


/**
 * Utility class to convert DataObjects dynamically.
 */
@SuppressWarnings({"OverlyCoupledClass", "unchecked", "IfStatementWithTooManyBranches"})
public final class Wrapper {

    private static final String UNKNOWN_TYPE = "Unknown type: %s";


    private Wrapper() {
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
        U converted;

        if (object instanceof RectangleData) {
            converted = (U) new RectangleWrapper((RectangleData) object);
        } else if (object instanceof PolygonData) {
            converted = (U) new PolygonWrapper((PolygonData) object);
        } else if (object instanceof PolylineData) {
            converted = (U) new PolylineWrapper((PolylineData) object);
        } else if (object instanceof EllipseData) {
            converted = (U) new EllipseWrapper((EllipseData) object);
        } else if (object instanceof PointData) {
            converted = (U) new PointWrapper((PointData) object);
        } else if (object instanceof LineData) {
            converted = (U) new LineWrapper((LineData) object);
        } else if (object instanceof TextData) {
            converted = (U) new TextWrapper((TextData) object);
        } else if (object instanceof MaskData) {
            converted = (U) new MaskWrapper((MaskData) object);
        } else {
            throw new IllegalArgumentException(String.format(UNKNOWN_TYPE, object.getClass().getName()));
        }
        return converted;
    }


    /**
     * Converts (wraps) an AnnotationData object to an Annotation object.
     *
     * @param object The object to convert.
     * @param <T>    The AnnotationData type.
     * @param <U>    The Annotation type.
     *
     * @return See above.
     */
    public static <T extends AnnotationData, U extends Annotation> U wrap(T object) {
        U converted;

        if (object instanceof FileAnnotationData) {
            converted = (U) new FileAnnotationWrapper((FileAnnotationData) object);
        } else if (object instanceof MapAnnotationData) {
            converted = (U) new MapAnnotationWrapper((MapAnnotationData) object);
        } else if (object instanceof TagAnnotationData) {
            converted = (U) new TagAnnotationWrapper((TagAnnotationData) object);
        } else if (object instanceof RatingAnnotationData) {
            converted = (U) new RatingAnnotationWrapper((RatingAnnotationData) object);
        } else if (object instanceof TextualAnnotationData) {
            converted = (U) new TextualAnnotationWrapper((TextualAnnotationData) object);
        } else {
            throw new IllegalArgumentException(String.format(UNKNOWN_TYPE, object.getClass().getName()));
        }
        return converted;
    }


    /**
     * Converts (wraps) a DataObject object to an Annotatable object.
     *
     * @param object The object to convert.
     * @param <T>    The DataObject type.
     * @param <U>    The Annotatable type.
     *
     * @return See above.
     */
    public static <T extends DataObject, U extends Annotatable>
    U wrapAnnotatableObject(T object) {
        U converted;

        if (object instanceof ShapeData) {
            converted = (U) wrap((ShapeData) object);
        } else if (object instanceof ROIData) {
            converted = (U) new ROIWrapper((ROIData) object);
        } else {
            converted = wrapRepositoryObject(object);
        }
        return converted;
    }


    /**
     * Converts (wraps) a DataObject object to a Repository object.
     *
     * @param object The object to convert.
     * @param <T>    The DataObject type.
     * @param <U>    The RepositoryObject type.
     *
     * @return See above.
     */
    public static <T extends DataObject, U extends RepositoryObject>
    U wrapRepositoryObject(T object) {
        U converted;

        if (object instanceof ProjectData) {
            converted = (U) new ProjectWrapper((ProjectData) object);
        } else if (object instanceof DatasetData) {
            converted = (U) new DatasetWrapper((DatasetData) object);
        } else if (object instanceof ImageData) {
            converted = (U) new ImageWrapper((ImageData) object);
        } else if (object instanceof ScreenData) {
            converted = (U) new ScreenWrapper((ScreenData) object);
        } else if (object instanceof PlateData) {
            converted = (U) new PlateWrapper((PlateData) object);
        } else if (object instanceof PlateAcquisitionData) {
            converted = (U) new PlateAcquisitionWrapper((PlateAcquisitionData) object);
        } else if (object instanceof WellData) {
            converted = (U) new WellWrapper((WellData) object);
        } else if (object instanceof FolderData) {
            converted = (U) new FolderWrapper((FolderData) object);
        } else {
            throw new IllegalArgumentException(String.format(UNKNOWN_TYPE, object.getClass().getName()));
        }
        return converted;
    }


    /**
     * Converts (wraps) a DataObject to a RemoteObject.
     *
     * @param object The object to convert.
     * @param <T>    The DataObject type.
     * @param <U>    The RemoteObject type.
     *
     * @return See above.
     */
    public static <T extends DataObject, U extends RemoteObject> U wrap(T object) {
        U converted;
        if (object instanceof AnnotationData) {
            converted = (U) wrap((AnnotationData) object);
        } else if (object instanceof PixelsData) {
            converted = (U) new PixelsWrapper((PixelsData) object);
        } else if (object instanceof PlaneInfoData) {
            converted = (U) new PlaneInfoWrapper((PlaneInfoData) object);
        } else if (object instanceof WellSampleData) {
            converted = (U) new WellSampleWrapper((WellSampleData) object);
        } else if (object instanceof ExperimenterData) {
            converted = (U) new ExperimenterWrapper((ExperimenterData) object);
        } else if (object instanceof GroupData) {
            converted = (U) new GroupWrapper((GroupData) object);
        } else if (object instanceof ChannelData) {
            converted = (U) new ChannelWrapper((ChannelData) object);
        } else {
            converted = wrapAnnotatableObject(object);
        }
        return converted;
    }

}
