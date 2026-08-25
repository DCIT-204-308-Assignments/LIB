package models;

/**
 * Typed vehicle category, complementing (not replacing) Resource's existing
 * String-based `type` field so the change stays backward compatible.
 */
public enum VehicleType {
    BICYCLE,
    MOTORCYCLE;

    public static VehicleType fromString(String raw) {
        if (raw == null) {
            return MOTORCYCLE;
        }
        String t = raw.trim().toUpperCase();
        if (t.contains("MOTOR") || t.contains("MOTO")) {
            return MOTORCYCLE;
        }
        return BICYCLE;
    }
}
