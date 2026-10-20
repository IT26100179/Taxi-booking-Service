package lk.gamana.service;

import lk.gamana.model.VehicleType;
import lk.gamana.util.Places;
import org.springframework.stereotype.Service;

@Service
public class FareService {

    /** Straight line distance between two places, times 1.3 as a road factor. */
    public double distanceKm(Places.Place a, Places.Place b) {
        if (a == null || b == null) {
            return 0.0;
        }
        double r = 6371.0;
        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLng = Math.toRadians(b.lng() - a.lng());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.lat())) * Math.cos(Math.toRadians(b.lat()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * r * Math.asin(Math.sqrt(h)) * 1.3;
    }

    public long fare(VehicleType t, double km, int discountPct) {
        if (t == null) return 0L;
        double gross = t.getBaseFare() + km * t.getPerKmRate();
        return Math.round(gross * (100 - discountPct) / 100.0 / 10.0) * 10;
    }
}
