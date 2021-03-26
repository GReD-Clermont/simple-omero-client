package fr.igred.omero;


import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class ConnectionTest extends BasicTest {


    @Test
    public void testRootConnection() throws Exception {
        Client testRoot = new Client();
        testRoot.connect("omero", 4064, "root", "omero", 3L);
        long id      = testRoot.getId();
        long groupId = testRoot.getGroupId();
        try {
            testRoot.disconnect();
        } catch (Exception ignored) {
        }
        assertEquals(0L, id);
        assertEquals(3L, groupId);
    }


    @Test
    public void testUserConnection() throws Exception {
        Client testUser = new Client();
        testUser.connect("omero", 4064, "testUser", "password");
        long id      = testUser.getId();
        long groupId = testUser.getGroupId();
        try {
            testUser.disconnect();
        } catch (Exception ignored) {
        }
        assertEquals(2L, id);
        assertEquals(3L, groupId);
    }


    @Test
    public void testSudoTag() throws Exception {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);
        assertEquals(0L, root.getId().longValue());

        Client test = root.sudoGetUser("testUser");
        assertEquals(2L, test.getId().longValue());

        TagAnnotationContainer tag = new TagAnnotationContainer(test, "Tag", "This is a tag");

        List<ImageContainer> images = test.getImages();

        for (ImageContainer image : images) {
            image.addTag(test, tag);
        }

        List<ImageContainer> tagged = test.getImagesTagged(tag);

        int differences = 0;
        for (int i = 0; i < images.size(); i++) {
            if (!images.get(i).getId().equals(tagged.get(i).getId()))
                differences++;
        }

        root.deleteTag(tag);
        try {
            root.disconnect();
        } catch (Exception ignored) {
        }

        assertNotEquals(0, images.size());
        assertEquals(images.size(), tagged.size());
        assertEquals(0, differences);
    }

}
