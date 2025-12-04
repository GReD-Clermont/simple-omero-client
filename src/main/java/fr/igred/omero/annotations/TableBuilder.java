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


import fr.igred.omero.client.Client;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.ROIWrapper;
import ij.gui.Roi;
import ij.macro.Variable;
import ij.measure.ResultsTable;
import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;
import omero.gateway.model.ROIData;
import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.annotations.ROIColumnHelper.createROIColumn;
import static fr.igred.omero.annotations.ResultsTableHelper.IMAGE;
import static fr.igred.omero.annotations.ResultsTableHelper.LABEL;
import static fr.igred.omero.annotations.ResultsTableHelper.isColumnNumeric;
import static fr.igred.omero.annotations.ResultsTableHelper.renameImageColumn;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Class containing the information to create a Table in OMERO.
 * <p> The TableData itself is not contained, only the elements to create it, because once created the TableData cannot
 * be altered.
 * <p> To get the TableData corresponding to the elements contained use createTable.
 */
public class TableBuilder {

    /** Information of each column (Name, Type) */
    private TableDataColumn[] columns;

    /** Content of the table */
    private Object[][] data;

    /** Number of column in the table */
    private int columnCount;

    /** Number of row in the table */
    private int rowCount;

    /** Current position in the table */
    private int row;

    /** Name of the table */
    private String name;


    /**
     * Constructor of the class TableBuilder
     *
     * @param columnCount Number of column in the table.
     * @param name        Name of the table.
     */
    public TableBuilder(int columnCount, String name) {
        this.columnCount = columnCount;
        this.name        = name;
        columns          = new TableDataColumn[columnCount];
        data             = new Object[columnCount][0];
        rowCount         = 0;
        row              = 0;
    }


    /**
     * Constructor of the class TableBuilder. Uses an already existing table to create.
     *
     * @param table The table.
     */
    public TableBuilder(TableData table) {
        this.columns = table.getColumns();
        columnCount  = columns.length;
        data         = table.getData();
        rowCount     = (int) table.getNumberOfRows();
        row          = rowCount;
        name         = null;
    }


    /**
     * Constructor of the class TableBuilder. Uses an ImageJ {@link ResultsTable} to create.
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
    public TableBuilder(Client client, ResultsTable results, Long imageId, Collection<? extends Roi> ijRois)
    throws ServiceException, AccessException, ExecutionException {
        this(client, results, imageId, ijRois, ROI.IJ_PROPERTY);
    }


    /**
     * Constructor of the class TableBuilder. Uses an ImageJ {@link ResultsTable} to create.
     *
     * @param client      The client handling the connection.
     * @param results     An ImageJ results table.
     * @param imageId     An image ID.
     * @param ijRois      A list of ImageJ Rois.
     * @param roiProperty The Roi property storing the local index/label. Defaults to {@link ROIWrapper#IJ_PROPERTY} if
     *                    null or empty.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public TableBuilder(Client client, ResultsTable results, Long imageId, Collection<? extends Roi> ijRois,
                        String roiProperty)
    throws ServiceException, AccessException, ExecutionException {
        roiProperty = ROI.checkProperty(roiProperty);

        ResultsTable rt = (ResultsTable) results.clone();
        this.name     = rt.getTitle();
        this.rowCount = rt.size();

        int offset = 0;

        Image image = new ImageWrapper(null);

        List<ROI> rois = new ArrayList<>(0);

        if (imageId != null) {
            image = client.getImage(imageId);
            rois  = image.getROIs(client);
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
        columns          = new TableDataColumn[columnCount];
        data             = new Object[columnCount][];

        if (offset > 0) {
            createColumn(0, IMAGE, ImageData.class);
            data[0] = new ImageData[rowCount];
            Arrays.fill(data[0], image.asDataObject());
        }
        if (offset > 1) {
            createColumn(1, roiProperty, ROIData.class);
            data[1] = roiColumn;
        }
        for (int i = 0; i < nColumns; i++) {
            Variable[] col = rt.getColumnAsVariables(headings[i]);

            if (isColumnNumeric(col) && !headings[i].equals(LABEL)) {
                createColumn(offset + i, shortHeadings[i], Double.class);
                data[offset + i] = Arrays.stream(col)
                                         .map(Variable::getValue)
                                         .toArray(Double[]::new);
            } else {
                createColumn(offset + i, shortHeadings[i], String.class);
                data[offset + i] = rt.getColumnAsStrings(headings[i]);
            }
        }
        this.row = rowCount;
    }


    /**
     * Checks if the new columns match the existing ones.
     *
     * @param nColumns The number of columns in the current Results Table.
     * @param offset   The offset in the current Results Table.
     */
    private boolean checkColumns(int nColumns, int offset) {
        boolean match = offset + nColumns == columnCount;
        if (offset > 0 && !columns[0].getType().equals(ImageData.class)) {
            match = false;
        }
        if (offset > 1 && !columns[1].getType().equals(ROIData.class)) {
            match = false;
        }
        return match;
    }


