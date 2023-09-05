package edu.andrews.cas.physics.inventory.server.model.app.lab.resource;

import edu.andrews.cas.physics.inventory.measurement.Quantity;
import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import org.bson.Document;

public record Quantities(Quantity frontTable, Quantity perStation) implements IDocumentConversion {
    @Override
    public Document toDocument() {
        return new Document()
                .append("frontTable", this.frontTable().toDocument())
                .append("perStation", this.perStation().toDocument());
    }
}
