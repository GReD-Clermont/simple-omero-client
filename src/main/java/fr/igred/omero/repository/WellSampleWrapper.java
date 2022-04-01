package fr.igred.omero.repository;


import fr.igred.omero.GenericObjectWrapper;
import ome.model.units.BigResult;
import omero.gateway.model.WellSampleData;
import omero.model.Length;
import omero.model.enums.UnitsLength;


public class WellSampleWrapper extends GenericObjectWrapper<WellSampleData> {


    /**
     * Constructor of the class WellSampleWrapper.
     *
     * @param wellSample The well sample contained in the WellSampleWrapper.
     */
    public WellSampleWrapper(WellSampleData wellSample) {
        super(wellSample);
    }


    /**
     * @return the WellSampleData contained.
     */
    public WellSampleData asWellSampleData() {
        return data;
    }


    /**
     * Returns the image related to that sample if any.
     *
     * @return See above.
     */
    public ImageWrapper getImage() {
        return new ImageWrapper(data.getImage());
    }


    /**
     * Sets the image linked to this well sample.
     *
     * @param image The image to set.
     */
    public void setImage(ImageWrapper image) {
        data.setImage(image.asImageData());
    }


    /**
     * Returns the position X.
     *
     * @param unit The unit (may be null, in which case no conversion will be performed)
     *
     * @return See above.
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getPositionX(UnitsLength unit) throws BigResult {
        return data.getPositionX(unit);
    }


    /**
     * Returns the position Y.
     *
     * @param unit The unit (may be null, in which case no conversion will be performed)
     *
     * @return See above.
     *
     * @throws BigResult If an arithmetic under-/overflow occurred
     */
    public Length getPositionY(UnitsLength unit) throws BigResult {
        return data.getPositionY(unit);
    }


    /**
     * Returns the time at which the field was acquired.
     *
     * @return See above.
     */
    public long getStartTime() {
        return data.getStartTime();
    }

}
