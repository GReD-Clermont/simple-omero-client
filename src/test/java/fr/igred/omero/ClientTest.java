/*
 *  Copyright (C) 2020 GReD
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


import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ProjectContainer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import loci.common.DebugTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;


public class ClientTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName Name of the test case.
     */
    public ClientTest(String testName) {
        super(testName);
    }


    /**
     * @return the suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(ClientTest.class);
    }


    public void testConnection() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        assert (0L == root.getId());

        root.disconnect();
    }


    public void testConnection2() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "testUser", "password");
        assert (root.getGroupId() == 3L);
    }


    public void testConnectionErrorUsername() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("omero", 4064, "badUser", "omero", 3L);
            assert (false);
        } catch (ServiceException e) {
            assert (true);
        }
    }


    public void testConnectionErrorPassword() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("omero", 4064, "root", "badPassword", 3L);
            assert (false);
        } catch (ServiceException e) {
            assert (true);
        }
    }


    public void testConnectionErrorHost() {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("127.0.0.1", 4064, "root", "omero", 3L);
            assert (false);
        } catch (Exception e) {
            assert (true);
        }
    }


    public void testConnectionErrorPort() {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("omero", 5000, "root", "omero", 3L);
            assert (false);
        } catch (Exception e) {
            assert (true);
        }
    }


    public void testConnectionErrorGroupNotExist() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 200L);

        assert (root.getGroupId() == 3L);
    }


    public void testConnectionErrorNotInGroup() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "testUser", "password", 54L);
        assert (root.getGroupId() == 3L);
    }


    public void testGetSingleProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        assertEquals("TestProject", root.getProject(2L).getName());
    }


    public void testGetSingleProjectError() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        try {
            root.connect("omero", 4064, "root", "omero");
            root.getProject(333L);
            assert (false);
        } catch (NoSuchElementException e) {
            assert (true);
        }
    }


    public void testGetAllProjects() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<ProjectContainer> projects = root.getProjects();

        assert (projects.size() == 2);
    }


    public void testGetProjectByName() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<ProjectContainer> projects = root.getProjects("TestProject");

        assert (projects.size() == 2);

        for (ProjectContainer project : projects) {
            assertEquals(project.getName(), "TestProject");
        }
    }


    public void testDeleteProject() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 0L);

        root.deleteProject(root.getProject(1L));

        try {
            root.getProject(1L);
            assert (false);
        } catch (Exception e) {
            assert (true);
        }
    }


    public void testGetSingleDataset() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        assertEquals("TestDataset", root.getDataset(1L).getName());
    }


    public void testGetAllDatasets() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<DatasetContainer> datasets = root.getDatasets();

        assert (datasets.size() == 3);
    }


    public void testGetDatasetByName() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<DatasetContainer> datasets = root.getDatasets("TestDataset");

        assert (datasets.size() == 2);

        for (DatasetContainer dataset : datasets) {
            assertEquals("TestDataset", dataset.getName());
        }
    }


    public void testGetImages() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImages();

        assert (images.size() == 4);
    }


    public void testGetImage() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        assertEquals("image1.fake", image.getName());
    }


    public void testGetImageError() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        try {
            root.getImage(200L);
            assert (false);
        } catch (NoSuchElementException e) {
            assert (true);
        }
    }


    public void testGetImagesName() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImages("image1.fake");

        assert (images.size() == 3);
    }


    public void testGetImagesLike() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesLike(".fake");

        assert (images.size() == 4);
    }


    public void testGetImagesTagged() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesTagged(1L);

        assert (images.size() == 3);
    }


    public void testGetImagesKey() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesKey("testKey1");

        assert (images.size() == 3);
    }


    public void testGetImagesKeyValue() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesPairKeyValue("testKey1", "testValue1");

        assert (images.size() == 2);
    }


    public void testGetImagesCond() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        String key = "testKey2";

        /* Load the image with the key */
        List<ImageContainer> images = root.getImagesKey(key);

        List<ImageContainer> imagesCond = new ArrayList<>();

        for (ImageContainer image : images) {
            /* Get the value for the key */
            String value = image.getValue(root, key);

            /* Condition */
            if (value.compareTo("25") > 0) {
                imagesCond.add(image);
            }
        }

        assert (imagesCond.size() == 1);
    }


    public void testSudoTag() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Client test = root.sudoGetUser("testUser");

        TagAnnotationContainer tag = new TagAnnotationContainer(test, "Tag", "This is a tag");

        List<ImageContainer> images = test.getImages();

        for (ImageContainer image : images) {
            image.addTag(test, tag);
        }

        List<ImageContainer> tagged = test.getImagesTagged(tag);

        for (int i = 0; i < images.size(); i++) {
            assertEquals(images.get(i).getId(), tagged.get(i).getId());
        }

        root.deleteTag(tag);
    }


    public void testProjectBasic() throws Exception {
        DebugTools.enableLogging("OFF");
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        assert (project.getId() == 2L);
        assertEquals("TestProject", project.getName());
        assertEquals("description", project.getDescription());
    }

}
