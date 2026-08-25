# Header 1 (Main Title - Use once per file)
## Header 2 (Major Sections like Approach, Diagrams)
### Header 3 (Sub-sections like Time Complexity)

**Bold text** for emphasis or keywords (e.g., **Time Complexity**).
*Italic text* for notes.
~~Strikethrough~~ to show deprecated thoughts.

- Bullet point item 1
- Bullet point item 2
    - Indented sub-item (use 2 spaces)

1. Numbered item 1
2. Numbered item 2

```java
public int twoSum(int[] nums, int target) {
    return new int[]{0, 1};
}
```

[Text to display](https://github.com)
![Image Alt Text](./diagrams/rate-limiter.png)

> "Design a system that can handle 10k requests per second."

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/v1/urls` | Create short URL |
| `GET` | `/:shortUrl` | Redirect to long URL |

# [Problem Title: e.g., Design a Scalable Rate Limiter]

## 1. Problem Statement
> Provide a brief 1-2 sentence overview of the problem or system goal here.
> Example: Design an API Rate Limiter to protect services from abuse, denial-of-service (DoS) attacks, and resource starvation.

### System Requirements
*   **Functional:**
    *   Limit requests per user identifier (e.g., API Key or IP address).
    *   Return a standard `429 Too Many Requests` HTTP error code when limits are exceeded.
*   **Non-Functional:**
    *   **Low Latency:** Must add minimal overhead to the request path ( 0) {
        tokens--;
        return true;
        }
        return false;
        }
        }
```

---

## 5. Complexity Analysis

### Time Complexity
*   **Token Lookup & Updates:** $\mathcal{O}(1)$ time complexity using Redis in-memory key-value lookups.
*   **Eviction Policy:** $\mathcal{O}(1)$ or $\mathcal{O}(\log N)$ depending on the sliding window algorithm choice.

### Space Complexity
*   **Memory Overhead:** $\mathcal{O}(U)$ where $U$ is the total number of active, tracked unique users in the active time window.

---

## 6. Personal Notes / Key Takeaways
*   **Trade-off:** Chose Redis Token Bucket over Leaky Bucket to allow burst traffic while safeguarding backend resources.
*   **Lessons Learned:** Learned how to handle race conditions in distributed environments using Redis Lua scripts.
