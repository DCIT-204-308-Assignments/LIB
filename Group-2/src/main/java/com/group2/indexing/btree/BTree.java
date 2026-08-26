package com.group2.indexing.btree;

import com.group2.indexing.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * B-tree of minimum degree {@code t}: every node other than the root holds between
 * {@code t-1} and {@code 2t-1} keys, keeping the tree shallow and disk/cache friendly.
 * O(log n) search/insert/delete, following the CLRS algorithm.
 */
public class BTree<K extends Comparable<K>, V> implements Index<K, V> {

    private final int minDegree;
    private Node root;
    private int size;

    public BTree(int minDegree) {
        if (minDegree < 2) {
            throw new IllegalArgumentException("minDegree must be >= 2");
        }
        this.minDegree = minDegree;
        this.root = new Node(true);
    }

    private final class Node {
        final List<K> keys = new ArrayList<>();
        final List<V> values = new ArrayList<>();
        final List<Node> children = new ArrayList<>();
        boolean leaf;

        Node(boolean leaf) {
            this.leaf = leaf;
        }

        int maxKeys() {
            return 2 * minDegree - 1;
        }

        int minKeys() {
            return minDegree - 1;
        }
    }

    // ---------------------------------------------------------------- search

    @Override
    public V get(K key) {
        return search(root, key);
    }

    @Override
    public boolean containsKey(K key) {
        return search(root, key) != null;
    }

    private V search(Node node, K key) {
        int i = 0;
        while (i < node.keys.size() && key.compareTo(node.keys.get(i)) > 0) {
            i++;
        }
        if (i < node.keys.size() && key.compareTo(node.keys.get(i)) == 0) {
            return node.values.get(i);
        }
        if (node.leaf) {
            return null;
        }
        return search(node.children.get(i), key);
    }

    // ---------------------------------------------------------------- insert

    @Override
    public void put(K key, V value) {
        Node existing = findNodeContaining(root, key);
        if (existing != null) {
            int i = indexOf(existing, key);
            existing.values.set(i, value);
            return;
        }

        if (root.keys.size() == root.maxKeys()) {
            Node newRoot = new Node(false);
            newRoot.children.add(root);
            root = newRoot;
            splitChild(newRoot, 0);
        }
        insertNonFull(root, key, value);
        size++;
    }

    private Node findNodeContaining(Node node, K key) {
        int i = indexOf(node, key);
        if (i < node.keys.size() && key.compareTo(node.keys.get(i)) == 0) {
            return node;
        }
        if (node.leaf) {
            return null;
        }
        return findNodeContaining(node.children.get(i), key);
    }

    private int indexOf(Node node, K key) {
        int i = 0;
        while (i < node.keys.size() && key.compareTo(node.keys.get(i)) > 0) {
            i++;
        }
        return i;
    }

    private void splitChild(Node parent, int index) {
        Node child = parent.children.get(index);
        Node sibling = new Node(child.leaf);
        int mid = minDegree - 1;

        K midKey = child.keys.get(mid);
        V midValue = child.values.get(mid);

        sibling.keys.addAll(child.keys.subList(mid + 1, child.keys.size()));
        sibling.values.addAll(child.values.subList(mid + 1, child.values.size()));
        if (!child.leaf) {
            sibling.children.addAll(child.children.subList(mid + 1, child.children.size()));
            removeRange(child.children, mid + 1, child.children.size());
        }
        removeRange(child.keys, mid, child.keys.size());
        removeRange(child.values, mid, child.values.size());

        parent.children.add(index + 1, sibling);
        parent.keys.add(index, midKey);
        parent.values.add(index, midValue);
    }

    private void insertNonFull(Node node, K key, V value) {
        int i = node.keys.size() - 1;
        if (node.leaf) {
            int pos = indexOf(node, key);
            node.keys.add(pos, key);
            node.values.add(pos, value);
            return;
        }

        int pos = indexOf(node, key);
        if (node.children.get(pos).keys.size() == node.maxKeys()) {
            splitChild(node, pos);
            if (key.compareTo(node.keys.get(pos)) > 0) {
                pos++;
            }
        }
        insertNonFull(node.children.get(pos), key, value);
    }

    // ---------------------------------------------------------------- delete

    @Override
    public boolean remove(K key) {
        if (!containsKey(key)) {
            return false;
        }
        delete(root, key);
        if (root.keys.isEmpty() && !root.leaf) {
            root = root.children.get(0);
        }
        size--;
        return true;
    }

    private void delete(Node node, K key) {
        int i = indexOf(node, key);

        if (i < node.keys.size() && key.compareTo(node.keys.get(i)) == 0) {
            if (node.leaf) {
                node.keys.remove(i);
                node.values.remove(i);
            } else {
                deleteFromInternalNode(node, i);
            }
        } else {
            if (node.leaf) {
                return;
            }
            boolean isLastChild = (i == node.keys.size());
            if (node.children.get(i).keys.size() < minDegree) {
                fill(node, i);
            }
            if (isLastChild && i > node.keys.size()) {
                delete(node.children.get(i - 1), key);
            } else {
                delete(node.children.get(i), key);
            }
        }
    }

