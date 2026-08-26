package engines;

import models.AuditEventType;
import models.Order;
import models.Resource;
import models.ServiceRequest;

/**
 * Records system events to the {@code audit_events} table.
 *
 * <p>This class is the single place where audit rows are built. Everything goes
 * through {@link #format}, so every row in the table has the same shape:</p>
 *
 * <pre>
 * ORDER_ASSIGNED | orderId=1234 | rider=7 (Kofi Mensah) | distanceKm=1.42 | etaMin=12.30
 * RIDER_STATUS_CHANGED | rider=7 (Kofi Mensah) | AVAILABLE
 * </pre>
 *
 * <p>The pipe-delimited {@code key=value} layout is chosen so a row is readable
 * as-is in the UI, but still splittable if the trail ever needs parsing.</p>
 *
 * <h2>Why format() and record() are separate</h2>
 *
 * <p>{@link #format} is a pure function: it builds a string and touches nothing
 * else. That makes the row layout unit-testable without a database, which is
 * why the test suite can cover this class without writing to {@code ug_swift.db}.
 * {@link #record} is the thin layer that adds persistence on top.</p>
 *
 * <h2>Auditing must never break a delivery</h2>
 *
 * <p>An audit trail is a record of work, not part of the work. If writing the
 * row fails, the delivery it describes should still succeed. {@code record}
 * therefore swallows every exception. {@code DatabaseManager.addAuditEvent}
 * already handles {@code SQLException}; the extra guard here covers anything
 * else (a null field, a formatting bug) so a logging problem can never
 * propagate into the order flow.</p>
 *
 * <h2>Do not call this from inside an algorithm</h2>
 *
 * <p>{@code addAuditEvent} opens a fresh SQLite connection per call. Calling it
 * inside {@code RouteEngine.dijkstra} would mean roughly thirty file opens per
 * order, because {@code DeliveryEngine.assignRider} runs Dijkstra once per
 * candidate rider. Audit calls belong at the points where a user-visible thing
 * happened, not inside a loop.</p>
 */
public final class AuditLog {

    /** Separates the event type from its details, and details from each other. */
    private static final String SEPARATOR = " | ";

    /** Written when the event type is missing, so a bad call still yields a usable row. */
    private static final String UNKNOWN_TYPE = "UNKNOWN_EVENT";

    private AuditLog() {
        // Utility class - never instantiated.
    }

    /**
     * Builds an audit row. Pure function: no database, no side effects.
     *
     * @param type    the kind of event; a null type degrades to UNKNOWN_EVENT
     *                rather than throwing, because losing the row entirely
     *                would be worse than recording it under a placeholder name
     * @param details already-joined detail text, or null/blank for none
     * @return a single-line row, never null and never containing a newline
     */
    public static String format(AuditEventType type, String details) {
        String name = (type == null) ? UNKNOWN_TYPE : type.name();

        if (details == null) {
            return name;
        }

        String cleaned = singleLine(details).trim();

        if (cleaned.isEmpty()) {
            return name;
        }

        return name + SEPARATOR + cleaned;
    }

