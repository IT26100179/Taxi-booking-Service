package lk.gamana.repository;

import lk.gamana.model.AuditLog;
import lk.gamana.storage.FileStore;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Repository
public class AuditRepository {
    private static final String FILE = "audit.txt";
    private final FileStore store;

    public AuditRepository(FileStore store) {
        this.store = store;
    }

    public List<AuditLog> findAll() {
        return store.read(FILE).stream()
                .map(AuditLog::fromRow)
                .sorted(Comparator.comparing(AuditLog::getTimestamp).reversed())
                .toList();
    }

    public void log(String adminId, String action, String target) {
        AuditLog entry = new AuditLog(LocalDateTime.now(), adminId, action, target);
        store.withLock(() -> {
            List<String[]> rows = store.read(FILE);
            rows.add(entry.toRow());
            store.write(FILE, rows);
            return null;
        });
    }
}
