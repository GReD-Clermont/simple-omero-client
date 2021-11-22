/*
 *  Copyright (C) 2020-2021 GReD
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


import fr.igred.omero.Client;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.roi.ROIWrapper;
import ij.gui.Roi;
import ij.macro.Variable;
import ij.measure.ResultsTable;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


/**
 * Class containing the information to create a Table in OMERO.
 * <p> The TableData itself is not contained, only the elements to create it, because once created the TableData cannot
 * be altered.
 * <p> To get the TableData corresponding to the elements contained use createTable.
 */
public class TableWrapper {

    /** Number of column in the table */
    final int columnCount;

    /** Information of each column (Name, Type) */
    final TableDataColumn[] columns;

    /** Number of row in the table */
    int rowCount;

    /** Content of the table */
    Object[][] data;

    /** Current position in the table */
    int row;

    /** Name of the table */
    String name;

    /** File id of the table */
    Long fileId;
    /** Id of the table */
    Long id;


    /**
     * Constructor of the class TableWrapper
     *
     * @param columnCount Number of column in the table.
     * @param name        Name of the table.
     */
    public TableWrapper(int columnCount, String name) {
        this.columnCount = columnCount;
        columns = new TableDataColumn[columnCount];

        rowCount = 0;

        this.name = name;

        row = 0;
    }


    /**
     * Constructor of the class TableWrapper. Uses an already existing table to create.
     *
     * @param table The table.
     */
    public TableWrapper(TableData table) {
        this.columns = table.getColumns();
        columnCount = columns.length;

        data = table.getData();
        rowCount = (int) table.getNumberOfRows();
        row = rowCount;
    }


    /**
     * Constructor of the class TableWrapper. Uses an ImageJ {@link ResultsTable} to create.
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
    public TableWrapper(Client client, ResultsTable results, Long imageId, List<Roi> ijRois)
    throws ServiceException, AccessException, ExecutionException {
        this(client, results, imageId, ijRois, ROIWrapper.ROI_IJ_PROPERTY);
    }


    /**
     * Constructor of the class TableWrapper. Uses an ImageJ {@link ResultsTable} to create.
     *
     * @param client      The client handling the connection.
     * @param results     An ImageJ results table.
     * @param imageId     An image ID.
     * @param ijRois      A list of ImageJ Rois.
     * @param roiProperty The Roi property storing the local ROI IDs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public TableWrapper(Client client, ResultsTable results, Long imageId, List<Roi> ijRois, String roiProperty)
    throws ServiceException, AccessException, ExecutionException {
        ResultsTable rt = (ResultsTable) results.clone();
        this.fileId = null;
        this.name = rt.getTitle();
        this.rowCount = rt.size();

        int offset = 0;

        ImageWrapper image = new ImageWrapper(null);

        List<ROIWrapper> rois = new ArrayList<>();

        if (imageId != null) {
            image = client.getImage(imageId);
            rois = image.getROIs(client);
            offset++;
            renameImageColumn(rt);
        }
        ROIData[] roiColumn = createROIColumn(rt, rois, ijRois, roiProperty);
        if (roiColumn.length > 0) {
            offset++;
        }

        String[] headings      = rt.getHeadings();
        String[] shortHeadings = rt.getHeadingsAsVariableNames();

        int nColumns = headings.length;
        this.columnCount = nColumns + offset;
        columns = new TableDataColumn[columnCount];
        data = new Object[columnCount][];

        if (offset > 0) {
            setColumn(0, "Image", ImageData.class);
            data[0] = new ImageData[rowCount];
            Arrays.fill(data[0], image.asImageData());
        }
        if (offset > 1) {
            setColumn(1, roiProperty, ROIData.class);
            data[1] = roiColumn;
        }
        for (int i = 0; i < nColumns; i++) {
            Variable[] col = rt.getColumnAsVariables(headings[i]);

            if (isColumnNumeric(col)) {
                setColumn(offset + i, shortHeadings[i], Double.class);
                data[offset + i] = Arrays.stream(col).map(Variable::getValue).toArray(Double[]::new);
            } else {
                setColumn(offset + i, shortHeadings[i], String.class);
                data[offset + i] = Arrays.stream(col).map(Variable::getString).toArray(String[]::new);
            }
        }
        this.row = rowCount;
    }


    /**
     * Checks if a column from a {@link ResultsTable} is numeric or not.
     *
     * @param resultsColumn An ImageJ results table column.
     *
     * @return Whether the column holds numeric values or not.
     */
    private static boolean isColumnNumeric(Variable[] resultsColumn) {
        return Arrays.stream(resultsColumn)
                     .map(v -> !Double.isNaN(v.getValue())
                               || v.toString().equals(String.valueOf(Double.NaN)))
                     .reduce(Boolean::logicalOr).orElse(false);
    }


