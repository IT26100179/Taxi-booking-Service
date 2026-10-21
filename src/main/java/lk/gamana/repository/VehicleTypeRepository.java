package lk.gamana.repository;

import lk.gamana.model.VehicleType;
import lk.gamana.storage.FileStore;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class VehicleTypeRepository {
    private static final String FILE = "vehicle_types.txt";
    private final FileStore store;

    public VehicleTypeRepository(FileStore store) {
        this.store = store;
    }

    public List<VehicleType> findAll() {
        return store.read(FILE).stream()
                .map(VehicleType::fromRow)
                .sorted(Comparator.comparingInt(VehicleType::getId))
                .toList();
    }

    public Optional<VehicleType> findById(int id) {
        return findAll().stream().filter(t -> t.getId() == id).findFirst();
    }

    public void save(VehicleType type) {
        store.withLock(() -> {
            List<String[]> rows = store.read(FILE);
            boolean replaced = false;
            for (int i = 0; i < rows.size(); i++) {
                if (Integer.parseInt(rows.get(i)[0]) == type.getId()) {
                    rows.set(i, type.toRow());
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                rows.add(type.toRow());
            }
            store.write(FILE, rows);
            return null;
        });
    }
}
