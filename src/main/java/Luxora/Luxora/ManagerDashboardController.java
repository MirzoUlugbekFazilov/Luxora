package Luxora.Luxora;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ManagerDashboardController {

    // ── Root ───────────────────────────────────────────────────────────────────
    @FXML private javafx.scene.layout.BorderPane rootPane;

    // ── Navigation ─────────────────────────────────────────────────────────────
    @FXML private HBox  navDashboard, navProfile, navLogistics, navOrders, navInventory;
    @FXML private Label navDashboardLabel, navProfileLabel, navLogisticsLabel, navOrdersLabel, navInventoryLabel;
    @FXML private VBox  dashboardSection, profileSection, logisticsSection, ordersSection, inventorySection;

    // ── Manager Inventory ──────────────────────────────────────────────────────
    @FXML private TableView<WarehouseStock>            mgrStockTable;
    @FXML private TableColumn<WarehouseStock, String>  colMgrStProduct;
    @FXML private TableColumn<WarehouseStock, String>  colMgrStWarehouse;
    @FXML private TableColumn<WarehouseStock, Integer> colMgrStQty;
    @FXML private TableColumn<WarehouseStock, Integer> colMgrStMin;
    @FXML private TableColumn<WarehouseStock, String>  colMgrStRestock;
    @FXML private TableColumn<WarehouseStock, String>  colMgrStActions;
    @FXML private TextField  mgrStockSearchField;
    @FXML private javafx.scene.control.CheckBox mgrLowStockOnlyCheck;
    @FXML private Label  inventoryErrorLabel;
    private final ObservableList<WarehouseStock> mgrAllStock      = FXCollections.observableArrayList();
    private final ObservableList<WarehouseStock> mgrFilteredStock = FXCollections.observableArrayList();

    // ── Logistics ──────────────────────────────────────────────────────────────
    @FXML private TableView<Delivery>            deliveryTable;
    @FXML private TableColumn<Delivery, String>  colDelId;
    @FXML private TableColumn<Delivery, String>  colDelOrder;
    @FXML private TableColumn<Delivery, String>  colDelAddress;
    @FXML private TableColumn<Delivery, String>  colDelDriver;
    @FXML private TableColumn<Delivery, String>  colDelStatus;
    @FXML private TableColumn<Delivery, String>  colDelActions;
    @FXML private Label                          logisticsErrorLabel;
    @FXML private Button                         refreshDeliveryBtn;

    private final ObservableList<Delivery>       allDeliveries        = FXCollections.observableArrayList();
    private       javafx.animation.Timeline      deliveryAutoRefresh;

    // ── Orders (manager's products only) ───────────────────────────────────────
    @FXML private TableView<Order>            mgrOrdersTable;
    @FXML private TableColumn<Order, String>  colMgrOrderCode;
    @FXML private TableColumn<Order, String>  colMgrOrderDate;
    @FXML private TableColumn<Order, String>  colMgrOrderEmail;
    @FXML private TableColumn<Order, Integer> colMgrOrderItems;
    @FXML private TableColumn<Order, Double>  colMgrOrderTotal;
    @FXML private TableColumn<Order, String>  colMgrOrderStatus;
    @FXML private ComboBox<String>            mgrOrderStatusFilter;
    @FXML private Label                       mgrOrderCountLabel;
    @FXML private Label                       ordersErrorLabel;

    private final ObservableList<Order>       allManagerOrders = FXCollections.observableArrayList();

    // ── Profile ────────────────────────────────────────────────────────────────
    @FXML private Label profileNameLabel, profileEmailLabel;

    // ── Inline error banner (shown/hidden programmatically; avoids native Alert dialogs) ──
    @FXML private Label dashboardErrorLabel;

    // ── Filters ────────────────────────────────────────────────────────────────
    @FXML private TextField        searchField, minPriceField, maxPriceField;
    @FXML private Button           categoryFilterBtn;
    @FXML private ComboBox<String> statusFilter;
    private String                 selectedCategory = "All Categories";
    private ContextMenu            categoryMenu     = new ContextMenu();

    // ── Table ──────────────────────────────────────────────────────────────────
    @FXML private TableView<Product>           productTable;
    @FXML private TableColumn<Product, String>  colImage;
    @FXML private TableColumn<Product, String>  colName;
    @FXML private TableColumn<Product, String>  colCategory;
    @FXML private TableColumn<Product, Double>  colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String>  colStatus;
    @FXML private TableColumn<Product, String>  colActions;

    @FXML private Button refreshBtn;

    private final ObservableList<Product> allProducts      = FXCollections.observableArrayList();
    private final ObservableList<Product> filteredProducts = FXCollections.observableArrayList();

    // ── Export ─────────────────────────────────────────────────────────────────
    @FXML private HBox  navExport;
    @FXML private Label navExportLabel;
    @FXML private VBox  exportSection;
    @FXML private TableView<Product>            exportTable;
    @FXML private TableColumn<Product, Boolean> colExportSelect;
    @FXML private TableColumn<Product, String>  colExportName;
    @FXML private TableColumn<Product, String>  colExportCategory;
    @FXML private TableColumn<Product, Double>  colExportPrice;
    @FXML private TableColumn<Product, Integer> colExportStock;
    @FXML private TableColumn<Product, String>  colExportStatus;
    @FXML private Label exportCountLabel;
    @FXML private Label exportStatusLabel;

    private final ObservableList<Product>       exportProducts   = FXCollections.observableArrayList();
    private final Map<Integer, BooleanProperty> exportSelections = new HashMap<>();

    // Icon SVG paths (Lucide 24×24 viewbox)
    private static final String EYE_PATH   =
        "M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z " +
        "M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z";
    private static final String EYE_OFF_PATH =
        "M9.88 9.88a3 3 0 1 0 4.24 4.24 " +
        "M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68 " +
        "M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61 " +
        "M2 2L22 22";
    private static final String EDIT_PATH  =
        "M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7 " +
        "M18.375 2.625a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4z";
    private static final String TRASH_PATH =
        "M3 6h18 M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2 " +
        "M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6 " +
        "M10 11v6 M14 11v6";

    // Nav style constants
    private static final String NAV_BOX_ACTIVE   =
            "-fx-background-color: #ececf0; -fx-background-radius: 8; -fx-padding: 0 12; -fx-cursor: hand; " +
            "-fx-border-color: #030213 transparent transparent transparent; -fx-border-width: 0 0 0 3;";
    private static final String NAV_BOX_INACTIVE =
            "-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 0 15; -fx-cursor: hand;";
    private static final String NAV_LBL_ACTIVE   = "-fx-font-size: 13; -fx-text-fill: #030213; -fx-font-weight: bold; -fx-font-family: Poppins;";
    private static final String NAV_LBL_INACTIVE = "-fx-font-size: 13; -fx-text-fill: #717182; -fx-font-weight: bold; -fx-font-family: Poppins;";

    // ── Initialisation ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        profileNameLabel.setText(UserSession.getName());
        profileEmailLabel.setText(UserSession.getEmail());

        navDashboard.setOnMouseClicked(e -> showSection("dashboard"));
        navProfile.setOnMouseClicked(e -> showSection("profile"));
        navExport.setOnMouseClicked(e -> showSection("export"));
        navOrders.setOnMouseClicked(e -> showSection("orders"));
        navLogistics.setOnMouseClicked(e -> showSection("logistics"));
        navInventory.setOnMouseClicked(e -> showSection("inventory"));

        statusFilter.setItems(FXCollections.observableArrayList("All Status", "active", "inactive"));
        statusFilter.setValue("All Status");

        categoryFilterBtn.setOnAction(e ->
                categoryMenu.show(categoryFilterBtn, Side.BOTTOM, 0, 4));

        // Funnel icon for category filter button
        SVGPath funnelIcon = new SVGPath();
        funnelIcon.setContent("M22 3H2l8 9.46V19l4 2v-8.54L22 3z");
        funnelIcon.setStroke(Color.web("#717182"));
        funnelIcon.setFill(Color.TRANSPARENT);
        funnelIcon.setStrokeWidth(1.8);
        double funnelScale = 12.0 / 24.0;
        funnelIcon.setScaleX(funnelScale);
        funnelIcon.setScaleY(funnelScale);
        categoryFilterBtn.setGraphic(funnelIcon);
        categoryFilterBtn.setContentDisplay(ContentDisplay.LEFT);
        categoryFilterBtn.setGraphicTextGap(8);

        // Refresh button icon
        SVGPath refreshIcon = new SVGPath();
        refreshIcon.setContent(
            "M23 4v6h-6 M1 20v-6h6 " +
            "M3.51 9a9 9 0 0 1 14.85-3.36L23 10 " +
            "M1 14l4.64 4.36A9 9 0 0 0 20.49 15");
        refreshIcon.setStroke(Color.web("#030213"));
        refreshIcon.setFill(Color.TRANSPARENT);
        refreshIcon.setStrokeWidth(1.8);
        double rs = 14.0 / 24.0;
        refreshIcon.setScaleX(rs);
        refreshIcon.setScaleY(rs);
        refreshBtn.setGraphic(refreshIcon);
        refreshBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        // Load filter CSS for ComboBox styling
        dashboardSection.getStylesheets().add(
                getClass().getResource("/dashboard-filter.css").toExternalForm());

        setupTableColumns();
        loadProducts();

        searchField.textProperty().addListener((o, old, nw) -> applyFilters());
        statusFilter.valueProperty().addListener((o, old, nw) -> applyFilters());
        minPriceField.textProperty().addListener((o, old, nw) -> applyFilters());
        maxPriceField.textProperty().addListener((o, old, nw) -> applyFilters());

        // Nav icons
        SVGPath dashIcon = makeNavIcon(
            "M3 3h7v7H3V3zm11 0h7v7h-7V3zM3 14h7v7H3v-7zm11 0h7v7h-7v-7z", false);
        navDashboard.getChildren().add(0, dashIcon);
        HBox.setMargin(dashIcon, new Insets(0, 8, 0, 0));

        SVGPath profIcon = makeNavIcon(
            "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z", false);
        navProfile.getChildren().add(0, profIcon);
        HBox.setMargin(profIcon, new Insets(0, 8, 0, 0));

        SVGPath exportIcon = makeNavIcon(
            "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3", false);
        navExport.getChildren().add(0, exportIcon);
        HBox.setMargin(exportIcon, new Insets(0, 8, 0, 0));

        SVGPath ordersIcon = makeNavIcon(
            "M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" +
            "M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 5a2 2 0 0 0 2-2h2a2 2 0 0 0 2 2" +
            "M12 12h4M12 16h4M8 12h.01M8 16h.01", false);
        navOrders.getChildren().add(0, ordersIcon);
        HBox.setMargin(ordersIcon, new Insets(0, 8, 0, 0));

        mgrOrderStatusFilter.setItems(FXCollections.observableArrayList(
            "All Statuses", "Processing", "Shipped", "Delivered", "Cancelled", "Returned", "Failed"));
        mgrOrderStatusFilter.setValue("All Statuses");
        mgrOrderStatusFilter.valueProperty().addListener((o, old, nw) -> loadManagerOrders());

        SVGPath inventoryIcon = makeNavIcon(
            "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z", false);
        navInventory.getChildren().add(0, inventoryIcon);
        HBox.setMargin(inventoryIcon, new Insets(0, 8, 0, 0));

        SVGPath logisticsIcon = makeNavIcon(
            "M1 3h15v13H1zM16 8h4l3 3v5h-7V8z" +
            "M5.5 21a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" +
            "M18.5 21a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z", false);
        navLogistics.getChildren().add(0, logisticsIcon);
        HBox.setMargin(logisticsIcon, new Insets(0, 8, 0, 0));

        // Refresh icon for logistics button
        SVGPath refreshDelIcon = new SVGPath();
        refreshDelIcon.setContent(
            "M23 4v6h-6 M1 20v-6h6 " +
            "M3.51 9a9 9 0 0 1 14.85-3.36L23 10 " +
            "M1 14l4.64 4.36A9 9 0 0 0 20.49 15");
        refreshDelIcon.setStroke(Color.web("#030213"));
        refreshDelIcon.setFill(Color.TRANSPARENT);
        refreshDelIcon.setStrokeWidth(1.8);
        double rds = 14.0 / 24.0;
        refreshDelIcon.setScaleX(rds);
        refreshDelIcon.setScaleY(rds);
        refreshDeliveryBtn.setGraphic(refreshDelIcon);
        refreshDeliveryBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        setupDeliveryTable();
        setupManagerOrdersTable();
        setupExportTable();
        setupManagerInventorySection();
        showSection("dashboard");
        HoverUtils.apply(rootPane);
    }

    private SVGPath makeNavIcon(String path, boolean active) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.setStroke(Color.web(active ? "#030213" : "#717182"));
        icon.setFill(Color.TRANSPARENT);
        icon.setStrokeWidth(1.8);
        double s = 14.0 / 24.0;
        icon.setScaleX(s);
        icon.setScaleY(s);
        return icon;
    }

    // ── Section navigation ────────────────────────────────────────────────────

    private void showSection(String section) {
        dashboardSection.setVisible(false);  dashboardSection.setManaged(false);
        profileSection.setVisible(false);    profileSection.setManaged(false);
        exportSection.setVisible(false);     exportSection.setManaged(false);
        ordersSection.setVisible(false);     ordersSection.setManaged(false);
        logisticsSection.setVisible(false);  logisticsSection.setManaged(false);
        inventorySection.setVisible(false);  inventorySection.setManaged(false);
        setNavActive(navDashboard,  navDashboardLabel,  false);
        setNavActive(navProfile,    navProfileLabel,    false);
        setNavActive(navExport,     navExportLabel,     false);
        setNavActive(navOrders,     navOrdersLabel,     false);
        setNavActive(navLogistics,  navLogisticsLabel,  false);
        setNavActive(navInventory,  navInventoryLabel,  false);

        switch (section) {
            case "profile"   -> { stopDeliveryAutoRefresh();
                                  profileSection.setVisible(true); profileSection.setManaged(true);
                                  setNavActive(navProfile, navProfileLabel, true); }
            case "export"    -> { stopDeliveryAutoRefresh();
                                  exportSection.setVisible(true);  exportSection.setManaged(true);
                                  setNavActive(navExport, navExportLabel, true);
                                  loadExportProducts(); }
            case "orders"    -> { stopDeliveryAutoRefresh();
                                  ordersSection.setVisible(true);  ordersSection.setManaged(true);
                                  setNavActive(navOrders, navOrdersLabel, true);
                                  loadManagerOrders(); }
            case "logistics" -> { logisticsSection.setVisible(true); logisticsSection.setManaged(true);
                                  setNavActive(navLogistics, navLogisticsLabel, true);
                                  loadDeliveries();
                                  startDeliveryAutoRefresh(); }
            case "inventory" -> { stopDeliveryAutoRefresh();
                                  inventorySection.setVisible(true); inventorySection.setManaged(true);
                                  setNavActive(navInventory, navInventoryLabel, true);
                                  loadManagerStock(); }
            default          -> { stopDeliveryAutoRefresh();
                                  dashboardSection.setVisible(true); dashboardSection.setManaged(true);
                                  setNavActive(navDashboard, navDashboardLabel, true); }
        }
    }

    private void setNavActive(HBox box, Label lbl, boolean active) {
        box.setStyle(active ? NAV_BOX_ACTIVE : NAV_BOX_INACTIVE);
        lbl.setStyle(active ? NAV_LBL_ACTIVE : NAV_LBL_INACTIVE);
        box.getChildren().stream()
            .filter(n -> n instanceof SVGPath)
            .forEach(n -> ((SVGPath) n).setStroke(Color.web(active ? "#030213" : "#717182")));
    }

    // ── Data loading ───────────────────────────────────────────────────────────

    /**
     * Loads the manager's own products from the database and populates the table.
     * Clears any previous error banner before loading; shows one inline on failure.
     */
    public void loadProducts() {
        try {
            clearSectionError(dashboardErrorLabel);
            List<Product> products = dbController.getProductsByCreator(UserSession.getEmail());
            allProducts.setAll(products);

            Set<String> cats = new LinkedHashSet<>();
            cats.add("All Categories");
            for (Product p : products) {
                if (p.getCategory() != null && !p.getCategory().isBlank()) {
                    for (String c : p.getCategory().split(",")) {
                        String t = c.trim();
                        if (!t.isEmpty()) cats.add(t);
                    }
                }
            }
            if (!cats.contains(selectedCategory)) {
                selectedCategory = "All Categories";
                categoryFilterBtn.setText("All Categories");
            }
            categoryMenu.getItems().clear();
            for (String cat : cats) {
                MenuItem item = new MenuItem(cat);
                item.setOnAction(e -> {
                    selectedCategory = cat;
                    categoryFilterBtn.setText("All Categories".equals(cat) ? "All Categories" : cat);
                    applyFilters();
                });
                categoryMenu.getItems().add(item);
            }

            applyFilters();
        } catch (Exception e) {
            showSectionError(dashboardErrorLabel, "Failed to load products: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String search = nvl(searchField.getText()).trim().toLowerCase();
        String cat    = selectedCategory;
        String stat   = statusFilter.getValue();
        double min    = parseDouble(minPriceField.getText(), 0);
        double max    = parseDouble(maxPriceField.getText(), Double.MAX_VALUE);

        filteredProducts.setAll(allProducts.stream().filter(p -> {
            boolean ms  = search.isEmpty()
                    || p.getName().toLowerCase().contains(search)
                    || p.getCategory().toLowerCase().contains(search);
            boolean mc  = cat  == null || "All Categories".equals(cat)
                    || p.getCategory().toLowerCase().contains(cat.toLowerCase());
            boolean mst = stat == null || "All Status".equals(stat)
                    || p.getStatus().equalsIgnoreCase(stat);
            boolean mp  = p.getPrice() >= min && p.getPrice() <= max;
            return ms && mc && mst && mp;
        }).toList());

        productTable.setItems(filteredProducts);
    }

    // ── Table column setup ─────────────────────────────────────────────────────

    private void setupTableColumns() {
        productTable.getStylesheets().add(
                getClass().getResource("/dashboard-table.css").toExternalForm());
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final String ROW_NORMAL = "-fx-background-color: white;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent;" +
                " -fx-border-width: 0 0 1 0;";
        final String ROW_HOVER  = "-fx-background-color: #f0f4f8;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent;" +
                " -fx-border-width: 0 0 1 0;";

        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) ->
                    row.setStyle(item == null ? "" : ROW_NORMAL));
            row.hoverProperty().addListener((obs, was, isNow) -> {
                if (!row.isEmpty())
                    row.setStyle(isNow ? ROW_HOVER : ROW_NORMAL);
            });
            return row;
        });

        // ── Image ──────────────────────────────────────────────────────────────
        colImage.setCellValueFactory(c -> c.getValue().imagesProperty());
        colImage.setCellFactory(col -> new TableCell<>() {
            final ImageView iv = new ImageView();
            final StackPane container = new StackPane(iv);
            {
                iv.setFitWidth(48); iv.setFitHeight(48);
                iv.setPreserveRatio(false);
                container.setMinSize(48, 48);
                container.setMaxSize(48, 48);
                container.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 6;");
                setAlignment(Pos.CENTER);
            }
            @Override protected void updateItem(String imgs, boolean empty) {
                super.updateItem(imgs, empty);
                if (empty) { setGraphic(null); return; }
                if (imgs == null || imgs.isBlank()) { iv.setImage(null); setGraphic(container); return; }
                String[] parts = imgs.split("\\|", -1);
                if (parts.length > 0 && !parts[0].isBlank()) {
                    try {
                        byte[] dec = Base64.getDecoder().decode(parts[0].trim());
                        iv.setImage(new Image(new ByteArrayInputStream(dec)));
                    } catch (Exception ex) { iv.setImage(null); }
                } else { iv.setImage(null); }
                setGraphic(container);
            }
        });

        // ── Name + ID ──────────────────────────────────────────────────────────
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        colName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String nm, boolean empty) {
                super.updateItem(nm, empty);
                if (empty || nm == null) { setGraphic(null); return; }
                Product p = getTableRow().getItem();
                if (p == null) { setGraphic(null); return; }
                VBox vb = new VBox(2);
                vb.setAlignment(Pos.CENTER);
                Label lName = new Label(nm);
                lName.setStyle("-fx-font-size: 13; -fx-text-fill: #030213;");
                Label lId = new Label("ID: " + p.getProductID());
                lId.setStyle("-fx-font-size: 11; -fx-text-fill: #717182;");
                vb.getChildren().addAll(lName, lId);
                setGraphic(vb);
                setAlignment(Pos.CENTER);
                setPadding(new Insets(8, 0, 8, 0));
            }
        });

        // ── Categories (chips) ─────────────────────────────────────────────────
        colCategory.setCellValueFactory(c -> c.getValue().categoryProperty());
        colCategory.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String cats, boolean empty) {
                super.updateItem(cats, empty);
                if (empty || cats == null || cats.isBlank()) { setGraphic(null); return; }
                FlowPane fp = new FlowPane();
                fp.setHgap(4); fp.setVgap(4); fp.setAlignment(Pos.CENTER);
                for (String c : cats.split(",")) {
                    String t = c.trim();
                    if (!t.isEmpty()) {
                        Label chip = new Label(t);
                        chip.setStyle("-fx-border-color: #e6e6e6; -fx-border-radius: 4;" +
                                " -fx-background-radius: 4; -fx-background-color: transparent;" +
                                " -fx-padding: 2 8; -fx-font-size: 11; -fx-text-fill: #030213;");
                        fp.getChildren().add(chip);
                    }
                }
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
                setPadding(new Insets(8, 8, 8, 8));
                setGraphic(fp);
            }
        });

        // ── Price ──────────────────────────────────────────────────────────────
        colPrice.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) { setText(null); return; }
                setText(String.format("$%.2f", price));
                setStyle("-fx-font-size: 13; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER);
            }
        });

        // ── Stock ──────────────────────────────────────────────────────────────
        colStock.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        colStock.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(qty));
                setAlignment(Pos.CENTER);
                setStyle(qty < 10
                        ? "-fx-font-size: 13; -fx-text-fill: #d4183d; -fx-font-weight: bold;"
                        : "-fx-font-size: 13; -fx-text-fill: #030213;");
            }
        });

        // ── Status badge ───────────────────────────────────────────────────────
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status.toLowerCase());
                if ("active".equals(status.toLowerCase())) {
                    badge.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                            " -fx-padding: 3 12; -fx-background-radius: 20;" +
                            " -fx-font-size: 11; -fx-font-weight: bold;");
                } else {
                    badge.setStyle("-fx-background-color: transparent; -fx-text-fill: #717182;" +
                            " -fx-border-color: #e6e6e6; -fx-border-radius: 20;" +
                            " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11;");
                }
                setGraphic(badge); setAlignment(Pos.CENTER);
            }
        });

        // ── Actions (toggle / edit / delete) ──────────────────────────────────
        colActions.setCellValueFactory(c -> c.getValue().nameProperty());
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button toggleBtn = makeOutlineBtn();
            final Button editBtn   = makeOutlineBtn();
            final Button deleteBtn = makeOutlineBtn();
            final HBox   box       = new HBox(4, toggleBtn, editBtn, deleteBtn);
            { box.setAlignment(Pos.CENTER); }

            @Override protected void updateItem(String dummy, boolean empty) {
                super.updateItem(dummy, empty);
                if (empty) { setGraphic(null); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                Product p = getTableView().getItems().get(idx);
                boolean active = "active".equals(p.getStatus());
                toggleBtn.setGraphic(makeSvgIcon(active ? EYE_PATH : EYE_OFF_PATH, "#717182"));
                editBtn  .setGraphic(makeSvgIcon(EDIT_PATH,  "#717182"));
                deleteBtn.setGraphic(makeSvgIcon(TRASH_PATH, "#d4183d"));
                toggleBtn.setOnAction(e -> handleToggleStatus(p));
                editBtn  .setOnAction(e -> handleEditProduct(p));
                deleteBtn.setOnAction(e -> handleDeleteProduct(p));
                setGraphic(box);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private Button makeOutlineBtn() {
        Button b = new Button();
        b.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 5;");
        HoverUtils.attachHover(b);
        return b;
    }

    private SVGPath makeSvgIcon(String pathData, String hexColor) {
        SVGPath p = new SVGPath();
        p.setContent(pathData);
        p.setStroke(Color.web(hexColor));
        p.setFill(Color.TRANSPARENT);
        p.setStrokeWidth(1.8);
        double scale = 14.0 / 24.0;
        p.setScaleX(scale);
        p.setScaleY(scale);
        return p;
    }

    // ── Event handlers ─────────────────────────────────────────────────────────

    @FXML private void handleAddProduct() { openProductForm(null); }

    @FXML private void handleRefresh() { loadProducts(); }

    private void handleToggleStatus(Product p) {
        try {
            dbController.toggleProductStatus(p.getProductID());
            loadProducts();
        } catch (Exception ex) {
            showSectionError(dashboardErrorLabel, "Failed to toggle product status: " + ex.getMessage());
        }
    }

    private void handleEditProduct(Product p) { openProductForm(p); }

    /**
     * Opens a styled in-app confirmation dialog before deleting a product.
     * Uses the {@link ConfirmDialog} helper — no native OS alerts.
     *
     * @param product the product selected for deletion
     */
    private void handleDeleteProduct(Product product) {
        new ConfirmDialog(
            (Stage) productTable.getScene().getWindow(),
            "Delete Product",
            "Are you sure you want to delete \"" + product.getName() + "\"?\n" +
            "This action cannot be undone.",
            "Delete",
            () -> dbController.deleteProduct(product.getProductID()),
            this::loadProducts
        ).show();
    }

    private void openProductForm(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProductForm.fxml"));
            Parent root = loader.load();
            ProductFormController ctrl = loader.getController();
            ctrl.setOnSave(this::loadProducts);
            if (product != null) ctrl.setProduct(product);

            Stage stage = new Stage();
            stage.initOwner(productTable.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(product == null ? "Add Product" : "Edit Product");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            showSectionError(dashboardErrorLabel, "Cannot open product form: " + ex.getMessage());
        }
    }

    @FXML private void handleClearFilters() {
        searchField.clear();
        selectedCategory = "All Categories";
        categoryFilterBtn.setText("All Categories");
        statusFilter.setValue("All Status");
        minPriceField.clear();
        maxPriceField.clear();
        applyFilters();
    }

    /**
     * Opens a styled in-app confirmation dialog for account deletion.
     * On confirmation, deletes the account and navigates to the Start screen.
     * All errors are shown inline — no native OS dialogs.
     */
    @FXML
    private void handleDeleteAccount() {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) productTable.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Delete Account");
        dialog.setResizable(false);

        Label titleLabel = new Label("Delete Your Account");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");

        Label bodyLabel = new Label(
            "Are you sure you want to permanently delete your account?\n" +
            "This action cannot be undone.");
        bodyLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #717182;");
        bodyLabel.setWrapText(true);

        // Inline error label — visible only when deletion fails
        Label errorLabel = new Label();
        errorLabel.setStyle(
            "-fx-font-size: 12; -fx-text-fill: #d4183d;" +
            " -fx-background-color: #fef2f2; -fx-background-radius: 6; -fx-padding: 8 12;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle(
            "-fx-background-color: #f3f4f6; -fx-text-fill: #030213;" +
            " -fx-font-size: 13; -fx-font-weight: bold;" +
            " -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> dialog.close());

        Button confirmButton = new Button("Delete Account");
        confirmButton.setStyle(
            "-fx-background-color: #d4183d; -fx-text-fill: white;" +
            " -fx-font-size: 13; -fx-font-weight: bold;" +
            " -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        confirmButton.setOnAction(e -> {
            try {
                dbController.deleteAccount(UserSession.getEmail());
                UserSession.clear();
                dialog.close();
                // Navigate back to the start screen after successful deletion
                Scene scene = productTable.getScene();
                scene.setFill(Color.web("#162535"));
                Parent navRoot = FXMLLoader.load(getClass().getResource("/Start.fxml"));
                scene.setRoot(navRoot);
            } catch (Exception ex) {
                errorLabel.setText("Failed to delete account: " + ex.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                HoverUtils.shake(errorLabel);
            }
        });

        HBox buttons = new HBox(10, cancelButton, confirmButton);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(6, 0, 0, 0));

        VBox root = new VBox(14, titleLabel, bodyLabel, errorLabel, buttons);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(400);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    @FXML
    public void handleLogout(ActionEvent event) throws Exception {
        Scene scene = productTable.getScene();
        scene.setFill(Color.web("#162535"));
        Parent root = FXMLLoader.load(getClass().getResource("/Start.fxml"));
        scene.setRoot(root);
    }

    // ── Export ─────────────────────────────────────────────────────────────────

    private void setupExportTable() {
        exportTable.getStylesheets().add(
                getClass().getResource("/dashboard-table.css").toExternalForm());
        exportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        exportTable.setEditable(true);

        final String ROW_NORMAL = "-fx-background-color: white;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String ROW_HOVER  = "-fx-background-color: #f0f4f8;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        exportTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) -> row.setStyle(item == null ? "" : ROW_NORMAL));
            row.hoverProperty().addListener((obs, was, isNow) -> {
                if (!row.isEmpty()) row.setStyle(isNow ? ROW_HOVER : ROW_NORMAL);
            });
            return row;
        });

        colExportSelect.setEditable(true);
        colExportSelect.setCellValueFactory(c -> {
            BooleanProperty bp = exportSelections.get(c.getValue().getProductID());
            return bp != null ? bp : new SimpleBooleanProperty(false);
        });
        colExportSelect.setCellFactory(CheckBoxTableCell.forTableColumn(colExportSelect));

        colExportName.setCellValueFactory(c -> c.getValue().nameProperty());
        colExportName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String nm, boolean empty) {
                super.updateItem(nm, empty);
                if (empty || nm == null) { setGraphic(null); return; }
                Product p = getTableRow().getItem();
                if (p == null) { setGraphic(null); return; }
                VBox vb = new VBox(2);
                Label lName = new Label(nm);
                lName.setStyle("-fx-font-size: 13; -fx-text-fill: #030213;");
                Label lId = new Label("ID: " + p.getProductID());
                lId.setStyle("-fx-font-size: 11; -fx-text-fill: #717182;");
                vb.getChildren().addAll(lName, lId);
                setGraphic(vb);
                setPadding(new Insets(8, 0, 8, 0));
            }
        });

        colExportCategory.setCellValueFactory(c -> c.getValue().categoryProperty());
        colExportCategory.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String cats, boolean empty) {
                super.updateItem(cats, empty);
                if (empty || cats == null || cats.isBlank()) { setGraphic(null); return; }
                FlowPane fp = new FlowPane(); fp.setHgap(4); fp.setVgap(4);
                for (String c : cats.split(",")) {
                    String t = c.trim();
                    if (!t.isEmpty()) {
                        Label chip = new Label(t);
                        chip.setStyle("-fx-border-color: #e6e6e6; -fx-border-radius: 4;" +
                                " -fx-background-radius: 4; -fx-background-color: transparent;" +
                                " -fx-padding: 2 8; -fx-font-size: 11; -fx-text-fill: #030213;");
                        fp.getChildren().add(chip);
                    }
                }
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setPadding(new Insets(8));
                setGraphic(fp);
            }
        });

        colExportPrice.setCellValueFactory(c -> c.getValue().priceProperty().asObject());
        colExportPrice.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) { setText(null); return; }
                setText(String.format("$%.2f", price));
                setStyle("-fx-font-size: 13; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER);
            }
        });

        colExportStock.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        colExportStock.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(qty));
                setAlignment(Pos.CENTER);
                setStyle(qty < 10
                        ? "-fx-font-size: 13; -fx-text-fill: #d4183d; -fx-font-weight: bold;"
                        : "-fx-font-size: 13; -fx-text-fill: #030213;");
            }
        });

        colExportStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colExportStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status.toLowerCase());
                if ("active".equals(status.toLowerCase())) {
                    badge.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                            " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;");
                } else {
                    badge.setStyle("-fx-background-color: transparent; -fx-text-fill: #717182;" +
                            " -fx-border-color: #e6e6e6; -fx-border-radius: 20;" +
                            " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11;");
                }
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void loadExportProducts() {
        try {
            List<Product> products = dbController.getProductsByCreator(UserSession.getEmail());
            exportProducts.setAll(products);
            exportSelections.clear();
            for (Product p : products) {
                BooleanProperty bp = new SimpleBooleanProperty(false);
                bp.addListener((obs, o, n) -> updateExportCount());
                exportSelections.put(p.getProductID(), bp);
            }
            exportTable.setItems(exportProducts);
            exportStatusLabel.setText("");
            updateExportCount();
        } catch (Exception e) {
            exportStatusLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #d4183d;");
            exportStatusLabel.setText("Failed to load products for export: " + e.getMessage());
        }
    }

    private void updateExportCount() {
        long count = exportSelections.values().stream().filter(BooleanProperty::get).count();
        exportCountLabel.setText(count + " product" + (count == 1 ? "" : "s") + " selected");
    }

    private List<Product> getSelectedProducts() {
        List<Product> result = new ArrayList<>();
        for (Product p : exportProducts) {
            BooleanProperty bp = exportSelections.get(p.getProductID());
            if (bp != null && bp.get()) result.add(p);
        }
        return result;
    }

    @FXML private void handleSelectAll() {
        exportSelections.values().forEach(bp -> bp.set(true));
        exportTable.refresh();
    }

    @FXML private void handleDeselectAll() {
        exportSelections.values().forEach(bp -> bp.set(false));
        exportTable.refresh();
    }

    @FXML private void handleExportCSV() {
        List<Product> selected = getSelectedProducts();
        if (selected.isEmpty()) {
            exportStatusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;");
            exportStatusLabel.setText("No products selected.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save CSV Export");
        fc.setInitialFileName("products_export.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));
        File file = fc.showSaveDialog(exportTable.getScene().getWindow());
        if (file != null) {
            try {
                writeCsv(selected, file);
                exportStatusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #16a34a;");
                exportStatusLabel.setText("✓ Exported " + selected.size() + " product(s) → " + file.getName());
            } catch (Exception e) {
                exportStatusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;");
                exportStatusLabel.setText("Export failed: " + e.getMessage());
            }
        }
    }

    @FXML private void handleExportXLSX() {
        List<Product> selected = getSelectedProducts();
        if (selected.isEmpty()) {
            exportStatusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;");
            exportStatusLabel.setText("No products selected.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save XLSX Export");
        fc.setInitialFileName("products_export.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx"));
        File file = fc.showSaveDialog(exportTable.getScene().getWindow());
        if (file != null) {
            try {
                writeXlsx(selected, file);
                exportStatusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #16a34a;");
                exportStatusLabel.setText("✓ Exported " + selected.size() + " product(s) → " + file.getName());
            } catch (Exception e) {
                exportStatusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;");
                exportStatusLabel.setText("Export failed: " + e.getMessage());
            }
        }
    }

    // ── Static export writers ──────────────────────────────────────────────────

    private static void writeCsv(List<Product> products, File file) throws Exception {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            pw.println("ID,Name,Categories,Description,Price ($),Stock,Status");
            for (Product p : products) {
                pw.printf("%d,%s,%s,%s,%.2f,%d,%s%n",
                        p.getProductID(), csvEsc(p.getName()), csvEsc(p.getCategory()),
                        csvEsc(p.getDescription()), p.getPrice(), p.getQuantity(), csvEsc(p.getStatus()));
            }
        }
    }

    private static void writeXlsx(List<Product> products, File file) throws Exception {
        String[] headers = {"ID", "Name", "Categories", "Description", "Price ($)", "Stock", "Status"};
        StringBuilder sheet = new StringBuilder();
        sheet.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
             .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
             .append("<sheetData>");

        sheet.append("<row r=\"1\">");
        for (int c = 0; c < headers.length; c++) {
            sheet.append("<c r=\"").append(xlCol(c)).append("1\" t=\"inlineStr\"><is><t>")
                 .append(xmlEsc(headers[c])).append("</t></is></c>");
        }
        sheet.append("</row>");

        for (int r = 0; r < products.size(); r++) {
            Product p = products.get(r);
            int rowNum = r + 2;
            String[] vals = {
                String.valueOf(p.getProductID()), p.getName(),
                p.getCategory()    == null ? "" : p.getCategory(),
                p.getDescription() == null ? "" : p.getDescription(),
                String.format("%.2f", p.getPrice()),
                String.valueOf(p.getQuantity()),
                p.getStatus()      == null ? "" : p.getStatus()
            };
            sheet.append("<row r=\"").append(rowNum).append("\">");
            for (int c = 0; c < vals.length; c++) {
                String ref = xlCol(c) + rowNum;
                // Numeric columns: ID(0), Price(4), Stock(5)
                if (c == 0 || c == 4 || c == 5) {
                    sheet.append("<c r=\"").append(ref).append("\"><v>")
                         .append(xmlEsc(vals[c])).append("</v></c>");
                } else {
                    sheet.append("<c r=\"").append(ref).append("\" t=\"inlineStr\"><is><t>")
                         .append(xmlEsc(vals[c])).append("</t></is></c>");
                }
            }
            sheet.append("</row>");
        }
        sheet.append("</sheetData></worksheet>");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            xlEntry(zos, "[Content_Types].xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                "<Default Extension=\"xml\"  ContentType=\"application/xml\"/>" +
                "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
                "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                "</Types>");
            xlEntry(zos, "_rels/.rels",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
                "</Relationships>");
            xlEntry(zos, "xl/workbook.xml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
                "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                "<sheets><sheet name=\"Products\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            xlEntry(zos, "xl/_rels/workbook.xml.rels",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
                "</Relationships>");
            xlEntry(zos, "xl/worksheets/sheet1.xml", sheet.toString());
        }
    }

    private static void xlEntry(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String xlCol(int c) { return String.valueOf((char) ('A' + c)); }

    private static String xmlEsc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String csvEsc(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    // ── Utilities ──────────────────────────────────────────────────────────────

    /** Parses a String to double; returns {@code def} if blank or unparseable. */
    private double parseDouble(String s, double def) {
        try { return (s == null || s.isBlank()) ? def : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    /** Returns {@code s} unchanged, or an empty String if {@code s} is null. */
    private String nvl(String s) { return s == null ? "" : s; }

    // ── Orders (manager view — read-only) ─────────────────────────────────────

    private void loadManagerOrders() {
        try {
            clearSectionError(ordersErrorLabel);
            List<Order> orders = dbController.getOrdersByProductManager(UserSession.getEmail());
            String filter = mgrOrderStatusFilter.getValue();
            if (filter != null && !"All Statuses".equals(filter)) {
                orders = orders.stream()
                    .filter(o -> filter.equals(o.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            }
            allManagerOrders.setAll(orders);
            mgrOrdersTable.setItems(allManagerOrders);
            int count = allManagerOrders.size();
            mgrOrderCountLabel.setText(count + " order" + (count == 1 ? "" : "s"));
        } catch (Exception e) {
            showSectionError(ordersErrorLabel, "Failed to load orders: " + e.getMessage());
        }
    }

    private void setupManagerOrdersTable() {
        mgrOrdersTable.getStylesheets().add(
                getClass().getResource("/dashboard-table.css").toExternalForm());
        mgrOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final String ROW_NORMAL = "-fx-background-color: white;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String ROW_HOVER  = "-fx-background-color: #f0f4f8;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        mgrOrdersTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) -> row.setStyle(item == null ? "" : ROW_NORMAL));
            row.hoverProperty().addListener((obs, was, isNow) -> {
                if (!row.isEmpty()) row.setStyle(isNow ? ROW_HOVER : ROW_NORMAL);
            });
            return row;
        });

        colMgrOrderCode.setCellValueFactory(c -> c.getValue().orderCodeProperty());
        colMgrOrderCode.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);
                if (empty || code == null) { setText(null); return; }
                setText(code);
                setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 8));
            }
        });

        colMgrOrderDate.setCellValueFactory(c -> c.getValue().orderDateProperty());
        colMgrOrderDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) { setText(null); return; }
                // Show only date part (first 10 chars of ISO string)
                setText(date.length() > 10 ? date.substring(0, 10) : date);
                setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 8));
            }
        });

        colMgrOrderEmail.setCellValueFactory(c -> c.getValue().userEmailProperty());
        colMgrOrderEmail.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) { setText(null); return; }
                setText(email);
                setStyle("-fx-font-size: 12; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 8));
            }
        });

        colMgrOrderItems.setCellValueFactory(c -> c.getValue().itemCountProperty().asObject());
        colMgrOrderItems.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer count, boolean empty) {
                super.updateItem(count, empty);
                if (empty || count == null) { setText(null); return; }
                setText(String.valueOf(count));
                setStyle("-fx-font-size: 12; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER);
            }
        });

        colMgrOrderTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());
        colMgrOrderTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) { setText(null); return; }
                setText(String.format("$%.2f", total));
                setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER);
            }
        });

        colMgrOrderStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colMgrOrderStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setStyle(mgrOrderStatusBadge(status));
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private String mgrOrderStatusBadge(String status) {
        return switch (status) {
            case "Processing" -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Shipped"    -> "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Delivered"  -> "-fx-background-color: #030213; -fx-text-fill: white;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Cancelled"  -> "-fx-background-color: #fef2f2; -fx-text-fill: #d4183d;" +
                                 " -fx-border-color: #fecaca; -fx-border-radius: 20;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Returned"   -> "-fx-background-color: #ede9fe; -fx-text-fill: #5b21b6;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Failed"     -> "-fx-background-color: #fef2f2; -fx-text-fill: #d4183d;" +
                                 " -fx-border-color: #fecaca; -fx-border-radius: 20;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            default           -> "-fx-background-color: #f3f4f6; -fx-text-fill: #717182;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11;";
        };
    }

    // ── Logistics (read + simulate) ────────────────────────────────────────────

    private void setupDeliveryTable() {
        deliveryTable.getStylesheets().add(
                getClass().getResource("/dashboard-table.css").toExternalForm());
        deliveryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final String ROW_NORMAL = "-fx-background-color: white;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String ROW_HOVER  = "-fx-background-color: #f0f4f8;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        deliveryTable.setRowFactory(tv -> {
            TableRow<Delivery> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) ->
                    row.setStyle(item == null ? "" : ROW_NORMAL));
            row.hoverProperty().addListener((obs, was, isNow) -> {
                if (!row.isEmpty()) row.setStyle(isNow ? ROW_HOVER : ROW_NORMAL);
            });
            return row;
        });

        colDelId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDeliveryId()));
        colDelId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); return; }
                setText(id);
                setStyle("-fx-font-size: 11; -fx-text-fill: #717182;");
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 8));
            }
        });

        colDelOrder.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getOrderRef()));
        colDelOrder.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String ref, boolean empty) {
                super.updateItem(ref, empty);
                if (empty || ref == null) { setText(null); return; }
                setText(ref);
                setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 8));
            }
        });

        colDelAddress.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDeliveryAddress()));
        colDelAddress.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String addr, boolean empty) {
                super.updateItem(addr, empty);
                if (empty || addr == null) { setGraphic(null); return; }
                Label lbl = new Label(addr);
                lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #030213;");
                lbl.setWrapText(true);
                setGraphic(lbl);
                setAlignment(Pos.CENTER_LEFT);
            }
        });

        colDelDriver.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getAssignedDriver()));
        colDelDriver.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String driver, boolean empty) {
                super.updateItem(driver, empty);
                if (empty) { setGraphic(null); return; }
                Label lbl;
                if (driver == null || driver.isBlank()) {
                    lbl = new Label("Unassigned");
                    lbl.setStyle("-fx-font-size: 11; -fx-text-fill: #717182; -fx-font-style: italic;");
                } else {
                    lbl = new Label(driver);
                    lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #030213;");
                }
                setGraphic(lbl);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 8));
            }
        });

        colDelStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStatus()));
        colDelStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setStyle(deliveryStatusStyle(status));
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // Actions column (read-only advance/fail buttons for managers)
        colDelActions.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDeliveryId()));
        colDelActions.setCellFactory(col -> new TableCell<>() {
            final Button advBtn  = new Button();
            final Button failBtn = new Button("Fail");
            final HBox   box     = new HBox(4);
            {
                advBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                        " -fx-font-size: 11; -fx-font-weight: bold;" +
                        " -fx-padding: 5 10; -fx-background-radius: 6; -fx-cursor: hand;");
                failBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d4183d;" +
                        " -fx-font-size: 11; -fx-font-weight: bold;" +
                        " -fx-border-color: #fecaca; -fx-border-radius: 6;" +
                        " -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");
                box.setAlignment(Pos.CENTER);
            }
            @Override protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setGraphic(null); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                Delivery d = getTableView().getItems().get(idx);
                box.getChildren().clear();
                switch (d.getStatus()) {
                    case "Pending" -> {
                        advBtn.setText("Start");
                        advBtn.setOnAction(e -> handleAdvanceStatus(d));
                        box.getChildren().add(advBtn);
                    }
                    case "In Transit" -> {
                        advBtn.setText("Out for Delivery");
                        advBtn.setOnAction(e -> handleAdvanceStatus(d));
                        failBtn.setOnAction(e -> handleFailDelivery(d));
                        box.getChildren().addAll(advBtn, failBtn);
                    }
                    case "Out for Delivery" -> {
                        advBtn.setText("Mark Delivered");
                        advBtn.setOnAction(e -> handleAdvanceStatus(d));
                        failBtn.setOnAction(e -> handleFailDelivery(d));
                        box.getChildren().addAll(advBtn, failBtn);
                    }
                }
                setGraphic(box.getChildren().isEmpty() ? null : box);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private String deliveryStatusStyle(String status) {
        return switch (status) {
            case "Pending"          -> "-fx-background-color: #f3f4f6; -fx-text-fill: #717182;" +
                                       " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "In Transit"       -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;" +
                                       " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Out for Delivery" -> "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;" +
                                       " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Delivered"        -> "-fx-background-color: #030213; -fx-text-fill: white;" +
                                       " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Failed"           -> "-fx-background-color: #fef2f2; -fx-text-fill: #d4183d;" +
                                       " -fx-border-color: #fecaca; -fx-border-radius: 20;" +
                                       " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            default                 -> "-fx-background-color: #f3f4f6; -fx-text-fill: #717182;" +
                                       " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11;";
        };
    }

    private void loadDeliveries() {
        try {
            clearSectionError(logisticsErrorLabel);
            List<Delivery> deliveries = dbController.getAllDeliveries();
            allDeliveries.setAll(deliveries);
            deliveryTable.setItems(allDeliveries);
        } catch (Exception e) {
            showSectionError(logisticsErrorLabel, "Failed to load deliveries: " + e.getMessage());
        }
    }

    @FXML private void handleRefreshDeliveries() { loadDeliveries(); }

    @FXML
    private void handleSimulateDeliveries() {
        List<Delivery> snapshot = new ArrayList<>(allDeliveries);
        for (Delivery d : snapshot) {
            String s = d.getStatus();
            if ("Delivered".equals(s) || "Failed".equals(s)) continue;
            int delaySec = 3 + (int) (Math.random() * 9);
            javafx.animation.Timeline t = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.seconds(delaySec),
                    e -> {
                        try { dbController.advanceDeliveryStatus(d.getDeliveryId()); }
                        catch (Exception ex) { /* driver conflict — skip */ }
                        javafx.application.Platform.runLater(() -> {
                            if (logisticsSection.isVisible()) loadDeliveries();
                        });
                    }
                )
            );
            t.play();
        }
    }

    private void handleAdvanceStatus(Delivery d) {
        try {
            dbController.advanceDeliveryStatus(d.getDeliveryId());
            loadDeliveries();
        } catch (Exception ex) {
            showSectionError(logisticsErrorLabel, "Failed to advance: " + ex.getMessage());
        }
    }

    private void handleFailDelivery(Delivery d) {
        try {
            dbController.failDelivery(d.getDeliveryId());
            loadDeliveries();
        } catch (Exception ex) {
            showSectionError(logisticsErrorLabel, "Failed: " + ex.getMessage());
        }
    }

    private void startDeliveryAutoRefresh() {
        stopDeliveryAutoRefresh();
        deliveryAutoRefresh = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(10),
                e -> loadDeliveries()
            )
        );
        deliveryAutoRefresh.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        deliveryAutoRefresh.play();
    }

    private void stopDeliveryAutoRefresh() {
        if (deliveryAutoRefresh != null) {
            deliveryAutoRefresh.stop();
            deliveryAutoRefresh = null;
        }
    }

    // ── Inline error-banner helpers ────────────────────────────────────────────

    /**
     * Displays an error message in the given section's dedicated Label.
     * Keeps all feedback within the app UI — no native OS alert dialogs.
     */
    private void showSectionError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        HoverUtils.shake(errorLabel);
    }

    /**
     * Hides and clears a section error label.
     * Called at the start of every data-load to reset previous feedback state.
     */
    private void clearSectionError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    // ── ConfirmDialog inner class ──────────────────────────────────────────────

    /**
     * Reusable styled in-app confirmation dialog.
     *
     * <p>Demonstrates OOP concepts: <b>Encapsulation</b> (private state),
     * <b>Abstraction</b> (callers supply actions via lambdas),
     * <b>User-defined type</b> ({@code ThrowingRunnable} supports checked exceptions),
     * and <b>Single responsibility</b> (dialog UI only).</p>
     */
    private static final class ConfirmDialog {

        /** Functional interface for actions that may throw a checked exception. */
        @FunctionalInterface
        interface ThrowingRunnable {
            void run() throws Exception;
        }

        private final Stage            owner;
        private final String           heading;
        private final String           message;
        private final String           confirmLabel;
        private final ThrowingRunnable onConfirm;
        private final Runnable         onSuccess;

        ConfirmDialog(Stage owner, String heading, String message,
                      String confirmLabel,
                      ThrowingRunnable onConfirm,
                      Runnable         onSuccess) {
            this.owner        = owner;
            this.heading      = heading;
            this.message      = message;
            this.confirmLabel = confirmLabel;
            this.onConfirm    = onConfirm;
            this.onSuccess    = onSuccess;
        }

        /** Builds and displays the confirmation dialog modally. */
        void show() {
            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(heading);
            dialog.setResizable(false);

            Label titleLabel = new Label(heading);
            titleLabel.setStyle(
                "-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");

            Label bodyLabel = new Label(message);
            bodyLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #717182;");
            bodyLabel.setWrapText(true);
            bodyLabel.setMaxWidth(340);

            // Inline error label — shown when onConfirm throws; no native Alert
            Label errorLabel = new Label();
            errorLabel.setStyle(
                "-fx-font-size: 12; -fx-text-fill: #d4183d;" +
                " -fx-background-color: #fef2f2; -fx-background-radius: 6;" +
                " -fx-padding: 8 12;");
            errorLabel.setWrapText(true);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            Button cancelButton = new Button("Cancel");
            cancelButton.setStyle(
                "-fx-background-color: #f3f4f6; -fx-text-fill: #030213;" +
                " -fx-font-size: 13; -fx-font-weight: bold;" +
                " -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
            cancelButton.setOnAction(e -> dialog.close());

            Button confirmButton = new Button(confirmLabel);
            confirmButton.setStyle(
                "-fx-background-color: #d4183d; -fx-text-fill: white;" +
                " -fx-font-size: 13; -fx-font-weight: bold;" +
                " -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
            confirmButton.setOnAction(e -> {
                try {
                    onConfirm.run();
                    dialog.close();
                    onSuccess.run();
                } catch (Exception ex) {
                    errorLabel.setText("Error: " + ex.getMessage());
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                    HoverUtils.shake(errorLabel);
                }
            });

            HBox buttons = new HBox(10, cancelButton, confirmButton);
            buttons.setAlignment(Pos.CENTER_RIGHT);
            buttons.setPadding(new Insets(6, 0, 0, 0));

            VBox root = new VBox(14, titleLabel, bodyLabel, errorLabel, buttons);
            root.setStyle("-fx-background-color: white; -fx-padding: 28;");
            root.setPrefWidth(400);

            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        }
    }

    // ── Manager Inventory Section ──────────────────────────────────────────────

    private void setupManagerInventorySection() {
        mgrStockTable.getStylesheets().add(getClass().getResource("/dashboard-table.css").toExternalForm());
        mgrStockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final String RN = "-fx-background-color: white; -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String RH = "-fx-background-color: #f0f4f8; -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        mgrStockTable.setRowFactory(tv -> { TableRow<WarehouseStock> row = new TableRow<>();
            row.itemProperty().addListener((o,x,i) -> row.setStyle(i==null?"":RN));
            row.hoverProperty().addListener((o,x,h) -> { if(!row.isEmpty()) row.setStyle(h?RH:RN); }); return row; });

        colMgrStProduct.setCellValueFactory(c -> c.getValue().productNameProperty());
        colMgrStWarehouse.setCellValueFactory(c -> c.getValue().warehouseNameProperty());
        colMgrStQty.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        colMgrStQty.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty); if (empty || qty == null) { setText(null); setStyle(""); return; }
                int idx = getIndex(); if (idx < 0 || idx >= getTableView().getItems().size()) { setText(null); return; }
                WarehouseStock ws = getTableView().getItems().get(idx);
                setText(String.valueOf(qty)); setAlignment(Pos.CENTER);
                setStyle(ws.isLowStock() ? "-fx-font-size:13;-fx-text-fill:#d4183d;-fx-font-weight:bold;" : "-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });
        colMgrStMin.setCellValueFactory(c -> c.getValue().minThresholdProperty().asObject());
        colMgrStMin.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(String.valueOf(v)); setAlignment(Pos.CENTER); setStyle("-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });
        colMgrStRestock.setCellValueFactory(c -> c.getValue().lastRestockDateProperty());
        colMgrStActions.setCellValueFactory(c -> c.getValue().productNameProperty());
        colMgrStActions.setCellFactory(col -> new TableCell<>() {
            final Button restockBtn = new Button("Restock");
            { restockBtn.setStyle("-fx-background-color:#030213;-fx-text-fill:white;-fx-font-size:11;-fx-font-weight:bold;-fx-padding:5 10;-fx-background-radius:6;-fx-cursor:hand;"); }
            @Override protected void updateItem(String d, boolean empty) {
                super.updateItem(d, empty); if (empty) { setGraphic(null); return; }
                int idx = getIndex(); if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                WarehouseStock ws = getTableView().getItems().get(idx);
                restockBtn.setOnAction(e -> showMgrRestockDialog(ws));
                setGraphic(restockBtn); setAlignment(Pos.CENTER);
            }
        });

        mgrStockSearchField.textProperty().addListener((o, x, n) -> applyMgrStockFilters());
        mgrLowStockOnlyCheck.selectedProperty().addListener((o, x, n) -> applyMgrStockFilters());
    }

    private void loadManagerStock() {
        try {
            clearSectionError(inventoryErrorLabel);
            String myEmail = UserSession.getEmail();
            List<WarehouseStock> all = dbController.getAllStock();
            // Filter to only products created by this manager
            List<Product> myProducts = dbController.getProductsByCreator(myEmail);
            java.util.Set<Integer> myProductIDs = new java.util.HashSet<>();
            for (Product p : myProducts) myProductIDs.add(p.getProductID());
            mgrAllStock.setAll(all.stream().filter(ws -> myProductIDs.contains(ws.getProductID())).toList());
            applyMgrStockFilters();
        } catch (Exception e) {
            showSectionError(inventoryErrorLabel, "Failed to load stock: " + e.getMessage());
        }
    }

    private void applyMgrStockFilters() {
        String search = mgrStockSearchField.getText() == null ? "" : mgrStockSearchField.getText().trim().toLowerCase();
        boolean lowOnly = mgrLowStockOnlyCheck.isSelected();
        mgrFilteredStock.setAll(mgrAllStock.stream().filter(ws -> {
            boolean ms = search.isEmpty() || ws.getProductName().toLowerCase().contains(search) || ws.getWarehouseName().toLowerCase().contains(search);
            boolean ml = !lowOnly || ws.isLowStock();
            return ms && ml;
        }).toList());
        mgrStockTable.setItems(mgrFilteredStock);
    }

    private void showMgrRestockDialog(WarehouseStock ws) {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) productTable.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Restock");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(380);

        Label title = new Label("Restock Product");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");
        Label info = new Label(ws.getProductName() + " @ " + ws.getWarehouseName() + "\nCurrent qty: " + ws.getQuantity());
        info.setStyle("-fx-font-size: 13; -fx-text-fill: #717182;"); info.setWrapText(true);

        Label qtyLbl = new Label("Add Quantity *"); qtyLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField qtyField = new TextField("1"); qtyField.setPrefHeight(38);
        qtyField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label errLbl = new Label(); errLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;"); errLbl.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #030213; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());
        Button saveBtn = new Button("Restock");
        saveBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                dbController.restockWarehouseProduct(ws.getStockID(), qty);
                dialog.close(); loadManagerStock();
            } catch (NumberFormatException ex) { errLbl.setText("Please enter a valid positive integer."); HoverUtils.shake(errLbl);
            } catch (Exception ex) { errLbl.setText(ex.getMessage()); HoverUtils.shake(errLbl); }
        });

        HBox btnRow = new HBox(10, cancelBtn, saveBtn); btnRow.setAlignment(Pos.CENTER_RIGHT); btnRow.setPadding(new Insets(6,0,0,0));
        root.getChildren().addAll(title, info, qtyLbl, qtyField, errLbl, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }
}
