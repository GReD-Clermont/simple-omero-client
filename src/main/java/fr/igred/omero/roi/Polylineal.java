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

package fr.igred.omero.roi;


import java.awt.geom.Point2D;
import java.util.List;


/**
 * Interface for polygonal shapes.
 */
public interface Polylineal {

    /**
     * Returns the points in the polygonal shape.
     *
     * @return See above.
     */
    List<Point2D.Double> getPoints();


    /**
     * Sets the points in the polygonal shape.
     *
     * @param points The points to set.
     */
    void setPoints(List<Point2D.Double> points);

}
