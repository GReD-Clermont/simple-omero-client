package fr.igred.omero;


import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class TagTest extends BasicTest {


    @Test
    public void testGetTagInfo() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        TagAnnotationContainer tag = client.getTag(1L);
        client.disconnect();

        assertEquals(1L, tag.getId().longValue());
        assertEquals("tag1", tag.getName());
        assertEquals("description", tag.getDescription());
    }


    @Test
    public void testGetTags() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        List<TagAnnotationContainer> tags = client.getTags();
        client.disconnect();

        assertEquals(3, tags.size());
    }


    @Test
    public void testGetTagsSorted() throws Exception {
        Client client = new Client();
        client.connect("omero", 4064, "testUser", "password", 3L);
        assertEquals(2L, client.getId().longValue());

        List<TagAnnotationContainer> tags = client.getTags();
        client.disconnect();

        for (int i = 1; i < tags.size(); i++) {
            assert (tags.get(i - 1).getId() <= tags.get(i).getId());
        }
    }

}