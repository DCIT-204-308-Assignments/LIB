# UG Swift

### Campus Service Operations Prototype

![Java](https://img.shields.io/badge/Java-17+-orange)
![SQLite](https://img.shields.io/badge/Database-SQLite-blue)
![Swing](https://img.shields.io/badge/UI-Java%20Swing-green)
![License](https://img.shields.io/badge/License-Academic-lightgrey)

UG Swift is a **Java desktop application** that simulates a smart campus courier and service dispatch system for the **University of Ghana**. The project was developed as part of the **DCIT 204/308 Data Structures & Algorithms Semester Project**, demonstrating practical applications of custom data structures, graph algorithms, scheduling techniques, and database systems.

---

## Features

- Interactive Java Swing desktop interface
- Campus routing using **Dijkstra's Shortest Path Algorithm**
- SQLite persistent database
- Custom implementations of core Data Structures
- Service request management
- Rider dispatch and scheduling
- Automatic request processing
- DSA demonstration module
- Performance testing suite

---

# Technologies

| Technology         | Purpose                 |
| ------------------ | ----------------------- |
| Java 17+           | Application Development |
| Java Swing         | Desktop UI              |
| SQLite             | Database                |
| JDBC               | Database Connectivity   |
| CSV                | Initial Dataset         |
| Custom DSA Library | Core Algorithms         |

---

# Custom Data Structures

Located in:

```
src/ds/
```

Implemented without relying on Java's built-in equivalents.

- Dynamic Array
- Stack
- Queue
- Circular Queue
- Deque
- Priority Queue (Min Heap)
- Hash Table
- Weighted Graph
- Linked Lists

---

# Algorithms

The project implements and demonstrates:

### Searching

- Linear Search
- Binary Search

### Sorting

- Selection Sort
- Insertion Sort
- Merge Sort
- Quick Sort

### Graph Algorithms

- Breadth-First Search (BFS)
- Depth-First Search (DFS)
- Dijkstra's Shortest Path
- Prim's Minimum Spanning Tree
- Kruskal's Minimum Spanning Tree

### Optimisation

- Greedy Nearest Neighbor
- Greedy Fastest Available Rider
- Dynamic Programming Knapsack
- Brute-Force Batching Baseline

### Scheduling and Supporting Algorithms

- FIFO Scheduling
- Priority Scheduling
- Circular Dispatch
- Heap Operations
- Hashing
- Union-Find / Disjoint Set

---

# Project Structure

```
UG-Swift
│
├── data/
│   ├── locations.csv
│   ├── roads.csv
│   └── ...
│
├── src/
│   ├── ds/
│   ├── engines/
│   ├── models/
│   ├── ui/
│   ├── SeedDB.java
│   ├── UGSwiftApp.java
│   └── UGSwiftTestSuite.java
│
├── bin/
│
├── ug_swift.db
│
├── sqlite-jdbc-3.42.0.0.jar
│
└── README.md
```

---

# Prerequisites

Before running the project, install:

- Java JDK 17 or newer
- SQLite JDBC Driver

Place the JDBC driver in the project root:

```
sqlite-jdbc-3.42.0.0.jar
```

---

# Getting Started

## Clone the Repository

```bash
git clone <repository-url>
cd UG-Swift
```

---

## Windows (PowerShell)

### Compile

```powershell
mkdir bin

javac -encoding UTF-8 -d bin @(Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object {$_.FullName})
```

### Seed Database

```powershell
java -cp "bin;sqlite-jdbc-3.42.0.0.jar" SeedDB
```

### Run Application

```powershell
java -cp "bin;sqlite-jdbc-3.42.0.0.jar" UGSwiftApp
```

---

## macOS / Linux

### Compile

```bash
mkdir -p bin

find src -name "*.java" > sources.txt

javac -encoding UTF-8 -d bin @sources.txt

rm sources.txt
```

### Seed Database

```bash
java -cp "bin:sqlite-jdbc-3.42.0.0.jar" SeedDB
```

### Run Application

```bash
java -cp "bin:sqlite-jdbc-3.42.0.0.jar" UGSwiftApp
```

---

# User Interface

The desktop application includes the following modules:

| Module             | Description                               |
| ------------------ | ----------------------------------------- |
| Dashboard          | Overview of the system                    |
| Place Request      | Create service requests                   |
| Rider Management   | Manage delivery riders                    |
| Incoming Queue     | View waiting requests                     |
| Completed Requests | Processed deliveries                      |
| DSA Demo           | Interactive data structure demonstrations |
| Reports            | Statistics and analytics                  |

---

# DSA Demonstration

The built-in DSA Demo provides live visual demonstrations of:

- Stack Operations
- Queue Operations
- Deque Operations
- Priority Queue
- Min Heap Behaviour

These demonstrations generate trace outputs suitable for coursework reports and presentations.

---

# Database Seeding

The project automatically imports data from CSV files.

Required files:

```
data/
├── locations.csv
└── roads.csv
```

Running

```bash
SeedDB
```

creates

```
ug_swift.db
```

and seeds:

- Campus Locations
- Road Network
- Riders
- 300 Service Requests

---

# Testing

Run the included validation suite:

```bash
java -cp "bin:sqlite-jdbc-3.42.0.0.jar" UGSwiftTestSuite
```

The suite verifies:

- Queue
- Stack
- Heap
- Graph
- Hash Table
- Routing Algorithms

---

# Algorithm Testing and Performance Evidence

The current validation suite contains **221 automated tests** covering custom
data structures, searching, sorting, routing, optimisation, models, and edge
cases.

Latest verified result:

```text
RESULTS: 221 passed, 0 failed  (out of 221 total)
ALL TESTS PASSED!
```

---

# Important Source Files

| File | Purpose |
|------|---------|
| `UGSwiftApp.java` | Main desktop application |
| `SeedDB.java` | Database seeder |
| `UGSwiftTestSuite.java` | DSA validation suite |
| `AlgorithmBenchmark.java` | Algorithm performance benchmark runner |
| `src/ds/` | Custom data structures |
| `src/engines/` | Routing, scheduling and database logic |
| `src/models/` | Domain models |
| `evidence/algorithm/algorithm_evidence.md` | Trace tables, proofs and analysis |
| `evidence/performance/performance_results.csv` | Raw benchmark results |
| `evidence/performance/graphs/` | Generated performance charts |
| `tools/generate_performance_graphs.py` | Performance graph generator |

---

# Coursework Objectives Demonstrated

This project showcases practical implementations of:

- Custom Data Structures
- Graph Algorithms
- Database Management
- Scheduling Algorithms
- Algorithm Analysis
- Performance Benchmarking
- Software Engineering Principles

---

# Troubleshooting

### `javac` not recognized

Install Java JDK 17+ and ensure `javac` is added to your system PATH.

---

### SQLite Driver Missing

Download the SQLite JDBC driver and place

```

sqlite-jdbc-3.42.0.0.jar

```

in the project root.

---

### CSV Files Not Found

Ensure the following exist:

```

data/
├── locations.csv
└── roads.csv

```

or regenerate the dataset from the application.

---

# Git Ignore

The repository ignores:

- Database files
- Build artifacts
- IDE configuration
- JDBC libraries

Examples:

```

bin/
\*.class
ug_swift.db
.idea/
.vscode/

```

---

# Contributing

1. Fork the repository
2. Create a feature branch
3. Implement your changes
4. Test thoroughly
5. Submit a Pull Request

---

# License

This repository was developed for academic purposes as part of the University of Ghana Data Structures & Algorithms coursework.

```

Copyright © 2026 UG Swift Team

```

```
