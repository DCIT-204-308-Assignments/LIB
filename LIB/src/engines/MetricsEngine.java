package engines;

import ds.DynamicArray;
import models.Order;
import models.Resource;

/**
 * Aggregates operational statistics from a set of orders and riders.
 * Works with any DynamicArray<Order>/<Resource> — orders produced by
 * SimulationEngine have distanceKm/estimatedDeliveryTimeMin/vehicleType
 * populated, so those numbers will be meaningful; orders coming straight
 * from the live UGSwiftApp session currently leave those fields at their
 * defaults (0 / "ANY"), since UGSwiftApp never calls their setters. Wiring
 * the UI to populate them is a good follow-up but out of scope here.
 */
public class MetricsEngine {

    public static class Stats {
        public int totalOrders;
        public int completedOrders;
        public int cancelledOrders;
        public int queuedOrders;
        public double avgDeliveryTimeMin;
        public double avgDistanceKm;
        public int bicycleDeliveries;
        public int motorcycleDeliveries;
        public int totalRiders;
        public int busyRiders;
        public double riderUtilisationPct;
    }

    public static Stats compute(DynamicArray<Order> orders, DynamicArray<Resource> riders) {
        Stats stats = new Stats();

        if (orders != null) {
            double timeSum = 0.0;
            double distanceSum = 0.0;
            int completedWithData = 0;

            for (Order order : orders) {
                if (order == null) {
                    continue;
                }
                stats.totalOrders++;

                String status = order.getStatus();
                if (Order.OrderState.COMPLETED.name().equalsIgnoreCase(status)) {
                    stats.completedOrders++;

                    if (order.getEstimatedDeliveryTimeMin() > 0) {
                        timeSum += order.getEstimatedDeliveryTimeMin();
                        distanceSum += order.getDistanceKm();
                        completedWithData++;
                    }

                    String vehicle = order.getVehicleType();
                    if (vehicle != null && vehicle.toUpperCase().contains("BICYCLE")) {
                        stats.bicycleDeliveries++;
                    } else if (vehicle != null && vehicle.toUpperCase().contains("MOTOR")) {
                        stats.motorcycleDeliveries++;
                    }
                } else if (Order.OrderState.CANCELLED.name().equalsIgnoreCase(status)) {
                    stats.cancelledOrders++;
                } else if (Order.OrderState.QUEUED.name().equalsIgnoreCase(status)) {
                    stats.queuedOrders++;
                }
            }

            stats.avgDeliveryTimeMin = completedWithData > 0 ? timeSum / completedWithData : 0.0;
            stats.avgDistanceKm = completedWithData > 0 ? distanceSum / completedWithData : 0.0;
        }

        if (riders != null) {
            for (Resource rider : riders) {
                if (rider == null) {
                    continue;
                }
                stats.totalRiders++;
                if (!rider.isAvailable()) {
                    stats.busyRiders++;
                }
            }
        }

        stats.riderUtilisationPct = stats.totalRiders > 0
                ? (100.0 * stats.busyRiders / stats.totalRiders)
                : 0.0;

        return stats;
    }
}
