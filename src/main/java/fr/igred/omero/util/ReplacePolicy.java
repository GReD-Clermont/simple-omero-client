package fr.igred.omero.util;


/**
 * Policy to specify how to handle objects when they are replaced.
 */
public enum ReplacePolicy {
    /** Unlink objects only */
    UNLINK,

    /** Delete all objects */
    DELETE,

    /** Delete orphaned objects */
    DELETE_ORPHANED
}
