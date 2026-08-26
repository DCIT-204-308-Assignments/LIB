import java.util.Comparator;
import java.util.NoSuchElementException;
import ds.*;
import engines.*;
import models.*;

/**
 * UG Swift — DSA Unit Test Suite
 * DCIT 204/308 Joint Semester Project — Ghana Campus Delivery DSA
 *
 * Every data structure is tested against three categories required by the
 * project brief (Section 8 — Implementation constraints, Section 10 —
 * Correctness and testing expectations):
 *
 *   [NORMAL]   - the structure behaves correctly under typical usage.
 *   [BOUNDARY] - single element, resize/capacity limits, full/empty
 *                transitions, duplicate keys.
 *   [INVALID]  - out-of-range indices, empty-structure access, null keys,
 *                disconnected graphs, unreachable paths — all of which must
 *                fail predictably (thrown exception or a defined sentinel
 *                value such as null/-1/false), never silently corrupt state.
 */
public class UGSwiftTestSuite {
    private static int passed = 0;
    private static int failed = 0;

    private static int normalCount = 0;
    private static int boundaryCount = 0;
    private static int invalidCount = 0;
    private static int counterexampleCount = 0;

    public static void main(String[] args) {
        System.out.println("======================================================");
        System.out.println("   UG SWIFT -- DSA Unit Test Suite");
        System.out.println("   DCIT 204/308 Joint Semester Project");
        System.out.println("======================================================\n");

        testDynamicArray();
        testLinkedList();
        testStack();
        testQueue();
        testCircularQueue();
        testDeque();
        testMinHeap();
        testBST();
        testRedBlackTree();
        testHashTable();
        testDisjointSet();
        testBTree();
        testGraph();
        testSortingSearch();
        testModelsAndLifecycle();

        // Engine-level coverage (previously untested: Section 5 M5/M6/M8 and the
        // Section 7 requirement for a documented greedy-failure counterexample).
        testOptimisationEngine();
        testSchedulingEngine();
        testDriverPool();
        testIncomingOrderManager();
        testIndexingEngine();
        testDeliveryEngine();
        testAuditLog();

        System.out.println("\n======================================================");
        System.out.printf("  RESULTS: %d passed, %d failed  (out of %d total)%n", passed, failed, passed + failed);
        System.out.printf("  Coverage by category -> Normal: %d | Boundary: %d | Invalid input: %d | Counterexamples: %d%n",
                normalCount, boundaryCount, invalidCount, counterexampleCount);
        System.out.println("======================================================");
        if (failed == 0) {
            System.out.println("  ALL TESTS PASSED");
        } else {
            System.out.println("  SOME TESTS FAILED -- see above for details.");
        }

        printTraceTables();
    }

    // ── DynamicArray ─────────────────────────────────────────────────────
    private static void testDynamicArray() {
        section("DynamicArray");
        DynamicArray<Integer> arr = new DynamicArray<>();

        // [NORMAL]
        arr.add(10); arr.add(20); arr.add(30);
        ok("add/size", arr.size() == 3);
        ok("get(1)==20", arr.get(1) == 20);
        arr.set(0, 99);
        ok("set(0,99)", arr.get(0) == 99);
        arr.remove(0);
        ok("remove(0) -> new[0]==20", arr.get(0) == 20);
        DynamicArray<Integer> itArr = new DynamicArray<>();
        for (int i = 1; i <= 5; i++) itArr.add(i);
        int sum = 0;
        for (int v : itArr) sum += v;
        ok("iterator sum 1..5 == 15", sum == 15);

        // [BOUNDARY]
        DynamicArray<Integer> big = new DynamicArray<>(4); // starts below needed capacity
        for (int i = 0; i < 200; i++) big.add(i);
        boundary("resize doubles capacity correctly up to 200 elements", big.size() == 200 && big.get(199) == 199);
        DynamicArray<String> empty = new DynamicArray<>();
        boundary("isEmpty on freshly constructed array", empty.isEmpty());
        DynamicArray<Integer> single = new DynamicArray<>();
        single.add(42);
        boundary("single-element array: get(0)==42 and size==1", single.get(0) == 42 && single.size() == 1);
        single.remove(0);
        boundary("removing the only element empties the array", single.isEmpty());
        DynamicArray<Integer> ins = new DynamicArray<>();
        ins.add(1); ins.add(3);
        ins.add(1, 2); // insert exactly in the middle
        ins.add(ins.size(), 4); // insert at index == size (append boundary)
        boundary("add(index,val) at middle and at size (append) boundary", ins.get(1) == 2 && ins.get(3) == 4 && ins.size() == 4);

        // [INVALID]
        invalid("get(-1) throws IndexOutOfBoundsException", throwsException(() -> arr.get(-1), IndexOutOfBoundsException.class));
        invalid("get(size) throws IndexOutOfBoundsException", throwsException(() -> arr.get(arr.size()), IndexOutOfBoundsException.class));
        invalid("remove on empty array throws IndexOutOfBoundsException",
                throwsException(() -> new DynamicArray<Integer>().remove(0), IndexOutOfBoundsException.class));
        invalid("add(index,val) with index > size throws IndexOutOfBoundsException",
                throwsException(() -> arr.add(arr.size() + 5, 1), IndexOutOfBoundsException.class));
    }

    // ── LinkedList ────────────────────────────────────────────────────────
    private static void testLinkedList() {
        section("LinkedList");
        LinkedList<Integer> list = new LinkedList<>();

        // [NORMAL]
        list.addFirst(2); list.addFirst(1); list.addLast(3); // [1,2,3]
        ok("addFirst/addLast: [1,2,3]", list.getFirst() == 1 && list.getLast() == 3);
        ok("size == 3", list.size() == 3);
        list.removeFirst();
        ok("removeFirst -> head=2", list.getFirst() == 2);
        list.removeLast();
        ok("removeLast -> tail=2 (single element remains)", list.getLast() == 2);
        LinkedList<String> sl = new LinkedList<>();
        sl.addLast("A"); sl.addLast("B"); sl.addLast("C");
        StringBuilder sb = new StringBuilder();
        for (String s : sl) sb.append(s);
        ok("iterator traverses A,B,C in order", sb.toString().equals("ABC"));

        // [BOUNDARY]
        LinkedList<Integer> single = new LinkedList<>();
        single.addFirst(7);
        boundary("single-element list: getFirst==getLast==7", single.getFirst() == 7 && single.getLast() == 7);
        single.removeFirst();
        boundary("removing the only element empties the list (head==tail==null)", single.isEmpty());
        LinkedList<Integer> mid = new LinkedList<>();
        mid.addLast(1); mid.addLast(2); mid.addLast(3);
        LinkedList.Node<Integer> headNode = mid.getHeadNode();
        mid.insertAfter(headNode, 99); // [1,99,2,3]
        boundary("insertAfter head node splices correctly", mid.getHeadNode().next.data == 99 && mid.size() == 4);
        LinkedList.Node<Integer> tailNode = mid.getTailNode();
        mid.insertAfter(tailNode, 100); // insertAfter the tail must update tail pointer
        boundary("insertAfter tail node updates tail reference", mid.getLast() == 100 && mid.getTailNode().data == 100);

        // [INVALID]
        invalid("removeFirst on empty list throws NoSuchElementException",
                throwsException(() -> new LinkedList<Integer>().removeFirst(), NoSuchElementException.class));
        invalid("getFirst on empty list throws NoSuchElementException",
                throwsException(() -> new LinkedList<Integer>().getFirst(), NoSuchElementException.class));
        invalid("insertAfter(null, x) throws IllegalArgumentException",
                throwsException(() -> new LinkedList<Integer>().insertAfter(null, 5), IllegalArgumentException.class));
    }

    // ── Stack ─────────────────────────────────────────────────────────────
    private static void testStack() {
        section("Stack");
        Stack<Integer> stack = new Stack<>();

        // [NORMAL]
        stack.push(1); stack.push(2); stack.push(3);
        ok("peek == 3 (LIFO top)", stack.peek() == 3);
        int a = stack.pop(), b = stack.pop(), c = stack.pop();
        ok("pop order is strictly LIFO: 3,2,1", a == 3 && b == 2 && c == 1);
        Stack<String> undo = new Stack<>();
        undo.push("assign_req_1"); undo.push("assign_req_2");
        ok("undo-log simulation: last action popped first", undo.pop().equals("assign_req_2"));

        // [BOUNDARY]
        boundary("isEmpty true after popping every element", stack.isEmpty());
        Stack<Integer> single = new Stack<>();
        single.push(5);
        boundary("single-element stack: peek==pop==5, then empty", single.peek() == 5 && single.pop() == 5 && single.isEmpty());

        // [INVALID]
        Stack<Integer> emptyStack = new Stack<>();
        invalid("pop on empty stack throws NoSuchElementException",
                throwsException(emptyStack::pop, NoSuchElementException.class));
        invalid("peek on empty stack throws NoSuchElementException",
                throwsException(emptyStack::peek, NoSuchElementException.class));
    }

    // ── Queue ─────────────────────────────────────────────────────────────
    private static void testQueue() {
        section("Queue");
        Queue<String> q = new Queue<>();

        // [NORMAL]
        q.enqueue("A"); q.enqueue("B"); q.enqueue("C");
        ok("dequeue is FIFO -> A first", q.dequeue().equals("A"));
        ok("peek == B (new head)", q.peek().equals("B"));
        ok("size == 2 after one dequeue", q.size() == 2);

        // [BOUNDARY]
        Queue<Integer> single = new Queue<>();
        single.enqueue(9);
        boundary("single-element queue: peek==dequeue==9, then empty", single.peek() == 9 && single.dequeue() == 9 && single.isEmpty());

        // [INVALID]
        Queue<Integer> emptyQ = new Queue<>();
        invalid("dequeue on empty queue throws NoSuchElementException",
                throwsException(emptyQ::dequeue, NoSuchElementException.class));
        invalid("peek on empty queue throws NoSuchElementException",
                throwsException(emptyQ::peek, NoSuchElementException.class));
    }

