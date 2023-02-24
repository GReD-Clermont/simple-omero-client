/*
 *  Copyright (C) 2020-2023 GReD
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


import fr.igred.omero.client.Client;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
import fr.igred.omero.exception.ServiceException;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.FileAnnotationData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Class containing a FileAnnotationData object.
 * <p> Wraps function calls to the FileAnnotationData contained.
 */
public class FileAnnotationWrapper extends AnnotationWrapper<FileAnnotationData> {

    /**
     * Constructor of the AnnotationWrapper class.
     *
     * @param annotation Annotation to be contained.
     */
    public FileAnnotationWrapper(FileAnnotationData annotation) {
        super(annotation);
    }


    /**
     * Writes this file annotation to the specified {@link FileOutputStream}.
     *
     * @param client The client handling the connection.
     * @param stream The {@link FileOutputStream} where the data will be written.
     *
     * @return The {@link RawFileStorePrx} used to read the file annotation.
     *
     * @throws ServerError             Server error.
     * @throws DSOutOfServiceException Cannot connect to OMERO.
     * @throws IOException             Cannot write to the file.
     */
    private RawFileStorePrx writeFile(Client client, FileOutputStream stream)
    throws ServerError, DSOutOfServiceException, IOException {
        final int inc = 262144;

        RawFileStorePrx store = client.getGateway().getRawFileService(client.getCtx());
        store.setFileId(this.getFileID());

        long size = getFileSize();
        long offset;
        for (offset = 0; offset + inc < size; offset += inc) {
            stream.write(store.read(offset, inc));
        }
        stream.write(store.read(offset, (int) (size - offset)));
        return store;
    }


    /**
     * Returns the format of the original file.
     *
     * @return See above.
     */
    public String getOriginalMimetype() {
        return data.getOriginalMimetype();
    }


    /**
     * Returns the file format as defined by the specification, corresponding to the file extension.
     *
     * @return See above.
     */
    public String getServerFileMimetype() {
        return data.getServerFileMimetype();
    }


    /**
     * Returns the format of the uploaded file.
     *
     * @return See above.
     */
    public String getFileFormat() {
        return data.getFileFormat();
    }


    /**
     * Returns a user readable description of the file.
     *
     * @return See above.
     */
    public String getFileKind() {
        return data.getFileKind();
    }


    /**
     * Returns the file to upload to the server.
     *
     * @return See above.
     */
    public File getAttachedFile() {
        return data.getAttachedFile();
    }


    /**
     * Returns the name of the file.
     *
     * @return See above.
     */
    public String getFileName() {
        return data.getFileName();
    }


    /**
     * Returns the absolute path to the file.
     *
     * @return See above.
     */
    public String getFilePath() {
        return data.getFilePath();
    }


    /**
     * Returns the size of the file.
     *
     * @return See above.
     */
    public long getFileSize() {
        return data.getFileSize();
    }


    /**
     * Returns the id of the file.
     *
     * @return See above.
     */
    public long getFileID() {
        return data.getFileID();
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
    public File getFile(Client client, String path) throws IOException, ServiceException, ServerException {
        File file = new File(path);

        RawFileStorePrx store;
        try (FileOutputStream stream = new FileOutputStream(file)) {
            store = ExceptionHandler.of(client, c -> writeFile(c, stream))
                                    .handleServiceOrServer("Could not create RawFileService")
                                    .rethrow(IOException.class)
                                    .get();
        }

        if (store != null) {
            ExceptionHandler.ofConsumer(store, RawFileStorePrx::close)
                            .rethrow(ServerError.class, ServerException::new, "Could not close RawFileService")
                            .rethrow();
        }

        return file;
    }


    /**
     * Returns the absolute path to the file
     *
     * @return See above.
     *
     * @see AnnotationData#getContentAsString()
     */
    public String getContentAsString() {
        return data.getContentAsString();
    }


    /**
     * Returns {@code true} if it is a movie file. {@code false} otherwise.
     *
     * @return See above.
     */
    public boolean isMovieFile() {
        return data.isMovieFile();
    }

}
