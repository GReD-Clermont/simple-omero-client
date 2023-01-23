package fr.igred.omero.repository;


import fr.igred.omero.RemoteObject;
import omero.gateway.model.ChannelData;

import java.awt.Color;


public interface Channel extends RemoteObject<ChannelData> {

    /**
     * Returns whether the channel contains all the RGBA values or not.
     *
     * @return See above.
     */
    default boolean hasRGBA() {
        return asDataObject().asChannel().getRed() != null &&
               asDataObject().asChannel().getGreen() != null &&
               asDataObject().asChannel().getBlue() != null &&
               asDataObject().asChannel().getAlpha() != null;
    }


    /**
     * Returns the channel index.
     *
     * @return See above.
     */
    default int getIndex() {
        return asDataObject().getIndex();
    }


    /**
     * Returns the label of the channel.
     * <p>Following the specification: Name&rarr;Fluor&rarr;Emission wavelength&rarr;index.
     *
     * @return See above.
     */
    default String getChannelLabeling() {
        return asDataObject().getChannelLabeling();
    }


    /**
     * Returns the name of the channel.
     *
     * @return See above.
     */
    default String getName() {
        return asDataObject().getName();
    }


    /**
     * Sets the name of the channel.
     *
     * @param name The name of the channel.
     */
    default void setName(String name) {
        asDataObject().setName(name);
    }


    /**
     * Gets the original channel color. Defaults to {@link Color#WHITE} if RGBA values are missing.
     *
     * @return The original channel color.
     */
    default Color getColor() {
        Color color = Color.WHITE;
        if (hasRGBA()) color = new Color(getRed(), getGreen(), getBlue(), getAlpha());
        return color;
    }


    /**
     * Gets the sRGB alpha value of the channel.
     *
     * @return See above.
     */
    default int getAlpha() {
        return asDataObject().asChannel().getAlpha().getValue();
    }


    /**
     * Gets the sRGB red value of the channel.
     *
     * @return See above.
     */
    default int getRed() {
        return asDataObject().asChannel().getRed().getValue();
    }


    /**
     * Gets the sRGB green value of the channel.
     *
     * @return See above.
     */
    default int getGreen() {
        return asDataObject().asChannel().getGreen().getValue();
    }


    /**
     * Gets the sRGB blue value of the channel.
     *
     * @return See above.
     */
    default int getBlue() {
        return asDataObject().asChannel().getBlue().getValue();
    }

}