    // ── CircularQueue ─────────────────────────────────────────────────────
    private static void testCircularQueue() {
        section("CircularQueue");
        CircularQueue<Integer> cq = new CircularQueue<>(4);

        // [NORMAL] enqueue/dequeue with front/rear wrap-around trace
        cq.enqueue(1); cq.enqueue(2); cq.enqueue(3); cq.enqueue(4); // front=0 rear=0(wrapped) full
        int frontBefore = cq.getFrontPointer();
        cq.dequeue(); cq.dequeue();                                 // front now advances to 2
        cq.enqueue(5); cq.enqueue(6);                                // rear wraps past array end back to 0,1
        ok("front pointer advanced from " + frontBefore + " after two dequeues", cq.getFrontPointer() == 2);
        ok("wrap-around: peek==3 (oldest remaining), size==4", cq.peek() == 3 && cq.size() == 4);

        // [BOUNDARY]
        CircularQueue<Integer> full = new CircularQueue<>(3);
        full.enqueue(1); full.enqueue(2); full.enqueue(3);
        boundary("isFull() true exactly at capacity", full.isFull());
        CircularQueue<Integer> smol = new CircularQueue<>(2);
        smol.enqueue(10); smol.enqueue(20);
        boolean wasFullBeforeResize = smol.isFull();
        smol.enqueue(30); // must trigger internal resize rather than reject/overwrite
        boundary("enqueue on a full queue resizes instead of failing; order preserved",
                wasFullBeforeResize && smol.size() == 3 && smol.peek() == 10 && smol.getCapacity() > 2);

        // [INVALID]
        CircularQueue<Integer> emptyCq = new CircularQueue<>(4);
        invalid("dequeue on empty circular queue throws NoSuchElementException",
                throwsException(emptyCq::dequeue, NoSuchElementException.class));
        invalid("peek on empty circular queue throws NoSuchElementException",
                throwsException(emptyCq::peek, NoSuchElementException.class));
    }

    // ── Deque ─────────────────────────────────────────────────────────────
    private static void testDeque() {
        section("Deque");
        Deque<Integer> dq = new Deque<>();

        // [NORMAL]
        dq.addRear(2); dq.addFront(1); dq.addRear(3); // [1,2,3]
        ok("addFront/addRear build [1,2,3]", dq.peekFront() == 1 && dq.peekRear() == 3);
        dq.removeFront();
        ok("removeFront -> new front=2", dq.peekFront() == 2);
        dq.removeRear();
        ok("removeRear -> new rear=2 (single element left)", dq.peekRear() == 2);
        Deque<String> urgDq = new Deque<>();
        urgDq.addRear("OrderA"); urgDq.addRear("OrderB");
        urgDq.addFront("URGENT_OrderC");
        ok("urgent request jumps queue via addFront", urgDq.peekFront().equals("URGENT_OrderC"));

        // [BOUNDARY]
        Deque<Integer> single = new Deque<>();
        single.addFront(8);
        boundary("single-element deque: front==rear==8", single.peekFront() == 8 && single.peekRear() == 8);
        single.removeRear();
        boundary("removing the only element empties the deque", single.isEmpty());

        // [INVALID]
        Deque<Integer> emptyDq = new Deque<>();
        invalid("removeFront on empty deque throws NoSuchElementException",
                throwsException(emptyDq::removeFront, NoSuchElementException.class));
        invalid("removeRear on empty deque throws NoSuchElementException",
                throwsException(emptyDq::removeRear, NoSuchElementException.class));
    }

    // ── MinHeap ───────────────────────────────────────────────────────────
    private static void testMinHeap() {
        section("MinHeap (Priority Queue)");
        MinHeap<Integer> heap = new MinHeap<>(10, Integer::compareTo);

        // [NORMAL]
        heap.insert(5); heap.insert(2); heap.insert(8); heap.insert(1);
        ok("peek == 1 (min at root)", heap.peek() == 1);
        heap.extractMin();
        ok("after extractMin, peek == 2", heap.peek() == 2);
        int prev = heap.extractMin();
        int curr = heap.extractMin();
        ok("successive extractMin calls come out in ascending order", prev <= curr);

        Comparator<ServiceRequest> maxPrio = (a, b) -> Double.compare(b.getPriority(), a.getPriority());
        MinHeap<ServiceRequest> pq = new MinHeap<>(10, maxPrio);
        pq.insert(new ServiceRequest(1, 1, 2, "Waakye", 3, 480, 560, "PENDING", 0));
        pq.insert(new ServiceRequest(2, 3, 4, "Documents", 5, 490, 520, "PENDING", 0));
        pq.insert(new ServiceRequest(3, 5, 6, "Groceries", 1, 500, 620, "PENDING", 0));
        ok("dispatch order: highest-urgency request extracted first", pq.extractMin().getUrgency() == 5);

        // [BOUNDARY]
        MinHeap<Integer> single = new MinHeap<>(4, Integer::compareTo);
        single.insert(42);
        boundary("single-element heap: peek==extractMin==42, then empty", single.peek() == 42 && single.extractMin() == 42 && single.isEmpty());
        MinHeap<Integer> tiny = new MinHeap<>(2, Integer::compareTo); // capacity 2, force resize
        int[] vals = {9, 4, 7, 1, 3};
        for (int v : vals) tiny.insert(v);
        boundary("heap resizes past initial capacity and still extracts fully sorted", isFullyAscending(tiny, vals.length));

        // [INVALID]
        MinHeap<Integer> emptyHeap = new MinHeap<>(4, Integer::compareTo);
        invalid("extractMin on empty heap throws NoSuchElementException",
                throwsException(emptyHeap::extractMin, NoSuchElementException.class));
        invalid("peek on empty heap throws NoSuchElementException",
                throwsException(emptyHeap::peek, NoSuchElementException.class));
    }

    private static boolean isFullyAscending(MinHeap<Integer> heap, int expectedCount) {
        int count = 0;
        int prev = Integer.MIN_VALUE;
        while (!heap.isEmpty()) {
            int v = heap.extractMin();
            if (v < prev) return false;
            prev = v;
            count++;
        }
        return count == expectedCount;
    }

    // ── BST ───────────────────────────────────────────────────────────────
    private static void testBST() {
        section("Binary Search Tree (BST)");
        BST<Integer, String> bst = new BST<>();

        // [NORMAL]
        bst.insert(50, "Accra"); bst.insert(30, "Kumasi"); bst.insert(70, "Tamale");
        bst.insert(20, "Cape Coast"); bst.insert(40, "Sunyani");
        ok("search(30) == Kumasi", "Kumasi".equals(bst.search(30)));
        BST<Integer, Integer> numBst = new BST<>();
        int[] keys = {5, 3, 7, 1, 4};
        for (int k : keys) numBst.insert(k, k);
        ok("inorder traversal is sorted ascending", isSortedAscending(numBst.inorder()));

        // [BOUNDARY]
        boundary("delete(30) on populated tree returns true", bst.delete(30));
        boundary("search(30) returns null after delete", bst.search(30) == null);
        boundary("size reflects the deletion (4 remain)", bst.size() == 4);
        BST<Integer, Integer> twoChildCase = new BST<>();
        twoChildCase.insert(50, 50); twoChildCase.insert(30, 30); twoChildCase.insert(70, 70);
        twoChildCase.insert(60, 60); twoChildCase.insert(80, 80);
        twoChildCase.delete(70); // node with two children: successor (80's predecessor path) must be spliced in
        boundary("deleting a two-child node preserves BST ordering (inorder still sorted)", isSortedAscending(twoChildCase.inorder()));
        BST<Integer, String> single = new BST<>();
        single.insert(1, "only");
        single.delete(1);
        boundary("deleting the only node empties the tree", single.isEmpty() && single.getRoot() == null);
        BST<Integer, String> dup = new BST<>();
        dup.insert(10, "first");
        dup.insert(10, "second"); // duplicate key
        boundary("inserting a duplicate key updates value, does not grow size", dup.size() == 1 && "second".equals(dup.search(10)));

        // [INVALID]
        invalid("search on empty tree returns null (not an exception)", new BST<Integer, String>().search(99) == null);
        invalid("delete of a non-existent key returns false", !bst.delete(9999));
    }

    private static boolean isSortedAscending(DynamicArray<Integer> arr) {
        for (int i = 0; i < arr.size() - 1; i++) {
            if (arr.get(i) > arr.get(i + 1)) return false;
        }
        return true;
    }

    // ── RedBlackTree ──────────────────────────────────────────────────────
    private static void testRedBlackTree() {
        section("Red-Black Tree");
        RedBlackTree<Integer, String> rbt = new RedBlackTree<>();

        // [NORMAL]
        rbt.insert(10, "Legon"); rbt.insert(5, "Madina"); rbt.insert(15, "Tema");
        rbt.insert(3, "Nungua"); rbt.insert(7, "Ofankor");
        ok("search(5) == Madina", "Madina".equals(rbt.search(5)));
        ok("root color is always BLACK after insertions", rbt.getRoot().color == RedBlackTree.BLACK);

        // [BOUNDARY] — sequential ascending insert is the BST worst case (degenerates to a
        // linked list without rebalancing); a real red-black tree must stay height-balanced.
        RedBlackTree<Integer, Integer> ascending = new RedBlackTree<>();
        for (int i = 1; i <= 15; i++) ascending.insert(i, i);
        int h = ascending.height();
        boundary("15 sequential ascending inserts stay height-balanced (height <= 2*log2(16)=8, not 15)", h <= 8);
        RedBlackTree<Integer, String> dup = new RedBlackTree<>();
        dup.insert(10, "first");
        dup.insert(10, "second");
        boundary("duplicate key insert updates value, size stays 1", dup.size() == 1 && "second".equals(dup.search(10)));
        DynamicArray<Integer> inorder = ascending.inorder();
        boundary("inorder traversal remains sorted despite rotations", isSortedAscending(inorder));

        // [INVALID]
        invalid("search for a missing key returns null", rbt.search(999) == null);
    }

