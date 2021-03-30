package fr.igred.omero.roi;


import org.junit.Test;


public class ShapeErrorTest {


    @Test(expected = IllegalArgumentException.class)
    public void testPointNullCoordinates() {
        PointWrapper point = new PointWrapper();
        point.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testTextNullCoordinates() {
        TextWrapper text = new TextWrapper();
        text.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRectangleNullCoordinates() {
        RectangleWrapper rectangle = new RectangleWrapper();
        rectangle.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testMaskNullCoordinates() {
        MaskWrapper mask = new MaskWrapper();
        mask.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEllipseNullCoordinates() {
        EllipseWrapper ellipse = new EllipseWrapper();
        ellipse.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testLineNullCoordinates() {
        LineWrapper line = new LineWrapper();
        line.setCoordinates(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testPointWrongCoordinates() {
        PointWrapper point       = new PointWrapper();
        double[]     coordinates = {2, 2, 4, 4};
        point.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testTextWrongCoordinates() {
        TextWrapper text        = new TextWrapper();
        double[]    coordinates = {2, 2, 4, 4};
        text.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRectangleWrongCoordinates() {
        RectangleWrapper rectangle   = new RectangleWrapper();
        double[]         coordinates = {2, 2};
        rectangle.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testMaskWrongCoordinates() {
        MaskWrapper mask        = new MaskWrapper();
        double[]    coordinates = {2, 2};
        mask.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testEllipseWrongCoordinates() {
        EllipseWrapper ellipse     = new EllipseWrapper();
        double[]       coordinates = {2, 2};
        ellipse.setCoordinates(coordinates);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testLineWrongCoordinates() {
        LineWrapper line        = new LineWrapper();
        double[]    coordinates = {2, 2};
        line.setCoordinates(coordinates);
    }

}
