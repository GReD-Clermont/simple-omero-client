package fr.igred.omero.roi;


import omero.gateway.model.ShapeData;


public class ConcreteShapeWrapper extends GenericShapeWrapper<ShapeData> {

    /**
     * Constructor of the GenericShapeWrapper class using a ShapeData.
     *
     * @param shape the shape
     */
    public ConcreteShapeWrapper(ShapeData shape) {
        super(shape);
    }

}
