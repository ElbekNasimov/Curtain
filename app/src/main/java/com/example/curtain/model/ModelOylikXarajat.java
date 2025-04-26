package com.example.curtain.model;

public class ModelOylikXarajat {
    String otchotId, oylikXarajatSumId, xarajatDesc, xarajatSum;

    public ModelOylikXarajat() {
    }

    public ModelOylikXarajat(String otchotId, String oylikXarajatSumId, String xarajatDesc, String xarajatSum) {
        this.otchotId = otchotId;
        this.oylikXarajatSumId = oylikXarajatSumId;
        this.xarajatDesc = xarajatDesc;
        this.xarajatSum = xarajatSum;
    }

    public String getOtchotId() {
        return otchotId;
    }

    public void setOtchotId(String otchotId) {
        this.otchotId = otchotId;
    }

    public String getOylikXarajatSumId() {
        return oylikXarajatSumId;
    }

    public void setOylikXarajatSumId(String oylikXarajatSumId) {
        this.oylikXarajatSumId = oylikXarajatSumId;
    }

    public String getXarajatDesc() {
        return xarajatDesc;
    }

    public void setXarajatDesc(String xarajatDesc) {
        this.xarajatDesc = xarajatDesc;
    }

    public String getXarajatSum() {
        return xarajatSum;
    }

    public void setXarajatSum(String xarajatSum) {
        this.xarajatSum = xarajatSum;
    }
}
