package ds;

import java.util.NoSuchElementException;

public class Queue<T> {
    private final LinkedList<T> list;

    public Queue() {
        list = new LinkedList<>();
    }

    public void enqueue(T element) {
        list.addLast(element);
    }

    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("Queue is empty");
        return list.removeFirst();
    }

    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Queue is empty");
        return list.getFirst();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }
}
