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

package fr.igred.omero.util;


import fr.igred.omero.BasicTest;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.FileAnnotation;
import fr.igred.omero.annotations.MapAnnotation;
import fr.igred.omero.annotations.RatingAnnotation;
import fr.igred.omero.annotations.TagAnnotation;
import fr.igred.omero.annotations.TextualAnnotation;
import fr.igred.omero.containers.Folder;
import fr.igred.omero.containers.Project;
import fr.igred.omero.core.Channel;
import fr.igred.omero.core.Image;
import fr.igred.omero.core.PlaneInfo;
import fr.igred.omero.meta.Experimenter;
import fr.igred.omero.meta.Group;
import fr.igred.omero.containers.Dataset;
import fr.igred.omero.core.Pixels;
import fr.igred.omero.roi.Line;
import fr.igred.omero.roi.Mask;
import fr.igred.omero.roi.Polygon;
import fr.igred.omero.roi.ROI;
import fr.igred.omero.roi.Rectangle;
import fr.igred.omero.roi.Text;
import fr.igred.omero.screen.PlateAcquisition;
import fr.igred.omero.screen.Plate;
import fr.igred.omero.screen.Screen;
import fr.igred.omero.screen.WellSample;
import fr.igred.omero.screen.Well;
import fr.igred.omero.roi.Ellipse;
import fr.igred.omero.roi.Point;
import fr.igred.omero.roi.Polyline;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.EllipseData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FolderData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.LineData;
import omero.gateway.model.MapAnnotationData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.PlaneInfoData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.PointData;
import omero.gateway.model.PolygonData;
import omero.gateway.model.PolylineData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ROIData;
import omero.gateway.model.RatingAnnotationData;
import omero.gateway.model.RectangleData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.ShapeData;
import omero.gateway.model.TagAnnotationData;
import omero.gateway.model.TextData;
import omero.gateway.model.TextualAnnotationData;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;
import omero.model.CommentAnnotationI;
import omero.model.FileAnnotationI;
import omero.model.RectangleI;
import omero.model.TagAnnotationI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;


@SuppressWarnings({"OverlyCoupledMethod", "OverlyCoupledClass"})
class WrapperTest extends BasicTest {

    private static Stream<Arguments> classes() {
        return Stream.of(
                /* These first classes do not have a default constructor. */
                //arguments(named("ChannelData", ChannelData.class, Channel.class),
                //arguments(named("FileAnnotationData", FileAnnotationData.class, FileAnnotation.class),
                //arguments(named("TagAnnotationData", TagAnnotationData.class, TagAnnotation.class),
                //arguments(named("TextualAnnotationData", TextualAnnotationData.class), TextualAnnotation.class),
                arguments(named("RatingAnnotationData", RatingAnnotationData.class), RatingAnnotation.class),
                arguments(named("MapAnnotationData", MapAnnotationData.class), MapAnnotation.class),
                arguments(named("ProjectData", ProjectData.class), Project.class),
                arguments(named("DatasetData", DatasetData.class), Dataset.class),
                arguments(named("ImageData", ImageData.class), Image.class),
                arguments(named("ScreenData", ScreenData.class), Screen.class),
                arguments(named("PlateData", PlateData.class), Plate.class),
                arguments(named("PlateAcquisitionData", PlateAcquisitionData.class), PlateAcquisition.class),
                arguments(named("WellData", WellData.class), Well.class),
                arguments(named("FolderData", FolderData.class), Folder.class),
                arguments(named("RectangleData", RectangleData.class), Rectangle.class),
                arguments(named("PolygonData", PolygonData.class), Polygon.class),
                arguments(named("PolylineData", PolylineData.class), Polyline.class),
                arguments(named("EllipseData", EllipseData.class), Ellipse.class),
                arguments(named("PointData", PointData.class), Point.class),
                arguments(named("LineData", LineData.class), Line.class),
                arguments(named("TextData", TextData.class), Text.class),
                arguments(named("MaskData", MaskData.class), Mask.class),
                arguments(named("PixelsData", PixelsData.class), Pixels.class),
                arguments(named("ROIData", ROIData.class), ROI.class),
                arguments(named("PlaneInfoData", PlaneInfoData.class), PlaneInfo.class),
                arguments(named("WellSampleData", WellSampleData.class), WellSample.class),
                arguments(named("ExperimenterData", ExperimenterData.class), Experimenter.class),
                arguments(named("GroupData", GroupData.class), Group.class)
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("classes")
    <T extends DataObject, U extends RemoteObject> void testWrap(Class<T> input, Class<U> output)
    throws Exception {
        T object = input.getConstructor().newInstance();
        U result = Wrapper.wrap(object);
        assertSame(output, result.getClass());
    }


    @Test
    void testWrapChannelData() {
        ChannelData object = new ChannelData(0);
        assertSame(Channel.class, Wrapper.wrap(object).getClass());
    }


    @Test
    void testWrapFileAnnotationData() {
        FileAnnotationData object = new FileAnnotationData(new FileAnnotationI());
        assertSame(FileAnnotation.class, Wrapper.wrap(object).getClass());
    }


    @Test
    void testWrapTagAnnotationData() {
        TagAnnotationData object = new TagAnnotationData(new TagAnnotationI());
        assertSame(TagAnnotation.class, Wrapper.wrap(object).getClass());
    }


    @Test
    void testWrapTextualAnnotationData() {
        TextualAnnotationData object = new TextualAnnotationData(new CommentAnnotationI());
        assertSame(TextualAnnotation.class, Wrapper.wrap(object).getClass());
    }


    @Test
    void testWrapWrongDataObject() {
        DataObject object = new WrongDataObject();
        assertThrows(IllegalArgumentException.class, () -> Wrapper.wrap(object));
    }


    @Test
    void testWrapWrongShapeData() {
        ShapeData object = new WrongShapeData();
        assertThrows(IllegalArgumentException.class, () -> Wrapper.wrap(object));
    }


    @Test
    void testWrapWrongAnnotationData() {
        AnnotationData object = new WrongAnnotationData();
        assertThrows(IllegalArgumentException.class, () -> Wrapper.wrap(object));
    }


    private static class WrongDataObject extends DataObject {

        WrongDataObject() {
        }

    }


    private static class WrongShapeData extends ShapeData {

        WrongShapeData() {
            super(new RectangleI());
        }

    }


    private static class WrongAnnotationData extends AnnotationData {

        WrongAnnotationData() {
            super(new TagAnnotationI());
        }


        @Override
        public Object getContent() {
            return "null";
        }


        @Override
        public void setContent(Object content) {
        }


        @Override
        public String getContentAsString() {
            return "null";
        }

    }

}
