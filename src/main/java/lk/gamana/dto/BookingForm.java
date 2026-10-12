package lk.gamana.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BookingForm {
    @NotBlank(message = "Pickup location is required")
    private String pickup;

    @NotBlank(message = "Drop-off location is required")
    private String drop;

    @Min(value = 1, message = "Please select a vehicle type")
    private int typeId;

    private String promo;

    private Double pickupLat;
    private Double pickupLng;
    private Double dropLat;
    private Double dropLng;

    public String getPickup() { return pickup; }
    public void setPickup(String pickup) { this.pickup = pickup; }

    public String getDrop() { return drop; }
    public void setDrop(String drop) { this.drop = drop; }

    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }

    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }

    public Double getDropLat() { return dropLat; }
    public void setDropLat(Double dropLat) { this.dropLat = dropLat; }

    public Double getDropLng() { return dropLng; }
    public void setDropLng(Double dropLng) { this.dropLng = dropLng; }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public String getPromo() { return promo; }
    public void setPromo(String promo) { this.promo = promo; }
}
