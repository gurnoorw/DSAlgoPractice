# [Problem Title: e.g., Design a Movie Ticket Booking System]

## 1. Requirements & Scope

### Functional Requirements
*   **User Management:** Users can search for movies by title, genre, city, and release date.
*   **Booking Flow:** Users can select a cinema, a specific show, and choose available seats.
*   **Concurrency Handling:** A seat cannot be booked by two users simultaneously; it must be locked temporarily during payment.
*   **Payments:** System must support multiple payment modes (Credit Card, UPI, NetBanking).
*   **Notifications:** Send a booking confirmation via Email/SMS once payment succeeds.

### Non-Functional Requirements
*   **Thread Safety:** The core seat assignment logic must be completely thread-safe to avoid double bookings.
*   **Extensibility:** Adding a new payment gateway or notification channel should not require modifying existing core business logic.

---

## 2. Object-Oriented Design (Class Diagram)
This diagram illustrates the core domain entities, their relationships, and encapsulation boundaries.

![Class Diagram](./diagrams/class-diagram.png)

### Core Entities Breakdown
*   **Cinema/Hall:** Contains multiple `Show` objects. A `Show` maps a `Movie` to a specific time slot and a `Screen`.
*   **Seat:** Represents physical seats. Tracks states via a `SeatStatus` enum (`AVAILABLE`, `LOCKED`, `BOOKED`).
*   **Booking:** Encapsulates the transactional state of a user's selection, linking `User`, `Show`, `Seat` list, and `Payment`.

---

## 3. Design Patterns Applied

| Design Pattern | Purpose / Component Location | Why it was used |
| :--- | :--- | :--- |
| **Strategy Pattern** | `PaymentStrategy` interface | Decouples booking logic from specific payment providers (Stripe, Razorpay). |
| **Observer Pattern** | `NotificationService` | Automatically alerts the Email and SMS modules whenever a `BookingStatus` changes to `CONFIRMED`. |
| **State Pattern** | `Booking` state transitions | Manages complex states (`Created` -> `PendingPayment` -> `Confirmed` / `Expired`). |

---

## 4. Code Implementation (Core Logic)

### Thread-Safe Seat Booking Snippet
This section highlights how the system ensures thread-safe operations during concurrent bookings:

```java
package src.services;

import src.models.Seat;
import src.enums.SeatStatus;
import java.util.List;

public class BookingManager {
    
    // Uses intrinsic locking to prevent race conditions during seat assignment
    public synchronized boolean reserveSeats(List<Seat> selectedSeats) {
        for (Seat seat : selectedSeats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                return false; // Fail fast if any seat was taken midway
            }
        }
        
        // Temporarily lock seats for payment processing
        for (Seat seat : selectedSeats) {
            seat.setStatus(SeatStatus.LOCKED);
        }
        return true;
    }
}
```

### Strategy Pattern Implementation for Payments
```java
package src.strategies;

public interface PaymentStrategy {
    boolean processPayment(double amount);
}

// Concrete Strategy Example
public class UpiPaymentStrategy implements PaymentStrategy {
    @Override
    public boolean processPayment(double amount) {
        System.out.println("Processing UPI payment of ₹" + amount);
        return true; 
    }
}
```

---

## 5. Database Schema (Optional / Relational View)
Even in LLD, visualizing the data tables helps ground the class relationships.

```sql
CREATE TABLE Booking (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    show_id VARCHAR(50) NOT NULL,
    total_price DECIMAL(10,2),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 6. How to Run & Test
Provide clean instructions on how to run your local validations or drivers.

```bash
# Compile all source files
javac src/**/*.java -d out/

# Run the main driver file to simulate a complete booking flow
java -cp out src.MainDriver
```

---

## 7. Design Trade-offs & Future Scopes
*   **Trade-off:** Used synchronized methods for simplicity over finer-grained explicit locks (like `ReentrantLock`). In high-concurrency production setups, explicit locks or distributed Redis locks per `ShowID` would scale better.
*   **Future Enhancement:** Implement a cron scheduler to automatically release seats if the user's booking status stays stuck at `PENDING_PAYMENT` for more than 5 minutes.