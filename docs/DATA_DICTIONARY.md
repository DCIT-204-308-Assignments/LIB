# UG Smart Food Delivery & Dispatch Optimizer — Data Dictionary

**Group 1 — Database Architecture, Data Management & Core Data Structures**

This is the single canonical data dictionary for the project. It matches
`db/schema.sql` exactly, column for column, and uses the same camelCase
convention already used throughout the existing Java codebase
(`Location.java`, `Order.java`, `DatabaseManager.java`, etc.). Any DAO,
loader, or repository class should use these exact column names.

> Superseded documents: an earlier snake_case draft of this dictionary and
> ERD (`location_id`, `order_id`, etc.) is deprecated. Do not build against
> it — it does not match the running codebase.

---

## 1. LOCATIONS
Physical campus nodes used by the graph routing engines (halls, departments, vendors, shuttle stops).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `locationId` | INTEGER | PRIMARY KEY | Unique campus node ID. |
| `name` | TEXT | NOT NULL | Location name (e.g. Balme Library, Night Market). |
| `zone` | TEXT | NOT NULL | Broad area (e.g. Residential Area, Academic District). |
| `type` | TEXT | NOT NULL | Specific type (e.g. Hostel, Academic, Vendor, ShuttleStop). |
| `latitude` | REAL | NOT NULL | Geographic latitude. |
| `longitude` | REAL | NOT NULL | Geographic longitude. |

## 2. ROADS
Graph edges connecting locations — used by `Graph`/`RouteEngine`.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `roadId` | INTEGER | PRIMARY KEY | Unique edge ID. |
| `fromLocationId` | INTEGER | FK → locations.locationId | Edge start node. |
| `toLocationId` | INTEGER | FK → locations.locationId | Edge end node. |
| `distanceKm` | REAL | NOT NULL | Distance in kilometers. |
| `travelTimeMin` | REAL | NOT NULL | Estimated travel time in minutes. |
| `trafficLevel` | TEXT | NOT NULL | Traffic condition category. |
| `roadCondition` | TEXT | NOT NULL | Physical road condition. |
| `roadConditionWeight` | REAL | NOT NULL | Weighting factor for road condition. |
| `isOneWay` | BOOLEAN | NOT NULL | Whether the edge is directional. |
| `weight` | REAL | NOT NULL | Final graph weight used for pathfinding (Dijkstra/Prim/Kruskal). |

## 3. RESOURCES
Delivery riders (couriers).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `resourceId` | INTEGER | PRIMARY KEY | Unique rider ID. |
| `name` | TEXT | NOT NULL | Rider full name. |
| `type` | TEXT | NOT NULL | `BICYCLE` or `MOTORCYCLE` — drives the 6km eligibility rule. |
| `homeLocationId` | INTEGER | FK → locations.locationId | Rider's base/starting location. |
| `currentLocationId` | INTEGER | FK → locations.locationId, NULLABLE | Live position, updated as deliveries progress. |
| `capacityKg` | REAL | NOT NULL | Maximum load capacity. |
| `availabilityStatus` | TEXT | NOT NULL | `AVAILABLE` \| `BUSY` \| `OFFLINE`. |
| `completedDeliveries` | INTEGER | NOT NULL, DEFAULT 0 | Running count of completed deliveries. |
| `rating` | REAL | NULLABLE | Rider rating, used by SortingEngine. |

## 4. RESTAURANTS
Registered food vendors.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `restaurantId` | INTEGER | PRIMARY KEY | Unique restaurant ID. |
| `name` | TEXT | NOT NULL | Restaurant/vendor name. |
| `locationId` | INTEGER | FK → locations.locationId | Physical location on campus. |
| `category` | TEXT | NULLABLE | Cuisine type (Local, Fast Food, Continental, etc.). |
| `contactNumber` | TEXT | NULLABLE | Vendor contact number. |
| `avgPrepTimeMin` | REAL | NOT NULL, DEFAULT 15 | Average food preparation time, used in delivery-time estimates. |
| `popularityScore` | REAL | NOT NULL, DEFAULT 0 | Used by SortingEngine (sort by restaurant popularity). |
| `isOpen` | BOOLEAN | NOT NULL, DEFAULT 1 | Operational status. |

## 5. FOOD_ITEMS
Menu items per restaurant.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `foodItemId` | INTEGER | PRIMARY KEY | Unique menu item ID. |
| `restaurantId` | INTEGER | FK → restaurants.restaurantId | Owning restaurant. |
| `name` | TEXT | NOT NULL | Dish/item name. |
| `price` | REAL | NOT NULL | Price in GHS. |
| `category` | TEXT | NULLABLE | Main, Snack, Drink, etc. |
| `available` | BOOLEAN | NOT NULL, DEFAULT 1 | Whether currently orderable. |

## 6. CUSTOMERS
Students/staff placing orders.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `customerId` | INTEGER | PRIMARY KEY | Unique customer ID. |
| `name` | TEXT | NOT NULL | Full name. |
| `email` | TEXT | UNIQUE, NULLABLE | Institutional email (e.g. `name@st.ug.edu.gh`). |
| `phone` | TEXT | NULLABLE | Contact number. |
| `locationId` | INTEGER | FK → locations.locationId | Default delivery location (hall/hostel). |
| `createdAt` | TEXT | DEFAULT CURRENT_TIMESTAMP | Account creation time. |

