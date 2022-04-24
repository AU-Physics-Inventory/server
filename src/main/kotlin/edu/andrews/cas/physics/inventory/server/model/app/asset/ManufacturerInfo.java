package edu.andrews.cas.physics.inventory.server.model.app.asset;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import org.bson.Document;

public record ManufacturerInfo(String brand, String model, String partNo,
                               String serialNo) implements IDocumentConversion {
    public static ManufacturerInfo fromDocument(Document mfrInfo) {
        String brand = mfrInfo.getString("brand");
        String model = mfrInfo.getString("model");
        String partNo = mfrInfo.getString("partNo");
        String serialNo = mfrInfo.getString("serialNo");
        return new ManufacturerInfo(brand, model, partNo, serialNo);
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("brand", this.brand())
                .append("model", this.model())
                .append("partNo", this.partNo())
                .append("serialNo", this.serialNo());
    }
}
