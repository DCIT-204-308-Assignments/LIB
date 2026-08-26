package ds;

import java.util.NoSuchElementException;

public class CircularQueue<T> {
    private Object[] data;
    private int front;
    private int rear;
    private int size;

    public CircularQueue() {
        this(10);
    }

    public CircularQueue(int capacity) {
        data = new Object[capacity];
        front = 0;
        rear = 0;
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == data.length;
    }

    public void enqueue(T element) {
        if (isFull()) {
            resize();
        }
        data[rear] = element;
        rear = (rear + 1) % data.length;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("Queue is empty");
        T val = (T) data[front];
        data[front] = null; // gc
        front = (front + 1) % data.length;
        size--;
        return val;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Queue is empty");
        return (T) data[front];
    }

    private void resize() {
        int newCap = data.length * 2;
        if (newCap == 0) newCap = 1;
        Object[] newData = new Object[newCap];
        for (int i = 0; i < size; i++) {
            newData[i] = data[(front + i) % data.length];
        }
        data = newData;
        front = 0;
        rear = size;
    }

    public int getFrontPointer() {
        return front;
    }

    public int getRearPointer() {
        return rear;
    }

    public int getCapacity() {
        return data.length;
    }
}
