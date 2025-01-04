package com.example.curtain.model;

public class ModelOrderPays {

    private String created_at, created_by, orderNumber, orderPay, orderPayId;

    public ModelOrderPays() {
    }

    public ModelOrderPays(String created_at, String created_by, String orderNumber, String orderPay, String orderPayId) {
        this.created_at = created_at;
        this.created_by = created_by;
        this.orderNumber = orderNumber;
        this.orderPay = orderPay;
        this.orderPayId = orderPayId;
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderPay() {
        return orderPay;
    }

    public void setOrderPay(String orderPay) {
        this.orderPay = orderPay;
    }

    public String getOrderPayId() {
        return orderPayId;
    }

    public void setOrderPayId(String orderPayId) {
        this.orderPayId = orderPayId;
    }
}
