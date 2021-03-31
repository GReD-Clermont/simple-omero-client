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


import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;


/**
 * Class containing the information to create a Table in OMERO.
 * <p> The TableData itself is not contained, only the elements to create it, because once created the TableData cannot
 * be altered.
 * <p> To get the TableData corresponding to the elements contained use createTable.
 */
public class TableWrapper {

    /** Number of column in the table */
    final int               columnCount;
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
    public TableWrapper(int columnCount,
                        String name) {
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