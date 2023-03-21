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

package fr.igred.omero.containers;


import fr.igred.omero.AnnotatableWrapper;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.client.Browser;
import fr.igred.omero.client.DataManager;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ExceptionHandler;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.ROIWrapper;
import omero.gateway.facility.ROIFacility;
import omero.gateway.model.DataObject;
import omero.gateway.model.FolderData;
import omero.gateway.model.ROIResult;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.RemoteObject.distinct;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;


/**
 * Class containing a FolderData object.
 * <p> Wraps function calls to the FolderData contained.
 */
public class FolderWrapper extends AnnotatableWrapper<FolderData> implements Folder {

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
    public FolderWrapper(omero.model.Folder folder) {
        super(new FolderData(folder));
    }


    /**
     * Constructor of the FolderWrapper class. Save the folder in OMERO
     *
     * @param dm   The data manager.
     * @param name Name of the folder.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public FolderWrapper(DataManager dm, String name)
    throws ServiceException, AccessException, ExecutionException {
        super(new FolderData());
        data.setName(name);
        omero.model.Folder f = (omero.model.Folder) dm.save(data.asFolder());
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
    @Override
    public Folder getParent() {
        return new FolderWrapper(data.getParentFolder());
    }


    /**
     * Sets the parent folder for this folder.
     *
     * @param folder The new parent folder.
     */
    @Override
    public void setParent(Folder folder) {
        data.setParentFolder(folder.asDataObject().asFolder());
    }


    /**
     * Adds a child folder to this folder.
     *
     * @param folder The new child folder.
     */
    @Override
    public void addChild(Folder folder) {
        data.asFolder().addChildFolders(folder.asDataObject().asFolder());
    }


    /**
     * Adds children folders to this folder.
     *
     * @param folders The new children folders.
     */
    @Override
    public void addChildren(Collection<? extends Folder> folders) {
        data.asFolder().addAllChildFoldersSet(folders.stream()
                                                     .map(Folder::asDataObject)
                                                     .map(DataObject::asFolder)
                                                     .collect(toList()));
    }


    /**
     * Retrieves the children folders for this folder.
     *
     * @return See above
     */
    @Override
    public List<Folder> getChildren() {
        return wrap(data.copyChildFolders(), FolderWrapper::new);
    }


    /**
     * Retrieves the images contained in this folder.
     *
     * @return See above
     */
    @Override
    public List<Image> getImages() {
        return wrap(data.copyImageLinks(), ImageWrapper::new);
    }


    /**
     * Gets the ROIs contained in the folder associated with the provided image ID.
     *
     * @param dm      The data manager.
     * @param imageId The image.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public List<ROI> getROIs(DataManager dm, long imageId)
    throws ServiceException, AccessException, ExecutionException {
        ROIFacility roiFac = dm.getRoiFacility();

        String error = "Cannot get ROIs from " + this;
        Collection<ROIResult> results = ExceptionHandler.of(roiFac,
                                                            rf -> rf.loadROIsForFolder(dm.getCtx(),
                                                                                       imageId,
                                                                                       data.getId()))
                                                        .handleOMEROException(error)
                                                        .get();

        List<ROIWrapper> rois = results.stream()
                                       .map(ROIResult::getROIs)
                                       .flatMap(Collection::stream)
                                       .map(ROIWrapper::new)
                                       .sorted(comparing(RemoteObject::getId))
                                       .collect(toList());

        return distinct(rois);
    }


    /**
     * Unlink all ROIs associated to this folder.
     * <p> The folder must be loaded beforehand. </p>
     * <p> ROIs are now linked to their images directly.</p>
     *
     * @param dm The data manager.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    @Override
    public void unlinkAllROIs(DataManager dm)
    throws ServiceException, AccessException, ExecutionException {
        Collection<ROIWrapper> rois = wrap(data.copyROILinks(), ROIWrapper::new);
        unlinkROIs(dm, rois.toArray(EMPTY_ROI_ARRAY));
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
        data = ExceptionHandler.of(browser.getBrowseFacility(),
                                   bf -> bf.loadFolders(browser.getCtx(), singletonList(getId())))
                               .handleOMEROException("Cannot reload " + this)
                               .get()
                               .iterator()
                               .next();
    }

}