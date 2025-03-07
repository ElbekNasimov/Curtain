package com.example.curtain.model;
public class ModelOrder {
    private String created_at, created_by, orderId, orderNumber, orderCat, orderName, orderPhone, orderSum, orderLoc,
            orderDesc,
            orderStatus, orderPercent, edited_at, edit_by, orderCloseDate, orderZaklad, orderDeadline, orderUstanovka,
            orderPoshiv, orderDesignerSalary, orderTotal;
    /*
    orderNumber - smeta raqami,
    orderStatus - smeta holati,
    orderType - smeta yo'nalishi,
    orderName - mijoz ismi,
    orderPercent - dizayner foizi,
    orderCloseDate - smeta yopilgan sana
    orderSum - dizayner yozgan summasi,
    orderTotal - avtomatik hisoblangan summa
     */

    public ModelOrder() {
    }

    public ModelOrder(String created_at, String created_by, String orderId,   String orderNumber, String orderCat,
                      String orderName, String orderPhone, String orderZaklad, String orderDeadline,
                      String orderSum, String orderLoc, String orderDesc, String orderStatus, String orderPercent,
                      String edited_at, String edit_by, String orderCloseDate, String orderUstanovka, String orderPoshiv,
                      String orderDesignerSalary, String orderTotal) {
        this.created_at = created_at;
        this.created_by = created_by;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.orderCat = orderCat;
        this.orderName = orderName;
        this.orderPhone = orderPhone;
        this.orderSum = orderSum;
        this.orderLoc = orderLoc;
        this.orderDesc = orderDesc;
        this.orderStatus = orderStatus;
        this.orderPercent = orderPercent;
        this.edited_at = edited_at;
        this.edit_by = edit_by;
        this.orderZaklad = orderZaklad;
        this.orderCloseDate = orderCloseDate;
        this.orderDeadline = orderDeadline;
        this.orderUstanovka = orderUstanovka;
        this.orderPoshiv = orderPoshiv;
        this.orderDesignerSalary = orderDesignerSalary;
        this.orderTotal = orderTotal;
    }

    public String getOrderUstanovka() {
        return orderUstanovka;
    }

    public void setOrderUstanovka(String orderUstanovka) {
        this.orderUstanovka = orderUstanovka;
    }

    public String getOrderPoshiv() {
        return orderPoshiv;
    }

    public void setOrderPoshiv(String orderPoshiv) {
        this.orderPoshiv = orderPoshiv;
    }

    public String getOrderDeadline() {
        return orderDeadline;
    }

    public void setOrderDeadline(String orderDeadline) {
        this.orderDeadline = orderDeadline;
    }

    public String getOrderZaklad() {
        return orderZaklad;
    }

    public void setOrderZaklad(String orderZaklad) {
        this.orderZaklad = orderZaklad;
    }

    public String getOrderPhone() {
        return orderPhone;
    }

    public void setOrderPhone(String orderPhone) {
        this.orderPhone = orderPhone;
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

    public String getOrderId(){ return  orderId; }

    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderCat() {
        return orderCat;
    }

    public void setOrderCat(String orderCat) {
        this.orderCat = orderCat;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public String getOrderSum() {
        return orderSum;
    }

    public void setOrderSum(String orderSum) {
        this.orderSum = orderSum;
    }

    public String getOrderLoc() {
        return orderLoc;
    }

    public void setOrderLoc(String orderLoc) {
        this.orderLoc = orderLoc;
    }

    public String getOrderDesc() {
        return orderDesc;
    }

    public void setOrderDesc(String orderDesc) {
        this.orderDesc = orderDesc;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderPercent() {
        return orderPercent;
    }

    public void setOrderPercent(String orderPercent) {
        this.orderPercent = orderPercent;
    }

    public String getEdited_at() {
        return edited_at;
    }

    public void setEdited_at(String edited_at) {
        this.edited_at = edited_at;
    }

    public String getEdit_by() {
        return edit_by;
    }

    public void setEdit_by(String edit_by) {
        this.edit_by = edit_by;
    }

    public String getOrderCloseDate() {
        return orderCloseDate;
    }

    public void setOrderCloseDate(String orderCloseDate) {
        this.orderCloseDate = orderCloseDate;
    }

    public String getOrderDesignerSalary() {
        return orderDesignerSalary;
    }

    public void setOrderDesignerSalary(String orderDesignerSalary) {
        this.orderDesignerSalary = orderDesignerSalary;
    }

    public String getOrderTotal() {return orderTotal;}

    public void setOrderTotal(String orderTotal) {this.orderTotal = orderTotal;}
}
