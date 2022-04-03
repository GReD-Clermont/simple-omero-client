/*
 *  Copyright (C) 2020-2022 GReD
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
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


public class TableTest extends UserTest {

    protected static final double volume1 = 25.0d;
    protected static final double volume2 = 50.0d;


    @Test
    public void testCreateTable() throws Exception {
        DatasetWrapper dataset = client.getDataset(DATASET1.id);

        List<ImageWrapper> images = dataset.getImages(client);

        TableWrapper table = new TableWrapper(2, "TableTest");

        assertEquals(2, table.getColumnCount());

        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);
        assertEquals("Image", table.getColumnName(0));
        assertEquals("Name", table.getColumnName(1));
        assertSame(ImageData.class, table.getColumnType(0));

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
        client.delete(tables.get(0));
        List<TableWrapper> noTables = dataset.getTables(client);

        assertEquals(1, tables.size());
        assertEquals(0, noTables.size());
    }


    @Test
    public void testErrorTableFull() throws Exception {
        boolean        exception = false;
        DatasetWrapper dataset   = client.getDataset(DATASET1.id);

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
        boolean exception = false;

        TableWrapper table = new TableWrapper(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        try {
            table.setColumn(2, "Id", Long.class);
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testErrorTableUninitialized() throws Exception {
        boolean exception = false;

        DatasetWrapper dataset = client.getDataset(DATASET1.id);

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

        DatasetWrapper dataset = client.getDataset(DATASET1.id);

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
        ImageWrapper image = client.getImage(IMAGE1.id);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois, null);

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        results.setLabel(image.getName(), 0);
        results.setValue("Image", 0, image.getName());
        results.setValue(ROIWrapper.IJ_PROPERTY, 0, 1);
        results.setValue("Volume", 0, volume1);
        results.setValue("Volume Unit", 0, "µm^3");

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois);

        Object[][] data = table.getData();
        assertEquals(1, table.getRowCount());
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(image.getName(), data[2][0]);
        assertEquals(image.getName(), data[3][0]);
        assertEquals(volume1, (Double) data[4][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[5][0]);

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
        String property = "Cell";

        ImageWrapper image = client.getImage(IMAGE1.id);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = rois.get(0).toImageJ(property);

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        results.setLabel(image.getName(), 0);
        results.setValue("Image", 0, image.getName());
        results.setValue("Image_Name", 0, image.getName());
        results.setValue(property, 0, rois.get(0).getId());
        results.setValue("Volume", 0, volume1);
        results.setValue("Volume Unit", 0, "µm^3");

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois, property);

        Object[][] data = table.getData();
        assertEquals(1, table.getRowCount());
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(image.getName(), data[2][0]);
        assertEquals(image.getName(), data[3][0]);
        assertEquals(image.getName(), data[4][0]);
        assertEquals(volume1, (Double) data[5][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[6][0]);

        image.addTable(client, table);

        assertNotNull(table.getFileId());

        client.delete(table);

        for (ROIWrapper r : rois) {
            client.delete(r);
        }
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    public void testCreateTableWithROIsFromIJResults3() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(i);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = rois.get(0).toImageJ("");

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        results.setLabel(image.getName(), 0);
        results.setValue(ROIWrapper.ijIDProperty(null), 0, rois.get(0).getId());
        results.setValue("Volume", 0, volume1);
        results.setValue("Volume Unit", 0, "µm^3");

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois);

        Object[][] data = table.getData();
        assertEquals(1, table.getRowCount());
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(image.getName(), data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
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
    public void testCreateTableWithROIsFromIJResults4() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(i);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = rois.get(0).toImageJ();

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        String label = image.getName() + ":" + ijRois.get(0).getName() + ":4";
        results.setValue("Image", 0, label);
        results.setValue("Volume", 0, volume1);
        results.setValue("Volume Unit", 0, "µm^3");

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);

        Object[][] data = table.getData();
        assertEquals(1, table.getRowCount());
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
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
        ImageWrapper image = client.getImage(IMAGE1.id);

        List<Roi> ijRois = new ArrayList<>(0);

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        results.setLabel(image.getName(), 0);
        results.setValue("Volume", 0, volume1);
        results.setValue("Volume Unit", 0, "µm^3");

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        image.addTable(client, table);

        List<TableWrapper> tables = image.getTables(client);

        assertEquals(1, tables.size());
        assertEquals(1, tables.get(0).getRowCount());

        Object[][] data = tables.get(0).getData();
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(image.getName(), data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[3][0]);

        client.delete(tables.get(0));

        List<TableWrapper> noTables = image.getTables(client);

        assertEquals(0, noTables.size());
    }


    @Test
    public void testAddRowsFromIJResults() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        List<Roi> ijRois = new ArrayList<>(0);

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        results1.setLabel(image.getName(), 0);
        results1.setValue("Volume", 0, volume1);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(image.getName(), 0);
        results2.setValue("Volume", 0, volume2);
        results2.setValue("Volume Unit", 0, "m^3");

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        table.addRows(client, results2, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);

        image.addTable(client, table);

        List<TableWrapper> tables = image.getTables(client);

        assertEquals(1, tables.size());
        assertEquals(2, tables.get(0).getRowCount());

        Object[][] data = tables.get(0).getData();
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(image.getName(), data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[3][0]);
        assertEquals(image.getId(), ((DataObject) data[0][1]).getId());
        assertEquals(image.getName(), data[1][1]);
        assertEquals(volume2, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals("m^3", data[3][1]);

        client.delete(tables.get(0));

        List<TableWrapper> noTables = image.getTables(client);

        assertEquals(0, noTables.size());
    }


    @Test
    public void testAddRowsWithROIsFromIJResults() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i % 2);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois, "");

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        results1.setLabel(image.getName(), 0);
        results1.setValue(ROIWrapper.IJ_PROPERTY, 0, ijRois.get(0).getName());
        results1.setValue("Volume", 0, volume1);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(image.getName(), 0);
        results2.setValue(ROIWrapper.IJ_PROPERTY, 0, ijRois.get(0).getName());
        results2.setValue("Volume", 0, volume2);
        results2.setValue("Volume Unit", 0, "m^3");

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        table.addRows(client, results2, IMAGE1.id, ijRois);

        Object[][] data = table.getData();
        assertEquals(2, table.getRowCount());
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(image.getName(), data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[4][0]);
        assertEquals(image.getId(), ((DataObject) data[0][1]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][1]).getId());
        assertEquals(image.getName(), data[2][1]);
        assertEquals(volume2, (Double) data[3][1], Double.MIN_VALUE);
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
        ImageWrapper image = client.getImage(IMAGE1.id);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois);
        Roi              local  = new Roi(5, 5, 10, 10);
        local.setName("local");
        ijRois.add(local);

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        results.setLabel(image.getName(), 0);
        results.setValue(ROIWrapper.IJ_PROPERTY, 0, local.getName());
        results.setValue("Volume", 0, volume1);
        results.setValue("Volume Unit", 0, "µm^3");
        results.incrementCounter();
        results.setLabel(image.getName(), 1);
        results.setValue(ROIWrapper.IJ_PROPERTY, 1, ijRois.get(0).getName());
        results.setValue("Volume", 1, volume2);
        results.setValue("Volume Unit", 1, "m^3");

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);

        Object[][] data = table.getData();
        assertEquals(2, table.getRowCount());
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(image.getName(), data[1][0]);
        assertEquals(local.getName(), data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[4][0]);
        assertEquals(image.getId(), ((DataObject) data[0][1]).getId());
        assertEquals(image.getName(), data[1][1]);
        assertEquals(ijRois.get(0).getName(), data[2][1]);
        assertEquals(volume2, (Double) data[3][1], Double.MIN_VALUE);
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
        ImageWrapper image = client.getImage(IMAGE1.id);

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
        List<Roi>        ijRois = rois.get(0).toImageJ((String) null);
        Roi              local  = new Roi(5, 5, 10, 10);
        local.setName("local");
        ijRois.add(local);

        ResultsTable results = new ResultsTable();
        results.incrementCounter();
        String label1 = image.getName() + ":" + local.getName() + ":4";
        results.setLabel(label1, 0);
        results.setValue("Volume", 0, volume1);
        results.setValue("Volume Unit", 0, "µm^3");
        results.incrementCounter();
        String label2 = image.getName() + ":" + ijRois.get(0).getName() + ":10";
        results.setLabel(label2, 1);
        results.setValue("Volume", 1, volume2);
        results.setValue("Volume Unit", 1, "m^3");

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);

        Object[][] data = table.getData();
        assertEquals(2, table.getRowCount());
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(label1, data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[3][0]);
        assertEquals(image.getId(), ((DataObject) data[0][1]).getId());
        assertEquals(label2, data[1][1]);
        assertEquals(volume2, (Double) data[2][1], Double.MIN_VALUE);
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
    public void testCreateTableWithROINamesFromIJResults1() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        ROIWrapper roi1 = new ROIWrapper();
        ROIWrapper roi2 = new ROIWrapper();

        roi1.setImage(image);
        roi2.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setText(String.valueOf(10 + i % 2));
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            if (i % 2 == 1) roi1.addShape(rectangle);
            else roi2.addShape(rectangle);
        }

        image.saveROI(client, roi1);
        image.saveROI(client, roi2);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois);

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        String label1 = image.getName() + ":" + rois.get(0).getShapes().get(0).getText() + ":4";
        results1.setLabel(label1, 0);
        results1.setValue("Volume", 0, volume1);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        String label2 = image.getName() + ":" + rois.get(1).getShapes().get(0).getText() + ":10";
        results2.setLabel(label2, 0);
        results2.setValue("Volume", 0, volume2);
        results2.setValue("Volume Unit", 0, "m^3");

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        table.addRows(client, results2, IMAGE1.id, ijRois);

        Object[][] data = table.getData();
        assertEquals(2, table.getRowCount());
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(label1, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[4][0]);
        assertEquals(image.getId(), ((DataObject) data[0][1]).getId());
        assertEquals(rois.get(1).getId(), ((DataObject) data[1][1]).getId());
        assertEquals(label2, data[2][1]);
        assertEquals(volume2, (Double) data[3][1], Double.MIN_VALUE);
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
    public void testCreateTableWithROINamesFromIJResults2() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        ROIWrapper roi1 = new ROIWrapper();
        ROIWrapper roi2 = new ROIWrapper();

        roi1.setImage(image);
        roi2.setImage(image);

        final int max = 14;
        for (int i = 10; i < max; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setText(String.valueOf(10 + i % 2));
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            if (i % 2 == 1) roi1.addShape(rectangle);
            else roi2.addShape(rectangle);
        }

        image.saveROI(client, roi1);
        image.saveROI(client, roi2);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois);

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        String label1 = rois.get(0).getShapes().get(0).getText();
        results1.setLabel(label1, 0);
        results1.setValue("Volume", 0, volume1);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        String label2 = rois.get(1).getShapes().get(0).getText();
        results2.setLabel(label2, 0);
        results2.setValue("Volume", 0, volume2);
        results2.setValue("Volume Unit", 0, "m^3");

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        table.addRows(client, results2, IMAGE1.id, ijRois);

        Object[][] data = table.getData();
        assertEquals(2, table.getRowCount());
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(label1, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[4][0]);
        assertEquals(image.getId(), ((DataObject) data[0][1]).getId());
        assertEquals(rois.get(1).getId(), ((DataObject) data[1][1]).getId());
        assertEquals(label2, data[2][1]);
        assertEquals(volume2, (Double) data[3][1], Double.MIN_VALUE);
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
    public void testAddRowsFromIJResultsError() throws Exception {
        boolean error = false;

        ImageWrapper image = client.getImage(IMAGE1.id);

        List<Roi> ijRois = new ArrayList<>(0);

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        results1.setLabel(image.getName(), 0);
        results1.setValue("Volume", 0, volume1);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(image.getName(), 0);
        results2.setValue("Volume", 0, volume2);

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        try {
            table.addRows(client, results2, IMAGE1.id, ijRois);
        } catch (IllegalArgumentException e) {
            error = true;
        }

        assertTrue(error);
    }


    @Test
    public void testAddRowsFromIJResultsInverted() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        List<Roi> ijRois = new ArrayList<>(0);

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        results1.setLabel(image.getName(), 0);
        results1.setValue("Volume", 0, volume1);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(image.getName(), 0);
        results2.setValue("Volume Unit", 0, "m^3");
        results2.setValue("Volume", 0, volume2);

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        table.addRows(client, results2, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);

        image.addTable(client, table);

        List<TableWrapper> tables = image.getTables(client);

        assertEquals(1, tables.size());
        assertEquals(2, tables.get(0).getRowCount());

        Object[][] data = tables.get(0).getData();
        assertEquals(image.getId(), ((DataObject) data[0][0]).getId());
        assertEquals(image.getName(), data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals("µm^3", data[3][0]);
        assertEquals(image.getId(), ((DataObject) data[0][1]).getId());
        assertEquals(image.getName(), data[1][1]);
        assertEquals(Double.NaN, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals("50", data[3][1]);

        client.delete(tables.get(0));

        List<TableWrapper> noTables = image.getTables(client);

        assertEquals(0, noTables.size());
    }


    @Test
    public void testSaveTableAs() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        ROIWrapper roi = new ROIWrapper();

        roi.setImage(image);

        for (int i = 0; i < 4; i++) {
            RectangleWrapper rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i % 2);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }

        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois, "");

        ResultsTable results1 = new ResultsTable();
        results1.incrementCounter();
        results1.setLabel(image.getName(), 0);
        results1.setValue(ROIWrapper.IJ_PROPERTY, 0, ijRois.get(0).getName());
        results1.setValue("Volume", 0, volume1);
        results1.setValue("Volume Unit", 0, "µm^3");

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(image.getName(), 0);
        results2.setValue(ROIWrapper.IJ_PROPERTY, 0, ijRois.get(0).getName());
        results2.setValue("Volume", 0, volume2);
        results2.setValue("Volume Unit", 0, "m^3");

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        table.addRows(client, results2, IMAGE1.id, ijRois);

        String filename = "file.csv";
        table.saveAs(filename, ',');

        String line1 = "\"Image\",\"ROI\",\"Label\",\"Volume\",\"Volume_Unit\"";
        String line2 = String.format("\"1\",\"%d\",\"image1.fake\",\"%.1f\",\"µm^3\"", roi.getId(), volume1);
        String line3 = String.format("\"1\",\"%d\",\"image1.fake\",\"%.1f\",\"m^3\"", roi.getId(), volume2);

        List<String> expected = Arrays.asList(line1, line2, line3);

        File         file   = new File(filename);
        List<String> actual = Files.readAllLines(file.toPath());
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
        Files.deleteIfExists(file.toPath());

        for (ROIWrapper r : rois) {
            client.delete(r);
        }
        assertEquals(0, image.getROIs(client).size());
    }

}