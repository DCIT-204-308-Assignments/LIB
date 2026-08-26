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

    // Order priority benefit. Subtracted from the assignment score, so a
    // higher-priority order is more willing to accept a slightly worse rider
    // rather than wait. Kept small so it breaks ties without overriding
    // distance, which is still the dominant term.
    public static final double PRIORITY_WEIGHT = 0.02;

    /**
     * How many real milliseconds one simulated minute lasts.
     *
     * <p>Deliveries are 10-25 simulated minutes long. At 1000 ms per minute a
     * delivery visibly moves through PICKED_UP and IN_TRANSIT and finishes in
     * 10-25 seconds - fast enough to demonstrate, slow enough to watch. Raise
     * this to slow the simulation down, lower it to speed it up.</p>
     */
    public static final double SIMULATED_MINUTE_MILLIS = 1000.0;
}
