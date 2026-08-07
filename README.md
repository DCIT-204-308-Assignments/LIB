*** Begin README replacement ***

# UG Swift — Campus Service Operations Prototype

This repository is a Java desktop application demonstrating a campus logistics and routing system (UG Swift). It was built as a Data Structures & Algorithms (DSA) semester project and contains custom implementations of core data structures, graph routing, scheduling engines, and a Swing-based UI.

## Highlights

- Interactive Swing UI for placing orders, managing riders, viewing incoming and completed queues, and demoing data-structures.
- SQLite-backed persistent store for locations, roads, resources (riders), and service requests.
- Custom data-structure library under `src/ds/` (Stack, Queue, Deque, MinHeap, Graph, HashTable, DynamicArray, etc.).
- Engines for routing (Dijkstra), dispatch (FIFO/priority/circular), and simple optimisation.

## Prerequisites

- Java JDK 17 or later (ensure `javac` and `java` are on PATH).
- SQLite JDBC driver JAR. The project expects `sqlite-jdbc-3.42.0.0.jar` in the repository root — download and place it there if missing.

## Quick start — Windows (PowerShell)

1. Clone the repository and change directory:

	git clone <repo-url>
	cd LIB

2. Compile all sources and create the `bin` directory:

	javac -d bin @(Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })

3. Seed the database from CSV files (headless):

	java -cp "bin;sqlite-jdbc-3.42.0.0.jar" SeedDB

	This creates `ug_swift.db` with seeded locations, roads, riders and 300 service requests.

4. Launch the GUI:

	java -cp "bin;sqlite-jdbc-3.42.0.0.jar" UGSwiftApp

## Quick start — macOS / Linux (bash)

1. Clone and cd into project root.

2. Compile:

	find src -name "*.java" > sources.txt
	javac -d bin @sources.txt
	rm sources.txt

3. Seed DB:

	java -cp "bin:sqlite-jdbc-3.42.0.0.jar" SeedDB

4. Run app:

	java -cp "bin:sqlite-jdbc-3.42.0.0.jar" UGSwiftApp

## Key UI features

- `Initialize Data` / `Generate Roads` — seed and (re)create CSV-based datasets.
- `Show Seeded Requests` — modal lists the 300 seeded service requests.
- `DS Demo` — tabbed modal demonstrating `Stack`, `Queue`, `Deque`, and `Priority Queue` (MinHeap) with step-by-step trace output useful for reports and live demos.
- Incoming queue controls: `Process next`, `Start Auto` (auto-process interval), and `Process N` (bulk processing).

## Seeding notes

- CSV files are expected in a `data/` folder (the data resolver tries several common candidate directories). The `SeedDB` runner calls `DatabaseManager.initializeDatabase(locations.csv, roads.csv)` which will seed tables if empty.

## Running tests and demos

- A simple DSA validation harness is available at `src/UGSwiftTestSuite.java`.

  java -cp "bin;sqlite-jdbc-3.42.0.0.jar" UGSwiftTestSuite

## Developer notes (important paths)

- `src/UGSwiftApp.java` — main Swing application GUI.
- `src/SeedDB.java` — headless seeder utility.
- `src/engines` — routing, scheduling, database manager, and other engines.
- `src/ds` — custom data-structure implementations.
- `src/models` — domain models (`ServiceRequest`, `Resource`, `Location`, etc.).

## Evidence for coursework

- The project implements core structures (Queue, Stack, Deque, MinHeap, Graph, HashTable) without using Java built-ins for those components. Use the `DS Demo` modal and the `UGSwiftTestSuite` harness to produce trace outputs and screenshots for your report.

## Troubleshooting

- "`javac` not recognized": install a JDK and ensure `javac` is on PATH.
- Missing `sqlite-jdbc-3.42.0.0.jar`: download the JDBC driver and place it in the project root or update the classpath with your local JAR name.
- If `SeedDB` reports no CSVs found, ensure the `data/locations.csv` and `data/roads.csv` files exist or use the `Generate Roads` button in the UI.

## .gitignore

- The repository includes a `.gitignore` that excludes `ug_swift.db`, JDBC jars, build artifacts and common IDE files. If you need to keep CSVs under version control, remove them from the ignore file.

## Contributing

- Fork, implement a feature branch, add tests, update docs, and submit a PR. If you change the DB schema include migration SQL and update `DatabaseManager` accordingly.

## License

- Add an appropriate license file if the submission requires one.

*** End README replacement ***
