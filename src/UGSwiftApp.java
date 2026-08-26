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
import engines.RouteEngine;
import engines.SchedulingEngine;
import models.Location;
import models.Order;
import models.Resource;
import models.RoadEdge;
import models.ServiceRequest;

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
        {IconType.AUDIT, "Activity & Logs"}
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

        JPanel buttonGrid = new JPanel(new GridLayout(3, 3, 6, 6));
        buttonGrid.setOpaque(false);
        buttonGrid.setPreferredSize(new Dimension(0, 135));
        buttonGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 135));
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
        formCard.add(buttonGrid);

        restaurantCombo.addActionListener(e -> updateFoodOptions());
        orderBtn.addActionListener(e -> placeOrder());
        routeBtn.addActionListener(e -> computeRoute());
        dispatchBtn.addActionListener(e -> runDispatch("Nearest Rider"));
        refreshBtn.addActionListener(e -> loadData());
        initBtn.addActionListener(e -> runAction("initializing data", this::initializeData));
        generateBtn.addActionListener(e -> runAction("generating roads", this::generateRoadNetwork));
        seededBtn.addActionListener(e -> showSeededRequests());
        dsDemoBtn.addActionListener(e -> showDSDemo());
        openMapBtn.addActionListener(e -> {
            try {
                Class<?> launcherClass = Class.forName("tools.CampusMapLauncher");
                launcherClass.getMethod("openMap").invoke(null);
            } catch (Exception ex) {
                log("Failed to open campus map: " + ex.getMessage());
            }
        });

        // Right Column: System Overview & Instructions
        JPanel overviewCard = makeCard("Campus Dispatch Guidance");
        overviewCard.setLayout(new BorderLayout(12, 12));

        JTextArea guideText = new JTextArea(
            "★ Ghana Smart Service Operations Optimizer ★\n\n" +
            "How to dispatch food & service orders:\n" +
            "  1. Select a campus restaurant and menu meal.\n" +
            "  2. Choose pickup and delivery locations from the 75+ campus nodes.\n" +
            "  3. Set priority level (Standard, Express, Family Pack).\n" +
            "  4. Click 'Place Order' to assign the nearest rider via Dijkstra shortest path.\n\n" +
            "Key Platform Capabilities:\n" +
            "  • Automatic Background Delivery Processor\n" +
            "  • Fair Circular Driver Pool Rotation (O(1))\n" +
            "  • MinHeap Priority Queue Scheduling\n" +
            "  • B-Tree & Red-Black Tree Order Indexing\n" +
            "  • Disjoint-Set Campus Connectivity Verification"
        );
        guideText.setFont(FONT_BODY);
        guideText.setForeground(TEXT_SECONDARY);
        guideText.setBackground(BG_CARD);
        guideText.setEditable(false);
        guideText.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        overviewCard.add(guideText, BorderLayout.CENTER);

        center.add(formCard);
        center.add(overviewCard);
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

        JPanel incomingControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        incomingControls.setOpaque(false);
        JButton processNextBtn = makeAccentButton("Process Next", IconType.RUN);
        autoToggleBtn = makeSecondaryButton("Start Auto", IconType.REFRESH);
        intervalField = styleTextField(new JTextField("10", 4));
        bulkCountField = styleTextField(new JTextField("5", 4));
        bulkProcessBtn = makeSecondaryButton("Process N", IconType.SORT);

        JLabel intLbl = new JLabel("Interval(s):"); intLbl.setForeground(TEXT_SECONDARY); intLbl.setFont(FONT_SMALL);
        JLabel bulkLbl = new JLabel("Bulk:"); intLbl.setForeground(TEXT_SECONDARY); intLbl.setFont(FONT_SMALL);

        incomingControls.add(intLbl);
        incomingControls.add(intervalField);
        incomingControls.add(autoToggleBtn);
        incomingControls.add(processNextBtn);
        incomingControls.add(bulkLbl);
        incomingControls.add(bulkCountField);
        incomingControls.add(bulkProcessBtn);
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

        // 1. Graph & Dijkstra
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
        JPanel graphPanel = makeDemoPanel(graphArea, runGraph);
        tabs.addTab("Graph & Dijkstra", graphPanel);

        // 2. B-Tree
        JTextArea btreeArea = makeOutputArea();
        JButton runBTree = makeAccentButton("Run B-Tree Order Indexing Demo", IconType.STRUCTURES);
        runBTree.addActionListener(e -> {
            btreeArea.setText("");
            btreeArea.append("═══ B-TREE LARGE-SCALE ORDER INDEXING DEMO (degree t=3) ═══\n");
            ds.BTree<Integer, ServiceRequest> btree = new ds.BTree<>();
            DynamicArray<ServiceRequest> reqs = DatabaseManager.loadServiceRequests();
            btreeArea.append(String.format("Indexing %,d Service Requests into B-Tree...\n", reqs.size()));
            for (ServiceRequest r : reqs) {
                btree.insert(r.getRequestId(), r);
            }
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
        JButton runRBT = makeAccentButton("Run Red-Black Tree Demo", IconType.STRUCTURES);
        runRBT.addActionListener(e -> {
            rbtArea.setText("");
            rbtArea.append("═══ RED-BLACK TREE SELF-BALANCING ORDER REGISTRY DEMO ═══\n");
            ds.RedBlackTree<Integer, ServiceRequest> rbt = new ds.RedBlackTree<>();
            DynamicArray<ServiceRequest> reqs = DatabaseManager.loadServiceRequests();
            int count = Math.min(50, reqs.size());
            for (int i = 0; i < count; i++) {
                rbt.insert(reqs.get(i).getRequestId(), reqs.get(i));
            }
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
        JButton runDSet = makeAccentButton("Run Disjoint Set Demo", IconType.GRAPH);
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
        JButton runBST = makeAccentButton("Run BST Search & Delete Demo", IconType.SEARCH);
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
    private void startCompletionWatcher() {
        javax.swing.Timer timer = new javax.swing.Timer(10000, e -> checkForCompletedOrders());
        timer.setRepeats(true);
        timer.start();
    }

    private void checkForCompletedOrders() {
        try {
            double currentMinutes = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute() + LocalTime.now().getSecond() / 60.0;
            List<ServiceRequest> toComplete = new ArrayList<>();
            for (ServiceRequest req : requests) {
                if (req.getStatus() != null && req.getStatus().equalsIgnoreCase("ASSIGNED")) {
                    if (currentMinutes >= req.getDeadlineMin()) {
                        toComplete.add(req);
                    }
                }
            }

            for (ServiceRequest req : toComplete) {
                req.setStatus("DELIVERED");
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

                Order moved = null;
                for (Order o : activeOrders) {
                    if (o.getPickupLocationId() == req.getSourceLocationId() && o.getDeliveryLocationId() == req.getDestLocationId() && o.getFoodItem().equals(req.getCategory())) {
                        moved = o;
                        break;
                    }
                }
                if (moved != null) {
                    activeOrders.removeElement(moved);
                    completedOrders.add(moved);
                }
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

            Order order = new Order(
                    1000 + (int) (Math.random() * 9000),
                    "Queued",
                    "QueuedVendor",
                    req.getCategory(),
                    1.0,
                    req.getSourceLocationId(),
                    req.getDestLocationId(),
                    req.getTimeSubmittedMin(),
                    req.getStatus(),
                    req.getAssignedRiderId()
            );

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
            loadData();
            log("Road network generation completed.");
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
        RouteEngine.PathResult result = RouteEngine.dijkstra(graph, srcId, dstId);
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
                1000 + (int) (Math.random() * 9000),
                customerName,
                restaurant,
                meal,
                weightKg,
                pickupId,
                deliveryId,
                480.0,
                "PENDING",
                -1
        );
        ServiceRequest request = new ServiceRequest(
                10000 + activeOrders.size() + requests.size(),
                pickupId,
                deliveryId,
                meal,
                "Express".equalsIgnoreCase(priority) ? 4 : ("Family Pack".equalsIgnoreCase(priority) ? 3 : 2),
                480.0,
                480.0,
                "PENDING",
                -1
        );

        boolean highPriority = "Express".equalsIgnoreCase(priority);
        incomingManager.submit(request, highPriority);
        requests.add(request);
        AuditLog.orderCreated(order);

        Resource assigned = driverPool.nextSuitable(order, locations, roads);
        DeliveryEngine.AssignmentResult result = null;
        if (assigned != null) {
            ds.Graph graph = buildGraph();
            var path = RouteEngine.dijkstra(graph, assigned.getCurrentLocationId(), pickupId);
            if (path != null) {
                result = new DeliveryEngine.AssignmentResult(assigned, path.totalDistanceKm, path.totalTimeMin);
            }
        }

        if (result == null) {
            result = DeliveryEngine.assignRider(order, riders, locations, roads);
        }

        if (result == null || result.rider == null) {
            log("No rider could be assigned for this order right now. Order queued.");
            setStatus("Order queued");
            refreshDashboard();
            refreshAuditTrail();
            refreshSummary();
            return;
        }

        // Claim the rider in memory before any of the work below, so a failure
        // while computing the route or persisting cannot leave them looking
        // AVAILABLE and get them handed a second order. assignOrder both records
        // the order they are carrying and marks them BUSY.
        result.rider.assignOrder(order.getOrderId());
        double deliveryDuration = 0.0;
        try {
            Graph graph = buildGraph();
            var pd = RouteEngine.dijkstra(graph, order.getPickupLocationId(), order.getDeliveryLocationId());
            if (pd != null) {
                deliveryDuration = result.estimatedTimeMin + pd.totalTimeMin;
            } else {
                deliveryDuration = DeliveryEngine.estimateDeliveryDuration(order, result.distanceKm, result.rider);
            }
        } catch (Exception ex) {
            deliveryDuration = DeliveryEngine.estimateDeliveryDuration(order, result.distanceKm, result.rider);
        }

        request.setAssignedRiderId(result.rider.getResourceId());
        request.setStatus("ASSIGNED");
        request = new ServiceRequest(request.getRequestId(), request.getSourceLocationId(), request.getDestLocationId(), request.getCategory(), request.getUrgency(), request.getTimeSubmittedMin(), 480.0 + deliveryDuration, request.getStatus(), request.getAssignedRiderId());
        try {
            DatabaseManager.saveServiceRequest(request);
            DatabaseManager.updateResourceState(result.rider);
        } catch (Exception ex) {
            log("Warning: could not persist request or update rider status: " + ex.getMessage());
        }

        // Recorded outside the try/catch above because the assignment happened in
        // the running system either way - the audit trail describes what the app
        // did, not only what the database accepted.
        AuditLog.orderAssigned(order, result.rider, result.distanceKm, deliveryDuration);
        AuditLog.riderStatusChanged(result.rider, "BUSY");

        activeOrders.add(order);

        log("New order received for " + customerName + " from " + restaurant + " — " + meal);
        log("Assigned rider: " + result.rider.getName() + " (" + result.rider.getType() + ")");
        log("Estimated distance: " + String.format("%.2f km", result.distanceKm));
        log("Estimated time: " + String.format("%.1f min", result.estimatedTimeMin));
        log("Delivery window: " + String.format("%.1f min", deliveryDuration));
        log("Pickup: " + findLocationName(pickupId) + " → Delivery: " + findLocationName(deliveryId));
        setStatus("Order placed and rider assigned");
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
        activeOrdersArea.setText(builder.toString());

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
