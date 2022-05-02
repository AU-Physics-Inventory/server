package edu.andrews.cas.physics.inventory.server.model.app.asset;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper;
import lombok.NonNull;
import org.bson.Document;

import java.net.URL;

public record Vendor(@NonNull String name, URL url) implements IDocumentConversion {
    public static Vendor fromDocument(Document d) {
        String name = d.getString("name");
        URL url = ConversionHelper.parseURL(d.getString("url"));
        return new Vendor(name, url);
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("name", this.name())
                .append("url", this.url() == null ? null : this.url().toString());
    }
}