    // ── HashTable ─────────────────────────────────────────────────────────
    private static void testHashTable() {
        section("Hash Table");
        HashTable<String, Integer> ht = new HashTable<>(11);

        // [NORMAL]
        ht.put("Kofi", 1); ht.put("Ama", 2); ht.put("Yaw", 3);
        ok("get('Ama') == 2", ht.get("Ama") == 2);
        ht.put("Ama", 99);
        ok("re-putting an existing key updates its value", ht.get("Ama") == 99);
        ht.remove("Yaw");
        ok("get('Yaw') == null after remove", ht.get("Yaw") == null);
        ok("size == 2 after remove", ht.size() == 2);

        // [BOUNDARY] — force real collisions: capacity 5, keys 0/5/10 all hash to bucket 0
        HashTable<Integer, String> collideTable = new HashTable<>(5);
        collideTable.put(0, "zero");
        collideTable.put(5, "five");   // collides with 0 (5 % 5 == 0)
        collideTable.put(10, "ten");   // collides again (10 % 5 == 0)
        boundary("colliding keys are all retrievable despite sharing a bucket",
                "zero".equals(collideTable.get(0)) && "five".equals(collideTable.get(5)) && "ten".equals(collideTable.get(10)));
        boundary("collision statistics increment exactly once per extra key in the same bucket",
                collideTable.getCollisionCount() == 2);

        // [BOUNDARY] — load factor 0.75 must trigger an automatic resize
        HashTable<Integer, String> growTable = new HashTable<>(5);
        for (int i = 0; i < 5; i++) growTable.put(i, "v" + i); // 4th put pushes load factor to 0.8 -> resize
        boundary("table capacity grows once load factor >= 0.75 is reached", growTable.getCapacity() > 5);
        boundary("all entries survive the resize/rehash", growTable.size() == 5 && "v3".equals(growTable.get(3)));

        // [INVALID]
        invalid("put(null, ...) throws IllegalArgumentException",
                throwsException(() -> ht.put(null, 1), IllegalArgumentException.class));
        invalid("get on a missing key returns null (no exception)", ht.get("DoesNotExist") == null);
        invalid("remove on a missing key returns null", ht.remove("DoesNotExist") == null);
    }

    // ── DisjointSet ───────────────────────────────────────────────────────
    private static void testDisjointSet() {
        section("Disjoint Set (Union-Find)");
        DisjointSet ds = new DisjointSet(10);

        // [NORMAL]
        ds.union(0, 1); ds.union(2, 3); ds.union(0, 2);
        ok("find(0)==find(3) after transitive union (path compression)", ds.find(0) == ds.find(3));
        ok("find(4) != find(0) for a location never unioned", ds.find(4) != ds.find(0));

        // [BOUNDARY]
        DisjointSet single = new DisjointSet(1);
        boundary("a single-element set is its own root", single.find(0) == 0);
        boundary("unioning an element with itself returns false (already same set)", !single.union(0, 0));
        boundary("re-unioning an already-connected pair returns false", !ds.union(0, 3));

        // [INVALID]
        invalid("find() on an out-of-range index throws ArrayIndexOutOfBoundsException",
                throwsException(() -> ds.find(100), ArrayIndexOutOfBoundsException.class));
        invalid("union() with a negative index throws ArrayIndexOutOfBoundsException",
                throwsException(() -> ds.union(-1, 2), ArrayIndexOutOfBoundsException.class));
    }

    // ── BTree ─────────────────────────────────────────────────────────────
    private static void testBTree() {
        section("B-Tree (minimum degree t=3, max 5 keys/node)");
        BTree<Integer, String> bt = new BTree<>();

        // [NORMAL]
        for (int i = 1; i <= 20; i++) bt.insert(i, "Zone" + i);
        ok("search(10) == Zone10", "Zone10".equals(bt.search(10)));
        ok("search(1) == Zone1 (first key)", "Zone1".equals(bt.search(1)));
        ok("search(20) == Zone20 (last key)", "Zone20".equals(bt.search(20)));

        // [BOUNDARY] — node split trace: t=3 means a leaf can hold at most 2t-1=5 keys.
        // The 6th insert must split the root and increase the tree's height.
        BTree<Integer, String> splitTrace = new BTree<>();
        for (int i = 1; i <= 5; i++) splitTrace.insert(i, "K" + i);
        boolean rootStillLeafAt5 = splitTrace.getRoot().isLeaf;
        int keysInRootAt5 = splitTrace.getRoot().n;
        splitTrace.insert(6, "K6"); // triggers the split
        boolean rootIsInternalAfterSplit = !splitTrace.getRoot().isLeaf;
        int keysInRootAfterSplit = splitTrace.getRoot().n;
        boundary("root holds all 5 keys before the split (leaf, n=5)", rootStillLeafAt5 && keysInRootAt5 == 5);
        boundary("6th insert splits the root: becomes internal with a single median key",
                rootIsInternalAfterSplit && keysInRootAfterSplit == 1);
        boundary("all 6 keys remain searchable after the split", "K1".equals(splitTrace.search(1)) && "K6".equals(splitTrace.search(6)));

        // [INVALID]
        invalid("search for a missing key returns null", bt.search(99) == null);
        invalid("search on an empty B-tree returns null", new BTree<Integer, String>().search(1) == null);
    }

    // ── Graph (adjacency list + matrix, BFS/DFS/Dijkstra/MST) ──────────────
    private static void testGraph() {
        section("Graph (adjacency list/matrix, traversal & shortest path)");

        // Build a small campus-style network:
        //   1 -- 2 -- 3      (connected component, 1->3 direct edge is a longer detour)
        //   4 -> 5           (one-way road, disconnected from {1,2,3})
        Graph g = new Graph(5);
        g.addLocation(new Location(1, "Commonwealth Hall", "North", "HOSTEL", 5.65, -0.187));
        g.addLocation(new Location(2, "Night Market", "Central", "MARKET", 5.651, -0.186));
        g.addLocation(new Location(3, "Balme Library", "Central", "ACADEMIC", 5.652, -0.185));
        g.addLocation(new Location(4, "Legon Hall", "South", "HOSTEL", 5.648, -0.184));
        g.addLocation(new Location(5, "Athletic Oval", "South", "SPORT", 5.647, -0.183));

        g.addRoad(new RoadEdge(1, 1, 2, "Commonwealth", "NightMkt", 0.5, 5, "LOW", "GOOD", 1.0, false, 1.0));
        g.addRoad(new RoadEdge(2, 2, 3, "NightMkt", "Balme", 0.5, 5, "LOW", "GOOD", 1.0, false, 1.0));
        g.addRoad(new RoadEdge(3, 1, 3, "Commonwealth", "Balme", 3.0, 20, "HIGH", "FAIR", 1.5, false, 5.0)); // longer direct road
        g.addRoad(new RoadEdge(4, 4, 5, "LegonHall", "Oval", 1.0, 8, "LOW", "GOOD", 1.0, true, 2.0)); // ONE-WAY

        // [NORMAL]
        DynamicArray<Integer> bfs = RouteEngine.bfsReachable(g, 1);
        ok("BFS from node 1 reaches exactly {1,2,3}", containsAll(bfs, 1, 2, 3) && bfs.size() == 3);
        DynamicArray<Integer> dfs = RouteEngine.dfsTraversal(g, 1);
        ok("DFS from node 1 visits the same connected component {1,2,3}", containsAll(dfs, 1, 2, 3) && dfs.size() == 3);
        RouteEngine.PathResult sp = RouteEngine.dijkstra(g, 1, 3);
        ok("Dijkstra prefers the cheaper 1->2->3 path (weight 2.0) over the direct 1->3 road (weight 5.0)",
                sp != null && sp.totalWeight == 2.0 && sp.path.size() == 3);

        // [BOUNDARY]
        boundary("undirected road is traversable in both directions",
                contains(getNeighborIds(g, 2), 1) && contains(getNeighborIds(g, 2), 3));
        boundary("one-way road (4->5) creates an outgoing edge from 4", contains(getNeighborIds(g, 4), 5));
        boundary("one-way road does NOT create a return edge from 5 to 4", !contains(getNeighborIds(g, 5), 4));
        boundary("constructing Graph(-1) is rejected", throwsException(() -> new Graph(-1), IllegalArgumentException.class));

        // [INVALID] — disconnected graph / unreachable path (explicit edge case from Section 10)
        RouteEngine.PathResult unreachable = RouteEngine.dijkstra(g, 1, 5);
        invalid("Dijkstra between disconnected components returns null (no path exists)", unreachable == null);
        invalid("BFS from an unregistered node id returns an empty list, not an exception",
                RouteEngine.bfsReachable(g, 0).isEmpty());
        invalid("getNeighbors on a location that was never registered returns null",
                g.getNeighbors(0) == null);

        // MST cross-check: Kruskal and Prim must agree on total cost for the same network.
        Graph mstGraph = new Graph(3);
        mstGraph.addLocation(new Location(1, "A", "Z", "T", 0, 0));
        mstGraph.addLocation(new Location(2, "B", "Z", "T", 0, 0));
        mstGraph.addLocation(new Location(3, "C", "Z", "T", 0, 0));
        RoadEdge e12 = new RoadEdge(1, 1, 2, "A", "B", 1, 1, "LOW", "GOOD", 1.0, false, 1.0);
        RoadEdge e23 = new RoadEdge(2, 2, 3, "B", "C", 2, 2, "LOW", "GOOD", 1.0, false, 2.0);
        RoadEdge e13 = new RoadEdge(3, 1, 3, "A", "C", 3, 3, "LOW", "GOOD", 1.0, false, 3.0);
        mstGraph.addRoad(e12); mstGraph.addRoad(e23); mstGraph.addRoad(e13);

        DynamicArray<Location> mstLocations = mstGraph.getAllLocations();
        DynamicArray<RoadEdge> mstRoads = new DynamicArray<>();
        mstRoads.add(e12); mstRoads.add(e23); mstRoads.add(e13);

        DynamicArray<RoadEdge> kruskal = RouteEngine.kruskalMST(mstLocations, mstRoads);
        DynamicArray<RoadEdge> prim = RouteEngine.primMST(mstGraph);
        double kruskalCost = totalWeight(kruskal);
        double primCost = totalWeight(prim);
        ok("Kruskal MST picks the 2 cheapest edges out of 3 (skips the 3.0-weight edge)", kruskal.size() == 2 && kruskalCost == 3.0);
        ok("Prim MST total cost matches Kruskal MST total cost on the same graph", prim.size() == 2 && primCost == kruskalCost);
    }

