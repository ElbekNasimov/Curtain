package com.example.curtain.model;

public class ModelPart {

    private String created_at, created_by, partLen, partLoc, partMeas, prId, partId, prTitle, partEditAt, partEditBy,
    isReservePart, byReservedPart, isStock;

    public ModelPart() {
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public ModelPart(String created_at, String created_by, String partLen, String partLoc, String partMeas, String prId,
                     String partId, String prTitle, String partEditAt, String partEditBy, String isReservePart,
                     String byReservedPart, String isStock) {
        this.created_at = created_at;
        this.created_by = created_by;
        this.partLen = partLen;
        this.partLoc = partLoc;
        this.partMeas = partMeas;
        this.prId = prId;
        this.partId = partId;
        this.prTitle = prTitle;
        this.partEditAt = partEditAt;
        this.partEditBy = partEditBy;
        this.isReservePart = isReservePart;
        this.byReservedPart = byReservedPart;
        this.isStock = isStock;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getPartLen() {
        return partLen;
    }

    public void setPartLen(String partLen) {
        this.partLen = partLen;
    }

    public String getPartLoc() {
        return partLoc;
    }

    public void setPartLoc(String partLoc) {
        this.partLoc = partLoc;
    }

    public String getPartMeas() {
        return partMeas;
    }

    public void setPartMeas(String partMeas) {
        this.partMeas = partMeas;
    }

    public String getPrId() {
        return prId;
    }

    public void setPrId(String prId) {
        this.prId = prId;
    }

    public String getPrTitle() {
        return prTitle;
    }

    public void setPrTitle(String prTitle) {
        this.prTitle = prTitle;
    }

    public String getPartEditAt() {
        return partEditAt;
    }

    public void setPartEditAt(String partEditAt) {
        this.partEditAt = partEditAt;
    }

    public String getPartEditBy() {
        return partEditBy;
    }

    public void setPartEditBy(String partEditBy) {
        this.partEditBy = partEditBy;
    }

    public String getIsReservePart() {
        return isReservePart;
    }

    public void setIsReservePart(String isReservePart) {
        this.isReservePart = isReservePart;
    }

    public String getByReservedPart() {
        return byReservedPart;
    }

    public void setByReservedPart(String byReservedPart) {
        this.byReservedPart = byReservedPart;
    }
    public String getIsStock() {
        return isStock;
    }
    public void setIsStock(String isStock) {
        this.isStock = isStock;
    }
}
