-- =====================================================================
-- UG Smart Food Delivery & Dispatch Optimizer
-- Database Schema (SQLite)
-- Group 1 - Database Architecture, Data Management & Core Data Structures
-- =====================================================================
-- Naming convention: camelCase columns, singular table concepts,
-- matches existing tables already created in DatabaseManager.java
-- (locations, roads, resources, service_requests, algorithm_runs,
-- audit_events). New tables added below: restaurants, food_items,
-- customers, orders, delivery_assignments.
-- =====================================================================

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------
-- 1. LOCATIONS  (campus points: halls, departments, vendors, shuttle stops)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS locations (
    locationId   INTEGER PRIMARY KEY,
    name         TEXT NOT NULL,
    zone         TEXT NOT NULL,          -- e.g. 'Hostel', 'Academic', 'Vendor Area'
    type         TEXT NOT NULL,          -- e.g. 'Hall', 'Department', 'ShuttleStop', 'FoodVendor'
    latitude     REAL NOT NULL,
    longitude    REAL NOT NULL
);

-- ---------------------------------------------------------------------
-- 2. ROADS  (campus road network -> graph edges)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roads (
    roadId               INTEGER PRIMARY KEY,
    fromLocationId       INTEGER NOT NULL,
    toLocationId         INTEGER NOT NULL,
    fromName             TEXT NOT NULL,
    toName               TEXT NOT NULL,
    distanceKm           REAL NOT NULL,
    travelTimeMin        REAL NOT NULL,
    trafficLevel         TEXT NOT NULL,
    roadCondition        TEXT NOT NULL,
    roadConditionWeight  REAL NOT NULL,
    isOneWay             BOOLEAN NOT NULL,
    weight               REAL NOT NULL,
    FOREIGN KEY (fromLocationId) REFERENCES locations(locationId),
    FOREIGN KEY (toLocationId)   REFERENCES locations(locationId)
);

-- ---------------------------------------------------------------------
-- 3. RESOURCES  (delivery riders)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resources (
    resourceId          INTEGER PRIMARY KEY,
    name                TEXT NOT NULL,
    type                TEXT NOT NULL,      -- 'BICYCLE' | 'MOTORCYCLE'
    homeLocationId       INTEGER NOT NULL,
    currentLocationId    INTEGER,            -- live position, updates as deliveries progress
    capacityKg           REAL NOT NULL,
    availabilityStatus   TEXT NOT NULL,      -- 'AVAILABLE' | 'BUSY' | 'OFFLINE'
    completedDeliveries  INTEGER NOT NULL DEFAULT 0,
    rating                REAL,
    FOREIGN KEY (homeLocationId)    REFERENCES locations(locationId),
    FOREIGN KEY (currentLocationId) REFERENCES locations(locationId)
);

-- ---------------------------------------------------------------------
-- 4. RESTAURANTS  (food vendors on/around campus)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS restaurants (
    restaurantId   INTEGER PRIMARY KEY,
    name           TEXT NOT NULL,
    locationId     INTEGER NOT NULL,
    category       TEXT,                 -- e.g. 'Local', 'Fast Food', 'Continental'
    avgPrepTimeMin REAL NOT NULL DEFAULT 15,
    popularityScore REAL NOT NULL DEFAULT 0,   -- used by SortingEngine (popularity sort)
    isOpen          BOOLEAN NOT NULL DEFAULT 1,
    FOREIGN KEY (locationId) REFERENCES locations(locationId)
);

-- ---------------------------------------------------------------------
-- 5. FOOD_ITEMS  (menu items per restaurant)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS food_items (
    foodItemId    INTEGER PRIMARY KEY,
    restaurantId  INTEGER NOT NULL,
    name          TEXT NOT NULL,
    price         REAL NOT NULL,
    category      TEXT,                  -- e.g. 'Main', 'Drink', 'Snack'
    available     BOOLEAN NOT NULL DEFAULT 1,
    FOREIGN KEY (restaurantId) REFERENCES restaurants(restaurantId)
);

-- ---------------------------------------------------------------------
-- 6. CUSTOMERS
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customerId    INTEGER PRIMARY KEY,
    name          TEXT NOT NULL,
    phone         TEXT,
    locationId    INTEGER NOT NULL,       -- default delivery location (e.g. hostel room / hall)
    FOREIGN KEY (locationId) REFERENCES locations(locationId)
);

-- ---------------------------------------------------------------------
-- 7. SERVICE_REQUESTS  (raw incoming requests, pre-Order)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS service_requests (
    requestId          INTEGER PRIMARY KEY,
    sourceLocationId    INTEGER NOT NULL,
    destLocationId       INTEGER NOT NULL,
    category            TEXT NOT NULL,
    urgency              INTEGER NOT NULL,
    timeSubmittedMin      REAL NOT NULL,
    deadlineMin           REAL NOT NULL,
    status                TEXT NOT NULL,
    assignedRiderId       INTEGER,
    deliveredTimeMin      REAL,
    FOREIGN KEY (sourceLocationId) REFERENCES locations(locationId),
    FOREIGN KEY (destLocationId)   REFERENCES locations(locationId),
    FOREIGN KEY (assignedRiderId)  REFERENCES resources(resourceId)
);

