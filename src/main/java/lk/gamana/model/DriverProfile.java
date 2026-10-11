package lk.gamana.model;

public class DriverProfile {
    private String userId;
    private String nicNumber;
    private String licenseNo;
    private boolean online;
    private String currentPlace;
    private String rejectionNote;

    public DriverProfile() {}

    public DriverProfile(String userId, String nicNumber, String licenseNo,
                         boolean online, String currentPlace, String rejectionNote) {
        this.userId = userId;
        this.nicNumber = nicNumber;
        this.licenseNo = licenseNo;
        this.online = online;
        this.currentPlace = currentPlace;
        this.rejectionNote = rejectionNote;
    }

    public static DriverProfile fromRow(String[] row) {
        if (row.length < 6) {
            throw new IllegalArgumentException("Invalid driver profile row length: " + row.length);
        }
        return new DriverProfile(
                row[0],
                row[1],
                row[2],
                Boolean.parseBoolean(row[3]),
                row[4],
                row[5]
        );
    }

    public String[] toRow() {
        return new String[]{
                userId,
                nicNumber != null ? nicNumber : "",
                licenseNo != null ? licenseNo : "",
                String.valueOf(online),
                currentPlace != null ? currentPlace : "",
                rejectionNote != null ? rejectionNote : ""
        };
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNicNumber() { return nicNumber; }
    public void setNicNumber(String nicNumber) { this.nicNumber = nicNumber; }

    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getCurrentPlace() { return currentPlace; }
    public void setCurrentPlace(String currentPlace) { this.currentPlace = currentPlace; }

    public String getRejectionNote() { return rejectionNote; }
    public void setRejectionNote(String rejectionNote) { this.rejectionNote = rejectionNote; }
}
