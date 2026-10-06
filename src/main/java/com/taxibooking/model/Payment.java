package com.taxibooking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Payment Model (Component 5 - Fare Calculation & Payment Management)
 * Handles billing, payment methods, and transaction status.
 */
public class Payment {
    private String paymentId;
    private String bookingId;
    private String passengerId;
    private double amount;
    private String paymentMethod; // CASH, CARD, ONLINE
    private String paymentStatus; // PAID, PENDING, REFUNDED
    private String paidAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Payment() {
        this.paymentStatus = "PENDING";
        this.paidAt = LocalDateTime.now().format(FORMATTER);
    }

    public Payment(String paymentId, String bookingId, String passengerId, double amount,
                   String paymentMethod, String paymentStatus, String paidAt) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.amount = amount;
        this.paymentMethod = (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod : "CASH";
        this.paymentStatus = (paymentStatus != null && !paymentStatus.isBlank()) ? paymentStatus : "PENDING";
        this.paidAt = (paidAt != null && !paidAt.isBlank()) ? paidAt : LocalDateTime.now().format(FORMATTER);
    }

    public String toFileString() {
        return String.join("|",
                paymentId != null ? paymentId : "",
                bookingId != null ? bookingId : "",
                passengerId != null ? passengerId : "",
                String.valueOf(amount),
                paymentMethod != null ? paymentMethod : "CASH",
                paymentStatus != null ? paymentStatus : "PENDING",
                paidAt != null ? paidAt : LocalDateTime.now().format(FORMATTER)
        );
    }

    public static Payment fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) {
            return null;
        }
        double amt = 0.0;
        try {
            amt = Double.parseDouble(parts[3]);
        } catch (NumberFormatException ignored) {}

        return new Payment(parts[0], parts[1], parts[2], amt, parts[4], parts[5], parts[6]);
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }
}
