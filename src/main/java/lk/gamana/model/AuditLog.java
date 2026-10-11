package lk.gamana.model;

import java.time.LocalDateTime;

public class AuditLog {
    private LocalDateTime timestamp;
    private String adminId;
    private String action;
    private String target;

    public AuditLog() {}

    public AuditLog(LocalDateTime timestamp, String adminId, String action, String target) {
        this.timestamp = timestamp;
        this.adminId = adminId;
        this.action = action;
        this.target = target;
    }

    public static AuditLog fromRow(String[] row) {
        if (row.length < 4) {
            throw new IllegalArgumentException("Invalid audit row length: " + row.length);
        }
        return new AuditLog(
                LocalDateTime.parse(row[0]),
                row[1],
                row[2],
                row[3]
        );
    }

    public String[] toRow() {
        return new String[]{
                timestamp != null ? timestamp.toString() : LocalDateTime.now().toString(),
                adminId != null ? adminId : "",
                action != null ? action : "",
                target != null ? target : ""
        };
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
}
