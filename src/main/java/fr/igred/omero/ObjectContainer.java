package fr.igred.omero;


import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.annotations.TableContainer;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DataObject;
import omero.gateway.model.TableData;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;


public abstract class ObjectContainer<T extends DataObject> {

    protected T data;


    protected ObjectContainer(T object) {
        this.data = object;
    }


    /**
     * Gets the wrapped object
     *
     * @return ObjectData contained.
     */
    protected T getObject() {
        return data;
    }


    /**
     * Gets the object id
     *
     * @return id.
     */
    public Long getId() {
        return data.getId();
    }


    /**
     * Gets the object creation date
     *
     * @return creation date.
     */
    public Timestamp getCreated() {
        return data.getCreated();
    }


    /**
     * Gets the owner ID
     *
     * @return owner id.
     */
    public Long getOwnerId() {
        return data.getOwner().getId();
    }


    /**
     * Gets the group ID
     *
     * @return group id.
     */
    public Long getGroupId() {
        return data.getGroupId();
    }


    /**
     * Adds a table to the object in OMERO
     *
     * @param client The user.
     * @param table  Table to add to the dataobject.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTable(Client client, TableContainer table)
    throws ServiceException, AccessException, ExecutionException {
        TableData tableData = table.createTable();
        try {
            tableData = client.getTablesFacility().addTable(client.getCtx(), data, table.getName(), tableData);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot add table to " + data.getClass().getSimpleName() + " ID: " + getId());
        }
        table.setFileId(tableData.getOriginalFileId());
    }


    /**
     * Gets a certain table linked to the object in OMERO
     *
     * @param client The user.
     * @param fileId FileId of the table researched.
     *
     * @return TableContainer containing the table information.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public TableContainer getTable(Client client, Long fileId)
    throws ServiceException, AccessException, ExecutionException {
        TableData table = null;
        try {
            table = client.getTablesFacility().getTable(client.getCtx(), fileId);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get table from " + data.getClass().getSimpleName() + " ID: " + getId());
        }
        return new TableContainer(Objects.requireNonNull(table));
    }


}
