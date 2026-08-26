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
 * section 31. Five of them are emitted by the running application today. The
 * other three are declared here so the vocabulary is complete, but are
 * deliberately <b>not</b> emitted, because the application currently has no
 * moment that honestly corresponds to them. Each one documents why.</p>
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

    /**
     * NOT CURRENTLY EMITTED.
     *
     * <p>The application has no pickup stage. An order moves straight from
     * ASSIGNED to DELIVERED when the completion watcher fires, so there is no
     * point in time at which a pickup actually happens. Emitting this event
     * would mean inventing a moment that does not exist.</p>
     *
     * <p>Wire this up when the order lifecycle is implemented.</p>
     */
    ORDER_PICKED_UP,

    /**
     * NOT CURRENTLY EMITTED.
     *
     * <p>The only place an order is cancelled is
     * {@code DeliveryEngine.cancelAndReassign}, which has no callers anywhere
     * in the project. Emitting from there would add code to a path nothing
     * reaches.</p>
     *
     * <p>Wire this up when cancellation/reassignment is connected to the UI.</p>
     */
    ORDER_CANCELLED,

    /**
     * NOT CURRENTLY EMITTED.
     *
     * <p>Algorithm executions are already recorded in a richer form: the
     * benchmark runners persist {@code AlgorithmRun} rows carrying input size,
     * timing, and comparison counts. A duplicate free-text audit row would add
     * noise without adding information.</p>
     */
    ALGORITHM_EXECUTED
}