    /**
     * Rename "Image" column if it already exists to:
     * <ul>
     *     <li>"Label" if the column does not exist</li>
     *     <li>{@code "Image_column_" + columnNumber} otherwise</li>
     * </ul>
     *
     * @param results The results table to process.
     */
    private static void renameImageColumn(ResultsTable results) {
        final String labelColName = "Label";
        final String imageColName = "Image";
        if (results.columnExists(imageColName)) {
            if (!results.columnExists(labelColName)) results.renameColumn(imageColName, labelColName);
            else if (!results.columnExists("Image_Name")) results.renameColumn(imageColName, imageColName + "_Name");
            else results.renameColumn(imageColName, imageColName + "_column_" + results.getColumnIndex(imageColName));
        }
    }


    /**
     * Creates a ROIData column from a Variable column containing either:
     * <ul>
     *     <li>The ROI local IDs (indices, assumed by default)</li>
     *     <li>The ROI OMERO IDs (if indices do not map)</li>
     *     <li>The ShapeData names (if the column contains Strings)</li>
     * </ul>
     *
     * @param roiCol      Variable column containing ROI info
     * @param index2roi   ROI indices map
     * @param id2roi      ROI IDs map
     * @param roiName2roi ROI names map
     *
     * @return A ROIData column.
     */
    private static ROIData[] columnToROIColumn(Variable[] roiCol,
                                               Map<Integer, ROIData> index2roi,
                                               Map<Long, ROIData> id2roi,
                                               Map<String, ROIData> roiName2roi) {
        ROIData[] roiColumn = new ROIData[0];
        if (isColumnNumeric(roiCol)) {
            List<Long> ids = Arrays.stream(roiCol)
                                   .map(Variable::getValue)
                                   .map(Double::longValue)
                                   .collect(Collectors.toList());

            List<Integer> indices = Arrays.stream(roiCol)
                                          .map(Variable::getValue)
                                          .map(Double::intValue)
                                          .collect(Collectors.toList());

            index2roi.keySet().retainAll(indices);
            id2roi.keySet().retainAll(ids);
            boolean isIndices = index2roi.size() >= id2roi.size();
            if (isIndices) {
                roiColumn = indices.stream().map(index2roi::get).toArray(ROIData[]::new);
                if (Arrays.asList(roiColumn).contains(null)) isIndices = false;
            }
            if (!isIndices) {
                roiColumn = ids.stream().map(id2roi::get).toArray(ROIData[]::new);
            }
        } else {
            roiColumn = Arrays.stream(roiCol)
                              .map(v -> roiName2roi.get(v.getString()))
                              .toArray(ROIData[]::new);
        }
        return roiColumn;
    }


