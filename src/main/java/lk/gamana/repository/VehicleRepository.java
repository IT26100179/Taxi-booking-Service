package lk.gamana.repository;

import lk.gamana.model.Vehicle;
import lk.gamana.storage.FileStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VehicleRepository {
    private static final String FILE = "vehicles.txt";
    private final FileStore store;

    public VehicleRepository(FileStore store) {
        this.store = store;
    }

    public List<Vehicle> findAll() {
        return store.read(FILE).stream().map(Vehicle::fromRow).toList();
    }

    public Optional<Vehicle> findById(String id) {
        return findAll().stream().filter(v -> v.getId().equals(id)).findFirst();
    }

    public Optional<Vehicle> findByDriver(String driverId) {
        return findAll().stream().filter(v -> v.getDriverId().equals(driverId)).findFirst();
    }

    public boolean existsByPlate(String plateNo) {
        return findAll().stream().anyMatch(v -> v.getPlateNo().equalsIgnoreCase(plateNo));
    }

    public void save(Vehicle vehicle) {
        store.withLock(() -> {
            List<String[]> rows = store.read(FILE);
            boolean replaced = false;
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i)[0].equals(vehicle.getId())) {
                    rows.set(i, vehicle.toRow());
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                rows.add(vehicle.toRow());
            }
            store.write(FILE, rows);
            return null;
        });
    }

    public String nextId() {
        int max = findAll().stream()
                .mapToInt(v -> {
                    try {
                        return Integer.parseInt(v.getId().substring(2));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return "v-" + (max + 1);
    }
}
