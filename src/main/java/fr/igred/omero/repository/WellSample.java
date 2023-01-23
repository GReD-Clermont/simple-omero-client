package fr.igred.omero.repository;


import fr.igred.omero.Client;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import ome.model.units.BigResult;
import omero.gateway.model.WellSampleData;
import omero.model.Length;
import omero.model.enums.UnitsLength;

import java.util.concurrent.ExecutionException;


public interface WellSample extends RemoteObject<WellSampleData> {

    /**
     * Retrieves the well containing this well sample
     *
     * @param client The client handling the connection.
     *
     * @return See above
     *
     * @throws ServiceException   Cannot connect to OMERO.
     * @throws AccessException    Cannot access data.
     * @throws ExecutionException A Facility can't be retrieved or instantiated.
     */
    default Well getWell(Client client) throws AccessException, ServiceException, ExecutionException {
        return client.getWell(asDataObject().asWellSample().getWell().getId().getValue());
    }


    /**
     * Returns the image related to that sample if any.
     *
     * @return See above.
     */
    Image getImage();


    /**
     * Sets the image linked to this well sample.
     *
     * @param image The image to set.
     */
    default void setImage(Image image) {
        asDataObject().setImage(image.asDataObject());
    }


    /**
     * Returns the position X.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above.
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    default Length getPositionX(UnitsLength unit) throws BigResult {
        return asDataObject().getPositionX(unit);
    }


    /**
     * Returns the position Y.
     *
     * @param unit The unit (can be null, in which case no conversion will be performed)
     *
     * @return See above.
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    default Length getPositionY(UnitsLength unit) throws BigResult {
        return asDataObject().getPositionY(unit);
    }


    /**
     * Returns the time at which the field was acquired.
     *
     * @return See above.
     */
    default long getStartTime() {
        return asDataObject().getStartTime();
    }

}
