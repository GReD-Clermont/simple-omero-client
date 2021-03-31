package fr.igred.omero.roi;


import omero.gateway.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class ShapeList extends ArrayList<GenericShapeWrapper<?>> {


    <T extends GenericShapeWrapper<?>> List<T> getElementsOf(Class<T> clazz) {
        return stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }


    /**
     * Wraps the specified ShapeData object and add it to the end of this list.
     *
     * @param shape element to be wrapped and appended to this list
     *
     * @return {@code true} (as specified by {@link ArrayList#add(Object)})
     */
    public boolean add(ShapeData shape) {
        if (shape instanceof PointData) {
            return super.add(new PointWrapper((PointData) shape));
        } else if (shape instanceof TextData) {
            return super.add(new TextWrapper((TextData) shape));
        } else if (shape instanceof RectangleData) {
            return super.add(new RectangleWrapper((RectangleData) shape));
        } else if (shape instanceof MaskData) {
            return super.add(new MaskWrapper((MaskData) shape));
        } else if (shape instanceof EllipseData) {
            return super.add(new EllipseWrapper((EllipseData) shape));
        } else if (shape instanceof LineData) {
            return super.add(new LineWrapper((LineData) shape));
        } else if (shape instanceof PolylineData) {
            return super.add(new PolylineWrapper((PolylineData) shape));
        } else if (shape instanceof PolygonData) {
            return super.add(new PolygonWrapper((PolygonData) shape));
        } else {
            return super.add(new ShapeWrapper(shape));
        }
    }

}
