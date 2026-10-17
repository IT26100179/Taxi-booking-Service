package lk.gamana.repository;

import lk.gamana.model.Role;
import lk.gamana.model.User;
import lk.gamana.storage.FileStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private static final String FILE = "users.txt";
    private final FileStore store;

    public UserRepository(FileStore store) {
        this.store = store;
    }

    public List<User> findAll() {
        return store.read(FILE).stream().map(User::fromRow).toList();
    }

    public Optional<User> findById(String id) {
        return findAll().stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    public Optional<User> findByRoleAndPhone(Role role, String phone) {
        return findAll().stream()
                .filter(u -> u.getRole() == role && u.getPhone().equals(phone))
                .findFirst();
    }

    public boolean exists(Role role, String phone, String email) {
        return findAll().stream().anyMatch(u -> u.getRole() == role &&
                (u.getPhone().equals(phone) || (email != null && !email.isBlank() && u.getEmail().equalsIgnoreCase(email))));
    }

    /** Insert a new user, or replace the line that has the same id. */
    public void save(User user) {
        store.withLock(() -> {
            List<String[]> rows = store.read(FILE);
            boolean replaced = false;
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i)[0].equals(user.getId())) {
                    rows.set(i, user.toRow());
                    replaced = true;
                    break;
                }
            }
            if (!replaced) {
                rows.add(user.toRow());
            }
            store.write(FILE, rows);
            return null;
        });
    }

    public String nextId() {
        int max = findAll().stream()
                .mapToInt(u -> {
                    try {
                        return Integer.parseInt(u.getId().substring(2));
                    } catch (Exception e) {
                        return 1000;
                    }
                })
                .max()
                .orElse(1000);
        return "u-" + (max + 1);
    }
}
