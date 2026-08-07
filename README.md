# UG SWIFT

This repository now contains a fuller Java desktop application for a campus logistics and routing system built around the UG Swift concept.

## What the project now includes

- A desktop UI for initializing the database, generating roads, computing routes, and running dispatch demos.
- A realistic campus road-network generator based on UG Legon location data.
- Graph-based routing and shortest-path logic using Dijkstra.
- Scheduling and optimisation engines for FIFO, priority, and round-robin dispatching.
- A SQLite-backed data layer for locations, roads, riders, requests, and audit history.
- A comprehensive DSA test suite covering custom data structures and algorithms.

## How to run it

Run the project directly with the standard Java entry point:

- Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName } | Set-Content sources.txt
- javac -cp ".;sqlite-jdbc-3.42.0.0.jar" -d bin @(Get-Content sources.txt)
- Remove-Item sources.txt
- java -cp "bin;sqlite-jdbc-3.42.0.0.jar" Main

## Main project areas

- [src/UGSwiftApp.java](src/UGSwiftApp.java) — interactive desktop UI.
- [src/UGSwiftLauncher.java](src/UGSwiftLauncher.java) — startup entry point.
- [src/RoadNetworkGenerator.java](src/RoadNetworkGenerator.java) — road network generation.
- [src/UGSwiftTestSuite.java](src/UGSwiftTestSuite.java) — DSA validation suite.
- [src/engines](src/engines) — route planning, scheduler, optimiser, and database logic.
- [src/models](src/models) — domain objects for locations, roads, and requests.
