package fr.igred.omero;


import fr.igred.omero.util.Wrapper;
import omero.gateway.model.DataObject;

import java.util.List;
import java.util.stream.Collectors;


/** Generic list of Remote Objects */
public interface OMEROList<T extends DataObject, U extends RemoteObject<? extends T>> extends List<U> {


    /**
     * Gets a list of elements from this list whose class is specified.
     *
     * @param clazz Class of the wanted elements.
     * @param <V>   Subclass of RemoteObject.
     *
     * @return See above.
     */
    default <V extends U> List<V> getElementsOf(Class<? extends V> clazz) {
        return stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }


    /**
     * Wraps the specified Remote Object and add it to the end of this list.
     *
     * @param object element to be wrapped and appended to this list
     *
     * @return {@code true} (as specified by {@link List#add(Object)})
     */
    default boolean add(T object) {
        boolean added = false;

        try {
            U wrapper = Wrapper.wrap(object);
            added = add(wrapper);
        } catch (IllegalArgumentException e) {
            // IGNORE
        }

        return added;
    }

}
