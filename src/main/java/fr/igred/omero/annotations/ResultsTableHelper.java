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


import ij.macro.Variable;
import ij.measure.ResultsTable;

import java.util.Arrays;
import java.util.List;

import static java.lang.Double.NaN;
import static java.lang.Double.isNaN;
import static java.lang.String.valueOf;


/**
 * Class containing helper methods to manipulate ImageJ ResultsTable objects.
 */
final class ResultsTableHelper {

    /** Label column name */
    static final String LABEL = "Label";
    /** Image column name */
    static final String IMAGE = "Image";


    /**
     * Private constructor to prevent instantiation
     */
    private ResultsTableHelper() {
    }


    /**
     * Rename the {@value IMAGE} column if it already exists to:
     * <ul>
     *     <li>"{@value LABEL} if the column does not exist</li>
     *     <li>{@value IMAGE} + {@code "_Name"} if it does not exist but {@value LABEL} does</li>
     *     <li>{@value IMAGE} + {@code "_column_" + columnNumber} otherwise</li>
     * </ul>
     *
     * @param results The ResultsTable to process.
     */
    static void renameImageColumn(ResultsTable results) {
        if (results.columnExists(IMAGE)) {
            List<String> headings = Arrays.asList(results.getHeadings());

            String newName;
            if (!headings.contains(LABEL)) {
                newName = LABEL;
            } else if (!results.columnExists(IMAGE + "_Name")) {
                newName = IMAGE + "_Name";
            } else {
                newName = IMAGE + "_column_" + results.getColumnIndex(IMAGE);
            }

            results.renameColumn(IMAGE, newName);
        }
    }


    /**
     * Checks if a column from a {@link ResultsTable} is numeric or not.
     *
     * @param resultsColumn An ImageJ results table column.
     *
     * @return Whether the column holds numeric values or not.
     */
    static boolean isColumnNumeric(Variable[] resultsColumn) {
        return Arrays.stream(resultsColumn)
                     .map(ResultsTableHelper::isNumeric)
                     .reduce(Boolean::logicalOr).orElse(false);
    }


    /**
     * Checks if a Variable is numeric or not.
     *
     * @param v An ImageJ Variable.
     *
     * @return Whether the Variable holds a numeric value or not.
     */
    static boolean isNumeric(Variable v) {
        return !isNaN(v.getValue()) || v.toString().equals(valueOf(NaN));
    }

}
