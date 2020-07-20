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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import fr.igred.omero.metadata.ROIContainer;
import fr.igred.omero.metadata.TableContainer;
import fr.igred.omero.metadata.annotation.MapAnnotationContainer;
import fr.igred.omero.metadata.annotation.TagAnnotationContainer;
import fr.igred.omero.repository.DatasetContainer;
import fr.igred.omero.repository.ProjectContainer;

import ij.ImagePlus;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.ImageData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.ShapeData;
import omero.model.NamedValue;

public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }
    
    public void testConnection()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        assert(0L == root.getId());
    }

    public void testConnection2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "testUser", "password");
        assert(root.getGroupId() == 3L);
    }

    public void testConnectionErrorUsername()
        throws Exception
    {
        Client root = new Client();
        try  {
            root.connect("omero", 4064, "blabla", "omero", 3L);
            assert(false);
        }
        catch (DSOutOfServiceException e) {
            assert(true);
        }
    }

    public void testConnectionErrorMdp()
        throws Exception
    {
        Client root = new Client();
        try  {
            root.connect("omero", 4064, "root", "ba", 3L);
            assert(false);
        }
        catch (DSOutOfServiceException e) {
            assert(true);
        }
    }

    public void testConnectionErrorHost()
        throws Exception
    {
        Client root = new Client();
        try  {
            root.connect("google.com", 4064, "root", "omero", 3L);
            assert(false);
        }
        catch (Exception e)
        {
            assert(true);
        }
    }

    public void testConnectionErrorPort()
        throws Exception
    {
        Client root = new Client();
        try  {
            root.connect("local", 5000, "root", "omero", 3L);
            assert(false);
        }
        catch (Exception e)
        {
            assert(true);
        }
    }
    
    public void testConnectionErrorGroupNotExist()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 200L);

        assert(root.getGroupId() == 3L);
    }
    
    public void testConnectionErrorNotInGroup()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "testUser", "password", 54L);
        assert(root.getGroupId() == 3L);
    }

    public void testGetSingleProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        assertEquals("TestProject", root.getProject(2L).getName());
    }
    
    public void testGetSingleProjectError()
        throws Exception
    {
        Client root = new Client();
        try  {
            root.connect("omero", 4064, "root", "omero");
            root.getProject(333L);
            assert(false);
        }
        catch(NoSuchElementException e) {
            assert(true);
        }
    }
    
    public void testGetAllProjects()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<ProjectContainer> projects = root.getProjects();

        assert(projects.size() == 2);
    }

    public void testGetProjectByName()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<ProjectContainer> projects = root.getProjects("TestProject");

	assert(projects.size() == 2);

        for(ProjectContainer project : projects)
        {
            assertEquals(project.getName(), "TestProject");
        }
    }

    public void testDeleteProject()
	throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 0L);

        root.deleteProject(root.getProject(1L));

        try {
            root.getProject(1L);
            assert(false);
        }
        catch(Exception e)
        {
            assert(true);
        }
    }

    public void testGetSingleDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        assertEquals("TestDataset", root.getDataset(1L).getName());
    }

    public void testGetAllDatasets()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<DatasetContainer> datasets = root.getDatasets();

        assert(datasets.size() == 3);
    }

    public void testGetDatasetByName()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        Collection<DatasetContainer> datasets = root.getDatasets("TestDataset");

	assert(datasets.size() == 2);

        for(DatasetContainer dataset : datasets)
        {
            assertEquals("TestDataset", dataset.getName());
        }
    }

    
    public void testGetImages()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);
        
        List<ImageContainer> images = root.getImages();

        assert(images.size() == 4);
    }


    public void testGetImage()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        assertEquals("8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake", image.getName());
    }

    public void testGetImageError()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        try {
            root.getImage(200L);
            assert(false);
        }
        catch(NoSuchElementException e) {
            assert(true);
        }
    }

    public void testGetImagesName()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImages("8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");

        assert(images.size() == 3);
    }

    public void testGetImagesLike()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesLike(".fake");

        assert(images.size() == 4);
    }

    public void testGetImagesTagged()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesTagged(1L);

        assert(images.size() == 3);
    }

    public void testGetImagesKey()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesKey("testKey1");

        assert(images.size() == 3);
    }

    public void testGetImagesKeyValue()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImagesPairKeyValue("testKey1", "testValue1");

        assert(images.size() == 2);
    }

    public void testGetImagesCond()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        String key = "testKey2";

        //Load the image with the key
        List<ImageContainer> images = root.getImagesKey(key);

        List<ImageContainer> imagesCond = new ArrayList<ImageContainer>();

        for(ImageContainer image : images)
        {
            //Get the value for the key
            String value = image.getValue(root, key);

            //Condition
            if(value.compareTo("25") > 0)
            {
                imagesCond.add(image);
            }
        }
	
        assert(imagesCond.size() == 1);
    }

    public void testSudoTag()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);
        
        Client test = root.SudoGetUser("testUser");

        TagAnnotationContainer tag = new TagAnnotationContainer(test, "Tag", "This is a tag");

        List<ImageContainer> images = test.getImages();

        for(ImageContainer image : images)
        {
            image.addTag(test, tag);
        }

        List<ImageContainer> tagged = test.getImagesTagged(tag);

        for(int i = 0; i < images.size(); i++)
        {
            assertEquals(images.get(i).getId(), tagged.get(i).getId());
        }

        root.deleteTag(tag);
    }

    public void testProjectBasic()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        assert(project.getId() == 2L);
        assertEquals("TestProject", project.getName());
        assertEquals("description", project.getDescription());

    }

    public void testGetDatasetFromProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<DatasetContainer> datasets = project.getDatasets();
        
        assert(datasets.size() == 2);
    }

    public void testGetDatasetFromProject2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<DatasetContainer> datasets = project.getDatasets("TestDataset");
        
        assert(datasets.size() == 1);
    }

    public void testAddTagToProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        
        project.addTag(root, tag);

        List<TagAnnotationContainer> tags = project.getTags(root);

        assert(tags.size() == 1);

        root.deleteTag(tag);

        tags = project.getTags(root);

        assert(tags.size() == 0);
    }
    
    public void testAddTagToProject2()
        throws Exception
    {    
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);
        
        project.addTag(root, "test", "test");

        List<TagAnnotationContainer> tags = root.getTags("test");
        assert(tags.size() == 1);

        root.deleteTag(tags.get(0).getId());

        tags = root.getTags("test");
        assert(tags.size() == 0);
    }

    public void testAddTagIdToProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        
        project.addTag(root, tag.getId());

        List<TagAnnotationContainer> tags = project.getTags(root);

        assert(tags.size() == 1);

        root.deleteTag(tag);

        tags = project.getTags(root);

        assert(tags.size() == 0);
    }

    public void testAddTagsToProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        
        project.addTags(root, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationContainer> tags = project.getTags(root);

        assert(tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = project.getTags(root);

        assert(tags.size() == 0);
    }

    public void testAddTagsToProject2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Project tag", "tag attached to a project");
        
        project.addTags(root, tag1, tag2, tag3, tag4);

        List<TagAnnotationContainer> tags = project.getTags(root);

        assert(tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = project.getTags(root);

        assert(tags.size() == 0);
    }

    public void testGetImagesInProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImages(root);

        assert(images.size() == 3);
    }

    
    public void testGetImagesByNameInProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImages(root, "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");

        assert(images.size() == 2);
    }

    public void testGetImagesLikeInProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesLike(root, ".fake");

        assert(images.size() == 3);
    }

    public void testGetImagesTaggedInProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesTagged(root, 1L);

        assert(images.size() == 2);
    }

    public void testGetImagesTaggedInProject2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        TagAnnotationContainer tag = root.getTag(2L);
        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesTagged(root, tag);

        assert(images.size() == 1);
    }

    public void testGetImagesKeyInProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesKey(root, "testKey1");

        assert(images.size() == 3);
    }

    public void testGetImagesPairKeyValueInProject()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        List<ImageContainer> images = project.getImagesPairKeyValue(root, "testKey1", "testValue1");
        
        assert(images.size() == 2);
    }

    public void testCreateDatasetAndDeleteIt1()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        String name = "To delete";

        Long id = project.addDataset(root, name, "Dataset which will ne deleted").getId();

        DatasetContainer dataset = root.getDataset(id);
        
        assertEquals(dataset.getName(), name);

        root.deleteDataset(dataset);

        try {
            root.getDataset(id);
            assert(false);
        }
        catch(NoSuchElementException e) {
            assert(true);
        }
    }

    public void testCreateDatasetAndDeleteIt2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ProjectContainer project = root.getProject(2L);

        String description = "Dataset which will ne deleted";

        DatasetContainer dataset = new DatasetContainer("To delete", description);

        Long id = project.addDataset(root, dataset).getId();

        dataset = root.getDataset(id);
        
        assertEquals(dataset.getDescription(), description);

        root.deleteDataset(dataset);

        try {
            root.getDataset(id);
            assert(false);
        }
        catch(NoSuchElementException e) {
            assert(true);
        }
    }

    public void testCopieDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        ProjectContainer project = root.getProject(3L);

        String name = "Will be deleted";

        Long id = project.addDataset(root, name, "Dataset which will be deleted").getId();

        DatasetContainer newDataset = root.getDataset(id);

        newDataset.addImages(root, images);

        assert(newDataset.getImages(root).size() == images.size());

        root.deleteDataset(newDataset);

        List<ImageContainer> newImages = dataset.getImages(root);

        assert(newImages.size() == images.size());
    }

    public void testDatasetBasic()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        assertEquals("TestDataset", dataset.getName());
	assertEquals("description", dataset.getDescription()); 
        assert(dataset.getId() == 1L);
    }

    public void testAddTagToDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        
        dataset.addTag(root, tag);

        List<TagAnnotationContainer> tags = dataset.getTags(root);

        assert(tags.size() == 1);

        root.deleteTag(tag);

        tags = dataset.getTags(root);

        assert(tags.size() == 0);
    }

    public void testAddTagToDataset2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);
        
        dataset.addTag(root, "Dataset tag", "tag attached to a dataset");

        List<TagAnnotationContainer> tags = root.getTags("Dataset tag");
        assert(tags.size() == 1);

        root.deleteTag(tags.get(0).getId());

        tags = root.getTags("Dataset tag");
        assert(tags.size() == 0);
    }

    public void testAddTagIdToDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        
        dataset.addTag(root, tag.getId());

        List<TagAnnotationContainer> tags = dataset.getTags(root);

        assert(tags.size() == 1);

        root.deleteTag(tag);

        tags = dataset.getTags(root);

        assert(tags.size() == 0);
    }

    public void testAddTagsToDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        
        dataset.addTags(root, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationContainer> tags = dataset.getTags(root);

        assert(tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = dataset.getTags(root);

        assert(tags.size() == 0);
    }

    public void testAddTagsToDataset2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Dataset tag", "tag attached to a dataset");
        
        dataset.addTags(root, tag1, tag2, tag3, tag4);

        List<TagAnnotationContainer> tags = dataset.getTags(root);

        assert(tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = dataset.getTags(root);

        assert(tags.size() == 0);
    }

    public void testGetImagesInDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        assert(images.size() == 3);
    }

    public void testGetImagesByNameInDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root, "8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");

        assert(images.size() == 2);
    }

    public void testGetImagesLikeInDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesLike(root, ".fake");

        assert(images.size() == 3);
    }

    public void testGetImagesTaggedInDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesTagged(root, 1L);

        assert(images.size() == 2);
    }

    public void testGetImagesTaggedInDataset2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        TagAnnotationContainer tag = root.getTag(2L);
        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesTagged(root, tag);

        assert(images.size() == 1);
    }

    public void testGetImagesKeyInDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesKey(root, "testKey1");

        assert(images.size() == 3);
    }

    public void testGetImagesPairKeyValueInDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImagesPairKeyValue(root, "testKey1", "testValue1");

        assert(images.size() == 2);
    }

    public void testGetImagesFromDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        assertEquals(3, images.size()); 
    }

    public void testImportImage()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        File f = new File("./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");
        f.createNewFile();

        File f2 = new File("./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=6&sizeX=512&sizeY=512.fake");
        f2.createNewFile();

        DatasetContainer dataset = root.getDataset(2L);

        dataset.importImages(root, 
			     "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake",
			     "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=6&sizeX=512&sizeY=512.fake");

        List<ImageContainer> images =  dataset.getImages(root);

	    assertEquals(2, images.size());

        for(ImageContainer image : images)
        {
            root.deleteImage(image);
        }

        images =  dataset.getImages(root);

        assert(images.isEmpty());
    }

    public void testCreateTable()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        TableContainer table = new TableContainer(2, "TableTest");

	assertEquals(2, table.getColumnCount());

        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        table.setRowCount(images.size());

        for(ImageContainer image : images)
        {
            table.addRow(image.getImage(), image.getName());
        }

        dataset.addTable(root, table);

        List<TableContainer> tables = dataset.getTables(root);

        assertEquals(1, tables.size());
        
        root.deleteTable(tables.get(0));

        tables = dataset.getTables(root);

        assertEquals(0, tables.size());
    }

    public void testErrorTableFull()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        TableContainer table = new TableContainer(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        table.setRowCount(images.size() - 1);

        try {
            for(ImageContainer image : images) {
                table.addRow(image.getImage(), image.getName());
            }
            assert(false);
        }
        catch(IndexOutOfBoundsException e) {
            assert(true);
        }    
    }

    public void testErrorTableUnitialized()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        TableContainer table = new TableContainer(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        try {
            for(ImageContainer image : images) {
                table.addRow(image.getImage(), image.getName());
            }   
            assert(false);
        }
        catch(IndexOutOfBoundsException e) {
            assert(true);
        }    
    }

    public void testErrorTableNotEnoughArgs()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        List<ImageContainer> images = dataset.getImages(root);

        TableContainer table = new TableContainer(2, "TableTest");
        table.setColumn(0, "Image", ImageData.class);
        table.setColumn(1, "Name", String.class);

        table.setRowCount(images.size());

        try {
            for(ImageContainer image : images) {
                table.addRow(image.getImage());
            }   
            assert(false);
        }
        catch(IllegalArgumentException e) {
            assert(true);
        }    
    }
    
    public void testPairKeyValue()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        File f = new File("./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");
        f.createNewFile();

        DatasetContainer dataset = root.getDataset(2L);

        dataset.importImages(root, "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");

        List<ImageContainer> images =  dataset.getImages(root);

        ImageContainer image = images.get(0);

        List<NamedValue> result1 = new ArrayList<NamedValue>();
        result1.add(new NamedValue("Test result1", "Valeur Test"));
        result1.add(new NamedValue("Test2 result1", "Valeur Test2"));

        List<NamedValue> result2 = new ArrayList<NamedValue>();
        result2.add(new NamedValue("Test result2" , "Valeur Test"));
        result2.add(new NamedValue("Test2 result2", "Valeur Test2"));

        MapAnnotationContainer mapAnnotation1 = new MapAnnotationContainer(result1);
        MapAnnotationContainer mapAnnotation2 = new MapAnnotationContainer(result2);
        
        image.addMapAnnotation(root, mapAnnotation1);
        image.addMapAnnotation(root, mapAnnotation2);

        List<NamedValue> result = image.getKeyValuePairs(root);

        assert(result.size() == 4);
        assertEquals(image.getValue(root, "Test result1"), "Valeur Test");

        root.deleteImage(image);
    }
    
    public void testPairKeyValue2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        File f = new File("./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");
        f.createNewFile();

        DatasetContainer dataset = root.getDataset(2L);

        dataset.importImages(root, "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");

        List<ImageContainer> images =  dataset.getImages(root);

        ImageContainer image = images.get(0);

        List<NamedValue> result = new ArrayList<NamedValue>();
        result.add(new NamedValue("Test result1", "Valeur Test"));
        result.add(new NamedValue("Test2 result1", "Valeur Test2"));

        MapAnnotationContainer mapAnnotation = new MapAnnotationContainer();
        mapAnnotation.setContent(root, result);
        
        image.addMapAnnotation(root, mapAnnotation);

        List<NamedValue> results = image.getKeyValuePairs(root);

        assert(results.size() == 2);
        assertEquals(image.getValue(root, "Test result1"), "Valeur Test");

        root.deleteImage(image);
    }
    
    public void testPairKeyValue3()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        File f = new File("./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");
        f.createNewFile();

        DatasetContainer dataset = root.getDataset(2L);

        dataset.importImages(root, "./8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake");

        List<ImageContainer> images =  dataset.getImages(root);

        ImageContainer image = images.get(0);
        
        image.addPairKeyValue(root, "Test result1", "Valeur Test");
        image.addPairKeyValue(root, "Test result2", "Valeur Test2");

        List<NamedValue> results = image.getKeyValuePairs(root);

        assert(results.size() == 2);
        try {
            image.getValue(root, "Inexistant value");
            assert(false);
        }
        catch(Exception e) {
            assert(true);
        }
        finally {
            root.deleteImage(image);
        }
    }

    public void testROI()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ROIContainer roiContainer = new ROIContainer();
        
        ImageContainer image = root.getImage(1L);

        roiContainer.setImage(root, image);
        
        for(int i = 0; i < 4; i++)
        {
            RectangleData rectangle = new RectangleData(10, 10, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);
            rectangle.setY(i * 2);
            rectangle.setX(i * 2);

            roiContainer.addShape(rectangle);
        }

        image.saveROI(root, roiContainer);

        List<ROIContainer> rois = image.getROIs(root);

        assert(rois.size() == 1);
        assert(rois.get(0).getShapes().size() == 4);

        for(ROIContainer roi : rois)
        {
            root.deleteROI(roi);
        }

        rois = image.getROIs(root);

        assert(rois.size() == 0);
    }

    public void testROI2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);
        
        ImageContainer image = root.getImage(1L);

        List<ShapeData> shapes = new ArrayList<ShapeData>(4);
        
        for(int i = 0; i < 4; i++)
        {
            RectangleData rectangle = new RectangleData(10, 10, 10, 10);
            rectangle.setZ(0);
            rectangle.setT(0);
            rectangle.setC(0);
            rectangle.setY(i * 2);
            rectangle.setX(i * 2);

            shapes.add(rectangle);
        }

        ROIContainer roiContainer = new ROIContainer(shapes);
        roiContainer.setImage(root, image);
        image.saveROI(root, roiContainer);

        List<ROIContainer> rois = image.getROIs(root);

        assert(rois.size() == 1);
        assert(rois.get(0).getShapes().size() == 4);

        for(ROIContainer roi : rois)
        {
            root.deleteROI(roi);
        }

        rois = image.getROIs(root);

        assert(rois.size() == 0);
    }
    
    public void testRoiAddShapeAndDeleteIt()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);
        
        ImageContainer image = root.getImage(1L);

	List<ShapeData> shapes = new ArrayList<ShapeData>(4);
	for(int i = 0; i < 4; i++)
       	{	
	    RectangleData rectangle = new RectangleData(10, 10, 10, 10); 
	    rectangle.setZ(0);
	    rectangle.setT(0);
	    rectangle.setC(0);
	    rectangle.setY(i * 2);
	    rectangle.setX(i * 2);

	    shapes.add(rectangle);
	}

        ROIContainer roiContainer = new ROIContainer();
        roiContainer.addShapes(shapes);
	roiContainer.setImage(root, image);
	image.saveROI(root, roiContainer);

        List<ROIContainer> rois = image.getROIs(root);

        roiContainer = rois.get(0);
        int size = roiContainer.getShapes().size();
        int ROInunmber = rois.size();

        RectangleData rectangle = new RectangleData(10, 10, 8, 8);
        rectangle.setZ(2);
        rectangle.setT(2);
        rectangle.setC(2);
        rectangle.setY(2);
        rectangle.setX(2);

        roiContainer.addShape(rectangle);
        roiContainer.updateROI(root);

        rois = image.getROIs(root);
        roiContainer = rois.get(0);
        assert(size + 1 == roiContainer.getShapes().size());
        assert(ROInunmber == rois.size());

        roiContainer.deleteShape(roiContainer.getShapes().size() - 1);
        roiContainer.updateROI(root);

        rois = image.getROIs(root);
        roiContainer = rois.get(0);
        assert(size == roiContainer.getShapes().size());
        assert(ROInunmber == rois.size());
    }

    public void testGetImageInfo()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        assertEquals("8bit-unsigned&pixelType=uint8&sizeZ=3&sizeC=5&sizeT=7&sizeX=512&sizeY=512.fake" , image.getName());
        assert(1L == image.getId());
    }
    
    public void testGetImageTag()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        List<TagAnnotationContainer> tags = image.getTags(root);
        assert(tags.size() == 2);
    }

    public void testGetImageSize()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        PixelContainer pixels = image.getPixels();

        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        int sizeZ = pixels.getSizeZ();
        int sizeC = pixels.getSizeC();
        int sizeT = pixels.getSizeT();

        assert(512 == sizeX);
        assert(512 == sizeY);
        assert(3 == sizeZ);
        assert(5 == sizeC);
        assert(7 == sizeT);
    }

    public void testGetRawData()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);
        PixelContainer pixels = image.getPixels();
        double[][][][][] value = pixels.getAllPixels(root);

        int sizeX = pixels.getSizeX();
        int sizeY = pixels.getSizeY();
        int sizeZ = pixels.getSizeZ();
        int sizeC = pixels.getSizeC();
        int sizeT = pixels.getSizeT();

        assertEquals(sizeX, value[0][0][0][0].length);
        assertEquals(sizeY, value[0][0][0].length);
        assertEquals(sizeC, value[0][0].length);
        assertEquals(sizeZ, value[0].length);
        assertEquals(sizeT, value.length);
    }

    public void testGetRawDataBound()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);
        PixelContainer pixels = image.getPixels();

        int xBound[] = {0, 2};
        int yBound[] = {0, 2};
        int cBound[] = {0, 2};
        int zBound[] = {0, 2};
        int tBound[] = {0, 2};

        double[][][][][] value = pixels.getAllPixels(root, xBound, yBound, cBound, zBound, tBound);

        assertEquals(3, value[0][0][0][0].length);
        assertEquals(3, value[0][0][0].length);
        assertEquals(3, value[0][0].length);
        assertEquals(3, value[0].length);
        assertEquals(3, value.length);
    }

    public void testGetRawDataBoundError()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);
        PixelContainer pixels = image.getPixels();

        int xBound[] = {500, 550};
        try {
            pixels.getAllPixels(root, xBound, null, null, null, null);
            assert(false);
        } catch (Exception e) {
            assert(true);
        }
    }

    public void testGetRawDataBoundErrorNegative()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);
        PixelContainer pixels = image.getPixels();

        int xBound[] = {-1, 3};
        try {
            pixels.getAllPixels(root, xBound, null, null, null, null);
            assert(false);
        } catch (Exception e) {
            assert(true);
        }
    }

    public void testToImagePlusBound()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);
        
        int xBound[] = {0, 2};
        int yBound[] = {0, 2};
        int cBound[] = {0, 2};
        int zBound[] = {0, 2};
        int tBound[] = {0, 2};

        ImagePlus imp = image.toImagePlus(root, xBound, yBound, cBound, zBound, tBound);

        int dimensions[] = imp.getDimensions();

        assertEquals(3, dimensions[0]);
        assertEquals(3, dimensions[1]);
        assertEquals(3, dimensions[2]);
        assertEquals(3, dimensions[3]);
        assertEquals(3, dimensions[4]);
    }

    public void testToImagePlus()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);
        
        ImagePlus imp = image.toImagePlus(root);

        int dimensions[] = imp.getDimensions();

        assert(512 == dimensions[0]);
        assert(512 == dimensions[1]);
        assert(5 == dimensions[2]);
        assert(3 == dimensions[3]);
        assert(7 == dimensions[4]);
    }

    public void testGetImageChannel()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);
        assertEquals("0", image.getChannelName(root, 0));
    }

    public void testGetImageChannelError()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        try {
            image.getChannelName(root, 6);
            assert(false);
        }
        catch(Exception e) {
            assert(true);
        }
    }

    public void testAddTagToImage()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(3L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "image tag", "tag attached to an image");
        
        image.addTag(root, tag);

        List<TagAnnotationContainer> tags = image.getTags(root);

        assert(tags.size() == 1);

        root.deleteTag(tag);

        tags = image.getTags(root);

        assert(tags.size() == 0);
    }

    public void testAddTagToImage2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(3L);
        
        image.addTag(root, "image tag", "tag attached to an image");

        List<TagAnnotationContainer> tags = root.getTags("image tag");
        assert(tags.size() == 1);

        root.deleteTag(tags.get(0).getId());

        tags = root.getTags("image tag");
        assert(tags.size() == 0);
    }

    public void testAddTagIdToImage()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(3L);

        TagAnnotationContainer tag = new TagAnnotationContainer(root, "image tag", "tag attached to an image");
        
        image.addTag(root, tag.getId());

        List<TagAnnotationContainer> tags = image.getTags(root);

        assert(tags.size() == 1);

        root.deleteTag(tag);

        tags = image.getTags(root);

        assert(tags.size() == 0);
    }

    public void testAddTagsToImage()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(3L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Image tag", "tag attached to an image");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Image tag", "tag attached to an image");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Image tag", "tag attached to an image");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Image tag", "tag attached to an image");
        
        image.addTags(root, tag1.getId(), tag2.getId(), tag3.getId(), tag4.getId());

        List<TagAnnotationContainer> tags = image.getTags(root);

        assert(tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = image.getTags(root);

        assert(tags.size() == 0);
    }

    public void testAddTagsToImage2()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(3L);

        TagAnnotationContainer tag1 = new TagAnnotationContainer(root, "Image tag", "tag attached to an image");
        TagAnnotationContainer tag2 = new TagAnnotationContainer(root, "Image tag", "tag attached to an image");
        TagAnnotationContainer tag3 = new TagAnnotationContainer(root, "Image tag", "tag attached to an image");
        TagAnnotationContainer tag4 = new TagAnnotationContainer(root, "Image tag", "tag attached to an image");
        
        image.addTags(root, tag1, tag2, tag3, tag4);

        List<TagAnnotationContainer> tags = image.getTags(root);

        assert(tags.size() == 4);

        root.deleteTag(tag1);
        root.deleteTag(tag2);
        root.deleteTag(tag3);
        root.deleteTag(tag4);

        tags = image.getTags(root);

        assert(tags.size() == 0);
    }

    public void testImageOrder()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<ImageContainer> images = root.getImages();

        for(int i = 1; i < images.size(); i++) {
            assert(images.get(i - 1).getId() <= images.get(i).getId());
        }
    }

    public void testGetTagInfo()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        TagAnnotationContainer tag = root.getTag(1L);

        assert(1L == tag.getId());
        assertEquals("tag1", tag.getName());
        assertEquals("description", tag.getDescription());
    }

    public void testGetTags()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<TagAnnotationContainer> tags = root.getTags();

        assert(tags.size() == 3);
    }

    public void testGetTagsSorted()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        List<TagAnnotationContainer> tags = root.getTags();

            for(int i = 1; i < tags.size(); i++) {
            assert(tags.get(i - 1).getId() <= tags.get(i).getId());
        }
    }

    public void testAddFileDataset()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        DatasetContainer dataset = root.getDataset(1L);

        File file = new File("./test.txt");
        file.createNewFile();

        Long id = dataset.addFile(root, file).getId().getValue();
        file.delete();

        root.deleteFile(id);
    }

    public void testAddFileimage()
        throws Exception
    {
        Client root = new Client();
        root.connect("omero", 4064, "root", "omero", 3L);

        ImageContainer image = root.getImage(1L);

        File file = new File("./test.txt");
        file.createNewFile();

        Long id = image.addFile(root, file).getId().getValue();
        file.delete();

        root.deleteFile(id);
    }
}
