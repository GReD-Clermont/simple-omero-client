package fr.igred.omero.roi;


import omero.gateway.model.EllipseData;


public class EllipseWrapper extends GenericShapeWrapper<EllipseData> {


    /**
     * Constructor of the EllipseWrapper class using a EllipseData.
     *
     * @param shape the shape
     */
    public EllipseWrapper(EllipseData shape) {
        super(shape);
    }


    /**
     * Constructor of the EllipseWrapper class using a new empty EllipseData.
     */
    public EllipseWrapper() {
        this(new EllipseData());
    }


    /**
     * Constructor of the EllipseWrapper class using a new EllipseData.
     *
     * @param x       The x-coordinate of the center of the ellipse.
     * @param y       The y-coordinate of the center of the ellipse.
     * @param radiusX The radius along the X-axis.
     * @param radiusY The radius along the Y-axis.
     */
    public EllipseWrapper(double x, double y, double radiusX, double radiusY) {
        this(new EllipseData(x, y, radiusX, radiusY));
    }


    /**
     * Returns the x-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    public double getX() {
        return data.getX();
    }


    /**
     * Sets the x-coordinate of the center of the ellipse.
     *
     * @param x See above.
     */
    public void setX(double x) {
        data.setX(x);
    }


    /**
     * Returns the y-coordinate of the center of the ellipse.
     *
     * @return See above.
     */
    public double getY() {
        return data.getY();
    }


    /**
     * Sets the y-coordinate of the center of the ellipse.
     *
     * @param y See above.
     */
    public void setY(double y) {
        data.setY(y);
    }


    /**
     * Returns the radius along the X-axis.
     *
     * @return See above.
     */
    public double getRadiusX() {
        return data.getRadiusX();
    }


    /**
     * Sets the radius along the X-axis.
     *
     * @param x the value to set.
     */
    public void setRadiusX(double x) {
        data.setRadiusX(x);
    }


    /**
     * Returns the radius along the Y-axis.
     *
     * @return See above.
     */
    public double getRadiusY() {
        return data.getRadiusY();
    }


    /**
     * Sets the radius along the Y-axis.
     *
     * @param y The value to set.
     */
    public void setRadiusY(double y) {
        data.setRadiusY(y);
    }


    /**
     * Sets the coordinates of the EllipseData shape.
     *
     * @param x       The x-coordinate of the center of the ellipse.
     * @param y       The y-coordinate of the center of the ellipse.
     * @param radiusX The radius along the X-axis.
     * @param radiusY The radius along the Y-axis.
     */
    public void setCoordinates(double x, double y, double radiusX, double radiusY) {
        setX(x);
        setY(y);
        setRadiusX(radiusX);
        setRadiusY(radiusY);
    }


    /**
     * Gets the coordinates of the MaskData shape.
     *
     * @return Array of coordinates containing {X,Y,RadiusX,RadiusY}.
     */
    public double[] getCoordinates() {
        double[] coordinates = new double[4];
        coordinates[0] = getX();
        coordinates[1] = getY();
        coordinates[2] = getRadiusX();
        coordinates[3] = getRadiusY();
        return coordinates;
    }


    /**
     * Sets the coordinates of the EllipseData shape.
     *
     * @param coordinates Array of coordinates containing {X,Y,RadiusX,RadiusY}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("EllipseData cannot set null coordinates.");
        } else if (coordinates.length == 4) {
            setCoordinates(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
        } else {
            throw new IllegalArgumentException("4 coordinates required for EllipseData.");
        }
    }

}
