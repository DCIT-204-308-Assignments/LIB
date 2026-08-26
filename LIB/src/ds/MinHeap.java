package ds;

import java.util.Comparator;
import java.util.NoSuchElementException;

public class MinHeap<T> {
    private Object[] data;
    private int size;
    private final Comparator<? super T> comparator;

    public MinHeap(int capacity, Comparator<? super T> comparator) {
        this.data = new Object[capacity];
        this.size = 0;
        this.comparator = comparator;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void insert(T element) {
        if (size == data.length) {
            resize();
        }
        data[size] = element;
        siftUp(size);
        size++;
    }

    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (isEmpty()) throw new NoSuchElementException("Heap is empty");
        T minVal = (T) data[0];
        data[0] = data[size - 1];
        data[size - 1] = null;
        size--;
        if (size > 0) {
            siftDown(0);
        }
        return minVal;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Heap is empty");
        return (T) data[0];
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int index) {
        T element = (T) data[index];
        while (index > 0) {
            int parent = (index - 1) / 2;
            T parentVal = (T) data[parent];
            if (comparator.compare(element, parentVal) >= 0) {
                break;
            }
            data[index] = parentVal;
            index = parent;
        }
        data[index] = element;
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int index) {
        T element = (T) data[index];
        int half = size / 2;
        while (index < half) {
            int left = 2 * index + 1;
            int right = left + 1;
            int child = left;
            T childVal = (T) data[left];
            if (right < size && comparator.compare((T) data[right], childVal) < 0) {
                child = right;
                childVal = (T) data[right];
            }
            if (comparator.compare(element, childVal) <= 0) {
                break;
            }
            data[index] = childVal;
            index = child;
        }
        data[index] = element;
    }

    private void resize() {
        int newCap = data.length * 2;
        if (newCap == 0) newCap = 1;
        Object[] newData = new Object[newCap];
        System.arraycopy(data, 0, newData, 0, size);
        data = newData;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
        size = 0;
    }
}
