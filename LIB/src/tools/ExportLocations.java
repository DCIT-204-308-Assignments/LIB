package tools;

import engines.DatabaseManager;
import ds.DynamicArray;
import models.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ExportLocations {
    public static void main(String[] args) {
        try {
            DynamicArray<Location> locs = DatabaseManager.loadLocations();
            File outDir = new File("web/campus_map");
            outDir.mkdirs();
            File out = new File(outDir, "locations.js");
            try (PrintWriter pw = new PrintWriter(new FileWriter(out))) {
                pw.println("// Generated locations. Do not edit manually if regenerated from DB.");
                pw.println("var LOCATIONS = [");
                for (int i = 0; i < locs.size(); i++) {
                    Location l = locs.get(i);
                    String name = l.getName().replace("\"", "\\\"");
                    pw.printf("  { locationId: %d, name: \"%s\", lat: %f, lon: %f }%s\n",
                            l.getLocationId(), name, l.getLatitude(), l.getLongitude(), (i < locs.size()-1) ? "," : "");
                }
                pw.println("];\n");
            }
            System.out.println("Exported " + locs.size() + " locations to " + out.getPath());
        } catch (Exception ex) {
            System.err.println("Export failed: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(2);
        }
    }
}
