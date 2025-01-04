package com.example.curtain.model;

public class ModelProductOrder {
    private String created_by, orderId, productOrderId, productObjectOrderId, productId, partStatusProductOrder,
             lenProductObjectOrder, productObjectOrder, qoldiqKusok, kesilganKusoklarList
            ;

    public ModelProductOrder() {
    }

    public ModelProductOrder(String created_by, String orderId, String productObjectOrderId, String productOrderId,
                             String partStatusProductOrder, String productId, String lenProductObjectOrder,
                             String productObjectOrder,  String qoldiqKusok, String kesilganKusoklarList) {
        this.created_by = created_by; // kiritgan odam
        this.orderId = orderId; // smeta raqami
        this.productObjectOrderId = productObjectOrderId; // parda nomi
        this.lenProductObjectOrder = lenProductObjectOrder; // nechi metr kerak
        this.productObjectOrder = productObjectOrder; // title product order
        this.productOrderId = productOrderId;
        this.productId = productId; // mahsulot id
        this.partStatusProductOrder = partStatusProductOrder;
        this.qoldiqKusok = qoldiqKusok;
        this.kesilganKusoklarList = kesilganKusoklarList;
    }

    public String getKesilganKusoklarList() {
        return kesilganKusoklarList;
    }

    public void setKesilganKusoklarList(String kesilganKusoklarList) {
        this.kesilganKusoklarList = kesilganKusoklarList;
    }

    public String getQoldiqKusok() {
        return qoldiqKusok;
    }

    public void setQoldiqKusok(String qoldiqKusok) {
        this.qoldiqKusok = qoldiqKusok;
    }

    public String getPartStatusProductOrder() {
        return partStatusProductOrder;
    }

    public void setPartStatusProductOrder(String partStatusProductOrder) {
        this.partStatusProductOrder = partStatusProductOrder;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductOrderId() {
        return productOrderId;
    }

    public void setProductOrderId(String productOrderId) {
        this.productOrderId = productOrderId;
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

    public String getProductObjectOrderId() {
        return productObjectOrderId;
    }

    public void setProductObjectOrderId(String productObjectOrderId) {
        this.productObjectOrderId = productObjectOrderId;
    }

    public String getLenProductObjectOrder() {
        return lenProductObjectOrder;
    }

    public void setLenProductObjectOrder(String lenProductObjectOrder) {
        this.lenProductObjectOrder = lenProductObjectOrder;
    }

    public String getProductObjectOrder() {
        return productObjectOrder;
    }

    public void setProductObjectOrder(String productObjectOrder) {
        this.productObjectOrder = productObjectOrder;
    }
}
