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

package fr.igred.omero;


import fr.igred.omero.client.ConnectionHandler;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.cli.ErrorHandler;
import ome.formats.importer.cli.LoggingImportMonitor;
import omero.gateway.model.DataObject;
import omero.gateway.util.PojoMapper;
import omero.model.Pixels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class ImportWrapper<T extends DataObject> extends AnnotatableWrapper<T> implements RepositoryObject {

    /**
     * Constructor of the class ImportWrapper.
     *
     * @param o The DataObject to wrap in the ImportWrapper.
     */
    protected ImportWrapper(T o) {
        super(o);
    }


    /**
     * Method used for importing a number of import candidates.
     *
     * @param library    The importer.
     * @param config     The configuration information.
     * @param candidates Hosts information about the files to import.
     *
     * @return The list of imported pixels.
     */
    private List<Pixels> importCandidates(ImportLibrary library, ImportConfig config, ImportCandidates candidates) {
        List<Pixels> pixels = new ArrayList<>(0);

        ExecutorService uploadThreadPool = Executors.newFixedThreadPool(config.parallelUpload.get());

        List<ImportContainer> containers = candidates.getContainers();
        if (containers != null) {
            pixels = new ArrayList<>(containers.size());
            for (int i = 0; i < containers.size(); i++) {
                ImportContainer container = containers.get(i);
                container.setTarget(data.asIObject());
                List<Pixels> imported = new ArrayList<>(1);
                try {
                    imported = library.importImage(container, uploadThreadPool, i);
                } catch (Throwable e) {
                    String error = String.format("Error during image import for: %s", container.getFile().getName());
                    Logger.getLogger(getClass().getName()).severe(error);
                    if (Boolean.FALSE.equals(config.contOnError.get())) return pixels;
                }
                pixels.addAll(imported);
            }
        }
        uploadThreadPool.shutdown();
        return pixels;
    }


    /**
     * Imports all images candidates in the paths to the target in OMERO.
     *
     * @param client The client handling the connection.
     * @param paths  Paths to the image files on the computer.
     *
     * @return If the import did not exit because of an error.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot read file.
     */
    protected boolean importImages(ConnectionHandler client, String... paths)
    throws ServiceException, AccessException, IOException {
        boolean success;

        ImportConfig config = new ImportConfig();
        String       type   = PojoMapper.getGraphType(data.getClass());
        config.target.set(type + ":" + data.getId());
        config.username.set(client.getUser().getUserName());
        config.email.set(client.getUser().getEmail());

        OMEROMetadataStoreClient store = client.getImportStore();
        try (OMEROWrapper reader = new OMEROWrapper(config)) {
            ExceptionHandler.ofConsumer(store, s -> s.logVersionInfo(config.getIniVersionNumber()))
                            .handleServerError("Cannot log version information during import.")
                            .rethrow();
            reader.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.ALL));

            ImportLibrary library = new ImportLibrary(store, reader);
            library.addObserver(new LoggingImportMonitor());

            ErrorHandler handler = new ErrorHandler(config);

            ImportCandidates candidates = new ImportCandidates(reader, paths, handler);
            success = library.importCandidates(config, candidates);
        } finally {
            store.logout();
        }

        return success;
    }


    /**
     * Imports one image file to the target in OMERO.
     *
     * @param client The client handling the connection.
     * @param path   Path to the image file on the computer.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     * @throws IOException      Cannot read file.
     */
    protected List<Long> importImage(ConnectionHandler client, String path)
    throws ServiceException, AccessException, IOException {
        ImportConfig config = new ImportConfig();
        String       type   = PojoMapper.getGraphType(data.getClass());
        config.target.set(type + ":" + data.getId());
        config.username.set(client.getUser().getUserName());
        config.email.set(client.getUser().getEmail());

        Collection<Pixels> pixels;

        OMEROMetadataStoreClient store = client.getImportStore();
        try (OMEROWrapper reader = new OMEROWrapper(config)) {
            ExceptionHandler.ofConsumer(store, s -> s.logVersionInfo(config.getIniVersionNumber()))
                            .handleServerError("Cannot log version information during import.")
                            .rethrow();
            reader.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.ALL));

            ImportLibrary library = new ImportLibrary(store, reader);
            library.addObserver(new LoggingImportMonitor());

            ErrorHandler handler = new ErrorHandler(config);

            ImportCandidates candidates = new ImportCandidates(reader, new String[]{path}, handler);
            pixels = importCandidates(library, config, candidates);
        } finally {
            store.logout();
        }

        return pixels.stream()
                     .map(pix -> pix.getImage().getId().getValue())
                     .distinct()
                     .collect(Collectors.toList());
    }

}
