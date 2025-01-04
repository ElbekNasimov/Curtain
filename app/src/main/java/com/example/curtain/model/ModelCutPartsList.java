package com.example.curtain.model;

public class ModelCutPartsList {
    private String cutIdPartProductOrder, partCutPrObjLen, orderId, productId;

    public ModelCutPartsList() {
    }

    public ModelCutPartsList(String cutIdPartProductOrder, String partCutPrObjLen, String orderId, String productId) {
        this.cutIdPartProductOrder = cutIdPartProductOrder;
        this.partCutPrObjLen = partCutPrObjLen;
        this.orderId = orderId;
        this.productId = productId;
    }

    public String getCutIdPartProductOrder() {
        return cutIdPartProductOrder;
    }

    public void setCutIdPartProductOrder(String cutIdPartProductOrder) {
        this.cutIdPartProductOrder = cutIdPartProductOrder;
    }

    public String getPartCutPrObjLen() {
        return partCutPrObjLen;
    }

    public void setPartCutPrObjLen(String partCutPrObjLen) {
        this.partCutPrObjLen = partCutPrObjLen;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
