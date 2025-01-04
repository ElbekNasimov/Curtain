package com.example.curtain.model;

public class ModelOrderObject {

    private String orderObjectId, created_by, orderId, orderRoom, objRoom, objWidthET, objHeightET, objDescET,
            objectPoshiv, objectUstanovka;

    public ModelOrderObject() {
    }

    public ModelOrderObject(String orderObjectId, String created_by, String orderId, String orderRoom,
                            String objRoom, String objWidthET, String objHeightET, String objDescET,
                            String objectPoshiv, String objectUstanovka) {
        this.orderObjectId = orderObjectId;
        this.created_by = created_by;
        this.orderId = orderId;
        this.orderRoom = orderRoom;
        this.objRoom = objRoom;
        this.objWidthET = objWidthET;
        this.objHeightET = objHeightET;
        this.objDescET = objDescET;
        this.objectPoshiv = objectPoshiv;
        this.objectUstanovka = objectUstanovka;
    }

    public String getOrderObjectId() {
        return orderObjectId;
    }

    public void setOrderObjectId(String orderObjectId) {
        this.orderObjectId = orderObjectId;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderRoom() {
        return orderRoom;
    }

    public void setOrderRoom(String orderRoom) {
        this.orderRoom = orderRoom;
    }

    public String getObjRoom() {
        return objRoom;
    }

    public void setObjRoom(String objRoom) {
        this.objRoom = objRoom;
    }

    public String getObjWidthET() {
        return objWidthET;
    }

    public void setObjWidthET(String objWidthET) {
        this.objWidthET = objWidthET;
    }

    public String getObjHeightET() {
        return objHeightET;
    }

    public void setObjHeightET(String objHeightET) {
        this.objHeightET = objHeightET;
    }

    public String getObjDescET() {
        return objDescET;
    }

    public void setObjDescET(String objDescET) {
        this.objDescET = objDescET;
    }

    public String getObjectPoshiv() {
        return objectPoshiv;
    }

    public void setObjectPoshiv(String objectPoshiv) {
        this.objectPoshiv = objectPoshiv;
    }

    public String getObjectUstanovka() {
        return objectUstanovka;
    }

    public void setObjectUstanovka(String objectUstanovka) {
        this.objectUstanovka = objectUstanovka;
    }
}
