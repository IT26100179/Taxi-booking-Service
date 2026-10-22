package lk.gamana.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Places {
    public record Place(String key, String name, double lat, double lng) {}

    public static final Map<String, Place> ALL = new LinkedHashMap<>();

    static {
        // Western Province (Colombo & Suburbs)
        add("fort",         "Colombo Fort",           6.9344, 79.8428);
        add("pettah",       "Pettah",                 6.9366, 79.8510);
        add("gface",        "Galle Face",             6.9271, 79.8450);
        add("maradana",     "Maradana",               6.9290, 79.8650);
        add("koll",         "Kollupitiya",            6.9100, 79.8500);
        add("uoc",          "University of Colombo",  6.9022, 79.8611);
        add("borella",      "Borella",                6.9147, 79.8776);
        add("rajag",        "Rajagiriya",             6.9100, 79.8950);
        add("bamba",        "Bambalapitiya",          6.8896, 79.8567);
        add("havelock",     "Havelock Town",          6.8860, 79.8650);
        add("wella",        "Wellawatte",             6.8740, 79.8600);
        add("nugeg",        "Nugegoda",               6.8649, 79.8997);
        add("dehi",         "Dehiwala",               6.8511, 79.8650);
        add("mount",        "Mount Lavinia",          6.8385, 79.8658);
        add("moratuwa",     "Moratuwa",               6.7730, 79.8816);
        add("maharagama",   "Maharagama",             6.8480, 79.9265);
        add("malabe",       "Malabe",                 6.9042, 79.9549);
        add("kotte",        "Sri Jayawardenepura",    6.8912, 79.9004);
        add("battaramulla", "Battaramulla",           6.8998, 79.9172);
        add("gampaha",      "Gampaha",                7.0917, 79.9998);
        add("airport",      "BIA Katunayake Airport", 7.1808, 79.8841);
        add("negombo",      "Negombo",                7.2088, 79.8358);
        add("kalutara",     "Kalutara",               6.5854, 79.9607);
        add("panadura",     "Panadura",               6.7132, 79.9074);

        // Central Province
        add("kandy",        "Kandy City",             7.2906, 80.6337);
        add("peradeniya",   "Peradeniya",             7.2680, 80.5960);
        add("matale",       "Matale",                 7.4675, 80.6234);
        add("nuwara",       "Nuwara Eliya",           6.9497, 80.7891);
        add("dambulla",     "Dambulla",               7.8742, 80.6511);

        // Southern Province
        add("galle",        "Galle Fort",             6.0535, 80.2210);
        add("hikkaduwa",    "Hikkaduwa",              6.1408, 80.1033);
        add("unawatuna",    "Unawatuna",              6.0104, 80.2489);
        add("matara",       "Matara",                 5.9496, 80.5469);
        add("mirissa",      "Mirissa",                5.9483, 80.4578);
        add("tangalle",     "Tangalle",               6.0243, 80.7941);

        // North, East, North-Central & Other
        add("jaffna",       "Jaffna Town",            9.6615, 80.0255);
        add("anuradhapura", "Anuradhapura",           8.3114, 80.4037);
        add("polonnaruwa",  "Polonnaruwa",            7.9403, 81.0188);
        add("trinco",       "Trincomalee",            8.5874, 81.2152);
        add("batticaloa",   "Batticaloa",             7.7102, 81.6924);
        add("kurunegala",   "Kurunegala",             7.4863, 80.3623);
        add("badulla",      "Badulla",                6.9934, 81.0550);
        add("ella",         "Ella",                   6.8667, 81.0466);
        add("ratnapura",    "Ratnapura",              6.6828, 80.4024);
    }

    private static void add(String k, String n, double lat, double lng) {
        ALL.put(k, new Place(k, n, lat, lng));
    }

    /**
     * Resolves a Place from a predefined key/name or dynamic map coordinates.
     */
    public static Place getOrResolve(String keyOrName, Double lat, Double lng) {
        if (keyOrName == null || keyOrName.isBlank()) {
            if (lat != null && lng != null && (lat != 0.0 || lng != 0.0)) {
                String label = String.format("Map Location (%.4f, %.4f)", lat, lng);
                return new Place(label, label, lat, lng);
            }
            return ALL.get("fort");
        }

        String clean = keyOrName.trim();
        // Exact key match
        if (ALL.containsKey(clean.toLowerCase())) {
            return ALL.get(clean.toLowerCase());
        }

        // Exact name match
        for (Place p : ALL.values()) {
            if (p.name().equalsIgnoreCase(clean)) {
                return p;
            }
        }

        // If coordinates provided, create a dynamic place
        if (lat != null && lng != null && (lat != 0.0 || lng != 0.0)) {
            return new Place(clean, clean, lat, lng);
        }

        // Try extracting coordinate pattern from string: e.g. (6.1234, 79.1234)
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\(([0-9]+\\.[0-9]+),\\s*([0-9]+\\.[0-9]+)\\)").matcher(clean);
        if (m.find()) {
            try {
                double parsedLat = Double.parseDouble(m.group(1));
                double parsedLng = Double.parseDouble(m.group(2));
                return new Place(clean, clean, parsedLat, parsedLng);
            } catch (Exception ignored) {}
        }

        // Partial name search in known places
        for (Place p : ALL.values()) {
            if (p.name().toLowerCase().contains(clean.toLowerCase()) || clean.toLowerCase().contains(p.name().toLowerCase())) {
                return p;
            }
        }

        // Fallback with Colombo default coordinates
        return new Place(clean, clean, 6.9344, 79.8428);
    }

    private Places() {}
}
