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


import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.ROIWrapper;
import ij.gui.Roi;
import ij.macro.Variable;
import ij.measure.ResultsTable;
import omero.gateway.model.ROIData;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static fr.igred.omero.annotations.ResultsTableHelper.LABEL;
import static fr.igred.omero.annotations.ResultsTableHelper.isColumnNumeric;
import static java.util.stream.Collectors.toMap;


/**
 * Helper class to create a ROIData column from an ImageJ ResultsTable.
 */
final class ROIColumn {

    /** Empty ROI array */
    private static final ROIData[] EMPTY_ROI = new ROIData[0];


    /**
     * Default constructor: private to prevent instantiation
     */
    private ROIColumn() {
        // Prevent instantiation
    }


    /**
     * Creates a ROIData column.
     * <p>A column named either {@code roiProperty} or {@link ROIWrapper#ijIDProperty(String roiProperty)} is
     * expected. It will look for the ROI OMERO ID in the latter, or for the local label/index, the OMERO ID, the names
     * or the shape names in the former.
     * <p>If neither column is present, it will check the {@value ResultsTableHelper#LABEL} column for the ROI names inside.
     *
     * @param results     An ImageJ results table.
     * @param rois        A list of OMERO ROIs (each ROI (ID) should be present only once).
     * @param ijRois      A list of ImageJ Rois.
     * @param roiProperty The Roi property storing the local ROI label/index.
     */
    static ROIData[] createROIColumn(ResultsTable results,
                                     Collection<? extends ROI> rois,
                                     Collection<? extends Roi> ijRois,
                                     String roiProperty) {
        String roiIdProperty = ROI.ijIDProperty(roiProperty);

        ROIData[] roiColumn = EMPTY_ROI;

        Map<Long, ROIData> id2roi = rois.stream()
                                        .collect(toMap(ROI::getId,
                                                       ROI::asDataObject));
        Map<String, ROIData> name2roi = rois.stream()
                                            .filter(r -> !r.getName().isEmpty())
                                            .collect(toMap(ROI::getName,
                                                           ROI::asDataObject,
                                                           (x1, x2) -> x1));

        Map<String, ROIData> label2roi = ijRois.stream()
                                               .map(r -> new SimpleEntry<>(
                                                       r.getProperty(roiProperty),
                                                       safeParseLong(r.getProperty(roiIdProperty))))
                                               .filter(p -> p.getKey() != null)
                                               .filter(p -> p.getValue() != null)
                                               .collect(HashMap::new,
                                                        (m, v) -> m.put(v.getKey(),
                                                                        id2roi.get(v.getValue())),
                                                        HashMap::putAll);

        Map<String, ROIData> shape2roi = ijRois.stream()
                                               .map(r -> new SimpleEntry<>(r.getName(),
                                                                           safeParseLong(
                                                                                   r.getProperty(
                                                                                           roiIdProperty))))
                                               .filter(p -> p.getKey() != null)
                                               .filter(p -> p.getValue() != null)
                                               .collect(HashMap::new,
                                                        (m, v) -> m.put(v.getKey(),
                                                                        id2roi.get(v.getValue())),
                                                        HashMap::putAll);
        String colToDelete = "";
        if (results.columnExists(roiIdProperty)) {
            Variable[] roiCol = results.getColumnAsVariables(roiIdProperty);
            roiColumn   = idColumnToROIColumn(roiCol, id2roi);
            colToDelete = roiIdProperty;
        }
        if (roiColumn.length == 0 && results.columnExists(roiProperty)) {
            Variable[] roiCol = results.getColumnAsVariables(roiProperty);
            roiColumn   = propertyColumnToROIColumn(roiCol, id2roi, label2roi, name2roi, shape2roi);
            colToDelete = roiProperty;
        }
        if (roiColumn.length != 0 && !colToDelete.isEmpty()) {
            results.deleteColumn(colToDelete);
        }

        String[] headings = results.getHeadings();
        if (roiColumn.length == 0 && Arrays.asList(headings).contains(LABEL)) {
            Variable[] roiCol = results.getColumnAsVariables(LABEL);
            roiColumn = labelColumnToROIColumn(roiCol,
                                               shape2roi,
                                               (m, s) -> m.keySet()
                                                          .stream()
                                                          .filter(s::contains)
                                                          .findFirst()
                                                          .orElse(null));
            if (roiColumn.length == 0) {
                roiColumn = labelColumnToROIColumn(roiCol,
                                                   name2roi,
                                                   (m, s) -> m.keySet()
                                                              .stream()
                                                              .filter(s::contains)
                                                              .findFirst()
                                                              .orElse(null));
            }
        }
        return roiColumn;
    }


