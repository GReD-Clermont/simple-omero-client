/*
 *  Copyright (C) 2020-2025 GReD
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


import omero.gateway.model.DataObject;
import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.StringJoiner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedWriter;


public interface Table {

    /**
     * Get the TableData object.
     *
     * @return See above
     */
    TableData getTableData();

    /**
     * Get the name of the table.
     *
     * @return See above
     */
    String getName();

    /**
     * Set the name of the table.
     *
     * @param name See above.
     */
    void setName(String name);

    /**
     * Get the ID of the table.
     *
     * @return See above
     */
    long getId();

    /**
     * Set the ID of the table.
     *
     * @param id See above.
     */
    void setId(long id);

    /**
     * @return The total number of rows in the original table (this doesn't have
     * to match data[x].length, depending on how many rows are loaded)
     */
    default long getNumberOfRows() {
        return getTableData().getNumberOfRows();
    }

    /**
     * Set the total number of rows in the original table.
     *
     * @param numberOfRows See above
     */
    default void setNumberOfRows(long numberOfRows) {
        getTableData().setNumberOfRows(numberOfRows);
    }

    /**
     * Manually set completed state (sets the {@code TableData#numberOfRows} to
     * the last row in the {@code TableData#data} array)
     */
    default void setCompleted() {
        getTableData().setCompleted();
    }

    /**
     * @return {@code true} if the last available row is contained,
     * {@code false} if there's more data available in the original
     * table
     */
    default boolean isCompleted() {
        return getTableData().isCompleted();
    }

    /**
     * @return {@code true} if this TableData object doesn't contain any
     * data, {@code false} if it does contain data.
     */
    default boolean isEmpty() {
        return getTableData().isEmpty();
    }

    /**
     * Get the original file id
     *
     * @return See above
     */
    default long getOriginalFileId() {
        return getTableData().getOriginalFileId();
    }

    /**
     * Set the originalfile id
     *
     * @param originalFileId The originalfile id
     */
    default void setOriginalFileId(long originalFileId) {
        getTableData().setOriginalFileId(originalFileId);
    }

    /**
     * Get the row offset (if this {@link TableData} represents only a subset of
     * the original table)
     *
     * @return See above
     */
    default long getOffset() {
        return getTableData().getOffset();
    }

    /**
     * Set the row offset (if this {@link TableData} represents only a subset of
     * the original table)
     *
     * @param offset The row offset
     */
    default void setOffset(long offset) {
        getTableData().setOffset(offset);
    }

    /**
     * Get the data in form Object['column index']['row data']
     *
     * @return See above
     */
    default Object[][] getData() {
        return getTableData().getData();
    }

    /**
     * Get the headers
     *
     * @return See above
     */
    default TableDataColumn[] getColumns() {
        return getTableData().getColumns();
    }


    /**
     * Saves the current table as a character-delimited text file.
     *
     * @param path      The path to the file where the table will be saved.
     * @param delimiter The character used to specify the boundary between columns.
     *
     * @throws IOException Cannot write to the specified file.
     */
    default void saveAs(String path, char delimiter)
    throws IOException {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(4);
        formatter.setGroupingUsed(false);

        try (BufferedWriter writer = newBufferedWriter(Path.of(path), UTF_8)) {
            var  data        = getData();
            var  columns     = getColumns();
            int  columnCount = columns.length;
            long rowCount    = columnCount > 0 ? data[0].length : 0;

            String quote = "\"";
            String sep   = String.format("%s%c%s", quote, delimiter, quote);

            StringJoiner headers = new StringJoiner(sep, quote, quote);
            for (var col : columns) {
                headers.add(col.getName());
            }
            writer.write(headers.toString());
            writer.newLine();

            for (int i = 0; i < rowCount; i++) {
                StringJoiner line = new StringJoiner(sep, quote, quote);
                for (int j = 0; j < columnCount; j++) {
                    Object value = data[j][i];
                    if (DataObject.class.isAssignableFrom(columns[j].getType())) {
                        value = ((DataObject) value).getId();
                    }
                    if (value instanceof Number) {
                        value = formatter.format(value);
                    }
                    line.add(String.valueOf(value));
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

}
