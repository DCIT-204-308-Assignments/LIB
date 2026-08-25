package com.group2.routing;

import java.util.List;

/** A delivery route: the sequence of stops and the total distance travelled. */
public record Route<T>(List<T> path, double totalDistance) {

    public boolean isReachable() {
        return !path.isEmpty();
    }
}
