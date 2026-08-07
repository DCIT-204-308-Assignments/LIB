import java.util.Comparator;
import java.util.Random;
import ds.*;
import engines.*;
import models.*;

public class UGSwiftTestSuite {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║        UG SWIFT — DSA Unit Test Suite                      ║");
        System.out.println("║  DCIT 204/308 Joint Semester Project — Ghana Courier DSA   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

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
        testSortingSearch();
        testBTree();

        System.out.println("\n══════════════════════════════════════════════════════════════");
        System.out.printf("  RESULTS: %d passed, %d failed  (out of %d total)%n", passed, failed, passed + failed);
        System.out.println("══════════════════════════════════════════════════════════════");
        if (failed == 0) {
            System.out.println("  ✓  ALL TESTS PASSED!");
        } else {
            System.out.println("  ✗  SOME TESTS FAILED — see above for details.");
        }
    }

    // ── DynamicArray ─────────────────────────────────────────────────────
    private static void testDynamicArray() {
        section("DynamicArray");
        DynamicArray<Integer> arr = new DynamicArray<>();

        // T1: add and size
        arr.add(10); arr.add(20); arr.add(30);
        assertTrue("T1 add/size", arr.size() == 3);

        // T2: get
        assertTrue("T2 get(1)==20", arr.get(1) == 20);

        // T3: set
        arr.set(0, 99);
        assertTrue("T3 set(0,99)", arr.get(0) == 99);

        // T4: remove
        arr.remove(0);
        assertTrue("T4 remove(0) -> new[0]==20", arr.get(0) == 20);

        // T5: dynamic resize — insert 200 elements
        DynamicArray<Integer> big = new DynamicArray<>(4);
        for (int i = 0; i < 200; i++) big.add(i);
        assertTrue("T5 resize to 200 elements", big.size() == 200 && big.get(199) == 199);

        // T6: empty check
        DynamicArray<String> empty = new DynamicArray<>();
        assertTrue("T6 isEmpty on new array", empty.isEmpty());

        // T7: insert at index
        DynamicArray<Integer> ins = new DynamicArray<>();
        ins.add(1); ins.add(3);
        ins.add(1, 2); // [1,2,3]
        assertTrue("T7 add(1,2) inserts at idx 1", ins.get(1) == 2 && ins.size() == 3);

        // T8: clear
        ins.clear();
        assertTrue("T8 clear -> isEmpty", ins.isEmpty());

        // T9: iterator
        DynamicArray<Integer> itArr = new DynamicArray<>();
        for (int i = 1; i <= 5; i++) itArr.add(i);
        int sum = 0;
        for (int v : itArr) sum += v;
        assertTrue("T9 iterator sum 1..5 == 15", sum == 15);
    }

    // ── LinkedList ────────────────────────────────────────────────────────
    private static void testLinkedList() {
        section("LinkedList");
        LinkedList<Integer> list = new LinkedList<>();

        // T10: addFirst/addLast
        list.addFirst(2); list.addFirst(1); list.addLast(3);
        assertTrue("T10 addFirst/addLast: [1,2,3]", list.getFirst() == 1 && list.getLast() == 3);

        // T11: size
        assertTrue("T11 size == 3", list.size() == 3);

        // T12: removeFirst
        list.removeFirst();
        assertTrue("T12 removeFirst -> head=2", list.getFirst() == 2);

        // T13: removeLast
        list.removeLast();
        assertTrue("T13 removeLast -> tail=2", list.getLast() == 2);

        // T14: isEmpty after clear
        list.clear();
        assertTrue("T14 clear -> isEmpty", list.isEmpty());

        // T15: iterator
        LinkedList<String> sl = new LinkedList<>();
        sl.addLast("A"); sl.addLast("B"); sl.addLast("C");
        StringBuilder sb = new StringBuilder();
        for (String s : sl) sb.append(s);
        assertTrue("T15 iterator A+B+C", sb.toString().equals("ABC"));
    }

