package engines;

import ds.BST;
import ds.BTree;
import ds.DynamicArray;
import ds.RedBlackTree;
import models.ServiceRequest;

public class IndexingEngine {
    private BST<Integer, ServiceRequest> bstIndex;
    private RedBlackTree<Integer, ServiceRequest> rbtIndex;
    private BTree<Integer, ServiceRequest> btreeIndex;

    public IndexingEngine() {
        bstIndex = new BST<>();
        rbtIndex = new RedBlackTree<>();
        btreeIndex = new BTree<>();
    }

    public void indexRequests(DynamicArray<ServiceRequest> requests) {
        bstIndex.clear();
        rbtIndex = new RedBlackTree<>(); // reset
        btreeIndex = new BTree<>(); // reset

        for (ServiceRequest req : requests) {
            bstIndex.insert(req.getRequestId(), req);
            rbtIndex.insert(req.getRequestId(), req);
            btreeIndex.insert(req.getRequestId(), req);
        }
    }

    public ServiceRequest searchBST(int id) {
        return bstIndex.search(id);
    }

    public ServiceRequest searchRBT(int id) {
        return rbtIndex.search(id);
    }

    public ServiceRequest searchBTree(int id) {
        return btreeIndex.search(id);
    }

    public int getBSTHeight() {
        return getBSTHeightRec(bstIndex.getRoot());
    }

    private int getBSTHeightRec(BST.Node<Integer, ServiceRequest> node) {
        if (node == null) return 0;
        return 1 + Math.max(getBSTHeightRec(node.left), getBSTHeightRec(node.right));
    }

    public int getRBTHeight() {
        return rbtIndex.height();
    }

    public int getBTreeSize() {
        return btreeIndex.size();
    }
}
