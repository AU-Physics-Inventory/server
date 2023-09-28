package edu.andrews.cas.physics.inventory.server.model.app.asset;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import edu.andrews.cas.physics.inventory.measurement.Quantity;
import edu.andrews.cas.physics.inventory.server.exception.InvalidAssetRequestException;
import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import edu.andrews.cas.physics.inventory.server.model.app.asset.accountability.AccountabilityReports;
import edu.andrews.cas.physics.inventory.server.model.app.asset.maintenance.MaintenanceRecord;
import edu.andrews.cas.physics.inventory.server.request.app.asset.UpdateAssetRequest;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

@JsonPropertyOrder("id")
public class Asset implements IDocumentConversion {
    private ObjectId _id = null;
    private String name;
    private String location;
    private List<String> keywords;
    private List<String> images;
    private Integer identityNo;
    private String AUInventoryNo;
    private Boolean consumable;
    private ManufacturerInfo manufacturerInfo;
    private List<Purchase> purchases;
    private Quantity quantity;
    private AccountabilityReports accountabilityReports;
    private MaintenanceRecord maintenanceRecord;
    private String notes;

    public Asset(String name, String location, Integer identityNo, String AUInventoryNo,
                 boolean consumable, ManufacturerInfo manufacturerInfo, Quantity quantity, MaintenanceRecord maintenanceRecord, String notes) {
        this.name = name;
        this.location = location;
        this.keywords = new ArrayList<>();
        this.identityNo = identityNo;
        this.AUInventoryNo = AUInventoryNo;
        this.consumable = consumable;
        this.manufacturerInfo = manufacturerInfo;
        this.quantity = quantity;
        this.accountabilityReports = new AccountabilityReports();
        this.maintenanceRecord = maintenanceRecord;
        this.notes = notes;
        this.purchases = new ArrayList<>();
        this.images = new ArrayList<>();
    }

    private Asset() {}

    public static Asset fromDocument(Document d) {
        Asset asset = new Asset()
                ._id(d.getObjectId("_id"))
                .name(d.getString("name"))
                .location(d.getString("location"))
                .keywords(d.getList("keywords", String.class))
                .manufacturerInfo(ManufacturerInfo.fromDocument((Document) d.get("mfrInfo")))
                .AUInventoryNo(d.getString("AUInventoryNo"))
                .purchases(d.getList("purchases", Document.class).parallelStream().map(Purchase::fromDocument).toList())
                .quantity(Quantity.fromDocument((Document) d.get("quantity")))
                .accountabilityReports(AccountabilityReports.fromDocument(d.get("accountabilityReports", Document.class)))
                .identityNo(d.getInteger("identityNo"))
                .notes(d.getString("notes"))
                .maintenanceRecord(MaintenanceRecord.fromDocument(d.get("maintenanceRecord", Document.class)))
                .consumable(d.getBoolean("consumable"))
                .images(d.getList("images", String.class));
        asset.getAccountabilityReports().setUnit(asset.quantity.getUnit());
        return asset;
    }

    public static Asset fromUpdateRequest(UpdateAssetRequest request) throws InvalidAssetRequestException {
        if (request.getId() == null) throw new InvalidAssetRequestException("id");
        try {
            return new Asset()
                    ._id(new ObjectId(request.getId()))
                    .name(request.getName())
                    .location(request.getLocation())
                    .keywords(request.getKeywords())
                    .identityNo(request.getIdentityNo())
                    .AUInventoryNo(request.getAUInventoryNo())
                    .consumable(request.isConsumable())
                    .notes(request.getNotes())
                    .manufacturerInfo(new ManufacturerInfo(request.getBrand(), request.getModel(), request.getPartNo(), request.getSerialNo()));
        } catch (IllegalArgumentException e) {
            throw new InvalidAssetRequestException("id");
        }
    }

    private Asset _id(ObjectId id) {
        this._id = id;
        return this;
    }

    private Asset name(String name) {
        this.name = name;
        return this;
    }

    private Asset location(String location) {
        this.location = location;
        return this;
    }

    private Asset keywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    private Asset images(List<String> images) {
        this.images = images;
        return this;
    }

    private Asset identityNo(Integer identityNo) {
        this.identityNo = identityNo;
        return this;
    }

