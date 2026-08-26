package ds;

public class BTree<K extends Comparable<K>, V> {
    private static final int T = 3; // Minimum degree (defines capacity bounds)

    public static class BTreeNode<K, V> {
        public int n; // Number of keys currently stored
        @SuppressWarnings("unchecked")
        public K[] keys = (K[]) new Comparable[2 * T - 1];
        public Object[] values = new Object[2 * T - 1];
        @SuppressWarnings("unchecked")
        public BTreeNode<K, V>[] children = (BTreeNode<K, V>[]) new BTreeNode[2 * T];
        public boolean isLeaf;

        public BTreeNode(boolean isLeaf) {
            this.isLeaf = isLeaf;
            this.n = 0;
        }
    }

    private BTreeNode<K, V> root;
    private int size;

    public BTree() {
        root = new BTreeNode<>(true);
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public BTreeNode<K, V> getRoot() {
        return root;
    }

    public V search(K key) {
        return search(root, key);
    }

    @SuppressWarnings("unchecked")
    private V search(BTreeNode<K, V> node, K key) {
        int i = 0;
        while (i < node.n && key.compareTo(node.keys[i]) > 0) {
            i++;
        }
        if (i < node.n && key.compareTo(node.keys[i]) == 0) {
            return (V) node.values[i];
        }
        if (node.isLeaf) {
            return null;
        }
        return search(node.children[i], key);
    }

    public boolean containsKey(K key) {
        return search(key) != null;
    }

    public void insert(K key, V value) {
        if (updateValue(root, key, value)) {
            return;
        }
        BTreeNode<K, V> r = root;
        if (r.n == 2 * T - 1) {
            BTreeNode<K, V> s = new BTreeNode<>(false);
            root = s;
            s.children[0] = r;
            splitChild(s, 0, r);
            insertNonFull(s, key, value);
        } else {
            insertNonFull(r, key, value);
        }
        size++;
    }

    private boolean updateValue(BTreeNode<K, V> node, K key, V value) {
        if (node == null) return false;
        int i = 0;
        while (i < node.n && key.compareTo(node.keys[i]) > 0) {
            i++;
        }
        if (i < node.n && key.compareTo(node.keys[i]) == 0) {
            node.values[i] = value;
            return true;
        }
        if (node.isLeaf) return false;
        return updateValue(node.children[i], key, value);
    }

    public void clear() {
        root = new BTreeNode<>(true);
        size = 0;
    }

    private void insertNonFull(BTreeNode<K, V> node, K key, V value) {
        int i = node.n - 1;
        if (node.isLeaf) {
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                node.keys[i + 1] = node.keys[i];
                node.values[i + 1] = node.values[i];
                i--;
            }
            node.keys[i + 1] = key;
            node.values[i + 1] = value;
            node.n = node.n + 1;
        } else {
            while (i >= 0 && key.compareTo(node.keys[i]) < 0) {
                i--;
            }
            i++;
            BTreeNode<K, V> child = node.children[i];
            if (child.n == 2 * T - 1) {
                splitChild(node, i, child);
                if (key.compareTo(node.keys[i]) > 0) {
                    i++;
                }
            }
            insertNonFull(node.children[i], key, value);
        }
    }

    private void splitChild(BTreeNode<K, V> parent, int i, BTreeNode<K, V> child) {
        BTreeNode<K, V> z = new BTreeNode<>(child.isLeaf);
        z.n = T - 1;
        for (int j = 0; j < T - 1; j++) {
            z.keys[j] = child.keys[j + T];
            z.values[j] = child.values[j + T];
            child.keys[j + T] = null;
            child.values[j + T] = null;
        }
        if (!child.isLeaf) {
            for (int j = 0; j < T; j++) {
                z.children[j] = child.children[j + T];
                child.children[j + T] = null;
            }
        }
        child.n = T - 1;

        for (int j = parent.n; j >= i + 1; j--) {
            parent.children[j + 1] = parent.children[j];
        }
        parent.children[i + 1] = z;

        for (int j = parent.n - 1; j >= i; j--) {
            parent.keys[j + 1] = parent.keys[j];
            parent.values[j + 1] = parent.values[j];
        }
        parent.keys[i] = child.keys[T - 1];
        parent.values[i] = child.values[T - 1];
        child.keys[T - 1] = null;
        child.values[T - 1] = null;
        parent.n = parent.n + 1;
    }
}
