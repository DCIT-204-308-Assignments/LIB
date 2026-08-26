# Group 1 - Database Architecture, Data Management & Core Data Structures

## Project

UG Smart Food Delivery & Dispatch Optimizer

This repository contains all components related to the database layer, persistence, data loading, and the custom linear data structures used throughout the system.

---

## Objectives

- Design and implement the complete relational database.
- Build the persistence layer using JDBC.
- Develop CSV importers and seed scripts.
- Implement the required linear data structures from scratch.
- Provide reusable APIs for other modules.

---

## Scope

### Database

- Database schema
- SQL scripts
- Seed data
- Constraints
- Indexes
- Foreign keys

### Data Loading

- CSV importers
- Validation
- Database initialization
- JDBC integration

### Data Structures

Implement the following without using Java's built-in implementations:

- Dynamic Array
- Linked List
- Stack
- Queue
- Circular Queue
- Deque

### Persistence

- CRUD operations
- Repository layer
- Transaction management
- Database reload
- Benchmark storage

### Documentation

- ER Diagram
- Database Schema
- Data Dictionary
- Dataset Documentation

---

## Expected Folder Structure

```
src/
│
├── database/
│   ├── connection/
│   ├── schema/
│   ├── repository/
│   └── migrations/
│
├── loader/
│
├── validation/
│
├── datastructures/
│   ├── array/
│   ├── linkedlist/
│   ├── stack/
│   ├── queue/
│   ├── circularqueue/
│   └── deque/
│
├── model/
│
└── util/
```

---

## Deliverables

- Complete SQL schema
- Seed scripts
- JDBC implementation
- CSV loaders
- Data validation
- Custom linear data structures
- Unit tests
- Documentation

---

## Team Members

- Edmund Nii Laryea Boye
- Jason Nana Sam Mends-Brew
- Kimathi Elikplim Sedegah
- Rabiatu Abdul Salam
- Nana Ohenewaa Owusu-Ansah

---

## Dependencies

- Java 21+
- JDBC
- PostgreSQL / MySQL
- Maven

---

## Branch Strategy

```
main
develop
feature/database
feature/jdbc
feature/loader
feature/datastructures
```

---

## Definition of Done

A task is complete when:

- Code compiles successfully.
- Unit tests pass.
- Documentation is updated.
- Code has been reviewed.
- No merge conflicts exist.
