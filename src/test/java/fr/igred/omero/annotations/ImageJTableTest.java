/*
 *  Copyright (C) 2020-2024 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero.annotations;


import fr.igred.omero.UserTest;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.Rectangle;
import fr.igred.omero.roi.RectangleWrapper;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static java.util.logging.Level.SEVERE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;


class ImageJTableTest extends UserTest {

    protected static final double VOLUME1  = 25.023579d;
    protected static final double VOLUME2  = 50.0d;
    protected static final String UNIT1    = "µm^3";
    protected static final String UNIT2    = "m^3";
    protected static final long   IMAGE_ID = IMAGE1.id;

    protected Image image = new ImageWrapper(new ImageData());


    private static List<ROI> createAndSaveROI(DataManager dm, Image image, String name)
    throws AccessException, ServiceException, ExecutionException {
        ROI roi = new ROIWrapper();
        roi.setImage(image);
        for (int i = 0; i < 4; i++) {
            Rectangle rectangle = new RectangleWrapper();
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            roi.addShape(rectangle);
        }
        if (name != null && !name.trim().isEmpty()) {
            roi.setName(name);
        }
        image.saveROIs(dm, roi);
        return image.getROIs(dm);
    }


    private static ResultsTable createOneRowResultsTable(String imageName, double volume, String unit) {
        ResultsTable results = new ResultsTable();
        addRowToResultsTable(results, imageName, volume, unit);
        return results;
    }


    private static void addRowToResultsTable(ResultsTable results, String imageName, double volume, String unit) {
        int i = results.size();
        results.incrementCounter();
        if (imageName != null && !imageName.trim().isEmpty()) {
            results.setLabel(imageName, i);
        }
        results.setValue("Volume", i, volume);
        if (unit != null && !unit.trim().isEmpty()) {
            results.setValue("Volume Unit", i, unit);
        }
    }


    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        boolean failed = false;
        try {
            image = client.getImage(IMAGE_ID);
        } catch (AccessException | RuntimeException | ExecutionException | ServiceException e) {
            failed = true;
            String msg = "%sCould not retrieve image.%s";
            logger.log(SEVERE, format(msg, ANSI_RED, ANSI_RESET), e);
        }
        String error = "Could not retrieve image with ID=%d.";
        assumeFalse(failed, format(error, IMAGE_ID));
    }


    @AfterEach
    @Override
    public void cleanUp() {
        if (client.isConnected()) {
            try {
                List<ROI> rois = image.getROIs(client);
                for (ROI r : rois) {
                    client.delete(r);
                }
                int nRois = image.getROIs(client).size();
                if (nRois != 0) {
                    String msg = "%sROIs were not properly deleted.%s";
                    logger.log(SEVERE, format(msg, ANSI_RED, ANSI_RESET));
                }
            } catch (AccessException | ServiceException | ExecutionException |
                     InterruptedException e) {
                String msg = "%sROIs were not properly deleted.%s";
                logger.log(SEVERE, format(msg, ANSI_RED, ANSI_RESET), e);
            }
        }
        super.cleanUp();
    }


    @Test
    void testCreateTableWithROIsFromIJResults1() throws Exception {
        List<ROI> rois   = createAndSaveROI(client, image, "ROI_1");
        List<Roi> ijRois = ROI.toImageJ(rois, null, false);

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, VOLUME1, UNIT1);
        results.setValue("Image", 0, label);
        results.setValue(ROI.IJ_PROPERTY, 0, "ROI_1");

        TableBuilder builder = new TableBuilder(client, results, IMAGE_ID, ijRois);
        Table        table   = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(1, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(VOLUME1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[4][0]);
        assertEquals(label, data[5][0]);
        assertNotNull(fileId);
    }


    @Test
    void testCreateTableWithROIsFromIJResults2() throws Exception {
        String    property = "Cell";
        List<ROI> rois     = createAndSaveROI(client, image, "");
        List<Roi> ijRois   = rois.get(0).toImageJ(property);

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, VOLUME1, UNIT1);
        results.setValue("Image", 0, label);
        results.setValue("Image_Name", 0, label);
        results.setValue(property, 0, rois.get(0).getId());

        TableBuilder builder = new TableBuilder(client, results, IMAGE_ID, ijRois, property);
        Table        table   = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(1, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(VOLUME1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[4][0]);
        assertEquals(label, data[5][0]);
        assertEquals(label, data[6][0]);
        assertNotNull(fileId);
    }


    @Test
    void testCreateTableWithROIsFromIJResults3() throws Exception {
        List<ROI> rois   = createAndSaveROI(client, image, "");
        List<Roi> ijRois = rois.get(0).toImageJ("");

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, VOLUME1, UNIT1);
        results.setValue(ROI.ijIDProperty(null), 0, rois.get(0).getId());

        TableBuilder builder = new TableBuilder(client, results, IMAGE_ID, ijRois);
        Table        table   = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(1, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(VOLUME1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[4][0]);
        assertNotNull(fileId);
    }


    @Test
    void testCreateTableWithROIsFromIJResults4() throws Exception {
        List<ROI> rois   = createAndSaveROI(client, image, "");
        List<Roi> ijRois = rois.get(0).toImageJ();

        String label = image.getName() + ":" + ijRois.get(0).getName() + ":4";

        ResultsTable results = createOneRowResultsTable("", VOLUME1, UNIT1);
        results.setValue("Image", 0, label);

        TableBuilder builder = new TableBuilder(client, results, IMAGE_ID, ijRois, ROI.IJ_PROPERTY);
        Table        table   = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(1, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(VOLUME1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[3][0]);
        assertEquals(label, data[4][0]);
        assertNotNull(fileId);
    }


    @Test
    void testCreateTableFromIJResults() throws Exception {
        List<Roi> ijRois = new ArrayList<>(0);

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, VOLUME1, UNIT1);
        TableBuilder builder = new TableBuilder(client, results, IMAGE_ID, ijRois, ROI.IJ_PROPERTY);
        Table        table   = builder.createTable();
        image.addTable(client, table);

        List<Table> tables = image.getTables(client);
        assertEquals(1, tables.size());
        assertEquals(1, tables.get(0).getNumberOfRows());

        Object[][] data = tables.get(0).getData();

        client.deleteTable(tables.get(0));
        List<Table> noTables = image.getTables(client);

        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(VOLUME1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[3][0]);
        assertEquals(0, noTables.size());
    }


    @Test
    void testAddRowsFromIJResults() throws Exception {
        List<Roi> ijRois = new ArrayList<>(0);

        String label = image.getName();

        ResultsTable results1 = createOneRowResultsTable(label, VOLUME1, UNIT1);
        ResultsTable results2 = createOneRowResultsTable(label, VOLUME2, UNIT2);

        TableBuilder builder = new TableBuilder(client, results1, IMAGE_ID, ijRois, ROI.IJ_PROPERTY);
        builder.addRows(client, results2, IMAGE_ID, ijRois, ROI.IJ_PROPERTY);
        Table table = builder.createTable();
        image.addTable(client, table);

        List<Table> tables = image.getTables(client);
        assertEquals(1, tables.size());
        assertEquals(2, tables.get(0).getNumberOfRows());

        Object[][] data = tables.get(0).getData();

        client.deleteTables(tables);
        List<Table> noTables = image.getTables(client);

        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(VOLUME1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[3][0]);
        assertEquals(IMAGE_ID, ((DataObject) data[0][1]).getId());
        assertEquals(label, data[1][1]);
        assertEquals(VOLUME2, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals(UNIT2, data[3][1]);
        assertEquals(0, noTables.size());
    }


    @Test
    void testAddRowsWithROIsFromIJResults() throws Exception {
        List<ROI> rois   = createAndSaveROI(client, image, "");
        List<Roi> ijRois = ROI.toImageJ(rois, "");

        String label = image.getName();

        ResultsTable results1 = createOneRowResultsTable(label, VOLUME1, UNIT1);
        results1.setValue(ROI.IJ_PROPERTY, 0, ijRois.get(0).getName());

        ResultsTable results2 = createOneRowResultsTable(label, VOLUME2, UNIT2);
        results2.setValue(ROI.IJ_PROPERTY, 0, ijRois.get(0).getName());

        TableBuilder builder = new TableBuilder(client, results1, IMAGE_ID, ijRois);
        builder.addRows(client, results2, IMAGE_ID, ijRois);
        Table table = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        long       roiId    = rois.get(0).getId();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(2, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(roiId, ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(VOLUME1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[4][0]);
        assertEquals(IMAGE_ID, ((DataObject) data[0][1]).getId());
        assertEquals(roiId, ((DataObject) data[1][1]).getId());
        assertEquals(label, data[2][1]);
        assertEquals(VOLUME2, (Double) data[3][1], Double.MIN_VALUE);
        assertEquals(UNIT2, data[4][1]);
        assertNotNull(fileId);
    }


    @Test
    void testCreateTableWithLocalROIFromIJResults1() throws Exception {
        List<ROI> rois   = createAndSaveROI(client, image, "");
        List<Roi> ijRois = ROI.toImageJ(rois);

        Roi local = new Roi(5, 5, 10, 10);
        local.setName("local");
        ijRois.add(local);

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, VOLUME1, UNIT1);
        addRowToResultsTable(results, label, VOLUME2, UNIT2);
        results.setValue(ROI.IJ_PROPERTY, 0, local.getName());
        results.setValue(ROI.IJ_PROPERTY, 1, ijRois.get(0).getName());

        TableBuilder builder = new TableBuilder(client, results, IMAGE_ID, ijRois, ROI.IJ_PROPERTY);
        Table        table   = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(2, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(VOLUME1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[3][0]);
        assertEquals(local.getName(), data[4][0]);
        assertEquals(IMAGE_ID, ((DataObject) data[0][1]).getId());
        assertEquals(label, data[1][1]);
        assertEquals(VOLUME2, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals(UNIT2, data[3][1]);
        assertEquals(ijRois.get(0).getName(), data[4][1]);
        assertNotNull(fileId);
    }


    @Test
    void testCreateTableWithLocalROIFromIJResults2() throws Exception {
        List<ROI> rois   = createAndSaveROI(client, image, "");
        List<Roi> ijRois = rois.get(0).toImageJ((String) null);

        Roi local = new Roi(5, 5, 10, 10);
        local.setName("local");
        ijRois.add(local);

        String label1 = image.getName() + ":" + local.getName() + ":4";
        String label2 = image.getName() + ":" + ijRois.get(0).getName() + ":10";

        ResultsTable results = createOneRowResultsTable(label1, VOLUME1, UNIT1);
        addRowToResultsTable(results, label2, VOLUME2, UNIT2);

        TableBuilder builder = new TableBuilder(client, results, IMAGE_ID, ijRois, ROI.IJ_PROPERTY);
        Table        table   = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(2, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(label1, data[1][0]);
        assertEquals(VOLUME1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[3][0]);
        assertEquals(IMAGE_ID, ((DataObject) data[0][1]).getId());
        assertEquals(label2, data[1][1]);
        assertEquals(VOLUME2, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals(UNIT2, data[3][1]);
        assertNotNull(fileId);
    }


    @Test
    void testCreateTableWithROINamesFromIJResults1() throws Exception {
        List<ROI> rois = new ArrayList<>(2);
        rois.add(new ROIWrapper());
        rois.add(new ROIWrapper());

        rois.get(0).setImage(image);
        rois.get(1).setImage(image);

        for (int i = 0; i < 4; i++) {
            Rectangle rectangle = new RectangleWrapper();
            rectangle.setText(String.valueOf(10 + i % 2));
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            if (i % 2 == 1) {
                rois.get(0).addShape(rectangle);
            } else {
                rois.get(1).addShape(rectangle);
            }
        }

        List<ROI> newROIs = image.saveROIs(client, rois);
        List<Roi> ijRois  = ROI.toImageJ(newROIs);

        String name1 = newROIs.get(0).getShapes().get(0).getText();
        String name2 = newROIs.get(1).getShapes().get(0).getText();

        String label1 = image.getName() + ":" + name1 + ":4";
        String label2 = image.getName() + ":" + name2 + ":10";

        ResultsTable res1 = createOneRowResultsTable(label1, VOLUME1, UNIT1);
        ResultsTable res2 = createOneRowResultsTable(label2, VOLUME2, UNIT2);

        TableBuilder builder = new TableBuilder(client, res1, IMAGE_ID, ijRois);
        builder.addRows(client, res2, IMAGE_ID, ijRois);
        Table table = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(2, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(newROIs.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(label1, data[2][0]);
        assertEquals(VOLUME1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[4][0]);
        assertEquals(IMAGE_ID, ((DataObject) data[0][1]).getId());
        assertEquals(newROIs.get(1).getId(), ((DataObject) data[1][1]).getId());
        assertEquals(label2, data[2][1]);
        assertEquals(VOLUME2, (Double) data[3][1], Double.MIN_VALUE);
        assertEquals(UNIT2, data[4][1]);
        assertNotNull(fileId);
    }


    @Test
    void testCreateTableWithROINamesFromIJResults2() throws Exception {
        ROI roi1 = new ROIWrapper();
        ROI roi2 = new ROIWrapper();

        roi1.setImage(image);
        roi1.setName("ROI");
        roi2.setImage(image);
        roi2.setName("ROI");

        final int max = 14;
        for (int i = 10; i < max; i++) {
            Rectangle rectangle = new RectangleWrapper();
            rectangle.setText(String.valueOf(10 + i % 2));
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            if (i % 2 == 1) {
                roi1.addShape(rectangle);
            } else {
                roi2.addShape(rectangle);
            }
        }

        image.saveROIs(client, roi1);
        image.saveROIs(client, roi2);

        List<ROI> rois   = image.getROIs(client);
        List<Roi> ijRois = ROI.toImageJ(rois);

        String label1 = rois.get(0).getShapes().get(0).getText();
        String label2 = rois.get(1).getShapes().get(0).getText();

        ResultsTable res1 = createOneRowResultsTable(label1, VOLUME1, UNIT1);
        ResultsTable res2 = createOneRowResultsTable(label2, VOLUME2, UNIT2);

        TableBuilder builder = new TableBuilder(client, res1, IMAGE_ID, ijRois);
        builder.addRows(client, res2, IMAGE_ID, ijRois);
        Table table = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(2, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(label1, data[2][0]);
        assertEquals(VOLUME1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[4][0]);
        assertEquals(IMAGE_ID, ((DataObject) data[0][1]).getId());
        assertEquals(rois.get(1).getId(), ((DataObject) data[1][1]).getId());
        assertEquals(label2, data[2][1]);
        assertEquals(VOLUME2, (Double) data[3][1], Double.MIN_VALUE);
        assertEquals(UNIT2, data[4][1]);
        assertNotNull(fileId);
    }


    @Test
    void testAddRowsFromIJResultsError() throws Exception {
        boolean error = false;

        List<Roi> ijRois = new ArrayList<>(0);

        String label = image.getName();

        ResultsTable results1 = createOneRowResultsTable(label, VOLUME1, UNIT1);
        ResultsTable results2 = createOneRowResultsTable(label, VOLUME2, null);

        TableBuilder table = new TableBuilder(client, results1, IMAGE_ID, ijRois);
        try {
            table.addRows(client, results2, IMAGE_ID, ijRois);
        } catch (IllegalArgumentException e) {
            error = true;
        }

        assertTrue(error);
    }


    @Test
    void testNumberFormatException() throws Exception {
        List<ROI> rois   = createAndSaveROI(client, image, "");
        List<Roi> ijRois = ROI.toImageJ(rois, null);
        ijRois.get(0).setProperty(ROI.IJ_PROPERTY, "tutu");
        ijRois.get(1).setProperty(ROI.IJ_PROPERTY, "tutu");
        ijRois.get(2).setProperty(ROI.IJ_PROPERTY, "tutu");
        ijRois.get(3).setProperty(ROI.ijIDProperty(ROI.IJ_PROPERTY), "tata");

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, VOLUME1, UNIT1);
        results.setValue("Image", 0, label);
        results.setValue(ROI.IJ_PROPERTY, 0, 1);

        TableBuilder builder = new TableBuilder(client, results, IMAGE_ID, ijRois);
        Table        table   = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(1, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(VOLUME1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[3][0]);
        assertEquals(label, data[4][0]);
        assertEquals(1.0, data[5][0]);
        assertNotNull(fileId);
    }


    @Test
    void testNumericName() throws Exception {
        List<ROI> rois   = createAndSaveROI(client, image, "1");
        List<Roi> ijRois = ROI.toImageJ(rois, null);

        String label = image.getName();

        ResultsTable results = createOneRowResultsTable(label, VOLUME1, UNIT1);
        results.setValue("Image", 0, label);
        results.setValue(ROI.IJ_PROPERTY, 0, 1.0d);

        TableBuilder builder = new TableBuilder(client, results, IMAGE_ID, ijRois);
        Table        table   = builder.createTable();
        image.addTable(client, table);

        long       rowCount = table.getNumberOfRows();
        Object[][] data     = table.getData();
        Long       fileId   = table.getOriginalFileId();

        client.deleteTable(table);

        assertEquals(1, rowCount);
        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(rois.get(0).getId(), ((DataObject) data[1][0]).getId());
        assertEquals(label, data[2][0]);
        assertEquals(VOLUME1, (Double) data[3][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[4][0]);
        assertEquals(label, data[5][0]);
        assertNotNull(fileId);
    }


    @Test
    void testAddRowsFromIJResultsInverted() throws Exception {
        List<Roi> ijRois = new ArrayList<>(0);

        String label = image.getName();

        ResultsTable results1 = createOneRowResultsTable(label, VOLUME1, UNIT1);

        ResultsTable results2 = new ResultsTable();
        results2.incrementCounter();
        results2.setLabel(label, 0);
        results2.setValue("Volume Unit", 0, UNIT2);
        results2.setValue("Volume", 0, VOLUME2);

        TableBuilder builder = new TableBuilder(client, results1, IMAGE_ID, ijRois,
                                                ROI.IJ_PROPERTY);
        builder.addRows(client, results2, IMAGE_ID, ijRois, ROI.IJ_PROPERTY);
        Table table = builder.createTable();
        image.addTable(client, table);
        Object[][] data = table.getData();

        List<Table> tables = image.getTables(client);
        assertEquals(1, tables.size());
        assertEquals(2, tables.get(0).getNumberOfRows());

        client.deleteTables(tables);
        List<Table> noTables = image.getTables(client);

        assertEquals(IMAGE_ID, ((DataObject) data[0][0]).getId());
        assertEquals(label, data[1][0]);
        assertEquals(VOLUME1, (Double) data[2][0], Double.MIN_VALUE);
        assertEquals(UNIT1, data[3][0]);
        assertEquals(IMAGE_ID, ((DataObject) data[0][1]).getId());
        assertEquals(label, data[1][1]);
        assertEquals(Double.NaN, (Double) data[2][1], Double.MIN_VALUE);
        assertEquals("50", data[3][1]);
        assertEquals(0, noTables.size());
    }


    @Test
    void testSaveTableAs() throws Exception {
        List<ROI> rois   = createAndSaveROI(client, image, "1");
        List<Roi> ijRois = ROI.toImageJ(rois, "");

        String label = image.getName();
        long   roiId = rois.get(0).getId();

        ResultsTable results1 = createOneRowResultsTable(label, VOLUME1, UNIT1);
        results1.setValue(ROI.IJ_PROPERTY, 0, ijRois.get(0).getName());
        results1.setValue("Removed", 0, "");

        ResultsTable results2 = createOneRowResultsTable(label, VOLUME2, UNIT2);
        results2.setValue(ROI.IJ_PROPERTY, 0, ijRois.get(0).getName());
        results2.setValue("Removed", 0, "");

        TableBuilder table = new TableBuilder(client, results1, IMAGE_ID, ijRois);
        table.addRows(client, results2, IMAGE_ID, ijRois);
        table.createTable();

        @SuppressWarnings("MagicCharacter")
        char delimiter = '\t';
        String filename = "file.csv";
        table.saveAs(filename, delimiter);

        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(4);
        String vol1 = formatter.format(VOLUME1);
        String vol2 = formatter.format(VOLUME2);

        String line  = "\"%d\"\t\"%d\"\t\"%s\"\t\"%s\"\t\"%s\"";
        String line1 = "\"Image\"\t\"ROI\"\t\"Label\"\t\"Volume\"\t\"Volume_Unit\"";
        String line2 = format(line, IMAGE_ID, roiId, label, vol1, UNIT1);
        String line3 = format(line, IMAGE_ID, roiId, label, vol2, UNIT2);

        List<String> expected = Arrays.asList(line1, line2, line3);

        File         file   = new File(filename);
        List<String> actual = Files.readAllLines(file.toPath());
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
        Files.deleteIfExists(file.toPath());
    }


    @Test
    void testAddRowsWithMismatch() throws Exception {
        List<ROI> rois = new ArrayList<>(2);
        rois.add(new ROIWrapper());
        rois.add(new ROIWrapper());

        rois.get(0).setImage(image);
        rois.get(1).setImage(image);

        for (int i = 0; i < 4; i++) {
            Rectangle rectangle = new RectangleWrapper();
            rectangle.setText(String.valueOf(10 + i % 2));
            rectangle.setCoordinates(i * 2, i * 2, 10, 10);
            rectangle.setZ(i);
            rectangle.setT(0);
            rectangle.setC(0);

            if (i % 2 == 1) {
                rois.get(0).addShape(rectangle);
            } else {
                rois.get(1).addShape(rectangle);
            }
        }

        List<ROI> newROIs = image.saveROIs(client, rois);
        List<Roi> ijRois  = ROI.toImageJ(newROIs);

        String name1 = newROIs.get(0).getShapes().get(0).getText();
        String name2 = newROIs.get(1).getShapes().get(0).getText();

        String label1 = image.getName() + ":" + name1 + ":4";
        String label2 = image.getName() + ":" + name2 + ":10";

        ResultsTable res1 = createOneRowResultsTable(label1, VOLUME1, UNIT1);
        ResultsTable res2 = createOneRowResultsTable(label2, VOLUME2, UNIT2);

        TableBuilder table = new TableBuilder(client, res1, IMAGE_ID, ijRois);

        assertThrows(IllegalArgumentException.class,
                     () -> table.addRows(client, res2, null, ijRois));
    }

}