package tools;

import java.awt.Desktop;
import java.io.File;

public class CampusMapLauncher {
    public static void main(String[] args) {
        openMap();
    }

    public static void openMap() {
        try {
            ExportLocations.main(new String[]{});

            File f = new File("web/campus_map/index.html");
            if (!f.exists()) {
                System.err.println("Campus map HTML not found: " + f.getPath());
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(f.toURI());
            } else {
                System.err.println("Desktop browse not supported on this platform. Open " + f.getAbsolutePath() + " manually.");
            }
        } catch (Exception ex) {
            System.err.println("Failed to open campus map: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
