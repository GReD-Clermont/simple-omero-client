package fr.igred.omero;


import fr.igred.omero.metadata.ROIContainer;
import fr.igred.omero.metadata.ShapeContainer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class ROITest extends UserTest {


    @Test
    public void testROI() throws Exception {
        ROIContainer roiContainer = new ROIContainer();

        ImageContainer image = client.getImage(1L);

        roiContainer.setImage(image);

        for (int i = 0; i < 4; i++) {
            ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
            rectangle.setRectangleCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roiContainer.addShape(rectangle);
        }

        image.saveROI(client, roiContainer);

        List<ROIContainer> rois = image.getROIs(client);

        assertEquals(1, rois.size());
        assertEquals(4, rois.get(0).getShapes().size());

        for (ROIContainer roi : rois) {
            client.deleteROI(roi);
        }

        rois = image.getROIs(client);

        assertEquals(0, rois.size());
    }


    @Test
    public void testROI2() throws Exception {
        ImageContainer image = client.getImage(1L);

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
        image.saveROI(client, roiContainer);

        List<ROIContainer> rois = image.getROIs(client);

        assertEquals(1, rois.size());
        assertEquals(4, rois.get(0).getShapes().size());

        for (ROIContainer roi : rois) {
            client.deleteROI(roi);
        }

        rois = image.getROIs(client);

        assertEquals(0, rois.size());
    }


    @Test
    public void testRoiAddShapeAndDeleteIt() throws Exception {
        ImageContainer image = client.getImage(1L);

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
        image.saveROI(client, roiContainer);

        List<ROIContainer> rois = image.getROIs(client);

        roiContainer = rois.get(0);
        int size      = roiContainer.getShapes().size();
        int ROINumber = rois.size();

        ShapeContainer rectangle = new ShapeContainer(ShapeContainer.RECTANGLE);
        rectangle.setRectangleCoordinates(2, 2, 8, 8);
        rectangle.setZ(2);
        rectangle.setT(2);
        rectangle.setC(2);

        roiContainer.addShape(rectangle);
        roiContainer.saveROI(client);

        rois = image.getROIs(client);
        roiContainer = rois.get(0);
        assertEquals(size + 1, roiContainer.getShapes().size());
        assertEquals(ROINumber, rois.size());

        roiContainer.deleteShape(roiContainer.getShapes().size() - 1);
        roiContainer.saveROI(client);

        rois = image.getROIs(client);
        roiContainer = rois.get(0);

        assertEquals(size, roiContainer.getShapes().size());
        assertEquals(ROINumber, rois.size());
    }

}