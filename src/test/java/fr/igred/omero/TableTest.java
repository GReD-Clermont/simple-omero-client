package fr.igred.omero;


import fr.igred.omero.metadata.TableContainer;
import fr.igred.omero.repository.DatasetContainer;
import omero.gateway.model.ImageData;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


public class TableTest extends UserTest {


    @Test
    public void testCreateTable() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(client);

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

        dataset.addTable(client, table);

        List<TableContainer> tables = dataset.getTables(client);

        assertEquals(1, tables.size());

        client.deleteTable(tables.get(0));

        tables = dataset.getTables(client);

        assertEquals(0, tables.size());
    }


    @Test
    public void testErrorTableFull() throws Exception {
        boolean          exception = false;
        DatasetContainer dataset   = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(client);

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
        } catch (IndexOutOfBoundsException e) {
            exception = true;
        }
        assertTrue(exception);
    }


    @Test
    public void testErrorTableColumn() {
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


    @Test
    public void testErrorTableUninitialized() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(client);

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


    @Test
    public void testErrorTableNotEnoughArgs() throws Exception {
        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(client);

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