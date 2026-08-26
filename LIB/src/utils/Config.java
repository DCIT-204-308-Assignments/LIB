package utils;

/**
 * UG Swift - centralised configuration constants.
 * Replaces hardcoded magic numbers scattered across the engines.
 */
public final class Config {

    private Config() {}

    // Vehicle eligibility
    public static final double MAX_BICYCLE_DISTANCE_KM = 6.0;

    // Average speeds (km/h), used for ETA calculations
    public static final double DEFAULT_BICYCLE_SPEED_KMH = 15.0;
    public static final double DEFAULT_MOTORCYCLE_SPEED_KMH = 35.0;

    // Order queue / scheduling
    public static final int MAX_ORDER_QUEUE_SIZE = 500;
    public static final double DEFAULT_ORDER_PRIORITY = 1.0;

    // Optimisation scoring weights (lower score wins in DeliveryEngine.assignRider)
    public static final double WORKLOAD_WEIGHT = 0.15; // penalty per completed delivery already on a rider's record
}
