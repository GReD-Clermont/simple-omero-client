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

package fr.igred.omero.repository;


import fr.igred.omero.Browser;
import fr.igred.omero.Client;
import fr.igred.omero.GenericObjectWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROIWrapper;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIData;
import omero.gateway.model.ROIResult;
import omero.model.Folder;
import omero.model.FolderAnnotationLink;
import omero.model.FolderAnnotationLinkI;
import omero.model.FolderImageLink;
import omero.model.FolderImageLinkI;
import omero.model.IObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.call;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;


/**
 * Class containing a FolderData object.
 * <p> Wraps function calls to the FolderData contained.
 */
public class FolderWrapper extends GenericRepositoryObjectWrapper<FolderData> {

    /** Annotation link name for this type of object */
    public static final String ANNOTATION_LINK = "FolderAnnotationLink";

    /** Empty ROI array for fast list conversion */
    private static final ROIWrapper[] EMPTY_ROI_ARRAY = new ROIWrapper[0];


    /**
     * Constructor of the FolderWrapper class.
     *
     * @param folder The FolderData to wrap.
     */
    public FolderWrapper(FolderData folder) {
        super(folder);
    }


    /**
     * Constructor of the FolderWrapper class.
     *
     * @param folder Folder to contain.
     */
    public FolderWrapper(Folder folder) {
        super(new FolderData(folder));
    }


    /**
     * Constructor of the FolderWrapper class. Save the folder in OMERO
     *
     * @param client The client handling the connection.
     * @param name   Name of the folder.
     *
     * @throws ServiceException Cannot connect to OMERO.
     * @throws AccessException  Cannot access data.
     */
    public FolderWrapper(Client client, String name)
    throws ServiceException, AccessException {
        super(new FolderData());
        data.setName(name);
        Folder f = (Folder) ExceptionHandler.of(client.getGateway(),
                                                g -> g.getUpdateService(client.getCtx())
                                                      .saveAndReturnObject(data.asIObject()))
                                            .handleServerAndService("Could not create Folder with name: " + name)
                                            .get();
        data.setFolder(f);
    }


    /**
     * Returns the type of annotation link for this object.
     *
     * @return See above.
     */
    @Override
    protected String annotationLinkType() {
        return ANNOTATION_LINK;
    }


    /**
     * Adds an annotation to the object in OMERO, if possible.
     *
     * @param client     The client handling the connection.
     * @param annotation Annotation to be added.
     * @param <A>        The type of the annotation.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    protected <A extends AnnotationData> void link(Client client, A annotation)
    throws ServiceException, AccessException, ExecutionException {
        FolderAnnotationLink link = new FolderAnnotationLinkI();
        link.setChild(annotation.asAnnotation());
        link.setParent(data.asFolder());
        client.save(link);
    }


    /**
     * Gets the name of the folder
     *
     * @return name.
     */
    @Override
    public String getName() {
        return data.getName();
    }


