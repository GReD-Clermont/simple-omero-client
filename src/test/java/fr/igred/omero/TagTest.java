package fr.igred.omero;


import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import loci.common.DebugTools;

import java.util.List;


public class TagTest extends TestCase {

    /**
     * Create the test case for Client
     *
     * @param testName Name of the test case.
     */
    public TagTest(String testName) {
        super(testName);
    }


    /**
     * @return the suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(TagTest.class);
    }


    public void testGetTagInfo() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        TagAnnotationContainer tag = root.getTag(1L);

        assert (1L == tag.getId());
        assertEquals("tag1", tag.getName());
        assertEquals("description", tag.getDescription());
    }


    public void testGetTags() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<TagAnnotationContainer> tags = root.getTags();

        assert (tags.size() == 3);
    }


    public void testGetTagsSorted() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<TagAnnotationContainer> tags = root.getTags();

        for (int i = 1; i < tags.size(); i++) {
            assert (tags.get(i - 1).getId() <= tags.get(i).getId());
        }
    }

}