    /**
     * Sets the information about a given column.
     *
     * @param column     Column number.
     * @param columnName Name of the column.
     * @param type       Type of the column.
     *
     * @throws IndexOutOfBoundsException Column number is bigger than actual number of column in the table.
     */
    private void createColumn(int column, String columnName, Class<?> type) {
        if (column < columnCount) {
            columns[column] = new TableDataColumn(columnName, column, type);
        } else {
            String error = String.format("Column %d doesn't exist", column);
            throw new IndexOutOfBoundsException(error);
        }
    }


    /**
     * Gets the indices of empty String columns.
     *
     * @return See above.
     */
    private Collection<Integer> getEmptyStringColumns() {
        Collection<Integer> emptyColumns = new ArrayList<>(0);
        for (int j = columns.length - 1; j >= 0; j--) {
            TableDataColumn column = columns[j];
            if (column.getType().equals(String.class)) {
                boolean empty = true;
                int     i     = 0;
                while (i < rowCount && empty) {
                    if (data[j][i] != null && !((String) data[j][i]).isEmpty()) {
                        empty = false;
                    }
                    i++;
                }
                if (empty) {
                    emptyColumns.add(j);
                }
            }
        }
        return emptyColumns;
    }


    /**
     * Removes the column at the specified index.
     *
     * @param index The index of the column to remove.
     */
    private void removeColumn(int index) {
        if (index < columnCount) {
            columnCount--;
            int               length     = columnCount - index;
            TableDataColumn[] newColumns = new TableDataColumn[columnCount];
            Object[][]        newData    = new Object[columnCount][];
            System.arraycopy(columns, 0, newColumns, 0, index);
            System.arraycopy(columns, index + 1, newColumns, index, length);
            System.arraycopy(data, 0, newData, 0, index);
            System.arraycopy(data, index + 1, newData, index, length);
            columns = newColumns;
            data    = newData;
        }
    }


