package com.example.curtain.model;

public class ModelProduct {

    private String prId, prTitle, prCat, prBarcode, prDesc, prPrice, prCost, created_at, created_by, prHeight, prMass, prComp,
            prOldPrice, prDiscNote, prEditAt, prEditBy;

    public ModelProduct() {
    }

    public ModelProduct(String prId, String prTitle, String prCat, String prPrice, String prCost, String prBarcode,
                        String prDesc, String created_at, String created_by, String prOldPrice, String prHeight, String prMass,
                        String prComp, String prDiscNote, String prEditAt, String prEditBy) {
        this.prId = prId;
        this.prTitle = prTitle;
        this.prCat = prCat;
        this.prBarcode = prBarcode;
        this.prDesc = prDesc;
        this.prPrice = prPrice;
        this.prCost = prCost;
        this.created_at = created_at;
        this.created_by = created_by;
        this.prOldPrice = prOldPrice;
        this.prDiscNote = prDiscNote;
        this.prEditAt = prEditAt;
        this.prEditBy = prEditBy;
        this.prHeight = prHeight;
        this.prMass = prMass;
        this.prComp = prComp;
    }

    public String getPrHeight() {
        return prHeight;
    }

    public void setPrHeight(String prHeight) {
        this.prHeight = prHeight;
    }

    public String getPrMass() {
        return prMass;
    }

    public void setPrMass(String prMass) {
        this.prMass = prMass;
    }

    public String getPrComp() {
        return prComp;
    }

    public void setPrComp(String prComp) {
        this.prComp = prComp;
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

    public String getPrCat() {
        return prCat;
    }

    public void setPrCat(String prCat) {
        this.prCat = prCat;
    }

    public String getPrPrice() {
        return prPrice;
    }

    public void setPrPrice(String prPrice) {
        this.prPrice = prPrice;
    }

    public String getPrCost() {
        return prCost;
    }

    public void setPrCost(String prCost) {
        this.prCost = prCost;
    }

    public String getPrBarcode() {
        return prBarcode;
    }

    public void setPrBarcode(String prBarcode) {
        this.prBarcode = prBarcode;
    }

    public String getPrDesc() {
        return prDesc;
    }

    public void setPrDesc(String prDesc) {
        this.prDesc = prDesc;
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

    public String getPrOldPrice() {
        return prOldPrice;
    }

    public void setPrOldPrice(String prOldPrice) {
        this.prOldPrice = prOldPrice;
    }

    public String getPrDiscNote() {
        return prDiscNote;
    }

    public void setPrDiscNote(String prDiscNote) {
        this.prDiscNote = prDiscNote;
    }

    public String getPrEditAt(){ return prEditAt; }

    public void setPrEditAt(String prEditAt) { this.prEditAt = prEditAt; }

    public String getPrEditBy(){ return prEditBy; }

    public void setPrEditBy(String prEditBy) { this.prEditBy = prEditBy; }
}
