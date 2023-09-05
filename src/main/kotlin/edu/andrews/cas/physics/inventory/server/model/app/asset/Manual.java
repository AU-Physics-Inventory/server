package edu.andrews.cas.physics.inventory.server.model.app.asset;

import edu.andrews.cas.physics.inventory.server.model.app.IDocumentConversion;
import org.bson.Document;

public final class Manual implements IDocumentConversion {
    private int identityNo;
    private boolean hardcopyAvailable;
    private String softcopy;

    public Manual(int identityNo, boolean hasHardcopy, String softcopy) {
        this.identityNo = identityNo;
        this.hardcopyAvailable = hasHardcopy;
        this.softcopy = softcopy;
    }

    public int getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(int identityNo) {
        this.identityNo = identityNo;
    }

    public boolean isHardcopyAvailable() {
        return hardcopyAvailable;
    }

    public void setHardcopyAvailable(boolean hardcopyAvailable) {
        this.hardcopyAvailable = hardcopyAvailable;
    }

    public String getSoftcopy() {
        return softcopy;
    }

    public void setSoftcopy(String softcopy) {
        this.softcopy = softcopy;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("identityNo", getIdentityNo())
                .append("hardcopy", isHardcopyAvailable())
                .append("softcopy", getSoftcopy());
    }
}
