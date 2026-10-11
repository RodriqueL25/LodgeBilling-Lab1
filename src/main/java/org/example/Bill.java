package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bill {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    private static final int WIDTH = 40;

    private final String billId;
    private final String guestName;
    private final String roomType;
    private final int nights;
    private final double extras;
    private final double amountPaid;
    private final String notes;
    private final LocalDateTime created = LocalDateTime.now();

    public Bill(String billId, String guestName, String roomType, int nights,
                double extras, double amountPaid, String notes) {
        this.billId = billId;
        this.guestName = guestName;
        this.roomType = roomType;
        this.nights = nights;
        this.extras = extras;
        this.amountPaid = amountPaid;
        this.notes = notes;
    }

    // ---------- Pricing rules ----------

    public static double rateFor(String roomType) {
        switch (roomType) {
            case "Standard": return 350.00;
            case "Deluxe":   return 550.00;
            case "Family":   return 750.00;
            default:         return 0.00;
        }
    }

    public double getRate() {
        return rateFor(roomType);
    }

    public double getRoomCharge() {
        return getRate() * nights;
    }

    // 5 nights or more: 5% off the room charge (extras get no discount)
    public double getDiscount() {
        return nights >= 5 ? getRoomCharge() * 0.05 : 0.0;
    }

    public double getTotalCost() {
        return getRoomCharge() - getDiscount() + extras;
    }

    public double getBalance() {
        return Math.round((getTotalCost() - amountPaid) * 100) / 100.0;
    }

    // ---------- Getters (the table reads these by name) ----------

    public String getBillId()      { return billId; }
    public String getGuestName()   { return guestName; }
    public String getRoomType()    { return roomType; }
    public int getNights()         { return nights; }
    public double getExtras()      { return extras; }
    public double getAmountPaid()  { return amountPaid; }
    public String getNotes()       { return notes; }

    // ---------- Receipt ----------

    public String toReceipt(String servedBy) {
        String bar = "=".repeat(WIDTH) + "\n";
        String thin = "-".repeat(WIDTH) + "\n";

        StringBuilder sb = new StringBuilder();
        sb.append(bar);
        sb.append(center("LODGE BILLING LAB")).append("\n");
        sb.append(center("RECEIPT")).append("\n");
        sb.append(bar);
        sb.append(String.format("%-10s%s\n", "Bill no:", billId));
        sb.append(String.format("%-10s%s\n", "Date:", created.format(DATE_FORMAT)));
        sb.append(String.format("%-10s%s\n", "Guest:", guestName));
        sb.append(String.format("%-10s%s\n", "Room:", roomType));
        sb.append(String.format("%-10s%d\n", "Nights:", nights));
        sb.append(thin);

        sb.append(line(String.format("Room (%d x K %.2f)", nights, getRate()), getRoomCharge()));
        if (getDiscount() > 0) {
            sb.append(line("Discount (5%)", -getDiscount()));
        }
        sb.append(line("Extras", extras));
        sb.append(thin);
        sb.append(line("TOTAL", getTotalCost()));
        sb.append(line("Paid", amountPaid));
        sb.append(thin);

        double balance = getBalance();
        if (balance > 0) {
            sb.append(line("BALANCE DUE", balance));
        } else if (balance < 0) {
            sb.append(line("REFUND DUE", -balance));
        } else {
            sb.append(line("BALANCE", 0.0));
            sb.append(center("FULLY PAID")).append("\n");
        }

        if (notes != null && !notes.isBlank()) {
            sb.append(thin);
            sb.append("Notes: ").append(notes).append("\n");
        }

        sb.append(bar);
        if (servedBy != null) {
            sb.append("Served by: ").append(servedBy).append("\n");
        }
        sb.append(center("Thank you for staying with us")).append("\n");
        return sb.toString();
    }

    private static String line(String label, double amount) {
        return String.format("%-26s K %10.2f\n", label, amount);
    }

    private static String center(String text) {
        int pad = Math.max(0, (WIDTH - text.length()) / 2);
        return " ".repeat(pad) + text;
    }
}