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


import omero.gateway.model.TableData;


public class TableWrapper implements Table {

    /** The wrapped TableData object */
    private final TableData tableData;

    /** Name of the table */
    private String name = null;

    /** ID of the table */
    private Long id = -1L;


    /**
     * Constructor of the TableWrapper class.
     *
     * @param tableData TableData to wrap.
     */
    public TableWrapper(TableData tableData) {
        this.tableData = tableData;
    }


    /**
     * Constructor of the TableWrapper class.
     *
     * @param tableData TableData to wrap.
     */
    public TableWrapper(TableData tableData, String name) {
        this(tableData);
        this.name = name;
    }


    @Override
    public boolean equals(Object obj) {
        return tableData.equals(obj);
    }


    @Override
    public int hashCode() {
        return tableData.hashCode();
    }


    /**
     * Get the TableData object.
     *
     * @return See above
     */
    @Override
    public TableData getTableData() {
        return tableData;
    }


    /**
     * Get the name of the table.
     *
     * @return See above
     */
    @Override
    public String getName() {
        return name;
    }


    /**
     * Set the name of the table.
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Get the ID of the table.
     *
     * @return See above
     */
    @Override
    public long getId() {
        return id;
    }


    /**
     * Set the ID of the table.
     */
    @Override
    public void setId(long id) {
        this.id = id;
    }


}
