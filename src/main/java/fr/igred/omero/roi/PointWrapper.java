package fr.igred.omero.roi;


import omero.gateway.model.PointData;


public class PointWrapper extends GenericShapeWrapper<PointData> {


    /**
     * Constructor of the PointWrapper class using a PointData.
     *
     * @param shape the shape
     */
    public PointWrapper(PointData shape) {
        super(shape);
    }


    /**
     * Constructor of the PointWrapper class using a new empty PointData.
     */
    public PointWrapper() {
        this(new PointData());
    }


    /**
     * Constructor of the PointWrapper class using a new empty ShapeData.
     *
     * @param x x-coordinate of the shape.
     * @param y y-coordinate of the shape.
     */
    public PointWrapper(double x, double y) {
        this(new PointData(x, y));
    }


    /**
     * Returns the x-coordinate of the shape.
     *
     * @return See above.
     */
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate of the shape.
     *
     * @param x See above.
     */
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y coordinate of the shape.
     *
     * @return See above.
     */
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate of the shape.
     *
     * @param y See above.
     */
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Sets the coordinates the PointData shape.
     *
     * @param x x-coordinate of the PointData shape.
     * @param y y-coordinate of the PointData shape.
     */
    public void setCoordinates(double x, double y) {
        setX(x);
        setY(y);
    }


    /**
     * Gets the coordinates of the PointData shape.
     *
     * @return Array of coordinates containing {X,Y}.
     */
    public double[] getCoordinates() {
        double[] coordinates = new double[2];
        coordinates[0] = getX();
        coordinates[1] = getY();
        return coordinates;
    }


    /**
     * Sets the coordinates of the PointData shape.
     *
     * @param coordinates Array of coordinates containing {X,Y}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("PointData cannot set null coordinates.");
        } else if (coordinates.length == 2) {
            setCoordinates(coordinates[0], coordinates[1]);
        } else {
            throw new IllegalArgumentException("2 coordinates required for PointData.");
        }
    }

}
