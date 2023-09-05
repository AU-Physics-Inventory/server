package edu.andrews.cas.physics.inventory.server.model.app.asset.accountability;

import edu.andrews.cas.physics.inventory.measurement.Quantity;
import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper;
import lombok.NonNull;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.LocalDate;

public record RecoveryReport(@NonNull @BsonProperty("quantity") Quantity quantityRecovered,
                             @NonNull @BsonProperty("date") LocalDate dateRecovered,
                             @NonNull String reportedBy) implements IDocumentConversion {
    public static RecoveryReport fromDocument(Document d) {
        Quantity quantityRecovered = Quantity.fromDocument((Document) d.get("quantity"));
        LocalDate date = ConversionHelper.parseDate(d.getString("date"));
        String reportedBy = d.getString("reportedBy");
        return new RecoveryReport(quantityRecovered, date, reportedBy);
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("quantity", this.quantityRecovered().toDocument())
                .append("date", this.dateRecovered())
                .append("reportedBy", this.reportedBy());
    }
}
