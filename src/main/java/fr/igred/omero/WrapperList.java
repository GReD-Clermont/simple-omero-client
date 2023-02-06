package fr.igred.omero;


import omero.gateway.model.DataObject;

import java.util.ArrayList;


/** ArrayList of Remote Objects implementing the OMEROList interface */
@SuppressWarnings("ClassExtendsConcreteCollection")
public class WrapperList<T extends DataObject, U extends RemoteObject<? extends T>>
        extends ArrayList<U>
        implements OMEROList<T, U> {

    private static final long serialVersionUID = -4150557399805439478L;

}
