package fr.igred.omero.roi;


import omero.gateway.model.RectangleData;


public class RectangleWrapper extends ShapeWrapper<RectangleData> {


    /**
     * Constructor of the RectangleWrapper class using a RectangleData.
     *
     * @param shape the shape
     */
    public RectangleWrapper(RectangleData shape) {
        super(shape);
    }


    /**
     * Constructor of the RectangleWrapper class using a new empty RectangleData.
     */
    public RectangleWrapper() {
        this(new RectangleData());
    }


    /**
     * Constructor of the RectangleWrapper class using a new RectangleData.
     *
     * @param x      The x-coordinate of the top-left corner.
     * @param y      The y-coordinate of the top-left corner.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public RectangleWrapper(double x, double y, double width, double height) {
        this(new RectangleData(x, y, width, height));
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
     * Returns the width untransformed rectangle.
     *
     * @return See above.
     */
    public double getWidth() {
        return data.getWidth();
    }


    /**
     * Sets width of an untransformed rectangle.
     *
     * @param width See above.
     */
    public void setWidth(double width) {
        data.setWidth(width);
    }


    /**
     * Returns the height untransformed rectangle.
     *
     * @return See above.
     */
    public double getHeight() {
        return data.getHeight();
    }


    /**
     * Sets the height of an untransformed rectangle.
     *
     * @param height See above.
     */
    public void setHeight(double height) {
        data.setHeight(height);
    }


    /**
     * Sets the coordinates of the RectangleData shape.
     *
     * @param x      The x-coordinate of the top-left corner.
     * @param y      The y-coordinate of the top-left corner.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public void setCoordinates(double x, double y, double width, double height) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }


    /**
     * Gets the coordinates of the RectangleData shape.
     *
     * @return Array of coordinates containing {X,Y,Width,Height}.
     */
    public double[] getCoordinates() {
        double[] coordinates = new double[4];
        coordinates[0] = getX();
        coordinates[1] = getY();
        coordinates[2] = getWidth();
        coordinates[3] = getHeight();
        return coordinates;
    }


    /**
     * Sets the coordinates of the RectangleData shape.
     *
     * @param coordinates Array of coordinates containing {X,Y,Width,Height}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("RectangleData cannot set null coordinates.");
        } else if (coordinates.length == 4) {
            setCoordinates(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
        } else {
            throw new IllegalArgumentException("4 coordinates required for RectangleData.");
        }
    }

}
