package com.example.curtain.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ModelCutPartsList)) return false;
        ModelCutPartsList that = (ModelCutPartsList) obj;
        return Objects.equals(cutIdPartProductOrder, that.cutIdPartProductOrder) &&
                Objects.equals(partCutPrObjLen, that.partCutPrObjLen) &&
                Objects.equals(orderId, that.orderId) &&
                Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cutIdPartProductOrder, partCutPrObjLen, orderId, productId);
    }
}
