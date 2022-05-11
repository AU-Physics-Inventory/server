package edu.andrews.cas.physics.inventory.server.repository.model;

import edu.andrews.cas.physics.inventory.server.exception.InvalidIrregularLocationDocumentException;
import edu.andrews.cas.physics.inventory.server.util.ConversionHelper;
import lombok.NonNull;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;

public class IrregularLocation implements RepositoryModel {
    private final Document document;

    public IrregularLocation() throws Exception {
        document = new Document()
                .append("date", LocalDate.now())
                .append("approval", new Approval().build());
    }

    public IrregularLocation(Document document) {
        this.document = document;
    }

    public Approval getApproval() {
        return new Approval(this.document.get("approval", Document.class));
    }

    public LocalDate getDate() {
        return ConversionHelper.parseDate(this.document.getDate("date"));
    }

    public String getUser() {
        return this.document.getString("user");
    }

    public ObjectId getAssetID() {
        return this.document.getObjectId("assetID");
    }

    public String getComments() {
        return this.document.getString("comments");
    }

    public String getLocation() {
        return this.document.getString("location");
    }

    public String getPreviousLocation() {
        return this.document.getString("previousLocation");
    }

    public IrregularLocation comments(String comments) {
        this.document.append("comments", comments);
        return this;
    }

    public IrregularLocation location(@NonNull String location) {
        this.document.append("location", location);
        return this;
    }

    public IrregularLocation previousLocation(String previousLocation) {
        this.document.append("previousLocation", previousLocation);
        return this;
    }

    public IrregularLocation assetID(@NonNull ObjectId assetID) {
        this.document.append("assetID", assetID);
        return this;
    }

    public IrregularLocation assetID(@NonNull String assetID) {
        this.document.append("assetID", new ObjectId(assetID));
        return this;
    }

    public IrregularLocation user(@NonNull String user) {
        this.document.append("user", user);
        return this;
    }

    public Document build() throws Exception {
        if (this.document.containsKey("user") && this.document.containsKey("assetID") && this.document.containsKey("location")) return this.document;
        else throw new InvalidIrregularLocationDocumentException();
    }
}

class Approval implements RepositoryModel {
    private final Document document;

    public Approval() {
        this.document = new Document()
                .append("approved", null)
                .append("by", null)
                .append("date", null);
    }

    public Approval(Document document) {
        this.document = document;
    }

    public Approval(boolean approved, String by, LocalDate date) {
        this.document = new Document()
                .append("approved", approved)
                .append("by", by)
                .append("date", date);
    }

    public Document build() throws Exception {
        return document;
    }

    public Boolean approved() {
        return this.document.getBoolean("approved");
    }

    public String by() {
        return this.document.getString("by");
    }

    public LocalDate date() {
        return ConversionHelper.parseDate(this.document.getDate("date"));
    }
}

