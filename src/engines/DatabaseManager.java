package engines;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.Random;
import ds.DynamicArray;
import models.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:ug_swift.db";
    private static final long SEED = 22237205L;

    static {
        // Load SQLite JDBC driver class
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found. Ensure the JAR is in the classpath.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase(String locationsCsv, String roadsCsv) {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Create tables
            stmt.execute("CREATE TABLE IF NOT EXISTS locations (" +
                    "locationId INTEGER PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "zone TEXT NOT NULL," +
                    "type TEXT NOT NULL," +
                    "latitude REAL NOT NULL," +
                    "longitude REAL NOT NULL" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS roads (" +
                    "roadId INTEGER PRIMARY KEY," +
                    "fromLocationId INTEGER NOT NULL," +
                    "toLocationId INTEGER NOT NULL," +
                    "fromName TEXT NOT NULL," +
                    "toName TEXT NOT NULL," +
                    "distanceKm REAL NOT NULL," +
                    "travelTimeMin REAL NOT NULL," +
                    "trafficLevel TEXT NOT NULL," +
                    "roadCondition TEXT NOT NULL," +
                    "roadConditionWeight REAL NOT NULL," +
                    "isOneWay BOOLEAN NOT NULL," +
                    "weight REAL NOT NULL," +
                    "FOREIGN KEY(fromLocationId) REFERENCES locations(locationId)," +
                    "FOREIGN KEY(toLocationId) REFERENCES locations(locationId)" +
                    ");");

            stmt.execute("CREATE TABLE IF NOT EXISTS resources (" +
                    "resourceId INTEGER PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "type TEXT NOT NULL," +
                    "homeLocationId INTEGER NOT NULL," +
                    "capacityKg REAL NOT NULL," +
                    "availabilityStatus TEXT NOT NULL," +
                    "FOREIGN KEY(homeLocationId) REFERENCES locations(locationId)" +
                    ");");

                stmt.execute("CREATE TABLE IF NOT EXISTS service_requests (" +
                    "requestId INTEGER PRIMARY KEY," +
                    "sourceLocationId INTEGER NOT NULL," +
                    "destLocationId INTEGER NOT NULL," +
                    "category TEXT NOT NULL," +
                    "urgency INTEGER NOT NULL," +
                    "timeSubmittedMin REAL NOT NULL," +
                    "deadlineMin REAL NOT NULL," +
                    "status TEXT NOT NULL," +
                    "assignedRiderId INTEGER," +
                    "deliveredTimeMin REAL," +
                    "FOREIGN KEY(sourceLocationId) REFERENCES locations(locationId)," +
                    "FOREIGN KEY(destLocationId) REFERENCES locations(locationId)" +
                    ");");

            // Migration check: ensure deliveredTimeMin exists if table was created previously without it
            try {
                stmt.execute("ALTER TABLE service_requests ADD COLUMN deliveredTimeMin REAL;");
            } catch (SQLException ignored) {
                // Column already exists
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS algorithm_runs (" +
                    "runId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "algorithmName TEXT NOT NULL," +
                    "inputSize INTEGER NOT NULL," +
                    "timeNs INTEGER NOT NULL," +
                    "memoryKb INTEGER NOT NULL," +
                    "dateRun TEXT NOT NULL," +
                    "operationsCount INTEGER NOT NULL DEFAULT 0," +
                    "comparisonsCount INTEGER NOT NULL DEFAULT 0," +
                    "status TEXT NOT NULL DEFAULT 'SUCCESS'," +
                    "resultSummary TEXT NOT NULL DEFAULT 'Completed'" +
                    ");");

            // Migration checks for algorithm performance evidence fields.
            try {
                stmt.execute(
                        "ALTER TABLE algorithm_runs " +
                        "ADD COLUMN operationsCount INTEGER NOT NULL DEFAULT 0;"
                );
            } catch (SQLException ignored) {
                // Column already exists.
            }

            try {
                stmt.execute(
                        "ALTER TABLE algorithm_runs " +
                        "ADD COLUMN comparisonsCount INTEGER NOT NULL DEFAULT 0;"
                );
            } catch (SQLException ignored) {
                // Column already exists.
            }

            try {
                stmt.execute(
                        "ALTER TABLE algorithm_runs " +
                        "ADD COLUMN status TEXT NOT NULL DEFAULT 'SUCCESS';"
                );
            } catch (SQLException ignored) {
                // Column already exists.
            }

            try {
                stmt.execute(
                        "ALTER TABLE algorithm_runs " +
                        "ADD COLUMN resultSummary TEXT NOT NULL DEFAULT 'Completed';"
                );
            } catch (SQLException ignored) {
                // Column already exists.
            }

            stmt.execute("CREATE TABLE IF NOT EXISTS audit_events (" +
                    "eventId INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "description TEXT NOT NULL," +
                    "timestamp TEXT NOT NULL" +
                    ");");

            // 2. Seed Locations if empty
            if (isTableEmpty(conn, "locations")) {
                seedLocations(conn, locationsCsv);
            }

            // 3. Seed Roads if empty
            if (isTableEmpty(conn, "roads")) {
                seedRoads(conn, roadsCsv);
            }

            // 4. Seed Riders (resources) if empty
            if (isTableEmpty(conn, "resources")) {
                seedRiders(conn);
            }

            // 5. Seed Requests if empty (minimum 300 requests)
            if (isTableEmpty(conn, "service_requests")) {
                seedServiceRequests(conn);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isTableEmpty(Connection conn, String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
        return true;
    }

    private static java.io.File resolveFile(String path) {
        java.io.File f = new java.io.File(path);
        if (f.exists()) return f;
        java.io.File inData = new java.io.File("data", path);
        if (inData.exists()) return inData;
        java.io.File stripped = new java.io.File("data", f.getName());
        if (stripped.exists()) return stripped;
        return f;
    }

    private static void seedLocations(Connection conn, String path) throws Exception {
        String insertSql = "INSERT INTO locations (locationId, name, zone, type, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)";
        java.io.File csvFile = resolveFile(path);
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql);
             BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            reader.readLine(); // skip header
            String line;
            conn.setAutoCommit(false);
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = parseCsvLine(line);
                pstmt.setInt(1, Integer.parseInt(parts[0].trim()));
                pstmt.setString(2, parts[1].trim());
                pstmt.setString(3, parts[2].trim());
                pstmt.setString(4, parts[3].trim());
                pstmt.setDouble(5, Double.parseDouble(parts[4].trim()));
                pstmt.setDouble(6, Double.parseDouble(parts[5].trim()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Seeded locations table.");
        }
    }

    private static void seedRoads(Connection conn, String path) throws Exception {
        String insertSql = "INSERT INTO roads (roadId, fromLocationId, toLocationId, fromName, toName, distanceKm, travelTimeMin, trafficLevel, roadCondition, roadConditionWeight, isOneWay, weight) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        java.io.File csvFile = resolveFile(path);
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql);
             BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            reader.readLine(); // skip header
            String line;
            conn.setAutoCommit(false);
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = parseCsvLine(line);
                pstmt.setInt(1, Integer.parseInt(parts[0].trim()));
                pstmt.setInt(2, Integer.parseInt(parts[1].trim()));
                pstmt.setInt(3, Integer.parseInt(parts[2].trim()));
                pstmt.setString(4, parts[3].trim());
                pstmt.setString(5, parts[4].trim());
                pstmt.setDouble(6, Double.parseDouble(parts[5].trim()));
                pstmt.setDouble(7, Double.parseDouble(parts[6].trim()));
                pstmt.setString(8, parts[7].trim());
                pstmt.setString(9, parts[8].trim());
                pstmt.setDouble(10, Double.parseDouble(parts[9].trim()));
                pstmt.setBoolean(11, Boolean.parseBoolean(parts[10].trim()));
                pstmt.setDouble(12, Double.parseDouble(parts[11].trim()));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Seeded roads table.");
        }
    }

    private static void seedRiders(Connection conn) throws SQLException {
        String[] ghanaianNames = {
                "Kofi Mensah", "Ama Serwaa", "Kwame Bempah", "Yaw Osei", "Adjoa Boateng",
                "Kweku Addo", "Abena Frimpong", "Ekow Arthur", "Efua Gyasi", "Kwesi Baah",
                "Akosua Darko", "Kojo Antwi", "Afia Appiah", "Kwabena Owusu", "Yaaba Koomson",
                "Aba Nyarko", "Sampson Ofori", "Ebenezer Quansah", "Emmanuel Tetteh", "Dorothy Abbey",
                "Gifty Agyei", "Grace Kusi", "Theresa Ansah", "Charles Boakye", "Daniel Mensah",
                "Josephine Addae", "Priscilla Okyere", "Ransford Osei", "Stephen Baidoo", "Vida Boateng"
        };
        String insertSql = "INSERT INTO resources (resourceId, name, type, homeLocationId, capacityKg, availabilityStatus) VALUES (?, ?, ?, ?, ?, ?)";
        Random rnd = new Random(SEED);
        conn.setAutoCommit(false);
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            for (int i = 1; i <= 30; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, ghanaianNames[i - 1]);
                String type = (rnd.nextDouble() < 0.3) ? "BICYCLE" : "MOTORBIKE";
                pstmt.setString(3, type);
                // Assign a random home location ID between 1 and 95
                int homeLoc = rnd.nextInt(95) + 1;
                pstmt.setInt(4, homeLoc);
                double capacity = (type.equals("BICYCLE")) ? 10.0 + rnd.nextDouble() * 5.0 : 25.0 + rnd.nextDouble() * 15.0;
                pstmt.setDouble(5, Math.round(capacity * 10.0) / 10.0);
                pstmt.setString(6, "AVAILABLE");
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Seeded 30 Ghanaian Riders.");
        }
    }

    private static void seedServiceRequests(Connection conn) throws SQLException {
        String[] categories = {"Waakye", "Jollof Rice", "Red Red", "Fufu & Goat Light Soup", "Pizza", "Documents", "Pharmacy", "Groceries", "Courier Package"};
        Random rnd = new Random(SEED);
        String insertSql = "INSERT INTO service_requests (requestId, sourceLocationId, destLocationId, category, urgency, timeSubmittedMin, deadlineMin, status, assignedRiderId, deliveredTimeMin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        conn.setAutoCommit(false);
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            for (int i = 1; i <= 300; i++) {
                pstmt.setInt(1, i);
                int src = rnd.nextInt(95) + 1;
                int dest = rnd.nextInt(95) + 1;
                while (src == dest) {
                    dest = rnd.nextInt(95) + 1;
                }
                pstmt.setInt(2, src);
                pstmt.setInt(3, dest);
                pstmt.setString(4, categories[rnd.nextInt(categories.length)]);
                int urgency = rnd.nextInt(5) + 1; // 1 to 5
                pstmt.setInt(5, urgency);

                double submitTime = 480 + rnd.nextInt(600); // 8:00 AM to 6:00 PM (in minutes)
                pstmt.setDouble(6, Math.round(submitTime * 10.0) / 10.0);
                double leadTime = 30 + rnd.nextInt(90) + (6 - urgency) * 15; // tighter deadlines for urgent requests
                pstmt.setDouble(7, Math.round((submitTime + leadTime) * 10.0) / 10.0);

                pstmt.setString(8, "PENDING");
                pstmt.setNull(9, Types.INTEGER);
                pstmt.setNull(10, Types.REAL);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Seeded 300 delivery requests.");
        }
    }

    public static DynamicArray<Location> loadLocations() {
        DynamicArray<Location> list = new DynamicArray<>();
        String sql = "SELECT * FROM locations";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Location(
                        rs.getInt("locationId"),
                        rs.getString("name"),
                        rs.getString("zone"),
                        rs.getString("type"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static DynamicArray<RoadEdge> loadRoads() {
        DynamicArray<RoadEdge> list = new DynamicArray<>();
        String sql = "SELECT * FROM roads";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new RoadEdge(
                        rs.getInt("roadId"),
                        rs.getInt("fromLocationId"),
                        rs.getInt("toLocationId"),
                        rs.getString("fromName"),
                        rs.getString("toName"),
                        rs.getDouble("distanceKm"),
                        rs.getDouble("travelTimeMin"),
                        rs.getString("trafficLevel"),
                        rs.getString("roadCondition"),
                        rs.getDouble("roadConditionWeight"),
                        rs.getBoolean("isOneWay"),
                        rs.getDouble("weight")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static DynamicArray<ServiceRequest> loadServiceRequests() {
        DynamicArray<ServiceRequest> list = new DynamicArray<>();
        String sql = "SELECT * FROM service_requests";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            boolean hasDeliveredCol = false;
            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if ("deliveredTimeMin".equalsIgnoreCase(meta.getColumnName(i))) {
                    hasDeliveredCol = true;
                    break;
                }
            }
            while (rs.next()) {
                double delivered = -1.0;
                if (hasDeliveredCol) {
                    delivered = rs.getDouble("deliveredTimeMin");
                    if (rs.wasNull()) delivered = -1.0;
                }
                list.add(new ServiceRequest(
                        rs.getInt("requestId"),
                        rs.getInt("sourceLocationId"),
                        rs.getInt("destLocationId"),
                        rs.getString("category"),
                        rs.getInt("urgency"),
                        rs.getDouble("timeSubmittedMin"),
                        rs.getDouble("deadlineMin"),
                        rs.getString("status"),
                        rs.getInt("assignedRiderId"),
                        delivered
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void saveServiceRequest(ServiceRequest req) {
        String sql = "UPDATE service_requests SET status = ?, assignedRiderId = ?, deliveredTimeMin = ? WHERE requestId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, req.getStatus());
            if (req.getAssignedRiderId() > 0) {
                pstmt.setInt(2, req.getAssignedRiderId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            if (req.getDeliveredTimeMin() >= 0) {
                pstmt.setDouble(3, req.getDeliveredTimeMin());
            } else {
                pstmt.setNull(3, Types.REAL);
            }
            pstmt.setInt(4, req.getRequestId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addServiceRequest(ServiceRequest req) {
        String sql = "INSERT INTO service_requests (requestId, sourceLocationId, destLocationId, category, urgency, timeSubmittedMin, deadlineMin, status, assignedRiderId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, req.getRequestId());
            pstmt.setInt(2, req.getSourceLocationId());
            pstmt.setInt(3, req.getDestLocationId());
            pstmt.setString(4, req.getCategory());
            pstmt.setInt(5, req.getUrgency());
            pstmt.setDouble(6, req.getTimeSubmittedMin());
            pstmt.setDouble(7, req.getDeadlineMin());
            pstmt.setString(8, req.getStatus());
            if (req.getAssignedRiderId() > 0) {
                pstmt.setInt(9, req.getAssignedRiderId());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DynamicArray<Resource> loadResources() {
        DynamicArray<Resource> list = new DynamicArray<>();
        String sql = "SELECT * FROM resources";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Resource(
                        rs.getInt("resourceId"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("homeLocationId"),
                        rs.getDouble("capacityKg"),
                        rs.getString("availabilityStatus")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void updateResourceStatus(int id, String status) {
        String sql = "UPDATE resources SET availabilityStatus = ? WHERE resourceId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addAlgorithmRun(AlgorithmRun run) {
        if (run == null) {
            return;
        }

        String sql =
                "INSERT INTO algorithm_runs (" +
                "algorithmName, inputSize, timeNs, memoryKb, dateRun, " +
                "operationsCount, comparisonsCount, status, resultSummary" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, run.getAlgorithmName());
            pstmt.setInt(2, run.getInputSize());
            pstmt.setLong(3, run.getTimeNs());
            pstmt.setLong(4, run.getMemoryKb());
            pstmt.setString(5, run.getDateRun());
            pstmt.setLong(6, run.getOperationsCount());
            pstmt.setLong(7, run.getComparisonsCount());
            pstmt.setString(8, run.getStatus());
            pstmt.setString(9, run.getResultSummary());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DynamicArray<AlgorithmRun> loadAlgorithmRuns() {
        DynamicArray<AlgorithmRun> list = new DynamicArray<>();

        String sql =
                "SELECT * FROM algorithm_runs ORDER BY runId";

        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(
                        new AlgorithmRun(
                                rs.getInt("runId"),
                                rs.getString("algorithmName"),
                                rs.getInt("inputSize"),
                                rs.getLong("timeNs"),
                                rs.getLong("memoryKb"),
                                rs.getString("dateRun"),
                                rs.getLong("operationsCount"),
                                rs.getLong("comparisonsCount"),
                                rs.getString("status"),
                                rs.getString("resultSummary")
                        )
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public static void clearAlgorithmRuns() {
        String sql = "DELETE FROM algorithm_runs";

        try (Connection conn = getConnection();
            Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addAuditEvent(String description) {
        String sql = "INSERT INTO audit_events (description, timestamp) VALUES (?, ?)";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new java.util.Date());
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, description);
            pstmt.setString(2, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DynamicArray<AuditEvent> loadAuditEvents() {
        DynamicArray<AuditEvent> list = new DynamicArray<>();
        String sql = "SELECT * FROM audit_events ORDER BY eventId DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new AuditEvent(
                        rs.getInt("eventId"),
                        rs.getString("description"),
                        rs.getString("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void removeLastAuditEvent() {
        String sql = "DELETE FROM audit_events WHERE eventId = (SELECT MAX(eventId) FROM audit_events)";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String[] parseCsvLine(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
