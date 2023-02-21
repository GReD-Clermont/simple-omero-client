[![Java CI with Maven](https://github.com/GReD-Clermont/simple-omero-client/actions/workflows/maven.yml/badge.svg)](https://github.com/GReD-Clermont/simple-omero-client/actions/workflows/maven.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=GReD-Clermont_simple-omero-client&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=GReD-Clermont_simple-omero-client)
[![codecov](https://codecov.io/gh/GReD-Clermont/simple-omero-client/branch/main/graph/badge.svg)](https://codecov.io/gh/GReD-Clermont/simple-omero-client)
[![DOI](https://img.shields.io/badge/DOI-10.12688%2Ff1000research.110385.2-GREEN)](https://doi.org/10.12688/f1000research.110385.2)

# simple-omero-client

A Maven project to easily connect to OMERO.

This library presents a simplified API to put/get objects on an OMERO server. 
<p>It can be used from Maven.
<p>The JAR can be put into the ImageJ plugins folder to use it from scripts.


## How to use


### Connection
The main entry point is the Client class, which can be used to retrieve, save or delete objects.

<p>To use it, a connection has to be established first:

```java
Client client = new Client();
client.connect("host", 4064, "username", password, groupId);
```

### Repository objects (projects, datasets, images, screens, wells, plates)

It can then be used to retrieve all the repository objects the user has access to, like projects or datasets:

```java
List<ProjectWrapper> projects = client.getProjects();
List<DatasetWrapper> datasets = client.getDatasets();
```

These objects can then be used to retrieve their children:

```java
for (DatasetWrapper dataset:datasets) {
    List<ImageWrapper> images = dataset.getImages(client);
    //...
}
```

### Annotations

For each type of objects (project, dataset or image), annotations can be retrieved/added, such as:

* #### Tags:

```java
TagAnnotationWrapper tag = new TagAnnotationWrapper(client, "name", "description");
dataset.addTag(client, tag);
List<TagAnnotationWrapper> tags = dataset.getTags(client);
```

* #### Key/Value pairs:

```java
dataset.addPairKeyValue(client, "key", "value");
String value = dataset.getValue(client, "key");
```

* #### Tables:

```java
TableWrapper table = new TableWrapper(columnCount, "name");
dataset.addTable(client, table);
List<TableWrapper> tables = dataset.getTables(client);
```

* #### Files:

```java
File file = new File("file.csv");
dataset.addFile(client, file);
```

### Images

Pixel intensities can be downloaded from images to a Java array or as an ImagePlus:

```java
double[][][][][] pixels = image.getPixels().getAllPixels(client);
ImagePlus imp = image.toImagePlus(client);
```

Thumbnails of the specified size can be retrieved:

```java
int size = 128;
BufferedImage thumbnail = image.getThumbnail(client, size);
```

### ROIs

ROIs can be added to images or retrieved from them:

```java
ROIWrapper roi = new ROIWrapper();
roi.addShape(new RectangleWrapper(0, 0, 5, 5));
roi.setImage(image);
image.saveROI(client, roi);
List<ROIWrapper> rois = image.getROIs(client);
```

They can also be converted from or to ImageJ Rois:

```java
// The property is a string used to create 3D/4D ROIs in OMERO, by grouping shapes sharing the same value (e.g. local index).
// The ROI name can be set/accessed using the property ROI.ijNameProperty(property).
// The OMERO IDs are available through the property ROI.ijIDProperty(property).
// The ijNameProperty() and ijIDProperty() methods append "_NAME" and "_ID" to the property (respectively).
// By default, the property is "ROI", and thus, the name property is "ROI_NAME" and the ID property, "ROI_ID".
ROIWrapper roi = new ROIWrapper();
roi.setName("ROI name");
roi.addShape(new RectangleWrapper(0, 0, 5, 5));
List<Roi> imagejRois = roi.toImageJ();
String name = imagejRois.get(0).getProperty(ROI.ijNameProperty(property));
String ID = imagejRois.get(0).getProperty(ROI.ijIDProperty(property));

// Conversely ImageJ Rois can be converted to OMERO from ImageJ using "ROIWrapper::fromImageJ"
Roi ijRoi1 = new Roi(1.0, 2.0, 3.0, 4.0);
ijRoi1.setProperty(property, 0);
ijRoi1.setProperty(ROIWrapper.ijNameProperty(property), "Name 1");
Roi ijRoi2 = new Roi(2.0, 3.0, 4.0, 5.0);
ijRoi2.setProperty(property, 1);
ijRoi2.setProperty(ROIWrapper.ijNameProperty(property), "Name 2");
List<Roi> ijRois = new ArrayList(2);
ijRois.add(ijRoi1);
ijRois.add(ijRoi2);
List<ROIWrapper> rois = ROIWrapper.fromImageJ(ijRois);
```

## License
[GPLv2+](https://choosealicense.com/licenses/gpl-2.0/)
