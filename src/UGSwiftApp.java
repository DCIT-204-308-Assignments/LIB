import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

import ds.DynamicArray;
import ds.Graph;
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
    private final JComboBox<String> sourceCombo = new JComboBox<>();
    private final JComboBox<String> destinationCombo = new JComboBox<>();
    private final JComboBox<String> restaurantCombo = new JComboBox<>();
    private final JComboBox<String> foodCombo = new JComboBox<>();
    private final JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"Standard", "Express", "Family Pack"});
    private final JTextField customerField = new JTextField("Amina");
    private final JLabel summaryLabel = new JLabel("Preparing campus food delivery...");
    private final JLabel statusLabel = new JLabel("Status: idle");

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UGSwiftApp app = new UGSwiftApp();
            app.setVisible(true);
        });
    }

    public UGSwiftApp() {
        super("UG Swift - Campus Food Delivery");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);
        buildUI();
        initializeData();
        startCompletionWatcher();
    }

    private void startCompletionWatcher() {
        // Check every 10 seconds for completed deliveries
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
                // Persist status
                DatabaseManager.saveServiceRequest(req);
                // Free up rider
                int riderId = req.getAssignedRiderId();
                if (riderId > 0) {
                    DatabaseManager.updateResourceStatus(riderId, "AVAILABLE");
                    // Update in-memory copy
                    for (Resource r : riders) {
                        if (r.getResourceId() == riderId) {
                            r.setAvailabilityStatus("AVAILABLE");
                            break;
                        }
                    }
                }

                // Move matching order from active -> completed
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
            models.ServiceRequest req = incomingManager.next();
            if (req == null) {
                log("No incoming request retrieved.");
                return;
            }

            // build a lightweight order for assignment
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

            // attempt circular pool assignment
            models.Resource assigned = driverPool.nextSuitable(order, locations, roads);
            DeliveryEngine.AssignmentResult result = null;
            if (assigned != null) {
                ds.Graph graph = buildGraph();
                var path = RouteEngine.dijkstra(graph, assigned.getHomeLocationId(), order.getPickupLocationId());
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

            // assign and persist
            req.setAssignedRiderId(result.rider.getResourceId());
            req.setStatus("ASSIGNED");
            // compute realistic delivery duration: rider travel to pickup + pickup->delivery travel time
            double deliveryDuration = 0.0;
            try {
                ds.Graph fullGraph = buildGraph();
                RouteEngine.PathResult pd = RouteEngine.dijkstra(fullGraph, order.getPickupLocationId(), order.getDeliveryLocationId());
                if (pd != null) {
                    deliveryDuration = result.estimatedTimeMin + pd.totalTimeMin;
                } else {
                    deliveryDuration = DeliveryEngine.estimateDeliveryDuration(order, result.distanceKm, result.rider);
                }
            } catch (Exception ex) {
                deliveryDuration = DeliveryEngine.estimateDeliveryDuration(order, result.distanceKm, result.rider);
            }
            req = new models.ServiceRequest(req.getRequestId(), req.getSourceLocationId(), req.getDestLocationId(), req.getCategory(), req.getUrgency(), req.getTimeSubmittedMin(), 480.0 + deliveryDuration, req.getStatus(), req.getAssignedRiderId(), req.getDeliveredTimeMin());
            DatabaseManager.saveServiceRequest(req);
            DatabaseManager.updateResourceStatus(result.rider.getResourceId(), "BUSY");
            result.rider.setAvailabilityStatus("BUSY");
            activeOrders.add(order);
            log("Processed incoming request " + req.getRequestId() + " and assigned rider " + result.rider.getName());
            refreshDashboard();
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

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(new Color(0xF3F7FB));

        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD5E2EC), 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        header.setBackground(new Color(0xFFFFFF));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("UG Swift Delivery Console", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(0x133C5C));
        JLabel subtitle = new JLabel("Fast campus ordering, smart rider assignment, and live delivery tracking");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(0x5B6F7A));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        header.add(titlePanel, BorderLayout.WEST);

        JPanel meta = new JPanel(new GridLayout(1, 2, 10, 0));
        meta.setOpaque(false);
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        summaryLabel.setForeground(new Color(0x295A7A));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(0x1E7A3B));
        meta.add(summaryLabel);
        meta.add(statusLabel);
        header.add(meta, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);
        JPanel leftPanel = buildOrderPanel();
        JPanel rightPanel = buildDashboardPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        // Make right panel 30% larger than left: leftShare = 1 / (1 + 1.3) = ~0.43478
        double leftShare = 1.0 / 2.3;
        splitPane.setResizeWeight(leftShare);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerSize(8);
        // Set reasonable minimum sizes so user can drag the divider leftwards
        leftPanel.setMinimumSize(new java.awt.Dimension(220, 200));
        rightPanel.setMinimumSize(new java.awt.Dimension(320, 200));
        // If window width is known, set divider accordingly; fallback to default
        int w = getWidth();
        if (w > 0) {
            splitPane.setDividerLocation((int) (w * leftShare));
        }
        splitPane.setBorder(null);
        content.add(splitPane, BorderLayout.CENTER);
        root.add(content, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildOrderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD6E4EE), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        panel.setPreferredSize(new Dimension(430, 0));
        panel.setBackground(new Color(0xFFFFFF));

        JLabel customerLabel = new JLabel("Customer name");
        customerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        customerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(customerLabel);
        panel.add(customerField);
        panel.add(Box.createVerticalStrut(8));

        JLabel restaurantLabel = new JLabel("Restaurant");
        restaurantLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        restaurantCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(restaurantLabel);
        panel.add(restaurantCombo);
        panel.add(Box.createVerticalStrut(8));

        JLabel foodLabel = new JLabel("Meal");
        foodLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        foodCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(foodLabel);
        panel.add(foodCombo);
        panel.add(Box.createVerticalStrut(8));

        JLabel pickupLabel = new JLabel("Pickup location");
        pickupLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sourceCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(pickupLabel);
        panel.add(sourceCombo);
        panel.add(Box.createVerticalStrut(8));

        JLabel deliveryLabel = new JLabel("Delivery location");
        deliveryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        destinationCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(deliveryLabel);
        panel.add(destinationCombo);
        panel.add(Box.createVerticalStrut(8));

        JLabel priorityLabel = new JLabel("Order priority");
        priorityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        priorityCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(priorityLabel);
        panel.add(priorityCombo);
        panel.add(Box.createVerticalStrut(12));

        JButton orderBtn = new JButton("Place Order");
        JButton routeBtn = new JButton("Preview Route");
        JButton dispatchBtn = new JButton("Dispatch Queue");
        JButton refreshBtn = new JButton("Refresh Dashboard");
        JButton initBtn = new JButton("Initialize Data");
        JButton generateBtn = new JButton("Generate Roads");
        JButton seededBtn = new JButton("Show Seeded Requests");
        JButton dsDemoBtn = new JButton("DS Demo");
        JButton openMapBtn = new JButton("Open Campus Map");

        orderBtn.setBackground(new Color(0x1F7A1F));
        orderBtn.setForeground(Color.WHITE);
        routeBtn.setBackground(new Color(0x2F5D7C));
        routeBtn.setForeground(Color.WHITE);
        dispatchBtn.setBackground(new Color(0x8A4B08));
        dispatchBtn.setForeground(Color.WHITE);

        // use 3 columns and allow rows to wrap automatically
        JPanel buttonRow = new JPanel(new GridLayout(0, 3, 8, 8));
        buttonRow.setOpaque(false);
        buttonRow.add(orderBtn);
        buttonRow.add(routeBtn);
        buttonRow.add(dispatchBtn);
        buttonRow.add(refreshBtn);
        buttonRow.add(initBtn);
        buttonRow.add(generateBtn);
        buttonRow.add(seededBtn);
        buttonRow.add(dsDemoBtn);
        buttonRow.add(openMapBtn);
        panel.add(buttonRow);
        panel.add(Box.createVerticalStrut(12));

        JLabel guidanceTitle = new JLabel("How it works");
        guidanceTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(guidanceTitle);
        JTextArea guidance = new JTextArea(
                "1. Choose a restaurant and meal.\n"
                        + "2. Select pickup and destination.\n"
                        + "3. Place the order to auto-assign a rider."
        );
        guidance.setEditable(false);
        guidance.setBackground(panel.getBackground());
        guidance.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        guidance.setLineWrap(true);
        guidance.setWrapStyleWord(true);
        panel.add(guidance);
        panel.add(Box.createVerticalGlue());

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

        return panel;
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD6E4EE), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        panel.setBackground(new Color(0xF9FCFF));

        JPanel top = new JPanel(new GridLayout(1, 3, 8, 8));
        top.setOpaque(false);
        JPanel ridersPanel = new JPanel(new BorderLayout());
        ridersPanel.setBorder(BorderFactory.createTitledBorder("Available riders"));
        ridersPanel.setBackground(Color.WHITE);
        riderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ridersPanel.add(new JScrollPane(riderList), BorderLayout.CENTER);
        top.add(ridersPanel);

        JPanel stationsPanel = new JPanel(new BorderLayout());
        stationsPanel.setBorder(BorderFactory.createTitledBorder("Rider stations"));
        stationsPanel.setBackground(Color.WHITE);
        stationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stationsPanel.add(new JScrollPane(stationList), BorderLayout.CENTER);
        top.add(stationsPanel);

        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setBorder(BorderFactory.createTitledBorder("Active deliveries"));
        ordersPanel.setBackground(Color.WHITE);
        activeOrdersArea.setEditable(false);
        activeOrdersArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        activeOrdersArea.setLineWrap(true);
        activeOrdersArea.setWrapStyleWord(true);
        ordersPanel.add(new JScrollPane(activeOrdersArea), BorderLayout.CENTER);
        top.add(ordersPanel);

        panel.add(top, BorderLayout.NORTH);

        JPanel mid = new JPanel(new GridLayout(1, 2, 8, 8));
        JPanel incomingPanel = new JPanel(new BorderLayout());
        incomingPanel.setBorder(BorderFactory.createTitledBorder("Incoming queue"));
        incomingPanel.setBackground(Color.WHITE);
        incomingList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        incomingPanel.add(new JScrollPane(incomingList), BorderLayout.CENTER);
        JPanel incomingControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton processNextBtn = new JButton("Process next");
        autoToggleBtn = new JButton("Start Auto");
        intervalField = new JTextField("10", 4);
        bulkCountField = new JTextField("5", 4);
        bulkProcessBtn = new JButton("Process N");
        incomingControls.add(new JLabel("Interval(s):"));
        incomingControls.add(intervalField);
        incomingControls.add(autoToggleBtn);
        incomingControls.add(processNextBtn);
        incomingControls.add(new JLabel("Bulk:"));
        incomingControls.add(bulkCountField);
        incomingControls.add(bulkProcessBtn);
        incomingPanel.add(incomingControls, BorderLayout.SOUTH);
        mid.add(incomingPanel);

        JPanel completedPanel = new JPanel(new BorderLayout());
        completedPanel.setBorder(BorderFactory.createTitledBorder("Completed deliveries"));
        completedPanel.setBackground(Color.WHITE);
        completedList.setFont(new Font("Consolas", Font.PLAIN, 12));
        completedPanel.add(new JScrollPane(completedList), BorderLayout.CENTER);
        mid.add(completedPanel);

        panel.add(mid, BorderLayout.CENTER);

        processNextBtn.addActionListener(e -> processNextIncoming());
        autoToggleBtn.addActionListener(e -> toggleAutoProcessing());
        bulkProcessBtn.addActionListener(e -> startBulkProcessing());

        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setWrapStyleWord(true);
        logArea.setLineWrap(true);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Live activity"));
        panel.add(logScroll, BorderLayout.CENTER);

        return panel;
    }

    private void initializeData() {
        setStatus("Initializing database...");
        log("Initializing campus data and rider network...");
        try {
            String dataDir = resolveDataDir();
            DatabaseManager.initializeDatabase(dataDir + "locations.csv", dataDir + "roads.csv");
            loadData();
            setStatus("Ready for orders");
            log("The food delivery platform is ready.");
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
            // rebuild driver pool for fair circular assignment
            driverPool.rebuild(riders);
            populateLocationSelectors();
            populateRestaurantMenus();
            refreshDashboard();
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
        // Build service request and submit to incoming manager (FIFO / priority)
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

        // Try to assign a rider fairly using the circular driver pool first
        models.Resource assigned = driverPool.nextSuitable(order, locations, roads);
        DeliveryEngine.AssignmentResult result = null;
        if (assigned != null) {
            // compute path/time
            ds.Graph graph = buildGraph();
            var path = RouteEngine.dijkstra(graph, assigned.getHomeLocationId(), pickupId);
            if (path != null) {
                result = new DeliveryEngine.AssignmentResult(assigned, path.totalDistanceKm, path.totalTimeMin);
            }
        }

        // Fallback to earlier assignment algorithm
        if (result == null) {
            result = DeliveryEngine.assignRider(order, riders, locations, roads);
        }

        if (result == null || result.rider == null) {
            log("No rider could be assigned for this order right now. Order queued.");
            setStatus("Order queued");
            refreshDashboard();
            refreshSummary();
            return;
        }

        result.rider.setAvailabilityStatus("BUSY");
        // compute realistic delivery duration: rider travel to pickup + pickup->delivery travel time
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

        // update request as assigned with deadline
        request.setAssignedRiderId(result.rider.getResourceId());
        request.setStatus("ASSIGNED");
        request = new ServiceRequest(request.getRequestId(), request.getSourceLocationId(), request.getDestLocationId(), request.getCategory(), request.getUrgency(), request.getTimeSubmittedMin(), 480.0 + deliveryDuration, request.getStatus(), request.getAssignedRiderId());
        try {
            DatabaseManager.saveServiceRequest(request);
            DatabaseManager.updateResourceStatus(result.rider.getResourceId(), "BUSY");
        } catch (Exception ex) {
            log("Warning: could not persist request or update rider status: " + ex.getMessage());
        }

        activeOrders.add(order);

        log("New order received for " + customerName + " from " + restaurant + " — " + meal);
        log("Assigned rider: " + result.rider.getName() + " (" + result.rider.getType() + ")");
        log("Estimated distance: " + String.format("%.2f km", result.distanceKm));
        log("Estimated time: " + String.format("%.1f min", result.estimatedTimeMin));
        log("Delivery window: " + String.format("%.1f min", deliveryDuration));
        log("Pickup: " + findLocationName(pickupId) + " → Delivery: " + findLocationName(deliveryId));
        setStatus("Order placed and rider assigned");
        refreshDashboard();
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

    private void showSummary() {
        refreshSummary();
        log("Summary: " + locations.size() + " locations, " + roads.size() + " roads, " + requests.size() + " requests, " + activeOrders.size() + " active orders.");
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
            // If DB returned nothing (or different ids), fall back to in-memory requests loaded on startup
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
            list.setFont(new Font("Consolas", Font.PLAIN, 12));
            JDialog dlg = new JDialog(this, "Seeded Requests (1-300)", true);
            dlg.setSize(760, 520);
            dlg.setLocationRelativeTo(this);
            dlg.setLayout(new BorderLayout());
            dlg.add(new JScrollPane(list), BorderLayout.CENTER);
            JButton close = new JButton("Close");
            close.addActionListener(e -> dlg.dispose());
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
        JTabbedPane tabs = new JTabbedPane();

        // 1. Graph & MinHeap (Campus Network & Dijkstra)
        JTextArea graphArea = new JTextArea();
        graphArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        graphArea.setEditable(false);
        JButton runGraph = new JButton("Run Campus Graph & Dijkstra Demo");
        runGraph.addActionListener(e -> {
            graphArea.setText("");
            graphArea.append("═══ CAMPUS ROAD NETWORK & DIJKSTRA (Graph + MinHeap) ═══\n");
            DynamicArray<Location> locs = DatabaseManager.loadLocations();
            DynamicArray<RoadEdge> rds = DatabaseManager.loadRoads();
            graphArea.append(String.format("Loaded %d Campus Locations & %d Road Edges into Graph.\n", locs.size(), rds.size()));
            Graph graph = buildGraph();
            long t0 = System.nanoTime();
            RouteEngine.PathResult res = RouteEngine.dijkstra(graph, 1, 75); // Balme Library -> Night Market
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
        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.add(new JScrollPane(graphArea), BorderLayout.CENTER);
        JPanel gp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); gp.add(runGraph); graphPanel.add(gp, BorderLayout.SOUTH);
        tabs.addTab("Graph & Dijkstra", graphPanel);

        // 2. B-Tree (Multi-Way Order Indexing)
        JTextArea btreeArea = new JTextArea();
        btreeArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        btreeArea.setEditable(false);
        JButton runBTree = new JButton("Run B-Tree Order Indexing Demo");
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
        JPanel btreePanel = new JPanel(new BorderLayout());
        btreePanel.add(new JScrollPane(btreeArea), BorderLayout.CENTER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bp.add(runBTree); btreePanel.add(bp, BorderLayout.SOUTH);
        tabs.addTab("B-Tree Indexing", btreePanel);

        // 3. Red-Black Tree (Self-Balancing Order Registry)
        JTextArea rbtArea = new JTextArea();
        rbtArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        rbtArea.setEditable(false);
        JButton runRBT = new JButton("Run Red-Black Tree Balance Demo");
        runRBT.addActionListener(e -> {
            rbtArea.setText("");
            rbtArea.append("═══ RED-BLACK TREE SELF-BALANCING ORDER REGISTRY DEMO ═══\n");
            ds.RedBlackTree<Integer, ServiceRequest> rbt = new ds.RedBlackTree<>();
            DynamicArray<ServiceRequest> reqs = DatabaseManager.loadServiceRequests();
            int count = Math.min(50, reqs.size());
            for (int i = 0; i < count; i++) {
                rbt.insert(reqs.get(i).getRequestId(), reqs.get(i));
            }
            btreeArea.append(String.format("Inserted %d Active Orders into Red-Black Tree.\n", count));
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
        JPanel rbtPanel = new JPanel(new BorderLayout());
        rbtPanel.add(new JScrollPane(rbtArea), BorderLayout.CENTER);
        JPanel rbp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); rbp.add(runRBT); rbtPanel.add(rbp, BorderLayout.SOUTH);
        tabs.addTab("Red-Black Tree", rbtPanel);

        // 4. Hash Table (Rider O(1) Lookup)
        JTextArea hashArea = new JTextArea();
        hashArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        hashArea.setEditable(false);
        JButton runHash = new JButton("Run Hash Table Benchmark");
        runHash.addActionListener(e -> {
            hashArea.setText("");
            hashArea.append("═══ HASH TABLE RIDER O(1) LOOKUP DEMO ═══\n");
            ds.HashTable<Integer, Resource> table = new ds.HashTable<>(211);
            DynamicArray<Resource> ridersList = DatabaseManager.loadResources();
            for (Resource r : ridersList) {
                table.put(r.getResourceId(), r);
            }
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
        JPanel hashPanel = new JPanel(new BorderLayout());
        hashPanel.add(new JScrollPane(hashArea), BorderLayout.CENTER);
        JPanel hp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); hp.add(runHash); hashPanel.add(hp, BorderLayout.SOUTH);
        tabs.addTab("Hash Table", hashPanel);

        // 5. Disjoint Set (Campus Connectivity)
        JTextArea dsetArea = new JTextArea();
        dsetArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        dsetArea.setEditable(false);
        JButton runDSet = new JButton("Run Disjoint Set Connectivity Demo");
        runDSet.addActionListener(e -> {
            dsetArea.setText("");
            dsetArea.append("═══ DISJOINT SET (UNION-FIND) CAMPUS ZONE CONNECTIVITY ═══\n");
            DynamicArray<Location> locs = DatabaseManager.loadLocations();
            int maxId = 0;
            for (Location l : locs) maxId = Math.max(maxId, l.getLocationId());
            ds.DisjointSet dset = new ds.DisjointSet(maxId + 1);

            // Union locations in the same zone
            for (int i = 0; i < locs.size(); i++) {
                for (int j = i + 1; j < locs.size(); j++) {
                    if (locs.get(i).getZone().equalsIgnoreCase(locs.get(j).getZone())) {
                        dset.union(locs.get(i).getLocationId(), locs.get(j).getLocationId());
                    }
                }
            }
            dsetArea.append(String.format("Grouped %d Campus Locations into Disjoint Zone Sets.\n\n", locs.size()));
            Location l1 = locs.get(0); // Balme Library
            Location l2 = locs.get(1); // Great Hall
            Location l3 = locs.get(locs.size() - 1);
            
            dsetArea.append(String.format("Connectivity Check: '%s' vs '%s': %s\n",
                    l1.getName(), l2.getName(), (dset.find(l1.getLocationId()) == dset.find(l2.getLocationId()) ? "CONNECTED (Same Zone)" : "DISCONNECTED")));
            dsetArea.append(String.format("Connectivity Check: '%s' vs '%s': %s\n",
                    l1.getName(), l3.getName(), (dset.find(l1.getLocationId()) == dset.find(l3.getLocationId()) ? "CONNECTED (Same Zone)" : "DISCONNECTED")));
        });
        JPanel dsetPanel = new JPanel(new BorderLayout());
        dsetPanel.add(new JScrollPane(dsetArea), BorderLayout.CENTER);
        JPanel dsp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); dsp.add(runDSet); dsetPanel.add(dsp, BorderLayout.SOUTH);
        tabs.addTab("Disjoint Set", dsetPanel);

        // 6. CircularQueue (Round-Robin Rider Dispatch)
        JTextArea cqArea = new JTextArea();
        cqArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        cqArea.setEditable(false);
        JButton runCQ = new JButton("Run Round-Robin Pool Rotation Demo");
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
                pool.enqueue(dispatched); // Rotate to back
                cqArea.append(String.format("  Step %d: Dispatched Rider '%s' (%s) -> Rotated to rear | Front Pointer: %d | Rear Pointer: %d\n",
                        step, dispatched.getName(), dispatched.getType(), pool.getFrontPointer(), pool.getRearPointer()));
            }
        });
        JPanel cqPanel = new JPanel(new BorderLayout());
        cqPanel.add(new JScrollPane(cqArea), BorderLayout.CENTER);
        JPanel cqp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); cqp.add(runCQ); cqPanel.add(cqp, BorderLayout.SOUTH);
        tabs.addTab("Circular Queue", cqPanel);

        // 7. Deque (Express vs Standard Buffer)
        JTextArea dqArea = new JTextArea();
        dqArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        dqArea.setEditable(false);
        JButton runDQ = new JButton("Run Deque Priority Buffer Demo");
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
        JPanel dqPanel = new JPanel(new BorderLayout());
        dqPanel.add(new JScrollPane(dqArea), BorderLayout.CENTER);
        JPanel dqp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); dqp.add(runDQ); dqPanel.add(dqp, BorderLayout.SOUTH);
        tabs.addTab("Deque", dqPanel);

        // 8. Stack (Scheduling Backtracking)
        JTextArea stackArea = new JTextArea();
        stackArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        stackArea.setEditable(false);
        JButton runStack = new JButton("Run Scheduling Stack Demo");
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
        JPanel stackPanel = new JPanel(new BorderLayout());
        stackPanel.add(new JScrollPane(stackArea), BorderLayout.CENTER);
        JPanel stp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); stp.add(runStack); stackPanel.add(stp, BorderLayout.SOUTH);
        tabs.addTab("Stack", stackPanel);

        // 9. BST 
        JTextArea bstArea = new JTextArea();
        bstArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        bstArea.setEditable(false);
        JButton runBST = new JButton("Run BST Search & Deletion Demo");
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
        JPanel bstPanel = new JPanel(new BorderLayout());
        bstPanel.add(new JScrollPane(bstArea), BorderLayout.CENTER);
        JPanel bstp = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bstp.add(runBST); bstPanel.add(bstp, BorderLayout.SOUTH);
        tabs.addTab("BST", bstPanel);

        dlg.add(tabs, BorderLayout.CENTER);
        JButton close = new JButton("Close"); close.addActionListener(e -> dlg.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.add(close);
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
        // show a summary of incoming manager queues
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
        // completed list
        completedListModel.clear();
        for (Order o : completedOrders) {
            completedListModel.addElement("#" + o.getOrderId() + " | " + o.getFoodItem() + " | " + o.getRestaurant());
        }
    }

    private void refreshSummary() {
        summaryLabel.setText("Locations: " + locations.size() + " | Riders: " + riders.size() + " | Active orders: " + activeOrders.size());
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
}
