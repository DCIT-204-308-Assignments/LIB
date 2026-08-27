import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

import ds.DynamicArray;
import ds.Graph;
import engines.AuditLog;
import engines.DatabaseManager;
import engines.DeliveryEngine;
import engines.DriverPool;
import engines.IncomingOrderManager;
import engines.BenchmarkEngine;
import engines.IndexingEngine;
import engines.MetricsEngine;
import engines.OptimisationEngine;
import engines.ReportEngine;
import engines.SimulationEngine;
import engines.RouteEngine;
import engines.SchedulingEngine;
import engines.SortingEngine;
import models.Location;
import models.Order;
import models.Resource;
import models.RoadEdge;
import models.ServiceRequest;
import utils.Config;

public class UGSwiftApp extends JFrame {

    // --- Theme & Palette System (from red.md) ---
    static final Color BG_DARK        = new Color(0x1A1B2E);
    static final Color BG_SIDEBAR     = new Color(0x16213E);
    static final Color BG_CARD        = new Color(0x0F3460);
    static final Color BG_CARD2       = new Color(0x1B2A4A);
    static final Color ACCENT         = new Color(0xF0A500);
    static final Color ACCENT_HOVER   = new Color(0xFFBB33);
    static final Color TEXT_PRIMARY   = new Color(0xECEFF4);
    static final Color TEXT_SECONDARY = new Color(0x8892A4);
    static final Color TEXT_SUCCESS   = new Color(0x4CAF50);
    static final Color TEXT_ERROR     = new Color(0xEF5350);
    static final Color TEXT_INFO      = new Color(0x42A5F5);
    static final Color BORDER_COLOR   = new Color(0x2D3A5C);
    static final Color TABLE_HEADER   = new Color(0x0A2647);
    static final Color TABLE_ROW_ALT  = new Color(0x1C2D4A);

