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


import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.model.FileAnnotationData;

import java.io.File;
import java.io.IOException;


/**
 * Interface to handle File Annotations on OMERO.
 */
public interface FileAnnotation extends Annotation {

    /**
     * Returns an {@link FileAnnotationData} corresponding to the handled object.
     *
     * @return See above.
     */
    @Override
    FileAnnotationData asDataObject();


    /**
     * Returns the format of the original file.
     *
     * @return See above.
     */
    String getOriginalMimetype();


    /**
     * Returns the file format as defined by the specification, corresponding to the file extension.
     *
     * @return See above.
     */
    String getServerFileMimetype();


    /**
     * Returns the format of the uploaded file.
     *
     * @return See above.
     */
    String getFileFormat();


    /**
     * Returns a user readable description of the file.
     *
     * @return See above.
     */
    String getFileKind();


    /**
     * Returns the file to upload to the server.
     *
     * @return See above.
     */
    File getAttachedFile();


    /**
     * Returns the name of the file.
     *
     * @return See above.
     */
    String getFileName();


    /**
     * Returns the absolute path to the file.
     *
     * @return See above.
     */
    String getFilePath();


    /**
     * Returns the size of the file.
     *
     * @return See above.
     */
    long getFileSize();


    /**
     * Returns the ID of the file.
     *
     * @return See above.
     */
    long getFileID();


    /**
     * Returns the original file.
     *
     * @param conn The connection handler.
     * @param path The path where the file will be saved.
     *
     * @return See above.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot write to the file.
     */
    File getFile(ConnectionHandler conn, String path)
    throws ServiceException, AccessException, IOException;


    /**
     * Returns the absolute path to the file
     *
     * @return See above.
     *
     * @see FileAnnotationData#getContentAsString()
     */
    String getContentAsString();


    /**
     * Returns {@code true} if it is a movie file. {@code false} otherwise.
     *
     * @return See above.
     */
    boolean isMovieFile();

}
