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


import ij.macro.Variable;
import ij.measure.ResultsTable;

import java.util.Arrays;
import java.util.List;

import static java.lang.Double.NaN;


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
     *     <li>{@code "Image_column_" + columnNumber} otherwise</li>
     * </ul>
     *
     * @param results The ResultsTable to process.
     */
    static void renameImageColumn(ResultsTable results) {
        if (results.columnExists(IMAGE)) {
            List<String> headings = Arrays.asList(results.getHeadings());
            if (!headings.contains(LABEL)) {
                results.renameColumn(IMAGE, LABEL);
            } else if (!results.columnExists(IMAGE + "_Name")) {
                results.renameColumn(IMAGE, IMAGE + "_Name");
            } else {
                results.renameColumn(IMAGE, IMAGE + "_column_" + results.getColumnIndex(IMAGE));
            }
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
                     .map(v -> !Double.isNaN(v.getValue())
                               || v.toString().equals(String.valueOf(NaN)))
                     .reduce(Boolean::logicalOr).orElse(false);
    }

}
