package edu.andrews.cas.physics.inventory.server.model.app.asset;

import edu.andrews.cas.physics.inventory.measurement.Quantity;
import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bson.Document;

import java.net.URL;
import java.time.LocalDate;

public class Purchase implements IDocumentConversion {
    private Vendor vendor;
    private LocalDate date;
    private double cost;
    private double unitPrice;
    private Quantity quantity;
    private URL url;
    private String receipt;

    public Purchase(Vendor vendor, LocalDate date, double cost, double unitPrice, Quantity quantity) {
        this.vendor = vendor;
        this.date = date;
        this.cost = cost;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.url = null;
        this.receipt = null;
    }

    public Purchase(Vendor vendor, LocalDate date, double cost, double unitPrice, Quantity quantity, URL url, String receipt) {
        this.vendor = vendor;
        this.date = date;
        this.cost = cost;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.url = url;
        this.receipt = receipt;
    }

    private Purchase() {
    }

    public static Purchase fromDocument(Document d) {
        return new Purchase()
                .vendor(Vendor.fromDocument((Document) d.get("vendor")))
                .date(ConversionHelper.parseDate(d.getDate("date")))
                .cost(d.getDouble("cost"))
                .unitPrice(d.getDouble("unitPrice"))
                .quantity(Quantity.fromDocument((Document) d.get("quantity")))
                .url(ConversionHelper.parseURL(d.getString("url")))
                .receipt(d.getString("receipt"));
    }

    private Purchase vendor(Vendor vendor) {
        this.vendor = vendor;
        return this;
    }

    private Purchase date(LocalDate date) {
        this.date = date;
        return this;
    }

    private Purchase cost(double cost) {
        this.cost = cost;
        return this;
    }

    private Purchase unitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    private Purchase quantity(Quantity quantity) {
        this.quantity = quantity;
        return this;
    }

    private Purchase url(URL url) {
        this.url = url;
        return this;
    }

    private Purchase receipt(String receipt) {
        this.receipt = receipt;
        return this;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getCost() {
        return cost;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public URL getUrl() {
        return url;
    }

    public String getReceipt() {
        return receipt;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("vendor", getVendor().toDocument())
                .append("date", getDate())
                .append("cost", getCost())
                .append("unitPrice", getUnitPrice())
                .append("quantity", getQuantity().toDocument())
                .append("url", getUrl())
                .append("receipt", getReceipt());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Purchase that = (Purchase) o;

        return new EqualsBuilder().append(cost, that.cost).append(unitPrice, that.unitPrice).append(vendor, that.vendor).append(date, that.date).append(quantity, that.quantity).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(vendor).append(date).append(cost).append(unitPrice).append(quantity).toHashCode();
    }
}
