package fr.igred.omero;


import fr.igred.omero.annotations.TableWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.DataObject;
import omero.gateway.model.TableData;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static fr.igred.omero.exception.ExceptionHandler.handleServiceOrAccess;


public abstract class ObjectWrapper<T extends DataObject> {

    protected T data;


    /**
     * Constructor of the class ObjectWrapper.
     *
     * @param object The object contained in the ObjectWrapper.
     */
    protected ObjectWrapper(T object) {
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
     * @param table  Table to add to the object.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public void addTable(Client client, TableWrapper table)
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
     * @return TableWrapper containing the table information.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    public TableWrapper getTable(Client client, Long fileId)
    throws ServiceException, AccessException, ExecutionException {
        TableData table = null;
        try {
            table = client.getTablesFacility().getTable(client.getCtx(), fileId);
        } catch (DSOutOfServiceException | DSAccessException e) {
            handleServiceOrAccess(e, "Cannot get table from " + data.getClass().getSimpleName() + " ID: " + getId());
        }
        return new TableWrapper(Objects.requireNonNull(table));
    }


    /**
     * Class used to sort TagAnnotationWrappers
     */
    public static class SortById<U extends ObjectWrapper<?>> implements Comparator<U> {

        /**
         * Compare 2 ObjectWrappers. Compare the id of the ObjectWrappers.
         *
         * @param object1 First object to compare.
         * @param object2 Second object to compare.
         *
         * @return <ul><li>-1 if the id of object1 is lower than the id object2.</li>
         * <li>0  if the ids are the same.</li>
         * <li>1 if the id of object1 is greater than the id of object2.</li></ul>
         */
        public int compare(U object1, U object2) {
            return Long.compare(object1.getId(), object2.getId());
        }

    }

}
