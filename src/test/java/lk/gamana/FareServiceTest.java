package lk.gamana;

import lk.gamana.model.VehicleType;
import lk.gamana.service.FareService;
import lk.gamana.util.Places;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FareServiceTest {

    private final FareService fareService = new FareService();

    @Test
    void testDistanceCalculation() {
        Places.Place fort = Places.ALL.get("fort");
        Places.Place bamba = Places.ALL.get("bamba");

        assertNotNull(fort);
        assertNotNull(bamba);

        double dist = fareService.distanceKm(fort, bamba);
        assertTrue(dist > 2.0 && dist < 10.0, "Distance between Fort and Bambalapitiya should be reasonable road km (~6-8 km)");
    }

    @Test
    void testFareCalculationWithDiscount() {
        VehicleType tuk = new VehicleType(2, "TUK", 80, 65);
        double km = 5.0; // gross = 80 + 5 * 65 = 80 + 325 = 405

        // No discount
        long standardFare = fareService.fare(tuk, km, 0);
        assertEquals(410L, standardFare, "405 rounded to nearest 10 is 410");

        // 10% discount: 405 * 0.9 = 364.5 -> rounded to nearest 10 is 360
        long discountedFare = fareService.fare(tuk, km, 10);
        assertEquals(360L, discountedFare);
    }
}
