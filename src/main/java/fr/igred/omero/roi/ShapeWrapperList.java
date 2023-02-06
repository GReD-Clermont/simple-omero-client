package fr.igred.omero.roi;


import fr.igred.omero.WrapperList;
import omero.gateway.model.ShapeData;


/** ArrayList of Shape Objects implementing the ShapeList interface */
public class ShapeWrapperList extends WrapperList<ShapeData, Shape<?>> implements ShapeList {

    private static final long serialVersionUID = -602818154494998324L;

}
