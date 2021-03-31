package fr.igred.omero.roi;


import omero.gateway.model.TextData;


public class TextWrapper extends GenericShapeWrapper<TextData> {


    /**
     * Constructor of the TextWrapper class using a TextData.
     *
     * @param shape the shape
     */
    public TextWrapper(TextData shape) {
        super(shape);
    }


    /**
     * Constructor of the TextWrapper class using a new empty ShapeData.
     */
    public TextWrapper() {
        this(new TextData());
    }


    /**
     * Creates a new instance of the TextWrapper, sets the centre and major, minor axes.
     *
     * @param text Object text.
     * @param x    x-coordinate of the shape.
     * @param y    y-coordinate of the shape.
     */
    public TextWrapper(String text, double x, double y) {
        this(new TextData(text, x, y));
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
     * Sets the coordinates of the TextData shape.
     *
     * @param x x-coordinate of the TextData shape.
     * @param y y-coordinate of the TextData shape.
     */
    public void setCoordinates(double x, double y) {
        setX(x);
        setY(y);
    }


    /**
     * Gets the coordinates of the TextData shape.
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
     * Sets the coordinates of the TextData object.
     *
     * @param coordinates Array of coordinates containing {X,Y}.
     */
    public void setCoordinates(double[] coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("TextData cannot set null coordinates.");
        } else if (coordinates.length == 2) {
            setCoordinates(coordinates[0], coordinates[1]);
        } else {
            throw new IllegalArgumentException("2 coordinates required for TextData.");
        }
    }

}
