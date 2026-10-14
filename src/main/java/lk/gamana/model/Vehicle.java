package lk.gamana.model;

public class Vehicle {
    private String id;
    private String driverId;
    private int typeId;
    private String plateNo;
    private String makeModel;
    private String color;

    public Vehicle() {}

    public Vehicle(String id, String driverId, int typeId, String plateNo, String makeModel, String color) {
        this.id = id;
        this.driverId = driverId;
        this.typeId = typeId;
        this.plateNo = plateNo;
        this.makeModel = makeModel;
        this.color = color;
    }

    public static Vehicle fromRow(String[] row) {
        if (row.length < 6) {
            throw new IllegalArgumentException("Invalid vehicle row length: " + row.length);
        }
        return new Vehicle(
                row[0],
                row[1],
                Integer.parseInt(row[2]),
                row[3],
                row[4],
                row[5]
        );
    }

    public String[] toRow() {
        return new String[]{
                id,
                driverId != null ? driverId : "",
                String.valueOf(typeId),
                plateNo != null ? plateNo : "",
                makeModel != null ? makeModel : "",
                color != null ? color : ""
        };
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }

    public String getMakeModel() { return makeModel; }
    public void setMakeModel(String makeModel) { this.makeModel = makeModel; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
