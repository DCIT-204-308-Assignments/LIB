import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

import ds.DynamicArray;
import ds.Graph;
import engines.DatabaseManager;
import engines.DeliveryEngine;
import engines.IncomingOrderManager;
import engines.DriverPool;
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

    // Auto-processing controls
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
                    activeOrders.remove(moved);
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
            double deliveryDuration = DeliveryEngine.estimateDeliveryDuration(order, result.distanceKm, result.rider);
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
        splitPane.setDividerLocation(430);
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

        orderBtn.setBackground(new Color(0x1F7A1F));
        orderBtn.setForeground(Color.WHITE);
        routeBtn.setBackground(new Color(0x2F5D7C));
        routeBtn.setForeground(Color.WHITE);
        dispatchBtn.setBackground(new Color(0x8A4B08));
        dispatchBtn.setForeground(Color.WHITE);

        JPanel buttonRow = new JPanel(new GridLayout(2, 3, 8, 8));
        buttonRow.setOpaque(false);
        buttonRow.add(orderBtn);
        buttonRow.add(routeBtn);
        buttonRow.add(dispatchBtn);
        buttonRow.add(refreshBtn);
        buttonRow.add(initBtn);
        buttonRow.add(generateBtn);
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
            refreshSummary();
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
        double deliveryDuration = DeliveryEngine.estimateDeliveryDuration(order, result.distanceKm, result.rider);

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
