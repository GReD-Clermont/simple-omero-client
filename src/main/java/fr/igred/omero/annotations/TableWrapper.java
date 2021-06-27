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
    int        rowCount;
    /** Content of the table */
    Object[][] data;

    /** Current position in the table */
    int    row;
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
     * @param client        The client handling the connection.
     * @param results       An ImageJ results table.
     * @param imageId       An image ID.
     * @param ijRois        A list of ImageJ Rois.
     * @param roiIdProperty The Roi property storing the ROI IDs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public TableWrapper(Client client, ResultsTable results, Long imageId, List<Roi> ijRois, String roiIdProperty)
    throws ServiceException, AccessException, ExecutionException {
        this.name = results.getTitle();
        this.rowCount = results.size();

        ImageWrapper image = new ImageWrapper(null);

        List<ROIWrapper> rois = new ArrayList<>();

        int offset = 0;
        if (imageId != null) {
            image = client.getImage(imageId);
            rois = image.getROIs(client);
            offset++;
        }
        ROIData[] roiColumn = createROIColumn(results, rois, ijRois, roiIdProperty);
        if (roiColumn != null) {
            offset++;
        }

        String[] headings      = results.getHeadings();
        String[] shortHeadings = results.getHeadingsAsVariableNames();

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
            setColumn(1, "ROI", ROIData.class);
            data[1] = roiColumn;
        }
        for (int i = 0; i < nColumns; i++) {
            Variable[] col = results.getColumnAsVariables(headings[i]);

            if (isNumeric(col)) {
                setColumn(offset + i, shortHeadings[i], Double.class);
                data[offset + i] = Arrays.stream(col).map(Variable::getValue).toArray(Double[]::new);
            } else {
                setColumn(offset + i, shortHeadings[i], String.class);
                data[offset + i] = Arrays.stream(col).map(Variable::toString).toArray(String[]::new);
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
    private boolean isNumeric(Variable[] resultsColumn) {
        return Arrays.stream(resultsColumn)
                     .map(v -> !Double.isNaN(v.getValue())
                               || v.toString().equals(String.valueOf(Double.NaN)))
                     .reduce(Boolean::logicalOr).orElse(false);
    }


    /**
     * Creates a ROIData column
     *
     * @param results       An ImageJ results table.
     * @param rois          A list of OMERO ROIs.
     * @param ijRois        A list of ImageJ Rois.
     * @param roiIdProperty The Roi property storing the ROI IDs.
     *
     * @return An ROIData column.
     */
    private ROIData[] createROIColumn(ResultsTable results,
                                      List<ROIWrapper> rois,
                                      List<Roi> ijRois,
                                      String roiIdProperty) {
        ROIData[] roiColumn = null;

        Map<Long, ROIData> id2roi = rois.stream().collect(Collectors.toMap(ROIWrapper::getId, ROIWrapper::asROIData));

        Map<String, ROIData> roiName2roi = new HashMap<>(ijRois.size());
        for (Roi ijRoi : ijRois) {
            String value = ijRoi.getProperty(roiIdProperty);
            if (value != null) {
                roiName2roi.put(ijRoi.getName(), id2roi.get(Long.parseLong(value)));
            }
        }

        String[] labels = results.getColumnAsStrings("Label");
        boolean hasROIsInLabel = Arrays.stream(labels)
                                       .map(s -> roiName2roi.keySet().stream().anyMatch(s::contains))
                                       .reduce(Boolean::logicalAnd).orElse(false);

        if (results.columnExists("ROI")) {
            Variable[] roiCol = results.getColumnAsVariables("ROI");
            if (isNumeric(roiCol)) {
                roiColumn = Arrays.stream(roiCol)
                                  .map(v -> id2roi.get((long) v.getValue()))
                                  .toArray(ROIData[]::new);
            } else {
                roiColumn = Arrays.stream(roiCol)
                                  .map(v -> roiName2roi.get(v.toString()))
                                  .toArray(ROIData[]::new);
            }
            results.deleteColumn("ROI");
        } else if (hasROIsInLabel) {
            String[] roiNames = Arrays.stream(labels)
                                      .map(s -> roiName2roi.keySet().stream().filter(s::contains)
                                                           .collect(Collectors.toList()).get(0))
                                      .toArray(String[]::new);
            roiColumn = Arrays.stream(roiNames).map(roiName2roi::get).toArray(ROIData[]::new);
        }

        return roiColumn;
    }


    /**
     * Adds rows from an ImageJ {@link ResultsTable}.
     *
     * @param client        The client handling the connection.
     * @param results       An ImageJ results table.
     * @param imageId       An image ID.
     * @param ijRois        A list of ImageJ Rois.
     * @param roiIdProperty The Roi property storing the ROI IDs.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addRows(Client client, ResultsTable results, Long imageId, List<Roi> ijRois, String roiIdProperty)
    throws ServiceException, AccessException, ExecutionException {
        int offset = 0;

        ImageWrapper image = new ImageWrapper(null);

        List<ROIWrapper> rois = new ArrayList<>();

        if (imageId != null) {
            image = client.getImage(imageId);
            rois = image.getROIs(client);
            offset++;
        }
        ROIData[] roiColumn = createROIColumn(results, rois, ijRois, roiIdProperty);
        if (roiColumn != null) {
            offset++;
        }

        String[] headings = results.getHeadings();

        int nColumns = headings.length;
        if (nColumns + offset != columnCount) {
            throw new IllegalArgumentException("Number of columns mismatch");
        }

        Object[] newRow = new Object[offset + nColumns];

        final int n = results.size();
        setRowCount(rowCount + n);

        final boolean[] isNumeric = new boolean[nColumns];
        for (int j = 0; j < nColumns; j++) {
            isNumeric[j] = isNumeric(results.getColumnAsVariables(headings[j]));
        }
        for (int i = 0; i < n; i++) {
            if (offset > 0) newRow[0] = image.asImageData();
            if (roiColumn != null) newRow[1] = roiColumn[i];
            for (int j = 0; j < nColumns; j++) {
                if (isNumeric[j]) newRow[offset + j] = results.getValueAsDouble(j, i);
                else newRow[offset + j] = results.getStringValue(headings[j], i);
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
        truncateRow();

        return new TableData(columns, data);
    }

}