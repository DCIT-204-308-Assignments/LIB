package ds;

public class RedBlackTree<K extends Comparable<K>, V> {
    public static final boolean RED = true;
    public static final boolean BLACK = false;

    public static class Node<K, V> {
        public K key;
        public V value;
        public Node<K, V> left, right, parent;
        public boolean color;

        public Node(K key, V value, boolean color) {
            this.key = key;
            this.value = value;
            this.color = color;
        }
    }

    private Node<K, V> root;
    private int size;

    public RedBlackTree() {
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
        Node<K, V> node = new Node<>(key, value, RED);
        if (root == null) {
            root = node;
            root.color = BLACK;
            size++;
            return;
        }

        Node<K, V> temp = root;
        Node<K, V> parent = null;
        while (temp != null) {
            parent = temp;
            int cmp = key.compareTo(temp.key);
            if (cmp < 0) {
                temp = temp.left;
            } else if (cmp > 0) {
                temp = temp.right;
            } else {
                temp.value = value; // overwrite
                return;
            }
        }

        node.parent = parent;
        int cmp = key.compareTo(parent.key);
        if (cmp < 0) {
            parent.left = node;
        } else {
            parent.right = node;
        }
        size++;

        fixInsert(node);
    }

    public V search(K key) {
        Node<K, V> node = searchNode(root, key);
        return node == null ? null : node.value;
    }

    public boolean containsKey(K key) {
        return search(key) != null;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    private Node<K, V> searchNode(Node<K, V> node, K key) {
        while (node != null) {
            int cmp = key.compareTo(node.key);
            if (cmp < 0) node = node.left;
            else if (cmp > 0) node = node.right;
            else return node;
        }
        return null;
    }

    private void fixInsert(Node<K, V> k) {
        Node<K, V> u;
        while (k.parent != null && k.parent.color == RED) {
            if (k.parent == k.parent.parent.right) {
                u = k.parent.parent.left;
                if (u != null && u.color == RED) {
                    u.color = BLACK;
                    k.parent.color = BLACK;
                    k.parent.parent.color = RED;
                    k = k.parent.parent;
                } else {
                    if (k == k.parent.left) {
                        k = k.parent;
                        rotateRight(k);
                    }
                    k.parent.color = BLACK;
                    k.parent.parent.color = RED;
                    rotateLeft(k.parent.parent);
                }
            } else {
                u = k.parent.parent.right;
                if (u != null && u.color == RED) {
                    u.color = BLACK;
                    k.parent.color = BLACK;
                    k.parent.parent.color = RED;
                    k = k.parent.parent;
                } else {
                    if (k == k.parent.right) {
                        k = k.parent;
                        rotateLeft(k);
                    }
                    k.parent.color = BLACK;
                    k.parent.parent.color = RED;
                    rotateRight(k.parent.parent);
                }
            }
            if (k == root) {
                break;
            }
        }
        root.color = BLACK;
    }

    private void rotateLeft(Node<K, V> x) {
        Node<K, V> y = x.right;
        x.right = y.left;
        if (y.left != null) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == null) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
    }

    private void rotateRight(Node<K, V> x) {
        Node<K, V> y = x.left;
        x.left = y.right;
        if (y.right != null) {
            y.right.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == null) {
            root = y;
        } else if (x == x.parent.right) {
            x.parent.right = y;
        } else {
            x.parent.left = y;
        }
        y.right = x;
        x.parent = y;
    }

    public int height() {
        return heightRec(root);
    }

    private int heightRec(Node<K, V> node) {
        if (node == null) return 0;
        return 1 + Math.max(heightRec(node.left), heightRec(node.right));
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
}