    /**
     * Overridden to return the name of the class and the object id.
     */
    @Override
    public String toString() {
        return String.format("%s (name=%s)", getClass().getSimpleName(), name);
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
    public void addRows(Client client, ResultsTable results, Long imageId, Collection<? extends Roi> ijRois)
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
     * @param roiProperty The Roi property storing the local ROI index/label. Defaults to {@link ROIWrapper#IJ_PROPERTY}
     *                    if null or empty.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addRows(Client client, ResultsTable results, Long imageId, Collection<? extends Roi> ijRois,
                        String roiProperty)
    throws ServiceException, AccessException, ExecutionException {
        roiProperty = ROI.checkProperty(roiProperty);

        ResultsTable rt = (ResultsTable) results.clone();

        Image image = new ImageWrapper(null);

        List<ROI> rois = new ArrayList<>(0);

        int offset = 0;
        if (imageId != null) {
            image = client.getImage(imageId);
            rois  = image.getROIs(client);
            offset++;
            renameImageColumn(rt);
        }
        ROIData[] roiColumn = createROIColumn(rt, rois, ijRois, roiProperty);
        if (roiColumn.length > 0) {
            offset++;
        }

        String[] headings = rt.getHeadings();

        int nColumns = headings.length;
        if (!checkColumns(nColumns, offset)) {
            String error = "Number or type of columns mismatch";
            throw new IllegalArgumentException(error);
        }

        int n = rt.size();
        setRowCount(rowCount + n);

        if (offset > 0) {
            Arrays.fill(data[0], row, row + n, image.asDataObject());
        }
        if (offset > 1) {
            System.arraycopy(roiColumn, 0, data[1], row, n);
        }
        for (int i = 0; i < nColumns; i++) {
            if (columns[offset + i].getType().equals(String.class)) {
                String[] col = rt.getColumnAsStrings(headings[i]);
                System.arraycopy(col, 0, data[offset + i], row, n);
            } else {
                Double[] col = Arrays.stream(rt.getColumn(headings[i]))
                                     .boxed()
                                     .toArray(Double[]::new);
                System.arraycopy(col, 0, data[offset + i], row, n);
            }
        }
        row += n;
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
     * Returns the name of the table.
     *
     * @return See above.
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
     * Returns the number of columns in the table.
     *
     * @return See above.
     */
    public int getColumnCount() {
        return columnCount;
    }


    /**
     * Returns the name of the column.
     *
     * @param column Column number.
     *
     * @return See above.
     */
    public String getColumnName(int column) {
        return columns[column].getName();
    }


    /**
     * Returns the type of the column.
     *
     * @param column Column number.
     *
     * @return See above.
     */
    public Class<?> getColumnType(int column) {
        return columns[column].getType();
    }


    /**
     * Returns the number of rows in the table.
     *
     * @return See above.
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
        if (rowCount != this.rowCount) {
            Object[][] temp = new Object[columnCount][rowCount];
            if (data != null) {
                row = Math.min(rowCount, row);
                for (int j = 0; j < columnCount; j++) {
                    System.arraycopy(data[j], 0, temp[j], 0, row);
                }
            }
            this.rowCount = rowCount;
            data          = temp;
        }
    }


    /**
     * Checks if the table is complete
     *
     * @return true if the table is completed, false if some rows are still empty.
     */
    public boolean isComplete() {
        return row == rowCount;
    }


    /**
     * Sets the information about a certain column.
     *
     * @param column     Column number.
     * @param columnName Name of the column.
     * @param type       Type of the column.
     *
     * @throws IndexOutOfBoundsException Column number is bigger than actual number of column in the table.
     */
    public void setColumn(int column, String columnName, Class<?> type) {
        createColumn(column, columnName, type);
    }


    /**
     * Adds a row to the table.
     *
     * @param os Value for each column for the row.
     *
     * @throws IndexOutOfBoundsException Table is not initialized or already full.
     * @throws IllegalArgumentException  Incorrect argument number.
     */
    public void addRow(Object... os) {
        if (row < rowCount && os.length == columnCount) {
            for (int i = 0; i < os.length; i++) {
                Object o = os[i];
                data[i][row] = o;
            }
            row++;
        } else if (row >= rowCount) {
            if (rowCount == 0) {
                throw new IndexOutOfBoundsException("Row size is 0");
            } else {
                String error = "The table is already complete";
                throw new IndexOutOfBoundsException(error);
            }
        } else {
            String error = "Argument count is different than the column size";
            throw new IllegalArgumentException(error);
        }
    }


    /**
     * Deletes all unused row in the table
     */
    public void truncateRow() {
        setRowCount(row);
    }


    /**
     * Creates the corresponding TableData object.
     *
     * @return See above.
     */
    public Table createTable() {
        if (!isComplete()) {
            truncateRow();
        }

        List<Integer> emptyColumns = new ArrayList<>(getEmptyStringColumns());
        emptyColumns.sort(Collections.reverseOrder());
        emptyColumns.forEach(this::removeColumn);

        return new TableWrapper(new TableData(columns, data), name);
    }


    /**
     * Saves the current table as a character-delimited text file.
     *
     * @param path      The path to the file where the table will be saved.
     * @param delimiter The character used to specify the boundary between columns.
     *
     * @throws FileNotFoundException        The requested file cannot be written.
     * @throws UnsupportedEncodingException If the UTF8 charset is not supported.
     */
    public void saveAs(String path, char delimiter)
    throws FileNotFoundException, UnsupportedEncodingException {
        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMaximumFractionDigits(4);
        formatter.setGroupingUsed(false);

        StringBuilder sb = new StringBuilder(10 * columnCount * rowCount);

        File file = new File(path);

        String sol = "\"";
        String sep = String.format("\"%c\"", delimiter);
        String eol = String.format("\"%n");
        try (PrintWriter stream = new PrintWriter(file, UTF_8.name())) {
            sb.append(sol);
            for (int j = 0; j < columnCount; j++) {
                sb.append(columns[j].getName());
                if (j != columnCount - 1) {
                    sb.append(sep);
                }
            }
            sb.append(eol);
            for (int i = 0; i < rowCount; i++) {
                sb.append(sol);
                for (int j = 0; j < columnCount; j++) {
                    Object value = data[j][i];
                    if (DataObject.class.isAssignableFrom(columns[j].getType())) {
                        value = ((DataObject) value).getId();
                    }
                    if (value instanceof Number) {
                        value = formatter.format(value);
                    }
                    sb.append(value);
                    if (j != columnCount - 1) {
                        sb.append(sep);
                    }
                }
                sb.append(eol);
            }
            stream.write(sb.toString());
        }
    }

}