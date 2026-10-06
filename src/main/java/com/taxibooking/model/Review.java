package com.taxibooking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Review & Rating Model (Component 6 - Rating, Reviews & Admin Panel)
 * Allows passengers to evaluate completed rides and drivers.
 */
public class Review {
    private String reviewId;
    private String bookingId;
    private String driverId;
    private String passengerId;
    private int rating; // 1 to 5 stars
    private String feedback;
    private String reviewDate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Review() {
        this.rating = 5;
        this.reviewDate = LocalDateTime.now().format(FORMATTER);
    }

    public Review(String reviewId, String bookingId, String driverId, String passengerId,
                  int rating, String feedback, String reviewDate) {
        this.reviewId = reviewId;
        this.bookingId = bookingId;
        this.driverId = driverId;
        this.passengerId = passengerId;
        this.rating = (rating >= 1 && rating <= 5) ? rating : 5;
        this.feedback = feedback;
        this.reviewDate = (reviewDate != null && !reviewDate.isBlank()) ? reviewDate : LocalDateTime.now().format(FORMATTER);
    }

    public String toFileString() {
        return String.join("|",
                reviewId != null ? reviewId : "",
                bookingId != null ? bookingId : "",
                driverId != null ? driverId : "",
                passengerId != null ? passengerId : "",
                String.valueOf(rating),
                feedback != null ? feedback.replace("|", " ") : "",
                reviewDate != null ? reviewDate : LocalDateTime.now().format(FORMATTER)
        );
    }

    public static Review fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) {
            return null;
        }
        int r = 5;
        try {
            r = Integer.parseInt(parts[4]);
        } catch (NumberFormatException ignored) {}

        return new Review(parts[0], parts[1], parts[2], parts[3], r, parts[5], parts[6]);
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = (rating >= 1 && rating <= 5) ? rating : 5;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }
}