    /**
     * Safely converts a String to a Long, returning null if it fails.
     *
     * @param s The string.
     *
     * @return The integer value represented by s, null if not applicable.
     */
    private static Long safeParseLong(String s) {
        Long l = null;
        if (s != null) {
            try {
                l = Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                // DO NOTHING
            }
        }
        return l;
    }


    /**
     * Creates a ROIData column from a Variable column containing either:
     * <ul>
     *     <li>The ROI local indices/labels (assumed by default)</li>
     *     <li>The ROI OMERO IDs (checked if labels do not map)</li>
     *     <li>The ROI names (checked if IDs do not map or if the column contains Strings)</li>
     *     <li>The ShapeData names (checked if the column contains Strings and ROI names do not map)</li>
     * </ul>
     *
     * @param roiCol        Variable column containing ROI info
     * @param label2roi     ROI local labels/IDs map
     * @param id2roi        ROI IDs map
     * @param name2roi      ROI names map
     * @param shapeName2roi ROI shape names map
     *
     * @return A ROIData column.
     */
    private static ROIData[] propertyColumnToROIColumn(Variable[] roiCol,
                                                       Map<Long, ROIData> id2roi,
                                                       Map<String, ROIData> label2roi,
                                                       Map<String, ROIData> name2roi,
                                                       Map<String, ROIData> shapeName2roi) {
        ROIData[] roiColumn;
        if (isColumnNumeric(roiCol)) {
            roiColumn = numericColumnToROIColumn(roiCol, label2roi);
            if (roiColumn.length == 0) {
                roiColumn = idColumnToROIColumn(roiCol, id2roi);
            }
            if (roiColumn.length == 0) {
                roiColumn = numericColumnToROIColumn(roiCol, name2roi);
            }
        } else {
            roiColumn = labelColumnToROIColumn(roiCol, name2roi, (m, s) -> s);
            if (roiColumn.length == 0) {
                roiColumn = labelColumnToROIColumn(roiCol, label2roi, (m, s) -> s);
            }
            if (roiColumn.length == 0) {
                roiColumn = labelColumnToROIColumn(roiCol, shapeName2roi, (m, s) -> s);
            }
        }
        return roiColumn;
    }


    /**
     * Creates a ROIData column from a Variable column containing the ROI OMERO IDs.
     *
     * @param roiCol Variable column containing ROI info
     * @param id2roi ROI IDs map
     *
     * @return A ROIData column.
     */
    private static ROIData[] idColumnToROIColumn(Variable[] roiCol, Map<Long, ROIData> id2roi) {
        ROIData[] roiColumn = Arrays.stream(roiCol)
                                    .map(Variable::getValue)
                                    .map(Double::longValue)
                                    .map(id2roi::get).toArray(ROIData[]::new);
        // If roiColumn contains null, we return an empty array
        if (Arrays.asList(roiColumn).contains(null)) {
            roiColumn = EMPTY_ROI;
        }
        return roiColumn;
    }


    /**
     * Creates a ROIData column from a Variable column containing the ROI OMERO IDs.
     *
     * @param roiCol Variable column containing ROI info
     * @param id2roi ROI IDs map
     *
     * @return A ROIData column.
     */
    private static ROIData[] numericColumnToROIColumn(Variable[] roiCol,
                                                      Map<String, ROIData> id2roi) {
        ROIData[] roiColumn = Arrays.stream(roiCol)
                                    .map(Variable::toString)
                                    .map(id2roi::get).toArray(ROIData[]::new);
        // If roiColumn contains null, we return an empty array
        if (Arrays.asList(roiColumn).contains(null)) {
            roiColumn = EMPTY_ROI;
        }
        return roiColumn;
    }


    /**
     * Creates a ROIData column from a Variable column containing the ROI OMERO IDs.
     *
     * @param roiCol    Variable column containing ROI info
     * @param label2roi ROI shape names map
     *
     * @return A ROIData column.
     */
    private static ROIData[] labelColumnToROIColumn(Variable[] roiCol,
                                                    Map<String, ROIData> label2roi,
                                                    BiFunction<? super Map<String, ROIData>, ? super String, String> filter) {
        ROIData[] roiColumn = Arrays.stream(roiCol)
                                    .map(Variable::getString)
                                    .map(s -> filter.apply(label2roi, s))
                                    .map(label2roi::get)
                                    .toArray(ROIData[]::new);
        // If roiColumn contains null, we return an empty array
        if (Arrays.asList(roiColumn).contains(null)) {
            roiColumn = EMPTY_ROI;
        }
        return roiColumn;
    }

}