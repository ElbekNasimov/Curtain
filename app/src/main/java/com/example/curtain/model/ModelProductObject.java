package com.example.curtain.model;

public class ModelProductObject {
    String created_by, orderId, productObjectId, objectOrderId, lenProductObject, titleProductObject,
            partStatusProductObject, productId, qoldiqKusok, kesilganKusoklarList, productPriceProductOrder,
            productTypeProductOrder;

    public ModelProductObject() {
    }

    public ModelProductObject(String created_by, String orderId, String productObjectId, String objectOrderId,
                              String lenProductObject, String titleProductObject, String partStatusProductObject,
                              String productId, String qoldiqKusok, String kesilganKusoklarList,
                              String productPriceProductOrder, String productTypeProductOrder) {
        this.created_by = created_by;
            this.orderId = orderId;
            this.productObjectId = productObjectId;   // id
            this.objectOrderId = objectOrderId;   // object id
            this.lenProductObject = lenProductObject;   // tanlangan parda kiritilgan (kerakli) uzunligi
            this.titleProductObject = titleProductObject;  // tanlangan parda nomi
            this.partStatusProductObject = partStatusProductObject;
            this.productId = productId;      // parda narxi, tannarxi olish uchun productId
            this.qoldiqKusok = qoldiqKusok;        // keyin qaraymiz, hozir shartmas
            this.kesilganKusoklarList = kesilganKusoklarList;       // keyin qaraymiz, hozir shartmas
            this.productPriceProductOrder = productPriceProductOrder;  // keyin qaraymiz, hozir shartmas
            this.productTypeProductOrder = productTypeProductOrder;  // product turini olish uchun
    }

    public String getProductPriceProductOrder() {
        return productPriceProductOrder;
    }

    public void setProductPriceProductOrder(String productPriceProductOrder) {
        this.productPriceProductOrder = productPriceProductOrder;
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

    public String getProductObjectId() {
        return productObjectId;
    }

    public void setProductObjectId(String productObjectId) {
        this.productObjectId = productObjectId;
    }

    public String getObjectOrderId() {
        return objectOrderId;
    }

    public void setObjectOrderId(String objectOrderId) {
        this.objectOrderId = objectOrderId;
    }

    public String getLenProductObject() {
        return lenProductObject;
    }

    public void setLenProductObject(String lenProductObject) {
        this.lenProductObject = lenProductObject;
    }

    public String getTitleProductObject() {
        return titleProductObject;
    }

    public void setTitleProductObject(String titleProductObject) {
        this.titleProductObject = titleProductObject;
    }

    public String getPartStatusProductObject() {
        return partStatusProductObject;
    }

    public void setPartStatusProductObject(String partStatusProductObject) {
        this.partStatusProductObject = partStatusProductObject;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getQoldiqKusok() {
        return qoldiqKusok;
    }

    public void setQoldiqKusok(String qoldiqKusok) {
        this.qoldiqKusok = qoldiqKusok;
    }

    public String getKesilganKusoklarList() {
        return kesilganKusoklarList;
    }

    public void setKesilganKusoklarList(String kesilganKusoklarList) {
        this.kesilganKusoklarList = kesilganKusoklarList;
    }

    public String getProductTypeProductOrder() {
        return productTypeProductOrder;
    }

    public void setProductTypeProductOrder(String productTypeProductOrder) {
        this.productTypeProductOrder = productTypeProductOrder;
    }
}
