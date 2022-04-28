package edu.andrews.cas.physics.inventory.server.model.app.asset.accountability;

import edu.andrews.cas.physics.inventory.measurement.Quantity;
import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.LocalDate;

public record MissingReport(@BsonProperty("quantity") Quantity quantityMissing, @BsonProperty("date") LocalDate reportDate, String reportedBy) implements IDocumentConversion {
    @Override
    public Document toDocument() {
        return new Document()
                .append("quantity", this.quantityMissing().toDocument())
                .append("date", this.reportDate())
                .append("reportedBy", this.reportedBy());
    }

    public static MissingReport fromDocument(Document d) {
        Quantity quantityMissing = Quantity.fromDocument((Document) d.get("quantity"));
        LocalDate date = ConversionHelper.parseDate(d.getDate("date"));
        String reportedBy = d.getString("reportedBy");
        return new MissingReport(quantityMissing, date, reportedBy);
    }
}