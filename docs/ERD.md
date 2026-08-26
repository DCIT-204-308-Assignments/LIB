# Entity Relationship Diagram — UG Smart Food Delivery & Dispatch Optimizer

Group 1 — Database Architecture, Data Management & Core Data Structures

**Canonical version.** Matches `db/schema.sql` and `DATA_DICTIONARY.md` exactly (camelCase, matching the existing Java models). Supersedes any earlier snake_case ERD/dictionary draft — do not build against that one.

This diagram renders automatically on GitHub (any `.md` file with a ```mermaid
code block displays as a diagram in the file preview).

```mermaid
erDiagram
    LOCATIONS ||--o{ ROADS : "fromLocationId"
    LOCATIONS ||--o{ ROADS : "toLocationId"
    LOCATIONS ||--o{ RESOURCES : "homeLocationId"
    LOCATIONS ||--o{ RESOURCES : "currentLocationId"
    LOCATIONS ||--o{ RESTAURANTS : "locationId"
    LOCATIONS ||--o{ CUSTOMERS : "locationId"
    LOCATIONS ||--o{ SERVICE_REQUESTS : "sourceLocationId"
    LOCATIONS ||--o{ SERVICE_REQUESTS : "destLocationId"
    LOCATIONS ||--o{ ORDERS : "pickupLocationId"
    LOCATIONS ||--o{ ORDERS : "deliveryLocationId"

    RESTAURANTS ||--o{ FOOD_ITEMS : "restaurantId"
    RESTAURANTS ||--o{ ORDERS : "restaurantId"

    CUSTOMERS ||--o{ ORDERS : "customerId"

    SERVICE_REQUESTS |o--o| ORDERS : "requestId"
    SERVICE_REQUESTS }o--|| RESOURCES : "assignedRiderId"
    RESOURCES ||--o{ ORDERS : "assignedResourceId"

    ORDERS ||--o{ ORDER_ITEMS : "orderId"
    FOOD_ITEMS ||--o{ ORDER_ITEMS : "foodItemId"

    ORDERS ||--o{ DELIVERY_ASSIGNMENTS : "orderId"
    RESOURCES ||--o{ DELIVERY_ASSIGNMENTS : "resourceId"

    LOCATIONS {
        int locationId PK
        string name
        string zone
        string type
        real latitude
        real longitude
    }

    ROADS {
        int roadId PK
        int fromLocationId FK
        int toLocationId FK
        real distanceKm
        real travelTimeMin
        string trafficLevel
        string roadCondition
        real roadConditionWeight
        boolean isOneWay
        real weight
    }

    RESOURCES {
        int resourceId PK
        string name
        string type
        int homeLocationId FK
        int currentLocationId FK
        real capacityKg
        string availabilityStatus
        int completedDeliveries
        real rating
    }

    RESTAURANTS {
        int restaurantId PK
        string name
        int locationId FK
        string category
        string contactNumber
        real avgPrepTimeMin
        real popularityScore
        boolean isOpen
    }

    FOOD_ITEMS {
        int foodItemId PK
        int restaurantId FK
        string name
        real price
        string category
        boolean available
    }

    CUSTOMERS {
        int customerId PK
        string name
        string email
        string phone
        int locationId FK
        string createdAt
    }

    SERVICE_REQUESTS {
        int requestId PK
        int sourceLocationId FK
        int destLocationId FK
        string category
        int urgency
        real timeSubmittedMin
        real deadlineMin
        string status
        int assignedRiderId FK
        real deliveredTimeMin
    }

    ORDERS {
        int orderId PK
        int requestId FK
        int customerId FK
        int restaurantId FK
        int pickupLocationId FK
        int deliveryLocationId FK
        string creationTime
        string requestedDeliveryTime
        int priority
        string status
        real distanceKm
        real estimatedDeliveryMin
        real totalPrice
        string vehicleType
        int assignedResourceId FK
    }

    ORDER_ITEMS {
        int orderItemId PK
        int orderId FK
        int foodItemId FK
        int quantity
        real unitPrice
    }

    DELIVERY_ASSIGNMENTS {
        int assignmentId PK
        int orderId FK
        int resourceId FK
        string assignedTime
        string pickedUpTime
        string deliveredTime
        real assignmentScore
        string status
    }

    ALGORITHM_RUNS {
        int runId PK
        string algorithmName
        int inputSize
        int timeNs
        int memoryKb
        string dateRun
        int operationsCount
        int comparisonsCount
        string status
        string resultSummary
    }

    AUDIT_EVENTS {
        int eventId PK
        string eventType
        string description
        string timestamp
    }
```

## Notes on relationships

- **LOCATIONS** is the central hub — every physical point in the system (campus buildings, hostels, restaurants, customer addresses) is a `locationId` reference into this one table. This keeps distance/routing calculations consistent regardless of what kind of entity is at each point.
- **ROADS** forms the edges of the campus graph (`Graph` data structure in `src/ds`) — two foreign keys into `LOCATIONS` per edge.
- **SERVICE_REQUESTS → ORDERS** is a one-to-one-or-zero relationship: a request becomes an order once accepted, but not every request necessarily results in one (e.g. cancelled/invalid requests).
- **ORDERS → ORDER_ITEMS → FOOD_ITEMS** models the many-to-many between orders and menu items (an order can contain multiple food items, and a food item can appear in many orders) via the `order_items` junction table.
- **ORDERS → DELIVERY_ASSIGNMENTS → RESOURCES** separates *what was ordered* from *who is delivering it and when*, so the delivery lifecycle timestamps (assigned/picked up/delivered) don't clutter the `orders` table itself.
- **ALGORITHM_RUNS** and **AUDIT_EVENTS** are standalone log tables with no foreign keys — they record system behavior (performance benchmarks, event trail) rather than domain entities.
