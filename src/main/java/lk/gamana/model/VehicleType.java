package lk.gamana.model;

public class VehicleType {
    private int id;
    private String name;
    private double baseFare;
    private double perKmRate;

    public VehicleType() {}

    public VehicleType(int id, String name, double baseFare, double perKmRate) {
        this.id = id;
        this.name = name;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
    }

    public static VehicleType fromRow(String[] row) {
        if (row.length < 4) {
            throw new IllegalArgumentException("Invalid vehicle type row length: " + row.length);
        }
        return new VehicleType(
                Integer.parseInt(row[0]),
                row[1],
                Double.parseDouble(row[2]),
                Double.parseDouble(row[3])
        );
    }

    public String[] toRow() {
        return new String[]{
                String.valueOf(id),
                name != null ? name : "",
                String.valueOf(Math.round(baseFare)),
                String.valueOf(Math.round(perKmRate))
        };
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }

    public double getPerKmRate() { return perKmRate; }
    public void setPerKmRate(double perKmRate) { this.perKmRate = perKmRate; }

    public long getBaseFareInt() { return Math.round(baseFare); }
    public long getPerKmRateInt() { return Math.round(perKmRate); }
}
