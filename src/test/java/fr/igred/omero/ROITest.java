package fr.igred.omero;


import fr.igred.omero.metadata.ROIContainer;
import fr.igred.omero.metadata.ShapeContainer;
import loci.common.DebugTools;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class ROITest extends BasicTest {


    @Test
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

        assertEquals(1, rois.size());
        assertEquals(4, rois.get(0).getShapes().size());

        for (ROIContainer roi : rois) {
            root.deleteROI(roi);
        }

        rois = image.getROIs(root);

        assertEquals(0, rois.size());
    }


    @Test
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

        assertEquals(1, rois.size());
        assertEquals(4, rois.get(0).getShapes().size());

        for (ROIContainer roi : rois) {
            root.deleteROI(roi);
        }

        rois = image.getROIs(root);

        assertEquals(0, rois.size());
    }


    @Test
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
        assertEquals(size + 1, roiContainer.getShapes().size());
        assertEquals(ROINumber, rois.size());

        roiContainer.deleteShape(roiContainer.getShapes().size() - 1);
        roiContainer.saveROI(root);

        rois = image.getROIs(root);
        roiContainer = rois.get(0);
        assertEquals(size, roiContainer.getShapes().size());
        assertEquals(ROINumber, rois.size());
    }

}