-- ---------------------------------------------------------------------
-- 8. ORDERS  (the core missing entity - operational order record)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    orderId                INTEGER PRIMARY KEY AUTOINCREMENT,
    requestId              INTEGER,                 -- links back to originating service_request, if any
    customerId             INTEGER NOT NULL,
    restaurantId           INTEGER NOT NULL,
    pickupLocationId       INTEGER NOT NULL,         -- normally = restaurant's locationId
    deliveryLocationId     INTEGER NOT NULL,         -- normally = customer's locationId
    creationTime           TEXT NOT NULL,
    requestedDeliveryTime  TEXT,
    priority               INTEGER NOT NULL DEFAULT 1,
    status                 TEXT NOT NULL DEFAULT 'CREATED',
        -- one of: CREATED, QUEUED, SCHEDULED, ASSIGNED,
        --         PICKED_UP, IN_TRANSIT, COMPLETED, CANCELLED
    distanceKm             REAL,
    estimatedDeliveryMin   REAL,
    totalPrice             REAL,
    vehicleType            TEXT,                     -- 'BICYCLE' | 'MOTORCYCLE'
    assignedResourceId     INTEGER,
    FOREIGN KEY (requestId)          REFERENCES service_requests(requestId),
    FOREIGN KEY (customerId)         REFERENCES customers(customerId),
    FOREIGN KEY (restaurantId)       REFERENCES restaurants(restaurantId),
    FOREIGN KEY (pickupLocationId)   REFERENCES locations(locationId),
    FOREIGN KEY (deliveryLocationId) REFERENCES locations(locationId),
    FOREIGN KEY (assignedResourceId) REFERENCES resources(resourceId)
);

-- Order line items (which food items were ordered, what quantity)
CREATE TABLE IF NOT EXISTS order_items (
    orderItemId   INTEGER PRIMARY KEY AUTOINCREMENT,
    orderId       INTEGER NOT NULL,
    foodItemId    INTEGER NOT NULL,
    quantity      INTEGER NOT NULL DEFAULT 1,
    unitPrice     REAL NOT NULL,
    FOREIGN KEY (orderId)    REFERENCES orders(orderId),
    FOREIGN KEY (foodItemId) REFERENCES food_items(foodItemId)
);

-- ---------------------------------------------------------------------
-- 9. DELIVERY_ASSIGNMENTS  (rider <-> order assignment + lifecycle timing)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_assignments (
    assignmentId      INTEGER PRIMARY KEY AUTOINCREMENT,
    orderId           INTEGER NOT NULL,
    resourceId        INTEGER NOT NULL,
    assignedTime      TEXT NOT NULL,
    pickedUpTime      TEXT,
    deliveredTime     TEXT,
    assignmentScore   REAL,          -- score produced by OptimisationEngine at assignment time
    status            TEXT NOT NULL DEFAULT 'ASSIGNED',
    FOREIGN KEY (orderId)    REFERENCES orders(orderId),
    FOREIGN KEY (resourceId) REFERENCES resources(resourceId)
);

-- ---------------------------------------------------------------------
-- 10. ALGORITHM_RUNS  (empirical performance evidence)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS algorithm_runs (
    runId              INTEGER PRIMARY KEY AUTOINCREMENT,
    algorithmName      TEXT NOT NULL,
    inputSize          INTEGER NOT NULL,
    timeNs             INTEGER NOT NULL,
    memoryKb           INTEGER NOT NULL,
    dateRun            TEXT NOT NULL,
    operationsCount    INTEGER NOT NULL DEFAULT 0,
    comparisonsCount   INTEGER NOT NULL DEFAULT 0,
    status             TEXT NOT NULL DEFAULT 'SUCCESS',
    resultSummary      TEXT NOT NULL DEFAULT 'Completed'
);

-- ---------------------------------------------------------------------
-- 11. AUDIT_EVENTS  (system event trail)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_events (
    eventId       INTEGER PRIMARY KEY AUTOINCREMENT,
    eventType     TEXT,        -- e.g. ORDER_CREATED, ORDER_ASSIGNED, RIDER_STATUS_CHANGED
    description   TEXT NOT NULL,
    timestamp     TEXT NOT NULL
);

-- =====================================================================
-- INDEXES  (supporting fast lookup per assignment brief)
-- =====================================================================
CREATE INDEX IF NOT EXISTS idx_roads_from            ON roads(fromLocationId);
CREATE INDEX IF NOT EXISTS idx_roads_to              ON roads(toLocationId);
CREATE INDEX IF NOT EXISTS idx_resources_status       ON resources(availabilityStatus);
CREATE INDEX IF NOT EXISTS idx_food_items_restaurant  ON food_items(restaurantId);
CREATE INDEX IF NOT EXISTS idx_orders_customer        ON orders(customerId);
CREATE INDEX IF NOT EXISTS idx_orders_restaurant      ON orders(restaurantId);
CREATE INDEX IF NOT EXISTS idx_orders_status          ON orders(status);
CREATE INDEX IF NOT EXISTS idx_service_requests_status ON service_requests(status);
CREATE INDEX IF NOT EXISTS idx_delivery_assignments_order    ON delivery_assignments(orderId);
CREATE INDEX IF NOT EXISTS idx_delivery_assignments_resource ON delivery_assignments(resourceId);
CREATE INDEX IF NOT EXISTS idx_algorithm_runs_name    ON algorithm_runs(algorithmName);