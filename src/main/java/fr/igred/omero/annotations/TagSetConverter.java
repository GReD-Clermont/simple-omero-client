package fr.igred.omero.annotations;


/**
 * Interface to convert a tag annotation to a tag set.
 */
@FunctionalInterface
interface TagSetConverter {

    /**
     * Converts this tag annotation to a tag set.
     *
     * @return See above.
     */
    TagSet toTagSet();

}
