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


import fr.igred.omero.annotations.TagAnnotationWrapper;

import java.util.Comparator;


/**
 * Class used to sort TagAnnotationWrappers
 */
public class SortTagAnnotationWrapper implements Comparator<TagAnnotationWrapper> {

    /**
     * Compare 2 TagAnnotationWrappers. Compare the id of the TagAnnotationWrappers.
     *
     * @param tag1 First tag to compare.
     * @param tag2 Second tag to compare.
     *
     * @return -1 if the id of tag1 is lower than the id tag2. 0  if the ids are the same. 1  if the id of tag1 is.
     * greater than the id of tag2.
     */
    public int compare(TagAnnotationWrapper tag1, TagAnnotationWrapper tag2) {
        return Long.compare(tag1.getId(), tag2.getId());
    }

}