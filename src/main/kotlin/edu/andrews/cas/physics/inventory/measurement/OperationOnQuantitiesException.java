package edu.andrews.cas.physics.inventory.measurement;

public class OperationOnQuantitiesException extends RuntimeException {
    public OperationOnQuantitiesException(ReflectiveOperationException e) {
        super(e);
    }

    public OperationOnQuantitiesException(String s) {
        super(s);
    }
}