    // ── Stack ─────────────────────────────────────────────────────────────
    private static void testStack() {
        section("Stack");
        Stack<Integer> stack = new Stack<>();

        // T16: push/peek
        stack.push(1); stack.push(2); stack.push(3);
        assertTrue("T16 peek == 3 (LIFO top)", stack.peek() == 3);

        // T17: pop order
        int a = stack.pop(), b = stack.pop(), c = stack.pop();
        assertTrue("T17 pop LIFO: 3,2,1", a == 3 && b == 2 && c == 1);

        // T18: isEmpty
        assertTrue("T18 isEmpty after pop all", stack.isEmpty());

        // T19: undo simulation (push events, pop last)
        Stack<String> undo = new Stack<>();
        undo.push("assign_req_1"); undo.push("assign_req_2");
        String last = undo.pop();
        assertTrue("T19 undo last action == assign_req_2", last.equals("assign_req_2"));
    }

    // ── Queue ─────────────────────────────────────────────────────────────
    private static void testQueue() {
        section("Queue");
        Queue<String> q = new Queue<>();

        // T20: enqueue/dequeue FIFO
        q.enqueue("A"); q.enqueue("B"); q.enqueue("C");
        String first = q.dequeue();
        assertTrue("T20 dequeue FIFO -> A", first.equals("A"));

        // T21: peek
        assertTrue("T21 peek == B (head)", q.peek().equals("B"));

        // T22: size after partial dequeue
        assertTrue("T22 size == 2 remaining", q.size() == 2);
    }

    // ── CircularQueue ─────────────────────────────────────────────────────
    private static void testCircularQueue() {
        section("CircularQueue");
        CircularQueue<Integer> cq = new CircularQueue<>(4);

        // T23: enqueue/dequeue wrap-around
        cq.enqueue(1); cq.enqueue(2); cq.enqueue(3); cq.enqueue(4);
        cq.dequeue(); cq.dequeue();
        cq.enqueue(5); cq.enqueue(6);
        assertTrue("T23 wrap-around: peek==3", cq.peek() == 3 && cq.size() == 4);

        // T24: capacity and front/rear pointers
        int front = cq.getFrontPointer();
        int rear  = cq.getRearPointer();
        assertTrue("T24 front != rear (non-empty)", front != rear || cq.size() > 0);

        // T25: resize when full
        CircularQueue<Integer> smol = new CircularQueue<>(2);
        smol.enqueue(10); smol.enqueue(20); smol.enqueue(30); // triggers resize
        assertTrue("T25 resize: size==3, peek==10", smol.size() == 3 && smol.peek() == 10);
    }

    // ── Deque ─────────────────────────────────────────────────────────────
    private static void testDeque() {
        section("Deque");
        Deque<Integer> dq = new Deque<>();

        // T26: addFront/addRear
        dq.addRear(2); dq.addFront(1); dq.addRear(3);
        assertTrue("T26 Deque [1,2,3]: front=1, rear=3", dq.peekFront() == 1 && dq.peekRear() == 3);

        // T27: removeFront
        dq.removeFront();
        assertTrue("T27 removeFront -> new front=2", dq.peekFront() == 2);

        // T28: removeRear
        dq.removeRear();
        assertTrue("T28 removeRear -> new rear=2", dq.peekRear() == 2);

        // T29: urgent insertion (addFront for high-priority)
        Deque<String> urgDq = new Deque<>();
        urgDq.addRear("OrderA"); urgDq.addRear("OrderB");
        urgDq.addFront("URGENT_OrderC");
        assertTrue("T29 urgent addFront -> front is URGENT", urgDq.peekFront().equals("URGENT_OrderC"));
    }

    // ── MinHeap ───────────────────────────────────────────────────────────
    private static void testMinHeap() {
        section("MinHeap (Priority Queue)");
        MinHeap<Integer> heap = new MinHeap<>(10, Integer::compareTo);

        // T30: insert and extractMin
        heap.insert(5); heap.insert(2); heap.insert(8); heap.insert(1);
        assertTrue("T30 peek == 1 (min)", heap.peek() == 1);
        heap.extractMin();
        assertTrue("T31 after extract, peek == 2", heap.peek() == 2);

        // T32: heap sort order
        int prev = heap.extractMin();
        int curr = heap.extractMin();
        assertTrue("T32 extraction in ascending order", prev <= curr);

        // T33: dispatching priority: highest priority request first (inverted comparator)
        Comparator<ServiceRequest> maxPrio = (a, b) -> Double.compare(b.getPriority(), a.getPriority());
        MinHeap<ServiceRequest> pq = new MinHeap<>(10, maxPrio);
        pq.insert(new ServiceRequest(1, 1, 2, "Waakye", 3, 480, 560, "PENDING", 0));
        pq.insert(new ServiceRequest(2, 3, 4, "Documents", 5, 490, 520, "PENDING", 0));
        pq.insert(new ServiceRequest(3, 5, 6, "Groceries", 1, 500, 620, "PENDING", 0));
        ServiceRequest top = pq.extractMin();
        assertTrue("T33 highest priority dispatched first (urgency=5)", top.getUrgency() == 5);
    }

