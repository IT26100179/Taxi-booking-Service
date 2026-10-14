package lk.gamana.model;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Ride {
    private String id;
    private String passengerId;
    private String driverId;
    private int typeId;
    private String promoCode;
    private String pickupPlace;
    private String dropPlace;
    private RideStatus status;
    private double distanceKm;
    private long fareEstimate;
    private long fareFinal;
    private String offeredTo;
    private Set<String> rejectedDrivers = new LinkedHashSet<>();
    private long offerDeadline;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private String paymentStatus;
    private int rating;
    private String ratingComment;
    private Double pickupLat;
    private Double pickupLng;
    private Double dropLat;
    private Double dropLng;

    public Ride() {}

    public Ride(String id, String passengerId, int typeId, String promoCode,
                String pickupPlace, String dropPlace, double distanceKm, long fareEstimate) {
        this.id = id;
        this.passengerId = passengerId;
        this.driverId = "";
        this.typeId = typeId;
        this.promoCode = promoCode != null ? promoCode : "";
        this.pickupPlace = pickupPlace;
        this.dropPlace = dropPlace;
        this.status = RideStatus.REQUESTED;
        this.distanceKm = distanceKm;
        this.fareEstimate = fareEstimate;
        this.fareFinal = 0;
        this.offeredTo = "";
        this.rejectedDrivers = new LinkedHashSet<>();
        this.offerDeadline = 0L;
        this.requestedAt = LocalDateTime.now();
        this.completedAt = null;
        this.paymentStatus = "PENDING";
        this.rating = 0;
        this.ratingComment = "";
    }

    public static Ride fromRow(String[] row) {
        if (row.length < 19) {
            throw new IllegalArgumentException("Invalid ride row length: " + row.length);
        }
        Ride r = new Ride();
        r.setId(row[0]);
        r.setPassengerId(row[1]);
        r.setDriverId(row[2]);
        r.setTypeId(Integer.parseInt(row[3]));
        r.setPromoCode(row[4]);
        r.setPickupPlace(row[5]);
        r.setDropPlace(row[6]);
        r.setStatus(RideStatus.valueOf(row[7]));
        r.setDistanceKm(Double.parseDouble(row[8]));
        r.setFareEstimate(Long.parseLong(row[9]));
        r.setFareFinal(Long.parseLong(row[10]));
        r.setOfferedTo(row[11]);
        
        Set<String> rejected = new LinkedHashSet<>();
        if (!row[12].isBlank()) {
            for (String d : row[12].split(",")) {
                if (!d.isBlank()) rejected.add(d.trim());
            }
        }
        r.setRejectedDrivers(rejected);
        r.setOfferDeadline(Long.parseLong(row[13]));
        r.setRequestedAt(!row[14].isBlank() ? LocalDateTime.parse(row[14]) : LocalDateTime.now());
        r.setCompletedAt(!row[15].isBlank() ? LocalDateTime.parse(row[15]) : null);
        r.setPaymentStatus(row[16]);
        r.setRating(Integer.parseInt(row[17]));
        r.setRatingComment(row[18]);
        if (row.length >= 23) {
            r.setPickupLat(!row[19].isBlank() ? Double.parseDouble(row[19]) : null);
            r.setPickupLng(!row[20].isBlank() ? Double.parseDouble(row[20]) : null);
            r.setDropLat(!row[21].isBlank() ? Double.parseDouble(row[21]) : null);
            r.setDropLng(!row[22].isBlank() ? Double.parseDouble(row[22]) : null);
        }
        return r;
    }

    public String[] toRow() {
        String rej = String.join(",", rejectedDrivers);
        return new String[]{
                id,
                passengerId != null ? passengerId : "",
                driverId != null ? driverId : "",
                String.valueOf(typeId),
                promoCode != null ? promoCode : "",
                pickupPlace != null ? pickupPlace : "",
                dropPlace != null ? dropPlace : "",
                status.name(),
                String.format(Locale.US, "%.1f", distanceKm),
                String.valueOf(fareEstimate),
                String.valueOf(fareFinal),
                offeredTo != null ? offeredTo : "",
                rej,
                String.valueOf(offerDeadline),
                requestedAt != null ? requestedAt.toString() : "",
                completedAt != null ? completedAt.toString() : "",
                paymentStatus != null ? paymentStatus : "PENDING",
                String.valueOf(rating),
                ratingComment != null ? ratingComment : "",
                pickupLat != null ? String.valueOf(pickupLat) : "",
                pickupLng != null ? String.valueOf(pickupLng) : "",
                dropLat != null ? String.valueOf(dropLat) : "",
                dropLng != null ? String.valueOf(dropLng) : ""
        };
    }

    public void addRejected(String dId) {
        if (dId != null && !dId.isBlank()) {
            rejectedDrivers.add(dId);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }

    public String getPickupPlace() { return pickupPlace; }
    public void setPickupPlace(String pickupPlace) { this.pickupPlace = pickupPlace; }

    public String getDropPlace() { return dropPlace; }
    public void setDropPlace(String dropPlace) { this.dropPlace = dropPlace; }

    public RideStatus getStatus() { return status; }
    public void setStatus(RideStatus status) { this.status = status; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public long getFareEstimate() { return fareEstimate; }
    public void setFareEstimate(long fareEstimate) { this.fareEstimate = fareEstimate; }

    public long getFareFinal() { return fareFinal; }
    public void setFareFinal(long fareFinal) { this.fareFinal = fareFinal; }

    public String getOfferedTo() { return offeredTo; }
    public void setOfferedTo(String offeredTo) { this.offeredTo = offeredTo; }

    public Set<String> getRejectedDrivers() { return rejectedDrivers; }
    public void setRejectedDrivers(Set<String> rejectedDrivers) { this.rejectedDrivers = rejectedDrivers; }

    public long getOfferDeadline() { return offerDeadline; }
    public void setOfferDeadline(long offerDeadline) { this.offerDeadline = offerDeadline; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getRatingComment() { return ratingComment; }
    public void setRatingComment(String ratingComment) { this.ratingComment = ratingComment; }

    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }

    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }

    public Double getDropLat() { return dropLat; }
    public void setDropLat(Double dropLat) { this.dropLat = dropLat; }

    public Double getDropLng() { return dropLng; }
    public void setDropLng(Double dropLng) { this.dropLng = dropLng; }
}