    private static boolean containsAll(DynamicArray<Integer> arr, int... vals) {
        for (int v : vals) {
            boolean found = false;
            for (int x : arr) if (x == v) { found = true; break; }
            if (!found) return false;
        }
        return true;
    }

    private static DynamicArray<Integer> getNeighborIds(Graph g, int nodeId) {
        DynamicArray<Integer> ids = new DynamicArray<>();
        LinkedList<Graph.Edge> neighbors = g.getNeighbors(nodeId);
        if (neighbors != null) {
            for (Graph.Edge e : neighbors) ids.add(e.to);
        }
        return ids;
    }

    private static double totalWeight(DynamicArray<RoadEdge> edges) {
        double total = 0.0;
        for (RoadEdge e : edges) total += e.getWeight();
        return total;
    }

    // Small helper so `.contains` reads naturally above without importing java.util.List
    private static boolean contains(DynamicArray<Integer> arr, int val) {
        for (int x : arr) if (x == val) return true;
        return false;
    }

    // ── Sorting & Searching ───────────────────────────────────────────────
    private static void testSortingSearch() {
        section("Sorting & Searching");

        DynamicArray<Integer> nums = new DynamicArray<>();
        int[] raw = {64, 34, 25, 12, 22, 11, 90};
        for (int n : raw) nums.add(n);

        // [NORMAL]
        DynamicArray<Integer> sel = copy(nums);
        SortingEngine.selectionSort(sel, Integer::compareTo);
        ok("SelectionSort produces ascending order", SortingEngine.isSorted(sel, Integer::compareTo));
        DynamicArray<Integer> ins = copy(nums);
        SortingEngine.insertionSort(ins, Integer::compareTo);
        ok("InsertionSort produces ascending order", SortingEngine.isSorted(ins, Integer::compareTo));
        DynamicArray<Integer> mrg = copy(nums);
        SortingEngine.mergeSort(mrg, Integer::compareTo);
        ok("MergeSort produces ascending order", SortingEngine.isSorted(mrg, Integer::compareTo));
        DynamicArray<Integer> qck = copy(nums);
        SortingEngine.quickSort(qck, Integer::compareTo);
        ok("QuickSort produces ascending order", SortingEngine.isSorted(qck, Integer::compareTo));
        int idx = SortingEngine.linearSearch(copy(nums), 25, Integer::compareTo);
        ok("LinearSearch finds an existing value", idx != -1);
        DynamicArray<Integer> sorted = copy(nums);
        SortingEngine.quickSort(sorted, Integer::compareTo);
        ok("BinarySearch finds an existing value in sorted input", SortingEngine.binarySearch(sorted, 34, Integer::compareTo) != -1);

        // [BOUNDARY]
        DynamicArray<Integer> emptyArr = new DynamicArray<>();
        SortingEngine.quickSort(emptyArr, Integer::compareTo);
        boundary("sorting an empty array is a safe no-op", emptyArr.isEmpty());
        DynamicArray<Integer> singleArr = new DynamicArray<>();
        singleArr.add(1);
        SortingEngine.mergeSort(singleArr, Integer::compareTo);
        boundary("sorting a single-element array leaves it unchanged", singleArr.size() == 1 && singleArr.get(0) == 1);
        DynamicArray<Integer> dupArr = new DynamicArray<>();
        int[] dups = {5, 3, 5, 1, 5, 2};
        for (int d : dups) dupArr.add(d);
        SortingEngine.insertionSort(dupArr, Integer::compareTo);
        boundary("sorting an array with duplicate keys is stable-ordered (still ascending)", SortingEngine.isSorted(dupArr, Integer::compareTo));

        // [INVALID]
        int notFound = SortingEngine.linearSearch(copy(nums), 999, Integer::compareTo);
        invalid("LinearSearch returns -1 for a value that does not exist", notFound == -1);
        int emptySearch = SortingEngine.linearSearch(new DynamicArray<Integer>(), 1, Integer::compareTo);
        invalid("LinearSearch on an empty array returns -1", emptySearch == -1);
        DynamicArray<Integer> unsorted = copy(nums); // still in original, unsorted order
        invalid("BinarySearch throws when its sorted-input precondition is violated (counterexample)",
                throwsException(() -> SortingEngine.binarySearch(unsorted, 25, Integer::compareTo), IllegalStateException.class));
        // Section 10 requires >= 2 counterexamples: one invalid precondition (this one) + one greedy failure
        // (see testOptimisationEngine -> greedyNearestNeighbor suboptimality below).
        counterexample("Invalid-precondition counterexample: BinarySearch on unsorted input is rejected, not silently wrong",
                throwsException(() -> SortingEngine.binarySearch(unsorted, 25, Integer::compareTo), IllegalStateException.class));
    }

    // ── Models & lifecycle ───────────────────────────────────────────────
    private static void testModelsAndLifecycle() {
        section("Models & Order/Rider Lifecycle");

        // [NORMAL]
        Location loc1 = new Location(1, "Balme Library", "Academic", "LIBRARY", 5.6500, -0.1870);
        Location loc2 = new Location(2, "Night Market", "Commercial", "MARKET", 5.6550, -0.1820);
        ok("Haversine distance between two real campus points is plausible (0.4-1.0 km)",
                loc1.distanceTo(loc2) > 0.4 && loc1.distanceTo(loc2) < 1.0);

        Order order = new Order(101, "Kofi", "JCS", "Jollof", 0.5, 1, 2, 120.0, "CREATED", -1);
        order.setStatus(Order.OrderState.SCHEDULED);
        order.setAssignedRiderId(5);
        ok("order transitions CREATED -> SCHEDULED and gets a rider assigned", "SCHEDULED".equals(order.getStatus()) && order.getAssignedRiderId() == 5);

        Resource rider = new Resource(5, "Kwame", "BICYCLE", 1, 15.0, "AVAILABLE");
        rider.assignOrder(101);
        ok("assigning an order flips rider to BUSY", rider.getCurrentOrderId() == 101 && "BUSY".equals(rider.getAvailabilityStatus()));
        rider.completeOrder(2);
        ok("completing a delivery frees the rider and updates location/tally",
                rider.getCurrentLocationId() == 2 && rider.isAvailable() && rider.getCompletedDeliveries() == 1);

        ServiceRequest req = new ServiceRequest(201, 1, 2, "Food", 4, 120.0, 150.0, "PENDING", -1);
        Order converted = req.toOrder(102);
        ok("ServiceRequest -> Order conversion preserves pickup location", converted.getOrderId() == 102 && converted.getPickupLocationId() == 1);

        // [BOUNDARY]
        boundary("Haversine distance between identical coordinates is exactly 0.0",
                Location.haversineDistance(5.65, -0.187, 5.65, -0.187) == 0.0);
        Resource freshRider = new Resource(6, "Ama", "MOTORCYCLE", 1, 20.0, "AVAILABLE");
        boundary("brand-new rider starts with 0 completed deliveries", freshRider.getCompletedDeliveries() == 0);

        // [INVALID]
        invalid("distanceTo(null) does not throw, returns 0.0 by contract", loc1.distanceTo(null) == 0.0);
    }

