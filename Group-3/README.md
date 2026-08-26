# Group 3 - Testing, Performance Analysis & Technical Documentation

## Project

UG Smart Food Delivery & Dispatch Optimizer

This repository contains all testing, benchmarking, correctness verification, performance evaluation, and technical documentation for the project.

---

## Objectives

Ensure the project is reliable, correct, efficient, and ready for submission.

---

## Scope

### Unit Testing

Develop tests for:

- Data Structures
- Searching
- Sorting
- Trees
- Graph Algorithms
- Optimization
- Database

| Testing Type        | Tool                         | Purpose                                            |
| ------------------- | ---------------------------- | -------------------------------------------------- |
| Unit Testing        | JUnit 5                      | Test algorithms, data structures, database methods |
| Integration Testing | JUnit + Testcontainers       | Test database and module communication             |
| End-to-End Testing  | Playwright                   | Test complete user workflows through the UI        |
| Benchmarking        | JMH / custom benchmark tools | Measure algorithm performance                      |


Minimum:

40+ unit tests

---

### Correctness

Produce

- Trace Tables
- Loop Invariants
- Proof Sketches
- Counterexamples
- Edge Case Tests

---

### Benchmarking

Measure

- Runtime
- Memory Usage

Across multiple dataset sizes.

---

### Performance Analysis

Generate

- CSV results
- Performance graphs
- Runtime comparisons
- Complexity discussions

---

### Documentation

Prepare

- Final Report
- Architecture
- Algorithm Documentation
- Database Documentation
- User Guide
- Presentation Slides

---

### Quality Assurance

Verify

- Module integration
- Documentation
- Testing coverage
- Submission readiness

---

## Expected Folder Structure

```
tests/
│
├── datastructures/
├── searching/
├── sorting/
├── graph/
├── optimization/
└── database/

benchmark/
│
├── csv/
├── graphs/
└── reports/

documentation/
│
├── report/
├── screenshots/
├── diagrams/
└── slides/
```

---

## Deliverables

- 40+ Unit Tests
- Benchmark Results
- CSV Exports
- Performance Graphs
- Trace Tables
- Proof Sketches
- Final Report
- User Guide
- Presentation Slides

---

## Team Members

- Marcia Candle Salifu
- Kimathi Elikplim Sedegah
- Paa Amon Boakye Yeboah
- Rexford Twum Yarko
- Chris Nana Baah Heighty

---

## Dependencies

- JUnit 5
- Maven
- Java 21+
- PostgreSQL / MySQL

---

## Branch Strategy

```
main
develop
feature/testing
feature/benchmark
feature/documentation
feature/report
```

---

## Definition of Done

Work is complete when:

- Tests pass successfully.
- Benchmark results are reproducible.
- Documentation is complete.
- Performance graphs are generated.
- Final report has been reviewed.
- All project requirements have been verified.
