package ds;

public class BST<K extends Comparable<K>, V> {
    public static class Node<K, V> {
        public K key;
        public V value;
        public Node<K, V> left;
        public Node<K, V> right;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node<K, V> root;
    private int size;

    public BST() {
        root = null;
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Node<K, V> getRoot() {
        return root;
    }

    public void insert(K key, V value) {
        root = insertRec(root, key, value);
    }

    private Node<K, V> insertRec(Node<K, V> node, K key, V value) {
        if (node == null) {
            size++;
            return new Node<>(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = insertRec(node.left, key, value);
        } else if (cmp > 0) {
            node.right = insertRec(node.right, key, value);
        } else {
            node.value = value; // Update value
        }
        return node;
    }

    public V search(K key) {
        Node<K, V> node = searchRec(root, key);
        return node == null ? null : node.value;
    }

    private Node<K, V> searchRec(Node<K, V> node, K key) {
        if (node == null) return null;
        int cmp = key.compareTo(node.key);
        if (cmp < 0) return searchRec(node.left, key);
        if (cmp > 0) return searchRec(node.right, key);
        return node;
    }

    public DynamicArray<V> inorder() {
        DynamicArray<V> result = new DynamicArray<>();
        inorderRec(root, result);
        return result;
    }

    private void inorderRec(Node<K, V> node, DynamicArray<V> result) {
        if (node != null) {
            inorderRec(node.left, result);
            result.add(node.value);
            inorderRec(node.right, result);
        }
    }

    public void clear() {
        root = null;
        size = 0;
    }
}
