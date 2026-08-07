package engines;

import ds.MinHeap;
import ds.Queue;
import ds.DynamicArray;
import models.ServiceRequest;
import java.util.Comparator;

public class IncomingOrderManager {
    private final Queue<ServiceRequest> fifo;
    private final MinHeap<ServiceRequest> priorityHeap;

    public IncomingOrderManager() {
        this.fifo = new Queue<>();
        this.priorityHeap = new MinHeap<>(64, (a, b) -> Double.compare(b.getPriority(), a.getPriority()));
    }

    public void submit(ServiceRequest req, boolean highPriority) {
        if (highPriority) {
            priorityHeap.insert(req);
        } else {
            fifo.enqueue(req);
        }

        // persist immediately
        DatabaseManager.addServiceRequest(req);
    }

    public void requeue(ServiceRequest req, boolean highPriority) {
        if (highPriority) {
            priorityHeap.insert(req);
        } else {
            fifo.enqueue(req);
        }
    }

    public ServiceRequest next() {
        if (!priorityHeap.isEmpty()) {
            return priorityHeap.extractMin();
        }
        if (!fifo.isEmpty()) {
            return fifo.dequeue();
        }
        return null;
    }

    public boolean hasPending() {
        return !priorityHeap.isEmpty() || !fifo.isEmpty();
    }

    public int pendingCount() {
        return fifo.size() + priorityHeap.size();
    }

    public int fifoSize() {
        return fifo.size();
    }

    public int prioritySize() {
        return priorityHeap.size();
    }
}
