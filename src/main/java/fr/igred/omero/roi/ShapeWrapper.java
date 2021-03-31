package fr.igred.omero.roi;


import omero.gateway.model.ShapeData;


public class ShapeWrapper extends GenericShapeWrapper<ShapeData> {

    /**
     * Constructor of the ShapeWrapper class using a ShapeData.
     *
     * @param shape the shape
     */
    public ShapeWrapper(ShapeData shape) {
        super(shape);
    }

}
