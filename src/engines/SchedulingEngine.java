package engines;

import ds.CircularQueue;
import ds.DynamicArray;
import ds.MinHeap;
import ds.Queue;
import ds.Stack;
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
        @SuppressWarnings("unchecked")
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

    /**
     * Urgent Override Dispatcher (default threshold: urgency >= 4 counts as urgent).
     * See the overload below for the full explanation.
     */
    public static Stack<ServiceRequest> dispatchUrgentOverride(DynamicArray<ServiceRequest> requests) {
        return dispatchUrgentOverride(requests, 4);
    }

    /**
     * Urgent Override Dispatcher: uses a Stack (LIFO) to model an "interrupt" style
     * dispatch queue, similar to how a call center or emergency desk handles walk-ins.
     *
     * Requests are first sorted by submission time (oldest first). They are then split
     * into two groups while preserving that order: normal requests (urgency below the
     * threshold) and escalated/urgent requests (urgency >= threshold). The normal
     * requests are pushed onto the stack first, then the urgent requests are pushed on
     * top last.
     *
     * Because a Stack always pops the most recently pushed item first, this guarantees:
     *   1. Every urgent request is dispatched before any normal request.
     *   2. Among urgent requests, the most recently submitted one is dispatched first
     *      (a fresh emergency always preempts an older one still waiting).
     *   3. Once no urgent requests remain, normal requests are dispatched in the same
     *      "most recent first" LIFO order.
     *
     * @param requests        all requests (pending ones are filtered internally)
     * @param urgencyThreshold minimum urgency (1-5) to be treated as "urgent"
     */
    public static Stack<ServiceRequest> dispatchUrgentOverride(DynamicArray<ServiceRequest> requests, int urgencyThreshold) {
        DynamicArray<ServiceRequest> pending = filterPending(requests);
        SortingEngine.quickSort(pending, Comparator.comparingDouble(ServiceRequest::getTimeSubmittedMin));

        DynamicArray<ServiceRequest> normalList = new DynamicArray<>();
        DynamicArray<ServiceRequest> escalationList = new DynamicArray<>();
        for (ServiceRequest req : pending) {
            if (req.getUrgency() >= urgencyThreshold) {
                escalationList.add(req);
            } else {
                normalList.add(req);
            }
        }

        Stack<ServiceRequest> dispatchStack = new Stack<>();
        // Bottom layer: normal requests, oldest pushed first so the newest normal
        // request ends up nearer the top (dispatched sooner than older normal ones).
        for (ServiceRequest req : normalList) {
            dispatchStack.push(req);
        }
        // Top layer: urgent requests pushed last, so they are always popped first.
        for (ServiceRequest req : escalationList) {
            dispatchStack.push(req);
        }

        return dispatchStack;
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