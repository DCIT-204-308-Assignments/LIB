import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import ds.DynamicArray;
import ds.Graph;
import engines.DatabaseManager;
import engines.DeliveryEngine;
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
    private final Map<String, Integer> locationIds = new LinkedHashMap<>();
    private final Map<String, List<String>> restaurantMenus = new LinkedHashMap<>();
    private final Map<String, Double> menuWeights = new LinkedHashMap<>();

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

        DeliveryEngine.AssignmentResult result = DeliveryEngine.assignRider(order, riders, locations, roads);
        if (result == null || result.rider == null) {
            log("No rider could be assigned for this order right now.");
            return;
        }

        result.rider.setAvailabilityStatus("BUSY");
        double deliveryDuration = DeliveryEngine.estimateDeliveryDuration(order, result.distanceKm, result.rider);
        ServiceRequest request = new ServiceRequest(
            10000 + activeOrders.size() + requests.size(),
            pickupId,
            deliveryId,
            meal,
            "Express".equalsIgnoreCase(priority) ? 4 : ("Family Pack".equalsIgnoreCase(priority) ? 3 : 2),
            480.0,
            480.0 + deliveryDuration,
            "ASSIGNED",
            result.rider.getResourceId()
        );

        // Persist the new request and update rider status in the database so reloads reflect changes
        try {
            DatabaseManager.addServiceRequest(request);
            DatabaseManager.updateResourceStatus(result.rider.getResourceId(), "BUSY");
        } catch (Exception ex) {
            log("Warning: could not persist request or update rider status: " + ex.getMessage());
        }

        requests.add(request);
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
        activeOrdersArea.setText(builder.toString());
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
