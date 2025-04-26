package com.example.curtain.model;

public class ModelOtchotlar {
    private String title, otchotId, oylikXarajatSum, countNewOrders,
            countChiqqanOrders, sumChiqqanOrder, costChiqqanOrder,
            countYopilganOrders, sumYopilganOrder, costYopilganOrder;

    public ModelOtchotlar() {
    }

    public ModelOtchotlar(String title, String otchotId, String oylikXarajatSum, String countNewOrders,
                          String countChiqqanOrders, String sumChiqqanOrder, String costChiqqanOrder,
                          String countYopilganOrders, String sumYopilganOrder, String costYopilganOrder) {
        this.title = title;
        this.otchotId = otchotId;
        this.oylikXarajatSum = oylikXarajatSum;
        this.countNewOrders = countNewOrders;
        this.countChiqqanOrders = countChiqqanOrders;
        this.sumChiqqanOrder = sumChiqqanOrder;
        this.costChiqqanOrder = costChiqqanOrder;
        this.countYopilganOrders = countYopilganOrders;
        this.sumYopilganOrder = sumYopilganOrder;
        this.costYopilganOrder = costYopilganOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOtchotId() {
        return otchotId;
    }

    public void setOtchotId(String otchotId) {
        this.otchotId = otchotId;
    }

    public String getOylikXarajatSum() {
        return oylikXarajatSum;
    }

    public void setOylikXarajatSum(String oylikXarajatSum) {
        this.oylikXarajatSum = oylikXarajatSum;
    }

    public String getCountNewOrders() {
        return countNewOrders;
    }

    public void setCountNewOrders(String countNewOrders) {
        this.countNewOrders = countNewOrders;
    }

    public String getCountChiqqanOrders() {
        return countChiqqanOrders;
    }

    public void setCountChiqqanOrders(String countChiqqanOrders) {
        this.countChiqqanOrders = countChiqqanOrders;
    }

    public String getSumChiqqanOrder() {
        return sumChiqqanOrder;
    }

    public void setSumChiqqanOrder(String sumChiqqanOrder) {
        this.sumChiqqanOrder = sumChiqqanOrder;
    }

    public String getCostChiqqanOrder() {
        return costChiqqanOrder;
    }

    public void setCostChiqqanOrder(String costChiqqanOrder) {
        this.costChiqqanOrder = costChiqqanOrder;
    }

    public String getCountYopilganOrders() {
        return countYopilganOrders;
    }

    public void setCountYopilganOrders(String countYopilganOrders) {
        this.countYopilganOrders = countYopilganOrders;
    }

    public String getSumYopilganOrder() {
        return sumYopilganOrder;
    }

    public void setSumYopilganOrder(String sumYopilganOrder) {
        this.sumYopilganOrder = sumYopilganOrder;
    }
    public String getCostYopilganOrder() {
        return costYopilganOrder;
    }
    public void setCostYopilganOrder(String costYopilganOrder) {
        this.costYopilganOrder = costYopilganOrder;
    }
}
