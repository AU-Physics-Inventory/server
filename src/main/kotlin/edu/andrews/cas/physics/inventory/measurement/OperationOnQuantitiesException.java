package edu.andrews.cas.physics.inventory.measurement;

public class OperationOnQuantitiesException extends Exception {
    public OperationOnQuantitiesException(ReflectiveOperationException e) {
        super();
    }

    public OperationOnQuantitiesException(String s) {
        super(s);
    }
}
