/*
 *  Copyright (C) 2020-2023 GReD
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


import fr.igred.omero.Client;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.FileAnnotationData;

import java.io.File;
import java.io.IOException;


public interface FileAnnotation extends Annotation<FileAnnotationData> {

    /**
     * Returns the format of the original file.
     *
     * @return See above.
     */
    default String getOriginalMimetype() {
        return asDataObject().getOriginalMimetype();
    }


    /**
     * Returns the file format as defined by the specification, corresponding to the file extension.
     *
     * @return See above.
     */
    default String getServerFileMimetype() {
        return asDataObject().getServerFileMimetype();
    }


    /**
     * Returns the format of the uploaded file.
     *
     * @return See above.
     */
    default String getFileFormat() {
        return asDataObject().getFileFormat();
    }


    /**
     * Returns a user readable description of the file.
     *
     * @return See above.
     */
    default String getFileKind() {
        return asDataObject().getFileKind();
    }


    /**
     * Returns the file to upload to the server.
     *
     * @return See above.
     */
    default File getAttachedFile() {
        return asDataObject().getAttachedFile();
    }


    /**
     * Returns the name of the file.
     *
     * @return See above.
     */
    default String getFileName() {
        return asDataObject().getFileName();
    }


    /**
     * Returns the absolute path to the file.
     *
     * @return See above.
     */
    default String getFilePath() {
        return asDataObject().getFilePath();
    }


    /**
     * Returns the size of the file.
     *
     * @return See above.
     */
    default long getFileSize() {
        return asDataObject().getFileSize();
    }


    /**
     * Returns the id of the file.
     *
     * @return See above.
     */
    default long getFileID() {
        return asDataObject().getFileID();
    }


    /**
     * Returns the original file.
     *
     * @param client The client handling the connection.
     * @param path   The path where the file will be saved.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws IOException      Cannot write to the file.
     * @throws ServerException  Server error.
     */
    File getFile(Client client, String path) throws IOException, ServiceException, ServerException;


    /**
     * Returns the absolute path to the file
     *
     * @return See above.
     *
     * @see AnnotationData#getContentAsString()
     */
    default String getContentAsString() {
        return asDataObject().getContentAsString();
    }


    /**
     * Returns {@code true} if it is a movie file. {@code false} otherwise.
     *
     * @return See above.
     */
    default boolean isMovieFile() {
        return asDataObject().isMovieFile();
    }

}