    /**
     * Formats an event and writes it to the database.
     * Never throws - see the class javadoc.
     */
    public static void record(AuditEventType type, String details) {
        try {
            DatabaseManager.addAuditEvent(format(type, details));
        } catch (Exception ex) {
            // Deliberately swallowed. A failed audit write must not abort the
            // delivery operation that triggered it.
            System.err.println("Audit write failed (ignored): " + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // Convenience wrappers.
    //
    // These exist so the call sites in UGSwiftApp stay short and readable, and
    // so the same fields are recorded the same way every time. Without them,
    // two call sites recording "the same" event would drift apart in wording.
    // ---------------------------------------------------------------------

    /** A customer submitted a new order. */
    public static void orderCreated(Order order) {
        if (order == null) {
            return;
        }

        record(AuditEventType.ORDER_CREATED,
                "orderId=" + order.getOrderId()
                        + SEPARATOR + "customer=" + safe(order.getCustomerName())
                        + SEPARATOR + "item=" + safe(order.getFoodItem())
                        + SEPARATOR + "pickup=" + order.getPickupLocationId()
                        + SEPARATOR + "delivery=" + order.getDeliveryLocationId()
                        + SEPARATOR + "weightKg=" + round(order.getFoodWeightKg()));
    }

    /** An order was matched to a rider. */
    public static void orderAssigned(Order order, Resource rider,
                                     double distanceKm, double etaMin) {
        if (order == null) {
            return;
        }

        record(AuditEventType.ORDER_ASSIGNED,
                "orderId=" + order.getOrderId()
                        + SEPARATOR + riderLabel(rider)
                        + SEPARATOR + "distanceKm=" + round(distanceKm)
                        + SEPARATOR + "etaMin=" + round(etaMin));
    }

    /** A rider collected the parcel and set off for the destination. */
    public static void orderPickedUp(Order order, Resource rider) {
        if (order == null) {
            return;
        }

        record(AuditEventType.ORDER_PICKED_UP,
                "orderId=" + order.getOrderId()
                        + SEPARATOR + riderLabel(rider)
                        + SEPARATOR + "pickup=" + order.getPickupLocationId()
                        + SEPARATOR + "delivery=" + order.getDeliveryLocationId());
    }

    /** An order was cancelled, either by the user or by a failed reassignment. */
    public static void orderCancelled(Order order, String reason) {
        if (order == null) {
            return;
        }

        record(AuditEventType.ORDER_CANCELLED,
                "orderId=" + order.getOrderId()
                        + SEPARATOR + "reason=" + safe(reason));
    }

    /** A benchmark or algorithm suite finished. */
    public static void algorithmExecuted(String algorithmName, int inputSize, long timeNs) {
        record(AuditEventType.ALGORITHM_EXECUTED,
                "algorithm=" + safe(algorithmName)
                        + SEPARATOR + "inputSize=" + inputSize
                        + SEPARATOR + "timeNs=" + timeNs);
    }

    /** A rider became BUSY, AVAILABLE or OFFLINE. */
    public static void riderStatusChanged(Resource rider, String newStatus) {
        record(AuditEventType.RIDER_STATUS_CHANGED,
                riderLabel(rider) + SEPARATOR + safe(newStatus));
    }

    /**
     * A delivery completed.
     *
     * <p>The field is named {@code requestId}, not {@code orderId}, because what
     * the application actually completes here is a {@link ServiceRequest}. The
     * in-memory {@link Order} is matched to it separately and is not persisted
     * at all. Naming the field accurately keeps the trail honest about which
     * identifier it refers to.</p>
     */
    public static void orderDelivered(ServiceRequest request, Resource rider) {
        if (request == null) {
            return;
        }

        record(AuditEventType.ORDER_DELIVERED,
                "requestId=" + request.getRequestId()
                        + SEPARATOR + riderLabel(rider)
                        + SEPARATOR + "category=" + safe(request.getCategory())
                        + SEPARATOR + "from=" + request.getSourceLocationId()
                        + SEPARATOR + "to=" + request.getDestLocationId());
    }

    /** A shortest path was computed for the user. */
    public static void routeCalculated(String fromName, String toName,
                                       double distanceKm, double timeMin) {
        record(AuditEventType.ROUTE_CALCULATED,
                "from=" + safe(fromName)
                        + SEPARATOR + "to=" + safe(toName)
                        + SEPARATOR + "distanceKm=" + round(distanceKm)
                        + SEPARATOR + "timeMin=" + round(timeMin));
    }

    // ---------------------------------------------------------------------
    // Small helpers
    // ---------------------------------------------------------------------

    /** Renders a rider as "rider=7 (Kofi Mensah)", tolerating a missing rider. */
    private static String riderLabel(Resource rider) {
        if (rider == null) {
            return "rider=none";
        }
        return "rider=" + rider.getResourceId() + " (" + safe(rider.getName()) + ")";
    }

    /** Replaces a null with a visible placeholder and flattens any line breaks. */
    private static String safe(String value) {
        if (value == null) {
            return "unknown";
        }
        String cleaned = singleLine(value).trim();
        return cleaned.isEmpty() ? "unknown" : cleaned;
    }

    /**
     * Collapses newlines and carriage returns to spaces.
     *
     * <p>The audit trail is displayed one row per line. A value containing a
     * newline would split across two lines and look like two separate events,
     * so line breaks are removed at the point the row is built.</p>
     */
    private static String singleLine(String value) {
        return value.replace('\r', ' ').replace('\n', ' ');
    }

    /** Two decimal places, and never "NaN" or "Infinity" in the trail. */
    private static String round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "n/a";
        }
        return String.format("%.2f", value);
    }
}
