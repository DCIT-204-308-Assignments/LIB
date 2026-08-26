package com.group2.dispatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Binary min-heap (priority queue) backed by an array list. O(log n) insert/extract,
 * O(1) peek. Used by {@link RiderDispatchService} to always pick the
 * highest-priority rider next.
 */
public class MinHeap<T> {

    private final List<T> elements = new ArrayList<>();
    private final Comparator<T> comparator;

    public MinHeap(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public void insert(T item) {
        elements.add(item);
        siftUp(elements.size() - 1);
    }

    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("heap is empty");
        }
        return elements.get(0);
    }

    public T extractMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("heap is empty");
        }
        T min = elements.get(0);
        T last = elements.remove(elements.size() - 1);
        if (!elements.isEmpty()) {
            elements.set(0, last);
            siftDown(0);
        }
        return min;
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (comparator.compare(elements.get(index), elements.get(parent)) >= 0) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }

    private void siftDown(int index) {
        int size = elements.size();
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;
            if (left < size && comparator.compare(elements.get(left), elements.get(smallest)) < 0) {
                smallest = left;
            }
            if (right < size && comparator.compare(elements.get(right), elements.get(smallest)) < 0) {
                smallest = right;
            }
            if (smallest == index) {
                break;
            }
            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int i, int j) {
        T temp = elements.get(i);
        elements.set(i, elements.get(j));
        elements.set(j, temp);
    }
}
