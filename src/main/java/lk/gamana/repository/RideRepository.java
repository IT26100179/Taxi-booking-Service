package lk.gamana.repository;

import lk.gamana.model.Ride;
import lk.gamana.model.RideStatus;
import lk.gamana.storage.FileStore;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RideRepository {
    private static final String FILE = "rides.txt";
    private final FileStore store;

    public RideRepository(FileStore store) {
        this.store = store;
    }

    public List<Ride> findAll() {
        return store.read(FILE).stream().map(Ride::fromRow).toList();
    }

    public Optional<Ride> findById(String id) {
        return findAll().stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    public Optional<Ride> findActiveByPassenger(String passengerId) {
        Set<RideStatus> activeStatuses = Set.of(
                RideStatus.REQUESTED,
                RideStatus.OFFERED,
                RideStatus.ACCEPTED,
                RideStatus.ARRIVED,
                RideStatus.ONTRIP,
                RideStatus.NODRIVER
        );
        return findAll().stream()
                .filter(r -> r.getPassengerId().equals(passengerId) && activeStatuses.contains(r.getStatus()))
                .findFirst();
    }

    public Optional<Ride> findLastFinishedByPassenger(String passengerId) {
        return findAll().stream()
                .filter(r -> r.getPassengerId().equals(passengerId) &&
                        (r.getStatus() == RideStatus.COMPLETED || r.getStatus() == RideStatus.CANCELLED))
                .max(Comparator.comparing(Ride::getRequestedAt));
    }

    public Optional<Ride> findActiveByDriver(String driverId) {
        Set<RideStatus> activeStatuses = Set.of(
                RideStatus.ACCEPTED,
                RideStatus.ARRIVED,
                RideStatus.ONTRIP
        );
        return findAll().stream()
                .filter(r -> driverId.equals(r.getDriverId()) && activeStatuses.contains(r.getStatus()))
                .findFirst();
    }

    public Optional<Ride> findCurrentOfferForDriver(String driverId) {
        long now = System.currentTimeMillis();
        return findAll().stream()
                .filter(r -> r.getStatus() == RideStatus.OFFERED &&
                        driverId.equals(r.getOfferedTo()) &&
                        r.getOfferDeadline() > now)
                .findFirst();
    }

    public boolean isBusy(String driverId) {
        return findActiveByDriver(driverId).isPresent();
    }

    public List<Ride> findByPassenger(String passengerId) {
        return findAll().stream()
                .filter(r -> r.getPassengerId().equals(passengerId))
                .sorted(Comparator.comparing(Ride::getRequestedAt).reversed())
                .toList();
    }

    public List<Ride> findByDriver(String driverId) {
        return findAll().stream()
                .filter(r -> driverId.equals(r.getDriverId()))
                .sorted(Comparator.comparing(Ride::getRequestedAt).reversed())
                .toList();
    }

    public List<Ride> findByStatus(RideStatus status) {
        return findAll().stream().filter(r -> r.getStatus() == status).toList();
    }

    public void save(Ride ride) {
        store.withLock(() -> {
            List<String[]> rows = store.read(FILE);
            boolean replaced = false;
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i)[0].equals(ride.getId())) {
                    rows.set(i, ride.toRow());
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                rows.add(ride.toRow());
            }
            store.write(FILE, rows);
            return null;
        });
    }

    public String nextId() {
        int max = findAll().stream()
                .mapToInt(r -> {
                    try {
                        return Integer.parseInt(r.getId().substring(2));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return "r-" + (max + 1);
    }
}
