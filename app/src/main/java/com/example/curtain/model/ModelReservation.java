package com.example.curtain.model;

public class ModelReservation {
    private String reservationId;
    private String reservedBy; // designerName
    private String reservedLength;
    private String reservedFor; // purpose
    private String reservedDate;
    private String status;
    private String reservedPartId;

    public ModelReservation() {
    }

    public ModelReservation(String reservationId, String reservedBy, String reservedLength, String reservedFor,
                            String reservedDate, String status, String reservedPartId) {
        this.reservationId = reservationId;
        this.reservedBy = reservedBy;
        this.reservedLength = reservedLength;
        this.reservedFor = reservedFor;
        this.reservedDate = reservedDate;
        this.status = status;
        this.reservedPartId = reservedPartId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getReservedBy() {
        return reservedBy;
    }

    public void setReservedBy(String reservedBy) {
        this.reservedBy = reservedBy;
    }

    public String getReservedLength() {
        return reservedLength;
    }

    public void setReservedLength(String reservedLength) {
        this.reservedLength = reservedLength;
    }

    public String getReservedFor() {
        return reservedFor;
    }

    public void setReservedFor(String reservedFor) {
        this.reservedFor = reservedFor;
    }

    public String getReservedDate() {
        return reservedDate;
    }

    public void setReservedDate(String reservedDate) {
        this.reservedDate = reservedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReservedPartId() {
        return reservedPartId;
    }

    public void setReservedPartId(String reservedPartId) {
        this.reservedPartId = reservedPartId;
    }
}