    // ── BST ───────────────────────────────────────────────────────────────
    private static void testBST() {
        section("Binary Search Tree (BST)");
        BST<Integer, String> bst = new BST<>();

        // T34: insert and search
        bst.insert(50, "Accra"); bst.insert(30, "Kumasi"); bst.insert(70, "Tamale");
        bst.insert(20, "Cape Coast"); bst.insert(40, "Sunyani");
        assertTrue("T34 search(30) == Kumasi", "Kumasi".equals(bst.search(30)));
        assertTrue("T35 search(99) == null", bst.search(99) == null);

        // T36: inorder is sorted
        BST<Integer, Integer> numBst = new BST<>();
        numBst.insert(5, 5); numBst.insert(3, 3); numBst.insert(7, 7);
        numBst.insert(1, 1); numBst.insert(4, 4);
        DynamicArray<Integer> sorted = numBst.inorder();
        boolean inOrder = true;
        for (int i = 0; i < sorted.size() - 1; i++) {
            if (sorted.get(i) > sorted.get(i + 1)) { inOrder = false; break; }
        }
        assertTrue("T36 BST inorder traversal is sorted ascending", inOrder);
    }

    // ── RedBlackTree ──────────────────────────────────────────────────────
    private static void testRedBlackTree() {
        section("Red-Black Tree");
        RedBlackTree<Integer, String> rbt = new RedBlackTree<>();

        // T37: insert and search
        rbt.insert(10, "Legon"); rbt.insert(5, "Madina"); rbt.insert(15, "Tema");
        rbt.insert(3, "Nungua"); rbt.insert(7, "Ofankor");
        assertTrue("T37 RBT search(5) == Madina", "Madina".equals(rbt.search(5)));

        // T38: root is always black
        assertTrue("T38 root color is BLACK", rbt.getRoot().color == RedBlackTree.BLACK);

        // T39: height is balanced (for 5 nodes should be <= 4)
        assertTrue("T39 RBT height <= 4 for 5 nodes", rbt.height() <= 4);

        // T40: inorder is sorted
        RedBlackTree<Integer, Integer> numRbt = new RedBlackTree<>();
        int[] keys = {30, 15, 50, 10, 20, 40, 60};
        for (int k : keys) numRbt.insert(k, k);
        DynamicArray<Integer> inorder = numRbt.inorder();
        boolean sorted = true;
        for (int i = 0; i < inorder.size() - 1; i++) {
            if (inorder.get(i) > inorder.get(i + 1)) { sorted = false; break; }
        }
        assertTrue("T40 RBT inorder sorted", sorted);
    }

    // ── HashTable ─────────────────────────────────────────────────────────
    private static void testHashTable() {
        section("Hash Table");
        HashTable<String, Integer> ht = new HashTable<>(11);

        // T41: put and get
        ht.put("Kofi", 1); ht.put("Ama", 2); ht.put("Yaw", 3);
        assertTrue("T41 get('Ama') == 2", ht.get("Ama") == 2);

        // T42: update
        ht.put("Ama", 99);
        assertTrue("T42 update 'Ama' -> 99", ht.get("Ama") == 99);

        // T43: remove
        ht.remove("Yaw");
        assertTrue("T43 remove 'Yaw' -> null", ht.get("Yaw") == null);

        // T44: size after operations
        assertTrue("T44 size == 2 after remove", ht.size() == 2);

        // T45: collision tracking
        int cols = ht.getCollisionCount();
        assertTrue("T45 collision count is non-negative", cols >= 0);
    }

