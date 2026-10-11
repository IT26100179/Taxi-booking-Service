package lk.gamana.model;

import java.time.LocalDateTime;

public class User {
    private String id;
    private Role role;
    private String fullName;
    private String phone;
    private String email;
    private String passwordHash;
    private UserStatus status;
    private int failedAttempts;
    private long lockedUntil;
    private LocalDateTime createdAt;

    public User() {}

    public User(String id, Role role, String fullName, String phone, String email,
                String passwordHash, UserStatus status, int failedAttempts,
                long lockedUntil, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
    }

    public static User fromRow(String[] row) {
        if (row.length < 10) {
            throw new IllegalArgumentException("Invalid user row length: " + row.length);
        }
        return new User(
                row[0],
                Role.valueOf(row[1]),
                row[2],
                row[3],
                row[4],
                row[5],
                UserStatus.valueOf(row[6]),
                Integer.parseInt(row[7]),
                Long.parseLong(row[8]),
                LocalDateTime.parse(row[9])
        );
    }

    public String[] toRow() {
        return new String[]{
                id,
                role.name(),
                fullName != null ? fullName : "",
                phone != null ? phone : "",
                email != null ? email : "",
                passwordHash != null ? passwordHash : "",
                status.name(),
                String.valueOf(failedAttempts),
                String.valueOf(lockedUntil),
                createdAt != null ? createdAt.toString() : LocalDateTime.now().toString()
        };
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public long getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(long lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
