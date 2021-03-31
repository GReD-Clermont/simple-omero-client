package fr.igred.omero.roi;


import omero.gateway.model.PolygonData;

import java.awt.geom.Point2D;
import java.util.List;


public class PolygonWrapper extends GenericShapeWrapper<PolygonData> {


    /**
     * Constructor of the PolygonWrapper class using a PolygonData.
     *
     * @param shape the shape.
     */
    public PolygonWrapper(PolygonData shape) {
        super(shape);
    }


    /**
     * Constructor of the PolygonWrapper class using a new empty LineData.
     */
    public PolygonWrapper() {
        this(new PolygonData());
    }


    /**
     * Constructor of the PolygonWrapper class using a new LineData.
     *
     * @param points the points in the polyline.
     */
    public PolygonWrapper(List<Point2D.Double> points) {
        this(new PolygonData(points));
    }


    /**
     * Returns the points in the Polygon.
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


    /**
     * Returns the points in the polygon.
     *
     * @return See above.
     */
    public List<Integer> getMaskPoints() {
        return data.getMaskPoints();
    }

}