    private void deleteFromInternalNode(Node node, int i) {
        K key = node.keys.get(i);
        Node leftChild = node.children.get(i);
        Node rightChild = node.children.get(i + 1);

        if (leftChild.keys.size() >= minDegree) {
            Node predNode = leftChild;
            while (!predNode.leaf) {
                predNode = predNode.children.get(predNode.children.size() - 1);
            }
            K predKey = predNode.keys.get(predNode.keys.size() - 1);
            V predValue = predNode.values.get(predNode.values.size() - 1);
            node.keys.set(i, predKey);
            node.values.set(i, predValue);
            delete(leftChild, predKey);
        } else if (rightChild.keys.size() >= minDegree) {
            Node succNode = rightChild;
            while (!succNode.leaf) {
                succNode = succNode.children.get(0);
            }
            K succKey = succNode.keys.get(0);
            V succValue = succNode.values.get(0);
            node.keys.set(i, succKey);
            node.values.set(i, succValue);
            delete(rightChild, succKey);
        } else {
            merge(node, i);
            delete(leftChild, key);
        }
    }

    /** Ensures {@code node.children.get(i)} has at least {@code minDegree} keys before recursing into it. */
    private void fill(Node node, int i) {
        if (i > 0 && node.children.get(i - 1).keys.size() >= minDegree) {
            borrowFromPrev(node, i);
        } else if (i < node.keys.size() && node.children.get(i + 1).keys.size() >= minDegree) {
            borrowFromNext(node, i);
        } else if (i < node.keys.size()) {
            merge(node, i);
        } else {
            merge(node, i - 1);
        }
    }

    private void borrowFromPrev(Node node, int i) {
        Node child = node.children.get(i);
        Node sibling = node.children.get(i - 1);

        child.keys.add(0, node.keys.get(i - 1));
        child.values.add(0, node.values.get(i - 1));
        if (!child.leaf) {
            child.children.add(0, sibling.children.remove(sibling.children.size() - 1));
        }

        node.keys.set(i - 1, sibling.keys.remove(sibling.keys.size() - 1));
        node.values.set(i - 1, sibling.values.remove(sibling.values.size() - 1));
    }

    private void borrowFromNext(Node node, int i) {
        Node child = node.children.get(i);
        Node sibling = node.children.get(i + 1);

        child.keys.add(node.keys.get(i));
        child.values.add(node.values.get(i));
        if (!child.leaf) {
            child.children.add(sibling.children.remove(0));
        }

        node.keys.set(i, sibling.keys.remove(0));
        node.values.set(i, sibling.values.remove(0));
    }

    /** Merges {@code node.children.get(i)}, {@code node.keys.get(i)} and {@code node.children.get(i+1)} into one node. */
    private void merge(Node node, int i) {
        Node child = node.children.get(i);
        Node sibling = node.children.get(i + 1);

        child.keys.add(node.keys.remove(i));
        child.values.add(node.values.remove(i));
        child.keys.addAll(sibling.keys);
        child.values.addAll(sibling.values);
        if (!child.leaf) {
            child.children.addAll(sibling.children);
        }

        node.children.remove(i + 1);
    }

    private void removeRange(List<?> list, int fromIndex, int toIndex) {
        list.subList(fromIndex, toIndex).clear();
    }

    // ---------------------------------------------------------------- misc

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** Returns all keys in ascending order. */
    public List<K> inOrderKeys() {
        List<K> keys = new ArrayList<>(size);
        inOrder(root, keys);
        return keys;
    }

    private void inOrder(Node node, List<K> keys) {
        for (int i = 0; i < node.keys.size(); i++) {
            if (!node.leaf) {
                inOrder(node.children.get(i), keys);
            }
            keys.add(node.keys.get(i));
        }
        if (!node.leaf) {
            inOrder(node.children.get(node.keys.size()), keys);
        }
    }

    /**
     * Validates the B-tree invariants: every non-root node has between {@code t-1} and
     * {@code 2t-1} keys, all leaves are at the same depth, and keys are sorted per node.
     * Used by tests.
     */
    public boolean isValidBTree() {
        if (root.keys.size() > root.maxKeys()) {
            return false;
        }
        int leafDepth = leafDepth(root, 0);
        return leafDepth != -1 && isValid(root, 0, true, leafDepth);
    }

    private int leafDepth(Node node, int depth) {
        if (node.leaf) {
            return depth;
        }
        return leafDepth(node.children.get(0), depth + 1);
    }

    private boolean isValid(Node node, int depth, boolean isRoot, int expectedLeafDepth) {
        int minKeys = isRoot ? 0 : node.minKeys();
        if (node.keys.size() < minKeys || node.keys.size() > node.maxKeys()) {
            return false;
        }
        for (int i = 1; i < node.keys.size(); i++) {
            if (node.keys.get(i - 1).compareTo(node.keys.get(i)) >= 0) {
                return false;
            }
        }
        if (node.leaf) {
            return depth == expectedLeafDepth;
        }
        if (node.children.size() != node.keys.size() + 1) {
            return false;
        }
        for (Node child : node.children) {
            if (!isValid(child, depth + 1, false, expectedLeafDepth)) {
                return false;
            }
        }
        return true;
    }
}
