package com.example.curtain.model;

public class ModelOtchot {
    private String title, otchotId;

    public ModelOtchot() {
    }

    public ModelOtchot(String title, String otchotId) {
        this.title = title;
        this.otchotId = otchotId;
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
}