    // ── OptimisationEngine (greedy + DP, Section 5 M8 / Section 7) ─────────
    private static void testOptimisationEngine() {
        section("OptimisationEngine (Greedy Dispatch, Greedy Rider Pick, DP Knapsack Batching)");

        // Small campus graph reused across this section's cases.
        Graph g = new Graph(5);
        g.addLocation(new Location(1, "Dispatch Point", "Central", "HUB", 5.65, -0.187));
        g.addLocation(new Location(2, "Hall A", "North", "HOSTEL", 5.66, -0.187));
        g.addLocation(new Location(3, "Hall B", "South", "HOSTEL", 5.64, -0.187));
        g.addLocation(new Location(4, "Library", "Central", "ACADEMIC", 5.651, -0.186));
        g.addRoad(new RoadEdge(1, 1, 2, "Dispatch", "HallA", 1.0, 5, "LOW", "GOOD", 1.0, false, 1.0));
        g.addRoad(new RoadEdge(2, 1, 3, "Dispatch", "HallB", 3.0, 15, "LOW", "GOOD", 1.0, false, 3.0));
        g.addRoad(new RoadEdge(3, 2, 4, "HallA", "Library", 1.0, 5, "LOW", "GOOD", 1.0, false, 1.0));

        DynamicArray<ServiceRequest> reqs = new DynamicArray<>();
        reqs.add(new ServiceRequest(1, 1, 2, "Food", 3, 100, 200, "PENDING", -1));
        reqs.add(new ServiceRequest(2, 1, 3, "Food", 3, 100, 200, "PENDING", -1));

        // [NORMAL]
        DynamicArray<ServiceRequest> order = OptimisationEngine.greedyNearestNeighbor(g, 1, reqs);
        ok("greedyNearestNeighbor visits the closer destination (Hall A, dist 1.0) before the farther one (Hall B, dist 3.0)",
                order.size() == 2 && order.get(0).getDestLocationId() == 2 && order.get(1).getDestLocationId() == 3);

        DynamicArray<Resource> riders = new DynamicArray<>();
        Resource far = new Resource(1, "Ama", "BICYCLE", 3, 10.0, "AVAILABLE");   // at Hall B, dist 3.0 from pickup
        Resource near = new Resource(2, "Kofi", "BICYCLE", 2, 10.0, "AVAILABLE"); // at Hall A, dist 1.0 from pickup
        riders.add(far); riders.add(near);
        Resource picked = OptimisationEngine.greedyFastestAvailableRider(g, 1, riders, 2.0);
        ok("greedyFastestAvailableRider picks the closer/faster rider (Kofi at Hall A) over the farther one",
                picked != null && picked.getResourceId() == 2);

        // [BOUNDARY]
        boundary("greedyNearestNeighbor on empty request list returns an empty (not null) result",
                OptimisationEngine.greedyNearestNeighbor(g, 1, new DynamicArray<>()).isEmpty());
        Resource tooHeavy = new Resource(3, "Yaw", "BICYCLE", 2, 1.0, "AVAILABLE"); // capacity below requirement
        DynamicArray<Resource> onlyHeavyReq = new DynamicArray<>();
        onlyHeavyReq.add(tooHeavy);
        boundary("greedyFastestAvailableRider rejects a rider whose capacity is below the requirement",
                OptimisationEngine.greedyFastestAvailableRider(g, 1, onlyHeavyReq, 5.0) == null);

        // [INVALID]
        invalid("greedyNearestNeighbor with a null graph returns an empty result, not an exception",
                OptimisationEngine.greedyNearestNeighbor(null, 1, reqs).isEmpty());
        invalid("greedyNearestNeighbor with an out-of-range dispatch id returns an empty result",
                OptimisationEngine.greedyNearestNeighbor(g, 999, reqs).isEmpty());
        invalid("dpKnapsackBatching with capacityKg <= 0 returns an empty result",
                OptimisationEngine.dpKnapsackBatching(reqs, 0.0).isEmpty());

        // [NORMAL] DP knapsack: pick the higher-priority item when both can't fit.
        // "Documents" -> weight 0.3kg, "Groceries" -> weight 5.0kg (see getWeightByCategory).
        DynamicArray<ServiceRequest> batch = new DynamicArray<>();
        ServiceRequest cheapUrgent = new ServiceRequest(10, 1, 2, "Documents", 5, 100, 110, "PENDING", -1); // light, high priority
        ServiceRequest heavyLowPriority = new ServiceRequest(11, 1, 3, "Groceries", 1, 100, 500, "PENDING", -1); // heavy, low priority
        batch.add(cheapUrgent); batch.add(heavyLowPriority);
        DynamicArray<ServiceRequest> selected = OptimisationEngine.dpKnapsackBatching(batch, 1.0); // capacity too small for both
        ok("dpKnapsackBatching selects the request that maximizes total priority under the weight cap",
                selected.size() == 1 && selected.get(0).getRequestId() == 10);

        // [COUNTEREXAMPLE] Section 7: "include a counterexample where greedy fails".
        // Classic nearest-neighbor trap: three destinations on a line where always
        // stepping to the closest unvisited point is NOT the shortest overall tour.
        Graph line = new Graph(3);
        line.addLocation(new Location(0, "Start", "Z", "HUB", 0, 0));
        line.addLocation(new Location(1, "P1", "Z", "T", 0, 0)); // position +1
        line.addLocation(new Location(2, "P2", "Z", "T", 0, 0)); // position -1
        line.addLocation(new Location(3, "P3", "Z", "T", 0, 0)); // position +2
        // Fully connect with weight/distance = |position difference| (a consistent metric,
        // so Dijkstra's shortest path between any two points is always the direct edge).
        line = new Graph(3);
        line.addLocation(new Location(0, "Start", "Z", "HUB", 0, 0));
        line.addLocation(new Location(1, "P1", "Z", "T", 0, 0));
        line.addLocation(new Location(2, "P2", "Z", "T", 0, 0));
        line.addLocation(new Location(3, "P3", "Z", "T", 0, 0));
        line.addRoad(new RoadEdge(1, 0, 1, "Start", "P1", 1, 2, "LOW", "GOOD", 1.0, false, 1)); // |0-1|=1
        line.addRoad(new RoadEdge(2, 0, 2, "Start", "P2", 1, 2, "LOW", "GOOD", 1.0, false, 1)); // |0-(-1)|=1
        line.addRoad(new RoadEdge(3, 0, 3, "Start", "P3", 2, 4, "LOW", "GOOD", 1.0, false, 2)); // |0-2|=2
        line.addRoad(new RoadEdge(4, 1, 2, "P1", "P2", 2, 4, "LOW", "GOOD", 1.0, false, 2));    // |1-(-1)|=2
        line.addRoad(new RoadEdge(5, 1, 3, "P1", "P3", 1, 2, "LOW", "GOOD", 1.0, false, 1));    // |1-2|=1
        line.addRoad(new RoadEdge(6, 2, 3, "P2", "P3", 3, 6, "LOW", "GOOD", 1.0, false, 3));    // |-1-2|=3

        DynamicArray<ServiceRequest> lineReqs = new DynamicArray<>();
        lineReqs.add(new ServiceRequest(20, 0, 1, "Food", 3, 100, 200, "PENDING", -1)); // dest P1
        lineReqs.add(new ServiceRequest(21, 0, 2, "Food", 3, 100, 200, "PENDING", -1)); // dest P2
        lineReqs.add(new ServiceRequest(22, 0, 3, "Food", 3, 100, 200, "PENDING", -1)); // dest P3

        DynamicArray<ServiceRequest> greedyOrder = OptimisationEngine.greedyNearestNeighbor(line, 0, lineReqs);
        double greedyTotal = 0.0;
        int cursor = 0;
        for (ServiceRequest r : greedyOrder) {
            greedyTotal += RouteEngine.dijkstra(line, cursor, r.getDestLocationId()).totalDistanceKm;
            cursor = r.getDestLocationId();
        }
        // Optimal tour (computed independently via the same Dijkstra engine, visiting P2 -> P1 -> P3):
        double optimalTotal = RouteEngine.dijkstra(line, 0, 2).totalDistanceKm
                + RouteEngine.dijkstra(line, 2, 1).totalDistanceKm
                + RouteEngine.dijkstra(line, 1, 3).totalDistanceKm;
        System.out.printf("      greedy tour cost = %.1f, optimal tour cost = %.1f%n", greedyTotal, optimalTotal);
        counterexample("Greedy-failure counterexample: nearest-neighbor dispatch (" + greedyTotal +
                        ") is strictly worse than the optimal visiting order (" + optimalTotal + ")",
                greedyTotal > optimalTotal);
    }

    // ── SchedulingEngine (Section 5 M5: FIFO / priority / round-robin / stack) ──
    private static void testSchedulingEngine() {
        section("SchedulingEngine (FIFO, Priority, Round-Robin, Urgent Override)");

        DynamicArray<ServiceRequest> reqs = new DynamicArray<>();
        reqs.add(new ServiceRequest(1, 1, 2, "Food", 2, 300, 400, "PENDING", -1));   // submitted latest
        reqs.add(new ServiceRequest(2, 1, 2, "Food", 2, 100, 400, "PENDING", -1));   // submitted earliest
        reqs.add(new ServiceRequest(3, 1, 2, "Food", 5, 200, 210, "PENDING", -1));   // urgent, mid-time
        reqs.add(new ServiceRequest(4, 1, 2, "Food", 1, 250, 260, "DELIVERED", -1)); // not pending -> excluded

        // [NORMAL]
        Queue<ServiceRequest> fifo = SchedulingEngine.dispatchFIFO(reqs);
        ok("dispatchFIFO orders by submission time regardless of insertion order (id2 @100 first)",
                fifo.dequeue().getRequestId() == 2 && fifo.dequeue().getRequestId() == 3 && fifo.dequeue().getRequestId() == 1);

        MinHeap<ServiceRequest> prio = SchedulingEngine.dispatchPriority(reqs);
        ok("dispatchPriority dispatches the highest-urgency request first (id3, urgency=5)",
                prio.extractMin().getRequestId() == 3);

        Stack<ServiceRequest> urgentStack = SchedulingEngine.dispatchUrgentOverride(reqs); // default threshold=4
        ok("dispatchUrgentOverride pops the urgent request (id3) before any normal request",
                urgentStack.pop().getRequestId() == 3);

        ds.HashTable<Integer, Location> locMap = new ds.HashTable<>();
        locMap.put(2, new Location(2, "Zone Stop", "Central", "STOP", 5.65, -0.187));
        DynamicArray<ServiceRequest> rr = SchedulingEngine.dispatchRoundRobin(reqs, locMap);
        ok("dispatchRoundRobin returns exactly the 3 PENDING requests (filters out DELIVERED)", rr.size() == 3);

        // [BOUNDARY]
        DynamicArray<ServiceRequest> onlyNormal = new DynamicArray<>();
        onlyNormal.add(new ServiceRequest(5, 1, 2, "Food", 1, 100, 400, "PENDING", -1));
        Stack<ServiceRequest> noUrgent = SchedulingEngine.dispatchUrgentOverride(onlyNormal);
        boundary("dispatchUrgentOverride with no urgent requests still returns all normal requests", noUrgent.size() == 1);
        Stack<ServiceRequest> customThreshold = SchedulingEngine.dispatchUrgentOverride(reqs, 5); // only urgency==5 counts
        boundary("dispatchUrgentOverride(threshold) overload changes what counts as urgent",
                customThreshold.pop().getRequestId() == 3);

        // [INVALID]
        DynamicArray<ServiceRequest> empty = new DynamicArray<>();
        invalid("dispatchFIFO on an empty request list returns an empty (not null) queue", SchedulingEngine.dispatchFIFO(empty).isEmpty());
        invalid("dispatchPriority on an empty request list returns an empty heap", SchedulingEngine.dispatchPriority(empty).isEmpty());
        DynamicArray<ServiceRequest> allDelivered = new DynamicArray<>();
        allDelivered.add(new ServiceRequest(6, 1, 2, "Food", 3, 100, 200, "DELIVERED", -1));
        invalid("dispatchFIFO excludes all non-PENDING requests, leaving an empty queue", SchedulingEngine.dispatchFIFO(allDelivered).isEmpty());
    }

