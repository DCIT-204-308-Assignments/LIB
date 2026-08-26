package models;

/**
 * The vocabulary of system events that UG Swift can record to the
 * {@code audit_events} table.
 *
 * <p>Using an enum rather than free-form strings means every audit row starts
 * with one of a known, fixed set of names. That keeps the trail consistent
 * enough to filter and group, and makes a typo a compile error instead of a
 * silently unsearchable row.</p>
 *
 * <p>The eight constants below are the event types named in {@code Progress.md}
 * section 31. All eight are emitted by the running application, and each one
 * documents where from.</p>
 */
public enum AuditEventType {

    /** A customer submitted a new order. Emitted by {@code UGSwiftApp.placeOrder}. */
    ORDER_CREATED,

    /** An order was matched to a rider. Emitted by {@code placeOrder} and {@code processNextIncoming}. */
    ORDER_ASSIGNED,

    /** A rider became BUSY or AVAILABLE. Emitted wherever {@code updateResourceState} is called. */
    RIDER_STATUS_CHANGED,

    /** A delivery completed. Emitted by {@code UGSwiftApp.checkForCompletedOrders}. */
    ORDER_DELIVERED,

    /** A shortest path was computed for the user. Emitted by {@code UGSwiftApp.computeRoute}. */
    ROUTE_CALCULATED,

    /** A rider collected the parcel. Emitted by {@code UGSwiftApp.advanceInFlightOrders}. */
    ORDER_PICKED_UP,

    /** An order was cancelled or could not be reassigned. Emitted by {@code UGSwiftApp.cancelActiveOrder}. */
    ORDER_CANCELLED,

    /** A benchmark suite finished. Emitted by {@code UGSwiftApp.runBenchmarkSuite}. */
    ALGORITHM_EXECUTED
}
