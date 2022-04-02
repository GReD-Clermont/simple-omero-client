/*
 *  Copyright (C) 2020-2022 GReD
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


import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.exception.AccessException;
import fr.igred.omero.exception.ServiceException;
import fr.igred.omero.repository.FolderWrapper;
import fr.igred.omero.repository.ImageWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import omero.gateway.model.ProjectData;
import omero.model.NamedValue;
import omero.model.ProjectI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class AccessExceptionTest extends BasicTest {

    private final PrintStream empty = new PrintStream(new OutputStream() {
        public void write(int b) {
            //DO NOTHING
        }
    });
    private final PrintStream error = System.err;
    protected     Client      client;
    protected     Client      sudo;


    void hideErrors() {
        System.setErr(empty);
    }


    void showErrors() {
        System.setErr(error);
    }


    @Before
    public void setUp() {
        boolean failed = false;
        client = new Client();
        try {
            client.connect(HOST, PORT, "testUser", "password".toCharArray(), GROUP1.id);
            assertEquals("Wrong user", USER1.id, client.getId());
            assertEquals("Wrong group", GROUP1.id, client.getCurrentGroupId());
            sudo = client.sudoGetUser("testUser2");
        } catch (AccessException | ServiceException | ExecutionException | RuntimeException e) {
            sudo = null;
            failed = true;
            logger.log(Level.SEVERE, ANSI_RED + "Connection failed." + ANSI_RESET, e);
        }
        org.junit.Assume.assumeFalse(failed);
        hideErrors();
    }


    @After
    public void cleanUp() {
        try {
            client.disconnect();
            showErrors();
        } catch (Exception e) {
            showErrors();
            logger.log(Level.WARNING, ANSI_YELLOW + "Disconnection failed." + ANSI_RESET, e);
        }
    }


    @Test
    public void testAddTagToImageWrongUser() throws Exception {
        boolean exception = false;
        client.disconnect();
        client.connect(HOST, PORT, "root", "omero".toCharArray(), GROUP1.id);
        assertEquals(0L, client.getId());

        ImageWrapper image = client.getImage(IMAGE2.id);
        assertFalse(image.canLink());
        assertFalse(image.canAnnotate());
        assertTrue(image.canEdit());
        assertTrue(image.canDelete());
        assertTrue(image.canChgrp());
        assertTrue(image.canChown());

        TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "image tag", "tag attached to an image");

        try {
            image.addTag(client, tag);
        } catch (AccessException e) {
            exception = true;
        }

        client.delete(tag);
        assertTrue(exception);
    }


    @Test(expected = AccessException.class)
    public void testFolderAddROIWithoutImage() throws Exception {
        FolderWrapper folder = new FolderWrapper(client, "Test1");

        RectangleWrapper rectangle = new RectangleWrapper(0, 0, 10, 10);
        rectangle.setCZT(0, 0, 0);

        ROIWrapper roi = new ROIWrapper();
        roi.addShape(rectangle);
        roi.saveROI(client);

        folder.addROI(client, roi);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetProjects() throws Exception {
        sudo.getProjects();
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetSingleProject() throws Exception {
        sudo.getProject(PROJECT1.id);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetProjectByName() throws Exception {
        sudo.getProjects("TestProject");
    }


    @Test(expected = AccessException.class)
    public void testSudoFailDeleteProject() throws Exception {
        ProjectI       projectI    = new ProjectI(1L, false);
        ProjectData    projectData = new ProjectData(projectI);
        ProjectWrapper project     = new ProjectWrapper(projectData);
        sudo.delete(project);
    }


    // This test returns a ServiceException for a "security violation".
    @Test(expected = ServiceException.class)
    public void testSudoFailGetDatasets() throws Exception {
        sudo.getDatasets();
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetSingleDataset() throws Exception {
        sudo.getDataset(DATASET1.id);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetDatasetByName() throws Exception {
        sudo.getDatasets("TestDataset");
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImages() throws Exception {
        sudo.getImages();
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImage() throws Exception {
        sudo.getImage(IMAGE1.id);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImagesName() throws Exception {
        sudo.getImages("image1.fake");
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImagesLike() throws Exception {
        sudo.getImagesLike("image1");
    }


    @Test(expected = ServiceException.class)
    public void testSudoFailGetAllTags() throws Exception {
        sudo.getTags();
    }


    @Test(expected = ServiceException.class)
    public void testSudoFailGetTag() throws Exception {
        sudo.getTag(TAG1.id);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetImageTag() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        image.getTags(sudo);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailGetKVPairs() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        image.getKeyValuePairs(sudo);
    }


    @Test(expected = AccessException.class)
    public void testSudoFailAddKVPair() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        List<NamedValue> result1 = new ArrayList<>(2);
        result1.add(new NamedValue("Test result1", "Value Test"));
        result1.add(new NamedValue("Test2 result1", "Value Test2"));

        MapAnnotationWrapper mapAnnotation1 = new MapAnnotationWrapper(result1);
        image.addMapAnnotation(sudo, mapAnnotation1);
    }


    @Test(expected = AccessException.class)
    public void testSudoFail() throws Exception {
        sudo.sudoGetUser("root");
    }


}