    /**
     * Sets the name of the folder.
     *
     * @param name The name of the folder. Mustn't be {@code null}.
     *
     * @throws IllegalArgumentException If the name is {@code null}.
     */
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Gets the folder description
     *
     * @return The folder description.
     */
    @Override
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the folder.
     *
     * @param description The folder description.
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }


    /**
     * Retrieves the parent folders for this folder.
     *
     * @return See above
     */
    public FolderWrapper getParent() {
        return new FolderWrapper(data.getParentFolder());
    }


    /**
     * Sets the parent folder for this folder.
     *
     * @param folder The new parent folder.
     */
    public void setParent(FolderWrapper folder) {
        data.setParentFolder(folder.asDataObject().asFolder());
    }


    /**
     * Adds a child folder to this folder.
     *
     * @param folder The new child folder.
     */
    public void addChild(FolderWrapper folder) {
        data.asFolder().addChildFolders(folder.asDataObject().asFolder());
    }


    /**
     * Adds children folders to this folder.
     *
     * @param folders The new children folders.
     */
    public void addChildren(Collection<? extends FolderWrapper> folders) {
        data.asFolder()
            .addAllChildFoldersSet(folders.stream()
                                          .map(GenericObjectWrapper::asDataObject)
                                          .map(DataObject::asFolder)
                                          .collect(toList()));
    }


    /**
     * Retrieves the children folders for this folder.
     *
     * @return See above
     */
    public List<FolderWrapper> getChildren() {
        return wrap(data.copyChildFolders(), FolderWrapper::new);
    }


    /**
     * Links images to the folder in OMERO.
     *
     * @param client The client handling the connection.
     * @param images Images to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addImages(Client client, ImageWrapper... images)
    throws ServiceException, AccessException, ExecutionException {
        List<IObject> links = new ArrayList<>(images.length);
        List<Long> linkedIds = getImages().stream()
                                          .map(GenericObjectWrapper::getId)
                                          .collect(toList());
        for (ImageWrapper image : images) {
            if (!linkedIds.contains(image.getId())) {
                FolderImageLink link = new FolderImageLinkI();
                link.setChild(image.asDataObject().asImage());
                link.setParent(data.asFolder());
                links.add(link);
            }
        }
        ExceptionHandler.of(client.getDm(),
                            d -> d.saveAndReturnObject(client.getCtx(), links, null, null))
                        .handleOMEROException("Cannot save links.")
                        .rethrow();
    }


    /**
     * Retrieves the images contained in this folder.
     *
     * @return See above
     */
    public List<ImageWrapper> getImages() {
        return wrap(data.copyImageLinks(), ImageWrapper::new);
    }


    /**
     * Retrieves the images contained in this folder.
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ImageWrapper> getImages(Client client)
    throws AccessException, ServiceException, ExecutionException {
        reload(client);
        return getImages();
    }


    /**
     * Adds ROIs to the folder and associate them to the provided image ID.
     *
     * @param client  The client handling the connection.
     * @param imageId The image ID.
     * @param rois    ROIs to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    public void addROIs(Client client, long imageId, ROIWrapper... rois)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIData> roiData = Arrays.stream(rois)
                                      .map(GenericObjectWrapper::asDataObject)
                                      .collect(toList());
        ROIFacility roiFac = client.getRoiFacility();
        ExceptionHandler.of(roiFac,
                            rf -> rf.addRoisToFolders(client.getCtx(),
                                                      imageId,
                                                      roiData,
                                                      singletonList(data)))
                        .handleOMEROException("Cannot add ROIs to " + this)
                        .rethrow();
    }


    /**
     * Adds ROIs to the folder and associate them to the provided image.
     *
     * @param client The client handling the connection.
     * @param image  The image.
     * @param rois   ROIs to add.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException If the ROIFacility can't be retrieved or instantiated.
     */
    public void addROIs(Client client, ImageWrapper image, ROIWrapper... rois)
    throws ServiceException, AccessException, ExecutionException {
        addROIs(client, image.getId(), rois);
    }


    /**
     * Gets the ROIs contained in the folder associated with the provided image ID.
     *
     * @param client  The client handling the connection.
     * @param imageId The image.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ROIWrapper> getROIs(Client client, long imageId)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ROIResult> roiResults = call(client.getRoiFacility(),
                                                rf -> rf.loadROIsForFolder(client.getCtx(),
                                                                           imageId,
                                                                           data.getId()),
                                                "Cannot get ROIs from " + this);

        List<ROIWrapper> roiWrappers = roiResults.stream()
                                                 .map(ROIResult::getROIs)
                                                 .flatMap(Collection::stream)
                                                 .map(ROIWrapper::new)
                                                 .sorted(Comparator.comparing(GenericObjectWrapper::getId))
                                                 .collect(toList());

        return distinct(roiWrappers);
    }


    /**
     * Gets the ROIs contained in the folder associated with the provided image.
     *
     * @param client The client handling the connection.
     * @param image  The image.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public List<ROIWrapper> getROIs(Client client, ImageWrapper image)
    throws ServiceException, AccessException, ExecutionException {
        return getROIs(client, image.getId());
    }


    /**
     * Unlink all ROIs associated to the provided image ID from the folder.
     * <p> ROIs are now linked to the image directly.
     *
     * @param client  The client handling the connection.
     * @param imageId The image ID.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void unlinkAllROIs(Client client, long imageId)
    throws ServiceException, AccessException, ExecutionException {
        unlinkROIs(client, getROIs(client, imageId));
    }


    /**
     * Unlink all ROIs associated to the provided image from the folder.
     * <p> ROIs are now linked to the image directly.
     *
     * @param client The client handling the connection.
     * @param image  The image.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void unlinkAllROIs(Client client, ImageWrapper image)
    throws ServiceException, AccessException, ExecutionException {
        unlinkAllROIs(client, image.getId());
    }


    /**
     * Unlink all ROIs associated to this folder.
     * <p> The folder must be loaded beforehand. </p>
     * <p> ROIs are now linked to their images directly.</p>
     *
     * @param client The client handling the connection.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void unlinkAllROIs(Client client)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ROIWrapper> rois = wrap(data.copyROILinks(), ROIWrapper::new);
        unlinkROIs(client, rois.toArray(EMPTY_ROI_ARRAY));
    }


    /**
     * Unlink ROIs from the folder.
     * <p> The ROIs are now linked to the image directly.
     *
     * @param client The data manager.
     * @param rois   ROI to unlink.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void unlinkROIs(Client client, ROIWrapper... rois)
    throws ServiceException, AccessException, ExecutionException {
        unlinkROIs(client, Arrays.asList(rois));
    }


    /**
     * Unlink ROIs from the folder.
     * <p> The ROIs are now linked to the image directly.
     *
     * @param client The data manager.
     * @param rois   ROI to unlink.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void unlinkROIs(Client client, Collection<? extends ROIWrapper> rois)
    throws ServiceException, AccessException, ExecutionException {
        List<ROIData> roiData = rois.stream()
                                    .map(ROIWrapper::asDataObject)
                                    .collect(toList());
        ExceptionHandler.ofConsumer(client.getRoiFacility(),
                                    rf -> rf.removeRoisFromFolders(client.getCtx(),
                                                                   -1L,
                                                                   roiData,
                                                                   singletonList(data)))
                        .handleOMEROException("Cannot unlink ROI from " + this)
                        .rethrow();
    }


    /**
     * Reloads the folder from OMERO, to update all links.
     *
     * @param browser The data browser.
     *
     * @throws AccessException    Cannot access data.
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void reload(Browser browser)
    throws AccessException, ServiceException, ExecutionException {
        data = call(browser.getBrowseFacility(),
                    bf -> bf.loadFolders(browser.getCtx(),
                                         singletonList(getId()))
                            .iterator()
                            .next(),
                    "Cannot reload " + this);
    }

}