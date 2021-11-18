package fr.igred.omero;


import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class SudoTest extends RootTest {


    @Test
    public void testSudoTag() throws Exception {
        Client test = client.sudoGetUser("testUser");
        assertEquals(2L, test.getId());

        TagAnnotationWrapper tag = new TagAnnotationWrapper(test, "Tag", "This is a tag");

        List<ImageWrapper> images = test.getImages();

        for (ImageWrapper image : images) {
            image.addTag(test, tag);
        }

        List<ImageWrapper> tagged = test.getImagesTagged(tag);

        int differences = 0;
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getId() != tagged.get(i).getId())
                differences++;
        }

        test.delete(tag);
        try {
            test.disconnect();
            client.disconnect();
        } catch (Exception ignored) {
        }

        assertNotEquals(0, images.size());
        assertEquals(images.size(), tagged.size());
        assertEquals(0, differences);
        TimeUnit.SECONDS.sleep(2);
    }


    @Test
    public void sudoImport() throws Exception {
        String path = "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=256&sizeY=512.fake";

        Client test = client.sudoGetUser("testUser");
        assertEquals(2L, test.getId());
        test.switchGroup(4L);

        File f = new File(path);
        if (!f.createNewFile())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be created.");

        DatasetWrapper dataset = new DatasetWrapper("sudoTest", "");
        dataset.saveAndUpdate(test);

        assertTrue(dataset.canLink());
        dataset.importImages(test, f.getAbsolutePath());

        if (!f.delete())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be deleted.");

        List<ImageWrapper> images = dataset.getImages(test);
        assertEquals(1, images.size());

        test.delete(images.get(0));
        test.delete(dataset);

        try {
            test.disconnect();
            client.disconnect();
        } catch (Exception ignored) {
        }
        TimeUnit.SECONDS.sleep(2);
    }

}
