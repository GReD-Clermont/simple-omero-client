package fr.igred.omero.screen;


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


public interface ScreenBrowser extends BasicBrowser {

    /**
     * Gets the screen with the specified id from OMERO.
     *
     * @param id ID of the screen.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default Screen getScreen(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Screen> screens = getScreens(id);
        if (screens.isEmpty()) {
            String msg = format("Screen %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
        }
        return screens.iterator().next();
    }


    /**
     * Gets the screens with the specified ids from OMERO.
     *
     * @param ids A list of screen ids
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Screen> getScreens(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all screens available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Screen> getScreens()
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all screens available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Screen> getScreens(Experimenter experimenter)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets the plate with the specified id from OMERO.
     *
     * @param id ID of the plate.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default Plate getPlate(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Plate> plates = getPlates(id);
        if (plates.isEmpty()) {
            String msg = format("Plate %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
        }
        return plates.iterator().next();
    }


    /**
     * Gets the plates with the specified ids from OMERO.
     *
     * @param ids A list of plate ids
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Plate> getPlates(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all plates available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Plate> getPlates()
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all plates available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Plate> getPlates(Experimenter experimenter)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all orphaned plates available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Plate> getOrphanedPlates(Experimenter experimenter)
    throws ServiceException, ExecutionException, AccessException {
        String template = "select plate from Plate as plate" +
                          " join fetch plate.details.owner as o" +
                          " where o.id = %d" +
                          " and not exists" +
                          " (select obl from ScreenPlateLink as obl" +
                          " where obl.child = plate.id)";
        String query = format(template, experimenter.getId());
        Long[] ids = this.findByQuery(query)
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getPlates(ids);
    }


    /**
     * Gets all orphaned plates available from OMERO for the current user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Plate> getOrphanedPlates()
    throws ServiceException, ExecutionException, AccessException;


    /**
     * Gets the well with the specified id from OMERO.
     *
     * @param id ID of the well.
     *
     * @return See above.
     *
     * @throws ServiceException       Cannot connect to OMERO.
     * @throws AccessException        Cannot access data.
     * @throws NoSuchElementException No element with this ID.
     * @throws ExecutionException     A Facility can't be retrieved or instantiated.
     */
    default Well getWell(Long id)
    throws ServiceException, AccessException, ExecutionException {
        List<Well> wells = getWells(id);
        if (wells.isEmpty()) {
            String msg = format("Plate %d doesn't exist in this context", id);
            throw new NoSuchElementException(msg);
        }
        return wells.iterator().next();
    }


    /**
     * Gets the wells with the specified ids from OMERO.
     *
     * @param ids A list of well ids
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    List<Well> getWells(Long... ids)
    throws ServiceException, AccessException, ExecutionException;


    /**
     * Gets all wells available from OMERO.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Well> getWells()
    throws ServiceException, AccessException, ExecutionException {
        Long[] ids = this.findByQuery("select w from Well w")
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getWells(ids);
    }


    /**
     * Gets all wells available from OMERO owned by a given user.
     *
     * @param experimenter The user.
     *
     * @return See above.
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default List<Well> getWells(Experimenter experimenter)
    throws ServiceException, AccessException, ExecutionException {
        String template = "select w from Well w where w.details.owner.id=%d";
        String query    = format(template, experimenter.getId());
        Long[] ids = this.findByQuery(query)
                         .stream()
                         .map(IObject::getId)
                         .map(RLong::getValue)
                         .toArray(Long[]::new);
        return getWells(ids);
    }

}
