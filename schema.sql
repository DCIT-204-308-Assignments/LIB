PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS locations (
    locationId INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    zone TEXT NOT NULL,
    type TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL
);

CREATE TABLE IF NOT EXISTS roads (
    roadId INTEGER PRIMARY KEY,
    fromLocationId INTEGER NOT NULL,
    toLocationId INTEGER NOT NULL,
    fromName TEXT NOT NULL,
    toName TEXT NOT NULL,
    distanceKm REAL NOT NULL,
    travelTimeMin REAL NOT NULL,
    trafficLevel TEXT NOT NULL,
    roadCondition TEXT NOT NULL,
    roadConditionWeight REAL NOT NULL,
    isOneWay BOOLEAN NOT NULL,
    weight REAL NOT NULL,
    FOREIGN KEY (fromLocationId) REFERENCES locations (locationId),
    FOREIGN KEY (toLocationId) REFERENCES locations (locationId)
);

CREATE TABLE IF NOT EXISTS resources (
    resourceId INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    homeLocationId INTEGER NOT NULL,
    capacityKg REAL NOT NULL,
    availabilityStatus TEXT NOT NULL,
    FOREIGN KEY (homeLocationId) REFERENCES locations (locationId)
);

CREATE TABLE IF NOT EXISTS service_requests (
    requestId INTEGER PRIMARY KEY,
    sourceLocationId INTEGER NOT NULL,
    destLocationId INTEGER NOT NULL,
    category TEXT NOT NULL,
    urgency INTEGER NOT NULL,
    timeSubmittedMin REAL NOT NULL,
    deadlineMin REAL NOT NULL,
    status TEXT NOT NULL,
    assignedRiderId INTEGER,
    deliveredTimeMin REAL,
    FOREIGN KEY (sourceLocationId) REFERENCES locations (locationId),
    FOREIGN KEY (destLocationId) REFERENCES locations (locationId),
    FOREIGN KEY (assignedRiderId) REFERENCES resources (resourceId)
);

CREATE TABLE IF NOT EXISTS algorithm_runs (
    runId INTEGER PRIMARY KEY AUTOINCREMENT,
    algorithmName TEXT NOT NULL,
    inputSize INTEGER NOT NULL,
    timeNs INTEGER NOT NULL,
    memoryKb INTEGER NOT NULL,
    dateRun TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_events (
    eventId INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT NOT NULL,
    timestamp TEXT NOT NULL
);
