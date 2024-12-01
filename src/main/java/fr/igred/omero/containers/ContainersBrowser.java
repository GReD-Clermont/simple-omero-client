package fr.igred.omero.containers;


import fr.igred.omero.client.BasicBrowser;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.meta.Experimenter;
import omero.RLong;
import omero.model.IObject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;


public interface ContainersBrowser extends BasicBrowser {

    /**
     * Gets the project with the specified id from OMERO.
     *
     * @param id ID of the project.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default Project getProject(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Project> projects = getProjects(id);
        if (projects.isEmpty()) {
            String msg = format("Project %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
        }
        return projects.iterator().next();
    }


    /**
     * Gets the projects with the specified ids from OMERO.
     *
     * @param ids Project IDs
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Project> getProjects(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all projects available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Project> getProjects()
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all projects available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Project> getProjects(Experimenter experimenter)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all projects with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Project> getProjects(String name)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the dataset with the specified id from OMERO.
     *
     * @param id ID of the dataset.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default Dataset getDataset(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Dataset> datasets = getDatasets(id);
        if (datasets.isEmpty()) {
            String msg = format("Dataset %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
        }
        return datasets.iterator().next();
    }


    /**
     * Gets the datasets with the specified ids from OMERO.
     *
     * @param ids Dataset IDs
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Dataset> getDatasets(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all datasets available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Dataset> getDatasets()
    throws ServiceException, AccessException, ExecutionException {
        Long[] ids = this.findByQuery("select d from Dataset d")
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getDatasets(ids);
    }


    /**
     * Gets all datasets available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Dataset> getDatasets(Experimenter experimenter)
    throws ServiceException, AccessException, ExecutionException {
        String template = "select d from Dataset d where d.details.owner.id=%d";
        String query    = format(template, experimenter.getId());
        Long[] ids = this.findByQuery(query)
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getDatasets(ids);
    }


    /**
     * Gets all datasets with a certain name from OMERO.
     *
     * @param name Name searched.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Dataset> getDatasets(String name)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all orphaned datasets available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Dataset> getOrphanedDatasets(Experimenter experimenter)
    throws ServiceException, ExecutionException, AccessException {
        String template = "select dataset from Dataset as dataset" +
                          " join fetch dataset.details.owner as o" +
                          " where o.id = %d" +
                          " and not exists" +
                          " (select obl from ProjectDatasetLink as obl" +
                          " where obl.child = dataset.id)";
        String query = format(template, experimenter.getId());
        Long[] ids = this.findByQuery(query)
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getDatasets(ids);
    }


    /**
     * Gets all orphaned datasets available from OMERO owned by the current user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Dataset> getOrphanedDatasets()
    throws ServiceException, ExecutionException, AccessException;


    /**
     * Gets the folder with the specified ID from OMERO, fully loaded.
     *
     * @param id ID of the folder.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default Folder getFolder(long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Folder> folders = getFolders(id);
        if (folders.isEmpty()) {
            String msg = format("Folder %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
        }
        return folders.iterator().next();
    }


    /**
     * Gets all folders available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Folder> getFolders()
    throws ExecutionException, AccessException, ServiceException;


    /**
     * Gets all the folders owned by a given user from OMERO.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Folder> getFolders(Experimenter experimenter)
    throws ExecutionException, AccessException, ServiceException;


    /**
     * Gets the folders with the specified IDs from OMERO (fully loaded).
     *
     * @param ids Project IDs
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Folder> getFolders(Long... ids)
    throws ServiceException, AccessException, ExecutionException;

}
