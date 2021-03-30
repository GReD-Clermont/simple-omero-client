package fr.igred.omero.annotations;


import fr.igred.omero.ObjectWrapper;
import omero.gateway.model.AnnotationData;

import java.sql.Timestamp;


public abstract class AnnotationWrapper<T extends AnnotationData> extends ObjectWrapper<T> {


    /**
     * Constructor of the AnnotationWrapper class.
     *
     * @param annotation Annotation to be contained.
     */
    protected AnnotationWrapper(T annotation) {
        super(annotation);
    }


    /**
     * Retrieves the {@link AnnotationData} namespace of the underlying {@link AnnotationData} instance.
     *
     * @return See above.
     */
    public String getNameSpace() {
        return data.getNameSpace();
    }


    /**
     * Sets the name space of the underlying {@link AnnotationData} instance.
     *
     * @param name The value to set.
     */
    public void setNameSpace(String name) {
        data.setNameSpace(name);
    }


    /**
     * Returns the time when the annotation was last modified.
     *
     * @return See above.
     */
    public Timestamp getLastModified() {
        return data.getLastModified();
    }


    /**
     * Retrieves the {@link AnnotationData#getDescription() description} of the underlying {@link AnnotationData}
     * instance.
     *
     * @return See above
     */
    public String getDescription() {
        return data.getDescription();
    }


    /**
     * Sets the description of the underlying {@link AnnotationData} instance.
     *
     * @param description The description
     */
    public void setDescription(String description) {
        data.setDescription(description);
    }

}