    // ── DriverPool (rider registry + round-robin rotation) ─────────────────
    private static void testDriverPool() {
        section("DriverPool (Rider Registry, Availability, Nearest/Rotation Assignment)");

        DriverPool pool = new DriverPool();
        Resource r1 = new Resource(1, "Kofi", "BICYCLE", 1, 10.0, "AVAILABLE");   // home = Hall A
        Resource r2 = new Resource(2, "Ama", "MOTORCYCLE", 3, 20.0, "AVAILABLE"); // home = Hall B (not the target itself)
        pool.addDriver(r1);
        pool.addDriver(r2);

        // [NORMAL]
        ok("findDriver retrieves a rider by id after addDriver", pool.findDriver(2) == r2);
        ok("getAvailableDrivers lists both freshly-added AVAILABLE riders", pool.getAvailableDrivers().size() == 2);
        ok("getDriversByVehicleType('MOTORCYCLE') returns exactly Ama", pool.getDriversByVehicleType("MOTORCYCLE").size() == 1);
        pool.setDriverBusy(1);
        ok("setDriverBusy flips availability and removes the rider from getAvailableDrivers",
                !pool.findDriver(1).isAvailable() && pool.getAvailableDrivers().size() == 1);
        pool.setDriverAvailable(1);
        ok("setDriverAvailable flips the rider back to AVAILABLE", pool.findDriver(1).isAvailable());

        DynamicArray<Location> locs = new DynamicArray<>();
        locs.add(new Location(1, "Hall A", "North", "HOSTEL", 5.66, -0.187));
        locs.add(new Location(2, "Library", "Central", "ACADEMIC", 5.651, -0.186));
        locs.add(new Location(3, "Hall B", "South", "HOSTEL", 5.64, -0.187));
        DynamicArray<RoadEdge> roads = new DynamicArray<>();
        roads.add(new RoadEdge(1, 1, 2, "HallA", "Library", 1.0, 5, "LOW", "GOOD", 1.0, false, 1.0));
        roads.add(new RoadEdge(2, 3, 2, "HallB", "Library", 5.0, 25, "LOW", "GOOD", 1.0, false, 5.0));
        Resource nearest = pool.findNearestDriver(2, locs, roads);
        ok("findNearestDriver picks Kofi (dist 1.0 from Library) over Ama (dist 5.0)", nearest.getResourceId() == 1);

        // [BOUNDARY]
        pool.rebuild(new DynamicArray<>()); // wipe everything
        boundary("rebuild() with an empty rider list clears the pool entirely",
                pool.findDriver(1) == null && pool.getAvailableDrivers().isEmpty());
        DynamicArray<Resource> refill = new DynamicArray<>();
        refill.add(r1); refill.add(r2);
        pool.rebuild(refill);
        boundary("rebuild() repopulates the pool from a fresh rider list", pool.findDriver(1) == r1 && pool.findDriver(2) == r2);
        Resource removed = pool.removeDriver(1);
        boundary("removeDriver returns the removed rider and they disappear from lookup",
                removed == r1 && pool.findDriver(1) == null);

        // [INVALID]
        invalid("findDriver on a nonexistent id returns null", pool.findDriver(999) == null);
        invalid("removeDriver on a nonexistent id returns null", pool.removeDriver(999) == null);
        invalid("updateDriverLocation on a nonexistent id returns false", !pool.updateDriverLocation(999, 1));
        invalid("setDriverAvailable on a nonexistent id returns false", !pool.setDriverAvailable(999));
    }

    // ── IncomingOrderManager (priority-first intake queue) ──────────────────
    private static void testIncomingOrderManager() {
        section("IncomingOrderManager (Priority-first Intake)");
        // NOTE: submit() persists to the live SQLite DB as a side effect, so these
        // tests use requeue() (same queueing logic, no DB write) to stay isolated.
        IncomingOrderManager mgr = new IncomingOrderManager();

        ServiceRequest normal1 = new ServiceRequest(1, 1, 2, "Food", 2, 100, 400, "PENDING", -1);
        ServiceRequest normal2 = new ServiceRequest(2, 1, 2, "Food", 2, 100, 400, "PENDING", -1);
        ServiceRequest urgent = new ServiceRequest(3, 1, 2, "Food", 5, 100, 110, "PENDING", -1);

        // [NORMAL]
        mgr.requeue(normal1, false);
        mgr.requeue(urgent, true);
        mgr.requeue(normal2, false);
        ok("pendingCount reflects all 3 queued requests across both internal queues", mgr.pendingCount() == 3);
        ok("next() always drains the priority heap before the FIFO queue (urgent request first)",
                mgr.next().getRequestId() == 3);
        ok("next() then falls back to FIFO order for the remaining normal requests",
                mgr.next().getRequestId() == 1 && mgr.next().getRequestId() == 2);

        // [BOUNDARY]
        IncomingOrderManager sizes = new IncomingOrderManager();
        sizes.requeue(normal1, false);
        sizes.requeue(urgent, true);
        boundary("fifoSize/prioritySize correctly attribute each request to its own internal structure",
                sizes.fifoSize() == 1 && sizes.prioritySize() == 1);
        boundary("hasPending is true as long as either internal structure is non-empty", sizes.hasPending());

        // [INVALID]
        IncomingOrderManager empty = new IncomingOrderManager();
        invalid("next() on an empty manager returns null instead of throwing", empty.next() == null);
        invalid("hasPending() is false on a freshly constructed manager", !empty.hasPending());
        invalid("pendingCount() is 0 on a freshly constructed manager", empty.pendingCount() == 0);
    }

    // ── IndexingEngine (Section 5 M6: BST/RBT/BTree/Hash indexes) ──────────
    private static void testIndexingEngine() {
        section("IndexingEngine (Multi-structure Indexes over Requests/Orders/Riders)");

        IndexingEngine idx = new IndexingEngine();
        DynamicArray<ServiceRequest> reqs = new DynamicArray<>();
        reqs.add(new ServiceRequest(1, 1, 2, "Food", 3, 100, 200, "PENDING", -1));
        reqs.add(new ServiceRequest(2, 1, 3, "Documents", 2, 100, 200, "PENDING", -1));
        idx.indexRequests(reqs);

        // [NORMAL] all four index structures must agree on the same underlying record.
        ok("searchBST finds request 1", idx.searchBST(1) != null && idx.searchBST(1).getRequestId() == 1);
        ok("searchHashTable finds request 1", idx.searchHashTable(1) != null && idx.searchHashTable(1).getRequestId() == 1);
        ok("searchRBT finds request 1", idx.searchRBT(1) != null && idx.searchRBT(1).getRequestId() == 1);
        ok("searchBTree finds request 1", idx.searchBTree(1) != null && idx.searchBTree(1).getRequestId() == 1);

        DynamicArray<Order> orders = new DynamicArray<>();
        orders.add(new Order(101, "Kofi", "Night Market Grill", "Jollof", 1.0, 1, 2, 100, "CREATED", -1));
        orders.add(new Order(102, "Ama", "Night Market Grill", "Waakye", 1.2, 1, 3, 105, "CREATED", -1));
        DynamicArray<Resource> riders = new DynamicArray<>();
        riders.add(new Resource(1, "Yaw", "BICYCLE", 1, 10.0, "AVAILABLE"));
        idx.indexDeliveryData(orders, riders);

        ok("searchOrderBST finds order 101", idx.searchOrderBST(101) != null);
        ok("searchOrderBTree finds order 101", idx.searchOrderBTree(101) != null);
        ok("searchRiderRBT finds rider 1", idx.searchRiderRBT(1) != null);
        ok("searchRiderHash finds rider 1", idx.searchRiderHash(1) != null);
        ok("searchByRestaurant('Night Market Grill') returns both matching orders", idx.searchByRestaurant("Night Market Grill").size() == 2);
        ok("searchByRestaurant is case-insensitive (normalizeKey lowercases)", idx.searchByRestaurant("night market grill").size() == 2);

        // [BOUNDARY]
        boundary("getBSTHeight is positive after indexing 2 requests", idx.getBSTHeight() > 0);
        boundary("getHashTableSize matches the number of indexed requests", idx.getHashTableSize() == 2);
        idx.indexRequests(new DynamicArray<>());
        boundary("re-indexing with an empty list resets the index: request 1 is no longer found", idx.searchBST(1) == null);

        // [INVALID]
        invalid("searchBST for a negative id returns null (explicit guard, no exception)", idx.searchBST(-1) == null);
        invalid("searchHashTable for a nonexistent id returns null", idx.searchHashTable(999) == null);
        invalid("searchByRestaurant for an unknown name returns an empty (not null) list",
                idx.searchByRestaurant("Nonexistent Vendor").isEmpty());
    }

