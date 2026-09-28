public class CustomLRULinkedHashMap<K, V> {

    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;   // Collision chain pointer inside bucket

        Node<K, V> before; // Global DLL pointer (towards LRU)
        Node<K, V> after;  // Global DLL pointer (towards MRU)

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private static final int DEFAULT_BUCKET_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Node<K, V>[] buckets;
    private int bucketCapacity;
    private final int maxCapacity; // Upper limit for LRU eviction
    private int size;
    private final float loadFactor;

    // Sentinels: head.after points to LRU, tail.before points to MRU
    private final Node<K, V> head;
    private final Node<K, V> tail;

    @SuppressWarnings("unchecked")
    public CustomLRULinkedHashMap(int maxCapacity, int initialBucketCapacity, float loadFactor) {
        this.maxCapacity = maxCapacity;
        this.bucketCapacity = initialBucketCapacity;
        this.loadFactor = loadFactor;
        this.buckets = (Node<K, V>[]) new Node[bucketCapacity];
        this.size = 0;

        this.head = new Node<>(null, null);
        this.tail = new Node<>(null, null);
        this.head.after = this.tail;
        this.tail.before = this.head;
    }

    public CustomLRULinkedHashMap(int maxCapacity) {
        this(maxCapacity, DEFAULT_BUCKET_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    private int getBucketIndex(K key) {
        if (key == null) return 0;
        int hash = key.hashCode();
        hash = hash ^ (hash >>> 16);
        return (hash & 0x7FFFFFFF) % bucketCapacity;
    }

    // O(1) Lookup + LRU Order Update
    public V get(K key) {
        int index = getBucketIndex(key);
        Node<K, V> curr = buckets[index];

        while (curr != null) {
            if (keysEqual(curr.key, key)) {
                // Access-Order: Move accessed node to MRU (tail)
                moveToTail(curr);
                return curr.value;
            }
            curr = curr.next;
        }
        return null;
    }

    // O(1) Insertion / Update + Eviction
    public void put(K key, V value) {
        int index = getBucketIndex(key);
        Node<K, V> curr = buckets[index];

        // 1. Update value if key exists & move to MRU
        while (curr != null) {
            if (keysEqual(curr.key, key)) {
                curr.value = value;
                moveToTail(curr);
                return;
            }
            curr = curr.next;
        }

        // 2. Insert new node at bucket head
        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = buckets[index];
        buckets[index] = newNode;

        // 3. Append to tail as MRU
        linkAtEnd(newNode);
        size++;

        // 4. Handle Eviction vs Dynamic Resizing
        if (size > maxCapacity) {
            evictLRU();
        } else if ((float) size / bucketCapacity >= loadFactor) {
            resize();
        }
    }

    // O(1) Manual Removal
    public V remove(K key) {
        int index = getBucketIndex(key);
        Node<K, V> curr = buckets[index];
        Node<K, V> prev = null;

        while (curr != null) {
            if (keysEqual(curr.key, key)) {
                if (prev == null) {
                    buckets[index] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                unlinkNode(curr);
                size--;
                return curr.value;
            }
            prev = curr;
            curr = curr.next;
        }
        return null;
    }

    // Evicts head.after (Least Recently Used) in O(1)
    private void evictLRU() {
        Node<K, V> lruNode = head.after;
        if (lruNode == tail) return; // List is empty

        // 1. Unlink from bucket chain
        int index = getBucketIndex(lruNode.key);
        Node<K, V> curr = buckets[index];
        Node<K, V> prev = null;

        while (curr != null) {
            if (curr == lruNode) {
                if (prev == null) {
                    buckets[index] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                break;
            }
            prev = curr;
            curr = curr.next;
        }

        // 2. Unlink from global DLL
        unlinkNode(lruNode);
        size--;
    }

    private void moveToTail(Node<K, V> node) {
        unlinkNode(node);
        linkAtEnd(node);
    }

    private void linkAtEnd(Node<K, V> node) {
        Node<K, V> lastRealNode = tail.before;

        lastRealNode.after = node;
        node.before = lastRealNode;
        node.after = tail;
        tail.before = node;
    }

    private void unlinkNode(Node<K, V> node) {
        Node<K, V> prevNode = node.before;
        Node<K, V> nextNode = node.after;

        if (prevNode != null) prevNode.after = nextNode;
        if (nextNode != null) nextNode.before = prevNode;

        node.before = null;
        node.after = null;
    }

    private boolean keysEqual(K k1, K k2) {
        if (k1 == k2) return true;
        if (k1 == null || k2 == null) return false;
        return k1.equals(k2);
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        int newBucketCapacity = bucketCapacity * 2;
        Node<K, V>[] newBuckets = (Node<K, V>[]) new Node[newBucketCapacity];
        this.bucketCapacity = newBucketCapacity;

        Node<K, V> curr = head.after;
        while (curr != tail) {
            int newIndex = getBucketIndex(curr.key);
            curr.next = newBuckets[newIndex];
            newBuckets[newIndex] = curr;
            curr = curr.after;
        }
        this.buckets = newBuckets;
    }

    public int size() { return size; }
}