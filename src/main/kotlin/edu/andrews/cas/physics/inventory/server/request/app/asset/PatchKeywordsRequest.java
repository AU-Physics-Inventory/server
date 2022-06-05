package edu.andrews.cas.physics.inventory.server.request.app.asset;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

public class PatchKeywordsRequest {
    private Option patchOption;
    private String assetID;
    private List<String> keywords;

    public Option getPatchOption() {
        return patchOption;
    }

    public void setPatchOption(Option patchOption) {
        this.patchOption = patchOption;
    }

    public String getAssetID() {
        return assetID;
    }

    public void setAssetID(String assetID) {
        this.assetID = assetID;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("patchOption", patchOption)
                .append("assetID", assetID)
                .append("keywords", keywords)
                .toString();
    }

    public enum Option {
        ADD, REMOVE
    }
}
