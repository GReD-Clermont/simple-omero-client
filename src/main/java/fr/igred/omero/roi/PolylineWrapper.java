package fr.igred.omero.roi;


import omero.gateway.model.PolylineData;

import java.awt.geom.Point2D;
import java.util.List;


public class PolylineWrapper extends ShapeWrapper<PolylineData> {


    /**
     * Constructor of the PolylineWrapper class using a PolylineData.
     *
     * @param shape the shape.
     */
    public PolylineWrapper(PolylineData shape) {
        super(shape);
    }


    /**
     * Constructor of the RectangleWrapper class using a new empty LineData.
     */
    public PolylineWrapper() {
        this(new PolylineData());
    }


    /**
     * Constructor of the RectangleWrapper class using a new LineData.
     *
     * @param points the points in the polyline.
     */
    public PolylineWrapper(List<Point2D.Double> points) {
        this(new PolylineData(points));
    }


    /**
     * Returns the points in the Polyline.
     *
     * @return See above.
     */
    public List<Point2D.Double> getPoints() {
        return data.getPoints();
    }


    /**
     * Sets the points in the polyline.
     *
     * @param points The points to set.
     */
    public void setPoints(List<Point2D.Double> points) {
        data.setPoints(points);
    }

}
