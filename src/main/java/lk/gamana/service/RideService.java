package lk.gamana.service;

import lk.gamana.dto.BookingForm;
import lk.gamana.model.*;
import lk.gamana.repository.*;
import lk.gamana.storage.FileStore;
import lk.gamana.util.Places;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RideService {

    private final RideRepository rides;
    private final UserRepository users;
    private final DriverProfileRepository profiles;
    private final VehicleRepository vehicles;
    private final VehicleTypeRepository types;
    private final PromoRepository promos;
    private final FareService fares;
    private final FileStore store;

    private static final Map<RideStatus, Set<RideStatus>> ALLOWED = Map.of(
            RideStatus.REQUESTED, Set.of(RideStatus.OFFERED, RideStatus.NODRIVER, RideStatus.CANCELLED),
            RideStatus.OFFERED,   Set.of(RideStatus.OFFERED, RideStatus.ACCEPTED, RideStatus.NODRIVER, RideStatus.CANCELLED),
            RideStatus.ACCEPTED,  Set.of(RideStatus.ARRIVED, RideStatus.CANCELLED),
            RideStatus.ARRIVED,   Set.of(RideStatus.ONTRIP, RideStatus.CANCELLED),
            RideStatus.ONTRIP,    Set.of(RideStatus.COMPLETED),
            RideStatus.NODRIVER,  Set.of(RideStatus.CANCELLED)
    );

    public RideService(RideRepository rides, UserRepository users,
                       DriverProfileRepository profiles, VehicleRepository vehicles,
                       VehicleTypeRepository types, PromoRepository promos,
                       FareService fares, FileStore store) {
        this.rides = rides;
        this.users = users;
        this.profiles = profiles;
        this.vehicles = vehicles;
        this.types = types;
        this.promos = promos;
        this.fares = fares;
        this.store = store;
    }

    public Ride book(String passengerId, BookingForm f) {
        return store.withLock(() -> {
            expireOverdueOffers();
            if (rides.findActiveByPassenger(passengerId).isPresent()) {
                throw new RideException("You already have an active ride");
            }
            if (f.getPickup().equals(f.getDrop())) {
                throw new RideException("Pickup and drop-off locations must be different");
            }

            VehicleType type = types.findById(f.getTypeId())
                    .orElseThrow(() -> new RideException("Selected vehicle type not found"));

            int disc = promos.discountFor(f.getPromo());
            Places.Place pickup = Places.getOrResolve(f.getPickup(), f.getPickupLat(), f.getPickupLng());
            Places.Place drop = Places.getOrResolve(f.getDrop(), f.getDropLat(), f.getDropLng());
            if (pickup == null || drop == null) {
                throw new RideException("Invalid pickup or drop-off location");
            }

            double km = Math.max(0.5, Math.round(fares.distanceKm(pickup, drop) * 10.0) / 10.0);
            long fare = fares.fare(type, km, disc);

            String promoCode = (f.getPromo() != null && !f.getPromo().isBlank()) ? f.getPromo().trim().toUpperCase() : "";
            Ride r = new Ride(rides.nextId(), passengerId, f.getTypeId(), promoCode,
                    pickup.name(), drop.name(), km, fare);
            r.setPickupLat(pickup.lat());
            r.setPickupLng(pickup.lng());
            r.setDropLat(drop.lat());
            r.setDropLng(drop.lng());
            dispatch(r);
            rides.save(r);
            return r;
        });
    }

    public void dispatch(Ride r) {
        Optional<String> next = nextDriver(r);
        if (next.isEmpty()) {
            r.setStatus(RideStatus.NODRIVER);
            r.setOfferedTo("");
        } else {
            r.setStatus(RideStatus.OFFERED);
            r.setOfferedTo(next.get());
            r.setOfferDeadline(System.currentTimeMillis() + 60_000L);
        }
    }

    public Optional<String> nextDriver(Ride r) {
        Places.Place pickup = Places.getOrResolve(r.getPickupPlace(), null, null);
        if (pickup == null) return Optional.empty();

        return profiles.findOnline().stream()
                .filter(p -> users.findById(p.getUserId()).map(u -> u.getStatus() == UserStatus.ACTIVE).orElse(false))
                .filter(p -> !rides.isBusy(p.getUserId()))
                .filter(p -> vehicles.findByDriver(p.getUserId())
                        .map(v -> v.getTypeId() == r.getTypeId()).orElse(false))
                .filter(p -> !r.getRejectedDrivers().contains(p.getUserId()))
                .min(Comparator.comparingDouble(p -> {
                    Places.Place driverPlace = Places.getOrResolve(p.getCurrentPlace(), null, null);
                    return driverPlace != null ? fares.distanceKm(driverPlace, pickup) : Double.MAX_VALUE;
                }))
                .map(DriverProfile::getUserId);
    }

    public void expireOverdueOffers() {
        store.withLock(() -> {
            long now = System.currentTimeMillis();
            for (Ride r : rides.findByStatus(RideStatus.OFFERED)) {
                if (r.getOfferDeadline() < now) {
                    if (r.getOfferedTo() != null && !r.getOfferedTo().isBlank()) {
                        r.addRejected(r.getOfferedTo());
                    }
                    dispatch(r);
                    rides.save(r);
                }
            }
            return null;
        });
    }

    public Ride accept(String rideId, String driverId) {
        return store.withLock(() -> {
            expireOverdueOffers();
            Ride r = rides.findById(rideId).orElseThrow(() -> new RideException("Ride not found"));
            if (r.getStatus() != RideStatus.OFFERED || !driverId.equals(r.getOfferedTo())) {
                throw new RideException("This ride is no longer available");
            }
            if (rides.isBusy(driverId)) {
                throw new RideException("You already have an active ride");
            }
            r.setStatus(RideStatus.ACCEPTED);
            r.setDriverId(driverId);
            rides.save(r);
            return r;
        });
    }

    public void decline(String rideId, String driverId) {
        store.withLock(() -> {
            expireOverdueOffers();
            Ride r = rides.findById(rideId).orElseThrow(() -> new RideException("Ride not found"));
            if (r.getStatus() == RideStatus.OFFERED && driverId.equals(r.getOfferedTo())) {
                r.addRejected(driverId);
                dispatch(r);
                rides.save(r);
            }
            return null;
        });
    }

    public Ride arrived(String rideId, String driverId) {
        return store.withLock(() -> {
            Ride r = rides.findById(rideId).orElseThrow(() -> new RideException("Ride not found"));
            if (!driverId.equals(r.getDriverId())) {
                throw new RideException("You are not the assigned driver for this ride");
            }
            move(r, RideStatus.ARRIVED);
            rides.save(r);
            return r;
        });
    }

    public Ride startTrip(String rideId, String driverId) {
        return store.withLock(() -> {
            Ride r = rides.findById(rideId).orElseThrow(() -> new RideException("Ride not found"));
            if (!driverId.equals(r.getDriverId())) {
                throw new RideException("You are not the assigned driver for this ride");
            }
            move(r, RideStatus.ONTRIP);
            rides.save(r);
            return r;
        });
    }

    public Ride completeTrip(String rideId, String driverId) {
        return store.withLock(() -> {
            Ride r = rides.findById(rideId).orElseThrow(() -> new RideException("Ride not found"));
            if (!driverId.equals(r.getDriverId())) {
                throw new RideException("You are not the assigned driver for this ride");
            }
            move(r, RideStatus.COMPLETED);
            r.setFareFinal(r.getFareEstimate());
            r.setPaymentStatus("PAID");
            r.setCompletedAt(LocalDateTime.now());

            if (r.getPromoCode() != null && !r.getPromoCode().isBlank()) {
                promos.findByCode(r.getPromoCode()).ifPresent(p -> {
                    p.setUsedCount(p.getUsedCount() + 1);
                    promos.save(p);
                });
            }

            rides.save(r);
            return r;
        });
    }

    public Ride cancel(String rideId, String userId, boolean isPassenger) {
        return store.withLock(() -> {
            Ride r = rides.findById(rideId).orElseThrow(() -> new RideException("Ride not found"));
            if (isPassenger) {
                if (!userId.equals(r.getPassengerId())) {
                    throw new RideException("You cannot cancel another passenger's ride");
                }
            } else {
                if (!userId.equals(r.getDriverId())) {
                    throw new RideException("You cannot cancel this ride");
                }
            }

            if (r.getStatus() == RideStatus.ONTRIP || r.getStatus() == RideStatus.COMPLETED) {
                throw new RideException("Trip is already in progress or completed and cannot be cancelled");
            }

            move(r, RideStatus.CANCELLED);
            rides.save(r);
            return r;
        });
    }

    public void rate(String rideId, String passengerId, int rating, String comment) {
        store.withLock(() -> {
            Ride r = rides.findById(rideId).orElseThrow(() -> new RideException("Ride not found"));
            if (!passengerId.equals(r.getPassengerId())) {
                throw new RideException("You cannot rate a ride that isn't yours");
            }
            if (r.getStatus() != RideStatus.COMPLETED) {
                throw new RideException("Only completed rides can be rated");
            }
            if (r.getRating() > 0) {
                throw new RideException("This ride has already been rated");
            }
            r.setRating(rating);
            r.setRatingComment(FileStore.clean(comment));
            rides.save(r);
            return null;
        });
    }

    public double calculateDriverEarnings(String driverId) {
        long totalFare = rides.findByDriver(driverId).stream()
                .filter(r -> r.getStatus() == RideStatus.COMPLETED)
                .mapToLong(Ride::getFareFinal)
                .sum();
        return totalFare * 0.85; // 15% platform commission
    }

    public double calculateDriverRating(String driverId) {
        return rides.findByDriver(driverId).stream()
                .filter(r -> r.getRating() > 0)
                .mapToInt(Ride::getRating)
                .average()
                .orElse(0.0);
    }

    public long countCompletedRidesForDriver(String driverId) {
        return rides.findByDriver(driverId).stream()
                .filter(r -> r.getStatus() == RideStatus.COMPLETED)
                .count();
    }

    private void move(Ride r, RideStatus next) {
        if (!ALLOWED.getOrDefault(r.getStatus(), Set.of()).contains(next)) {
            throw new RideException("Invalid status transition: " + r.getStatus() + " to " + next);
        }
        r.setStatus(next);
    }
}
