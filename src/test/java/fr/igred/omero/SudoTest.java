package fr.igred.omero;


import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class SudoTest extends BasicTest {


    @Test
    public void testSudoTag() throws Exception {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero".toCharArray(), 3L);

        Client test = root.sudoGetUser("testUser");
        assertEquals(2L, test.getId());
        TagAnnotationWrapper tag = new TagAnnotationWrapper(test, "Tag", "This is a tag");

        DatasetWrapper     dataset = test.getDataset(1L);
        List<ImageWrapper> images  = dataset.getImages(test);

        for (ImageWrapper image : images) {
            image.addTag(test, tag);
        }

        List<ImageWrapper> tagged = dataset.getImagesTagged(test, tag);

        int differences = 0;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getId() != tagged.get(i).getId())
                differences++;
        }

        test.delete(tag);
        try {
            test.disconnect();
            root.disconnect();
        } catch (Exception ignored) {
        }

        assertNotEquals(0, images.size());
        assertEquals(images.size(), tagged.size());
        assertEquals(0, differences);
    }


    @Test
    public void sudoImport() throws Exception {
        String path = "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=256&sizeY=512.fake";

        Client client4 = new Client();
        client4.connect("omero", 4064, "testUser4", "password4".toCharArray(), 6L);
        assertEquals(5L, client4.getId());

        Client client3 = client4.sudoGetUser("testUser3");
        assertEquals(4L, client3.getId());
        client3.switchGroup(6L);

        File f = new File(path);
        if (!f.createNewFile())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be created.");

        DatasetWrapper dataset = new DatasetWrapper("sudoTest", "");
        dataset.saveAndUpdate(client3);

        assertTrue(dataset.canLink());
        dataset.importImages(client3, f.getAbsolutePath());

        if (!f.delete())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be deleted.");

        List<ImageWrapper> images = dataset.getImages(client3);
        assertEquals(1, images.size());

        client4.delete(images.get(0));
        client4.delete(dataset);

        try {
            client3.disconnect();
            client4.disconnect();
        } catch (Exception ignored) {
        }
    }

}
