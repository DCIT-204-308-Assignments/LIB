import engines.DatabaseManager;

public class SeedDB {
    public static void main(String[] args) {
        try {
            System.out.println("Seeding database from CSV files...");
            DatabaseManager.initializeDatabase("locations.csv", "roads.csv");
            System.out.println("Database seeding complete. DB file: ug_swift.db");
        } catch (Exception ex) {
            System.err.println("Seeding failed: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(2);
        }
    }
}
