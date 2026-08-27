-- =====================================================================
-- UG SWIFT: Smart Campus Logistics & Dispatch Optimizer
-- Database Schema Definition (SQLite 3)
-- Course: DCIT 204 / DCIT 308 - Joint Semester Project
-- Location: University of Ghana, Legon Campus
-- =====================================================================
-- Standardized SQLite DDL schema corresponding strictly to the live 
-- database manager engine (DatabaseManager.java) and operational models.
-- =====================================================================

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS locations (
    locationId   INTEGER PRIMARY KEY,
    name         TEXT NOT NULL,
    zone         TEXT NOT NULL,
    type         TEXT NOT NULL,
    latitude     REAL NOT NULL,
    longitude    REAL NOT NULL
);

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
    isOneWay             BOOLEAN NOT NULL DEFAULT 0,
    weight               REAL NOT NULL,
    FOREIGN KEY(fromLocationId) REFERENCES locations(locationId) ON DELETE CASCADE,
    FOREIGN KEY(toLocationId)   REFERENCES locations(locationId) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS resources (
    resourceId           INTEGER PRIMARY KEY,
    name                 TEXT NOT NULL,
    type                 TEXT NOT NULL,
    homeLocationId        INTEGER NOT NULL,
    capacityKg            REAL NOT NULL,
    availabilityStatus    TEXT NOT NULL DEFAULT 'AVAILABLE',
    currentLocationId     INTEGER NOT NULL DEFAULT -1,
    currentOrderId        INTEGER NOT NULL DEFAULT -1,
    completedDeliveries   INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY(homeLocationId)    REFERENCES locations(locationId),
    FOREIGN KEY(currentLocationId) REFERENCES locations(locationId)
);

CREATE TABLE IF NOT EXISTS service_requests (
    requestId            INTEGER PRIMARY KEY,
    sourceLocationId     INTEGER NOT NULL,
    destLocationId        INTEGER NOT NULL,
    category             TEXT NOT NULL,
    urgency              INTEGER NOT NULL,
    timeSubmittedMin     REAL NOT NULL,
    deadlineMin          REAL NOT NULL,
    status               TEXT NOT NULL DEFAULT 'PENDING',
    assignedRiderId      INTEGER,
    deliveredTimeMin     REAL,
    FOREIGN KEY(sourceLocationId) REFERENCES locations(locationId),
    FOREIGN KEY(destLocationId)   REFERENCES locations(locationId),
    FOREIGN KEY(assignedRiderId)  REFERENCES resources(resourceId)
);

CREATE TABLE IF NOT EXISTS orders (
    orderId                   INTEGER PRIMARY KEY,
    requestId                 INTEGER,
    customerName              TEXT NOT NULL,
    restaurant                TEXT NOT NULL,
    foodItem                  TEXT NOT NULL,
    foodWeightKg              REAL NOT NULL,
    pickupLocationId          INTEGER NOT NULL,
    deliveryLocationId        INTEGER NOT NULL,
    orderTimeMin              REAL NOT NULL,
    requestedDeliveryTimeMin  REAL NOT NULL,
    priority                  REAL NOT NULL,
    status                    TEXT NOT NULL DEFAULT 'CREATED',
    assignedRiderId           INTEGER,
    distanceKm                REAL NOT NULL DEFAULT 0.0,
    estimatedDeliveryTimeMin  REAL NOT NULL DEFAULT 0.0,
    vehicleType               TEXT NOT NULL DEFAULT 'ANY',
    FOREIGN KEY(requestId)          REFERENCES service_requests(requestId) ON DELETE SET NULL,
    FOREIGN KEY(pickupLocationId)   REFERENCES locations(locationId),
    FOREIGN KEY(deliveryLocationId) REFERENCES locations(locationId),
    FOREIGN KEY(assignedRiderId)    REFERENCES resources(resourceId) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS algorithm_runs (
    runId             INTEGER PRIMARY KEY AUTOINCREMENT,
    algorithmName     TEXT NOT NULL,
    inputSize         INTEGER NOT NULL,
    timeNs            INTEGER NOT NULL,
    memoryKb          INTEGER NOT NULL,
    dateRun           TEXT NOT NULL,
    operationsCount   INTEGER NOT NULL DEFAULT 0,
    comparisonsCount  INTEGER NOT NULL DEFAULT 0,
    status            TEXT NOT NULL DEFAULT 'SUCCESS',
    resultSummary     TEXT NOT NULL DEFAULT 'Completed'
);

CREATE TABLE IF NOT EXISTS audit_events (
    eventId       INTEGER PRIMARY KEY AUTOINCREMENT,
    eventType     TEXT,
    description   TEXT NOT NULL,
    timestamp     TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_roads_from             ON roads(fromLocationId);
CREATE INDEX IF NOT EXISTS idx_roads_to               ON roads(toLocationId);
CREATE INDEX IF NOT EXISTS idx_resources_status        ON resources(availabilityStatus);
CREATE INDEX IF NOT EXISTS idx_resources_current_loc  ON resources(currentLocationId);
CREATE INDEX IF NOT EXISTS idx_service_requests_status ON service_requests(status);
CREATE INDEX IF NOT EXISTS idx_orders_status           ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_request          ON orders(requestId);
CREATE INDEX IF NOT EXISTS idx_orders_assigned_rider   ON orders(assignedRiderId);
CREATE INDEX IF NOT EXISTS idx_algorithm_runs_name     ON algorithm_runs(algorithmName);
CREATE INDEX IF NOT EXISTS idx_audit_events_type       ON audit_events(eventType);