    // ── DeliveryEngine (rider scoring + duration estimate) ─────────────────
    private static void testDeliveryEngine() {
        section("DeliveryEngine (Score-based Rider Assignment, Duration Estimate)");

        DynamicArray<Location> locs = new DynamicArray<>();
        locs.add(new Location(1, "Vendor", "Central", "MARKET", 5.65, -0.187));
        locs.add(new Location(2, "Rider Spot", "Central", "STOP", 5.651, -0.186));
        DynamicArray<RoadEdge> roads = new DynamicArray<>();
        roads.add(new RoadEdge(1, 2, 1, "RiderSpot", "Vendor", 3.0, 15, "LOW", "GOOD", 1.0, false, 3.0));

        DynamicArray<Resource> riders = new DynamicArray<>();
        Resource bike = new Resource(1, "Yaw", "BICYCLE", 2, 10.0, "AVAILABLE");
        Resource moto = new Resource(2, "Ama", "MOTORCYCLE", 2, 10.0, "AVAILABLE");
        riders.add(bike); riders.add(moto);

        Order heavyOrder = new Order(1, "Kofi", "Vendor", "Groceries", 2.0, 1, 3, 100, "CREATED", -1); // 2.0kg, long trip (3.0km)

        // [NORMAL] scoreRider penalizes bicycles on long/heavy trips -> motorcycle should win.
        DeliveryEngine.AssignmentResult result = DeliveryEngine.assignRider(heavyOrder, riders, locs, roads);
        ok("assignRider picks the motorcycle over the bicycle for a heavy, long-distance order",
                result != null && result.rider.getResourceId() == 2);

        double motoTime = DeliveryEngine.estimateDeliveryDuration(heavyOrder, 3.0, moto);
        double bikeTime = DeliveryEngine.estimateDeliveryDuration(heavyOrder, 3.0, bike);
        ok("estimateDeliveryDuration gives the motorcycle a shorter ETA than the bicycle for the same distance",
                motoTime < bikeTime);

        // [BOUNDARY]
        double floorTime = DeliveryEngine.estimateDeliveryDuration(heavyOrder, 0.0, moto);
        boundary("estimateDeliveryDuration applies an 8-minute floor even for a 0km trip", floorTime == Math.round((8.0 * 0.7) * 10.0) / 10.0);
        Resource exactCapacity = new Resource(3, "Kwesi", "MOTORCYCLE", 2, 2.0, "AVAILABLE"); // capacity == order weight exactly
        DynamicArray<Resource> exactOnly = new DynamicArray<>();
        exactOnly.add(exactCapacity);
        DeliveryEngine.AssignmentResult exactResult = DeliveryEngine.assignRider(heavyOrder, exactOnly, locs, roads);
        boundary("a rider whose capacity exactly equals the order weight is still eligible", exactResult != null);

        // [INVALID]
        DynamicArray<Resource> allBusy = new DynamicArray<>();
        allBusy.add(new Resource(4, "Busy Rider", "MOTORCYCLE", 2, 10.0, "BUSY"));
        invalid("assignRider returns null when every candidate rider is unavailable",
                DeliveryEngine.assignRider(heavyOrder, allBusy, locs, roads) == null);
        Order zeroWeight = new Order(2, "Kofi", "Vendor", "Note", 0.0, 1, 2, 100, "CREATED", -1);
        invalid("assignRider returns null for a non-positive order weight",
                DeliveryEngine.assignRider(zeroWeight, riders, locs, roads) == null);
        invalid("assignRider returns null when given a null order", DeliveryEngine.assignRider(null, riders, locs, roads) == null);
    }

    // ── AuditLog (Progress.md Section 31 — audit event trail) ──────────────
    //
    // These tests deliberately exercise AuditLog.format() only, never record().
    // format() is a pure function, so the row layout can be verified without
    // opening a database connection. That keeps the suite runnable offline and
    // stops test runs from writing junk rows into ug_swift.db.
    private static void testAuditLog() {
        section("AuditLog (audit event row formatting)");

        // A row must begin with its event type, so the trail can be filtered by
        // type with a simple prefix match.
        ok("format() starts the row with the event type name",
                AuditLog.format(AuditEventType.ORDER_CREATED, "orderId=1").startsWith("ORDER_CREATED"));

        // Every emitted type should round-trip its own name.
        boolean allTypesNamed = true;
        for (AuditEventType type : AuditEventType.values()) {
            if (!AuditLog.format(type, "x").startsWith(type.name())) {
                allTypesNamed = false;
                break;
            }
        }
        ok("format() preserves the name of every declared event type", allTypesNamed);

        // Details are appended after the pipe separator.
        ok("format() joins details after a ' | ' separator",
                AuditLog.format(AuditEventType.ORDER_ASSIGNED, "orderId=7")
                        .equals("ORDER_ASSIGNED | orderId=7"));

        // A row is one line. A newline in a value would render as two separate
        // events in the UI list, so it must be flattened at format time.
        String multiline = AuditLog.format(AuditEventType.ROUTE_CALCULATED, "from=A\nto=B\r\nvia=C");
        boundary("format() flattens newlines so one event is always one row",
                multiline.indexOf('\n') < 0 && multiline.indexOf('\r') < 0);

        // Missing or blank details produce the bare type, with no dangling separator.
        boundary("format() with null details returns the bare type name",
                AuditLog.format(AuditEventType.ORDER_DELIVERED, null).equals("ORDER_DELIVERED"));
        boundary("format() with blank details leaves no trailing separator",
                AuditLog.format(AuditEventType.ORDER_DELIVERED, "   ").equals("ORDER_DELIVERED"));

        // Auditing must never be the thing that breaks a delivery, so a bad call
        // degrades to a placeholder row instead of throwing.
        boolean threw = false;
        String nullTypeRow = null;
        try {
            nullTypeRow = AuditLog.format(null, "orderId=9");
        } catch (Exception ex) {
            threw = true;
        }
        invalid("format() with a null event type does not throw", !threw);
        invalid("format() with a null event type still yields a usable row",
                nullTypeRow != null && nullTypeRow.startsWith("UNKNOWN_EVENT"));

        // Convenience wrappers guard their own nulls, since they are called from
        // UI code paths where a rider or order may legitimately be absent.
        boolean wrappersSafe = true;
        try {
            AuditLog.orderCreated(null);
            AuditLog.orderAssigned(null, null, 0.0, 0.0);
            AuditLog.orderDelivered(null, null);
        } catch (Exception ex) {
            wrappersSafe = false;
        }
        invalid("convenience wrappers ignore null models instead of throwing", wrappersSafe);
    }

    // ── Trace tables (Section 10: "at least six trace tables") ─────────────
    // These are printed, not asserted — they are the human-readable evidence
    // the report requires. Each one is cross-checked against the real engine
    // call at the end so the printed trace is provably what the code actually
    // does, not just a hand-written illustration.
    private static void printTraceTables() {
        System.out.println("\n\n======================================================");
        System.out.println("  TRACE TABLES (Section 10 evidence)");
        System.out.println("======================================================");
        traceBinarySearch();
        traceInsertionSort();
        traceMergeSort();
        traceDijkstra();
        traceKruskal();
        traceDPKnapsack();
    }

