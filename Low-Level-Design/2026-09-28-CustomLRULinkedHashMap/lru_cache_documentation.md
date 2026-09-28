# Production-Grade LRU Cache & Custom LinkedHashMap (Java)

A zero-dependency, high-performance Java implementation of a Least Recently Used (LRU) Key-Value Cache built from foundational computer science primitives.

This repository demonstrates the construction of a custom hybrid data structure combining a Hash Table with Separate Chaining for $O(1)$ key lookups and a Sentinel-Anchored Doubly Linked List for $O(1)$ recency promotion and capacity-bounded eviction.

---

## 1. Architectural Design & Topology

Standard language collections abstract away internal pointer mechanics. This project explicitly implements a dual-graph topology over a single set of node instances in memory:

* **Hash Bucket Array (Collision Chain Topology):** Resolves hash collisions using a singly linked list (`next` pointer) within each array bucket index.
* **Global Doubly Linked List (Temporal Access Topology):** Tracks global access order from Least Recently Used (`head.after`) to Most Recently Used (`tail.before`) using dedicated `before` and `after` pointers.

```text
                       GLOBAL LRU ACCESS ORDER (Doubly Linked List)
     [Head Sentinel] <---> [Node A (LRU)] <---> [Node B] <---> [Node C (MRU)] <---> [Tail Sentinel]
                                 |                |                |
  BUCKETS ARRAY                  |                |                |
  +--------------+               |                |                |
  |  index [0]   |-------------->+                |                |
  +--------------+                                |                |
  |  index [1]   |------------------------------->+                |
  +--------------+                                                 |
  |  index [2]   |------------------------------------------------>+ ---> [Node X] ---> null
  +--------------+                                                        (Collision Chain)
```

---

## 2. Key Engineering Highlights

* **Strict $O(1)$ Amortized Operations:** Guarantees constant-time performance for `get()`, `put()`, `remove()`, and `evictLRU()`.
* **Sentinel Node Pattern:** Uses dummy `head` and `tail` boundary nodes to eliminate edge-case `null` checks during node unlinking and tail appending.
* **Dual Unlinking Mechanics:** On eviction, nodes are unlinked from both the global recency list and the underlying bucket collision chain simultaneously to prevent memory leaks and state drift.
* **Entropy Distribution & Sign Masking:** Implements bitwise XOR shifting (`hash ^ (hash >>> 16)`) and sign-bit masking (`hash & 0x7FFFFFFF`) to ensure uniform bucket distribution and prevent negative index bounds.
* **Dynamic Capacity Resizing:** Automatically doubles bucket capacity when $\frac{\text{size}}{\text{bucketCapacity}} \ge \text{loadFactor}$ ($0.75$), bounding collision chain depth.

---

## 3. Time & Space Complexity Analysis

| Operation | Average Time Complexity | Worst-Case Time Complexity | Space Complexity | Notes |
| :--- | :---: | :---: | :---: | :--- |
| `get(K key)` | $O(1)$ | $O(N)^*$ | $O(1)$ | Access promotes entry to MRU (`tail.before`). |
| `put(K key, V val)` | $O(1)$ | $O(N)^*$ | $O(1)$ | Triggers $O(1)$ LRU eviction if $\text{size} > \text{maxCapacity}$. |
| `remove(K key)` | $O(1)$ | $O(N)^*$ | $O(1)$ | Unlinks node from both array bucket and DLL. |
| `evictLRU()` | $O(1)$ | $O(1)$ | $O(1)$ | Always purges `head.after` in constant time. |

*\*Worst-case time complexity $O(N)$ occurs only under catastrophic hash collision scenarios where all keys map to a single bucket index. Dynamic array resizing with load factor $0.75$ keeps average bucket chain depth $< 1.33$.*

---

## 4. Why 3 Pointers per Node? (`next`, `before`, `after`)

A common mistake in custom map implementations is trying to reuse a single `next`/`prev` pair. This implementation explicitly decouples collision logic from ordering logic:

* **`next` Pointer:** Governs **Spatial Storage**. Connects nodes that happen to produce the same bucket index (`hashCode() % capacity`).
* **`before` & `after` Pointers:** Govern **Temporal History**. Connect nodes based on access timestamps across different buckets.

---

## 5. API Usage Example

```java
// Initialize an LRU Cache with Max Capacity = 3, Initial Bucket Capacity = 16, Load Factor = 0.75
CustomLRULinkedHashMap<String, Integer> lruCache = new CustomLRULinkedHashMap<>(3);

lruCache.put("A", 100); // Cache: [A]
lruCache.put("B", 200); // Cache: [A, B]
lruCache.put("C", 300); // Cache: [A, B, C]

// Accessing "A" promotes it to Most Recently Used (MRU)
lruCache.get("A");      // Cache order: [B, C, A]

// Inserting "D" exceeds Max Capacity (3), triggering O(1) eviction of LRU item ("B")
lruCache.put("D", 400); // Evicts "B". Cache order: [C, A, D]

System.out.println(lruCache.get("B")); // Returns null
System.out.println(lruCache.get("A")); // Returns 100
```

---

## 6. Production Roadmap (Concurrency & High Contention)

While this repository provides a single-threaded reference implementation, scaling this architecture for concurrent production environments requires the following enhancements:

### A. Lock Striping (Segmented Concurrency)
Replace coarse-grained class level synchronized blocks with Lock Striping (similar to `ConcurrentHashMap` v1.7):
* Divide the internal buckets array into $K$ segments, each protected by an independent `ReentrantReadWriteLock`.
* Allows concurrent writes across non-overlapping array indices.

### B. Read/Write Lock Separation for Read-Heavy Workloads
For scenarios where read operations outnumber writes ($90/10$ ratio):
* Utilize `ReentrantReadWriteLock` to allow parallel `get()` reads.
* *Note:* Because `get()` in an LRU cache modifies node pointers (promoting to MRU), read operations require write lock elevation unless recency updates are buffered asynchronously via thread-safe ring buffers (e.g., Caffeine Cache architecture).

### C. Atomic Eviction Buffering
Decouple cache hits from recency list mutations by logging reads to a lock-free `ConcurrentLinkedQueue`. A background maintenance thread drains the queue in batches to update `head` and `tail` pointers, removing lock contention on key lookups.

---

## 7. How to Run Tests

```bash
# Clone repository
git clone https://github.com/your-username/custom-lru-linkedhashmap.git
cd custom-lru-linkedhashmap

# Compile and run unit tests (JUnit 5)
javac -d bin src/*.java
java -cp bin CustomLRULinkedHashMapTest
```