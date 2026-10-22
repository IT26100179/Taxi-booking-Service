package lk.gamana.repository;

import lk.gamana.model.Promo;
import lk.gamana.storage.FileStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PromoRepository {
    private static final String FILE = "promos.txt";
    private final FileStore store;

    public PromoRepository(FileStore store) {
        this.store = store;
    }

    public List<Promo> findAll() {
        return store.read(FILE).stream().map(Promo::fromRow).toList();
    }

    public Optional<Promo> findByCode(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        return findAll().stream()
                .filter(p -> p.getCode().equalsIgnoreCase(code.trim()))
                .findFirst();
    }

    public int discountFor(String code) {
        if (code == null || code.isBlank()) return 0;
        return findByCode(code)
                .filter(Promo::isValid)
                .map(Promo::getDiscountPct)
                .orElse(0);
    }

    public void save(Promo promo) {
        store.withLock(() -> {
            List<String[]> rows = store.read(FILE);
            boolean replaced = false;
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i)[0].equalsIgnoreCase(promo.getCode())) {
                    rows.set(i, promo.toRow());
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                rows.add(promo.toRow());
            }
            store.write(FILE, rows);
            return null;
        });
    }
}
