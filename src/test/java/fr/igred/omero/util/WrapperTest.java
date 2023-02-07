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
import fr.igred.omero.OMEROList;
import fr.igred.omero.RemoteObject;
import fr.igred.omero.annotations.Annotation;
import fr.igred.omero.annotations.AnnotationWrapperList;
import fr.igred.omero.annotations.FileAnnotationWrapper;
import fr.igred.omero.annotations.MapAnnotationWrapper;
import fr.igred.omero.annotations.RatingAnnotationWrapper;
import fr.igred.omero.annotations.TagAnnotationWrapper;
import fr.igred.omero.annotations.TextualAnnotationWrapper;
import fr.igred.omero.meta.ExperimenterWrapper;
import fr.igred.omero.meta.GroupWrapper;
import fr.igred.omero.core.PlaneInfoWrapper;
import fr.igred.omero.core.ChannelWrapper;
import fr.igred.omero.repository.DatasetWrapper;
import fr.igred.omero.repository.FolderWrapper;
import fr.igred.omero.core.ImageWrapper;
import fr.igred.omero.core.PixelsWrapper;
import fr.igred.omero.repository.PlateAcquisitionWrapper;
import fr.igred.omero.repository.PlateWrapper;
import fr.igred.omero.repository.ProjectWrapper;
import fr.igred.omero.repository.ScreenWrapper;
import fr.igred.omero.repository.WellSampleWrapper;
import fr.igred.omero.repository.WellWrapper;
import fr.igred.omero.roi.EllipseWrapper;
import fr.igred.omero.roi.LineWrapper;
import fr.igred.omero.roi.MaskWrapper;
import fr.igred.omero.roi.PointWrapper;
import fr.igred.omero.roi.PolygonWrapper;
import fr.igred.omero.roi.PolylineWrapper;
import fr.igred.omero.roi.ROIWrapper;
import fr.igred.omero.roi.RectangleWrapper;
import fr.igred.omero.roi.TextWrapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;


@SuppressWarnings({"OverlyCoupledMethod", "OverlyCoupledClass"})
class WrapperTest extends BasicTest {

    private static Stream<Arguments> classes() {
        return Stream.of(
                /* These first classes do not have a default constructor. */
                //arguments(named("ChannelData", ChannelData.class, ChannelWrapper.class),
                //arguments(named("FileAnnotationData", FileAnnotationData.class, FileAnnotationWrapper.class),
                //arguments(named("TagAnnotationData", TagAnnotationData.class, TagAnnotationWrapper.class),
                //arguments(named("TextualAnnotationData", TextualAnnotationData.class), TextualAnnotationWrapper.class),
                arguments(named("RatingAnnotationData", RatingAnnotationData.class), RatingAnnotationWrapper.class),
                arguments(named("MapAnnotationData", MapAnnotationData.class), MapAnnotationWrapper.class),
                arguments(named("ProjectData", ProjectData.class), ProjectWrapper.class),
                arguments(named("DatasetData", DatasetData.class), DatasetWrapper.class),
                arguments(named("ImageData", ImageData.class), ImageWrapper.class),
                arguments(named("ScreenData", ScreenData.class), ScreenWrapper.class),
                arguments(named("PlateData", PlateData.class), PlateWrapper.class),
                arguments(named("PlateAcquisitionData", PlateAcquisitionData.class), PlateAcquisitionWrapper.class),
                arguments(named("WellData", WellData.class), WellWrapper.class),
                arguments(named("FolderData", FolderData.class), FolderWrapper.class),
                arguments(named("RectangleData", RectangleData.class), RectangleWrapper.class),
                arguments(named("PolygonData", PolygonData.class), PolygonWrapper.class),
                arguments(named("PolylineData", PolylineData.class), PolylineWrapper.class),
                arguments(named("EllipseData", EllipseData.class), EllipseWrapper.class),
                arguments(named("PointData", PointData.class), PointWrapper.class),
                arguments(named("LineData", LineData.class), LineWrapper.class),
                arguments(named("TextData", TextData.class), TextWrapper.class),
                arguments(named("MaskData", MaskData.class), MaskWrapper.class),
                arguments(named("PixelsData", PixelsData.class), PixelsWrapper.class),
                arguments(named("ROIData", ROIData.class), ROIWrapper.class),
                arguments(named("PlaneInfoData", PlaneInfoData.class), PlaneInfoWrapper.class),
                arguments(named("WellSampleData", WellSampleData.class), WellSampleWrapper.class),
                arguments(named("ExperimenterData", ExperimenterData.class), ExperimenterWrapper.class),
                arguments(named("GroupData", GroupData.class), GroupWrapper.class)
        );
    }


    @ParameterizedTest(name = "{0}")
    @MethodSource("classes")
    <T extends DataObject, U extends RemoteObject<? extends T>> void testWrap(Class<T> input, Class<U> output)
    throws Exception {
        T object = input.getConstructor().newInstance();
        U result = Wrapper.wrap(object);
        assertSame(output, result.getClass());
    }


    @Test
    void testWrapChannelData() {
        ChannelData object = new ChannelData(0);
        assertSame(ChannelWrapper.class, Wrapper.wrap(object).getClass());
    }


    @Test
    void testWrapFileAnnotationData() {
        FileAnnotationData object = new FileAnnotationData(new FileAnnotationI());
        assertSame(FileAnnotationWrapper.class, Wrapper.wrap(object).getClass());
    }


    @Test
    void testWrapTagAnnotationData() {
        TagAnnotationData object = new TagAnnotationData(new TagAnnotationI());
        assertSame(TagAnnotationWrapper.class, Wrapper.wrap(object).getClass());
    }


    @Test
    void testWrapTextualAnnotationData() {
        TextualAnnotationData object = new TextualAnnotationData(new CommentAnnotationI());
        assertSame(TextualAnnotationWrapper.class, Wrapper.wrap(object).getClass());
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


    @Test
    void testAddWrongAnnotationDataToAnnotationList() {
        OMEROList<AnnotationData, Annotation<?>> annotations = new AnnotationWrapperList();

        boolean added = annotations.add(new WrongAnnotationData());
        assertFalse(added);
        assertEquals(0, annotations.size());
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
