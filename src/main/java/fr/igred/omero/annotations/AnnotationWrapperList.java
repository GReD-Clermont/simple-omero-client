package fr.igred.omero.annotations;


import fr.igred.omero.WrapperList;
import omero.gateway.model.AnnotationData;


/** ArrayList of Annotation Objects implementing the AnnotationList interface */
public class AnnotationWrapperList extends WrapperList<AnnotationData, Annotation<?>> implements AnnotationList {


    private static final long serialVersionUID = -7103737611318554645L;


    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public AnnotationWrapperList() {
    }


    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     *
     * @throws IllegalArgumentException if the specified initial capacity is negative
     */
    public AnnotationWrapperList(int initialCapacity) {
        super(initialCapacity);
    }

}
