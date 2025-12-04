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
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.core.Image;
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
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client);

        TableBuilder builder = new TableBuilder(2, "TableTest");

        assertEquals(2, builder.getColumnCount());

        builder.setColumn(0, "Image", ImageData.class);
        builder.setColumn(1, "Name", String.class);
        assertEquals("Image", builder.getColumnName(0));
        assertEquals("Name", builder.getColumnName(1));
        assertSame(ImageData.class, builder.getColumnType(0));

        builder.setRowCount(images.size());

        assertEquals(images.size(), builder.getRowCount());

        for (Image image : images) {
            assertFalse(builder.isComplete());
            builder.addRow(image.asDataObject(), image.getName());
        }

        assertEquals(images.get(0).asDataObject(), builder.getData(0, 0));
        assertEquals(images.get(1).getName(), builder.getData(1, 1));

        Table table = builder.createTable();
        dataset.addTable(client, table);

        List<Table> tables = dataset.getTables(client);
        client.deleteTables(tables);
        List<Table> noTables = dataset.getTables(client);

        assertEquals(1, tables.size());
        assertEquals(0, noTables.size());

        Table newTable = tables.get(0);
        assertEquals(builder.getName(), newTable.getName());
        assertEquals(table.getName(), newTable.getName());
        assertEquals(table.hashCode(), newTable.hashCode());
        assertEquals(table.getNumberOfRows(), newTable.getNumberOfRows());
        assertEquals(table.getOffset(), newTable.getOffset());
        assertEquals(table.getOriginalFileId(), newTable.getOriginalFileId());
        assertEquals(table.isCompleted(), newTable.isCompleted());
        assertEquals(table.isEmpty(), newTable.isEmpty());
    }


    @Test
    void testCreateTableWithEmptyColumns() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client);

        TableBuilder builder = new TableBuilder(4, "TableTest");

        assertEquals(4, builder.getColumnCount());

        builder.setColumn(0, "Image", ImageData.class);
        builder.setColumn(1, "Condition", String.class);
        builder.setColumn(2, "Name", String.class);
        builder.setColumn(3, "Phenotype", String.class);
        assertEquals("Image", builder.getColumnName(0));
        assertEquals("Condition", builder.getColumnName(1));
        assertEquals("Name", builder.getColumnName(2));
        assertEquals("Phenotype", builder.getColumnName(3));
        assertSame(ImageData.class, builder.getColumnType(0));

        builder.setRowCount(images.size());

        assertEquals(images.size(), builder.getRowCount());

        for (Image image : images) {
            assertFalse(builder.isComplete());
            builder.addRow(image.asDataObject(), "", image.getName(), "");
        }

        assertEquals(images.get(0).asDataObject(), builder.getData(0, 0));
        assertEquals("", builder.getData(0, 1));
        assertEquals(images.get(1).getName(), builder.getData(1, 2));
        assertEquals("", builder.getData(0, 3));

        Table table = builder.createTable();
        dataset.addTable(client, table);

        assertEquals(2, builder.getColumnCount());
        assertEquals(images.get(0).asDataObject(), builder.getData(0, 0));
        assertEquals(images.get(1).getName(), builder.getData(1, 1));

        List<Table> tables = dataset.getTables(client);
        client.deleteTables(tables);
        List<Table> noTables = dataset.getTables(client);

        assertEquals(1, tables.size());
        assertEquals(0, noTables.size());
    }


    @Test
    void testReplaceTable() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client);

        TableBuilder builder1 = new TableBuilder(2, "TableTest");

        assertEquals(2, builder1.getColumnCount());

        builder1.setColumn(0, "Image", ImageData.class);
        builder1.setColumn(1, "Name", String.class);
        assertEquals("Image", builder1.getColumnName(0));
        assertEquals("Name", builder1.getColumnName(1));
        assertSame(ImageData.class, builder1.getColumnType(0));

        builder1.setRowCount(images.size());

        assertEquals(images.size(), builder1.getRowCount());

        for (Image image : images) {
            assertFalse(builder1.isComplete());
            builder1.addRow(image.asDataObject(), image.getName());
        }

        assertEquals(images.get(0).asDataObject(), builder1.getData(0, 0));
        assertEquals(images.get(1).getName(), builder1.getData(0, 1));

        Table table1 = builder1.createTable();
        dataset.addTable(client, table1);
        long tableId1 = table1.getId();

        TableBuilder builder2 = new TableBuilder(2, "TableTest 2");
        builder2.setColumn(0, "Image", ImageData.class);
        builder2.setColumn(1, "Description", String.class);
        builder2.setRowCount(images.size());
        boolean untouched = true;
        for (Image image : images) {
            assertFalse(builder2.isComplete());
            String description = image.getDescription();
            if (untouched && description == null) {
                description = "";
                untouched   = false;
            }
            builder2.addRow(image.asDataObject(), description);
        }
        Table table2 = builder2.createTable();
        dataset.addTable(client, table2);
        long tableId2 = table2.getId();

        TableBuilder builder3 = new TableBuilder(2, "TableTest");
        builder3.setColumn(0, "Image", ImageData.class);
        builder3.setColumn(1, "Name", String.class);
        builder3.setRowCount(images.size());
        for (Image image : images) {
            assertFalse(builder3.isComplete());
            builder3.addRow(image.asDataObject(), "Test name");
        }
        Table table3 = builder3.createTable();
        dataset.addAndReplaceTable(client, table3);
        long tableId3 = table3.getId();

        assertNotEquals(tableId1, tableId3);
        assertNotEquals(tableId2, tableId3);

        List<Table> tables = dataset.getTables(client);
        for (Table table : tables) {
            client.deleteTable(table);
        }
        List<Table> noTables = dataset.getTables(client);

        assertEquals(1, builder2.getColumnCount());
        assertEquals(2, tables.size());
        assertEquals(0, noTables.size());
    }


    @Test
    void testErrorTableFull() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client);

        TableBuilder table = new TableBuilder(2, "TableTest");
        table.setName("TableTestNewName");

        assertEquals("TableTestNewName", table.getName());

        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        table.setRowCount(images.size() - 1);

        assertThrows(IndexOutOfBoundsException.class,
                     () -> images.forEach(i -> table.addRow(i.asDataObject(),
                                                            i.getName())));
    }


    @Test
    void testErrorTableColumn() {
        TableBuilder table = new TableBuilder(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);
        assertThrows(IndexOutOfBoundsException.class,
                     () -> table.setColumn(2, "Id", Long.class));
    }


    @Test
    void testErrorTableUninitialized() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client);

        TableBuilder table = new TableBuilder(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);
        assertThrows(IndexOutOfBoundsException.class,
                     () -> images.forEach(i -> table.addRow(i.asDataObject(),
                                                            i.getName())));
    }


    @Test
    void testErrorTableNotEnoughArgs() throws Exception {
        Dataset dataset = client.getDataset(DATASET1.id);

        List<Image> images = dataset.getImages(client);

        TableBuilder table = new TableBuilder(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);
        table.setRowCount(images.size());
        assertThrows(IllegalArgumentException.class,
                     () -> images.forEach(i -> table.addRow(i.asDataObject())));
    }

}