package fr.igred.omero;


import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class TagTest extends UserTest {


    @Test
    public void testGetTagInfo() throws Exception {
        TagAnnotationContainer tag = client.getTag(1L);
        assertEquals(1L, tag.getId().longValue());
        assertEquals("tag1", tag.getName());
        assertEquals("description", tag.getDescription());
    }


    @Test
    public void testGetTags() throws Exception {
        List<TagAnnotationContainer> tags = client.getTags();
        assertEquals(3, tags.size());
    }


    @Test
    public void testGetTagsSorted() throws Exception {
        List<TagAnnotationContainer> tags = client.getTags();
        for (int i = 1; i < tags.size(); i++) {
            assert (tags.get(i - 1).getId() <= tags.get(i).getId());
        }
    }

}