package fr.igred.omero;


import omero.gateway.model.DataObject;

import java.util.ArrayList;


/** ArrayList of Remote Objects implementing the OMEROList interface */
@SuppressWarnings("ClassExtendsConcreteCollection")
public class WrapperList<T extends DataObject, U extends RemoteObject<? extends T>>
        extends ArrayList<U>
        implements OMEROList<T, U> {

    private static final long serialVersionUID = -4150557399805439478L;


    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public WrapperList() {
    }


    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *                                  is negative
     */
    public WrapperList(int initialCapacity) {
        super(initialCapacity);
    }

}
