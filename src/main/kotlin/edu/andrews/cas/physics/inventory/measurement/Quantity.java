package edu.andrews.cas.physics.inventory.measurement;

import com.fasterxml.jackson.annotation.JsonGetter;
import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import lombok.NonNull;
import org.bson.Document;

public class Quantity implements IDocumentConversion {
    private Double value;
    private Unit unit;

    public Quantity(double value) {
        this.value = value;
        this.unit = Unit.UNITS;
    }

    public Quantity(double value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    private Quantity() {
    }

    public static boolean compareUnits(@NonNull Quantity q1, @NonNull Quantity q2) {
        return q1.getUnit().equals(q2.getUnit());
    }

    public static Quantity fromDocument(Document d) {
        return new Quantity()
                .value(d.getDouble("amount"))
                .unit(Unit.lookup(d.getString("unit")));
    }

    private Quantity value(Double value) {
        this.value = value;
        return this;
    }

    private Quantity unit(Unit unit) {
        this.unit = unit;
        return this;
    }

    public Quantity add(Quantity q) throws OperationOnQuantitiesException {
        try {
            return sameUnitsAs(q)
                    ? new Quantity(q.getValue() + this.getValue(), this.getUnit())
                    : new Quantity((
                    (Quantity) this.unit.getMeasurementClass()
                            .getMethod("convert", Quantity.class, Unit.class)
                            .invoke(this.unit.getMeasurementClass()
                                            .getConstructor()
                                            .newInstance(),
                                    q, this.getUnit()))
                    .getValue() + this.getValue(), this.getUnit());
        } catch (ReflectiveOperationException e) {
            throw new OperationOnQuantitiesException(e);
        }
    }

    public Quantity subtract(Quantity q) throws OperationOnQuantitiesException {
        try {
            return sameUnitsAs(q)
                    ? new Quantity(q.getValue() + this.getValue(), this.getUnit())
                    : new Quantity((
                    (Quantity) this.unit.getMeasurementClass()
                            .getMethod("convert", Quantity.class, Unit.class)
                            .invoke(this.unit.getMeasurementClass()
                                            .getConstructor()
                                            .newInstance(),
                                    q, this.getUnit()))
                    .getValue() - this.getValue(), this.getUnit());
        } catch (ReflectiveOperationException e) {
            throw new OperationOnQuantitiesException(e);
        }
    }

    public Quantity divide(Quantity q) throws OperationOnQuantitiesException {
        try {
            return sameUnitsAs(q)
                    ? new Quantity(q.getValue() + this.getValue(), this.getUnit())
                    : new Quantity((
                    (Quantity) this.unit.getMeasurementClass()
                            .getMethod("convert", Quantity.class, Unit.class)
                            .invoke(this.unit.getMeasurementClass()
                                            .getConstructor()
                                            .newInstance(),
                                    q, this.getUnit()))
                    .getValue() / this.getValue(), this.getUnit());
        } catch (ReflectiveOperationException e) {
            throw new OperationOnQuantitiesException(e);
        }
    }

    public Quantity multiply(Quantity q) throws OperationOnQuantitiesException {
        try {
            return sameUnitsAs(q)
                    ? new Quantity(q.getValue() + this.getValue(), this.getUnit())
                    : new Quantity((
                    (Quantity) this.unit.getMeasurementClass()
                            .getMethod("convert", Quantity.class, Unit.class)
                            .invoke(this.unit.getMeasurementClass()
                                            .getConstructor()
                                            .newInstance(),
                                    q, this.getUnit()))
                    .getValue() * this.getValue(), this.getUnit());
        } catch (ReflectiveOperationException e) {
            throw new OperationOnQuantitiesException(e);
        }
    }

    public Double getValue() {
        return value;
    }

    public Unit getUnit() {
        return unit;
    }

    @JsonGetter("unit")
    public String getUnitSymbol() {
        return getUnit().getSymbol();
    }

    public boolean sameUnitsAs(@NonNull Quantity q) {
        return sameUnitsAs(q.getUnit());
    }

    public boolean sameUnitsAs(@NonNull Unit u) {
        return this.unit.equals(u);
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("amount", getValue())
                .append("unit", getUnit().toString());
    }
}
