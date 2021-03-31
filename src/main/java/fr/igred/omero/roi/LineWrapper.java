package fr.igred.omero.roi;


import omero.gateway.model.LineData;


public class LineWrapper extends GenericShapeWrapper<LineData> {


    /**
     * Constructor of the LineWrapper class using a LineData.
     *
     * @param shape the shape
     */
    public LineWrapper(LineData shape) {
        super(shape);
    }


    /**
     * Constructor of the RectangleWrapper class using a new empty LineData.
     */
    public LineWrapper() {
        this(new LineData());
    }


    /**
     * Constructor of the RectangleWrapper class using a new LineData.
     *
     * @param x1 x1-coordinate of the shape.
     * @param y1 y1-coordinate of the shape.
     * @param x2 x2-coordinate of the shape.
     * @param y2 y2-coordinate of the shape.
     */
    public LineWrapper(double x1, double y1, double x2, double y2) {
        this(new LineData(x1, y1, x2, y2));
    }


    /**
     * Returns the x-coordinate of the starting point of an untransformed line.
     *
     * @return See above.
     */
    public double getX1() {
        return data.getX1();
    }


    /**
     * Set the x-coordinate of the starting point of an untransformed line.
     *
     * @param x1 See above.
     */
    public void setX1(double x1) {
        data.setX1(x1);
    }


    /**
     * Returns the x-coordinate of the end point of an untransformed line.
     *
     * @return See above.
     */
    public double getX2() {
        return data.getX2();
    }


    /**
     * Set the x-coordinate of the end point of an untransformed line.
     *
     * @param x2 See above.
     */
    public void setX2(double x2) {
        data.setX2(x2);
    }


    /**
     * Returns the y-coordinate of the starting point of an untransformed line.
     *
     * @return See above.
     */
    public double getY1() {
        return data.getY1();
    }


    /**
     * Set the y-coordinate of the starting point of an untransformed line.
     *
     * @param y1 See above.
     */
    public void setY1(double y1) {
        data.setY1(y1);
    }


    /**
     * Returns the y-coordinate of the end point of an untransformed line.
     *
     * @return See above.
     */
    public double getY2() {
        return data.getY2();
    }


    /**
     * Set the y-coordinate of the end point of an untransformed line.
     *
     * @param y2 See above.
     */
    public void setY2(double y2) {
        data.setY2(y2);
    }


    /**
     * Sets the coordinates of the LineData shape.
     *
     * @param x1 x-coordinate of the starting point of an untransformed line.
     * @param y1 y-coordinate of the starting point of an untransformed line.
     * @param x2 x-coordinate of the end point of an untransformed line.
     * @param y2 y-coordinate of the end point of an untransformed line.
     */
    public void setCoordinates(double x1, double y1, double x2, double y2) {
        setX1(x1);
        setY1(y1);
        setX2(x2);
        setY2(y2);
    }


    /**
     * Gets the coordinates of the LineData shape.
     *
     * @return Array of coordinates containing {X1,Y1,X2,Y2}.
     */
    public double[] getCoordinates() {
        double[] coordinates = new double[4];
        coordinates[0] = getX1();
        coordinates[1] = getY1();
        coordinates[2] = getX2();
        coordinates[3] = getY2();
        return coordinates;
    }


    /**
     * Sets the coordinates of the LineData shape.
     *
     * @param coordinates Array of coordinates containing {X1,Y1,X2,Y2}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("LineData cannot set null coordinates.");
        } else if (coordinates.length == 4) {
            setCoordinates(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
        } else {
            throw new IllegalArgumentException("4 coordinates required for LineData.");
        }
    }

}
