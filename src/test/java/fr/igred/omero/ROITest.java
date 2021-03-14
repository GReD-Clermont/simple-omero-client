package fr.igred.omero;


import fr.igred.omero.metadata.ROIContainer;
import fr.igred.omero.metadata.ShapeContainer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import loci.common.DebugTools;

import java.util.ArrayList;
import java.util.List;


public class ROITest extends TestCase {

    /**
     * Create the test case for Client
     *
     * @param testName Name of the test case.
     */
    public ROITest(String testName) {
        super(testName);
    }


    /**
     * @return the suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(ROITest.class);
    }


    public void testROI() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ROIContainer roiContainer = new ROIContainer();

        ImageContainer image = root.getImage(1L);

        roiContainer.setImage(image);

        for (int i = 0; i < 4; i++) {
            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roiContainer.addShape(rectangle);
        }

        image.saveROI(root, roiContainer);

        List<ROIContainer> rois = image.getROIs(root);

        assert (rois.size() == 1);
        assert (rois.get(0).getShapes().size() == 4);

        for (ROIContainer roi : rois) {
            root.deleteROI(roi);
        }

        rois = image.getROIs(root);

        assert (rois.size() == 0);
    }


    public void testROI2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        List<ShapeContainer> shapes = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            shapes.add(rectangle);
        }

        ROIContainer roiContainer = new ROIContainer(shapes);
        roiContainer.setImage(image);
        image.saveROI(root, roiContainer);

        List<ROIContainer> rois = image.getROIs(root);

        assert (rois.size() == 1);
        assert (rois.get(0).getShapes().size() == 4);

        for (ROIContainer roi : rois) {
            root.deleteROI(roi);
        }

        rois = image.getROIs(root);

        assert (rois.size() == 0);
    }


    public void testRoiAddShapeAndDeleteIt() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        List<ShapeContainer> shapes = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            shapes.add(rectangle);
        }

        ROIContainer roiContainer = new ROIContainer();
        roiContainer.addShapes(shapes);
        roiContainer.setImage(image);
        image.saveROI(root, roiContainer);

        List<ROIContainer> rois = image.getROIs(root);

        roiContainer = rois.get(0);
        int size      = roiContainer.getShapes().size();
        int ROINumber = rois.size();

        ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
        rectangle.setRectangleCoordinates(2, 2, 8, 8);
        rectangle.setZ(2);
        rectangle.setT(2);
        rectangle.setC(2);

        roiContainer.addShape(rectangle);
        roiContainer.saveROI(root);

        rois = image.getROIs(root);
        roiContainer = rois.get(0);
        assert (size + 1 == roiContainer.getShapes().size());
        assert (ROINumber == rois.size());

        roiContainer.deleteShape(roiContainer.getShapes().size() - 1);
        roiContainer.saveROI(root);

        rois = image.getROIs(root);
        roiContainer = rois.get(0);
        assert (size == roiContainer.getShapes().size());
        assert (ROINumber == rois.size());
    }

}