    /**
     * Creates a ROIData column.
     * <p>A column named either {@code roiProperty} or {@link ROIWrapper#roiIdIjProperty(String roiProperty)} is
     * expected. It will look for the ROI OMERO ID in the latter, or for the local ID, the OMERO ID or the shape names
     * in the former.
     * <p>If neither column is present, it will check the "Label" column for the ROI names inside.
     *
     * @param results     An ImageJ results table.
     * @param rois        A list of OMERO ROIs.
     * @param ijRois      A list of ImageJ Rois.
     * @param roiProperty The Roi property storing the local ROI IDs.
     *
     * @return An ROIData column.
     */
    private static ROIData[] createROIColumn(ResultsTable results,
                                             List<ROIWrapper> rois,
                                             List<Roi> ijRois,
                                             String roiProperty) {
        String roiIdProperty = ROIWrapper.roiIdIjProperty(roiProperty);

        ROIData[] empty     = new ROIData[0];
        ROIData[] roiColumn = empty;

        Map<Long, ROIData> id2roi = rois.stream().collect(Collectors.toMap(ROIWrapper::getId, ROIWrapper::asROIData));

        Map<Integer, ROIData> index2roi   = new HashMap<>(ijRois.size());
        Map<String, ROIData>  roiName2roi = new HashMap<>(ijRois.size());
        for (Roi ijRoi : ijRois) {
            String index = ijRoi.getProperty(roiProperty);
            String id    = ijRoi.getProperty(roiIdProperty);
            if (id != null) {
                roiName2roi.put(ijRoi.getName(), id2roi.get(Long.parseLong(id)));
                if (index != null) index2roi.putIfAbsent(Integer.parseInt(index), id2roi.get(Long.parseLong(id)));
            }
        }

        String[] headings = results.getHeadings();

        final String labelColName = "Label";

        if (results.columnExists(roiProperty)) {
            Variable[] roiCol = results.getColumnAsVariables(roiProperty);
            roiColumn = columnToROIColumn(roiCol, index2roi, id2roi, roiName2roi);
            // If roiColumn contains null, we return an empty array
            if (Arrays.asList(roiColumn).contains(null)) return empty;
            results.deleteColumn(roiProperty);
        } else if (results.columnExists(roiIdProperty)) {
            Variable[] roiCol = results.getColumnAsVariables(roiIdProperty);
            List<Long> ids = Arrays.stream(roiCol)
                                   .map(Variable::getValue)
                                   .map(Double::longValue)
                                   .collect(Collectors.toList());
            roiColumn = ids.stream().map(id2roi::get).toArray(ROIData[]::new);
            // If roiColumn contains null, we return an empty array
            if (Arrays.asList(roiColumn).contains(null)) return empty;
            results.deleteColumn(roiIdProperty);
        } else if (Arrays.asList(headings).contains(labelColName)) {
            String[] roiNames = Arrays.stream(results.getColumnAsVariables(labelColName))
                                      .map(Variable::getString)
                                      .map(s -> roiName2roi.keySet().stream().filter(s::contains)
                                                           .findFirst().orElse(null))
                                      .toArray(String[]::new);
            roiColumn = Arrays.stream(roiNames).map(roiName2roi::get).toArray(ROIData[]::new);
            if (Arrays.asList(roiColumn).contains(null)) return empty;
        }

        return roiColumn;
    }


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
    public void addRows(Client client, ResultsTable results, Long imageId, List<Roi> ijRois)
    throws ServiceException, AccessException, ExecutionException {
        this.addRows(client, results, imageId, ijRois, ROIWrapper.ROI_IJ_PROPERTY);
    }


    /**
     * Adds rows from an ImageJ {@link ResultsTable}.
     *
     * @param client      The client handling the connection.
     * @param results     An ImageJ results table.
     * @param imageId     An image ID.
     * @param ijRois      A list of ImageJ Rois.
     * @param roiProperty The Roi property storing the local ROI IDs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addRows(Client client, ResultsTable results, Long imageId, List<Roi> ijRois, String roiProperty)
    throws ServiceException, AccessException, ExecutionException {
        ResultsTable rt = (ResultsTable) results.clone();

        ImageWrapper image = new ImageWrapper(null);

        List<ROIWrapper> rois = new ArrayList<>();

        int offset = 0;
        if (imageId != null) {
            image = client.getImage(imageId);
            rois = image.getROIs(client);
            offset++;
            renameImageColumn(rt);
        }
        ROIData[] roiColumn = createROIColumn(rt, rois, ijRois, roiProperty);
        if (roiColumn.length > 0) {
            offset++;
        }

        String[] headings = rt.getHeadings();

        int nColumns = headings.length;
        if (nColumns + offset != columnCount) {
            throw new IllegalArgumentException("Number of columns mismatch");
        }

        Object[] newRow = new Object[offset + nColumns];

        final int n = rt.size();
        setRowCount(rowCount + n);

        for (int i = 0; i < n; i++) {
            if (offset > 0) newRow[0] = image.asImageData();
            if (roiColumn.length > 0) newRow[1] = roiColumn[i];
            for (int j = 0; j < nColumns; j++) {
                if (columns[offset + j].getType().equals(String.class)) {
                    newRow[offset + j] = rt.getStringValue(headings[j], i);
                } else {
                    newRow[offset + j] = rt.getValue(headings[j], i);
                }
            }
            addRow(newRow);
        }
    }


    /**
     * Gets the {@link TableDataColumn} which contains information on each column of the table
     *
     * @return the {@link TableDataColumn} which contains information on each column of the table.
     */
    public TableDataColumn[] getColumns() {
        return columns.clone();
    }


