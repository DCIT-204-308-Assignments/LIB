package engines;

import ds.CircularQueue;
import ds.DynamicArray;
import ds.MinHeap;
import ds.Queue;
import models.ServiceRequest;
import java.util.Comparator;

public class SchedulingEngine {

    /**
     * FIFO dispatcher: loads all pending requests into a FIFO Queue in order of submission time.
     */
    public static Queue<ServiceRequest> dispatchFIFO(DynamicArray<ServiceRequest> requests) {
        // First sort requests by submission time
        DynamicArray<ServiceRequest> pending = filterPending(requests);
        SortingEngine.quickSort(pending, Comparator.comparingDouble(ServiceRequest::getTimeSubmittedMin));

        Queue<ServiceRequest> queue = new Queue<>();
        for (ServiceRequest req : pending) {
            queue.enqueue(req);
        }
        return queue;
    }

    /**
     * Priority dispatcher: loads pending requests into a Max-Priority Heap (using MinHeap with inverted comparator).
     * Highest priority request is processed first.
     */
    public static MinHeap<ServiceRequest> dispatchPriority(DynamicArray<ServiceRequest> requests) {
        DynamicArray<ServiceRequest> pending = filterPending(requests);
        
        // MinHeap extracts the minimum value. We invert the comparison to make it act like a Max-Priority heap.
        Comparator<ServiceRequest> maxPriorityComp = (a, b) -> Double.compare(b.getPriority(), a.getPriority());
        
        MinHeap<ServiceRequest> heap = new MinHeap<>(pending.size() + 1, maxPriorityComp);
        for (ServiceRequest req : pending) {
            heap.insert(req);
        }
        return heap;
    }

    /**
     * Round-Robin Zone Dispatcher: dispatches requests rotating through customer zones.
     * We group requests by zone, load each zone's requests into a queue, and then draw from them circularly.
     */
    public static DynamicArray<ServiceRequest> dispatchRoundRobin(DynamicArray<ServiceRequest> requests, ds.HashTable<Integer, models.Location> locationMap) {
        DynamicArray<ServiceRequest> pending = filterPending(requests);
        
        // Find distinct zones
        DynamicArray<String> zones = new DynamicArray<>();
        for (ServiceRequest req : pending) {
            models.Location loc = locationMap.get(req.getDestLocationId());
            if (loc != null) {
                String zone = loc.getZone();
                boolean exists = false;
                for (String z : zones) {
                    if (z.equals(zone)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    zones.add(zone);
                }
            }
        }

        // Create queues for each zone
        CircularQueue<ServiceRequest>[] zoneQueues = (CircularQueue<ServiceRequest>[]) new CircularQueue[zones.size()];
        for (int i = 0; i < zones.size(); i++) {
            zoneQueues[i] = new CircularQueue<>(10);
        }

        // Fill queues
        for (ServiceRequest req : pending) {
            models.Location loc = locationMap.get(req.getDestLocationId());
            if (loc != null) {
                String zone = loc.getZone();
                for (int i = 0; i < zones.size(); i++) {
                    if (zones.get(i).equals(zone)) {
                        zoneQueues[i].enqueue(req);
                        break;
                    }
                }
            }
        }

        // Circulate round-robin
        DynamicArray<ServiceRequest> result = new DynamicArray<>();
        boolean elementsLeft = true;
        while (elementsLeft) {
            elementsLeft = false;
            for (int i = 0; i < zones.size(); i++) {
                if (!zoneQueues[i].isEmpty()) {
                    result.add(zoneQueues[i].dequeue());
                    elementsLeft = true;
                }
            }
        }
        return result;
    }

    private static DynamicArray<ServiceRequest> filterPending(DynamicArray<ServiceRequest> requests) {
        DynamicArray<ServiceRequest> pending = new DynamicArray<>();
        for (ServiceRequest req : requests) {
            if ("PENDING".equalsIgnoreCase(req.getStatus())) {
                pending.add(req);
            }
        }
        return pending;
    }
}
