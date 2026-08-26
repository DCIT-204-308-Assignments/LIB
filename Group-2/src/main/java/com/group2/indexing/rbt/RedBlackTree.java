package com.group2.indexing.rbt;

import com.group2.indexing.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Left-leaning-free (CLRS style) red-black tree: a self-balancing BST that guarantees
 * O(log n) search/insert/delete by keeping the longest root-to-leaf path no more than
 * twice the shortest one. Uses a single shared NIL sentinel per tree, as in CLRS.
 */
public class RedBlackTree<K extends Comparable<K>, V> implements Index<K, V> {

    private enum Color { RED, BLACK }

    private final class Node {
        K key;
        V value;
        Color color;
        Node left;
        Node right;
        Node parent;

        Node(K key, V value, Color color) {
            this.key = key;
            this.value = value;
            this.color = color;
        }
    }

    private final Node nil = new Node(null, null, Color.BLACK);
    private Node root = nil;
    private int size;

    // ---------------------------------------------------------------- rotations

    private void leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;
        if (y.left != nil) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
    }

    private void rightRotate(Node x) {
        Node y = x.left;
        x.left = y.right;
        if (y.right != nil) {
            y.right.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == nil) {
            root = y;
        } else if (x == x.parent.right) {
            x.parent.right = y;
        } else {
            x.parent.left = y;
        }
        y.right = x;
        x.parent = y;
    }

    // ---------------------------------------------------------------- insert

    @Override
    public void put(K key, V value) {
        Node existing = findNode(key);
        if (existing != null) {
            existing.value = value;
            return;
        }

        Node z = new Node(key, value, Color.RED);
        z.left = nil;
        z.right = nil;

        Node y = nil;
        Node x = root;
        while (x != nil) {
            y = x;
            if (z.key.compareTo(x.key) < 0) {
                x = x.left;
            } else {
                x = x.right;
            }
        }
        z.parent = y;
        if (y == nil) {
            root = z;
        } else if (z.key.compareTo(y.key) < 0) {
            y.left = z;
        } else {
            y.right = z;
        }

        size++;
        insertFixup(z);
    }

    private void insertFixup(Node z) {
        while (z.parent.color == Color.RED) {
            if (z.parent == z.parent.parent.left) {
                Node uncle = z.parent.parent.right;
                if (uncle.color == Color.RED) {
                    z.parent.color = Color.BLACK;
                    uncle.color = Color.BLACK;
                    z.parent.parent.color = Color.RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        z = z.parent;
                        leftRotate(z);
                    }
                    z.parent.color = Color.BLACK;
                    z.parent.parent.color = Color.RED;
                    rightRotate(z.parent.parent);
                }
            } else {
                Node uncle = z.parent.parent.left;
                if (uncle.color == Color.RED) {
                    z.parent.color = Color.BLACK;
                    uncle.color = Color.BLACK;
                    z.parent.parent.color = Color.RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        rightRotate(z);
                    }
                    z.parent.color = Color.BLACK;
                    z.parent.parent.color = Color.RED;
                    leftRotate(z.parent.parent);
                }
            }
        }
        root.color = Color.BLACK;
    }

    // ---------------------------------------------------------------- search

    @Override
    public V get(K key) {
        Node node = findNode(key);
        return node == null ? null : node.value;
    }

    @Override
    public boolean containsKey(K key) {
        return findNode(key) != null;
    }

    private Node findNode(K key) {
        Node current = root;
        while (current != nil) {
            int cmp = key.compareTo(current.key);
            if (cmp < 0) {
                current = current.left;
            } else if (cmp > 0) {
                current = current.right;
            } else {
                return current;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- delete

    @Override
    public boolean remove(K key) {
        Node z = findNode(key);
        if (z == null) {
            return false;
        }
        delete(z);
        size--;
        return true;
    }

    private void transplant(Node u, Node v) {
        if (u.parent == nil) {
            root = v;
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        v.parent = u.parent;
    }

    private Node minimum(Node node) {
        while (node.left != nil) {
            node = node.left;
        }
        return node;
    }

    private void delete(Node z) {
        Node y = z;
        Color yOriginalColor = y.color;
        Node x;

        if (z.left == nil) {
            x = z.right;
            transplant(z, z.right);
        } else if (z.right == nil) {
            x = z.left;
            transplant(z, z.left);
        } else {
            y = minimum(z.right);
            yOriginalColor = y.color;
            x = y.right;
            if (y.parent == z) {
                x.parent = y;
            } else {
                transplant(y, y.right);
                y.right = z.right;
                y.right.parent = y;
            }
            transplant(z, y);
            y.left = z.left;
            y.left.parent = y;
            y.color = z.color;
        }

        if (yOriginalColor == Color.BLACK) {
            deleteFixup(x);
        }
    }

    private void deleteFixup(Node x) {
        while (x != root && x.color == Color.BLACK) {
            if (x == x.parent.left) {
                Node w = x.parent.right;
                if (w.color == Color.RED) {
                    w.color = Color.BLACK;
                    x.parent.color = Color.RED;
                    leftRotate(x.parent);
                    w = x.parent.right;
                }
                if (w.left.color == Color.BLACK && w.right.color == Color.BLACK) {
                    w.color = Color.RED;
                    x = x.parent;
                } else {
                    if (w.right.color == Color.BLACK) {
                        w.left.color = Color.BLACK;
                        w.color = Color.RED;
                        rightRotate(w);
                        w = x.parent.right;
                    }
                    w.color = x.parent.color;
                    x.parent.color = Color.BLACK;
                    w.right.color = Color.BLACK;
                    leftRotate(x.parent);
                    x = root;
                }
            } else {
                Node w = x.parent.left;
                if (w.color == Color.RED) {
                    w.color = Color.BLACK;
                    x.parent.color = Color.RED;
                    rightRotate(x.parent);
                    w = x.parent.left;
                }
                if (w.right.color == Color.BLACK && w.left.color == Color.BLACK) {
                    w.color = Color.RED;
                    x = x.parent;
                } else {
                    if (w.left.color == Color.BLACK) {
                        w.right.color = Color.BLACK;
                        w.color = Color.RED;
                        leftRotate(w);
                        w = x.parent.left;
                    }
                    w.color = x.parent.color;
                    x.parent.color = Color.BLACK;
                    w.left.color = Color.BLACK;
                    rightRotate(x.parent);
                    x = root;
                }
            }
        }
        x.color = Color.BLACK;
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
        if (node == nil) {
            return;
        }
        inOrder(node.left, keys);
        keys.add(node.key);
        inOrder(node.right, keys);
    }

    /**
     * Validates the red-black properties: the root is black, no red node has a red
     * child, and every root-to-NIL path has the same black-height. Used by tests.
     */
    public boolean isValidRedBlackTree() {
        if (root.color != Color.BLACK) {
            return false;
        }
        return blackHeight(root) != -1;
    }

    private int blackHeight(Node node) {
        if (node == nil) {
            return 1;
        }
        if (node.color == Color.RED) {
            if (node.left.color == Color.RED || node.right.color == Color.RED) {
                return -1;
            }
        }
        int leftHeight = blackHeight(node.left);
        if (leftHeight == -1) {
            return -1;
        }
        int rightHeight = blackHeight(node.right);
        if (rightHeight == -1) {
            return -1;
        }
        if (leftHeight != rightHeight) {
            return -1;
        }
        return leftHeight + (node.color == Color.BLACK ? 1 : 0);
    }
}
