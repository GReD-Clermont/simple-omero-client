/*
 *  Copyright (C) 2020 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero.annotations;


import fr.igred.omero.UserTest;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class TableTest extends UserTest {


    @Test
    public void testCreateTable() throws Exception {
        DatasetWrapper dataset = client.getDataset(1L);

        List<ImageWrapper> images = dataset.getImages(client);

        TableWrapper table = new TableWrapper(2, "TableTest");

        assertEquals(2, table.getColumnCount());

        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);
        assertEquals("Image", table.getColumns()[0].getName());
        assertEquals("Name", table.getColumns()[1].getName());

        table.setRowCount(images.size());

        assertEquals(images.size(), table.getRowCount());

        for (ImageWrapper image : images) {
            assertNotEquals(true, table.isComplete());
            table.addRow(image.asImageData(), image.getName());
        }

        assertEquals(images.get(0).asImageData(), table.getData(0, 0));
        assertEquals(images.get(1).getName(), table.getData(0, 1));

        dataset.addTable(client, table);

        List<TableWrapper> tables = dataset.getTables(client);

        assertEquals(1, tables.size());

        client.delete(tables.get(0));

        tables = dataset.getTables(client);

        assertEquals(0, tables.size());
    }


    @Test
    public void testErrorTableFull() throws Exception {
        boolean        exception = false;
        DatasetWrapper dataset   = client.getDataset(1L);

        List<ImageWrapper> images = dataset.getImages(client);

        TableWrapper table = new TableWrapper(2, "TableTest");
        table.setName("TableTestNewName");

        assertEquals("TableTestNewName", table.getName());

        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        table.setRowCount(images.size() - 1);

        try {
            for (ImageWrapper image : images) {
                table.addRow(image.asImageData(), image.getName());
            }
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testErrorTableColumn() {
        TableWrapper table = new TableWrapper(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        try {
            table.setColumn(2, "Id", Long.class);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }


    @Test
    public void testErrorTableUninitialized() throws Exception {
        boolean exception = false;

        DatasetWrapper dataset = client.getDataset(1L);

        List<ImageWrapper> images = dataset.getImages(client);

        TableWrapper table = new TableWrapper(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        try {
            for (ImageWrapper image : images) {
                table.addRow(image.asImageData(), image.getName());
            }
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testErrorTableNotEnoughArgs() throws Exception {
        boolean exception = false;

        DatasetWrapper dataset = client.getDataset(1L);

        List<ImageWrapper> images = dataset.getImages(client);

        TableWrapper table = new TableWrapper(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        table.setRowCount(images.size());

        try {
            for (ImageWrapper image : images) {
                table.addRow(image.asImageData());
            }
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testCreateTableWithROIsFromIJResults1() throws Exception {
        long imageId = 1L;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois);

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        results.setLabel(image.getName(), 0);
        results.setValue("ROI", 0, rois.get(0).getId());
        results.setValue("Volume", 0, 25.0);
        results.setValue("Volume Unit", 0, "µm^3");

        TableWrapper table = new TableWrapper(client, results, imageId, ijRois, "ROI");

        Object[][] data = table.getData();
        assertEquals(1, table.getRowCount());
        assertEquals(image.getId(), ((ImageData) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((ROIData) data[1][0]).getId());
        assertEquals(image.getName(), data[2][0]);
        assertEquals(25.0d, (Double) data[3][0], 0.05);
        assertEquals("µm^3", data[4][0]);

        image.addTable(client, table);

        assertNotNull(table.getFileId());

        client.delete(table);

        for (ROIWrapper r : rois) {
            client.delete(r);
        }
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testCreateTableWithROIsFromIJResults2() throws Exception {
        long imageId = 1L;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois);

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        String label = image.getName() + ":" + ijRois.get(0).getName() + ":4";
        results.setLabel(label, 0);
        results.setValue("Volume", 0, 25.0);
        results.setValue("Volume Unit", 0, "µm^3");

        TableWrapper table = new TableWrapper(client, results, imageId, ijRois, "ROI");

        Object[][] data = table.getData();
        assertEquals(1, table.getRowCount());
        assertEquals(image.getId(), ((ImageData) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((ROIData) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(25.0d, (Double) data[3][0], 0.05);
        assertEquals("µm^3", data[4][0]);

        image.addTable(client, table);

        assertNotNull(table.getFileId());

        client.delete(table);

        for (ROIWrapper r : rois) {
            client.delete(r);
        }
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testCreateTableFromIJResults() throws Exception {
        long imageId = 1L;

        ImageWrapper image = client.getImage(imageId);

        List<Roi> ijRois = new ArrayList<>();

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        results.setLabel(image.getName(), 0);
        results.setValue("Volume", 0, 25.0);
        results.setValue("Volume Unit", 0, "µm^3");

        TableWrapper table = new TableWrapper(client, results, imageId, ijRois, "ROI");
        image.addTable(client, table);

        List<TableWrapper> tables = image.getTables(client);

        assertEquals(1, tables.size());
        assertEquals(1, tables.get(0).getRowCount());

        Object[][] data = tables.get(0).getData();
        assertEquals(image.getId(), ((ImageData) data[0][0]).getId());
        assertEquals(image.getName(), data[1][0]);
        assertEquals(25.0d, (Double) data[2][0], 0.05);
        assertEquals("µm^3", data[3][0]);

        client.delete(tables.get(0));

        tables = image.getTables(client);

        assertEquals(0, tables.size());
    }


    @Test
    public void testAddRowsFromIJResults() throws Exception {
        long imageId = 1L;

        ImageWrapper image = client.getImage(imageId);

        List<Roi> ijRois = new ArrayList<>();

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        results1.setLabel(image.getName(), 0);
        results1.setValue("Volume", 0, 25.0);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(image.getName(), 0);
        results2.setValue("Volume", 0, 50);
        results2.setValue("Volume Unit", 0, "m^3");

        TableWrapper table = new TableWrapper(client, results1, imageId, ijRois, "ROI");
        table.addRows(client, results2, imageId, ijRois, "ROI");

        image.addTable(client, table);

        List<TableWrapper> tables = image.getTables(client);

        assertEquals(1, tables.size());
        assertEquals(2, tables.get(0).getRowCount());

        Object[][] data = tables.get(0).getData();
        assertEquals(image.getId(), ((ImageData) data[0][0]).getId());
        assertEquals(image.getName(), data[1][0]);
        assertEquals(25.0d, (Double) data[2][0], 0.05);
        assertEquals("µm^3", data[3][0]);
        assertEquals(image.getId(), ((ImageData) data[0][1]).getId());
        assertEquals(image.getName(), data[1][1]);
        assertEquals(50, (Double) data[2][1], 0.05);
        assertEquals("m^3", data[3][1]);

        client.delete(tables.get(0));

        tables = image.getTables(client);

        assertEquals(0, tables.size());
    }


    @Test
    public void testAddRowsWithROIsFromIJResults() throws Exception {
        long imageId = 1L;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois);

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        results1.setLabel(image.getName(), 0);
        results1.setValue("ROI", 0, ijRois.get(0).getName());
        results1.setValue("Volume", 0, 25.0);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(image.getName(), 0);
        results2.setValue("ROI", 0, ijRois.get(0).getName());
        results2.setValue("Volume", 0, 50);
        results2.setValue("Volume Unit", 0, "m^3");

        TableWrapper table = new TableWrapper(client, results1, imageId, ijRois, "ROI");
        table.addRows(client, results2, imageId, ijRois, "ROI");

        Object[][] data = table.getData();
        assertEquals(2, table.getRowCount());
        assertEquals(image.getId(), ((ImageData) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((ROIData) data[1][0]).getId());
        assertEquals(image.getName(), data[2][0]);
        assertEquals(25.0d, (Double) data[3][0], 0.05);
        assertEquals("µm^3", data[4][0]);
        assertEquals(image.getId(), ((ImageData) data[0][1]).getId());
        assertEquals(rois.get(0).getId(), ((ROIData) data[1][1]).getId());
        assertEquals(image.getName(), data[2][1]);
        assertEquals(50, (Double) data[3][1], 0.05);
        assertEquals("m^3", data[4][1]);

        image.addTable(client, table);

        assertNotNull(table.getFileId());

        client.delete(table);

        for (ROIWrapper r : rois) {
            client.delete(r);
        }
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testCreateTableWithLocalROIFromIJResults1() throws Exception {
        long imageId = 1L;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois);
        Roi local = new Roi(5, 5, 10, 10);
        local.setName("local");
        ijRois.add(local);

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        results.setLabel(image.getName(), 0);
        results.setValue("ROI", 0, local.getName());
        results.setValue("Volume", 0, 25.0);
        results.setValue("Volume Unit", 0, "µm^3");
        results.incrementCounter();
        results.setLabel(image.getName(), 1);
        results.setValue("ROI", 1, ijRois.get(0).getName());
        results.setValue("Volume", 1, 50);
        results.setValue("Volume Unit", 1, "m^3");

        TableWrapper table = new TableWrapper(client, results, imageId, ijRois, "ROI");

        Object[][] data = table.getData();
        assertEquals(2, table.getRowCount());
        assertEquals(image.getId(), ((ImageData) data[0][0]).getId());
        assertEquals(image.getName(), data[1][0]);
        assertEquals(local.getName(), data[2][0]);
        assertEquals(25.0d, (Double) data[3][0], 0.05);
        assertEquals("µm^3", data[4][0]);
        assertEquals(image.getId(), ((ImageData) data[0][1]).getId());
        assertEquals(image.getName(), data[1][1]);
        assertEquals(ijRois.get(0).getName(), data[2][1]);
        assertEquals(50, (Double) data[3][1], 0.05);
        assertEquals("m^3", data[4][1]);

        image.addTable(client, table);

        assertNotNull(table.getFileId());

        client.delete(table);

        for (ROIWrapper r : rois) {
            client.delete(r);
        }
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testCreateTableWithLocalROIFromIJResults2() throws Exception {
        long imageId = 1L;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois);
        Roi local = new Roi(5, 5, 10, 10);
        local.setName("local");
        ijRois.add(local);

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        String label1 = image.getName() + ":" + local.getName() + ":4";
        results.setLabel(label1, 0);
        results.setValue("Volume", 0, 25.0);
        results.setValue("Volume Unit", 0, "µm^3");
        results.incrementCounter();
        String label2 = image.getName() + ":" + ijRois.get(0).getName() + ":10";
        results.setLabel(label2, 1);
        results.setValue("Volume", 1, 50);
        results.setValue("Volume Unit", 1, "m^3");

        TableWrapper table = new TableWrapper(client, results, imageId, ijRois, "ROI");

        Object[][] data = table.getData();
        assertEquals(2, table.getRowCount());
        assertEquals(image.getId(), ((ImageData) data[0][0]).getId());
        assertEquals(label1, data[1][0]);
        assertEquals(25.0d, (Double) data[2][0], 0.05);
        assertEquals("µm^3", data[3][0]);
        assertEquals(image.getId(), ((ImageData) data[0][1]).getId());
        assertEquals(label2, data[1][1]);
        assertEquals(50, (Double) data[2][1], 0.05);
        assertEquals("m^3", data[3][1]);

        image.addTable(client, table);

        assertNotNull(table.getFileId());

        client.delete(table);

        for (ROIWrapper r : rois) {
            client.delete(r);
        }
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testAddRowsFromIJResultsError() throws Exception {
        boolean error   = false;
        long    imageId = 1L;

        ImageWrapper image = client.getImage(imageId);

        List<Roi> ijRois = new ArrayList<>();

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        results1.setLabel(image.getName(), 0);
        results1.setValue("Volume", 0, 25.0);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(image.getName(), 0);
        results2.setValue("Volume", 0, 50);

        TableWrapper table = new TableWrapper(client, results1, imageId, ijRois, "ROI");
        try {
            table.addRows(client, results2, imageId, ijRois, "ROI");
        } catch (IllegalArgumentException e) {
            error = true;
        }

        assertTrue(error);
    }


    @Test
    public void testAddRowsFromIJResultsInverted() throws Exception {
        long imageId = 1L;

        ImageWrapper image = client.getImage(imageId);

        List<Roi> ijRois = new ArrayList<>();

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        results1.setLabel(image.getName(), 0);
        results1.setValue("Volume", 0, 25.0);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(image.getName(), 0);
        results2.setValue("Volume Unit", 0, "m^3");
        results2.setValue("Volume", 0, 50);

        TableWrapper table = new TableWrapper(client, results1, imageId, ijRois, "ROI");
        table.addRows(client, results2, imageId, ijRois, "ROI");

        image.addTable(client, table);

        List<TableWrapper> tables = image.getTables(client);

        assertEquals(1, tables.size());
        assertEquals(2, tables.get(0).getRowCount());

        Object[][] data = tables.get(0).getData();
        assertEquals(image.getId(), ((ImageData) data[0][0]).getId());
        assertEquals(image.getName(), data[1][0]);
        assertEquals(25.0d, (Double) data[2][0], 0.05);
        assertEquals("µm^3", data[3][0]);
        assertEquals(image.getId(), ((ImageData) data[0][1]).getId());
        assertEquals(image.getName(), data[1][1]);
        assertEquals(Double.NaN, (Double) data[2][1], 0.05);
        assertEquals("50", data[3][1]);

        client.delete(tables.get(0));

        tables = image.getTables(client);

        assertEquals(0, tables.size());
    }

}