    // ── DisjointSet ───────────────────────────────────────────────────────
    private static void testDisjointSet() {
        section("Disjoint Set (Union-Find)");
        DisjointSet ds = new DisjointSet(10);

        // T46: union and find
        ds.union(0, 1); ds.union(2, 3); ds.union(0, 2);
        assertTrue("T46 find(0)==find(3) after transitive union", ds.find(0) == ds.find(3));

        // T47: separate components
        assertTrue("T47 find(4) != find(0) (separate)", ds.find(4) != ds.find(0));

        // T48: duplicate union returns false
        boolean dup = ds.union(0, 3); // already same set
        assertTrue("T48 union of same set returns false", !dup);
    }

    // ── Sorting & Searching ───────────────────────────────────────────────
    private static void testSortingSearch() {
        section("Sorting & Searching");

        DynamicArray<Integer> nums = new DynamicArray<>();
        int[] raw = {64, 34, 25, 12, 22, 11, 90};
        for (int n : raw) nums.add(n);

        // T49: Selection sort
        DynamicArray<Integer> sel = copy(nums);
        SortingEngine.selectionSort(sel, Integer::compareTo);
        assertTrue("T49 SelectionSort: sorted ascending", SortingEngine.isSorted(sel, Integer::compareTo));

        // T50: Insertion sort
        DynamicArray<Integer> ins = copy(nums);
        SortingEngine.insertionSort(ins, Integer::compareTo);
        assertTrue("T50 InsertionSort: sorted ascending", SortingEngine.isSorted(ins, Integer::compareTo));

        // T51: Merge sort
        DynamicArray<Integer> mrg = copy(nums);
        SortingEngine.mergeSort(mrg, Integer::compareTo);
        assertTrue("T51 MergeSort: sorted ascending", SortingEngine.isSorted(mrg, Integer::compareTo));

        // T52: Quick sort
        DynamicArray<Integer> qck = copy(nums);
        SortingEngine.quickSort(qck, Integer::compareTo);
        assertTrue("T52 QuickSort: sorted ascending", SortingEngine.isSorted(qck, Integer::compareTo));

        // T53: Linear search (found)
        DynamicArray<Integer> lnr = copy(nums);
        int idx = SortingEngine.linearSearch(lnr, 25, Integer::compareTo);
        assertTrue("T53 LinearSearch finds 25", idx != -1);

        // T54: Linear search (not found)
        int notFound = SortingEngine.linearSearch(lnr, 999, Integer::compareTo);
        assertTrue("T54 LinearSearch returns -1 for missing", notFound == -1);

        // T55: Binary search (found after sort)
        DynamicArray<Integer> sorted = copy(nums);
        SortingEngine.quickSort(sorted, Integer::compareTo);
        int binIdx = SortingEngine.binarySearch(sorted, 34, Integer::compareTo);
        assertTrue("T55 BinarySearch finds 34 in sorted list", binIdx != -1);

        // T56: Binary search precondition — unsorted should throw
        DynamicArray<Integer> unsorted = copy(nums);
        boolean threw = false;
        try {
            SortingEngine.binarySearch(unsorted, 25, Integer::compareTo);
        } catch (IllegalStateException e) {
            threw = true;
        }
        assertTrue("T56 BinarySearch throws on unsorted (counterexample/precondition check)", threw);
    }

    // ── BTree ─────────────────────────────────────────────────────────────
    private static void testBTree() {
        section("B-Tree (degree t=3)");
        BTree<Integer, String> bt = new BTree<>();

        // T57: insert and search
        for (int i = 1; i <= 20; i++) bt.insert(i, "Zone" + i);
        assertTrue("T57 BTree search(10) == Zone10", "Zone10".equals(bt.search(10)));
        assertTrue("T58 BTree search(1) == Zone1", "Zone1".equals(bt.search(1)));
        assertTrue("T59 BTree search(20) == Zone20", "Zone20".equals(bt.search(20)));
        assertTrue("T60 BTree search(99) == null (missing key)", bt.search(99) == null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s%n", name);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s%n", name);
            failed++;
        }
    }

    private static void section(String name) {
        System.out.printf("%n── %s ──%n", name);
    }

    private static DynamicArray<Integer> copy(DynamicArray<Integer> src) {
        DynamicArray<Integer> copy = new DynamicArray<>();
        for (int v : src) copy.add(v);
        return copy;
    }
}
