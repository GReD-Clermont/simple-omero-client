/*
 *  Copyright (C) 2020-2023 GReD
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;


class AccessExceptionTest extends BasicTest {

    protected Client client;
    protected Client sudo;


    @BeforeEach
    void setUp() {
        boolean failed = false;
        client = new Client();
        try {
            client.connect(HOST, PORT, "testUser", "password".toCharArray(), GROUP1.id);
            assertEquals(USER1.id, client.getId(), "Wrong user");
            assertEquals(GROUP1.id, client.getCurrentGroupId(), "Wrong group");
            sudo = client.sudoGetUser("testUser2");
        } catch (AccessException | ServiceException | ExecutionException | RuntimeException e) {
            sudo = null;
            failed = true;
            logger.log(Level.SEVERE, String.format("%sConnection failed.%s", ANSI_RED, ANSI_RESET), e);
        }
        assumeFalse(failed, "Connection failed");
    }


    @AfterEach
    void cleanUp() {
        try {
            client.disconnect();
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, String.format("%sDisconnection failed.%s", ANSI_YELLOW, ANSI_RESET), e);
        }
    }


    @Test
    void testAddTagToImageWrongUser() throws Exception {
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
            image.link(client, tag);
        } catch (AccessException e) {
            exception = true;
        }

        client.delete(tag);
        assertTrue(exception);
    }


    @Test
    void testFolderAddROIWithoutImage() throws Exception {
        FolderWrapper folder = new FolderWrapper(client, "Test1");

        RectangleWrapper rectangle = new RectangleWrapper(0, 0, 10, 10);
        rectangle.setCZT(0, 0, 0);

        ROIWrapper roi = new ROIWrapper();
        roi.addShape(rectangle);
        roi.saveROI(client);

        assertThrows(AccessException.class, () -> folder.addROIs(client, -1L, roi));
        client.delete(folder);
    }


    @Test
    void testSudoFailGetProjects() {
        assertThrows(AccessException.class, () -> sudo.getProjects());
    }


    @Test
    void testSudoFailGetSingleProject() {
        assertThrows(AccessException.class, () -> sudo.getProject(PROJECT1.id));
    }


    @Test
    void testSudoFailGetProjectByName() {
        assertThrows(AccessException.class, () -> sudo.getProjects("TestProject"));
    }


    @Test
    void testSudoFailDeleteProject() {
        ProjectI       projectI    = new ProjectI(PROJECT1.id, false);
        ProjectData    projectData = new ProjectData(projectI);
        ProjectWrapper project     = new ProjectWrapper(projectData);
        assertThrows(AccessException.class, () -> sudo.delete(project));
    }


    @Test
    void testSudoFailDeleteProjects() {
        ProjectI    projectI1    = new ProjectI(PROJECT1.id, false);
        ProjectI    projectI2    = new ProjectI(2L, false);
        ProjectData projectData1 = new ProjectData(projectI1);
        ProjectData projectData2 = new ProjectData(projectI2);

        Collection<ProjectWrapper> projects = new ArrayList<>(2);
        projects.add(new ProjectWrapper(projectData1));
        projects.add(new ProjectWrapper(projectData2));
        assertThrows(AccessException.class, () -> sudo.delete(projects));
    }


    // This test returns a ServiceException for a "security violation".
    @Test
    void testSudoFailGetDatasets() {
        assertThrows(ServiceException.class, () -> sudo.getDatasets());
    }


    @Test
    void testSudoFailGetSingleDataset() {
        assertThrows(AccessException.class, () -> sudo.getDataset(DATASET1.id));
    }


    @Test
    void testSudoFailGetDatasetByName() {
        assertThrows(AccessException.class, () -> sudo.getDatasets("TestDataset"));
    }


    @Test
    void testSudoFailGetImages() {
        assertThrows(AccessException.class, () -> sudo.getImages());
    }


    @Test
    void testSudoFailGetImage() {
        assertThrows(AccessException.class, () -> sudo.getImage(IMAGE1.id));
    }


    @Test
    void testSudoFailGetImagesName() {
        assertThrows(AccessException.class, () -> sudo.getImages("image1.fake"));
    }


    @Test
    void testSudoFailGetImagesLike() {
        assertThrows(AccessException.class, () -> sudo.getImagesLike("image1"));
    }


    @Test
    void testSudoFailGetAllTags() {
        assertThrows(ServiceException.class, () -> sudo.getTags());
    }


    @Test
    void testSudoFailGetTag() {
        assertThrows(ServiceException.class, () -> sudo.getTag(TAG1.id));
    }


    @Test
    void testSudoFailGetImageTag() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        assertThrows(AccessException.class, () -> image.getTags(sudo));
    }


    @Test
    void testSudoFailGetKVPairs() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);
        assertThrows(AccessException.class, () -> image.getKeyValuePairs(sudo));
    }


    @Test
    void testSudoFailAddKVPair() throws Exception {
        ImageWrapper image = client.getImage(IMAGE1.id);

        List<NamedValue> result1 = new ArrayList<>(2);
        result1.add(new NamedValue("Test result1", "Value Test"));
        result1.add(new NamedValue("Test2 result1", "Value Test2"));

        MapAnnotationWrapper mapAnnotation1 = new MapAnnotationWrapper(result1);
        assertThrows(AccessException.class, () -> image.addMapAnnotation(sudo, mapAnnotation1));
    }


    @Test
    void testSudoFail() {
        assertThrows(AccessException.class, () -> sudo.sudoGetUser("root"));
    }


}
