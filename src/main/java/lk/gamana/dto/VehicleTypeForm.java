package lk.gamana.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class VehicleTypeForm {
    private int id;

    @NotBlank(message = "Vehicle type name cannot be blank")
    private String name;

    @Min(value = 0, message = "Base fare cannot be negative")
    private double baseFare;

    @Min(value = 1, message = "Per km rate must be at least 1")
    private double perKmRate;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }

    public double getPerKmRate() { return perKmRate; }
    public void setPerKmRate(double perKmRate) { this.perKmRate = perKmRate; }
}
