package fr.igred.omero.util;


import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import fr.igred.omero.meta.PlaneInfoWrapper;
import fr.igred.omero.repository.ChannelWrapper;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.FolderWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.PixelsWrapper;
import fr.igred.omero.repository.PlateAcquisitionWrapper;
import fr.igred.omero.repository.PlateWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.omero.repository.RepositoryObjectWrapper;
import fr.igred.omero.repository.ScreenWrapper;
import fr.igred.omero.repository.WellSampleWrapper;
import fr.igred.omero.repository.WellWrapper;
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
import omero.gateway.model.RectangleData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TextData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;


@SuppressWarnings({"OverlyCoupledClass", "unchecked"})
public final class Wrapper {

    private static final String UNKNOWN_TYPE = "Unknown type: %s";


    private Wrapper() {
    }


    @SuppressWarnings("IfStatementWithTooManyBranches")
    public static <T extends ShapeData, U extends Shape<? extends T>> U wrap(T object) {
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


    public static <T extends AnnotationData, U extends Annotation<? extends T>> U wrap(T object) {
        U converted;

        if (object instanceof FileAnnotationData) {
            converted = (U) new FileAnnotationWrapper((FileAnnotationData) object);
        } else if (object instanceof MapAnnotationData) {
            converted = (U) new MapAnnotationWrapper((MapAnnotationData) object);
        } else if (object instanceof TagAnnotationData) {
            converted = (U) new TagAnnotationWrapper((TagAnnotationData) object);
        } else {
            throw new IllegalArgumentException(String.format(UNKNOWN_TYPE, object.getClass().getName()));
        }
        return converted;
    }


    @SuppressWarnings("IfStatementWithTooManyBranches")
    public static <T extends DataObject, U extends RepositoryObjectWrapper<? extends T>>
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


    @SuppressWarnings("IfStatementWithTooManyBranches")
    public static <T extends DataObject, U extends RemoteObject<? extends T>> U wrap(T object) {
        U converted;
        if (object instanceof ShapeData) {
            converted = (U) wrap((ShapeData) object);
        } else if (object instanceof AnnotationData) {
            converted = (U) wrap((AnnotationData) object);
        } else if (object instanceof PixelsData) {
            converted = (U) new PixelsWrapper((PixelsData) object);
        } else if (object instanceof ROIData) {
            converted = (U) new ROIWrapper((ROIData) object);
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
            converted = (U) wrapRepositoryObject(object);
        }
        return converted;
    }

}