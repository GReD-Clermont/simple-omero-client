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

import java.util.Comparator;

import fr.igred.omero.ImageContainer;

/**
 * Class used to sort ImageContainers
 */
public class SortImageContainer implements Comparator<ImageContainer> {
    /**
     * Compare 2 imageContainer.
     * Compare the id of the imageContainer.
     * 
     * @param img1 First image to compare
     * @param img2 Second image to comapre
     * 
     * @return 
     *      -1 if the id of img1 is lower than the id img2.
     *      0  if the ids are the same.
     *      1  if the id of img1 is greater than the id of img2.
     */
    public int compare(ImageContainer img1, 
                       ImageContainer img2)
    {
        return Long.compare(img1.getId(), img2.getId());
    } 
}