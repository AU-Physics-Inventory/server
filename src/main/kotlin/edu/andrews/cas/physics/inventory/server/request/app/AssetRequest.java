package edu.andrews.cas.physics.inventory.server.request.app;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDate;
import java.util.List;

public class AssetRequest {
    private String name;
    private String location;
    private List<String> keywords;
    private List<String> images;
    private Integer identityNo;
    private String AUInventoryNo;
    private boolean consumable;
    private String brand;
    private String model;
    private String partNo;
    private String serialNo;
    private String vendor;
    private String vendorURL;
    private LocalDate purchaseDate;
    private Double cost;
    private Double unitPrice;
    private double quantity;
    private String unit;
    private String purchaseURL;
    private String receipt;
    private String notes;
    private LocalDate nextCalibrationDate;
    private Integer calibrationInterval;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Integer getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(Integer identityNo) {
        this.identityNo = identityNo;
    }

    public String getAUInventoryNo() {
        return AUInventoryNo;
    }

    public void setAUInventoryNo(String AUInventoryNo) {
        this.AUInventoryNo = AUInventoryNo;
    }

    public boolean isConsumable() {
        return consumable;
    }

    public void setConsumable(boolean consumable) {
        this.consumable = consumable;
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

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVendorURL() {
        return vendorURL;
    }

    public void setVendorURL(String vendorURL) {
        this.vendorURL = vendorURL;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPurchaseURL() {
        return purchaseURL;
    }

    public void setPurchaseURL(String purchaseURL) {
        this.purchaseURL = purchaseURL;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getNextCalibrationDate() {
        return nextCalibrationDate;
    }

    public void setNextCalibrationDate(LocalDate nextCalibrationDate) {
        this.nextCalibrationDate = nextCalibrationDate;
    }

    public Integer getCalibrationInterval() {
        return calibrationInterval;
    }

    public void setCalibrationInterval(Integer calibrationInterval) {
        this.calibrationInterval = calibrationInterval;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("location", location)
                .append("keywords", keywords)
                .append("images", images)
                .append("identityNo", identityNo)
                .append("AUInventoryNo", AUInventoryNo)
                .append("consumable", consumable)
                .append("brand", brand)
                .append("model", model)
                .append("partNo", partNo)
                .append("serialNo", serialNo)
                .append("vendor", vendor)
                .append("vendorURL", vendorURL)
                .append("purchaseDate", purchaseDate)
                .append("cost", cost)
                .append("unitPrice", unitPrice)
                .append("quantity", quantity)
                .append("unit", unit)
                .append("purchaseURL", purchaseURL)
                .append("receipt", receipt)
                .append("notes", notes)
                .append("nextCalibrationDate", nextCalibrationDate)
                .append("calibrationInterval", calibrationInterval)
                .toString();
    }
}
