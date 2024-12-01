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


import fr.igred.omero.Annotatable;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.RepositoryObject;
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

import static java.lang.String.format;


/**
 * Utility class to convert DataObjects dynamically.
 */
@SuppressWarnings({"OverlyCoupledClass",
                   "IfStatementWithTooManyBranches"})
public final class Wrapper {

    private static final String UNKNOWN_TYPE = "Unknown type: %s";


    private Wrapper() {
    }


    /**
     * Converts (wraps) a ShapeData object to a Shape object.
     *
     * @param object The object to convert.
     * @param <T>    The ShapeData type.
     *
     * @return See above.
     */
    public static <T extends ShapeData> Shape wrap(T object) {
        Shape converted;

        if (object instanceof RectangleData) {
            converted = new RectangleWrapper((RectangleData) object);
        } else if (object instanceof PolygonData) {
            converted = new PolygonWrapper((PolygonData) object);
        } else if (object instanceof PolylineData) {
            converted = new PolylineWrapper((PolylineData) object);
        } else if (object instanceof EllipseData) {
            converted = new EllipseWrapper((EllipseData) object);
        } else if (object instanceof PointData) {
            converted = new PointWrapper((PointData) object);
        } else if (object instanceof LineData) {
            converted = new LineWrapper((LineData) object);
        } else if (object instanceof TextData) {
            converted = new TextWrapper((TextData) object);
        } else if (object instanceof MaskData) {
            converted = new MaskWrapper((MaskData) object);
        } else {
            String msg = format(UNKNOWN_TYPE, object.getClass().getName());
            throw new IllegalArgumentException(msg);
        }
        return converted;
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
        Annotation converted;
        if (object instanceof FileAnnotationData) {
            converted = new FileAnnotationWrapper((FileAnnotationData) object);
        } else if (object instanceof MapAnnotationData) {
            converted = new MapAnnotationWrapper((MapAnnotationData) object);
        } else if (object instanceof TagAnnotationData) {
            converted = new TagAnnotationWrapper((TagAnnotationData) object);
        } else if (object instanceof RatingAnnotationData) {
            converted = new RatingAnnotationWrapper((RatingAnnotationData) object);
        } else if (object instanceof TextualAnnotationData) {
            converted = new TextualAnnotationWrapper((TextualAnnotationData) object);
        } else {
            String msg = format(UNKNOWN_TYPE, object.getClass().getName());
            throw new IllegalArgumentException(msg);
        }
        return converted;
    }


    /**
     * Converts (wraps) a DataObject object to an Annotatable object.
     *
     * @param object The object to convert.
     * @param <T>    The DataObject type.
     *
     * @return See above.
     */
    public static <T extends DataObject> Annotatable wrapAnnotatableObject(T object) {
        Annotatable converted;
        if (object instanceof ShapeData) {
            converted = wrap((ShapeData) object);
        } else if (object instanceof ROIData) {
            converted = new ROIWrapper((ROIData) object);
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
     *
     * @return See above.
     */
    public static <T extends DataObject> RepositoryObject wrapRepositoryObject(T object) {
        RepositoryObject converted;

        if (object instanceof ProjectData) {
            converted = new ProjectWrapper((ProjectData) object);
        } else if (object instanceof DatasetData) {
            converted = new DatasetWrapper((DatasetData) object);
        } else if (object instanceof ImageData) {
            converted = new ImageWrapper((ImageData) object);
        } else if (object instanceof ScreenData) {
            converted = new ScreenWrapper((ScreenData) object);
        } else if (object instanceof PlateData) {
            converted = new PlateWrapper((PlateData) object);
        } else if (object instanceof PlateAcquisitionData) {
            converted = new PlateAcquisitionWrapper((PlateAcquisitionData) object);
        } else if (object instanceof WellData) {
            converted = new WellWrapper((WellData) object);
        } else if (object instanceof FolderData) {
            converted = new FolderWrapper((FolderData) object);
        } else {
            String msg = format(UNKNOWN_TYPE, object.getClass().getName());
            throw new IllegalArgumentException(msg);
        }
        return converted;
    }


    /**
     * Converts (wraps) a DataObject to a ObjectWrapper.
     *
     * @param object The object to convert.
     * @param <T>    The DataObject type.
     *
     * @return See above.
     */
    public static <T extends DataObject> RemoteObject wrap(T object) {
        RemoteObject converted;
        if (object instanceof AnnotationData) {
            converted = wrap((AnnotationData) object);
        } else if (object instanceof PixelsData) {
            converted = new PixelsWrapper((PixelsData) object);
        } else if (object instanceof PlaneInfoData) {
            converted = new PlaneInfoWrapper((PlaneInfoData) object);
        } else if (object instanceof WellSampleData) {
            converted = new WellSampleWrapper((WellSampleData) object);
        } else if (object instanceof ExperimenterData) {
            converted = new ExperimenterWrapper((ExperimenterData) object);
        } else if (object instanceof GroupData) {
            converted = new GroupWrapper((GroupData) object);
        } else if (object instanceof ChannelData) {
            converted = new ChannelWrapper((ChannelData) object);
        } else {
            converted = wrapAnnotatableObject(object);
        }
        return converted;
    }

}
