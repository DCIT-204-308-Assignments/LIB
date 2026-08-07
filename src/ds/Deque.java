package ds;

import java.util.NoSuchElementException;

public class Deque<T> {
    private final LinkedList<T> list;

    public Deque() {
        list = new LinkedList<>();
    }

    public void addFront(T element) {
        list.addFirst(element);
    }

    public void addRear(T element) {
        list.addLast(element);
    }

    public T removeFront() {
        if (isEmpty()) throw new NoSuchElementException("Deque is empty");
        return list.removeFirst();
    }

    public T removeRear() {
        if (isEmpty()) throw new NoSuchElementException("Deque is empty");
        return list.removeLast();
    }

    public T peekFront() {
        if (isEmpty()) throw new NoSuchElementException("Deque is empty");
        return list.getFirst();
    }

    public T peekRear() {
        if (isEmpty()) throw new NoSuchElementException("Deque is empty");
        return list.getLast();
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
