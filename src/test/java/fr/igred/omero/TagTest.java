package fr.igred.omero;


import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import loci.common.DebugTools;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class TagTest extends BasicTest {


    @Test
    public void testGetTagInfo() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        TagAnnotationContainer tag = root.getTag(1L);

        assertEquals(1L, tag.getId().longValue());
        assertEquals("tag1", tag.getName());
        assertEquals("description", tag.getDescription());
    }


    @Test
    public void testGetTags() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<TagAnnotationContainer> tags = root.getTags();

        assertEquals(3, tags.size());
    }


    @Test
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