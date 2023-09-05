package edu.andrews.cas.physics.inventory.server.model.app.asset;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import org.bson.Document;

import java.util.Objects;

public class ManufacturerInfo implements IDocumentConversion {
    private String brand;
    private String model;
    private String partNo;
    private String serialNo;

    public ManufacturerInfo(String brand, String model, String partNo,
                            String serialNo) {
        this.brand = brand;
        this.model = model;
        this.partNo = partNo;
        this.serialNo = serialNo;
    }

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
                .append("brand", this.getBrand())
                .append("model", this.getModel())
                .append("partNo", this.getPartNo())
                .append("serialNo", this.getSerialNo());
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPartNo() {
        return partNo;
    }

    public void setPartNo(String partNo) {
        this.partNo = partNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ManufacturerInfo) obj;
        return Objects.equals(this.brand, that.brand) &&
                Objects.equals(this.model, that.model) &&
                Objects.equals(this.partNo, that.partNo) &&
                Objects.equals(this.serialNo, that.serialNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brand, model, partNo, serialNo);
    }

    @Override
    public String toString() {
        return "ManufacturerInfo[" +
                "brand=" + brand + ", " +
                "model=" + model + ", " +
                "partNo=" + partNo + ", " +
                "serialNo=" + serialNo + ']';
    }

}
