package edu.andrews.cas.physics.inventory.server.request.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class UpdateAssetRequest {
    @JsonProperty(required = true)
    private String id;

    private String name;
    private String location;
    private List<String> keywords;
    private Integer identityNo;
    private String AUInventoryNo;
    private boolean consumable;
    private String notes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("location", location)
                .append("keywords", keywords)
                .append("identityNo", identityNo)
                .append("AUInventoryNo", AUInventoryNo)
                .append("consumable", consumable)
                .append("notes", notes)
                .toString();
    }
}
