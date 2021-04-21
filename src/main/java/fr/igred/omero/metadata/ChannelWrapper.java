package fr.igred.omero.metadata;


import fr.igred.omero.GenericObjectWrapper;
import omero.gateway.model.ChannelData;

import java.awt.*;


public class ChannelWrapper extends GenericObjectWrapper<ChannelData> {


    /**
     * Constructor of the class ChannelWrapper.
     *
     * @param channel The ChannelData contained in the ChannelWrapper.
     */
    public ChannelWrapper(ChannelData channel) {
        super(channel);
    }


    /**
     * @return ChannelData contained.
     */
    public ChannelData asChannelData() {
        return data;
    }


    /**
     * Returns the channel index.
     *
     * @return See above.
     */
    public int getIndex() {
        return data.getIndex();
    }


    /**
     * Returns the label of the channel.
     * <p>Following the specification: Name&rarr;Fluor&rarr;Emission wavelength&rarr;index.
     *
     * @return See above.
     */
    public String getChannelLabeling() {
        return data.getChannelLabeling();
    }


    /**
     * Returns the name of the channel.
     *
     * @return See above.
     */
    public String getName() {
        return asChannelData().getName();
    }


    /**
     * Sets the name of the channel.
     *
     * @param name The name of the channel.
     */
    public void setName(String name) {
        data.setName(name);
    }


    /**
     * Gets the original channel color.
     *
     * @return The original channel color.
     */
    public Color getColor() {
        return new Color(getRed(), getGreen(), getBlue(), getAlpha());
    }


    /**
     * Gets the sRGB alpha value of the channel.
     *
     * @return See above.
     */
    public int getAlpha() {
        return data.asChannel().getAlpha().getValue();
    }


    /**
     * Gets the sRGB red value of the channel.
     *
     * @return See above.
     */
    public int getRed() {
        return data.asChannel().getRed().getValue();
    }


    /**
     * Gets the sRGB green value of the channel.
     *
     * @return See above.
     */
    public int getGreen() {
        return data.asChannel().getGreen().getValue();
    }


    /**
     * Gets the sRGB blue value of the channel.
     *
     * @return See above.
     */
    public int getBlue() {
        return data.asChannel().getBlue().getValue();
    }

}
