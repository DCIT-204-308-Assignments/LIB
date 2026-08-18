package engines;

import ds.BST;
import ds.BTree;
import ds.DynamicArray;
import ds.HashTable;
import ds.RedBlackTree;
import models.Order;
import models.Resource;
import models.ServiceRequest;

public class IndexingEngine {

    // Existing ServiceRequest indexes
    private BST<Integer, ServiceRequest> bstIndex;
    private RedBlackTree<Integer, ServiceRequest> rbtIndex;
    private BTree<Integer, ServiceRequest> btreeIndex;
    private HashTable<Integer, ServiceRequest> hashIndex;

    // Delivery-specific Group 2 indexes
    private BST<Integer, Order> orderBSTIndex;
    private BTree<Integer, Order> orderBTreeIndex;

    private RedBlackTree<Integer, Resource> riderRBTIndex;
    private HashTable<Integer, Resource> riderHashIndex;

    private HashTable<String, DynamicArray<Order>> restaurantIndex;
    private HashTable<String, DynamicArray<Order>> customerIndex;
    private HashTable<String, DynamicArray<Order>> menuItemIndex;

    public IndexingEngine() {
        resetRequestIndexes();
        resetDeliveryIndexes();
    }

    // ── Service Request Indexing ──────────────────────────────────────────

    public void indexRequests(DynamicArray<ServiceRequest> requests) {
        resetRequestIndexes();

        if (requests == null) {
            return;
        }

        for (ServiceRequest req : requests) {
            if (req == null || req.getRequestId() < 0) {
                continue;
            }

            bstIndex.insert(req.getRequestId(), req);
            rbtIndex.insert(req.getRequestId(), req);
            btreeIndex.insert(req.getRequestId(), req);
            hashIndex.put(req.getRequestId(), req);
        }
    }

    public ServiceRequest searchBST(int id) {
        if (id < 0) {
            return null;
        }

        return bstIndex.search(id);
    }

    public ServiceRequest searchHashTable(int id) {
        if (id < 0) {
            return null;
        }

        return hashIndex.get(id);
    }

    public ServiceRequest searchRBT(int id) {
        if (id < 0) {
            return null;
        }

        return rbtIndex.search(id);
    }

    public ServiceRequest searchBTree(int id) {
        if (id < 0) {
            return null;
        }

        return btreeIndex.search(id);
    }

    // ── Delivery Data Indexing ────────────────────────────────────────────

    public void indexDeliveryData(
            DynamicArray<Order> orders,
            DynamicArray<Resource> riders) {

        resetDeliveryIndexes();

        if (orders != null) {
            for (Order order : orders) {
                if (order == null || order.getOrderId() < 0) {
                    continue;
                }

                orderBSTIndex.insert(order.getOrderId(), order);
                orderBTreeIndex.insert(order.getOrderId(), order);

                addOrderToIndex(
                        restaurantIndex,
                        order.getRestaurant(),
                        order
                );

                addOrderToIndex(
                        customerIndex,
                        order.getCustomerName(),
                        order
                );

                addOrderToIndex(
                        menuItemIndex,
                        order.getFoodItem(),
                        order
                );
            }
        }

        if (riders != null) {
            for (Resource rider : riders) {
                if (rider == null || rider.getResourceId() < 0) {
                    continue;
                }

                riderRBTIndex.insert(
                        rider.getResourceId(),
                        rider
                );

                riderHashIndex.put(
                        rider.getResourceId(),
                        rider
                );
            }
        }
    }

    // ── Order Search ──────────────────────────────────────────────────────

    public Order searchOrderBST(int orderId) {
        if (orderId < 0) {
            return null;
        }

        return orderBSTIndex.search(orderId);
    }

    public Order searchOrderBTree(int orderId) {
        if (orderId < 0) {
            return null;
        }

        return orderBTreeIndex.search(orderId);
    }

    // ── Rider Search ──────────────────────────────────────────────────────

    public Resource searchRiderRBT(int riderId) {
        if (riderId < 0) {
            return null;
        }

        return riderRBTIndex.search(riderId);
    }

    public Resource searchRiderHash(int riderId) {
        if (riderId < 0) {
            return null;
        }

        return riderHashIndex.get(riderId);
    }

    // ── Restaurant / Customer / Menu Search ──────────────────────────────

    public DynamicArray<Order> searchByRestaurant(String restaurant) {
        return lookupOrders(restaurantIndex, restaurant);
    }

    public DynamicArray<Order> searchByCustomer(String customerName) {
        return lookupOrders(customerIndex, customerName);
    }

    public DynamicArray<Order> searchByMenuItem(String foodItem) {
        return lookupOrders(menuItemIndex, foodItem);
    }

    // ── Metrics ───────────────────────────────────────────────────────────

    public int getBSTHeight() {
        return getBSTHeightRec(bstIndex.getRoot());
    }

    private int getBSTHeightRec(BST.Node<Integer, ServiceRequest> node) {
        if (node == null) {
            return 0;
        }

        return 1 + Math.max(
                getBSTHeightRec(node.left),
                getBSTHeightRec(node.right)
        );
    }

    public int getRBTHeight() {
        return rbtIndex.height();
    }

    public int getBTreeSize() {
        return btreeIndex.size();
    }

    public int getHashTableSize() {
        return hashIndex.size();
    }

    public int getHashTableCollisionCount() {
        return hashIndex.getCollisionCount();
    }

    public int getOrderBTreeSize() {
        return orderBTreeIndex.size();
    }

    public int getRiderRBTHeight() {
        return riderRBTIndex.height();
    }

    public int getRiderHashTableSize() {
        return riderHashIndex.size();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void resetRequestIndexes() {
        bstIndex = new BST<>();
        rbtIndex = new RedBlackTree<>();
        btreeIndex = new BTree<>();
        hashIndex = new HashTable<>();
    }

    private void resetDeliveryIndexes() {
        orderBSTIndex = new BST<>();
        orderBTreeIndex = new BTree<>();

        riderRBTIndex = new RedBlackTree<>();
        riderHashIndex = new HashTable<>();

        restaurantIndex = new HashTable<>();
        customerIndex = new HashTable<>();
        menuItemIndex = new HashTable<>();
    }

    private void addOrderToIndex(
            HashTable<String, DynamicArray<Order>> index,
            String rawKey,
            Order order) {

        String key = normalizeKey(rawKey);

        if (key == null) {
            return;
        }

        DynamicArray<Order> matchingOrders = index.get(key);

        if (matchingOrders == null) {
            matchingOrders = new DynamicArray<>();
            index.put(key, matchingOrders);
        }

        matchingOrders.add(order);
    }

    private DynamicArray<Order> lookupOrders(
            HashTable<String, DynamicArray<Order>> index,
            String rawKey) {

        DynamicArray<Order> result = new DynamicArray<>();
        String key = normalizeKey(rawKey);

        if (key == null) {
            return result;
        }

        DynamicArray<Order> stored = index.get(key);

        if (stored == null) {
            return result;
        }

        for (Order order : stored) {
            result.add(order);
        }

        return result;
    }

    private String normalizeKey(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase();

        if (normalized.isEmpty()) {
            return null;
        }

        return normalized;
    }
}