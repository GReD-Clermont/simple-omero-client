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
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import omero.gateway.model.ImageData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;


class TableTest extends UserTest {


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
            assertFalse(table.isComplete());
            table.addRow(image.asDataObject(), image.getName());
        }

        assertEquals(images.get(0).asDataObject(), table.getData(0, 0));
        assertEquals(images.get(1).getName(), table.getData(0, 1));

        dataset.addTable(client, table);

        List<TableWrapper> tables = dataset.getTables(client);
        client.deleteTables(tables);
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
            assertFalse(table1.isComplete());
            table1.addRow(image.asDataObject(), image.getName());
        }

        assertEquals(images.get(0).asDataObject(), table1.getData(0, 0));
        assertEquals(images.get(1).getName(), table1.getData(0, 1));

        dataset.addTable(client, table1);
        long tableId1 = table1.getId();

        TableWrapper table2 = new TableWrapper(2, "TableTest 2");
        table2.setColumn(0, "Image", ImageData.class);
        table2.setColumn(1, "Description", String.class);
        table2.setRowCount(images.size());
        boolean untouched = true;
        for (ImageWrapper image : images) {
            assertFalse(table2.isComplete());
            String description = image.getDescription();
            if (untouched && description == null) {
                description = "";
                untouched   = false;
            }
            table2.addRow(image.asDataObject(), description);
        }
        dataset.addTable(client, table2);
        long tableId2 = table2.getId();

        TableWrapper table3 = new TableWrapper(2, "TableTest");
        table3.setColumn(0, "Image", ImageData.class);
        table3.setColumn(1, "Name", String.class);
        table3.setRowCount(images.size());
        for (ImageWrapper image : images) {
            assertFalse(table3.isComplete());
            table3.addRow(image.asDataObject(), "Test name");
        }
        dataset.addAndReplaceTable(client, table3);
        long tableId3 = table3.getId();

        assertNotEquals(tableId1, tableId3);
        assertNotEquals(tableId2, tableId3);

        List<TableWrapper> tables = dataset.getTables(client);
        for (TableWrapper table : tables) {
            client.deleteTable(table);
        }
        List<TableWrapper> noTables = dataset.getTables(client);

        assertEquals(1, table2.getColumnCount());
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
                     () -> images.forEach(img -> table.addRow(img.asDataObject(), img.getName())));
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
                     () -> images.forEach(img -> table.addRow(img.asDataObject(), img.getName())));
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
                     () -> images.forEach(img -> table.addRow(img.asDataObject())));
    }

}