    private static void traceBinarySearch() {
        System.out.println("\n--- Trace 1: Binary Search (target = 23) ---");
        int[] arr = {2, 5, 8, 12, 16, 23, 38, 45, 56, 72};
        System.out.println("  array = " + java.util.Arrays.toString(arr));
        System.out.println("  step | low | high | mid | arr[mid] | action");
        int low = 0, high = arr.length - 1, step = 1, foundAt = -1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            String action;
            if (arr[mid] == 23) { action = "found"; foundAt = mid; }
            else if (arr[mid] < 23) { action = "search right (low=mid+1)"; }
            else { action = "search left (high=mid-1)"; }
            System.out.printf("  %4d | %3d | %4d | %3d | %8d | %s%n", step, low, high, mid, arr[mid], action);
            if (arr[mid] == 23) break;
            else if (arr[mid] < 23) low = mid + 1;
            else high = mid - 1;
            step++;
        }
        DynamicArray<Integer> da = new DynamicArray<>();
        for (int v : arr) da.add(v);
        int engineResult = SortingEngine.binarySearch(da, 23, Integer::compareTo);
        System.out.println("  Engine SortingEngine.binarySearch(...) returned index " + engineResult +
                (engineResult == foundAt ? "  [matches trace]" : "  [MISMATCH]"));
    }

    private static void traceInsertionSort() {
        System.out.println("\n--- Trace 2: Insertion Sort ---");
        int[] arr = {29, 10, 14, 37, 13};
        System.out.println("  initial = " + java.util.Arrays.toString(arr));
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
            System.out.printf("  pass %d (insert %2d) -> %s%n", i, key, java.util.Arrays.toString(arr));
        }
        DynamicArray<Integer> da = new DynamicArray<>();
        for (int v : new int[]{29, 10, 14, 37, 13}) da.add(v);
        SortingEngine.insertionSort(da, Integer::compareTo);
        System.out.println("  Engine SortingEngine.insertionSort(...) result: " + toStr(da) +
                (toStr(da).equals(java.util.Arrays.toString(arr)) ? "  [matches trace]" : "  [MISMATCH]"));
    }

    private static void traceMergeSort() {
        System.out.println("\n--- Trace 3: Merge Sort (divide/conquer/merge) ---");
        int[] arr = {38, 27, 43, 3, 9, 82, 10};
        System.out.println("  initial = " + java.util.Arrays.toString(arr));
        int[] result = mergeSortTrace(arr, 0, "  ");
        DynamicArray<Integer> da = new DynamicArray<>();
        for (int v : new int[]{38, 27, 43, 3, 9, 82, 10}) da.add(v);
        SortingEngine.mergeSort(da, Integer::compareTo);
        System.out.println("  Engine SortingEngine.mergeSort(...) result: " + toStr(da) +
                (toStr(da).equals(java.util.Arrays.toString(result)) ? "  [matches trace]" : "  [MISMATCH]"));
    }

    private static int[] mergeSortTrace(int[] arr, int depth, String indent) {
        if (arr.length <= 1) return arr;
        int mid = arr.length / 2;
        int[] left = java.util.Arrays.copyOfRange(arr, 0, mid);
        int[] right = java.util.Arrays.copyOfRange(arr, mid, arr.length);
        System.out.println(indent + "split -> " + java.util.Arrays.toString(left) + " | " + java.util.Arrays.toString(right));
        int[] sortedLeft = mergeSortTrace(left, depth + 1, indent + "  ");
        int[] sortedRight = mergeSortTrace(right, depth + 1, indent + "  ");
        int[] merged = new int[arr.length];
        int i = 0, j = 0, k = 0;
        while (i < sortedLeft.length && j < sortedRight.length) merged[k++] = (sortedLeft[i] <= sortedRight[j]) ? sortedLeft[i++] : sortedRight[j++];
        while (i < sortedLeft.length) merged[k++] = sortedLeft[i++];
        while (j < sortedRight.length) merged[k++] = sortedRight[j++];
        System.out.println(indent + "merge  -> " + java.util.Arrays.toString(merged));
        return merged;
    }

    private static void traceDijkstra() {
        System.out.println("\n--- Trace 4: Dijkstra's Shortest Path (node 1 -> node 4) ---");
        Graph g = new Graph(4);
        g.addLocation(new Location(1, "A", "Z", "T", 0, 0));
        g.addLocation(new Location(2, "B", "Z", "T", 0, 0));
        g.addLocation(new Location(3, "C", "Z", "T", 0, 0));
        g.addLocation(new Location(4, "D", "Z", "T", 0, 0));
        g.addRoad(new RoadEdge(1, 1, 2, "A", "B", 4, 4, "LOW", "GOOD", 1.0, false, 4));
        g.addRoad(new RoadEdge(2, 1, 3, "A", "C", 1, 1, "LOW", "GOOD", 1.0, false, 1));
        g.addRoad(new RoadEdge(3, 3, 2, "C", "B", 1, 1, "LOW", "GOOD", 1.0, false, 1));
        g.addRoad(new RoadEdge(4, 2, 4, "B", "D", 2, 2, "LOW", "GOOD", 1.0, false, 2));
        g.addRoad(new RoadEdge(5, 3, 4, "C", "D", 7, 7, "LOW", "GOOD", 1.0, false, 7));
        System.out.println("  edges: A-B=4, A-C=1, C-B=1, B-D=2, C-D=7");
        System.out.println("  step | settle node | dist so far");
        System.out.println("   1   |     A       | A=0");
        System.out.println("   2   |     C       | A=0, C=1 (via A)");
        System.out.println("   3   |     B       | B=2 (via A->C->B, better than direct A-B=4)");
        System.out.println("   4   |     D       | D=4 (via A->C->B->D)");
        RouteEngine.PathResult res = RouteEngine.dijkstra(g, 1, 4);
        System.out.println("  Engine RouteEngine.dijkstra(...) -> total weight=" + res.totalWeight + ", path=" + toStr(res.path) +
                (res.totalWeight == 4.0 ? "  [matches trace]" : "  [MISMATCH]"));
    }

    private static void traceKruskal() {
        System.out.println("\n--- Trace 5: Kruskal's MST (edges sorted, union-find accept/reject) ---");
        DynamicArray<Location> locs = new DynamicArray<>();
        locs.add(new Location(1, "A", "Z", "T", 0, 0));
        locs.add(new Location(2, "B", "Z", "T", 0, 0));
        locs.add(new Location(3, "C", "Z", "T", 0, 0));
        DynamicArray<RoadEdge> roads = new DynamicArray<>();
        roads.add(new RoadEdge(1, 1, 2, "A", "B", 1, 1, "LOW", "GOOD", 1.0, false, 1));
        roads.add(new RoadEdge(2, 2, 3, "B", "C", 2, 2, "LOW", "GOOD", 1.0, false, 2));
        roads.add(new RoadEdge(3, 1, 3, "A", "C", 3, 3, "LOW", "GOOD", 1.0, false, 3));
        System.out.println("  edges sorted by weight: A-B(1), B-C(2), A-C(3)");
        System.out.println("  edge  | find(u)!=find(v)? | decision");
        System.out.println("  A-B(1)|  0 != 1 (yes)      | ACCEPT, union(A,B)");
        System.out.println("  B-C(2)|  0 != 2 (yes)      | ACCEPT, union(B,C)");
        System.out.println("  A-C(3)|  0 == 0 (no, cycle) | REJECT");
        DynamicArray<RoadEdge> mst = RouteEngine.kruskalMST(locs, roads);
        double total = 0;
        for (RoadEdge e : mst) total += e.getWeight();
        System.out.println("  Engine RouteEngine.kruskalMST(...) -> " + mst.size() + " edges, total weight=" + total +
                (mst.size() == 2 && total == 3.0 ? "  [matches trace]" : "  [MISMATCH]"));
    }

    private static void traceDPKnapsack() {
        System.out.println("\n--- Trace 6: 0/1 Knapsack DP Table (request batching) ---");
        System.out.println("  items (weight in hectograms, priority value): Documents(w=3,v=10), Groceries(w=50,v=4)");
        System.out.println("  capacity W = 10 hectograms (1.0 kg)");
        int[] w = {3, 50};
        double[] v = {10, 4};
        int W = 10;
        double[][] dp = new double[3][W + 1];
        for (int i = 1; i <= 2; i++) {
            for (int j = 0; j <= W; j++) {
                dp[i][j] = (w[i - 1] <= j) ? Math.max(v[i - 1] + dp[i - 1][j - w[i - 1]], dp[i - 1][j]) : dp[i - 1][j];
            }
        }
        System.out.println("  dp[items][capacity] table (rows=items considered, cols=capacity 0.." + W + "):");
        for (int i = 0; i <= 2; i++) System.out.println("    row " + i + ": " + java.util.Arrays.toString(dp[i]));
        System.out.println("  Best achievable value at dp[2][" + W + "] = " + dp[2][W] + " -> only Documents fits, Groceries doesn't (w=50 > W=10)");

        DynamicArray<ServiceRequest> reqs = new DynamicArray<>();
        reqs.add(new ServiceRequest(1, 1, 2, "Documents", 5, 100, 110, "PENDING", -1));
        reqs.add(new ServiceRequest(2, 1, 2, "Groceries", 1, 100, 500, "PENDING", -1));
        DynamicArray<ServiceRequest> selected = OptimisationEngine.dpKnapsackBatching(reqs, 1.0);
        System.out.println("  Engine OptimisationEngine.dpKnapsackBatching(...) selected: " + toStr(selected) +
                (selected.size() == 1 && selected.get(0).getRequestId() == 1 ? "  [matches trace]" : "  [MISMATCH]"));
    }

    private static <T> String toStr(DynamicArray<T> arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arr.get(i));
        }
        return sb.append("]").toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private static void ok(String name, boolean condition) {
        record("NORMAL", name, condition);
        normalCount++;
    }

    private static void boundary(String name, boolean condition) {
        record("BOUNDARY", name, condition);
        boundaryCount++;
    }

    private static void invalid(String name, boolean condition) {
        record("INVALID", name, condition);
        invalidCount++;
    }

    /**
     * Records a Section 7 / Section 10 "counterexample" requirement: a case
     * that proves an algorithm's known limitation empirically (e.g. a greedy
     * strategy producing a worse-than-optimal result) rather than just
     * checking that the code runs without crashing.
     */
    private static void counterexample(String name, boolean condition) {
        record("COUNTEREXAMPLE", name, condition);
        counterexampleCount++;
    }

    private static void record(String tag, String name, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS][%s] %s%n", tag, name);
            passed++;
        } else {
            System.out.printf("  [FAIL][%s] %s%n", tag, name);
            failed++;
        }
    }

    private static void section(String name) {
        System.out.printf("%n-- %s --%n", name);
    }

    private static DynamicArray<Integer> copy(DynamicArray<Integer> src) {
        DynamicArray<Integer> copy = new DynamicArray<>();
        for (int v : src) copy.add(v);
        return copy;
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws Exception;
    }

    /** Returns true only if action throws an exception assignable to expectedType. */
    private static boolean throwsException(ThrowingAction action, Class<? extends Exception> expectedType) {
        try {
            action.run();
            return false; // nothing was thrown -> precondition/invalid-input check failed
        } catch (Exception e) {
            return expectedType.isInstance(e);
        }
    }
}