    /**
     * Gets the value contained in the table
     *
     * @return the value contained in the table.
     */
    public Object[][] getData() {
        return data.clone();
    }


    /**
     * Gets a certain value of the table
     *
     * @param x Row position.
     * @param y Column position.
     *
     * @return the value at position data[y][x].
     */
    public Object getData(int x, int y) {
        return data[y][x];
    }


    /**
     * @return fileId of the table.
     */
    public Long getFileId() {
        return fileId;
    }


    /**
     * Sets the fileId of the table.
     *
     * @param fileId New fileId.
     */
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }


    /**
     * @return id of the table.
     */
    public Long getId() {
        return id;
    }


    /**
     * Sets the id of the table.
     *
     * @param id New id.
     */
    public void setId(Long id) {
        this.id = id;
    }


    /**
     * @return name of the table.
     */
    public String getName() {
        return name;
    }


    /**
     * Sets the name of the table.
     *
     * @param name New name.
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return number of column in the table.
     */
    public int getColumnCount() {
        return columnCount;
    }


    /**
     * @param column Column number.
     *
     * @return The name of the column.
     */
    public String getColumnName(int column) {
        return columns[column].getName();
    }


    /**
     * @param column Column number.
     *
     * @return The type of the column.
     */
    public Class<?> getColumnType(int column) {
        return columns[column].getType();
    }


    /**
     * @return number of row in the table.
     */
    public int getRowCount() {
        return rowCount;
    }


    /**
     * Sets the number of row in the table. Copies already existing data if some were already in the data
     *
     * @param rowCount New rowCount.
     */
    public void setRowCount(int rowCount) {
        Object[][] temp = new Object[columnCount][rowCount];

        if (data != null) {
            row = Math.min(rowCount, row);
            for (int i = 0; i < row; i++)
                for (int j = 0; j < columnCount; j++)
                    temp[j][i] = data[j][i];
        }

        this.rowCount = rowCount;
        data = temp;
    }


    /**
     * Checks if the table is complete
     *
     * @return true  if the table is completed. false if some row are still empty.
     */
    public boolean isComplete() {
        return row == rowCount;
    }


    /**
     * Sets the information about a certain column.
     *
     * @param column Column number.
     * @param name   Name of the column.
     * @param type   Type of the column.
     *
     * @throws IndexOutOfBoundsException Column number is bigger than actual number of column in the table.
     */
    public void setColumn(int column, String name, Class<?> type) throws IndexOutOfBoundsException {
        if (column < columnCount)
            columns[column] = new TableDataColumn(name, column, type);
        else
            throw new IndexOutOfBoundsException("Column " + column + " doesn't exist");
    }


    /**
     * Adds a row to the table.
     *
     * @param os Value for each column for the row.
     *
     * @throws IndexOutOfBoundsException Table is not initialized or already full.
     * @throws IllegalArgumentException  Incorrect argument number.
     */
    public void addRow(Object... os) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (row < rowCount && os.length == columnCount) {
            for (int i = 0; i < os.length; i++) {
                Object o = os[i];

                data[i][row] = o;
            }
            row++;
        } else {
            if (row >= rowCount) {
                if (rowCount == 0)
                    throw new IndexOutOfBoundsException("Row size is 0");
                else
                    throw new IndexOutOfBoundsException("The table is already complete");
            } else
                throw new IllegalArgumentException("Argument count is different than the column size");
        }
    }


    /**
     * Deletes all unused row in the table
     */
    public void truncateRow() {
        setRowCount(row);
    }


    public TableData createTable() {
        if (!isComplete()) truncateRow();

        return new TableData(columns, data);
    }

}