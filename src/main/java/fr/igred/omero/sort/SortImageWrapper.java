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

package fr.igred.omero.sort;


import fr.igred.omero.repository.ImageWrapper;

import java.util.Comparator;


/**
 * Class used to sort ImageWrappers
 */
public class SortImageWrapper implements Comparator<ImageWrapper> {

    /**
     * Compare 2 imageWrapper. Compare the id of the imageWrapper.
     *
     * @param img1 First image to compare.
     * @param img2 Second image to compare.
     *
     * @return -1 if the id of img1 is lower than the id img2. 0  if the ids are the same. 1  if the id of img1 is.
     * greater than the id of img2.
     */
    public int compare(ImageWrapper img1, ImageWrapper img2) {
        return Long.compare(img1.getId(), img2.getId());
    }

}