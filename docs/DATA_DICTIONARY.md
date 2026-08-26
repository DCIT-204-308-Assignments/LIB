# UG Smart Food Delivery & Logistics System - Data Dictionary

This document details the database schema for the UG Smart Food Delivery & Logistics Application, outlining tables, columns, data types, constraints, and key relationships.

---

## 1. LOCATIONS
Stores physical campus nodes and geographical coordinates used by the graph routing engines.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `location_id` | INTEGER | PRIMARY KEY | Unique identifier for each campus node/location. |
| `name` | TEXT | NOT NULL | Name of the location (e.g., Balme Library, Night Market). |
| `category` | TEXT | NOT NULL | Category type (e.g., Hostel, Academic, Vendor, Administration). |
| `latitude` | REAL | NOT NULL | Geographic latitude coordinate. |
| `longitude` | REAL | NOT NULL | Geographic longitude coordinate. |

---

## 2. RESTAURANTS
Stores registered food vendors located across the campus network.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `restaurant_id` | INTEGER | PRIMARY KEY | Unique identifier for each restaurant. |
| `name` | TEXT | NOT NULL | Commercial name of the eatery. |
| `location_id` | INTEGER | FOREIGN KEY -> LOCATIONS(location_id) | Physical location node on campus map. |
| `cuisine_type` | TEXT | - | Type of cuisine offered (e.g., Ghanaian Local, Fast Food). |
| `contact_number`| TEXT | - | Contact phone number for vendor operations. |
| `is_active` | INTEGER | DEFAULT 1 | Operational status flag (1 = Open, 0 = Closed). |

---

## 3. FOOD_ITEMS
Stores individual menu items offered by each restaurant.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `item_id` | INTEGER | PRIMARY KEY | Unique identifier for each menu item. |
| `restaurant_id` | INTEGER | FOREIGN KEY -> RESTAURANTS(restaurant_id) | The restaurant offering this menu item. |
| `name` | TEXT | NOT NULL | Name of the food dish or beverage. |
| `price` | REAL | NOT NULL | Item unit price in GHS. |
| `category` | TEXT | - | Food category (e.g., Main Dish, Snack, Drink). |
| `is_available` | INTEGER | DEFAULT 1 | Item availability status (1 = Available, 0 = Sold Out). |

---

## 4. CUSTOMERS
Stores student and staff profiles ordering food on campus.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `customer_id` | INTEGER | PRIMARY KEY | Unique customer account ID. |
| `name` | TEXT | NOT NULL | Full name of the customer. |
| `email` | TEXT | UNIQUE | Institutional email address (@st.ug.edu.gh). |
| `phone_number` | TEXT | - | Contact telephone number. |
| `default_location_id` | INTEGER | FOREIGN KEY -> LOCATIONS(location_id) | Customer's primary drop-off location/hall. |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Account creation timestamp. |

---

## 5. ORDERS
Tracks customer orders placed through the application.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `order_id` | INTEGER | PRIMARY KEY | Unique order tracking ID. |
| `customer_id` | INTEGER | FOREIGN KEY -> CUSTOMERS(customer_id) | Customer placing the order. |
| `restaurant_id` | INTEGER | FOREIGN KEY -> RESTAURANTS(restaurant_id) | Restaurant preparing the order. |
| `delivery_location_id` | INTEGER | FOREIGN KEY -> LOCATIONS(location_id) | Target delivery node on campus graph. |
| `total_amount` | REAL | NOT NULL | Total cost of order in GHS. |
| `status` | TEXT | CHECK (status IN ('PENDING', 'PREPARING', 'DISPATCHED', 'DELIVERED', 'CANCELLED')) | Current order processing status. |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Timestamp when order was placed. |

---

## 6. ORDER_ITEMS
Junction table tracking specific line items contained within each order.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `order_item_id` | INTEGER | PRIMARY KEY | Unique order item line entry ID. |
| `order_id` | INTEGER | FOREIGN KEY -> ORDERS(order_id) | Associated order ID. |
| `item_id` | INTEGER | FOREIGN KEY -> FOOD_ITEMS(item_id) | Selected food item ID. |
| `quantity` | INTEGER | NOT NULL | Quantity ordered. |
| `unit_price` | REAL | NOT NULL | Price per unit at time of purchase. |

---

## 7. DELIVERY_ASSIGNMENTS
Tracks real-time dispatch and fulfillment of orders by delivery riders.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `assignment_id` | INTEGER | PRIMARY KEY | Unique dispatch assignment ID. |
| `order_id` | INTEGER | FOREIGN KEY -> ORDERS(order_id) | Order assigned for delivery. |
| `resource_id` | INTEGER | FOREIGN KEY -> RESOURCES(resource_id) | Courier/rider resource assigned. |
| `status` | TEXT | CHECK (status IN ('ASSIGNED', 'IN_TRANSIT', 'COMPLETED', 'FAILED')) | Current delivery assignment state. |
| `assigned_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Dispatch timestamp. |
| `completed_at` | DATETIME | NULLABLE | Fulfillment completion timestamp. |

---

## 8. RESOURCES
Stores couriers, vehicles, and delivery agents available for routing.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `resource_id` | INTEGER | PRIMARY KEY | Unique courier/rider ID. |
| `name` | TEXT | NOT NULL | Courier full name. |
| `type` | TEXT | NOT NULL | Delivery mode (BIKE, FOOT, SCOOTER). |
| `status` | TEXT | NOT NULL | Operational status (AVAILABLE, BUSY, OFFLINE). |
| `current_location_id` | INTEGER | FOREIGN KEY -> LOCATIONS(location_id) | Current location node position on campus map. |

---

## 9. ROADS
Defines graph edges, distances, and weights connecting campus location nodes.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `edge_id` | INTEGER | PRIMARY KEY | Unique road graph edge ID. |
| `source_location_id` | INTEGER | FOREIGN KEY -> LOCATIONS(location_id) | Starting campus node. |
| `target_location_id` | INTEGER | FOREIGN KEY -> LOCATIONS(location_id) | Ending campus node. |
| `distance` | REAL | NOT NULL | Distance in kilometers. |
| `travel_time` | REAL | NOT NULL | Estimated transit time in minutes. |
| `weight` | REAL | NOT NULL | Graph weight calculated for pathfinding engines. |

---

## 10. ALGORITHM_RUNS
Logs performance benchmarking data for pathfinding and optimization algorithms.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `run_id` | INTEGER | PRIMARY KEY | Unique algorithm test execution ID. |
| `algorithm_name` | TEXT | NOT NULL | Executed engine name (e.g., Dijkstra, Prim, MST). |
| `execution_time_ms` | REAL | NOT NULL | Algorithm execution duration in milliseconds. |
| `node_count` | INTEGER | NOT NULL | Number of nodes evaluated during execution. |
| `edge_count` | INTEGER | NOT NULL | Number of edges evaluated during execution. |
| `executed_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Execution timestamp. |

---

## 11. AUDIT_EVENTS
Stores security, error, and system event execution logs.

| Column Name | Data Type | Constraints | Description |
| :--- | :--- | :--- | :--- |
| `event_id` | INTEGER | PRIMARY KEY | Unique system audit event ID. |
| `event_type` | TEXT | NOT NULL | Category classification of event (e.g., INFO, ERROR, WARN). |
| `description` | TEXT | NOT NULL | Detailed event description or error message. |
| `timestamp` | DATETIME | DEFAULT CURRENT_TIMESTAMP | Event logging timestamp. |