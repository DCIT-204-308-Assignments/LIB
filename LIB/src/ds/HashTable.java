package ds;

public class HashTable<K, V> {
    public static class Entry<K, V> {
        public K key;
        public V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof Entry) {
                @SuppressWarnings("unchecked")
                Entry<K, V> other = (Entry<K, V>) obj;
                return key.equals(other.key);
            }
            return false;
        }
    }

    private LinkedList<Entry<K, V>>[] buckets;
    private int size;
    private int capacity;
    private int collisionCount;

    @SuppressWarnings("unchecked")
    public HashTable(int capacity) {
        this.capacity = capacity;
        this.buckets = (LinkedList<Entry<K, V>>[]) new LinkedList[capacity];
        this.size = 0;
        this.collisionCount = 0;
    }

    public HashTable() {
        this(211); // Student index derived prime size
    }

    private int getBucketIndex(K key) {
        int hashCode = key.hashCode();
        int index = hashCode % capacity;
        if (index < 0) {
            index = index + capacity;
        }
        return index;
    }

    public int size() {
        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCollisionCount() {
        return collisionCount;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        if ((double) size / capacity >= 0.75) {
            resize();
        }

        int bucketIdx = getBucketIndex(key);
        if (buckets[bucketIdx] == null) {
            buckets[bucketIdx] = new LinkedList<>();
        } else if (!buckets[bucketIdx].isEmpty()) {
            // Collision occurred!
            collisionCount++;
        }

        LinkedList<Entry<K, V>> bucket = buckets[bucketIdx];
        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                entry.value = value; // update
                return;
            }
        }

        bucket.addLast(new Entry<>(key, value));
        size++;
    }

    public V get(K key) {
        if (key == null) return null;
        int bucketIdx = getBucketIndex(key);
        LinkedList<Entry<K, V>> bucket = buckets[bucketIdx];
        if (bucket == null) return null;
        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                return entry.value;
            }
        }
        return null;
    }

    public V remove(K key) {
        if (key == null) return null;
        int bucketIdx = getBucketIndex(key);
        LinkedList<Entry<K, V>> bucket = buckets[bucketIdx];
        if (bucket == null) return null;
        
        LinkedList.Node<Entry<K, V>> curr = bucket.getHeadNode();
        while (curr != null) {
            if (curr.data.key.equals(key)) {
                V val = curr.data.value;
                bucket.removeNode(curr);
                size--;
                return val;
            }
            curr = curr.next;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        int oldCap = capacity;
        capacity = getNextPrime(oldCap * 2);
        LinkedList<Entry<K, V>>[] oldBuckets = buckets;
        buckets = (LinkedList<Entry<K, V>>[]) new LinkedList[capacity];
        size = 0;
        collisionCount = 0;

        for (int i = 0; i < oldCap; i++) {
            if (oldBuckets[i] != null) {
                for (Entry<K, V> entry : oldBuckets[i]) {
                    put(entry.key, entry.value);
                }
            }
        }
    }

    private int getNextPrime(int n) {
        if (n % 2 == 0) n++;
        while (!isPrime(n)) {
            n += 2;
        }
        return n;
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        this.buckets = (LinkedList<Entry<K, V>>[]) new LinkedList[capacity];
        this.size = 0;
        this.collisionCount = 0;
    }

    public DynamicArray<Entry<K, V>> entries() {
        DynamicArray<Entry<K, V>> list = new DynamicArray<>();
        for (int i = 0; i < capacity; i++) {
            if (buckets[i] != null) {
                for (Entry<K, V> entry : buckets[i]) {
                    list.add(entry);
                }
            }
        }
        return list;
    }

    public DynamicArray<K> keys() {
        DynamicArray<K> list = new DynamicArray<>();
        for (int i = 0; i < capacity; i++) {
            if (buckets[i] != null) {
                for (Entry<K, V> entry : buckets[i]) {
                    list.add(entry.key);
                }
            }
        }
        return list;
    }

    public DynamicArray<V> values() {
        DynamicArray<V> list = new DynamicArray<>();
        for (int i = 0; i < capacity; i++) {
            if (buckets[i] != null) {
                for (Entry<K, V> entry : buckets[i]) {
                    list.add(entry.value);
                }
            }
        }
        return list;
    }
}
