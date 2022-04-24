package edu.andrews.cas.physics.inventory.measurement;

import lombok.NonNull;

public class Enumerable extends Measurement {
    public final static Unit siUnit = Unit.UNITS;

    public Enumerable() {
        super(siUnit);
    }

    @Override
    public Quantity convert(@NonNull Quantity q, @NonNull Unit to) throws OperationOnQuantitiesException {
        throw new OperationOnQuantitiesException("Unit is not convertable.");
    }

    @Override
    void loadConversions() {}
}
