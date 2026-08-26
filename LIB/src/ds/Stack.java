package ds;

import java.util.NoSuchElementException;

public class Stack<T> {
    private final LinkedList<T> list;

    public Stack() {
        list = new LinkedList<>();
    }

    public void push(T element) {
        list.addFirst(element);
    }

    public T pop() {
        if (isEmpty()) throw new NoSuchElementException("Stack is empty");
        return list.removeFirst();
    }

    public T peek() {
        if (isEmpty()) throw new NoSuchElementException("Stack is empty");
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
