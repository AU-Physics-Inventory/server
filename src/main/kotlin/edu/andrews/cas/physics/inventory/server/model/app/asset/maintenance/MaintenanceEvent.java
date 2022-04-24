package edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bson.Document;

import java.time.LocalDate;

public record MaintenanceEvent(Status status, LocalDate effectiveDate) implements IDocumentConversion {
    public static MaintenanceEvent fromDocument(Document d) {
        Status status = Status.lookup(d.getString("status"));
        LocalDate effectiveDate = ConversionHelper.parseDate(d.getDate("effectiveDate"));
        return new MaintenanceEvent(status, effectiveDate);
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("status", this.status().getCode())
                .append("effectiveDate", this.effectiveDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MaintenanceEvent that = (MaintenanceEvent) o;

        return new EqualsBuilder().append(status, that.status).append(effectiveDate, that.effectiveDate).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(status).append(effectiveDate).toHashCode();
    }
}
