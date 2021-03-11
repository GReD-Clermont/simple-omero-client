package fr.igred.omero;


import fr.igred.omero.metadata.TableContainer;
import fr.igred.omero.repository.DatasetContainer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import loci.common.DebugTools;
import omero.gateway.model.ImageData;

import java.util.List;

import static org.junit.Assert.assertNotEquals;


public class TableTest extends TestCase {

    /**
     * Create the test case for Client
     *
     * @param testName Name of the test case.
     */
    public TableTest(String testName) {
        super(testName);
    }


    /**
     * @return the suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(TableTest.class);
    }


    public void testCreateTable() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        TableContainer table = new TableContainer(2, "TableTest");

        assertEquals(2, table.getColumnCount());

        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);
        assertEquals("Image", table.getColumns()[0].getName());
        assertEquals("Name", table.getColumns()[1].getName());

        table.setRowCount(images.size());

        assertEquals(images.size(), table.getRowCount());

        for (ImageContainer image : images) {
            assertNotEquals(true, table.isComplete());
            table.addRow(image.getImage(), image.getName());
        }

        assertEquals(images.get(0).getImage(), table.getData(0, 0));
        assertEquals(images.get(1).getName(), table.getData(0, 1));

        dataset.addTable(root, table);

        List<TableContainer> tables = dataset.getTables(root);

        assertEquals(1, tables.size());

        root.deleteTable(tables.get(0));

        tables = dataset.getTables(root);

        assertEquals(0, tables.size());
    }


    public void testErrorTableFull() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        TableContainer table = new TableContainer(2, "TableTest");
        table.setName("TableTestNewName");

        assertEquals("TableTestNewName", table.getName());

        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        table.setRowCount(images.size() - 1);

        try {
            for (ImageContainer image : images) {
                table.addRow(image.getImage(), image.getName());
            }
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }


    public void testErrorTableColumn() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        TableContainer table = new TableContainer(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        try {
            table.setColumn(2, "Id", Long.class);
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }


    public void testErrorTableUninitialized() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        TableContainer table = new TableContainer(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        try {
            for (ImageContainer image : images) {
                table.addRow(image.getImage(), image.getName());
            }
            fail();
        } catch (IndexOutOfBoundsException e) {
            assertTrue(true);
        }
    }


    public void testErrorTableNotEnoughArgs() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        TableContainer table = new TableContainer(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        table.setRowCount(images.size());

        try {
            for (ImageContainer image : images) {
                table.addRow(image.getImage());
            }
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

}