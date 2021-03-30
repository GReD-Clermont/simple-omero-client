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
import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ImageContainer;
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
        boolean exception = false;

        DatasetContainer dataset = client.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(client);

        TableContainer table = new TableContainer(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

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
    public void testErrorTableNotEnoughArgs() throws Exception {
        boolean exception = false;

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
        } catch (IllegalArgumentException e) {
            exception = true;
        }
        assertTrue(exception);
    }

}