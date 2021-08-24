/*
 *  Copyright (C) 2020-2021 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.

 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package fr.igred.omero;


import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.ImageWrapper;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;


public class ConnectionTest extends BasicTest {


    @Test
    public void testDisconnect() {
        Client testRoot = new Client();
        testRoot.disconnect();
        assertTrue(true);
    }


    @Test
    public void testSessionConnect() throws Exception {
        Client client1 = new Client();
        client1.connect("omero", 4064, "testUser", "password".toCharArray());
        String sessionId = client1.getSessionId();
        Client client2   = new Client();
        client2.connect("omero", 4064, sessionId);
        assertEquals(client1.getUser().getId(), client2.getUser().getId());
        client1.disconnect();
        client2.disconnect();
    }


    @Test
    public void testRootConnection() throws Exception {
        Client testRoot = new Client();
        testRoot.connect("omero", 4064, "root", "omero".toCharArray(), 3L);
        long id      = testRoot.getId();
        long groupId = testRoot.getCurrentGroupId();
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
        testUser.connect("omero", 4064, "testUser", "password".toCharArray());
        long id      = testUser.getId();
        long groupId = testUser.getCurrentGroupId();
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
        root.connect("omero", 4064, "root", "omero".toCharArray(), 3L);
        assertEquals(0L, root.getId());

        Client test = root.sudoGetUser("testUser");
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

        root.delete(tag);
        try {
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

        Client client = new Client();
        client.connect("omero", 4064, "root", "omero".toCharArray());

        File f = new File(path);
        if (!f.createNewFile())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be created.");

        Client c = client.sudoGetUser("testUser");
        c.switchGroup(4L);

        DatasetWrapper dataset = new DatasetWrapper("sudoTest", "");
        dataset.saveAndUpdate(c);

        List<Long> ids = dataset.importImage(c, f.getAbsolutePath());

        if (!f.delete())
            System.err.println("\"" + f.getCanonicalPath() + "\" could not be deleted.");

        assertEquals(1, ids.size());

        client.delete(c.getImage(ids.get(0)));
        client.delete(dataset);
    }

}