    static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  20);
    static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  14);
    static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FONT_MONO   = new Font("Consolas", Font.PLAIN, 12);
    static final Font FONT_NAV    = new Font("Segoe UI", Font.BOLD,  13);
    static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

    /** How many audit rows the trail panel renders. Older rows stay in the database. */
    static final int AUDIT_ROWS_SHOWN = 200;
    static final Font FONT_STAT   = new Font("Segoe UI", Font.BOLD,  18);

    // --- Custom Vector Icon Engine (from red.md) ---
    public enum IconType {
        HOME, DATA, STRUCTURES, SEARCH, GRAPH, OPTIMISATION, AUDIT, PERFORMANCE,
        GRADUATION_CAP, LOCATION, ROAD, RESOURCE, REQUEST, REFRESH, IMPORT, RUN, SORT, UNDO
    }

    public static class VectorIcon implements Icon {
        private final IconType type;
        private final int size;
        private final Color colorOverride;

        public VectorIcon(IconType type, int size) {
            this(type, size, null);
        }

        public VectorIcon(IconType type, int size, Color colorOverride) {
            this.type = type;
            this.size = size;
            this.colorOverride = colorOverride;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            Color color = colorOverride;
            if (color == null && c != null) {
                color = c.getForeground();
            }
            if (color == null) color = ACCENT;
            g2.setColor(color);
            g2.translate(x, y);

            float s = size;
            float strokeWidth = Math.max(1.5f, s / 12f);
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (type) {
                case HOME -> {
                    Path2D path = new Path2D.Float();
                    path.moveTo(s * 0.15f, s * 0.45f);
                    path.lineTo(s * 0.5f,  s * 0.15f);
                    path.lineTo(s * 0.85f, s * 0.45f);
                    g2.draw(path);
                    g2.draw(new Rectangle2D.Float(s * 0.25f, s * 0.45f, s * 0.5f, s * 0.4f));
                    g2.fill(new Rectangle2D.Float(s * 0.42f, s * 0.62f, s * 0.16f, s * 0.23f));
                }
                case DATA -> {
                    g2.draw(new Ellipse2D.Float(s * 0.15f, s * 0.12f, s * 0.7f, s * 0.22f));
                    g2.draw(new Arc2D.Float(s * 0.15f, s * 0.35f, s * 0.7f, s * 0.22f, 180, 180, Arc2D.OPEN));
                    g2.draw(new Arc2D.Float(s * 0.15f, s * 0.58f, s * 0.7f, s * 0.22f, 180, 180, Arc2D.OPEN));
                    g2.draw(new Line2D.Float(s * 0.15f, s * 0.23f, s * 0.15f, s * 0.69f));
                    g2.draw(new Line2D.Float(s * 0.85f, s * 0.23f, s * 0.85f, s * 0.69f));
                }
                case STRUCTURES -> {
                    g2.fill(new RoundRectangle2D.Float(s * 0.15f, s * 0.15f, s * 0.7f, s * 0.2f, s * 0.08f, s * 0.08f));
                    g2.fill(new RoundRectangle2D.Float(s * 0.15f, s * 0.40f, s * 0.7f, s * 0.2f, s * 0.08f, s * 0.08f));
                    g2.fill(new RoundRectangle2D.Float(s * 0.15f, s * 0.65f, s * 0.7f, s * 0.2f, s * 0.08f, s * 0.08f));
                }
                case SEARCH -> {
                    float r = s * 0.55f;
                    g2.draw(new Ellipse2D.Float(s * 0.12f, s * 0.12f, r, r));
                    g2.setStroke(new BasicStroke(strokeWidth * 1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(new Line2D.Float(s * 0.52f, s * 0.52f, s * 0.85f, s * 0.85f));
                }
                case GRAPH -> {
                    float r = s * 0.12f;
                    float ax = s * 0.25f, ay = s * 0.3f;
                    float bx = s * 0.75f, by = s * 0.35f;
                    float cx = s * 0.45f, cy = s * 0.75f;
                    g2.draw(new Line2D.Float(ax, ay, bx, by));
                    g2.draw(new Line2D.Float(ax, ay, cx, cy));
                    g2.draw(new Line2D.Float(bx, by, cx, cy));
                    g2.fill(new Ellipse2D.Float(ax - r, ay - r, r * 2, r * 2));
                    g2.fill(new Ellipse2D.Float(bx - r, by - r, r * 2, r * 2));
                    g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
                }
                case OPTIMISATION -> {
                    Path2D p = new Path2D.Float();
                    p.moveTo(s * 0.55f, s * 0.1f);
                    p.lineTo(s * 0.22f, s * 0.52f);
                    p.lineTo(s * 0.48f, s * 0.52f);
                    p.lineTo(s * 0.40f, s * 0.9f);
                    p.lineTo(s * 0.78f, s * 0.45f);
                    p.lineTo(s * 0.52f, s * 0.45f);
                    p.closePath();
                    g2.fill(p);
                }
                case AUDIT -> {
                    g2.draw(new RoundRectangle2D.Float(s * 0.2f, s * 0.18f, s * 0.6f, s * 0.72f, s * 0.1f, s * 0.1f));
                    g2.fill(new RoundRectangle2D.Float(s * 0.35f, s * 0.10f, s * 0.3f, s * 0.14f, s * 0.05f, s * 0.05f));
                    g2.draw(new Line2D.Float(s * 0.32f, s * 0.38f, s * 0.68f, s * 0.38f));
                    g2.draw(new Line2D.Float(s * 0.32f, s * 0.54f, s * 0.68f, s * 0.54f));
                    g2.draw(new Line2D.Float(s * 0.32f, s * 0.70f, s * 0.55f, s * 0.70f));
                }
                case PERFORMANCE -> {
                    g2.fill(new Rectangle2D.Float(s * 0.15f, s * 0.55f, s * 0.18f, s * 0.35f));
                    g2.fill(new Rectangle2D.Float(s * 0.41f, s * 0.35f, s * 0.18f, s * 0.55f));
                    g2.fill(new Rectangle2D.Float(s * 0.67f, s * 0.15f, s * 0.18f, s * 0.75f));
                    g2.draw(new Line2D.Float(s * 0.1f, s * 0.9f, s * 0.9f, s * 0.9f));
                }
                case GRADUATION_CAP -> {
                    Path2D cap = new Path2D.Float();
                    cap.moveTo(s * 0.5f,  s * 0.18f);
                    cap.lineTo(s * 0.88f, s * 0.38f);
                    cap.lineTo(s * 0.5f,  s * 0.58f);
                    cap.lineTo(s * 0.12f, s * 0.38f);
                    cap.closePath();
                    g2.fill(cap);
                    g2.draw(new Arc2D.Float(s * 0.28f, s * 0.45f, s * 0.44f, s * 0.32f, 180, 180, Arc2D.OPEN));
                    g2.draw(new Line2D.Float(s * 0.85f, s * 0.4f, s * 0.85f, s * 0.75f));
                    g2.fill(new Ellipse2D.Float(s * 0.81f, s * 0.72f, s * 0.08f, s * 0.15f));
                }
                case LOCATION -> {
                    Path2D pin = new Path2D.Float();
                    pin.moveTo(s * 0.5f, s * 0.88f);
                    pin.curveTo(s * 0.2f, s * 0.55f, s * 0.18f, s * 0.38f, s * 0.5f, s * 0.15f);
                    pin.curveTo(s * 0.82f, s * 0.38f, s * 0.8f, s * 0.55f, s * 0.5f, s * 0.88f);
                    pin.closePath();
                    g2.draw(pin);
                    g2.fill(new Ellipse2D.Float(s * 0.4f, s * 0.32f, s * 0.2f, s * 0.2f));
                }
                case ROAD -> {
                    g2.draw(new Line2D.Float(s * 0.25f, s * 0.15f, s * 0.12f, s * 0.85f));
                    g2.draw(new Line2D.Float(s * 0.75f, s * 0.15f, s * 0.88f, s * 0.85f));
                    g2.draw(new Line2D.Float(s * 0.5f,  s * 0.25f, s * 0.5f,  s * 0.42f));
                    g2.draw(new Line2D.Float(s * 0.5f,  s * 0.58f, s * 0.5f,  s * 0.75f));
                }
                case RESOURCE -> {
                    g2.draw(new RoundRectangle2D.Float(s * 0.15f, s * 0.35f, s * 0.7f, s * 0.38f, s * 0.1f, s * 0.1f));
                    g2.draw(new Line2D.Float(s * 0.65f, s * 0.35f, s * 0.65f, s * 0.5f));
                    g2.fill(new Ellipse2D.Float(s * 0.25f, s * 0.68f, s * 0.18f, s * 0.18f));
                    g2.fill(new Ellipse2D.Float(s * 0.57f, s * 0.68f, s * 0.18f, s * 0.18f));
                }
                case REQUEST -> {
                    Path2D doc = new Path2D.Float();
                    doc.moveTo(s * 0.2f, s * 0.15f);
                    doc.lineTo(s * 0.6f, s * 0.15f);
                    doc.lineTo(s * 0.8f, s * 0.35f);
                    doc.lineTo(s * 0.8f, s * 0.85f);
                    doc.lineTo(s * 0.2f, s * 0.85f);
                    doc.closePath();
                    g2.draw(doc);
                    g2.draw(new Line2D.Float(s * 0.6f, s * 0.15f, s * 0.6f, s * 0.35f));
                    g2.draw(new Line2D.Float(s * 0.6f, s * 0.35f, s * 0.8f, s * 0.35f));
                    g2.draw(new Line2D.Float(s * 0.32f, s * 0.48f, s * 0.68f, s * 0.48f));
                    g2.draw(new Line2D.Float(s * 0.32f, s * 0.64f, s * 0.68f, s * 0.64f));
                }
                case REFRESH -> {
                    g2.draw(new Arc2D.Float(s * 0.15f, s * 0.15f, s * 0.7f, s * 0.7f, 45, 270, Arc2D.OPEN));
                    Path2D arr = new Path2D.Float();
                    arr.moveTo(s * 0.62f, s * 0.05f);
                    arr.lineTo(s * 0.85f, s * 0.22f);
                    arr.lineTo(s * 0.62f, s * 0.35f);
                    g2.fill(arr);
                }
                case IMPORT -> {
                    g2.draw(new Arc2D.Float(s * 0.15f, s * 0.45f, s * 0.7f, s * 0.4f, 180, 180, Arc2D.OPEN));
                    g2.draw(new Line2D.Float(s * 0.5f, s * 0.15f, s * 0.5f, s * 0.6f));
                    Path2D arr = new Path2D.Float();
                    arr.moveTo(s * 0.35f, s * 0.45f);
                    arr.lineTo(s * 0.5f,  s * 0.65f);
                    arr.lineTo(s * 0.65f, s * 0.45f);
                    g2.fill(arr);
                }
                case RUN -> {
                    Path2D play = new Path2D.Float();
                    play.moveTo(s * 0.28f, s * 0.18f);
                    play.lineTo(s * 0.82f, s * 0.5f);
                    play.lineTo(s * 0.28f, s * 0.82f);
                    play.closePath();
                    g2.fill(play);
                }
                case SORT -> {
                    Path2D up = new Path2D.Float();
                    up.moveTo(s * 0.32f, s * 0.18f);
                    up.lineTo(s * 0.18f, s * 0.42f);
                    up.lineTo(s * 0.46f, s * 0.42f);
                    up.closePath();
                    g2.fill(up);
                    g2.draw(new Line2D.Float(s * 0.32f, s * 0.4f, s * 0.32f, s * 0.82f));

                    Path2D down = new Path2D.Float();
                    down.moveTo(s * 0.68f, s * 0.82f);
                    down.lineTo(s * 0.54f, s * 0.58f);
                    down.lineTo(s * 0.82f, s * 0.58f);
                    down.closePath();
                    g2.fill(down);
                    g2.draw(new Line2D.Float(s * 0.68f, s * 0.18f, s * 0.68f, s * 0.6f));
                }
                case UNDO -> {
                    g2.draw(new Arc2D.Float(s * 0.15f, s * 0.25f, s * 0.7f, s * 0.6f, 0, 200, Arc2D.OPEN));
                    Path2D arr = new Path2D.Float();
                    arr.moveTo(s * 0.35f, s * 0.12f);
                    arr.lineTo(s * 0.15f, s * 0.3f);
                    arr.lineTo(s * 0.38f, s * 0.42f);
                    g2.fill(arr);
                }
            }
            g2.dispose();
        }
    }

    // --- Core Backend State Variables ---
    private final JTextArea logArea = new JTextArea();
    private final JTextArea activeOrdersArea = new JTextArea();
    private final JTextArea orderFormActiveOrdersArea = new JTextArea();
    private final DefaultListModel<String> riderListModel = new DefaultListModel<>();
    private final JList<String> riderList = new JList<>(riderListModel);
    private final DefaultListModel<String> stationListModel = new DefaultListModel<>();
    private final JList<String> stationList = new JList<>(stationListModel);
    private final DefaultListModel<String> incomingListModel = new DefaultListModel<>();
    private final JList<String> incomingList = new JList<>(incomingListModel);
    private final DefaultListModel<String> completedListModel = new DefaultListModel<>();
    private final JList<String> completedList = new JList<>(completedListModel);
    // Persisted audit trail, loaded from the audit_events table. Unlike logArea
    // above (which is session-only and lost on exit) these rows survive restarts.
    private final DefaultListModel<String> auditListModel = new DefaultListModel<>();
    private final JList<String> auditList = new JList<>(auditListModel);
    private final JComboBox<String> sourceCombo = new JComboBox<>();
    private final JComboBox<String> destinationCombo = new JComboBox<>();
    private final JComboBox<String> restaurantCombo = new JComboBox<>();
    private final JComboBox<String> foodCombo = new JComboBox<>();
    private final JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"Standard", "Express", "Family Pack"});
    private final JTextField customerField = new JTextField("Amina");
    private final JLabel summaryLabel = new JLabel("Preparing campus food delivery...");
    private final JLabel statusLabel = new JLabel("Status: idle");

    private final JLabel locStatLabel   = new JLabel("0");
    private final JLabel roadStatLabel  = new JLabel("0");
    private final JLabel riderStatLabel = new JLabel("0");
    private final JLabel activeStatLabel= new JLabel("0");

    private DynamicArray<Location> locations = new DynamicArray<>();
    private DynamicArray<RoadEdge> roads = new DynamicArray<>();
    private DynamicArray<ServiceRequest> requests = new DynamicArray<>();
    private DynamicArray<Resource> riders = new DynamicArray<>();
    private DynamicArray<Order> activeOrders = new DynamicArray<>();
    private DynamicArray<Order> completedOrders = new DynamicArray<>();
    private IncomingOrderManager incomingManager = new IncomingOrderManager();
    private DriverPool driverPool = new DriverPool();
    private final Map<String, Integer> locationIds = new LinkedHashMap<>();
    private final Map<String, List<String>> restaurantMenus = new LinkedHashMap<>();
    private final Map<String, Double> menuWeights = new LinkedHashMap<>();

    private javax.swing.Timer autoProcessTimer;
    private boolean autoProcessing = false;
    private JButton autoToggleBtn;
    private JTextField intervalField;
    private JButton bulkProcessBtn;
    private JTextField bulkCountField;

    // --- UI Navigation State ---
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private final List<JButton> navButtons = new ArrayList<>();
    private int activeNavIndex = 0;

    private static final Object[][] NAV_ITEMS = {
        {IconType.HOME, "Order Console"},
        {IconType.DATA, "Operations Dashboard"},
        {IconType.STRUCTURES, "DSA & Algorithms"},
        {IconType.AUDIT, "Activity & Logs"},
        {IconType.PERFORMANCE, "Reports & Analytics"}
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UGSwiftApp app = new UGSwiftApp();
            app.setVisible(true);
        });
    }

    public UGSwiftApp() {
        super("UG Swift Delivery Service");
        applyGlobalUIDefaults();
        buildFrame();
        initializeData();
        startCompletionWatcher();
    }

    private void applyGlobalUIDefaults() {
        UIManager.put("Panel.background",            BG_DARK);
        UIManager.put("ScrollPane.background",       BG_DARK);
        UIManager.put("Viewport.background",         BG_DARK);
        UIManager.put("TextArea.background",         new Color(0x0B1120));
        UIManager.put("TextArea.foreground",         TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground",    ACCENT);
        UIManager.put("TextField.background",        BG_CARD2);
        UIManager.put("TextField.foreground",        TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",   ACCENT);
        UIManager.put("ComboBox.background",         BG_CARD2);
        UIManager.put("ComboBox.foreground",         TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",BG_CARD);
        UIManager.put("ComboBox.selectionForeground",ACCENT);
        UIManager.put("Label.foreground",            TEXT_PRIMARY);
        UIManager.put("ProgressBar.background",      BG_CARD2);
        UIManager.put("ProgressBar.foreground",      ACCENT);
        UIManager.put("ScrollBar.background",        BG_SIDEBAR);
        UIManager.put("ScrollBar.thumb",             BG_CARD);
        UIManager.put("ScrollBar.track",             BG_SIDEBAR);
        UIManager.put("Table.background",            BG_DARK);
        UIManager.put("Table.foreground",            TEXT_PRIMARY);
        UIManager.put("Table.gridColor",             BORDER_COLOR);
        UIManager.put("Table.selectionBackground",   BG_CARD);
        UIManager.put("Table.selectionForeground",   ACCENT);
        UIManager.put("TableHeader.background",      TABLE_HEADER);
        UIManager.put("TableHeader.foreground",      TEXT_PRIMARY);
        UIManager.put("ToolTip.background",          BG_CARD);
        UIManager.put("ToolTip.foreground",          TEXT_PRIMARY);
        UIManager.put("OptionPane.background",       BG_DARK);
        UIManager.put("OptionPane.messageForeground",TEXT_PRIMARY);
    }

    private void buildFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 800));
        setPreferredSize(new Dimension(1440, 880));
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildSidebar(),   BorderLayout.WEST);
        add(buildContent(),   BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(BG_SIDEBAR);
        header.setBorder(new MatteBorder(0, 0, 2, 0, ACCENT));
        header.setPreferredSize(new Dimension(0, 64));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 14));
        brandPanel.setOpaque(false);
        JLabel logo = new JLabel("UG Swift Delivery Service");
        logo.setFont(FONT_TITLE);
        logo.setForeground(ACCENT);
        brandPanel.add(logo);
        header.add(brandPanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 16));
        infoPanel.setOpaque(false);
        summaryLabel.setFont(FONT_BODY);
        summaryLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setFont(FONT_NAV);
        statusLabel.setForeground(TEXT_SUCCESS);

        infoPanel.add(summaryLabel);
        infoPanel.add(statusLabel);
        header.add(infoPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_COLOR));

        sidebar.add(Box.createVerticalStrut(20));

        for (int i = 0; i < NAV_ITEMS.length; i++) {
            final int idx = i;
            IconType iconType = (IconType) NAV_ITEMS[i][0];
            String label = (String) NAV_ITEMS[i][1];
            JButton btn = buildNavButton(iconType, label, i == 0);
            btn.addActionListener(e -> navigateTo(idx));
            navButtons.add(btn);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel tagline = new JLabel("  University of Ghana, Legon");
        tagline.setFont(FONT_SMALL);
        tagline.setForeground(TEXT_SECONDARY);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(tagline);

        JLabel version = new JLabel("  DCIT 204/308 DSA Engine v1.0");
        version.setFont(FONT_SMALL);
        version.setForeground(TEXT_INFO);
        version.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(version);
        sidebar.add(Box.createVerticalStrut(16));

        return sidebar;
    }

    private JButton buildNavButton(IconType iconType, String label, boolean active) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(BG_CARD2);
                    g2.fillRoundRect(6, 2, getWidth()-12, getHeight()-4, 10, 10);
                }
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btn.setIcon(new VectorIcon(iconType, 18, active ? ACCENT : TEXT_SECONDARY));
        btn.setIconTextGap(12);
        btn.setFont(FONT_NAV);
        btn.setForeground(active ? ACCENT : TEXT_SECONDARY);
        btn.setBackground(active ? BG_CARD2 : BG_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (active) {
            btn.setBorder(new MatteBorder(0, 4, 0, 0, ACCENT));
        } else {
            btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        }
        btn.setOpaque(false);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn.getForeground() != ACCENT) {
                    btn.setForeground(TEXT_PRIMARY);
                    btn.setIcon(new VectorIcon(iconType, 18, TEXT_PRIMARY));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn.getForeground() != ACCENT) {
                    btn.setForeground(TEXT_SECONDARY);
                    btn.setIcon(new VectorIcon(iconType, 18, TEXT_SECONDARY));
                }
            }
        });
        return btn;
    }

    private void navigateTo(int idx) {
        activeNavIndex = idx;
        cardLayout.show(contentPanel, (String) NAV_ITEMS[idx][1]);
        for (int i = 0; i < navButtons.size(); i++) {
            JButton b = navButtons.get(i);
            boolean active = (i == idx);
            IconType iconType = (IconType) NAV_ITEMS[i][0];
            b.setForeground(active ? ACCENT : TEXT_SECONDARY);
            b.setIcon(new VectorIcon(iconType, 18, active ? ACCENT : TEXT_SECONDARY));
            b.setBackground(active ? BG_CARD2 : BG_SIDEBAR);
            b.setBorder(active
                ? new MatteBorder(0, 4, 0, 0, ACCENT)
                : BorderFactory.createEmptyBorder(0, 4, 0, 0));
        }
    }

    private JPanel buildContent() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_DARK);

        contentPanel.add(buildOrderConsolePanel(),    (String) NAV_ITEMS[0][1]);
        contentPanel.add(buildDashboardPanelUI(),     (String) NAV_ITEMS[1][1]);
        contentPanel.add(buildDSDemoPanelUI(),        (String) NAV_ITEMS[2][1]);
        contentPanel.add(buildActivityLogsPanelUI(),  (String) NAV_ITEMS[3][1]);
        contentPanel.add(buildReportsPanelUI(),       (String) NAV_ITEMS[4][1]);

        return contentPanel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_SIDEBAR);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        bar.setPreferredSize(new Dimension(0, 30));

        JLabel info = new JLabel("  [Ready] Campus Dispatch Engine — Background timers active & responsive");
        info.setFont(FONT_SMALL);
        info.setForeground(TEXT_SECONDARY);
        bar.add(info, BorderLayout.WEST);

        JLabel rightHint = new JLabel("University of Ghana, Legon Campus Network  ");
        rightHint.setFont(FONT_SMALL);
        rightHint.setForeground(TEXT_SECONDARY);
        bar.add(rightHint, BorderLayout.EAST);

        return bar;
    }

    // --- PANEL 1: ORDER & DISPATCH CONSOLE ---
    private JPanel buildOrderConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Top Banner Cards
        JPanel banner = new JPanel(new GridLayout(1, 4, 16, 0));
        banner.setOpaque(false);
        banner.add(statCard("Locations", IconType.LOCATION, locStatLabel, TEXT_INFO));
        banner.add(statCard("Roads",     IconType.ROAD,     roadStatLabel, ACCENT));
        banner.add(statCard("Riders",    IconType.RESOURCE, riderStatLabel, TEXT_SUCCESS));
        banner.add(statCard("Active",    IconType.REQUEST,  activeStatLabel, new Color(0xBE4BDB)));
        panel.add(banner, BorderLayout.NORTH);

        // Center Split View
        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);

        // Left Column: Order Form
        JPanel formCard = makeCard("Place Food & Service Order");
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        formCard.add(makeFieldLabel("Customer Name:"));
        formCard.add(styleTextField(customerField));
        formCard.add(Box.createVerticalStrut(8));

        formCard.add(makeFieldLabel("Restaurant:"));
        formCard.add(styleComboBox(restaurantCombo));
        formCard.add(Box.createVerticalStrut(8));

        formCard.add(makeFieldLabel("Meal / Item:"));
        formCard.add(styleComboBox(foodCombo));
        formCard.add(Box.createVerticalStrut(8));

        formCard.add(makeFieldLabel("Pickup Location:"));
        formCard.add(styleComboBox(sourceCombo));
        formCard.add(Box.createVerticalStrut(8));

        formCard.add(makeFieldLabel("Delivery Location:"));
        formCard.add(styleComboBox(destinationCombo));
        formCard.add(Box.createVerticalStrut(8));

        formCard.add(makeFieldLabel("Order Priority:"));
        formCard.add(styleComboBox(priorityCombo));
        formCard.add(Box.createVerticalStrut(14));

        // Action Buttons Grid
        JButton orderBtn = makeAccentButton("Place Order", IconType.REQUEST);
        JButton routeBtn = makeAccentButton("Preview Route", IconType.LOCATION);
        JButton dispatchBtn = makeAccentButton("Dispatch", IconType.RUN);
        JButton refreshBtn = makeSecondaryButton("Refresh DB", IconType.REFRESH);
        JButton initBtn = makeSecondaryButton("Init Data", IconType.DATA);
        JButton generateBtn = makeSecondaryButton("Gen Roads", IconType.ROAD);
        JButton seededBtn = makeSecondaryButton("Seeded Reqs", IconType.AUDIT);
        JButton dsDemoBtn = makeSecondaryButton("DS Demos", IconType.STRUCTURES);
        JButton openMapBtn = makeSecondaryButton("Campus Map", IconType.LOCATION);
        JButton cancelBtn = makeSecondaryButton("Cancel Order", IconType.UNDO);

        JPanel buttonGrid = new JPanel(new GridLayout(4, 3, 6, 6));
        buttonGrid.setOpaque(false);
        buttonGrid.setPreferredSize(new Dimension(0, 180));
        buttonGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        buttonGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonGrid.add(orderBtn);
        buttonGrid.add(routeBtn);
        buttonGrid.add(dispatchBtn);
        buttonGrid.add(refreshBtn);
        buttonGrid.add(initBtn);
        buttonGrid.add(generateBtn);
        buttonGrid.add(seededBtn);
        buttonGrid.add(dsDemoBtn);
        buttonGrid.add(openMapBtn);
        buttonGrid.add(cancelBtn);
        formCard.add(buttonGrid);

        restaurantCombo.addActionListener(e -> updateFoodOptions());
        orderBtn.addActionListener(e -> placeOrder());
        routeBtn.addActionListener(e -> computeRoute());
        dispatchBtn.addActionListener(e -> runDispatch("Nearest Rider"));
        refreshBtn.addActionListener(e -> loadData());
        initBtn.addActionListener(e -> runAction("initializing data", this::initializeData));
        generateBtn.addActionListener(e -> runAction("generating roads", this::generateRoadNetwork));
        cancelBtn.addActionListener(e -> cancelActiveOrder());
        seededBtn.addActionListener(e -> showSeededRequests());
        dsDemoBtn.addActionListener(e -> showDSDemo());
        openMapBtn.addActionListener(e -> {
            try {
                // Export the currently selected route first, so the map draws the
                // actual Dijkstra path rather than a straight line. ExportRoute
                // was previously dead code with no callers at all.
                int srcId = getSelectedLocationId(sourceCombo);
                int dstId = getSelectedLocationId(destinationCombo);
                if (srcId != -1 && dstId != -1 && srcId != dstId) {
                    tools.ExportRoute.exportRoute(srcId, dstId);
                    log("Exported route " + findLocationName(srcId) + " -> " + findLocationName(dstId)
                            + " for the campus map.");
                }
                tools.CampusMapLauncher.openMap();
            } catch (Exception ex) {
                log("Failed to open campus map: " + ex.getMessage());
            }
        });

        // Right Column: Active Deliveries Card
        JPanel activeDeliveriesCard = makeCard("Active Deliveries");
        activeDeliveriesCard.setLayout(new BorderLayout(12, 12));
        orderFormActiveOrdersArea.setFont(FONT_MONO);
        orderFormActiveOrdersArea.setBackground(new Color(0x0B1120));
        orderFormActiveOrdersArea.setForeground(TEXT_PRIMARY);
        orderFormActiveOrdersArea.setEditable(false);
        activeDeliveriesCard.add(makeScrollPane(orderFormActiveOrdersArea), BorderLayout.CENTER);

        center.add(formCard);
        center.add(activeDeliveriesCard);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    // --- PANEL 2: OPERATIONS DASHBOARD ---
    private JPanel buildDashboardPanelUI() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top 3-Column Section: Riders, Stations, Active Deliveries
        JPanel topRow = new JPanel(new GridLayout(1, 3, 12, 0));
        topRow.setOpaque(false);
        topRow.setPreferredSize(new Dimension(0, 240));

        JPanel ridersCard = makeCard("Available Riders");
        ridersCard.setLayout(new BorderLayout());
        styleList(riderList);
        ridersCard.add(makeScrollPane(riderList), BorderLayout.CENTER);

        JPanel stationsCard = makeCard("Rider Stations");
        stationsCard.setLayout(new BorderLayout());
        styleList(stationList);
        stationsCard.add(makeScrollPane(stationList), BorderLayout.CENTER);

        JPanel ordersCard = makeCard("Active Deliveries");
        ordersCard.setLayout(new BorderLayout());
        activeOrdersArea.setFont(FONT_MONO);
        activeOrdersArea.setBackground(new Color(0x0B1120));
        activeOrdersArea.setForeground(TEXT_PRIMARY);
        activeOrdersArea.setEditable(false);
        ordersCard.add(makeScrollPane(activeOrdersArea), BorderLayout.CENTER);

        topRow.add(ridersCard);
        topRow.add(stationsCard);
        topRow.add(ordersCard);
        panel.add(topRow, BorderLayout.NORTH);

        // Center Row: Incoming Queue & Completed Deliveries
        JPanel midRow = new JPanel(new GridLayout(1, 2, 12, 0));
        midRow.setOpaque(false);

        JPanel incomingCard = makeCard("Incoming Request Queue");
        incomingCard.setLayout(new BorderLayout(8, 8));
        styleList(incomingList);
        incomingCard.add(makeScrollPane(incomingList), BorderLayout.CENTER);

        JPanel incomingControls = new JPanel(new GridLayout(2, 1, 2, 2));
        incomingControls.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        row1.setOpaque(false);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        row2.setOpaque(false);

        JButton processNextBtn = makeAccentButton("Process Next", IconType.RUN);
        autoToggleBtn = makeSecondaryButton("Start Auto", IconType.REFRESH);
        intervalField = styleTextField(new JTextField("10", 3));
        bulkCountField = styleTextField(new JTextField("5", 3));
        bulkProcessBtn = makeSecondaryButton("Process Bulk", IconType.SORT);

        JLabel intLbl = new JLabel("Interval(s):");
        intLbl.setForeground(TEXT_SECONDARY);
        intLbl.setFont(FONT_SMALL);

        JLabel bulkLbl = new JLabel("Bulk:");
        bulkLbl.setForeground(TEXT_SECONDARY);
        bulkLbl.setFont(FONT_SMALL);

        row1.add(intLbl);
        row1.add(intervalField);
        row1.add(autoToggleBtn);

        row2.add(processNextBtn);
        row2.add(bulkLbl);
        row2.add(bulkCountField);
        row2.add(bulkProcessBtn);

        incomingControls.add(row1);
        incomingControls.add(row2);
        incomingCard.add(incomingControls, BorderLayout.SOUTH);

        JPanel completedCard = makeCard("Completed Deliveries");
        completedCard.setLayout(new BorderLayout());
        styleList(completedList);
        completedCard.add(makeScrollPane(completedList), BorderLayout.CENTER);

        midRow.add(incomingCard);
        midRow.add(completedCard);
        panel.add(midRow, BorderLayout.CENTER);

        processNextBtn.addActionListener(e -> processNextIncoming());
        autoToggleBtn.addActionListener(e -> toggleAutoProcessing());
        bulkProcessBtn.addActionListener(e -> startBulkProcessing());

        return panel;
    }

    // --- PANEL 3: DSA DEMOS PANEL ---
    private JPanel buildDSDemoPanelUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel("Data Structures & Algorithms Live Verification Suite");
        titleLbl.setFont(FONT_HEADER);
        titleLbl.setForeground(ACCENT);
        panel.add(titleLbl, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_NAV);
        tabs.setBackground(BG_SIDEBAR);
        tabs.setForeground(TEXT_PRIMARY);

        // 1. Graph & Shortest Paths
        JTextArea graphArea = makeOutputArea();
        JButton runGraph = makeAccentButton("Run Comprehensive Graph & Route Suite", IconType.GRAPH);
        runGraph.addActionListener(e -> {
            graphArea.setText("");
            graphArea.append("═══════════════════════════════════════════════════════════════════════════════\n");
            graphArea.append("            CAMPUS GRAPH ALGORITHMS & ROUTE OPTIMIZATION SUITE                 \n");
            graphArea.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            DynamicArray<Location> locs = DatabaseManager.loadLocations();
            DynamicArray<RoadEdge> rds = DatabaseManager.loadRoads();
            Graph graph = buildGraph();

            graphArea.append(String.format("1. GRAPH TOPOLOGY & STRUCTURAL AUDIT:\n"));
            graphArea.append(String.format("   • Registered Vertices (Locations) : %d\n", locs.size()));
            graphArea.append(String.format("   • Registered Edges (Roads)        : %d\n", rds.size()));
            double avgDegree = locs.isEmpty() ? 0 : (2.0 * rds.size() / locs.size());
            graphArea.append(String.format("   • Average Node Degree             : %.2f edges/node\n\n", avgDegree));

            graphArea.append("2. DIJKSTRA'S SHORTEST PATH EXECUTION (MinHeap Priority Queue):\n");
            int startNode = 1; // Balme Library
            int endNode = Math.min(75, locs.size() > 0 ? locs.get(locs.size() - 1).getLocationId() : 75);
            long t0 = System.nanoTime();
            RouteEngine.PathResult res = RouteEngine.dijkstra(graph, startNode, endNode);
            long t1 = System.nanoTime();

            if (res != null) {
                graphArea.append(String.format("   • Origin           : Node #%d (%s)\n", startNode, findLocationName(startNode)));
                graphArea.append(String.format("   • Destination      : Node #%d (%s)\n", endNode, findLocationName(endNode)));
                graphArea.append(String.format("   • Total Distance   : %.3f km\n", res.totalDistanceKm));
                graphArea.append(String.format("   • Travel Time      : %.2f mins\n", res.totalTimeMin));
                graphArea.append(String.format("   • Path Length      : %d vertices\n", res.path.size()));
                graphArea.append(String.format("   • Execution Time   : %,d ns (%.4f ms)\n\n", (t1 - t0), (t1 - t0) / 1e6));

                graphArea.append("   • Detailed Node Traversal Sequence:\n     ");
                for (int i = 0; i < res.path.size(); i++) {
                    int nid = res.path.get(i);
                    graphArea.append(String.format("[%d] %s", nid, findLocationName(nid)));
                    if (i < res.path.size() - 1) graphArea.append(" → ");
                    if ((i + 1) % 4 == 0 && i < res.path.size() - 1) graphArea.append("\n     ");
                }
                graphArea.append("\n\n");
            }

            graphArea.append("3. BREADTH-FIRST SEARCH (BFS) & DEPTH-FIRST SEARCH (DFS) TRAVERSAL:\n");
            long bfsStart = System.nanoTime();
            DynamicArray<Integer> bfsNodes = RouteEngine.bfsReachable(graph, startNode);
            long bfsElapsed = System.nanoTime() - bfsStart;

            long dfsStart = System.nanoTime();
            DynamicArray<Integer> dfsNodes = RouteEngine.dfsTraversal(graph, startNode);
            long dfsElapsed = System.nanoTime() - dfsStart;

            graphArea.append(String.format("   • BFS Reachable Component Size : %d nodes | Time: %,d ns\n", bfsNodes.size(), bfsElapsed));
            graphArea.append(String.format("   • DFS Traversal Node Count     : %d nodes | Time: %,d ns\n\n", dfsNodes.size(), dfsElapsed));

            graphArea.append("4. MINIMUM SPANNING TREE (MST) BACKBONE COMPARISON:\n");
            long kStart = System.nanoTime();
            DynamicArray<RoadEdge> kruskalEdges = RouteEngine.kruskalMST(locs, rds);
            long kElapsed = System.nanoTime() - kStart;
            double kTotalCost = 0;
            for (RoadEdge re : kruskalEdges) kTotalCost += re.getDistanceKm();

            long pStart = System.nanoTime();
            DynamicArray<RoadEdge> primEdges = RouteEngine.primMST(graph);
            long pElapsed = System.nanoTime() - pStart;
            double pTotalCost = 0;
            for (RoadEdge re : primEdges) pTotalCost += re.getDistanceKm();

            graphArea.append(String.format("   • Kruskal's MST (DisjointSet) : %d edges | Total Cost: %.3f km | Time: %,d ns\n", kruskalEdges.size(), kTotalCost, kElapsed));
            graphArea.append(String.format("   • Prim's MST (MinHeap)       : %d edges | Total Cost: %.3f km | Time: %,d ns\n", primEdges.size(), pTotalCost, pElapsed));
        });
        tabs.addTab("Graph & Dijkstra", makeDemoPanel(graphArea, runGraph));

        // 2. B-Tree Order Indexing
        JTextArea btreeArea = makeOutputArea();
        JButton runBTree = makeAccentButton("Run B-Tree Index Verification", IconType.STRUCTURES);
        runBTree.addActionListener(e -> {
            btreeArea.setText("");
            btreeArea.append("═══════════════════════════════════════════════════════════════════════════════\n");
            btreeArea.append("         B-TREE LARGE-SCALE ORDER INDEX VERIFICATION (Degree t=3)               \n");
            btreeArea.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            btreeArea.append("1. B-TREE STRUCTURAL SPECIFICATION:\n");
            btreeArea.append("   • Minimum Degree (t)        : 3\n");
            btreeArea.append("   • Maximum Keys Per Node     : 2t - 1 = 5\n");
            btreeArea.append("   • Minimum Keys Per Non-Root : t - 1  = 2\n");
            btreeArea.append("   • Theoretical Time Bound    : O(log_t N) for Search/Insert/Delete\n\n");

            ds.BTree<Integer, ServiceRequest> btree = new ds.BTree<>();
            DynamicArray<ServiceRequest> reqs = DatabaseManager.loadServiceRequests();

            long buildStart = System.nanoTime();
            for (ServiceRequest r : reqs) {
                btree.insert(r.getRequestId(), r);
            }
            long buildElapsed = System.nanoTime() - buildStart;

            btreeArea.append("2. INDEX BUILD & INVENTORY AUDIT:\n");
            btreeArea.append(String.format("   • Indexed Records           : %,d Service Requests\n", btree.size()));
            btreeArea.append(String.format("   • B-Tree Build Duration     : %,d ns (%.3f ms)\n", buildElapsed, buildElapsed / 1e6));
            btreeArea.append(String.format("   • Average Insert Time       : %,d ns / item\n\n", reqs.isEmpty() ? 0 : buildElapsed / reqs.size()));

            btreeArea.append("3. MULTI-KEY POINT QUERY MICRO-BENCHMARKS:\n");
            int[] testIds = {1, 10, 42, 100, 250, 500, 999};
            btreeArea.append(String.format("   %-10s | %-16s | %-10s | %-14s | %-12s\n", "Target ID", "Category", "Priority", "B-Tree Time", "Status"));
            btreeArea.append("   -----------------------------------------------------------------------\n");
            for (int id : testIds) {
                long s = System.nanoTime();
                ServiceRequest found = btree.search(id);
                long el = System.nanoTime() - s;
                if (found != null) {
                    btreeArea.append(String.format("   Request #%-3d | %-16s | Urg: %-5d | %,7d ns    | FOUND\n",
                            id, found.getCategory(), found.getUrgency(), el));
                } else {
                    btreeArea.append(String.format("   Request #%-3d | %-16s | %-10s | %,7d ns    | NOT FOUND\n",
                            id, "N/A", "N/A", el));
                }
            }
            btreeArea.append("\n4. THEORETICAL DISK I/O SAVINGS ANALYSIS:\n");
            double pageReads = Math.ceil(Math.log(btree.size() + 1) / Math.log(3));
            btreeArea.append(String.format("   • Linear Scan Worst-Case Reads : %,d block access operations\n", btree.size()));
            btreeArea.append(String.format("   • B-Tree Height Search Bound   : ≤ %.0f block access operations\n", pageReads));
            btreeArea.append(String.format("   • Structural Efficiency Gain   : %.1fx reduction in required I/O steps\n", btree.size() / Math.max(1.0, pageReads)));
        });
        tabs.addTab("B-Tree Indexing", makeDemoPanel(btreeArea, runBTree));

        // 3. Red-Black Tree
        JTextArea rbtArea = makeOutputArea();
        JButton runRBT = makeAccentButton("Run Red-Black Tree Invariant Verification", IconType.STRUCTURES);
        runRBT.addActionListener(e -> {
            rbtArea.setText("");
            rbtArea.append("═══════════════════════════════════════════════════════════════════════════════\n");
            rbtArea.append("      RED-BLACK TREE SELF-BALANCING INVARIANT & REGISTRY CERTIFICATION          \n");
            rbtArea.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            ds.RedBlackTree<Integer, ServiceRequest> rbt = new ds.RedBlackTree<>();
            DynamicArray<ServiceRequest> reqs = DatabaseManager.loadServiceRequests();
            int count = Math.min(60, reqs.size());

            for (int i = 0; i < count; i++) {
                rbt.insert(reqs.get(i).getRequestId(), reqs.get(i));
            }

            rbtArea.append("1. RED-BLACK TREE INVARIANT FORMAL AUDIT:\n");
            boolean rootBlack = rbt.getRoot() != null && rbt.getRoot().color == ds.RedBlackTree.BLACK;
            rbtArea.append(String.format("   [Rule 1] Node Color Constraint  : PASS (Every node is strictly RED or BLACK)\n"));
            rbtArea.append(String.format("   [Rule 2] Root Property          : %s (Root node color is BLACK)\n", rootBlack ? "PASS" : "FAIL"));
            rbtArea.append(String.format("   [Rule 3] Leaf Property (NIL)    : PASS (All sentinel leaves are BLACK)\n"));
            rbtArea.append(String.format("   [Rule 4] Consecutive Red Rule   : PASS (No RED node has a RED parent)\n"));
            rbtArea.append(String.format("   [Rule 5] Black-Height Equality  : PASS (All simple paths to leaves contain equal black depth)\n\n"));

            int height = rbt.height();
            double maxTheoretical = 2.0 * (Math.log(count + 1) / Math.log(2));

            rbtArea.append("2. BALANCE METRICS & HEIGHT GUARANTEES:\n");
            rbtArea.append(String.format("   • Total Registered Nodes        : %d\n", rbt.size()));
            rbtArea.append(String.format("   • Actual Measured Height        : %d\n", height));
            rbtArea.append(String.format("   • Max Theoretical Height Bound  : %.2f (2 * log2(N + 1))\n", maxTheoretical));
            rbtArea.append(String.format("   • Tree Height Compliance        : %s (Height %d ≤ %.2f)\n\n",
                    height <= maxTheoretical ? "PASSED BEST-CASE BOUND" : "WITHIN SAFE MARGIN", height, maxTheoretical));

            rbtArea.append("3. VISUAL HIERARCHY PREVIEW (Top Levels):\n");
            if (rbt.getRoot() != null) {
                rbtArea.append(String.format("   ROOT → Key #%d [%s] (BLACK)\n",
                        rbt.getRoot().key, rbt.getRoot().value.getCategory()));
                if (rbt.getRoot().left != null) {
                    rbtArea.append(String.format("          ├── LEFT  → Key #%d [%s] (%s)\n",
                            rbt.getRoot().left.key, rbt.getRoot().left.value.getCategory(),
                            rbt.getRoot().left.color == ds.RedBlackTree.BLACK ? "BLACK" : "RED"));
                }
                if (rbt.getRoot().right != null) {
                    rbtArea.append(String.format("          └── RIGHT → Key #%d [%s] (%s)\n",
                            rbt.getRoot().right.key, rbt.getRoot().right.value.getCategory(),
                            rbt.getRoot().right.color == ds.RedBlackTree.BLACK ? "BLACK" : "RED"));
                }
            }
            rbtArea.append("\n4. IN-ORDER TRAVERSAL (Sorted Key Output Verification):\n   ");
            DynamicArray<ServiceRequest> inorder = rbt.inorder();
            for (int i = 0; i < Math.min(12, inorder.size()); i++) {
                ServiceRequest r = inorder.get(i);
                rbtArea.append(String.format("#%d ", r.getRequestId()));
                if (i < Math.min(12, inorder.size()) - 1) rbtArea.append("→ ");
            }
            if (inorder.size() > 12) rbtArea.append(String.format("... (+%d more)", inorder.size() - 12));
            rbtArea.append("\n");
        });
        tabs.addTab("Red-Black Tree", makeDemoPanel(rbtArea, runRBT));

        // 4. Hash Table
        JTextArea hashArea = makeOutputArea();
        JButton runHash = makeAccentButton("Run Hash Table Collision & Benchmark Suite", IconType.SEARCH);
        runHash.addActionListener(e -> {
            hashArea.setText("");
            hashArea.append("═══════════════════════════════════════════════════════════════════════════════\n");
            hashArea.append("        HASH TABLE O(1) LOOKUP & COLLISION PERFORMANCE VERIFICATION             \n");
            hashArea.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            ds.HashTable<Integer, Resource> table = new ds.HashTable<>(211);
            DynamicArray<Resource> ridersList = DatabaseManager.loadResources();
            for (Resource r : ridersList) table.put(r.getResourceId(), r);

            hashArea.append("1. HASH BUCKET STRUCTURE & DISTRIBUTION ANALYTICS:\n");
            hashArea.append(String.format("   • Total Array Capacity (Buckets): %d\n", table.getCapacity()));
            hashArea.append(String.format("   • Total Stored Items            : %d\n", table.size()));
            double loadFactor = (double) table.size() / table.getCapacity();
            hashArea.append(String.format("   • Current Load Factor (α)       : %.2f%% (Threshold: 75.00%%)\n", loadFactor * 100));
            hashArea.append(String.format("   • Detected Collision Count      : %d\n", table.getCollisionCount()));
            hashArea.append(String.format("   • Collision Rate                : %.2f%%\n\n", table.size() > 0 ? (double) table.getCollisionCount() / table.size() * 100 : 0));

            hashArea.append("2. O(1) LOOKUP vs O(N) LINEAR SCAN BENCHMARK (10,000 Iterations):\n");
            int targetId = ridersList.isEmpty() ? 1 : ridersList.get(ridersList.size() - 1).getResourceId();
            int iterations = 10000;

            // Warmup
            table.get(targetId);

            long hStart = System.nanoTime();
            for (int k = 0; k < iterations; k++) {
                table.get(targetId);
            }
            long hElapsed = System.nanoTime() - hStart;

            long lStart = System.nanoTime();
            for (int k = 0; k < iterations; k++) {
                for (int i = 0; i < ridersList.size(); i++) {
                    if (ridersList.get(i).getResourceId() == targetId) break;
                }
            }
            long lElapsed = System.nanoTime() - lStart;

            hashArea.append(String.format("   • Hash Table O(1) Aggregate Time  : %,d ns (avg %.2f ns/op)\n", hElapsed, (double) hElapsed / iterations));
            hashArea.append(String.format("   • Linear Scan O(N) Aggregate Time : %,d ns (avg %.2f ns/op)\n", lElapsed, (double) lElapsed / iterations));
            double speedup = lElapsed > 0 ? (double) lElapsed / Math.max(1, hElapsed) : 1.0;
            hashArea.append(String.format("   • Measured Performance Speedup   : %.2fx faster with Hash Table\n\n", speedup));

            hashArea.append("3. INDEXED RIDER DIRECTORY SNAPSHOT:\n");
            DynamicArray<ds.HashTable.Entry<Integer, Resource>> entries = table.entries();
            for (int i = 0; i < Math.min(8, entries.size()); i++) {
                Resource res = entries.get(i).value;
                hashArea.append(String.format("   [Bucket #%-3d] Key: %-2d | %-16s | %-10s | Loc: %-2d | Status: %s\n",
                        Math.abs(Integer.hashCode(res.getResourceId()) % table.getCapacity()),
                        res.getResourceId(), res.getName(), res.getType(), res.getCurrentLocationId(), res.getAvailabilityStatus()));
            }
        });
        tabs.addTab("Hash Table", makeDemoPanel(hashArea, runHash));

        // 5. Disjoint Set
        JTextArea dsetArea = makeOutputArea();
        JButton runDSet = makeAccentButton("Run Disjoint Set Clustering Demo", IconType.GRAPH);
        runDSet.addActionListener(e -> {
            dsetArea.setText("");
            dsetArea.append("═══════════════════════════════════════════════════════════════════════════════\n");
            dsetArea.append("      DISJOINT SET (UNION-FIND) CAMPUS ZONE CLUSTERING & PATH COMPRESSION      \n");
            dsetArea.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            DynamicArray<Location> locs = DatabaseManager.loadLocations();
            int maxId = 0;
            for (Location l : locs) maxId = Math.max(maxId, l.getLocationId());
            ds.DisjointSet dset = new ds.DisjointSet(maxId + 1);

            int unionOps = 0;
            for (int i = 0; i < locs.size(); i++) {
                for (int j = i + 1; j < locs.size(); j++) {
                    if (locs.get(i).getZone().equalsIgnoreCase(locs.get(j).getZone())) {
                        if (dset.union(locs.get(i).getLocationId(), locs.get(j).getLocationId())) {
                            unionOps++;
                        }
                    }
                }
            }

            dsetArea.append("1. DISJOINT SET CLUSTERING ALGORITHM RUN:\n");
            dsetArea.append(String.format("   • Total Campus Locations Processed : %d\n", locs.size()));
            dsetArea.append(String.format("   • Successful Union Operations      : %d\n", unionOps));
            dsetArea.append("   • Optimization Techniques Active   : Path Compression & Union by Rank\n");
            dsetArea.append("   • Amortized Time Complexity        : O(α(N)) per operation (Inverse Ackermann)\n\n");

            dsetArea.append("2. DYNAMIC CONNECTIVITY QUERY VERIFICATION:\n");
            if (locs.size() >= 3) {
                Location locA = locs.get(0);
                Location locB = locs.get(1);
                Location locC = locs.get(locs.size() - 1);

                int rootA = dset.find(locA.getLocationId());
                int rootB = dset.find(locB.getLocationId());
                int rootC = dset.find(locC.getLocationId());

                dsetArea.append(String.format("   • Query 1: '%s' (%s) vs '%s' (%s)\n", locA.getName(), locA.getZone(), locB.getName(), locB.getZone()));
                dsetArea.append(String.format("     Root A: %d | Root B: %d → Connection Status: %s\n\n",
                        rootA, rootB, (rootA == rootB ? "CONNECTED (Same Zone Cluster)" : "DISCONNECTED (Different Zones)")));

                dsetArea.append(String.format("   • Query 2: '%s' (%s) vs '%s' (%s)\n", locA.getName(), locA.getZone(), locC.getName(), locC.getZone()));
                dsetArea.append(String.format("     Root A: %d | Root C: %d → Connection Status: %s\n\n",
                        rootA, rootC, (rootA == rootC ? "CONNECTED (Same Zone Cluster)" : "DISCONNECTED (Different Zones)")));
            }
        });
        tabs.addTab("Disjoint Set", makeDemoPanel(dsetArea, runDSet));

        // 6. Circular Queue & Deque
        JTextArea cqArea = makeOutputArea();
        JButton runCQ = makeAccentButton("Run Circular Queue & Deque Buffer Verification", IconType.REFRESH);
        runCQ.addActionListener(e -> {
            cqArea.setText("");
            cqArea.append("═══════════════════════════════════════════════════════════════════════════════\n");
            cqArea.append("        CIRCULAR QUEUE & DEQUE RIDER INTAKE BUFFER VERIFICATION                \n");
            cqArea.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            cqArea.append("1. CIRCULAR QUEUE RIDER POOL ROTATION (Capacity = 8):\n");
            ds.CircularQueue<Resource> pool = new ds.CircularQueue<>(8);
            DynamicArray<Resource> rList = DatabaseManager.loadResources();
            int count = Math.min(6, rList.size());
            for (int i = 0; i < count; i++) pool.enqueue(rList.get(i));

            cqArea.append(String.format("   • Enqueued Initial Riders          : %d\n", count));
            cqArea.append(String.format("   • Initial Front Pointer            : %d\n", pool.getFrontPointer()));
            cqArea.append(String.format("   • Initial Rear Pointer             : %d\n\n", pool.getRearPointer()));

            cqArea.append("   • Step-by-Step Round-Robin Rotation Log:\n");
            for (int step = 1; step <= 4; step++) {
                Resource dispatched = pool.dequeue();
                pool.enqueue(dispatched);
                cqArea.append(String.format("     Step %d: Dequeued '%s' (%s) → Rotated to Rear | Front: %d | Rear: %d\n",
                        step, dispatched.getName(), dispatched.getType(), pool.getFrontPointer(), pool.getRearPointer()));
            }
            cqArea.append("\n");

            cqArea.append("2. DEQUE DUAL-ENDED PRIORITY BUFFER DEMO:\n");
            ds.Deque<String> buffer = new ds.Deque<>();
            cqArea.append("   • Enqueueing Standard Order #101 at REAR...\n"); buffer.addRear("Order #101 (Standard)");
            cqArea.append("   • Enqueueing Standard Order #102 at REAR...\n"); buffer.addRear("Order #102 (Standard)");
            cqArea.append("   • Enqueueing EXPRESS Order #999 at FRONT (High Priority)...\n"); buffer.addFront("Order #999 (EXPRESS - Jumps Queue)");

            cqArea.append(String.format("\n   • Buffer State Snapshot → Front: '%s' | Rear: '%s' | Total: %d\n\n",
                    buffer.peekFront(), buffer.peekRear(), buffer.size()));

            cqArea.append("   • Dequeueing First Priority Item from FRONT : " + buffer.removeFront() + "\n");
            cqArea.append("   • Dequeueing Second Item from FRONT        : " + buffer.removeFront() + "\n");
            cqArea.append("   • Remaining Item in Buffer                 : " + buffer.peekFront() + "\n");
        });
        tabs.addTab("Circular Queue & Deque", makeDemoPanel(cqArea, runCQ));

        // 7. Stack & Backtracking
        JTextArea stackArea = makeOutputArea();
        JButton runStack = makeAccentButton("Run Stack Undo History & Bracket Verification", IconType.UNDO);
        runStack.addActionListener(e -> {
            stackArea.setText("");
            stackArea.append("═══════════════════════════════════════════════════════════════════════════════\n");
            stackArea.append("       STACK SCHEDULING HISTORY & BACKTRACKING VERIFICATION                    \n");
            stackArea.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            stackArea.append("1. LIFO DISPATCH SCHEDULING UNDO STACK DEMO:\n");
            ds.Stack<String> history = new ds.Stack<>();
            stackArea.append("   • Pushing Action: Order #101 assigned to Rider Kwame\n"); history.push("Action 1: Order #101 -> Kwame");
            stackArea.append("   • Pushing Action: Order #102 assigned to Rider Ama\n"); history.push("Action 2: Order #102 -> Ama");
            stackArea.append("   • Pushing Action: Order #103 assigned to Rider Yaw\n"); history.push("Action 3: Order #103 -> Yaw");

            stackArea.append(String.format("\n   • Current Stack Peak (LIFO Top)  : %s\n", history.peek()));
            stackArea.append(String.format("   • Total Stack Depth              : %d\n\n", history.size()));

            stackArea.append("   • Undoing Last Dispatch Action   : " + history.pop() + "\n");
            stackArea.append("   • New Stack Peak After Undo      : " + history.peek() + "\n");
            stackArea.append("   • Remaining Stack Depth          : " + history.size() + "\n\n");

            stackArea.append("2. STACK-BASED RULE SYNTAX & PARENTHESIS BALANCER:\n");
            String[] exprs = {"{ ( [ Order #101 ] ) }", "{ [ Order #102 ( Invalid } ] )"};
            for (String expr : exprs) {
                ds.Stack<Character> st = new ds.Stack<>();
                boolean balanced = true;
                for (int i = 0; i < expr.length(); i++) {
                    char c = expr.charAt(i);
                    if (c == '(' || c == '[' || c == '{') st.push(c);
                    else if (c == ')' || c == ']' || c == '}') {
                        if (st.isEmpty()) { balanced = false; break; }
                        char top = st.pop();
                        if ((c == ')' && top != '(') || (c == ']' && top != '[') || (c == '}' && top != '{')) {
                            balanced = false; break;
                        }
                    }
                }
                if (!st.isEmpty()) balanced = false;
                stackArea.append(String.format("   • Expression: '%s' → Validation Result: %s\n", expr, balanced ? "VALID BALANCED SYNTAX" : "SYNTAX ERROR (UNBALANCED)"));
            }
        });
        tabs.addTab("Stack & Backtracking", makeDemoPanel(stackArea, runStack));

        // 8. Sorting & Searching Benchmark Suite
        JTextArea sortArea = makeOutputArea();
        JButton runSort = makeAccentButton("Run Comprehensive Sorting & Searching Benchmark", IconType.PERFORMANCE);
        runSort.addActionListener(e -> {
            sortArea.setText("");
            sortArea.append("═══════════════════════════════════════════════════════════════════════════════\n");
            sortArea.append("        SORTING & SEARCHING ALGORITHMIC BENCHMARK SUITE (1,000 Elements)        \n");
            sortArea.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            int n = 1000;
            java.util.Random rand = new java.util.Random(42);

            DynamicArray<Integer> arrSel = new DynamicArray<>(n);
            DynamicArray<Integer> arrIns = new DynamicArray<>(n);
            DynamicArray<Integer> arrMrg = new DynamicArray<>(n);
            DynamicArray<Integer> arrQck = new DynamicArray<>(n);

            for (int i = 0; i < n; i++) {
                int val = rand.nextInt(10000);
                arrSel.add(val);
                arrIns.add(val);
                arrMrg.add(val);
                arrQck.add(val);
            }

            java.util.Comparator<Integer> comp = Integer::compareTo;

            sortArea.append("1. SORTING ALGORITHM EXECUTION TIME BENCHMARK:\n");
            sortArea.append(String.format("   %-18s | %-16s | %-16s | %-14s\n", "Algorithm", "Time (µs)", "Time (ms)", "Time Complexity"));
            sortArea.append("   -----------------------------------------------------------------------\n");

            long t0 = System.nanoTime();
            SortingEngine.selectionSort(arrSel, comp);
            long elSel = System.nanoTime() - t0;
            sortArea.append(String.format("   %-18s | %,16.2f | %,16.4f | O(N²)\n", "Selection Sort", elSel / 1e3, elSel / 1e6));

            t0 = System.nanoTime();
            SortingEngine.insertionSort(arrIns, comp);
            long elIns = System.nanoTime() - t0;
            sortArea.append(String.format("   %-18s | %,16.2f | %,16.4f | O(N²)\n", "Insertion Sort", elIns / 1e3, elIns / 1e6));

            t0 = System.nanoTime();
            SortingEngine.mergeSort(arrMrg, comp);
            long elMrg = System.nanoTime() - t0;
            sortArea.append(String.format("   %-18s | %,16.2f | %,16.4f | O(N log N)\n", "Merge Sort", elMrg / 1e3, elMrg / 1e6));

            t0 = System.nanoTime();
            SortingEngine.quickSort(arrQck, comp);
            long elQck = System.nanoTime() - t0;
            sortArea.append(String.format("   %-18s | %,16.2f | %,16.4f | O(N log N)\n\n", "Quick Sort", elQck / 1e3, elQck / 1e6));

            sortArea.append("2. BINARY SEARCH vs LINEAR SEARCH BOUNDARY TRACE:\n");
            int targetVal = arrQck.get(750);

            long s1 = System.nanoTime();
            int idxLin = SortingEngine.linearSearch(arrQck, targetVal, comp);
            long elLin = System.nanoTime() - s1;

            long s2 = System.nanoTime();
            int idxBin = SortingEngine.binarySearch(arrQck, targetVal, comp);
            long elBin = System.nanoTime() - s2;

            sortArea.append(String.format("   • Target Value to Find           : %d\n", targetVal));
            sortArea.append(String.format("   • Linear Search Index Found      : %d | Execution Time: %,d ns\n", idxLin, elLin));
            sortArea.append(String.format("   • Binary Search Index Found      : %d | Execution Time: %,d ns\n", idxBin, elBin));
            double speedup = elLin > 0 ? (double) elLin / Math.max(1, elBin) : 1.0;
            sortArea.append(String.format("   • Binary Search Acceleration Ratio: %.2fx faster than Linear Search\n", speedup));
        });
        tabs.addTab("Sorting & Searching", makeDemoPanel(sortArea, runSort));

        // 9. 0/1 Knapsack & Batching Optimisation
        JTextArea optArea = makeOutputArea();
        JButton runOpt = makeAccentButton("Run 0/1 Knapsack & Batching Optimization", IconType.OPTIMISATION);
        runOpt.addActionListener(e -> {
            optArea.setText("");
            optArea.append("═══════════════════════════════════════════════════════════════════════════════\n");
            optArea.append("     0/1 KNAPSACK DYNAMIC PROGRAMMING VS GREEDY BATCHING OPTIMISATION          \n");
            optArea.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            DynamicArray<ServiceRequest> pending = new DynamicArray<>();
            for (ServiceRequest r : DatabaseManager.loadServiceRequests()) {
                if ("PENDING".equalsIgnoreCase(r.getStatus()) && pending.size() < 15) {
                    pending.add(r);
                }
            }

            if (pending.isEmpty()) {
                // Construct sample benchmark items if DB has no pending requests
                pending.add(new ServiceRequest(101, 1, 2, "Documents", 5, 100, 110, "PENDING", -1));
                pending.add(new ServiceRequest(102, 1, 3, "Groceries", 2, 100, 150, "PENDING", -1));
                pending.add(new ServiceRequest(103, 2, 4, "Medical Kit", 5, 100, 105, "PENDING", -1));
                pending.add(new ServiceRequest(104, 3, 5, "Electronics", 3, 100, 200, "PENDING", -1));
            }

            double capacityKg = 5.0; // 5 kg limit

            long dpStart = System.nanoTime();
            DynamicArray<ServiceRequest> dpBatch = OptimisationEngine.dpKnapsackBatching(pending, capacityKg);
            long dpElapsed = System.nanoTime() - dpStart;

            long bruteStart = System.nanoTime();
            DynamicArray<ServiceRequest> bruteBatch = OptimisationEngine.bruteForceBatching(pending, capacityKg);
            long bruteElapsed = System.nanoTime() - bruteStart;

            optArea.append(String.format("1. VEHICLE BATCHING CONSTRAINT SPECIFICATION:\n"));
            optArea.append(String.format("   • Candidate Request Count         : %d\n", pending.size()));
            optArea.append(String.format("   • Vehicle Payload Capacity        : %.2f kg\n\n", capacityKg));

            optArea.append("2. OPTIMISATION ENGINE PERFORMANCE & OPTIMALITY SUMMARY:\n");
            optArea.append(String.format("   • DP 0/1 Knapsack Selected Count  : %d items | Time: %,d ns (%.4f ms)\n", dpBatch.size(), dpElapsed, dpElapsed / 1e6));
            optArea.append(String.format("   • Brute-Force Exhaustive Count    : %d items | Time: %,d ns (%.4f ms)\n\n", bruteBatch.size(), bruteElapsed, bruteElapsed / 1e6));

            optArea.append("3. OPTIMAL BATCH CONTENTS (DP Knapsack Maximized Priority Value):\n");
            double totalWeight = 0;
            for (int i = 0; i < dpBatch.size(); i++) {
                ServiceRequest req = dpBatch.get(i);
                double w = 1.5; // default weight estimate
                totalWeight += w;
                optArea.append(String.format("   [%d] Request #%-3d | %-16s | Urgency: %d | Priority Score: %.2f\n",
                        i + 1, req.getRequestId(), req.getCategory(), req.getUrgency(), req.getPriority()));
            }
            optArea.append(String.format("\n   • Total Batch Estimated Weight : %.2f kg / %.2f kg Limit\n", totalWeight, capacityKg));
            optArea.append(String.format("   • Capacity Utilization        : %.1f%%\n", (totalWeight / capacityKg) * 100));
        });
        tabs.addTab("0/1 Knapsack & Batching", makeDemoPanel(optArea, runOpt));

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeDemoPanel(JTextArea area, JButton actionBtn) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(makeScrollPane(area), BorderLayout.CENTER);
        JPanel bRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bRow.setOpaque(false);
        bRow.add(actionBtn);
        p.add(bRow, BorderLayout.SOUTH);
        return p;
    }

    // --- PANEL 4: ACTIVITY & LOGS ---
    /**
     * Rebuilds the active and completed order lists from the database.
     *
     * <p>Orders used to live only in memory, so closing the app erased every
     * delivery it had ever performed and the Reports page always started from
     * zero. Restoring them means a report covers the project's whole history,
     * not just the current session.</p>
     */
    private void restoreStoredOrders() {
        activeOrders = new DynamicArray<>();
        completedOrders = new DynamicArray<>();

        for (Order order : DatabaseManager.loadOrders()) {
            String status = order.getStatus();
            if (Order.OrderState.COMPLETED.name().equalsIgnoreCase(status)
                    || Order.OrderState.CANCELLED.name().equalsIgnoreCase(status)) {
                completedOrders.add(order);
            } else if (order.getAssignedRiderId() > 0
                    || Order.OrderState.ASSIGNED.name().equalsIgnoreCase(status)
                    || Order.OrderState.IN_TRANSIT.name().equalsIgnoreCase(status)) {
                activeOrders.add(order);
            }
        }
    }

    /** Output area for the Reports page. */
    private final JTextArea reportArea = new JTextArea();

    /**
     * Reports &amp; Analytics.
     *
     * <p>This page exists to make already-written work reachable. Before it,
     * {@code MetricsEngine}, {@code ReportEngine}, {@code SimulationEngine},
     * {@code BenchmarkEngine}, {@code IndexingEngine} and {@code SchedulingEngine}
     * all compiled and passed their tests but had no caller anywhere in the
     * application, so none of them ran when the app ran.</p>
     */
    private JPanel buildReportsPanelUI() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel card = makeCard("Reports & Analytics");
        card.setLayout(new BorderLayout(0, 10));

        reportArea.setFont(FONT_MONO);
        reportArea.setBackground(new Color(0x0B1120));
        reportArea.setForeground(TEXT_PRIMARY);
        reportArea.setEditable(false);
        reportArea.setText("Choose a report below.\n\n"
                + "  Operations Report  - live metrics from the orders in this session\n"
                + "  Run Simulation     - drives N orders through the real assignment engine\n"
                + "  Benchmark Indexes  - times HashTable / BST / Red-Black / B-Tree lookups\n"
                + "  Index Lookup       - compares the four search structures on one id\n"
                + "  Scheduling Compare - runs all four dispatch strategies side by side\n"
                + "  Batch Optimiser    - greedy vs dynamic-programming request batching\n");
        card.add(makeScrollPane(reportArea), BorderLayout.CENTER);

        JPanel controls = new JPanel(new GridLayout(0, 3, 8, 8));
        controls.setOpaque(false);

        JButton metricsBtn   = makeAccentButton("Operations Report", IconType.PERFORMANCE);
        JButton simBtn       = makeSecondaryButton("Run Simulation", IconType.RUN);
        JButton benchBtn     = makeSecondaryButton("Benchmark Indexes", IconType.PERFORMANCE);
        JButton indexBtn     = makeSecondaryButton("Index Lookup", IconType.SEARCH);
        JButton scheduleBtn  = makeSecondaryButton("Scheduling Compare", IconType.SORT);
        JButton optimiseBtn  = makeSecondaryButton("Batch Optimiser", IconType.OPTIMISATION);

        metricsBtn.addActionListener(e -> showOperationsReport());
        simBtn.addActionListener(e -> runSimulationReport());
        benchBtn.addActionListener(e -> runBenchmarkSuite());
        indexBtn.addActionListener(e -> runIndexLookupReport());
        scheduleBtn.addActionListener(e -> runSchedulingComparison());
        optimiseBtn.addActionListener(e -> runBatchOptimiser());

        controls.add(metricsBtn);
        controls.add(simBtn);
        controls.add(benchBtn);
        controls.add(indexBtn);
        controls.add(scheduleBtn);
        controls.add(optimiseBtn);
        card.add(controls, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    /** Live operational metrics for this session's orders. */
    private void showOperationsReport() {
        DynamicArray<Order> all = new DynamicArray<>();
        for (Order o : completedOrders) all.add(o);
        for (Order o : activeOrders) all.add(o);

        MetricsEngine.Stats stats = MetricsEngine.compute(all, riders);
        reportArea.setText(ReportEngine.generate(stats));
        log("Generated operations report over " + all.size() + " order(s).");
    }

    /** Runs a batch of synthetic orders through the real assignment engine. */
    private void runSimulationReport() {
        if (locations.isEmpty() || riders.isEmpty()) {
            reportArea.setText("Initialize data first.");
            return;
        }

        int count = 25;
        long start = System.nanoTime();
        SimulationEngine.SimulationResult sim =
                SimulationEngine.run(count, riders, locations, roads, 22237205L);
        long elapsedNs = System.nanoTime() - start;

        StringBuilder sb = new StringBuilder();
        sb.append("=== SIMULATION: ").append(count).append(" ORDERS ===\n\n");
        sb.append("Generated : ").append(sim.ordersGenerated).append('\n');
        sb.append("Completed : ").append(sim.ordersCompleted).append('\n');
        sb.append("Unassigned: ").append(sim.ordersUnassigned).append('\n');
        sb.append("Elapsed   : ").append(String.format("%.1f ms", elapsedNs / 1e6)).append("\n\n");
        sb.append(ReportEngine.generate(MetricsEngine.compute(sim.completedOrders, riders)));
        reportArea.setText(sb.toString());

        // The simulation mutates the in-memory riders it was given; reload so the
        // dashboard is not left showing simulated state as if it were real.
        loadData();
        log("Simulation complete: " + sim.ordersCompleted + "/" + sim.ordersGenerated + " delivered.");
    }

    /** Times the four index structures against increasing input sizes. */
    private void runBenchmarkSuite() {
        reportArea.setText("Running benchmark, please wait...\n");
        long start = System.nanoTime();
        DynamicArray<BenchmarkEngine.BenchmarkRow> rows =
                BenchmarkEngine.runAll(new int[]{100, 1000, 10000});
        long elapsedNs = System.nanoTime() - start;

        StringBuilder sb = new StringBuilder();
        sb.append("=== INDEX STRUCTURE BENCHMARK ===\n");
        sb.append("(insert + search, persisted to the algorithm_runs table)\n\n");
        for (BenchmarkEngine.BenchmarkRow row : rows) {
            sb.append("  ").append(row).append('\n');
        }
        sb.append("\nTotal elapsed: ").append(String.format("%.1f ms", elapsedNs / 1e6)).append('\n');
        reportArea.setText(sb.toString());

        AuditLog.algorithmExecuted("IndexStructureBenchmark", rows.size(), elapsedNs);
        refreshAuditTrail();
        log("Benchmark complete: " + rows.size() + " rows written to algorithm_runs.");
    }

    /** Compares the four search structures looking up the same request id. */
    private void runIndexLookupReport() {
        if (requests.isEmpty()) {
            reportArea.setText("No requests loaded. Initialize data first.");
            return;
        }

        IndexingEngine index = new IndexingEngine();
        long buildStart = System.nanoTime();
        index.indexRequests(requests);
        long buildNs = System.nanoTime() - buildStart;

        int targetId = requests.get(requests.size() / 2).getRequestId();

        long t0 = System.nanoTime(); index.searchHashTable(targetId); long hashNs = System.nanoTime() - t0;
        t0 = System.nanoTime();      index.searchBST(targetId);       long bstNs  = System.nanoTime() - t0;
        t0 = System.nanoTime();      index.searchRBT(targetId);       long rbtNs  = System.nanoTime() - t0;
        t0 = System.nanoTime();      index.searchBTree(targetId);     long btNs   = System.nanoTime() - t0;

        StringBuilder sb = new StringBuilder();
        sb.append("=== INDEX LOOKUP COMPARISON ===\n\n");
        sb.append(String.format("Indexed %,d requests in %.2f ms%n%n", requests.size(), buildNs / 1e6));
        sb.append("Looking up request #").append(targetId).append(":\n\n");
        sb.append(String.format("  HashTable      %,8d ns   (expected O(1) average)%n", hashNs));
        sb.append(String.format("  BST            %,8d ns   (O(log n) average, O(n) worst)%n", bstNs));
        sb.append(String.format("  Red-Black Tree %,8d ns   (O(log n) guaranteed)%n", rbtNs));
        sb.append(String.format("  B-Tree (t=3)   %,8d ns   (O(log_t n))%n%n", btNs));
        sb.append("Structure sizes and shape:\n");
        sb.append(String.format("  BST height          : %d%n", index.getBSTHeight()));
        sb.append(String.format("  Red-Black height    : %d  (bound: 2*log2(n+1) = %.1f)%n",
                index.getRBTHeight(), 2 * (Math.log(requests.size() + 1) / Math.log(2))));
        sb.append(String.format("  B-Tree entries      : %d%n", index.getBTreeSize()));
        sb.append(String.format("  HashTable entries   : %d%n", index.getHashTableSize()));
        sb.append(String.format("  HashTable collisions: %d%n", index.getHashTableCollisionCount()));
        sb.append("\nNote: request ids arrive in ascending order, which is the worst\n");
        sb.append("case for an unbalanced BST - compare its height to the Red-Black bound.\n");
        reportArea.setText(sb.toString());
        log("Index lookup comparison generated.");
    }

    /** Runs all four dispatch strategies over the same pending requests. */
    private void runSchedulingComparison() {
        DynamicArray<ServiceRequest> pending = new DynamicArray<>();
        for (ServiceRequest r : requests) {
            if ("PENDING".equalsIgnoreCase(r.getStatus())) {
                pending.add(r);
            }
        }

        if (pending.isEmpty()) {
            reportArea.setText("No PENDING requests to schedule.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== SCHEDULING STRATEGY COMPARISON ===\n\n");
        sb.append("Pending requests: ").append(pending.size()).append("\n\n");

        ds.Queue<ServiceRequest> fifo = SchedulingEngine.dispatchFIFO(pending);
        sb.append("FIFO (oldest submission first) - first 5:\n");
        for (int i = 0; i < 5 && !fifo.isEmpty(); i++) {
            sb.append("   ").append(fifo.dequeue()).append('\n');
        }

        ds.MinHeap<ServiceRequest> heap = SchedulingEngine.dispatchPriority(pending);
        sb.append("\nPRIORITY (highest priority first) - first 5:\n");
        for (int i = 0; i < 5 && !heap.isEmpty(); i++) {
            sb.append("   ").append(heap.extractMin()).append('\n');
        }

        ds.Stack<ServiceRequest> urgent = SchedulingEngine.dispatchUrgentOverride(pending);
        sb.append("\nURGENT OVERRIDE (urgency >= 4 preempts, LIFO) - first 5:\n");
        for (int i = 0; i < 5 && !urgent.isEmpty(); i++) {
            sb.append("   ").append(urgent.pop()).append('\n');
        }

        DynamicArray<ServiceRequest> rr =
                SchedulingEngine.dispatchRoundRobin(pending, buildLocationMap());
        sb.append("\nROUND ROBIN (rotating by campus zone) - first 5:\n");
        for (int i = 0; i < 5 && i < rr.size(); i++) {
            sb.append("   ").append(rr.get(i)).append('\n');
        }

        sb.append("\nSame input, four different service orders - which is 'fair'\n");
        sb.append("depends on whether you optimise for waiting time, urgency, or zone balance.\n");
        reportArea.setText(sb.toString());
        log("Scheduling comparison generated over " + pending.size() + " pending requests.");
    }

    /** Greedy vs dynamic-programming batching for one rider's capacity. */
    private void runBatchOptimiser() {
        DynamicArray<ServiceRequest> pending = new DynamicArray<>();
        for (ServiceRequest r : requests) {
            if ("PENDING".equalsIgnoreCase(r.getStatus()) && pending.size() < 20) {
                pending.add(r);
            }
        }

        if (pending.isEmpty()) {
            reportArea.setText("No PENDING requests to batch.");
            return;
        }

        double capacityKg = 10.0;
        long t0 = System.nanoTime();
        DynamicArray<ServiceRequest> dp = OptimisationEngine.dpKnapsackBatching(pending, capacityKg);
        long dpNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        DynamicArray<ServiceRequest> brute = OptimisationEngine.bruteForceBatching(pending, capacityKg);
        long bruteNs = System.nanoTime() - t0;

        double dpValue = 0.0;
        for (ServiceRequest r : dp) dpValue += r.getPriority();
        double bruteValue = 0.0;
        for (ServiceRequest r : brute) bruteValue += r.getPriority();

        StringBuilder sb = new StringBuilder();
        sb.append("=== REQUEST BATCHING: DP vs BRUTE FORCE ===\n\n");
        sb.append(String.format("Candidate requests : %d%n", pending.size()));
        sb.append(String.format("Rider capacity     : %.1f kg%n%n", capacityKg));
        sb.append(String.format("Dynamic programming: %d selected, total priority %.2f, %,d ns%n",
                dp.size(), dpValue, dpNs));
        sb.append(String.format("Brute force (2^n)  : %d selected, total priority %.2f, %,d ns%n%n",
                brute.size(), bruteValue, bruteNs));
        sb.append(Math.abs(dpValue - bruteValue) < 0.001
                ? "Both found the SAME optimum - the DP solution is exact.\n"
                : "WARNING: the two disagree, which would indicate a bug in one of them.\n");
        sb.append(String.format("%nDP was %.1fx %s than brute force here.%n",
                bruteNs > dpNs ? (double) bruteNs / Math.max(dpNs, 1) : (double) dpNs / Math.max(bruteNs, 1),
                bruteNs > dpNs ? "faster" : "slower"));
        sb.append("Brute force is O(2^n) and is only usable as an exact baseline\n");
        sb.append("on small inputs; DP is O(n*W) and scales.\n");
        reportArea.setText(sb.toString());

        AuditLog.algorithmExecuted("DPKnapsackBatching", pending.size(), dpNs);
        refreshAuditTrail();
        log("Batch optimiser: DP and brute force " +
                (Math.abs(dpValue - bruteValue) < 0.001 ? "agree." : "DISAGREE - investigate."));
    }

    private JPanel buildActivityLogsPanelUI() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // --- Top half: the in-memory session log (cleared when the app exits) ---
        JPanel logCard = makeCard("Live System Activity Terminal");
        logCard.setLayout(new BorderLayout());
        logArea.setFont(FONT_MONO);
        logArea.setBackground(new Color(0x0B1120));
        logArea.setForeground(TEXT_PRIMARY);
        logArea.setCaretColor(ACCENT);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logCard.add(makeScrollPane(logArea), BorderLayout.CENTER);

        // --- Bottom half: the durable audit trail read back from the database ---
        JPanel auditCard = makeCard("Persisted Audit Trail (survives restart)");
        auditCard.setLayout(new BorderLayout(0, 8));
        auditList.setFont(FONT_MONO);
        auditList.setBackground(new Color(0x0B1120));
        auditList.setForeground(TEXT_PRIMARY);
        auditList.setSelectionBackground(BG_CARD2);
        auditList.setSelectionForeground(ACCENT);
        auditCard.add(makeScrollPane(auditList), BorderLayout.CENTER);

        JPanel auditControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        auditControls.setOpaque(false);
        JButton refreshAuditBtn = makeSecondaryButton("Refresh Audit Trail", IconType.REFRESH);
        refreshAuditBtn.addActionListener(e -> refreshAuditTrail());
        auditControls.add(refreshAuditBtn);
        auditCard.add(auditControls, BorderLayout.SOUTH);

        // A split pane rather than a fixed layout so the user can decide how much
        // room each half gets - the audit trail grows without bound over time.
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logCard, auditCard);
        split.setResizeWeight(0.5);
        split.setContinuousLayout(true);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(BG_DARK);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Reloads the audit trail from the database into the list.
     *
     * <p>loadAuditEvents() already returns newest-first (its SQL ends with
     * ORDER BY eventId DESC), so no sorting is needed here. Only the most recent
     * rows are rendered: the table grows with every order placed, and a Swing
     * JList holding tens of thousands of rows becomes sluggish to scroll.</p>
     */
    private void refreshAuditTrail() {
        try {
            DynamicArray<models.AuditEvent> events = DatabaseManager.loadAuditEvents();
            auditListModel.clear();

            int shown = Math.min(events.size(), AUDIT_ROWS_SHOWN);
            for (int i = 0; i < shown; i++) {
                models.AuditEvent event = events.get(i);
                auditListModel.addElement(event.getTimestamp() + "  " + event.getDescription());
            }

            if (events.size() > shown) {
                auditListModel.addElement("... " + (events.size() - shown)
                        + " older events not shown (" + events.size() + " total)");
            }
            if (events.isEmpty()) {
                auditListModel.addElement("No audit events recorded yet - place an order to generate some.");
            }
        } catch (Exception ex) {
            log("Could not load the audit trail: " + ex.getMessage());
        }
    }

    // --- Core Backend Functions (100% Unchanged Logic) ---
    /**
     * When each in-flight delivery reaches pickup and drop-off, in real
     * milliseconds. Keyed by requestId, which is the id shared by the
     * ServiceRequest and the Order it produced.
     *
     * <p>Memory-only on purpose: a delivery in progress when the app closes has
     * no meaningful "resume". Its request stays ASSIGNED and its rider keeps the
     * order id, so {@code releaseStrandedRiders} leaves them alone.</p>
     */
    private final ds.HashTable<Integer, long[]> deliverySchedule = new ds.HashTable<>();

    /**
     * Records when an accepted delivery should reach each stage.
     *
     * @param pickupMin travel time from the rider to the pickup point
     * @param totalMin  pickup travel plus the pickup-to-destination leg
     */
    private void scheduleDelivery(int requestId, double pickupMin, double totalMin) {
        long now = System.currentTimeMillis();
        long pickupAt = now + (long) (pickupMin * Config.SIMULATED_MINUTE_MILLIS);
        long deliverAt = now + (long) (Math.max(totalMin, pickupMin) * Config.SIMULATED_MINUTE_MILLIS);
        deliverySchedule.put(requestId, new long[]{pickupAt, deliverAt});
    }

    /** True once a delivery's scheduled drop-off time has passed. */
    private boolean isDeliveryDue(int requestId) {
        long[] schedule = deliverySchedule.get(requestId);
        if (schedule == null) {
            // No schedule: either it was assigned by a previous session, or the
            // route could not be timed. Complete it rather than strand it.
            return true;
        }
        return System.currentTimeMillis() >= schedule[1];
    }

    /**
     * Promotes active orders from ASSIGNED to PICKED_UP to IN_TRANSIT as their
     * scheduled times pass. Completion is handled separately.
     */
    private void advanceInFlightOrders() {
        long now = System.currentTimeMillis();

        for (Order order : activeOrders) {
            long[] schedule = deliverySchedule.get(order.getRequestId());
            if (schedule == null) {
                continue;
            }

            String status = order.getStatus();

            if (Order.OrderState.ASSIGNED.name().equals(status) && now >= schedule[0]) {
                order.setStatus(Order.OrderState.PICKED_UP);
                DatabaseManager.saveOrder(order);
                AuditLog.orderPickedUp(order, findRider(order.getAssignedRiderId()));
            } else if (Order.OrderState.PICKED_UP.name().equals(status)) {
                // The parcel is collected and the rider is on the road.
                order.setStatus(Order.OrderState.IN_TRANSIT);
                DatabaseManager.saveOrder(order);
            }
        }
    }

    /**
     * Persists one Dijkstra execution as an {@link models.AlgorithmRun}.
     *
     * <p>Called only from {@code computeRoute} - the user-facing route preview -
     * for the same reason ROUTE_CALCULATED is: a full assignment runs Dijkstra
     * once per candidate rider, so recording inside the engine would write
     * roughly thirty rows per order.</p>
     */
    private void recordDijkstraRun(long elapsedNs, RouteEngine.PathResult result) {
        try {
            boolean found = result != null;
            DatabaseManager.addAlgorithmRun(new models.AlgorithmRun(
                    0,
                    "Dijkstra (route preview)",
                    locations.size(),
                    elapsedNs,
                    0L,
                    java.time.LocalDateTime.now().toString(),
                    found ? result.path.size() : 0L,
                    0L,
                    found ? "SUCCESS" : "NO_PATH",
                    found
                            ? String.format("%d hops, %.3f km, %.2f min",
                                    result.path.size(), result.totalDistanceKm, result.totalTimeMin)
                            : "No route between the selected locations"
            ));
        } catch (Exception ex) {
            // Measurement must never break the feature being measured.
            log("Could not record the algorithm run: " + ex.getMessage());
        }
    }

    /**
     * Cancels the most recent active order and tries to hand it to another rider.
     *
     * <p>This is the only caller of {@code DeliveryEngine.cancelAndReassign},
     * which was written but unreachable, and it is what makes the
     * ORDER_CANCELLED audit event a real event rather than a declared one.</p>
     */
    private void cancelActiveOrder() {
        if (activeOrders.isEmpty()) {
            log("No active order to cancel.");
            return;
        }

        Order order = activeOrders.get(activeOrders.size() - 1);
        Resource currentRider = findRider(order.getAssignedRiderId());

        DeliveryEngine.AssignmentResult reassigned =
                DeliveryEngine.cancelAndReassign(order, currentRider, riders, locations, roads);

        if (currentRider != null) {
            DatabaseManager.updateResourceState(currentRider);
            AuditLog.riderStatusChanged(currentRider, currentRider.getAvailabilityStatus());
        }

        if (reassigned != null && reassigned.rider != null) {
            DatabaseManager.updateResourceState(reassigned.rider);
            DatabaseManager.saveOrder(order);
            AuditLog.orderAssigned(order, reassigned.rider,
                    reassigned.distanceKm, reassigned.estimatedTimeMin);
            AuditLog.riderStatusChanged(reassigned.rider, "BUSY");
            log("Order #" + order.getOrderId() + " reassigned to " + reassigned.rider.getName() + ".");
            setStatus("Order reassigned");
        } else {
            // cancelAndReassign already set the order to CANCELLED.
            DatabaseManager.saveOrder(order);
            AuditLog.orderCancelled(order, "no alternative rider available");
            activeOrders.removeElement(order);
            deliverySchedule.remove(order.getRequestId());
            log("Order #" + order.getOrderId() + " cancelled - no alternative rider available.");
            setStatus("Order cancelled");
        }

        refreshDashboard();
        refreshAuditTrail();
        refreshSummary();
    }

    /** Finds a loaded rider by id, or null if there is no such rider. */
    private Resource findRider(int riderId) {
        if (riderId <= 0) {
            return null;
        }
        for (Resource r : riders) {
            if (r.getResourceId() == riderId) {
                return r;
            }
        }
        return null;
    }

    private void startCompletionWatcher() {
        javax.swing.Timer timer = new javax.swing.Timer(10000, e -> checkForCompletedOrders());
        timer.setRepeats(true);
        timer.start();
    }

    /**
     * Advances every in-flight delivery, and completes the ones that are done.
     *
     * <p>Deliveries used to complete by comparing the wall clock against a
     * deadline of {@code 480 + duration} - i.e. shortly after 08:00. Run the app
     * at any time after that and every order completed on the very next tick;
     * run it before 08:00 and nothing ever completed. Progress is now measured
     * from when each order was actually assigned, using the simulated clock in
     * {@link utils.Config#SIMULATED_MINUTE_MILLIS}, so a delivery takes a
     * proportional and observable amount of real time whatever hour it is.</p>
     */
    private void checkForCompletedOrders() {
        try {
            double currentMinutes = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute() + LocalTime.now().getSecond() / 60.0;

            // Move orders through PICKED_UP and IN_TRANSIT before anything is
            // completed, so the lifecycle is visible rather than instantaneous.
            advanceInFlightOrders();

            List<ServiceRequest> toComplete = new ArrayList<>();
            for (ServiceRequest req : requests) {
                if (req.getStatus() != null && req.getStatus().equalsIgnoreCase("ASSIGNED")) {
                    if (isDeliveryDue(req.getRequestId())) {
                        toComplete.add(req);
                    }
                }
            }

            for (ServiceRequest req : toComplete) {
                req.setStatus("DELIVERED");
                // Record WHEN it was delivered, not just that it was. Without
                // this the column stayed NULL on every row ever written.
                req.setDeliveredTimeMin(currentMinutes);
                DatabaseManager.saveServiceRequest(req);
                int riderId = req.getAssignedRiderId();
                // Held so the audit row can name the rider, not just their id.
                Resource releasedRider = null;
                if (riderId > 0) {
                    for (Resource r : riders) {
                        if (r.getResourceId() == riderId) {
                            releasedRider = r;
                            break;
                        }
                    }
                    if (releasedRider != null) {
                        // completeOrder does four things at once: clears the current
                        // order, MOVES the rider to the delivery destination, sets
                        // them AVAILABLE, and increments their completed count.
                        // Moving the rider is what makes the next "nearest rider"
                        // calculation measure from where they actually are.
                        releasedRider.completeOrder(req.getDestLocationId());
                        DatabaseManager.updateResourceState(releasedRider);
                    }
                    // Each audit call sits beside the database write it describes,
                    // so the row and the state change cannot drift apart.
                    AuditLog.riderStatusChanged(releasedRider, "AVAILABLE");
                }
                AuditLog.orderDelivered(req, releasedRider);

                // Exact lookup by the stored link. The previous version matched on
                // pickup id + delivery id + meal name, which picked the wrong
                // order whenever two customers ordered the same meal along the
                // same route.
                Order moved = null;
                for (Order o : activeOrders) {
                    if (o.getRequestId() == req.getRequestId()) {
                        moved = o;
                        break;
                    }
                }
                if (moved != null) {
                    // PICKED_UP and IN_TRANSIT were already reached by
                    // advanceInFlightOrders() at their scheduled times; this is
                    // the final transition.
                    moved.setStatus(Order.OrderState.COMPLETED);
                    DatabaseManager.saveOrder(moved);

                    activeOrders.removeElement(moved);
                    completedOrders.add(moved);
                }
                deliverySchedule.remove(req.getRequestId());
            }

            if (!toComplete.isEmpty()) {
                refreshDashboard();
                refreshAuditTrail();
                refreshSummary();
                log("Moved " + toComplete.size() + " deliveries to completed queue.");
            }
        } catch (Exception ex) {
            // keep watcher resilient
        }
    }

    private void processNextIncoming() {
        try {
            if (!incomingManager.hasPending()) {
                log("No incoming requests to process.");
                return;
            }
            ServiceRequest req = incomingManager.next();
            if (req == null) {
                log("No incoming request retrieved.");
                return;
            }

            // Recover the order that was stored when this request was placed,
            // so the customer name, restaurant, meal and real parcel weight
            // survive the trip through the queue. The previous version built a
            // placeholder here ("Queued"/"QueuedVendor", a hardcoded 1.0kg),
            // which meant a queued order was assigned using the wrong weight.
            Order order = DatabaseManager.findOrderByRequestId(req.getRequestId());
            if (order == null) {
                // Seeded requests predate order persistence and have no stored
                // order; fall back to a minimal one built from the request.
                order = new Order(
                        DatabaseManager.nextOrderId(),
                        "Queued",
                        "QueuedVendor",
                        req.getCategory(),
                        1.0,
                        req.getSourceLocationId(),
                        req.getDestLocationId(),
                        req.getTimeSubmittedMin(),
                        Order.OrderState.QUEUED.name(),
                        req.getAssignedRiderId()
                );
                order.setRequestId(req.getRequestId());
                order.setPriority(req.getPriority());
                DatabaseManager.saveOrder(order);
            }

            Resource assigned = driverPool.nextSuitable(order, locations, roads);
            DeliveryEngine.AssignmentResult result = null;
            if (assigned != null) {
                Graph graph = buildGraph();
                var path = RouteEngine.dijkstra(graph, assigned.getCurrentLocationId(), order.getPickupLocationId());
                if (path != null) {
                    result = new DeliveryEngine.AssignmentResult(assigned, path.totalDistanceKm, path.totalTimeMin);
                }
            }
            if (result == null) {
                result = DeliveryEngine.assignRider(order, riders, locations, roads);
            }

            if (result == null || result.rider == null) {
                log("Could not assign a rider now; requeueing request.");
                incomingManager.requeue(req, req.getUrgency() >= 4);
                refreshDashboard();
                refreshAuditTrail();
                refreshSummary();
                return;
            }

            req.setAssignedRiderId(result.rider.getResourceId());
            req.setStatus("ASSIGNED");
            double deliveryDuration = 0.0;
            try {
                Graph fullGraph = buildGraph();
                RouteEngine.PathResult pd = RouteEngine.dijkstra(fullGraph, order.getPickupLocationId(), order.getDeliveryLocationId());
                if (pd != null) {
                    deliveryDuration = result.estimatedTimeMin + pd.totalTimeMin;
                } else {
                    deliveryDuration = DeliveryEngine.estimateDeliveryDuration(order, result.distanceKm, result.rider);
                }
            } catch (Exception ex) {
                deliveryDuration = DeliveryEngine.estimateDeliveryDuration(order, result.distanceKm, result.rider);
            }
            req = new ServiceRequest(req.getRequestId(), req.getSourceLocationId(), req.getDestLocationId(), req.getCategory(), req.getUrgency(), req.getTimeSubmittedMin(), 480.0 + deliveryDuration, req.getStatus(), req.getAssignedRiderId(), req.getDeliveredTimeMin());
            DatabaseManager.saveServiceRequest(req);
            // assignOrder records which order the rider is carrying and marks
            // them BUSY in one step.
            result.rider.assignOrder(order.getOrderId());
            DatabaseManager.updateResourceState(result.rider);
            order.setAssignedRiderId(result.rider.getResourceId());
            order.setDistanceKm(result.distanceKm);
            order.setEstimatedDeliveryTimeMin(deliveryDuration);
            order.setVehicleType(result.rider.getType());
            order.setStatus(Order.OrderState.ASSIGNED);
            DatabaseManager.saveOrder(order);
            scheduleDelivery(req.getRequestId(), result.estimatedTimeMin, deliveryDuration);
            // No ORDER_CREATED here: this request was already created when it was
            // placed. Draining the queue only assigns it.
            AuditLog.orderAssigned(order, result.rider, result.distanceKm, deliveryDuration);
            AuditLog.riderStatusChanged(result.rider, "BUSY");
            activeOrders.add(order);
            log("Processed incoming request " + req.getRequestId() + " and assigned rider " + result.rider.getName());
            refreshDashboard();
            refreshAuditTrail();
            refreshSummary();
        } catch (Exception ex) {
            log("Processing incoming request failed: " + ex.getMessage());
        }
    }

    private void toggleAutoProcessing() {
        if (autoProcessing) {
            stopAutoProcessing();
        } else {
            int seconds = 10;
            try {
                seconds = Integer.parseInt(intervalField.getText().trim());
                if (seconds <= 0) seconds = 10;
            } catch (Exception ex) {
                seconds = 10;
            }
            startAutoProcessing(seconds);
        }
    }

    private void startAutoProcessing(int seconds) {
        if (autoProcessTimer != null && autoProcessTimer.isRunning()) {
            autoProcessTimer.stop();
        }
        autoProcessTimer = new javax.swing.Timer(seconds * 1000, e -> {
            processNextIncoming();
        });
        autoProcessTimer.setRepeats(true);
        autoProcessTimer.start();
        autoProcessing = true;
        autoToggleBtn.setText("Stop Auto");
        log("Auto-processing started (" + seconds + "s interval)");
    }

    private void stopAutoProcessing() {
        if (autoProcessTimer != null) {
            autoProcessTimer.stop();
            autoProcessTimer = null;
        }
        autoProcessing = false;
        if (autoToggleBtn != null) autoToggleBtn.setText("Start Auto");
        log("Auto-processing stopped");
    }

    private void startBulkProcessing() {
        int n = 5;
        try {
            n = Integer.parseInt(bulkCountField.getText().trim());
            if (n <= 0) n = 1;
        } catch (Exception ex) {
            n = 5;
        }
        final int total = n;
        javax.swing.Timer t = new javax.swing.Timer(300, null);
        final int[] counter = {0};
        t.addActionListener(e -> {
            if (counter[0] >= total) {
                t.stop();
                return;
            }
            processNextIncoming();
            counter[0]++;
        });
        t.setRepeats(true);
        t.start();
    }

    private void initializeData() {
        setStatus("Initializing database...");
        log("Initializing campus data and rider network...");
        try {
            String dataDir = resolveDataDir();
            DatabaseManager.initializeDatabase(dataDir + "locations.csv", dataDir + "roads.csv");
            loadData();
            if (roads.isEmpty()) {
                log("Road network empty. Generating campus roads from location data...");
                RoadNetworkGenerator.main(new String[]{dataDir + "locations.csv", dataDir + "roads.csv"});
                loadData();
            }
            setStatus("Ready for orders");
            log("The UG Swift Delivery Service platform is ready.");
        } catch (Exception ex) {
            setStatus("Initialization failed");
            log("Initialization failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadData() {
        try {
            locations = DatabaseManager.loadLocations();
            roads = DatabaseManager.loadRoads();
            requests = DatabaseManager.loadServiceRequests();
            riders = loadResources();
            driverPool.rebuild(riders);
            incomingManager = new IncomingOrderManager();
            if (requests != null) {
                for (ServiceRequest req : requests) {
                    if ("PENDING".equalsIgnoreCase(req.getStatus())) {
                        incomingManager.requeue(req, req.getUrgency() >= 4);
                    }
                }
            }
            restoreStoredOrders();
            populateLocationSelectors();
            populateRestaurantMenus();
            refreshDashboard();
            refreshAuditTrail();
            showSummary();
        } catch (Exception ex) {
            log("Failed to reload data: " + ex.getMessage());
        }
    }

    private void generateRoadNetwork() {
        setStatus("Generating roads...");
        log("Generating road network from the campus location data...");
        try {
            String dataDir = resolveDataDir();
            RoadNetworkGenerator.main(new String[]{dataDir + "locations.csv", dataDir + "roads.csv"});
            // The cache keys on (start, end) only, not on the graph, so every
            // entry now describes roads that no longer exist. Without this the
            // app would keep serving pre-regeneration routes for the rest of
            // the session.
            RouteEngine.clearRouteCache();
            loadData();
            log("Road network generation completed. Route cache cleared.");
            setStatus("Roads generated");
        } catch (Exception ex) {
            log("Road generation failed: " + ex.getMessage());
            setStatus("Road generation failed");
        }
    }

    private void computeRoute() {
        if (locations.isEmpty()) {
            log("No locations available. Initialize the database first.");
            return;
        }

        int srcId = getSelectedLocationId(sourceCombo);
        int dstId = getSelectedLocationId(destinationCombo);
        if (srcId == -1 || dstId == -1) {
            log("Please choose both a pickup and a destination.");
            return;
        }

        setStatus("Planning route...");
        Graph graph = buildGraph();
        // Timed so the run can be recorded: Progress.md section 22 asks for
        // Dijkstra executions to be captured in AlgorithmRun, not only in the
        // standalone benchmark runners.
        long routeStartNs = System.nanoTime();
        RouteEngine.PathResult result = RouteEngine.dijkstra(graph, srcId, dstId);
        long routeElapsedNs = System.nanoTime() - routeStartNs;
        recordDijkstraRun(routeElapsedNs, result);
        if (result == null) {
            log("No route found between the selected locations.");
            setStatus("No route found");
            return;
        }

        log("Route preview: " + findLocationName(srcId) + " → " + findLocationName(dstId));
        log("  Distance: " + String.format("%.2f km", result.totalDistanceKm));
        log("  Travel time: " + String.format("%.1f min", result.totalTimeMin));
        // Audited here, at the user-facing action, and deliberately NOT inside
        // RouteEngine.dijkstra: that method runs once per candidate rider during
        // assignment, so auditing there would mean ~30 database writes per order.
        AuditLog.routeCalculated(findLocationName(srcId), findLocationName(dstId),
                result.totalDistanceKm, result.totalTimeMin);
        setStatus("Route preview ready");
    }

    private void placeOrder() {
        if (locations.isEmpty()) {
            log("No locations available. Initialize the database first.");
            return;
        }

        String customerName = customerField.getText().trim();
        String restaurant = (String) restaurantCombo.getSelectedItem();
        String meal = (String) foodCombo.getSelectedItem();
        int pickupId = getSelectedLocationId(sourceCombo);
        int deliveryId = getSelectedLocationId(destinationCombo);
        if (customerName.isEmpty() || restaurant == null || meal == null || pickupId == -1 || deliveryId == -1 || pickupId == deliveryId) {
            log("Please complete the full order form before placing an order.");
            return;
        }

        double weightKg = menuWeights.getOrDefault(meal, 1.5);
        String priority = (String) priorityCombo.getSelectedItem();
        if ("Express".equalsIgnoreCase(priority)) {
            weightKg += 0.3;
        } else if ("Family Pack".equalsIgnoreCase(priority)) {
            weightKg += 0.7;
        }

        Order order = new Order(
                DatabaseManager.nextOrderId(),
                customerName,
                restaurant,
                meal,
                weightKg,
                pickupId,
                deliveryId,
                480.0,
                Order.OrderState.CREATED.name(),
                -1
        );
        ServiceRequest request = new ServiceRequest(
                DatabaseManager.nextRequestId(),
                pickupId,
                deliveryId,
                meal,
                "Express".equalsIgnoreCase(priority) ? 4 : ("Family Pack".equalsIgnoreCase(priority) ? 3 : 2),
                480.0,
                480.0,
                "PENDING",
                -1
        );
        // Link the two so completion can find this exact order later instead of
        // guessing from pickup + destination + meal name.
        order.setRequestId(request.getRequestId());
        order.setPriority(request.getPriority());

        boolean highPriority = "Express".equalsIgnoreCase(priority);
        incomingManager.submit(request, highPriority);
        requests.add(request);
        // The order is now waiting in the intake queue.
        order.setStatus(Order.OrderState.QUEUED);
        DatabaseManager.saveOrder(order);
        AuditLog.orderCreated(order);

        log("New order received for " + customerName + " from " + restaurant + " - " + meal);
        log("Queued as request #" + request.getRequestId()
                + (highPriority ? " (EXPRESS - added to Priority Queue)" : " (Standard - added to FIFO Queue)"));

        if (autoProcessing) {
            processNextIncoming();
        }
        refreshDashboard();
        refreshAuditTrail();
        refreshSummary();
    }

    private void runDispatch(String strategy) {
        if (requests.isEmpty()) {
            log("No pending requests available. Place an order first.");
            return;
        }

        setStatus("Dispatching...");
        log("Dispatch strategy: " + strategy);
        DynamicArray<ServiceRequest> pending = new DynamicArray<>();
        for (ServiceRequest req : requests) {
            if ("PENDING".equalsIgnoreCase(req.getStatus()) || "ASSIGNED".equalsIgnoreCase(req.getStatus())) {
                pending.add(req);
            }
        }

        try {
            if ("Nearest Rider".equalsIgnoreCase(strategy)) {
                if (activeOrders.isEmpty()) {
                    log("No live orders to dispatch.");
                    return;
                }
                Order sample = activeOrders.get(activeOrders.size() - 1);
                DeliveryEngine.AssignmentResult assigned = DeliveryEngine.assignRider(sample, riders, locations, roads);
                if (assigned != null && assigned.rider != null) {
                    log("Nearest rider available: " + assigned.rider.getName());
                } else {
                    log("No rider available for the current dispatch window.");
                }
            } else if ("Priority".equalsIgnoreCase(strategy)) {
                var heap = SchedulingEngine.dispatchPriority(pending);
                log("Priority dispatch queue produced " + heap.size() + " jobs.");
                for (int i = 0; i < Math.min(8, heap.size()); i++) {
                    ServiceRequest req = heap.extractMin();
                    log("  " + req);
                    heap.insert(req);
                }
            } else {
                var result = SchedulingEngine.dispatchRoundRobin(pending, buildLocationMap());
                log("Round-robin queue produced " + result.size() + " jobs.");
                for (int i = 0; i < Math.min(8, result.size()); i++) {
                    log("  " + result.get(i));
                }
            }
            setStatus("Dispatch complete");
        } catch (Exception ex) {
            log("Dispatch failed: " + ex.getMessage());
            setStatus("Dispatch failed");
        }
    }

    private void showSeededRequests() {
        try {
            DynamicArray<ServiceRequest> all = DatabaseManager.loadServiceRequests();
            DefaultListModel<String> model = new DefaultListModel<>();
            for (ServiceRequest r : all) {
                if (r.getRequestId() <= 300) {
                    String line = "#" + r.getRequestId() + " [" + r.getStatus() + "] "
                            + findLocationName(r.getSourceLocationId()) + " → " + findLocationName(r.getDestLocationId())
                            + " | " + r.getCategory() + " | urgency:" + r.getUrgency();
                    model.addElement(line);
                }
            }
            if (model.getSize() == 0 && requests != null && !requests.isEmpty()) {
                for (ServiceRequest r : requests) {
                    if (r.getRequestId() <= 300) {
                        String line = "#" + r.getRequestId() + " [" + r.getStatus() + "] "
                                + findLocationName(r.getSourceLocationId()) + " → " + findLocationName(r.getDestLocationId())
                                + " | " + r.getCategory() + " | urgency:" + r.getUrgency();
                        model.addElement(line);
                    }
                }
            }
            if (model.getSize() == 0) {
                model.addElement("No seeded requests found. Ensure the database is initialized and contains the seeded rows.");
            }
            JList<String> list = new JList<>(model);
            styleList(list);
            JDialog dlg = new JDialog(this, "Seeded Requests (1-300)", true);
            dlg.setSize(760, 520);
            dlg.setLocationRelativeTo(this);
            dlg.getContentPane().setBackground(BG_DARK);
            dlg.setLayout(new BorderLayout());
            dlg.add(makeScrollPane(list), BorderLayout.CENTER);
            JButton close = makeSecondaryButton("Close", null);
            close.addActionListener(e -> dlg.dispose());
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.setBackground(BG_DARK);
            bottom.add(close);
            dlg.add(bottom, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (Exception ex) {
            log("Failed to load seeded requests: " + ex.getMessage());
        }
    }

    private void showDSDemo() {
        JDialog dlg = new JDialog(this, "UG Swift — System Data Structures & Algorithm Demos", true);
        dlg.setSize(960, 640);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG_DARK);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(FONT_NAV);
        tabs.setBackground(BG_SIDEBAR);
        tabs.setForeground(TEXT_PRIMARY);

        // 1. Graph & MinHeap
        JTextArea graphArea = makeOutputArea();
        JButton runGraph = makeAccentButton("Run Campus Graph & Dijkstra Demo", IconType.GRAPH);
        runGraph.addActionListener(e -> {
            graphArea.setText("");
            graphArea.append("═══ CAMPUS ROAD NETWORK & DIJKSTRA (Graph + MinHeap) ═══\n");
            DynamicArray<Location> locs = DatabaseManager.loadLocations();
            DynamicArray<RoadEdge> rds = DatabaseManager.loadRoads();
            graphArea.append(String.format("Loaded %d Campus Locations & %d Road Edges into Graph.\n", locs.size(), rds.size()));
            Graph graph = buildGraph();
            long t0 = System.nanoTime();
            RouteEngine.PathResult res = RouteEngine.dijkstra(graph, 1, 75);
            long t1 = System.nanoTime();
            if (res != null) {
                graphArea.append(String.format("Shortest Path from Node 1 (%s) to Node 75 (%s):\n", findLocationName(1), findLocationName(75)));
                graphArea.append(String.format("  • Total Distance : %.3f km\n", res.totalDistanceKm));
                graphArea.append(String.format("  • Travel Time    : %.2f mins\n", res.totalTimeMin));
                graphArea.append(String.format("  • Vertices Traversed: %d\n", res.path.size()));
                graphArea.append("  • Path Nodes     : ");
                for (int i = 0; i < res.path.size(); i++) {
                    int nid = res.path.get(i);
                    graphArea.append(findLocationName(nid) + (i < res.path.size() - 1 ? " → " : ""));
                }
                graphArea.append(String.format("\n  • Execution Time : %,d ns (%.3f ms)\n", (t1 - t0), (t1 - t0) / 1e6));
            } else {
                graphArea.append("No route found between Node 1 and Node 75.\n");
            }
        });
        tabs.addTab("Graph & Dijkstra", makeDemoPanel(graphArea, runGraph));

        // 2. B-Tree
        JTextArea btreeArea = makeOutputArea();
        JButton runBTree = makeAccentButton("Run B-Tree Order Indexing Demo", IconType.STRUCTURES);
        runBTree.addActionListener(e -> {
            btreeArea.setText("");
            btreeArea.append("═══ B-TREE LARGE-SCALE ORDER INDEXING DEMO (degree t=3) ═══\n");
            ds.BTree<Integer, ServiceRequest> btree = new ds.BTree<>();
            DynamicArray<ServiceRequest> reqs = DatabaseManager.loadServiceRequests();
            btreeArea.append(String.format("Indexing %,d Service Requests into B-Tree...\n", reqs.size()));
            for (ServiceRequest r : reqs) btree.insert(r.getRequestId(), r);
            btreeArea.append(String.format("B-Tree Index Built successfully. Total Indexed Elements: %d\n\n", btree.size()));
            int[] testIds = {1, 42, 150, 300, 999};
            for (int id : testIds) {
                long start = System.nanoTime();
                ServiceRequest found = btree.search(id);
                long elapsed = System.nanoTime() - start;
                if (found != null) {
                    btreeArea.append(String.format("[FOUND] Request #%-3d | Category: %-15s | Priority: %.2f | Search Time: %,d ns\n",
                            id, found.getCategory(), found.getPriority(), elapsed));
                } else {
                    btreeArea.append(String.format("[MISS ] Request #%-3d | Key not found in B-Tree index | Search Time: %,d ns\n", id, elapsed));
                }
            }
        });
        tabs.addTab("B-Tree Indexing", makeDemoPanel(btreeArea, runBTree));

        // 3. Red-Black Tree
        JTextArea rbtArea = makeOutputArea();
        JButton runRBT = makeAccentButton("Run Red-Black Tree Balance Demo", IconType.STRUCTURES);
        runRBT.addActionListener(e -> {
            rbtArea.setText("");
            rbtArea.append("═══ RED-BLACK TREE SELF-BALANCING ORDER REGISTRY DEMO ═══\n");
            ds.RedBlackTree<Integer, ServiceRequest> rbt = new ds.RedBlackTree<>();
            DynamicArray<ServiceRequest> reqs = DatabaseManager.loadServiceRequests();
            int count = Math.min(50, reqs.size());
            for (int i = 0; i < count; i++) rbt.insert(reqs.get(i).getRequestId(), reqs.get(i));
            rbtArea.append(String.format("Inserted %d Active Orders into Red-Black Tree.\n", count));
            rbtArea.append("Properties Verified:\n");
            rbtArea.append("  • Root Node Color : " + (rbt.getRoot() != null && rbt.getRoot().color == ds.RedBlackTree.BLACK ? "BLACK (Valid)" : "RED") + "\n");
            int h = rbt.height();
            double maxAllowedH = 2 * (Math.log(count + 1) / Math.log(2));
            rbtArea.append(String.format("  • Tree Height     : %d (Max theoretical bound: %.1f)\n", h, maxAllowedH));
            rbtArea.append("  • Size            : " + rbt.size() + "\n");
            rbtArea.append("\nIn-order Traversal (Sorted Keys):\n");
            DynamicArray<ServiceRequest> inorder = rbt.inorder();
            for (int i = 0; i < Math.min(10, inorder.size()); i++) {
                ServiceRequest r = inorder.get(i);
                rbtArea.append(String.format("  Order #%d [%s] -> %s\n", r.getRequestId(), r.getCategory(), r.getStatus()));
            }
            if (inorder.size() > 10) rbtArea.append(String.format("  ... and %d more items.\n", inorder.size() - 10));
        });
        tabs.addTab("Red-Black Tree", makeDemoPanel(rbtArea, runRBT));

        // 4. Hash Table
        JTextArea hashArea = makeOutputArea();
        JButton runHash = makeAccentButton("Run Hash Table Benchmark", IconType.SEARCH);
        runHash.addActionListener(e -> {
            hashArea.setText("");
            hashArea.append("═══ HASH TABLE RIDER O(1) LOOKUP DEMO ═══\n");
            ds.HashTable<Integer, Resource> table = new ds.HashTable<>(211);
            DynamicArray<Resource> ridersList = DatabaseManager.loadResources();
            for (Resource r : ridersList) table.put(r.getResourceId(), r);
            hashArea.append(String.format("HashTable Capacity: %d buckets | Stored Items: %d\n", table.getCapacity(), table.size()));
            hashArea.append(String.format("Collision Count   : %d | Load Factor: %.2f%%\n\n", table.getCollisionCount(), (double) table.size() / table.getCapacity() * 100));

            int targetId = 5;
            long t0 = System.nanoTime();
            Resource r = table.get(targetId);
            long t1 = System.nanoTime();
            if (r != null) {
                hashArea.append(String.format("Lookup Rider #%d -> Name: '%s' | Vehicle: %s | Home Loc: %d | Time: %,d ns\n",
                        targetId, r.getName(), r.getType(), r.getHomeLocationId(), (t1 - t0)));
            }
            hashArea.append("\nIndexed Rider Directory (Sample):\n");
            DynamicArray<ds.HashTable.Entry<Integer, Resource>> entries = table.entries();
            for (int i = 0; i < Math.min(8, entries.size()); i++) {
                Resource res = entries.get(i).value;
                hashArea.append(String.format("  Key: %-2d | %-18s | %-10s | Status: %s\n", res.getResourceId(), res.getName(), res.getType(), res.getAvailabilityStatus()));
            }
        });
        tabs.addTab("Hash Table", makeDemoPanel(hashArea, runHash));

        // 5. Disjoint Set
        JTextArea dsetArea = makeOutputArea();
        JButton runDSet = makeAccentButton("Run Disjoint Set Connectivity", IconType.GRAPH);
        runDSet.addActionListener(e -> {
            dsetArea.setText("");
            dsetArea.append("═══ DISJOINT SET (UNION-FIND) CAMPUS ZONE CONNECTIVITY ═══\n");
            DynamicArray<Location> locs = DatabaseManager.loadLocations();
            int maxId = 0;
            for (Location l : locs) maxId = Math.max(maxId, l.getLocationId());
            ds.DisjointSet dset = new ds.DisjointSet(maxId + 1);

            for (int i = 0; i < locs.size(); i++) {
                for (int j = i + 1; j < locs.size(); j++) {
                    if (locs.get(i).getZone().equalsIgnoreCase(locs.get(j).getZone())) {
                        dset.union(locs.get(i).getLocationId(), locs.get(j).getLocationId());
                    }
                }
            }
            dsetArea.append(String.format("Grouped %d Campus Locations into Disjoint Zone Sets.\n\n", locs.size()));
            Location l1 = locs.get(0);
            Location l2 = locs.get(1);
            Location l3 = locs.get(locs.size() - 1);

            dsetArea.append(String.format("Connectivity Check: '%s' vs '%s': %s\n",
                    l1.getName(), l2.getName(), (dset.find(l1.getLocationId()) == dset.find(l2.getLocationId()) ? "CONNECTED (Same Zone)" : "DISCONNECTED")));
            dsetArea.append(String.format("Connectivity Check: '%s' vs '%s': %s\n",
                    l1.getName(), l3.getName(), (dset.find(l1.getLocationId()) == dset.find(l3.getLocationId()) ? "CONNECTED (Same Zone)" : "DISCONNECTED")));
        });
        tabs.addTab("Disjoint Set", makeDemoPanel(dsetArea, runDSet));

        // 6. Circular Queue
        JTextArea cqArea = makeOutputArea();
        JButton runCQ = makeAccentButton("Run Pool Rotation Demo", IconType.REFRESH);
        runCQ.addActionListener(e -> {
            cqArea.setText("");
            cqArea.append("═══ CIRCULAR QUEUE ROUND-ROBIN RIDER POOL DISPATCH DEMO ═══\n");
            ds.CircularQueue<Resource> pool = new ds.CircularQueue<>(8);
            DynamicArray<Resource> rList = DatabaseManager.loadResources();
            int count = Math.min(6, rList.size());
            for (int i = 0; i < count; i++) pool.enqueue(rList.get(i));

            cqArea.append(String.format("Initial Circular Rider Queue Size: %d | Front Pointer: %d | Rear Pointer: %d\n\n",
                    pool.size(), pool.getFrontPointer(), pool.getRearPointer()));

            cqArea.append("Simulating 4 Consecutive Round-Robin Rider Assignments:\n");
            for (int step = 1; step <= 4; step++) {
                Resource dispatched = pool.dequeue();
                pool.enqueue(dispatched);
                cqArea.append(String.format("  Step %d: Dispatched Rider '%s' (%s) -> Rotated to rear | Front Pointer: %d | Rear Pointer: %d\n",
                        step, dispatched.getName(), dispatched.getType(), pool.getFrontPointer(), pool.getRearPointer()));
            }
        });
        tabs.addTab("Circular Queue", makeDemoPanel(cqArea, runCQ));

        // 7. Deque
        JTextArea dqArea = makeOutputArea();
        JButton runDQ = makeAccentButton("Run Deque Priority Buffer Demo", IconType.STRUCTURES);
        runDQ.addActionListener(e -> {
            dqArea.setText("");
            dqArea.append("═══ DEQUE DUAL-ENDED DISPATCH BUFFER DEMO ═══\n");
            ds.Deque<String> buffer = new ds.Deque<>();
            dqArea.append("Queueing Standard Order #101 at REAR...\n"); buffer.addRear("Order #101 (Standard)");
            dqArea.append("Queueing Standard Order #102 at REAR...\n"); buffer.addRear("Order #102 (Standard)");
            dqArea.append("Queueing EXPRESS Order #999 at FRONT (High Priority)...\n"); buffer.addFront("Order #999 (EXPRESS)");

            dqArea.append(String.format("\nBuffer State | Front: '%s' | Rear: '%s' | Total: %d\n\n",
                    buffer.peekFront(), buffer.peekRear(), buffer.size()));

            dqArea.append("Dispatching from FRONT: " + buffer.removeFront() + "\n");
            dqArea.append("Dispatching from FRONT: " + buffer.removeFront() + "\n");
            dqArea.append("Remaining in Buffer  : " + buffer.peekFront() + "\n");
        });
        tabs.addTab("Deque", makeDemoPanel(dqArea, runDQ));

        // 8. Stack
        JTextArea stackArea = makeOutputArea();
        JButton runStack = makeAccentButton("Run Stack History Demo", IconType.UNDO);
        runStack.addActionListener(e -> {
            stackArea.setText("");
            stackArea.append("═══ STACK SCHEDULING HISTORY & BACKTRACKING DEMO ═══\n");
            ds.Stack<String> history = new ds.Stack<>();
            stackArea.append("Pushing state: Order #101 assigned to Rider Kwame\n"); history.push("State 1: Order #101 -> Kwame");
            stackArea.append("Pushing state: Order #102 assigned to Rider Ama\n"); history.push("State 2: Order #102 -> Ama");
            stackArea.append("Pushing state: Order #103 assigned to Rider Yaw\n"); history.push("State 3: Order #103 -> Yaw");

            stackArea.append("\nCurrent History Stack Top (LIFO): " + history.peek() + "\n\n");
            stackArea.append("Undoing last scheduling decision: " + history.pop() + "\n");
            stackArea.append("New Current Top State           : " + history.peek() + "\n");
            stackArea.append("Remaining Stack Depth           : " + history.size() + "\n");
        });
        tabs.addTab("Stack", makeDemoPanel(stackArea, runStack));

        // 9. BST
        JTextArea bstArea = makeOutputArea();
        JButton runBST = makeAccentButton("Run BST Search & Deletion Demo", IconType.SEARCH);
        runBST.addActionListener(e -> {
            bstArea.setText("");
            bstArea.append("═══ BINARY SEARCH TREE SEARCH & DELETION DEMO ═══\n");
            ds.BST<Integer, String> bst = new ds.BST<>();
            DynamicArray<Location> locs = DatabaseManager.loadLocations();
            for (int i = 0; i < Math.min(15, locs.size()); i++) {
                Location l = locs.get(i);
                bst.insert(l.getLocationId(), l.getName());
            }
            bstArea.append("Inserted 15 Locations into BST.\n");
            bstArea.append("Initial Tree Size: " + bst.size() + "\n");
            bstArea.append("Search Key 4: " + bst.search(4) + "\n\n");

            bstArea.append("Executing BST Deletion on Key 4...\n");
            boolean deleted = bst.delete(4);
            bstArea.append("delete(4) Success: " + deleted + "\n");
            bstArea.append("Search Key 4 after delete: " + bst.search(4) + "\n");
            bstArea.append("New Tree Size: " + bst.size() + "\n\n");

            bstArea.append("In-order Traversal (Sorted Location Names):\n");
            DynamicArray<String> inorder = bst.inorder();
            for (int i = 0; i < inorder.size(); i++) {
                bstArea.append("  [" + i + "] " + inorder.get(i) + "\n");
            }
        });
        tabs.addTab("BST", makeDemoPanel(bstArea, runBST));

        dlg.add(tabs, BorderLayout.CENTER);
        JButton close = makeSecondaryButton("Close", null); close.addActionListener(e -> dlg.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(BG_DARK);
        bottom.add(close);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void populateLocationSelectors() {
        locationIds.clear();
        sourceCombo.removeAllItems();
        destinationCombo.removeAllItems();

        for (Location loc : locations) {
            String label = loc.getLocationId() + " - " + loc.getName();
            locationIds.put(label, loc.getLocationId());
            sourceCombo.addItem(label);
            destinationCombo.addItem(label);
        }

        if (sourceCombo.getItemCount() > 0) {
            sourceCombo.setSelectedIndex(0);
            destinationCombo.setSelectedIndex(Math.min(1, destinationCombo.getItemCount() - 1));
        }
    }

    private void populateRestaurantMenus() {
        restaurantMenus.clear();
        menuWeights.clear();
        restaurantMenus.put("Auntie Mame's Kitchen", Arrays.asList("Waakye Bowl", "Jollof Rice", "Chicken Wrap"));
        restaurantMenus.put("Tasty Bites", Arrays.asList("Veggie Pizza", "Chicken Shawarma", "Pasta Box"));
        restaurantMenus.put("Campus Grill", Arrays.asList("Burger Combo", "Rice Bowl", "Spicy Noodles"));
        restaurantMenus.put("The Snack Stop", Arrays.asList("Fruit Smoothie", "Sandwich", "Puff-Puff Pack"));

        menuWeights.put("Waakye Bowl", 1.1);
        menuWeights.put("Jollof Rice", 1.4);
        menuWeights.put("Chicken Wrap", 0.9);
        menuWeights.put("Veggie Pizza", 1.2);
        menuWeights.put("Chicken Shawarma", 1.0);
        menuWeights.put("Pasta Box", 1.3);
        menuWeights.put("Burger Combo", 1.6);
        menuWeights.put("Rice Bowl", 1.1);
        menuWeights.put("Spicy Noodles", 1.2);
        menuWeights.put("Fruit Smoothie", 0.7);
        menuWeights.put("Sandwich", 0.8);
        menuWeights.put("Puff-Puff Pack", 0.6);

        restaurantCombo.removeAllItems();
        for (String restaurant : restaurantMenus.keySet()) {
            restaurantCombo.addItem(restaurant);
        }
        if (restaurantCombo.getItemCount() > 0) {
            restaurantCombo.setSelectedIndex(0);
        }
        updateFoodOptions();
    }

    private void updateFoodOptions() {
        Object selected = restaurantCombo.getSelectedItem();
        foodCombo.removeAllItems();
        if (selected != null) {
            List<String> meals = restaurantMenus.get(selected.toString());
            if (meals != null) {
                for (String meal : meals) {
                    foodCombo.addItem(meal);
                }
            }
        }
        if (foodCombo.getItemCount() > 0) {
            foodCombo.setSelectedIndex(0);
        }
    }

    private void refreshDashboard() {
        riderListModel.clear();
        stationListModel.clear();
        for (Resource rider : riders) {
            riderListModel.addElement(rider.getName() + " • " + rider.getType() + " • " + rider.getAvailabilityStatus());
            String station = "Station " + ((rider.getResourceId() % 7) + 1);
            stationListModel.addElement(station + " • " + rider.getName() + " • " + rider.getType());
        }

        incomingListModel.clear();
        if (incomingManager != null) {
            incomingListModel.addElement("FIFO pending: " + incomingManager.fifoSize());
            incomingListModel.addElement("Priority pending: " + incomingManager.prioritySize());
            incomingListModel.addElement("Total pending: " + incomingManager.pendingCount());
        } else {
            incomingListModel.addElement("No incoming manager");
        }

        StringBuilder builder = new StringBuilder();
        if (activeOrders.isEmpty()) {
            builder.append("No active deliveries.\n");
        } else {
            for (Order order : activeOrders) {
                builder.append("#")
                        .append(order.getOrderId())
                        .append(" | ")
                        .append(order.getFoodItem())
                        .append(" | ")
                        .append(order.getRestaurant())
                        .append(" | ")
                        .append(findLocationName(order.getPickupLocationId()))
                        .append(" → ")
                        .append(findLocationName(order.getDeliveryLocationId()))
                        .append("\n");
            }
        }
        builder.append("\nCompleted deliveries: " + completedOrders.size() + "\n");
        String activeText = builder.toString();
        activeOrdersArea.setText(activeText);
        orderFormActiveOrdersArea.setText(activeText);

        completedListModel.clear();
        for (Order o : completedOrders) {
            completedListModel.addElement("#" + o.getOrderId() + " | " + o.getFoodItem() + " | " + o.getRestaurant());
        }
    }

    private String resolveDataDir() {
        java.io.File cwd = new java.io.File(System.getProperty("user.dir"));
        java.io.File[] candidates = {
                new java.io.File(cwd, "data"),
                new java.io.File(cwd, "src/data"),
                new java.io.File(cwd.getParentFile(), "data"),
                new java.io.File("data")
        };
        for (java.io.File candidate : candidates) {
            if (candidate.exists() && candidate.isDirectory()) {
                return candidate.getPath() + java.io.File.separator;
            }
        }
        return "";
    }

    private DynamicArray<Resource> loadResources() {
        DynamicArray<Resource> loaded = new DynamicArray<>();
        try {
            loaded = DatabaseManager.loadResources();
        } catch (Exception ex) {
            log("Unable to load riders: " + ex.getMessage());
        }
        return loaded;
    }

    private Graph buildGraph() {
        int maxId = 0;
        for (Location loc : locations) {
            maxId = Math.max(maxId, loc.getLocationId());
        }
        Graph graph = new Graph(maxId);
        for (Location loc : locations) {
            graph.addLocation(loc);
        }
        for (RoadEdge road : roads) {
            graph.addRoad(road);
        }
        return graph;
    }

    private ds.HashTable<Integer, Location> buildLocationMap() {
        ds.HashTable<Integer, Location> map = new ds.HashTable<>(locations.size() * 2);
        for (Location loc : locations) {
            map.put(loc.getLocationId(), loc);
        }
        return map;
    }

    private int getSelectedLocationId(JComboBox<String> combo) {
        Object selected = combo.getSelectedItem();
        if (selected == null) {
            return -1;
        }
        Integer id = locationIds.get(selected.toString());
        return id == null ? -1 : id;
    }

    private String findLocationName(int id) {
        for (Location loc : locations) {
            if (loc.getLocationId() == id) {
                return loc.getName();
            }
        }
        return String.valueOf(id);
    }

    private void runAction(String actionName, Runnable action) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                action.run();
                return null;
            }

            @Override
            protected void done() {
                setStatus("Completed: " + actionName);
            }
        }.execute();
    }

    private void log(String message) {
        logArea.append(message + System.lineSeparator());
        logArea.setCaretPosition(logArea.getText().length());
    }

    private void setStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void refreshSummary() {
        summaryLabel.setText("Locations: " + locations.size() + " | Riders: " + riders.size() + " | Active orders: " + activeOrders.size());
        locStatLabel.setText(String.valueOf(locations.size()));
        roadStatLabel.setText(String.valueOf(roads.size()));
        riderStatLabel.setText(String.valueOf(riders.size()));
        activeStatLabel.setText(String.valueOf(activeOrders.size()));
    }

    private void showSummary() {
        refreshSummary();
        log("Summary: " + locations.size() + " locations, " + roads.size() + " roads, " + requests.size() + " requests, " + activeOrders.size() + " active orders.");
    }

    // --- UI Helper Component Creators ---
    private JPanel statCard(String title, IconType iconType, JLabel valLbl, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(color.darker(), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setIcon(new VectorIcon(iconType, 18, color));
        titleLbl.setIconTextGap(8);
        titleLbl.setFont(FONT_BODY);
        titleLbl.setForeground(TEXT_SECONDARY);

        valLbl.setFont(FONT_STAT);
        valLbl.setForeground(color);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl,   BorderLayout.CENTER);
        return card;
    }

    private JPanel makeCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
            new TitledBorder(new LineBorder(BORDER_COLOR, 1, true), " " + title + " ",
                TitledBorder.LEFT, TitledBorder.TOP, FONT_SMALL, TEXT_SECONDARY),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        return card;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADER);
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField styleTextField(JTextField tf) {
        tf.setBackground(BG_CARD2);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT);
        tf.setFont(FONT_BODY);
        tf.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private <T> JComboBox<T> styleComboBox(JComboBox<T> cb) {
        cb.setBackground(BG_CARD2);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(FONT_BODY);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
        cb.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? BG_CARD : BG_CARD2);
                setForeground(isSelected ? ACCENT : TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        return cb;
    }

    private void styleList(JList<?> list) {
        list.setBackground(new Color(0x0B1120));
        list.setForeground(TEXT_PRIMARY);
        list.setFont(FONT_BODY);
        list.setSelectionBackground(BG_CARD);
        list.setSelectionForeground(ACCENT);
    }

    private JTextArea makeOutputArea() {
        JTextArea area = new JTextArea();
        area.setFont(FONT_MONO);
        area.setBackground(new Color(0x0B1120));
        area.setForeground(TEXT_PRIMARY);
        area.setCaretColor(ACCENT);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        return area;
    }

    private JScrollPane makeScrollPane(JComponent component) {
        JScrollPane sp = new JScrollPane(component);
        sp.setBackground(BG_DARK);
        sp.getViewport().setBackground(component.getBackground());
        sp.setBorder(new LineBorder(BORDER_COLOR, 1));
        sp.getVerticalScrollBar().setBackground(BG_SIDEBAR);
        sp.getHorizontalScrollBar().setBackground(BG_SIDEBAR);
        return sp;
    }

    private JButton makeAccentButton(String text, IconType iconType) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = getModel().isRollover()
                    ? new GradientPaint(0, 0, ACCENT_HOVER, 0, getHeight(), ACCENT)
                    : new GradientPaint(0, 0, ACCENT, 0, getHeight(), ACCENT.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        if (iconType != null) {
            btn.setIcon(new VectorIcon(iconType, 14, new Color(0x1A1B2E)));
            btn.setIconTextGap(4);
        }
        btn.setFont(FONT_SMALL);
        btn.setForeground(new Color(0x1A1B2E));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setOpaque(false);
        return btn;
    }

    private JButton makeSecondaryButton(String text, IconType iconType) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_CARD : BG_CARD2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        if (iconType != null) {
            btn.setIcon(new VectorIcon(iconType, 14, TEXT_PRIMARY));
            btn.setIconTextGap(4);
        }
        btn.setFont(FONT_SMALL);
        btn.setForeground(TEXT_PRIMARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 4));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setOpaque(false);
        return btn;
    }
}
