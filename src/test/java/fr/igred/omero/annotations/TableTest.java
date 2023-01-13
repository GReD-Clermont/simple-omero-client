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
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TableTest extends UserTest {

    protected static final double volume1 = 25.023579d;
    protected static final double volume2 = 50.0d;

    protected static final String unit1 = "µm^3";
    protected static final String unit2 = "m^3";


    private static ROIWrapper createROIWrapper(ImageWrapper image) {
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
        return roi;
    }


    private static ResultsTable createOneRowResultsTable(String imageName, double volume, String unit) {
        ResultsTable results = new ResultsTable();
        addRowToResultsTable(results, imageName, volume, unit);
        return results;
    }


    private static void addRowToResultsTable(ResultsTable results, String imageName, double volume, String unit) {
        int i = results.size();
        results.incrementCounter();
        if (imageName != null && !imageName.trim().isEmpty())
            results.setLabel(imageName, i);
        results.setValue("Volume", i, volume);
        if (unit != null && !unit.trim().isEmpty())
            results.setValue("Volume Unit", i, unit);
    }


    @Test
    void testCreateTable() throws Exception {
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
    void testReplaceTable() throws Exception {
        DatasetWrapper dataset = client.getDataset(DATASET1.id);

        List<ImageWrapper> images = dataset.getImages(client);

        TableWrapper table1 = new TableWrapper(2, "TableTest");

        assertEquals(2, table1.getColumnCount());

        table1.setColumn(0, "Image", ImageData.class);
        table1.setColumn(1, "Name", String.class);
        assertEquals("Image", table1.getColumnName(0));
        assertEquals("Name", table1.getColumnName(1));
        assertSame(ImageData.class, table1.getColumnType(0));

        table1.setRowCount(images.size());

        assertEquals(images.size(), table1.getRowCount());

        for (ImageWrapper image : images) {
            assertNotEquals(true, table1.isComplete());
            table1.addRow(image.asImageData(), image.getName());
        }

        assertEquals(images.get(0).asImageData(), table1.getData(0, 0));
        assertEquals(images.get(1).getName(), table1.getData(0, 1));

        dataset.addTable(client, table1);
        long tableId1 = table1.getId();

        TableWrapper table2 = new TableWrapper(2, "TableTest 2");
        table2.setColumn(0, "Image", ImageData.class);
        table2.setColumn(1, "Name", String.class);
        table2.setRowCount(images.size());
        for (ImageWrapper image : images) {
            assertNotEquals(true, table2.isComplete());
            table2.addRow(image.asImageData(), image.getDescription());
        }
        dataset.addTable(client, table2);
        long tableId2 = table2.getId();

        TableWrapper table3 = new TableWrapper(2, "TableTest");
        table3.setColumn(0, "Image", ImageData.class);
        table3.setColumn(1, "Name", String.class);
        table3.setRowCount(images.size());
        for (ImageWrapper image : images) {
            assertNotEquals(true, table3.isComplete());
            table3.addRow(image.asImageData(), "Test name");
        }
        dataset.addAndReplaceTable(client, table3);
        long tableId3 = table3.getId();

        assertNotEquals(tableId1, tableId3);
        assertNotEquals(tableId2, tableId3);

        List<TableWrapper> tables = dataset.getTables(client);
        for (TableWrapper table : tables) {
            client.delete(table);
        }
        List<TableWrapper> noTables = dataset.getTables(client);

        assertEquals(2, tables.size());
        assertEquals(0, noTables.size());
    }


    @Test
    void testErrorTableFull() throws Exception {
        DatasetWrapper dataset = client.getDataset(DATASET1.id);

        List<ImageWrapper> images = dataset.getImages(client);

        TableWrapper table = new TableWrapper(2, "TableTest");
        table.setName("TableTestNewName");

        assertEquals("TableTestNewName", table.getName());

        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        table.setRowCount(images.size() - 1);

        assertThrows(IndexOutOfBoundsException.class,
                     () -> images.forEach(img -> table.addRow(img.asImageData(), img.getName())));
    }


    @Test
    void testErrorTableColumn() {
        TableWrapper table = new TableWrapper(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);
        assertThrows(IndexOutOfBoundsException.class, () -> table.setColumn(2, "Id", Long.class));
    }


    @Test
    void testErrorTableUninitialized() throws Exception {
        DatasetWrapper dataset = client.getDataset(DATASET1.id);

        List<ImageWrapper> images = dataset.getImages(client);

        TableWrapper table = new TableWrapper(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);
        assertThrows(IndexOutOfBoundsException.class,
                     () -> images.forEach(img -> table.addRow(img.asImageData(), img.getName())));
    }


    @Test
    void testErrorTableNotEnoughArgs() throws Exception {
        DatasetWrapper dataset = client.getDataset(DATASET1.id);

        List<ImageWrapper> images = dataset.getImages(client);

        TableWrapper table = new TableWrapper(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);
        table.setRowCount(images.size());
        assertThrows(IllegalArgumentException.class,
                     () -> images.forEach(img -> table.addRow(img.asImageData())));
    }


    @Test
    void testCreateTableWithROIsFromIJResults1() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        roi.setName("ROI_1");
        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois, null);

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, volume1, unit1);
        results.setValue("Image", 0, label);
        results.setValue(ROIWrapper.IJ_PROPERTY, 0, "ROI_1");

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(1, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(unit1, data[4][0]);
        assertEquals(label, data[5][0]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testCreateTableWithROIsFromIJResults2() throws Exception {
        long   imageId  = IMAGE1.id;
        String property = "Cell";

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = rois.get(0).toImageJ(property);

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, volume1, unit1);
        results.setValue("Image", 0, label);
        results.setValue("Image_Name", 0, label);
        results.setValue(property, 0, rois.get(0).getId());

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois, property);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getFileId();
        //Object[][] expected = {{image.asImageData()}, {rois.get(0))}, {label}, {volume1}, {unit1}, {label}, {label}};

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(1, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(unit1, data[4][0]);
        assertEquals(label, data[5][0]);
        assertEquals(label, data[6][0]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testCreateTableWithROIsFromIJResults3() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = rois.get(0).toImageJ("");

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, volume1, unit1);
        results.setValue(ROIWrapper.ijIDProperty(null), 0, rois.get(0).getId());

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(1, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(unit1, data[4][0]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testCreateTableWithROIsFromIJResults4() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = rois.get(0).toImageJ();

        String label = image.getName() + ":" + ijRois.get(0).getName() + ":4";

        ResultsTable results = createOneRowResultsTable("", volume1, unit1);
        results.setValue("Image", 0, label);

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(1, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(unit1, data[3][0]);
        assertEquals(label, data[4][0]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testCreateTableFromIJResults() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        List<Roi> ijRois = new ArrayList<>(0);

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, volume1, unit1);
        TableWrapper table   = new TableWrapper(client, results, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        image.addTable(client, table);

        List<TableWrapper> tables = image.getTables(client);
        assertEquals(1, tables.size());
        assertEquals(1, tables.get(0).getRowCount());

        Object[][] data = tables.get(0).getData();

        client.delete(tables.get(0));
        List<TableWrapper> noTables = image.getTables(client);

        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(unit1, data[3][0]);
        assertEquals(0, noTables.size());
    }


    @Test
    void testAddRowsFromIJResults() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        List<Roi> ijRois = new ArrayList<>(0);

        String label = image.getName();

        ResultsTable results1 = createOneRowResultsTable(label, volume1, unit1);
        ResultsTable results2 = createOneRowResultsTable(label, volume2, unit2);

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        table.addRows(client, results2, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        image.addTable(client, table);

        List<TableWrapper> tables = image.getTables(client);
        assertEquals(1, tables.size());
        assertEquals(2, tables.get(0).getRowCount());

        Object[][] data = tables.get(0).getData();

        client.delete(tables.get(0));
        List<TableWrapper> noTables = image.getTables(client);

        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(unit1, data[3][0]);
        assertEquals(imageId, ((DataObject) data[0][1]).getId());
        assertEquals(label, data[1][1]);
        assertEquals(volume2, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals(unit2, data[3][1]);
        assertEquals(0, noTables.size());
    }


    @Test
    void testAddRowsWithROIsFromIJResults() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois, "");

        String label = image.getName();

        ResultsTable results1 = createOneRowResultsTable(label, volume1, unit1);
        results1.setValue(ROIWrapper.IJ_PROPERTY, 0, ijRois.get(0).getName());

        ResultsTable results2 = createOneRowResultsTable(label, volume2, unit2);
        results2.setValue(ROIWrapper.IJ_PROPERTY, 0, ijRois.get(0).getName());

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        table.addRows(client, results2, IMAGE1.id, ijRois);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(2, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(unit1, data[4][0]);
        assertEquals(imageId, ((DataObject) data[0][1]).getId());
        assertEquals(roiId, ((DataObject) data[1][1]).getId());
        assertEquals(label, data[2][1]);
        assertEquals(volume2, (Double) data[3][1], Double.MIN_VALUE);
        assertEquals(unit2, data[4][1]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testCreateTableWithLocalROIFromIJResults1() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois);
        Roi              local  = new Roi(5, 5, 10, 10);
        local.setName("local");
        ijRois.add(local);

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, volume1, unit1);
        addRowToResultsTable(results, label, volume2, unit2);
        results.setValue(ROIWrapper.IJ_PROPERTY, 0, local.getName());
        results.setValue(ROIWrapper.IJ_PROPERTY, 1, ijRois.get(0).getName());

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(2, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(unit1, data[3][0]);
        assertEquals(local.getName(), data[4][0]);
        assertEquals(imageId, ((DataObject) data[0][1]).getId());
        assertEquals(label, data[1][1]);
        assertEquals(volume2, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals(unit2, data[3][1]);
        assertEquals(ijRois.get(0).getName(), data[4][1]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testCreateTableWithLocalROIFromIJResults2() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = rois.get(0).toImageJ((String) null);
        Roi              local  = new Roi(5, 5, 10, 10);
        local.setName("local");
        ijRois.add(local);

        String label1 = image.getName() + ":" + local.getName() + ":4";
        String label2 = image.getName() + ":" + ijRois.get(0).getName() + ":10";

        ResultsTable results = createOneRowResultsTable(label1, volume1, unit1);
        addRowToResultsTable(results, label2, volume2, unit2);

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(2, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(label1, data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(unit1, data[3][0]);
        assertEquals(imageId, ((DataObject) data[0][1]).getId());
        assertEquals(label2, data[1][1]);
        assertEquals(volume2, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals(unit2, data[3][1]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testCreateTableWithROINamesFromIJResults1() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

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

        String label1 = image.getName() + ":" + rois.get(0).getShapes().get(0).getText() + ":4";
        String label2 = image.getName() + ":" + rois.get(1).getShapes().get(0).getText() + ":10";

        ResultsTable results1 = createOneRowResultsTable(label1, volume1, unit1);
        ResultsTable results2 = createOneRowResultsTable(label2, volume2, unit2);

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        table.addRows(client, results2, IMAGE1.id, ijRois);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(2, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(label1, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(unit1, data[4][0]);
        assertEquals(imageId, ((DataObject) data[0][1]).getId());
        assertEquals(rois.get(1).getId(), ((DataObject) data[1][1]).getId());
        assertEquals(label2, data[2][1]);
        assertEquals(volume2, (Double) data[3][1], Double.MIN_VALUE);
        assertEquals(unit2, data[4][1]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testCreateTableWithROINamesFromIJResults2() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

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

        String label1 = rois.get(0).getShapes().get(0).getText();
        String label2 = rois.get(1).getShapes().get(0).getText();

        ResultsTable results1 = createOneRowResultsTable(label1, volume1, unit1);
        ResultsTable results2 = createOneRowResultsTable(label2, volume2, unit2);

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        table.addRows(client, results2, IMAGE1.id, ijRois);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(2, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(label1, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(unit1, data[4][0]);
        assertEquals(imageId, ((DataObject) data[0][1]).getId());
        assertEquals(rois.get(1).getId(), ((DataObject) data[1][1]).getId());
        assertEquals(label2, data[2][1]);
        assertEquals(volume2, (Double) data[3][1], Double.MIN_VALUE);
        assertEquals(unit2, data[4][1]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testAddRowsFromIJResultsError() throws Exception {
        boolean error   = false;
        long    imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        List<Roi> ijRois = new ArrayList<>(0);

        String label = image.getName();

        ResultsTable results1 = createOneRowResultsTable(label, volume1, unit1);
        ResultsTable results2 = createOneRowResultsTable(label, volume2, null);

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        try {
            table.addRows(client, results2, IMAGE1.id, ijRois);
        } catch (IllegalArgumentException e) {
            error = true;
        }

        assertTrue(error);
    }


    @Test
    void testNumberFormatException() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois, null);
        ijRois.get(0).setProperty(ROIWrapper.IJ_PROPERTY, "tutu");
        ijRois.get(1).setProperty(ROIWrapper.IJ_PROPERTY, "tutu");
        ijRois.get(2).setProperty(ROIWrapper.IJ_PROPERTY, "tutu");
        ijRois.get(3).setProperty(ROIWrapper.ijIDProperty(ROIWrapper.IJ_PROPERTY), "tata");

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, volume1, unit1);
        results.setValue("Image", 0, label);
        results.setValue(ROIWrapper.IJ_PROPERTY, 0, 1);

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(1, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(unit1, data[3][0]);
        assertEquals(label, data[4][0]);
        assertEquals(1.0, data[5][0]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testNumericName() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        image.saveROI(client, roi);

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois, null);
        ijRois.get(0).setProperty(ROIWrapper.IJ_PROPERTY, "1");
        ijRois.get(1).setProperty(ROIWrapper.IJ_PROPERTY, "1");
        ijRois.get(2).setProperty(ROIWrapper.IJ_PROPERTY, "1");

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, volume1, unit1);
        results.setValue("Image", 0, label);
        results.setValue(ROIWrapper.IJ_PROPERTY, 0, 1.0d);

        TableWrapper table = new TableWrapper(client, results, IMAGE1.id, ijRois);
        image.addTable(client, table);

        int        rowCount = table.getRowCount();
        Object[][] data     = table.getData();
        Long       fileId   = table.getFileId();

        client.delete(table);
        for (ROIWrapper r : rois) {
            client.delete(r);
        }

        assertEquals(1, rowCount);
        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(volume1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(unit1, data[4][0]);
        assertEquals(label, data[5][0]);
        assertNotNull(fileId);
        assertEquals(0, image.getROIs(client).size());
    }


    @Test
    void testAddRowsFromIJResultsInverted() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        List<Roi> ijRois = new ArrayList<>(0);

        String label = image.getName();

        ResultsTable results1 = createOneRowResultsTable(label, volume1, unit1);

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(label, 0);
        results2.setValue("Volume Unit", 0, unit2);
        results2.setValue("Volume", 0, volume2);

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        table.addRows(client, results2, IMAGE1.id, ijRois, ROIWrapper.IJ_PROPERTY);
        image.addTable(client, table);
        Object[][] data = table.getData();

        List<TableWrapper> tables = image.getTables(client);
        assertEquals(1, tables.size());
        assertEquals(2, tables.get(0).getRowCount());

        client.delete(tables.get(0));
        List<TableWrapper> noTables = image.getTables(client);

        assertEquals(imageId, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(volume1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(unit1, data[3][0]);
        assertEquals(imageId, ((DataObject) data[0][1]).getId());
        assertEquals(label, data[1][1]);
        assertEquals(Double.NaN, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals("50", data[3][1]);
        assertEquals(0, noTables.size());
    }


    @Test
    void testSaveTableAs() throws Exception {
        long imageId = IMAGE1.id;

        ImageWrapper image = client.getImage(imageId);

        ROIWrapper roi = createROIWrapper(image);
        roi.setName("1");
        image.saveROI(client, roi);
        long roiId = roi.getId();

        List<ROIWrapper> rois   = image.getROIs(client);
        List<Roi>        ijRois = ROIWrapper.toImageJ(rois, "");

        String label = image.getName();

        ResultsTable results1 = createOneRowResultsTable(label, volume1, unit1);
        results1.setValue(ROIWrapper.IJ_PROPERTY, 0, ijRois.get(0).getName());

        ResultsTable results2 = createOneRowResultsTable(label, volume2, unit2);
        results2.setValue(ROIWrapper.IJ_PROPERTY, 0, ijRois.get(0).getName());

        TableWrapper table = new TableWrapper(client, results1, IMAGE1.id, ijRois);
        table.addRows(client, results2, IMAGE1.id, ijRois);

        @SuppressWarnings("MagicCharacter")
        char delimiter = '\t';
        String filename = "file.csv";
        table.saveAs(filename, delimiter);

        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(4);
        String vol1 = formatter.format(volume1);
        String vol2 = formatter.format(volume2);

        String line1 = "\"Image\"\t\"ROI\"\t\"Label\"\t\"Volume\"\t\"Volume_Unit\"";
        String line2 = String.format("\"%d\"\t\"%d\"\t\"%s\"\t\"%s\"\t\"%s\"", imageId, roiId, label, vol1, unit1);
        String line3 = String.format("\"%d\"\t\"%d\"\t\"%s\"\t\"%s\"\t\"%s\"", imageId, roiId, label, vol2, unit2);

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