    private Asset AUInventoryNo(String AUInventoryNo) {
        this.AUInventoryNo = AUInventoryNo;
        return this;
    }

    private Asset consumable(boolean isConsumable) {
        this.consumable = isConsumable;
        return this;
    }

    private Asset manufacturerInfo(ManufacturerInfo mfrInfo) {
        this.manufacturerInfo = mfrInfo;
        return this;
    }

    private Asset purchases(List<Purchase> purchases) {
        this.purchases = purchases;
        return this;
    }

    private Asset quantity(Quantity quantity) {
        this.quantity = quantity;
        return this;
    }

    private Asset accountabilityReports(AccountabilityReports accountabilityReports) {
        this.accountabilityReports = accountabilityReports;
        return this;
    }

    private Asset maintenanceRecord(MaintenanceRecord maintenanceRecord) {
        this.maintenanceRecord = maintenanceRecord;
        return this;
    }

    private Asset notes(String notes) {
        this.notes = notes;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Integer getIdentityNo() {
        return identityNo;
    }

    @JsonGetter("AUInventoryNo")
    public String getAUInventoryNo() {
        return AUInventoryNo;
    }

    public Boolean isConsumable() {
        return consumable;
    }

    public ManufacturerInfo getManufacturerInfo() {
        return manufacturerInfo;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public AccountabilityReports getAccountabilityReports() {
        return accountabilityReports;
    }

    public MaintenanceRecord getMaintenanceRecord() {
        return maintenanceRecord;
    }

    public String getNotes() {
        return notes;
    }

    public void addPurchase(Purchase purchase) {
        if (!this.purchases.contains(purchase)) this.purchases.add(purchase);
    }

    @JsonIgnore
    public ObjectId get_id() {
        return _id;
    }

    @JsonGetter("id")
    public String getId() {
        return get_id().toString();
    }

    public void addKeyword(String keyword) {
        if (!this.keywords.contains(keyword)) this.keywords.add(keyword);
    }

    public List<String> getImages() {
        return images;
    }

    public void addImage(String img) {
        if (!this.images.contains(img)) images.add(img);
    }

    @Override
    public Document toDocument() {
        Document d = new Document()
                .append("name", getName())
                .append("location", getLocation())
                .append("keywords", getKeywords())
                .append("mfrInfo", getManufacturerInfo().toDocument())
                .append("AUInventoryNo", getAUInventoryNo())
                .append("purchases", getPurchases().stream().map(Purchase::toDocument).toList())
                .append("quantity", getQuantity().toDocument())
                .append("accountabilityReports", getAccountabilityReports().toDocument())
                .append("identityNo", getIdentityNo())
                .append("notes", getNotes())
                .append("maintenanceRecord", getMaintenanceRecord().toDocument())
                .append("consumable", isConsumable())
                .append("images", getImages());
        if (get_id() != null) d.append("_id", get_id());
        return d;
    }

    public Bson toUpdateDocument() {
        ArrayList<Bson> updates = new ArrayList<>();
        if (getName() != null) updates.add(set("name", getName()));
        if (getLocation() != null) updates.add(set("location", getLocation()));
        if (getKeywords() != null) updates.add(set("keywords", getKeywords()));
        if (getIdentityNo() != null) updates.add(set("identityNo", getIdentityNo()));
        if (getAUInventoryNo() != null) updates.add(set("AUInventoryNo", getAUInventoryNo()));
        if (isConsumable() != null) updates.add(set("consumable", isConsumable()));
        if (getNotes() != null) updates.add(set("notes", getNotes()));
        if (getManufacturerInfo() != null) {
            ManufacturerInfo mfrInfo = getManufacturerInfo();
            if (mfrInfo.getBrand() != null) updates.add(set("mfrInfo.brand", mfrInfo.getBrand()));
            if (mfrInfo.getModel() != null) updates.add(set("mfrInfo.model", mfrInfo.getModel()));
            if (mfrInfo.getPartNo() != null) updates.add(set("mfrInfo.partNo", mfrInfo.getPartNo()));
            if (mfrInfo.getSerialNo() != null) updates.add(set("mfrInfo.serialNo", mfrInfo.getSerialNo()));
        }
        return combine(updates);
    }
}
