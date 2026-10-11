package org.example;

import java.time.LocalDate;

public class Reservation {

    public static final String RESERVED = "Reserved";
    public static final String CHECKED_IN = "Checked in";
    public static final String CANCELLED = "Cancelled";

    private final String reservationId;
    private final String guestName;
    private final String roomType;
    private final LocalDate checkIn;
    private final int nights;
    private String status = RESERVED;

    public Reservation(String reservationId, String guestName, String roomType,
                       LocalDate checkIn, int nights) {
        this.reservationId = reservationId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.checkIn = checkIn;
        this.nights = nights;
    }

    public String getReservationId() { return reservationId; }
    public String getGuestName()     { return guestName; }
    public String getRoomType()      { return roomType; }
    public LocalDate getCheckIn()    { return checkIn; }
    public LocalDate getCheckOut()   { return checkIn.plusDays(nights); }
    public int getNights()           { return nights; }
    public String getStatus()        { return status; }

    public void setStatus(String status) {
        this.status = status;
    }
}