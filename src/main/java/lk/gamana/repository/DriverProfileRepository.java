package lk.gamana.repository;

import lk.gamana.model.DriverProfile;
import lk.gamana.storage.FileStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DriverProfileRepository {
    private static final String FILE = "driver_profiles.txt";
    private final FileStore store;

    public DriverProfileRepository(FileStore store) {
        this.store = store;
    }

    public List<DriverProfile> findAll() {
        return store.read(FILE).stream().map(DriverProfile::fromRow).toList();
    }

    public Optional<DriverProfile> findByUserId(String userId) {
        return findAll().stream().filter(p -> p.getUserId().equals(userId)).findFirst();
    }

    public List<DriverProfile> findOnline() {
        return findAll().stream().filter(DriverProfile::isOnline).toList();
    }

    public void save(DriverProfile profile) {
        store.withLock(() -> {
            List<String[]> rows = store.read(FILE);
            boolean replaced = false;
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i)[0].equals(profile.getUserId())) {
                    rows.set(i, profile.toRow());
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                rows.add(profile.toRow());
            }
            store.write(FILE, rows);
            return null;
        });
    }
}