## 7. SERVICE_REQUESTS
Raw incoming request before conversion into an operational `Order` — required by the project spec's intake flow (`ServiceRequest → IncomingOrderManager → Order`).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `requestId` | INTEGER | PRIMARY KEY | Unique request ID. |
| `sourceLocationId` | INTEGER | FK → locations.locationId | Where the request originates. |
| `destLocationId` | INTEGER | FK → locations.locationId | Requested destination. |
| `category` | TEXT | NOT NULL | Request category. |
| `urgency` | INTEGER | NOT NULL | Urgency level (used by priority dispatch/stack override). |
| `timeSubmittedMin` | REAL | NOT NULL | Submission time. |
| `deadlineMin` | REAL | NOT NULL | Requested deadline. |
| `status` | TEXT | NOT NULL | Request-level status (pre-Order). |
| `assignedRiderId` | INTEGER | FK → resources.resourceId, NULLABLE | Rider assigned, if any, at request stage. |
| `deliveredTimeMin` | REAL | NULLABLE | Actual delivered time, if applicable. |

## 8. ORDERS
The core operational order record — created from a `ServiceRequest` once accepted.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `orderId` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique order ID. |
| `requestId` | INTEGER | FK → service_requests.requestId, NULLABLE | Originating request, if any. |
| `customerId` | INTEGER | FK → customers.customerId | Customer who placed the order. |
| `restaurantId` | INTEGER | FK → restaurants.restaurantId | Restaurant fulfilling the order. |
| `pickupLocationId` | INTEGER | FK → locations.locationId | Pickup point (normally = restaurant's location). |
| `deliveryLocationId` | INTEGER | FK → locations.locationId | Drop-off point (normally = customer's location). |
| `creationTime` | TEXT | NOT NULL | Order creation timestamp. |
| `requestedDeliveryTime` | TEXT | NULLABLE | Requested delivery time. |
| `priority` | INTEGER | NOT NULL, DEFAULT 1 | Priority level — must factor into `OptimisationEngine.scoreRider`. |
| `status` | TEXT | NOT NULL, DEFAULT 'CREATED' | One of: `CREATED`, `QUEUED`, `SCHEDULED`, `ASSIGNED`, `PICKED_UP`, `IN_TRANSIT`, `COMPLETED`, `CANCELLED`. |
| `distanceKm` | REAL | NULLABLE | Pickup→delivery distance. |
| `estimatedDeliveryMin` | REAL | NULLABLE | Estimated delivery time. |
| `totalPrice` | REAL | NULLABLE | Total order cost in GHS. |
| `vehicleType` | TEXT | NULLABLE | `BICYCLE` or `MOTORCYCLE`, set by the 6km eligibility rule. |
| `assignedResourceId` | INTEGER | FK → resources.resourceId, NULLABLE | Assigned rider. |

## 9. ORDER_ITEMS
Junction table — line items within an order (many-to-many between orders and food items).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `orderItemId` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique line item ID. |
| `orderId` | INTEGER | FK → orders.orderId | Parent order. |
| `foodItemId` | INTEGER | FK → food_items.foodItemId | Selected menu item. |
| `quantity` | INTEGER | NOT NULL, DEFAULT 1 | Quantity ordered. |
| `unitPrice` | REAL | NOT NULL | Price per unit at time of purchase. |

## 10. DELIVERY_ASSIGNMENTS
Rider-to-order assignment and delivery lifecycle timing — kept separate from `orders` so timestamps don't clutter the order record.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `assignmentId` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique assignment ID. |
| `orderId` | INTEGER | FK → orders.orderId | Order being delivered. |
| `resourceId` | INTEGER | FK → resources.resourceId | Rider assigned. |
| `assignedTime` | TEXT | NOT NULL | Assignment timestamp. |
| `pickedUpTime` | TEXT | NULLABLE | Pickup timestamp. |
| `deliveredTime` | TEXT | NULLABLE | Delivery completion timestamp. |
| `assignmentScore` | REAL | NULLABLE | Score produced by `OptimisationEngine` at assignment time. |
| `status` | TEXT | NOT NULL, DEFAULT 'ASSIGNED' | Assignment status. |

## 11. ALGORITHM_RUNS
Empirical performance evidence — required for algorithm benchmarking (Section 32 of the spec).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `runId` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique run ID. |
| `algorithmName` | TEXT | NOT NULL | e.g. Dijkstra, Merge Sort, Nearest Rider. |
| `inputSize` | INTEGER | NOT NULL | Size of input dataset for this run. |
| `timeNs` | INTEGER | NOT NULL | Execution time in nanoseconds. |
| `memoryKb` | INTEGER | NOT NULL | Memory used, in KB. |
| `dateRun` | TEXT | NOT NULL | Timestamp of the run. |
| `operationsCount` | INTEGER | NOT NULL, DEFAULT 0 | Number of operations performed. |
| `comparisonsCount` | INTEGER | NOT NULL, DEFAULT 0 | Number of comparisons performed. |
| `status` | TEXT | NOT NULL, DEFAULT 'SUCCESS' | Run outcome. |
| `resultSummary` | TEXT | NOT NULL, DEFAULT 'Completed' | Short description of result. |

## 12. AUDIT_EVENTS
System event trail — traceability for demos/testing.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `eventId` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique event ID. |
| `eventType` | TEXT | NULLABLE | e.g. `ORDER_CREATED`, `ORDER_ASSIGNED`, `RIDER_STATUS_CHANGED`. |
| `description` | TEXT | NOT NULL | Event detail. |
| `timestamp` | TEXT | NOT NULL | When the event occurred. |