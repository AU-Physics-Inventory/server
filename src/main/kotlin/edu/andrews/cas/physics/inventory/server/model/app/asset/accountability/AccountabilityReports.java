package edu.andrews.cas.physics.inventory.server.model.app.asset.accountability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.andrews.cas.physics.inventory.measurement.OperationOnQuantitiesException;
import edu.andrews.cas.physics.inventory.measurement.Quantity;
import edu.andrews.cas.physics.inventory.measurement.Unit;
import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.model.app.asset.Asset;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class AccountabilityReports implements IDocumentConversion {
    @JsonIgnore
    private Unit unit;
    private List<MissingReport> missingReports;
    private List<RecoveryReport> recoveryReports;
    private Quantity quantityMissing;

    public AccountabilityReports() {
        this.unit = Unit.UNITS;
        this.missingReports = new ArrayList<>();
        this.recoveryReports = new ArrayList<>();
        this.quantityMissing = null;
    }

    public AccountabilityReports(Unit unit) {
        this.unit = unit;
        this.missingReports = new ArrayList<>();
        this.recoveryReports = new ArrayList<>();
        this.quantityMissing = null;
    }

    public AccountabilityReports(List<MissingReport> missingReports, List<RecoveryReport> recoveryReports) {
        this.unit = Unit.UNITS;
        this.missingReports = missingReports;
        this.recoveryReports = recoveryReports;
        this.quantityMissing = null;
    }

    public AccountabilityReports(Unit unit, List<MissingReport> missingReports, List<RecoveryReport> recoveryReports) {
        this.unit = unit;
        this.missingReports = missingReports;
        this.recoveryReports = recoveryReports;
        this.quantityMissing = null;
    }

    public static AccountabilityReports fromDocument(Document d) {
        return new AccountabilityReports()
                .missingReports(d.getList("missingReports", Document.class).parallelStream().map(MissingReport::fromDocument).toList())
                .recoveryReports(d.getList("recoveryReports", Document.class).parallelStream().map(RecoveryReport::fromDocument).toList())
                .quantityMissing(Quantity.fromDocument((Document) d.get("quantity")));
    }

    private AccountabilityReports missingReports(List<MissingReport> missingReports) {
        this.missingReports = missingReports;
        return this;
    }

    private AccountabilityReports recoveryReports(List<RecoveryReport> recoveryReports) {
        this.recoveryReports = recoveryReports;
        return this;
    }

    private AccountabilityReports quantityMissing(Quantity quantityMissing) {
        this.quantityMissing = quantityMissing;
        return this;
    }

    public List<MissingReport> getMissingReports() {
        return missingReports;
    }

    public List<RecoveryReport> getRecoveryReports() {
        return recoveryReports;
    }

    public Quantity getQuantityMissing() throws OperationOnQuantitiesException {
        return calculateQuantityMissing();
    }

//    public void setQuantityMissing(Quantity quantityMissing) {
//        this.quantityMissing = quantityMissing;
//    }

    public Quantity calculateQuantityMissing() throws OperationOnQuantitiesException {
        quantityMissing = new Quantity(0, this.unit);
        for (MissingReport report : missingReports) {
            quantityMissing = quantityMissing.add(report.quantityMissing());
        }
        for (RecoveryReport report : recoveryReports) {
            quantityMissing = quantityMissing.subtract(report.quantityRecovered());
        }
        return quantityMissing;
    }

    public void addMissingReport(MissingReport missingReport) {
        if (missingReport.quantityMissing().getValue() > 0) this.missingReports.add(missingReport);
    }

    public void addRecoveryReport(RecoveryReport recoveryReport) {
        this.recoveryReports.add(recoveryReport);
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @Override
    public Document toDocument() {
        Document d = new Document();
        try {
            d.put("missingReports", getMissingReports().parallelStream().map(MissingReport::toDocument).toList());
            d.put("recoveryReports", getRecoveryReports().parallelStream().map(RecoveryReport::toDocument).toList());
            d.put("quantity", getQuantityMissing().toDocument());
        } catch (OperationOnQuantitiesException e) {
            e.printStackTrace();
            d.put("quantity", null);
        }
        return d;
    }
}
