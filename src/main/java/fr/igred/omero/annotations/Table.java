/*
 *  Copyright (C) 2020-2023 GReD
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


import fr.igred.omero.client.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.ROIWrapper;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;


public interface Table {

    /**
     * Adds rows from an ImageJ {@link ResultsTable}.
     *
     * @param client  The client handling the connection.
     * @param results An ImageJ results table.
     * @param imageId An image ID.
     * @param ijRois  A list of ImageJ Rois.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default void addRows(Client client, ResultsTable results, Long imageId, Collection<? extends Roi> ijRois)
    throws ServiceException, AccessException, ExecutionException {
        this.addRows(client, results, imageId, ijRois, ROI.IJ_PROPERTY);
    }


    /**
     * Adds rows from an ImageJ {@link ResultsTable}.
     *
     * @param client      The client handling the connection.
     * @param results     An ImageJ results table.
     * @param imageId     An image ID.
     * @param ijRois      A list of ImageJ Rois.
     * @param roiProperty The Roi property storing the local ROI IDs. Defaults to {@link ROIWrapper#IJ_PROPERTY} if null
     *                    or empty.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    void addRows(Client client, ResultsTable results, Long imageId, Collection<? extends Roi> ijRois,
                 String roiProperty)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the {@link TableDataColumn} which contains information on each column of the table
     *
     * @return the {@link TableDataColumn} which contains information on each column of the table.
     */
    TableDataColumn[] getColumns();


    /**
     * Gets the value contained in the table
     *
     * @return the value contained in the table.
     */
    Object[][] getData();


    /**
     * Gets a certain value of the table
     *
     * @param x Row position.
     * @param y Column position.
     *
     * @return the value at position data[y][x].
     */
    Object getData(int x, int y);


    /**
     * Returns the fileId of the table.
     *
     * @return See above.
     */
    Long getFileId();


    /**
     * Sets the fileId of the table.
     *
     * @param fileId New fileId.
     */
    void setFileId(Long fileId);


    /**
     * Returns the table ID.
     *
     * @return See above.
     */
    Long getId();


    /**
     * Sets the id of the table.
     *
     * @param id New id.
     */
    void setId(Long id);


    /**
     * Returns the name of the table.
     *
     * @return See above.
     */
    String getName();


    /**
     * Sets the name of the table.
     *
     * @param name New name.
     */
    void setName(String name);


    /**
     * Returns the number of columns in the table.
     *
     * @return See above.
     */
    int getColumnCount();


    /**
     * Returns the name of the column.
     *
     * @param column Column number.
     *
     * @return See above.
     */
    String getColumnName(int column);


    /**
     * Returns the type of the column.
     *
     * @param column Column number.
     *
     * @return See above.
     */
    Class<?> getColumnType(int column);


    /**
     * Returns the number of rows in the table.
     *
     * @return See above.
     */
    int getRowCount();


    /**
     * Sets the number of row in the table. Copies already existing data if some were already in the data
     *
     * @param rowCount New rowCount.
     */
    void setRowCount(int rowCount);


    /**
     * Checks if the table is complete
     *
     * @return true if the table is completed, false if some rows are still empty.
     */
    boolean isComplete();


    /**
     * Sets the information about a certain column.
     *
     * @param column     Column number.
     * @param columnName Name of the column.
     * @param type       Type of the column.
     *
     * @throws IndexOutOfBoundsException Column number is bigger than actual number of column in the table.
     */
    void setColumn(int column, String columnName, Class<?> type);


    /**
     * Adds a row to the table.
     *
     * @param os Value for each column for the row.
     *
     * @throws IndexOutOfBoundsException Table is not initialized or already full.
     * @throws IllegalArgumentException  Incorrect argument number.
     */
    void addRow(Object... os);


    /**
     * Deletes all unused row in the table
     */
    void truncateRow();


    /**
     * Creates the corresponding TableData object.
     *
     * @return See above.
     */
    TableData createTable();


    /**
     * Saves the current table as a character-delimited text file.
     *
     * @param path      The path to the file where the table will be saved.
     * @param delimiter The character used to specify the boundary between columns.
     *
     * @throws FileNotFoundException        The requested file cannot be written.
     * @throws UnsupportedEncodingException If the UTF8 charset is not supported.
     */
    void saveAs(String path, char delimiter) throws FileNotFoundException, UnsupportedEncodingException;

}
