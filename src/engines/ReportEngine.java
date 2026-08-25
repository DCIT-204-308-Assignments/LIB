package engines;

/**
 * Formats MetricsEngine.Stats into the plain-text performance report
 * described in Suggestions.md #36 — useful for pasting straight into a
 * coursework writeup or printing to the DSA Demo console.
 */
public class ReportEngine {

    public static String generate(MetricsEngine.Stats stats) {
        if (stats == null) {
            return "No data available for report.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=======================================\n");
        sb.append("       UG SWIFT PERFORMANCE REPORT\n");
        sb.append("=======================================\n\n");

        sb.append(String.format("Orders Processed:        %d%n", stats.totalOrders));
        sb.append(String.format("Orders Completed:        %d%n", stats.completedOrders));
        sb.append(String.format("Orders Cancelled:        %d%n", stats.cancelledOrders));
        sb.append(String.format("Orders Queued:            %d%n", stats.queuedOrders));
        sb.append(String.format("Average Delivery Time:   %.1f min%n", stats.avgDeliveryTimeMin));
        sb.append(String.format("Average Delivery Dist.:  %.2f km%n", stats.avgDistanceKm));
        sb.append("\n");
        sb.append(String.format("Bicycle Deliveries:      %d%n", stats.bicycleDeliveries));
        sb.append(String.format("Motorcycle Deliveries:   %d%n", stats.motorcycleDeliveries));
        sb.append("\n");
        sb.append(String.format("Total Riders:            %d%n", stats.totalRiders));
        sb.append(String.format("Busy Riders:             %d%n", stats.busyRiders));
        sb.append(String.format("Rider Utilisation:       %.1f%%%n", stats.riderUtilisationPct));
        sb.append("=======================================\n");

        return sb.toString();
    }
}
