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
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServerException;
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
import omero.ServerError;
import omero.gateway.model.DataObject;
import omero.gateway.util.PojoMapper;
import omero.model.Pixels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


/**
 * Generic class containing a DataObject (or a subclass) object.
 *
 * @param <T> Subclass of {@link DataObject}
 */
public abstract class RepositoryObjectWrapper<T extends DataObject>
        extends AnnotatableWrapper<T>
        implements RepositoryObject {

    /**
     * Constructor of the class RepositoryObjectWrapper.
     *
     * @param o The DataObject to wrap in the RepositoryObjectWrapper.
     */
    protected RepositoryObjectWrapper(T o) {
        super(o);
    }


    /**
     * Imports all images candidates in the paths to the target in OMERO.
     *
     * @param client The client handling the connection.
     * @param target The import target.
     * @param paths  Paths to the image files on the computer.
     *
     * @return If the import did not exit because of an error.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     * @throws IOException      Cannot read file.
     */
    protected static boolean importImages(ConnectionHandler client, DataObject target, String... paths)
    throws ServiceException, ServerException, IOException {
        boolean success;

        ImportConfig config = new ImportConfig();
        String       type   = PojoMapper.getGraphType(target.getClass());
        config.target.set(type + ":" + target.getId());
        config.username.set(client.getUser().getUserName());
        config.email.set(client.getUser().getEmail());

        OMEROMetadataStoreClient store = client.getImportStore();
        try (OMEROWrapper reader = new OMEROWrapper(config)) {
            ExceptionHandler.ofConsumer(store,
                                        s -> s.logVersionInfo(config.getIniVersionNumber()))
                            .rethrow(ServerError.class, ServerException::new,
                                     "Cannot log version information during import.")
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
     * @param target The import target.
     * @param path   Path to the image file on the computer.
     *
     * @return The list of IDs of the newly imported images.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws ServerException  Server error.
     */
    protected static List<Long> importImage(ConnectionHandler client, DataObject target, String path)
    throws ServiceException, ServerException {
        ImportConfig config = new ImportConfig();
        String       type   = PojoMapper.getGraphType(target.getClass());
        config.target.set(type + ":" + target.getId());
        config.username.set(client.getUser().getUserName());
        config.email.set(client.getUser().getEmail());

        Collection<Pixels> pixels = new ArrayList<>(1);

        OMEROMetadataStoreClient store = client.getImportStore();
        try (OMEROWrapper reader = new OMEROWrapper(config)) {
            store.logVersionInfo(config.getIniVersionNumber());
            reader.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.ALL));

            ImportLibrary library = new ImportLibrary(store, reader);
            library.addObserver(new LoggingImportMonitor());

            ErrorHandler handler = new ErrorHandler(config);

            ImportCandidates candidates = new ImportCandidates(reader, new String[]{path}, handler);

            ExecutorService uploadThreadPool = Executors.newFixedThreadPool(config.parallelUpload.get());

            List<ImportContainer> containers = candidates.getContainers();
            if (containers != null) {
                for (int i = 0; i < containers.size(); i++) {
                    ImportContainer container = containers.get(i);
                    container.setTarget(target.asIObject());
                    List<Pixels> imported = library.importImage(container, uploadThreadPool, i);
                    pixels.addAll(imported);
                }
            }
            uploadThreadPool.shutdown();
        } catch (Throwable e) {
            throw new ServerException("Error during image import.", e);
        } finally {
            store.logout();
        }

        List<Long> ids = new ArrayList<>(pixels.size());
        pixels.forEach(pix -> ids.add(pix.getImage().getId().getValue()));
        return ids.stream().distinct().collect(Collectors.toList());
    }

}
