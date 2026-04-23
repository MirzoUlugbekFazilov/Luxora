package Luxora.Luxora;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdminDashboardController {

    // ── Root ───────────────────────────────────────────────────────────────────
    @FXML private javafx.scene.layout.BorderPane rootPane;

    // ── Navigation ─────────────────────────────────────────────────────────────
    @FXML private HBox  navDashboard, navProfile, navLogistics, navOrders, navInventory, navReviews;
    @FXML private Label navDashboardLabel, navProfileLabel, navLogisticsLabel, navOrdersLabel, navInventoryLabel, navReviewsLabel;
    @FXML private VBox  dashboardSection, profileSection, logisticsSection, ordersSection, inventorySection, adminReviewsSection;

    // ── Inventory ──────────────────────────────────────────────────────────────
    @FXML private Button   btnWarehousesTab, btnStockTab, btnReportsTab;
    @FXML private Button   btnAddWarehouse, btnAddStock;
    @FXML private VBox     warehousesSubView, stockSubView, reportsSubView;
    @FXML private Label    inventoryErrorLabel;
    @FXML private TableView<Warehouse>     warehousesTable;
    @FXML private TableColumn<Warehouse, String>  colWhName;
    @FXML private TableColumn<Warehouse, String>  colWhLocation;
    @FXML private TableColumn<Warehouse, String>  colWhContact;
    @FXML private TableColumn<Warehouse, String>  colWhCapacity;
    @FXML private TableColumn<Warehouse, String>  colWhUsed;
    @FXML private TableColumn<Warehouse, String>  colWhActions;
    @FXML private TableView<WarehouseStock> stockTable;
    @FXML private TableColumn<WarehouseStock, String>  colStProduct;
    @FXML private TableColumn<WarehouseStock, String>  colStWarehouse;
    @FXML private TableColumn<WarehouseStock, Integer> colStQty;
    @FXML private TableColumn<WarehouseStock, Integer> colStMin;
    @FXML private TableColumn<WarehouseStock, String>  colStRestock;
    @FXML private TableColumn<WarehouseStock, String>  colStActions;
    @FXML private TextField  stockSearchField;
    @FXML private ComboBox<String> stockWarehouseFilter;
    @FXML private javafx.scene.control.CheckBox lowStockOnlyCheck;
    // Reports tables
    @FXML private TableView<String[]>     totalStockTable;
    @FXML private TableColumn<String[], String> colRptProduct, colRptCategory, colRptWHQty, colRptCatQty, colRptWHCount;
    @FXML private TableView<String[]>     capacityTable;
    @FXML private TableColumn<String[], String> colCapName, colCapLocation, colCapTotal, colCapUsed, colCapUtil;
    private final ObservableList<Warehouse>      allWarehouses = FXCollections.observableArrayList();
    private final ObservableList<WarehouseStock> allStock      = FXCollections.observableArrayList();
    private final ObservableList<WarehouseStock> filteredStock = FXCollections.observableArrayList();
    private final ObservableList<String[]>       totalStockData   = FXCollections.observableArrayList();
    private final ObservableList<String[]>       capacityData     = FXCollections.observableArrayList();

    // ── Admin Reviews ──────────────────────────────────────────────────────────
    @FXML private TableView<Review>           adminReviewsTable;
    @FXML private TableColumn<Review, String> colRevProduct, colRevCustomer, colRevRating, colRevComment, colRevDate, colRevFlagged, colRevActions;
    @FXML private TextField  reviewSearchField;
    @FXML private ComboBox<String> reviewRatingFilter;
    @FXML private javafx.scene.control.CheckBox flaggedOnlyCheck;
    @FXML private Label adminReviewsErrorLabel;
    private final ObservableList<Review> allAdminReviews      = FXCollections.observableArrayList();
    private final ObservableList<Review> filteredAdminReviews = FXCollections.observableArrayList();

    // ── Admin Orders ───────────────────────────────────────────────────────────
    @FXML private TableView<Order>            adminOrdersTable;
    @FXML private TableColumn<Order, String>  colAdmOrderCode;
    @FXML private TableColumn<Order, String>  colAdmOrderDate;
    @FXML private TableColumn<Order, String>  colAdmOrderEmail;
    @FXML private TableColumn<Order, Integer> colAdmOrderItems;
    @FXML private TableColumn<Order, Double>  colAdmOrderTotal;
    @FXML private TableColumn<Order, String>  colAdmOrderStatus;
    @FXML private TableColumn<Order, String>  colAdmOrderActions;
    @FXML private ComboBox<String>            adminOrderStatusFilter;
    @FXML private Label                       adminOrderCountLabel;
    @FXML private Label                       ordersErrorLabel;

    private final ObservableList<Order> allAdminOrders = FXCollections.observableArrayList();

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

    private final ObservableList<Delivery> allDeliveries = FXCollections.observableArrayList();
    private javafx.animation.Timeline deliveryAutoRefresh;

    // ── Import ─────────────────────────────────────────────────────────────────
    @FXML private HBox  navImport;
    @FXML private Label navImportLabel;
    @FXML private VBox  importSection;
    @FXML private VBox  importDropZone;
    @FXML private VBox  importResultsBox;

    // ── Users ──────────────────────────────────────────────────────────────────
    @FXML private HBox  navUsers;
    @FXML private Label navUsersLabel;
    @FXML private VBox  usersSection;
    @FXML private TableView<User>           usersTable;
    @FXML private TableColumn<User, String> colUserName;
    @FXML private TableColumn<User, String> colUserEmail;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserActions;

    private final ObservableList<User> allUsers = FXCollections.observableArrayList();

    // ── Profile ────────────────────────────────────────────────────────────────
    @FXML private Label profileNameLabel, profileEmailLabel;

    // ── Inline error banners (shown/hidden programmatically; no native Alert pop-ups) ──
    @FXML private Label dashboardErrorLabel;
    @FXML private Label usersErrorLabel;

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
    private final Map<String, String>     creatorNames     = new HashMap<>();

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

    // ── Protected super-admin account ─────────────────────────────────────────
    /**
     * The bootstrap administrator account.
     * Only this account may edit itself, and its role can never be changed.
     */
    private static final String SUPER_ADMIN_EMAIL = "realadmin@gmail.com";

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
        navImport.setOnMouseClicked(e -> showSection("import"));
        navUsers.setOnMouseClicked(e -> showSection("users"));
        navOrders.setOnMouseClicked(e -> showSection("orders"));
        navLogistics.setOnMouseClicked(e -> showSection("logistics"));
        navInventory.setOnMouseClicked(e -> showSection("inventory"));
        navReviews.setOnMouseClicked(e -> showSection("reviews"));

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

        SVGPath importIcon = makeNavIcon(
            "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12", false);
        navImport.getChildren().add(0, importIcon);
        HBox.setMargin(importIcon, new Insets(0, 8, 0, 0));

        SVGPath usersIcon = makeNavIcon(
            "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z" +
            "M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75", false);
        navUsers.getChildren().add(0, usersIcon);
        HBox.setMargin(usersIcon, new Insets(0, 8, 0, 0));

        SVGPath ordersIcon = makeNavIcon(
            "M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" +
            "M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2" +
            "M9 12h6M9 16h4", false);
        navOrders.getChildren().add(0, ordersIcon);
        HBox.setMargin(ordersIcon, new Insets(0, 8, 0, 0));

        SVGPath logisticsIcon = makeNavIcon(
            "M1 3h15v13H1zM16 8h4l3 3v5h-7V8z" +
            "M5.5 21a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z" +
            "M18.5 21a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z", false);
        navLogistics.getChildren().add(0, logisticsIcon);
        HBox.setMargin(logisticsIcon, new Insets(0, 8, 0, 0));

        SVGPath inventoryIcon = makeNavIcon(
            "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z", false);
        navInventory.getChildren().add(0, inventoryIcon);
        HBox.setMargin(inventoryIcon, new Insets(0, 8, 0, 0));

        SVGPath reviewsIcon = makeNavIcon(
            "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z", false);
        navReviews.getChildren().add(0, reviewsIcon);
        HBox.setMargin(reviewsIcon, new Insets(0, 8, 0, 0));

        // Refresh button for logistics
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

        // Admin orders status filter
        adminOrderStatusFilter.setItems(FXCollections.observableArrayList(
                "All Statuses", "Processing", "Shipped", "Delivered", "Cancelled", "Returned", "Failed"));
        adminOrderStatusFilter.setValue("All Statuses");
        adminOrderStatusFilter.valueProperty().addListener((o, old, nw) -> loadAllOrders());

        setupAdminOrdersTable();
        setupDeliveryTable();
        setupUsersTable();

        // Import drop zone handlers
        importDropZone.setOnMouseClicked(e -> handleChooseImportFile());
        importDropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
                importDropZone.setStyle(importDropZoneStyle(true));
            }
            event.consume();
        });
        importDropZone.setOnDragExited(event -> importDropZone.setStyle(importDropZoneStyle(false)));
        importDropZone.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                File dropped = db.getFiles().get(0);
                importDropZone.setStyle(importDropZoneStyle(false));
                processImportFile(dropped);
            }
            event.setDropCompleted(true);
            event.consume();
        });

        setupExportTable();
        setupInventorySection();
        setupAdminReviewsSection();
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
        dashboardSection.setVisible(false);    dashboardSection.setManaged(false);
        profileSection.setVisible(false);      profileSection.setManaged(false);
        exportSection.setVisible(false);       exportSection.setManaged(false);
        importSection.setVisible(false);       importSection.setManaged(false);
        usersSection.setVisible(false);        usersSection.setManaged(false);
        ordersSection.setVisible(false);       ordersSection.setManaged(false);
        logisticsSection.setVisible(false);    logisticsSection.setManaged(false);
        inventorySection.setVisible(false);    inventorySection.setManaged(false);
        adminReviewsSection.setVisible(false); adminReviewsSection.setManaged(false);
        setNavActive(navDashboard, navDashboardLabel, false);
        setNavActive(navProfile,   navProfileLabel,   false);
        setNavActive(navExport,    navExportLabel,    false);
        setNavActive(navImport,    navImportLabel,    false);
        setNavActive(navUsers,     navUsersLabel,     false);
        setNavActive(navOrders,    navOrdersLabel,    false);
        setNavActive(navLogistics, navLogisticsLabel, false);
        setNavActive(navInventory, navInventoryLabel, false);
        setNavActive(navReviews,   navReviewsLabel,   false);

        switch (section) {
            case "profile"   -> { stopDeliveryAutoRefresh();
                                  profileSection.setVisible(true); profileSection.setManaged(true);
                                  setNavActive(navProfile, navProfileLabel, true); }
            case "export"    -> { stopDeliveryAutoRefresh();
                                  exportSection.setVisible(true);  exportSection.setManaged(true);
                                  setNavActive(navExport, navExportLabel, true);
                                  loadExportProducts(); }
            case "import"    -> { stopDeliveryAutoRefresh();
                                  importSection.setVisible(true);  importSection.setManaged(true);
                                  setNavActive(navImport, navImportLabel, true);
                                  importResultsBox.setVisible(false); importResultsBox.setManaged(false); }
            case "users"     -> { stopDeliveryAutoRefresh();
                                  usersSection.setVisible(true);   usersSection.setManaged(true);
                                  setNavActive(navUsers, navUsersLabel, true);
                                  loadUsers(); }
            case "orders"    -> { stopDeliveryAutoRefresh();
                                  ordersSection.setVisible(true);  ordersSection.setManaged(true);
                                  setNavActive(navOrders, navOrdersLabel, true);
                                  loadAllOrders(); }
            case "logistics" -> { logisticsSection.setVisible(true); logisticsSection.setManaged(true);
                                  setNavActive(navLogistics, navLogisticsLabel, true);
                                  loadDeliveries();
                                  startDeliveryAutoRefresh(); }
            case "inventory" -> { stopDeliveryAutoRefresh();
                                  inventorySection.setVisible(true); inventorySection.setManaged(true);
                                  setNavActive(navInventory, navInventoryLabel, true);
                                  loadWarehouses();
                                  loadAllStock(); }
            case "reviews"   -> { stopDeliveryAutoRefresh();
                                  adminReviewsSection.setVisible(true); adminReviewsSection.setManaged(true);
                                  setNavActive(navReviews, navReviewsLabel, true);
                                  loadAdminReviews(); }
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
     * Loads all products from the database and populates the product table.
     * Clears any previous error banner before loading; shows one inline on failure.
     */
    public void loadProducts() {
        try {
            clearSectionError(dashboardErrorLabel);
            List<Product> products = dbController.getAllProducts();
            allProducts.setAll(products);

            // Build email→name map for creator labels (admin-only feature)
            creatorNames.clear();
            for (Product p : products) {
                String email = p.getCreatedBy();
                if (email != null && !email.isBlank() && !creatorNames.containsKey(email)) {
                    try {
                        String name = dbController.getUserName(email);
                        creatorNames.put(email, name.isBlank() ? email : name);
                    } catch (Exception ignored) {
                        creatorNames.put(email, email);
                    }
                }
            }

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

        // ── Image + Creator name (admin-only) ──────────────────────────────────
        colImage.setCellValueFactory(c -> c.getValue().imagesProperty());
        colImage.setCellFactory(col -> new TableCell<>() {
            final ImageView iv = new ImageView();
            final StackPane imgBox = new StackPane(iv);
            final Label creatorLabel = new Label();
            final VBox cell = new VBox(4, imgBox, creatorLabel);
            {
                iv.setFitWidth(48); iv.setFitHeight(48);
                iv.setPreserveRatio(false);
                imgBox.setMinSize(48, 48);
                imgBox.setMaxSize(48, 48);
                imgBox.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 6;");
                creatorLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #717182;");
                creatorLabel.setMaxWidth(68);
                creatorLabel.setWrapText(true);
                cell.setAlignment(Pos.CENTER);
                setAlignment(Pos.CENTER);
            }
            @Override protected void updateItem(String imgs, boolean empty) {
                super.updateItem(imgs, empty);
                if (empty) { setGraphic(null); return; }
                if (imgs == null || imgs.isBlank()) {
                    iv.setImage(null);
                } else {
                    String[] parts = imgs.split("\\|", -1);
                    if (parts.length > 0 && !parts[0].isBlank()) {
                        try {
                            byte[] dec = Base64.getDecoder().decode(parts[0].trim());
                            iv.setImage(new Image(new ByteArrayInputStream(dec)));
                        } catch (Exception ex) { iv.setImage(null); }
                    } else { iv.setImage(null); }
                }
                Product p = getTableRow().getItem();
                if (p != null && p.getCreatedBy() != null && !p.getCreatedBy().isBlank()) {
                    String name = creatorNames.getOrDefault(p.getCreatedBy(), p.getCreatedBy());
                    creatorLabel.setText(name);
                    creatorLabel.setVisible(true);
                    creatorLabel.setManaged(true);
                } else {
                    creatorLabel.setText("");
                    creatorLabel.setVisible(false);
                    creatorLabel.setManaged(false);
                }
                setGraphic(cell);
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
                fp.setHgap(4); fp.setVgap(4);
                fp.setAlignment(Pos.CENTER);
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
                setGraphic(badge);
                setAlignment(Pos.CENTER);
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
     * Uses the {@link ConfirmDialog} helper to keep all feedback on-screen.
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
     * Opens a styled in-app confirmation dialog so the admin can delete their own account.
     * On confirmation: deletes the DB record, clears the session, and navigates to Start.
     * All errors are displayed inline within the dialog — no native OS alerts.
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

        // Inline error label — visible only when an operation fails
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
                dbController.deleteProductsByCreator(UserSession.getEmail());
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

    // ── Users ──────────────────────────────────────────────────────────────────

    private void setupUsersTable() {
        usersTable.getStylesheets().add(
                getClass().getResource("/dashboard-table.css").toExternalForm());
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final String ROW_NORMAL = "-fx-background-color: white;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String ROW_HOVER  = "-fx-background-color: #f0f4f8;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        usersTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) -> row.setStyle(item == null ? "" : ROW_NORMAL));
            row.hoverProperty().addListener((obs, was, isNow) -> {
                if (!row.isEmpty()) row.setStyle(isNow ? ROW_HOVER : ROW_NORMAL);
            });
            return row;
        });

        colUserName.setCellValueFactory(c -> c.getValue().nameProperty());
        colUserName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setText(null); return; }
                setText(name);
                setStyle("-fx-font-size: 13; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER);
            }
        });

        colUserEmail.setCellValueFactory(c -> c.getValue().emailProperty());
        colUserEmail.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) { setText(null); return; }
                setText(email);
                setStyle("-fx-font-size: 13; -fx-text-fill: #717182;");
                setAlignment(Pos.CENTER);
            }
        });

        colUserRole.setCellValueFactory(c -> c.getValue().nameProperty()); // triggers updates
        colUserRole.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String dummy, boolean empty) {
                super.updateItem(dummy, empty);
                if (empty) { setGraphic(null); return; }
                User u = getTableRow().getItem();
                if (u == null) { setGraphic(null); return; }
                Label badge = new Label(u.getRoleLabel());
                String color = switch (u.getUserType()) {
                    case 3  -> "#030213";
                    case 2  -> "#5b21b6";
                    default -> "#717182";
                };
                badge.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + ";" +
                        " -fx-border-color: " + color + "; -fx-border-radius: 20;" +
                        " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;");
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        colUserActions.setCellValueFactory(c -> c.getValue().nameProperty());
        colUserActions.setCellFactory(col -> new TableCell<>() {
            final Button editBtn   = makeOutlineBtn();
            final Button deleteBtn = makeOutlineBtn();
            final HBox   box       = new HBox(4, editBtn, deleteBtn);
            { box.setAlignment(Pos.CENTER); }

            @Override protected void updateItem(String dummy, boolean empty) {
                super.updateItem(dummy, empty);
                if (empty) { setGraphic(null); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                User u = getTableView().getItems().get(idx);

                boolean rowIsSuperAdmin    = SUPER_ADMIN_EMAIL.equals(u.getEmail());
                boolean viewerIsSuperAdmin = SUPER_ADMIN_EMAIL.equals(UserSession.getEmail());

                // Only the super-admin can see/use actions on their own row;
                // no other admin may edit or delete the super-admin account.
                if (rowIsSuperAdmin && !viewerIsSuperAdmin) {
                    setGraphic(null);
                    return;
                }

                editBtn  .setGraphic(makeSvgIcon(EDIT_PATH,  "#717182"));
                deleteBtn.setGraphic(makeSvgIcon(TRASH_PATH, "#d4183d"));
                // Delete is hidden for the super-admin row (use Profile section instead)
                deleteBtn.setVisible(!rowIsSuperAdmin);
                deleteBtn.setManaged(!rowIsSuperAdmin);
                editBtn  .setOnAction(e -> handleEditUser(u));
                deleteBtn.setOnAction(e -> handleDeleteUser(u));
                setGraphic(box);
                setAlignment(Pos.CENTER);
            }
        });
    }

    /**
     * Fetches all users from the database and populates the users table.
     * Displays any database error inline within the Users section.
     */
    private void loadUsers() {
        try {
            clearSectionError(usersErrorLabel);
            allUsers.setAll(dbController.getAllUsers());
            usersTable.setItems(allUsers);
        } catch (Exception e) {
            showSectionError(usersErrorLabel, "Failed to load users: " + e.getMessage());
        }
    }

    private void handleEditUser(User u) {
        Stage dialog = new Stage();
        dialog.initOwner(usersTable.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit User");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(400);

        Label title = new Label("Edit User");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");

        Label nameLbl = new Label("Name");
        nameLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField nameField = new TextField(u.getName());
        nameField.setPrefHeight(38);
        nameField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8;" +
                " -fx-padding: 0 12; -fx-font-size: 13;");

        Label emailLbl = new Label("Email");
        emailLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField emailField = new TextField(u.getEmail());
        emailField.setPrefHeight(38);
        emailField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8;" +
                " -fx-padding: 0 12; -fx-font-size: 13;");

        // The super-admin's role is permanently locked — nobody can change it
        boolean editingSuperAdmin = SUPER_ADMIN_EMAIL.equals(u.getEmail());

        Label roleLbl = new Label("Role");
        roleLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        ComboBox<String> roleBox = new ComboBox<>(
                FXCollections.observableArrayList("Customer", "Product Manager", "Admin"));
        roleBox.setValue(u.getRoleLabel());
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setPrefHeight(38);
        // Disable role selection for the super-admin — role is always Admin
        if (editingSuperAdmin) {
            roleBox.setDisable(true);
        }

        // Note shown only when editing the super-admin account
        Label roleNoteLbl = new Label("Role is permanently locked for this account.");
        roleNoteLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #717182; -fx-font-style: italic;");
        roleNoteLbl.setVisible(editingSuperAdmin);
        roleNoteLbl.setManaged(editingSuperAdmin);

        Label errLabel = new Label();
        errLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;");
        errLabel.setWrapText(true);

        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(6, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #030213;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String newName  = nameField.getText().trim();
            String newEmail = emailField.getText().trim();
            if (newName.isEmpty() || newEmail.isEmpty()) {
                errLabel.setText("Name and email cannot be empty.");
                HoverUtils.shake(errLabel);
                return;
            }
            // Super-admin role is always Admin (3) regardless of ComboBox state
            int newType = editingSuperAdmin ? 3 : switch (roleBox.getValue()) {
                case "Product Manager" -> 2;
                case "Admin"           -> 3;
                default                -> 1;
            };
            try {
                dbController.updateUser(u.getEmail(), newName, newEmail, newType);
                dialog.close();

                // If the admin just demoted their own account, their current session
                // is no longer valid — clear it and redirect to the Start screen.
                boolean editedSelf   = u.getEmail().equals(UserSession.getEmail());
                boolean demotedSelf  = editedSelf && newType != 3;
                if (demotedSelf) {
                    UserSession.clear();
                    Scene scene = productTable.getScene();
                    scene.setFill(Color.web("#162535"));
                    Parent navRoot = FXMLLoader.load(
                            getClass().getResource("/Start.fxml"));
                    scene.setRoot(navRoot);
                } else {
                    loadUsers();
                }
            } catch (Exception ex) {
                errLabel.setText("Failed to update: " + ex.getMessage());
                HoverUtils.shake(errLabel);
            }
        });

        btnRow.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(title, nameLbl, nameField, emailLbl, emailField,
                roleLbl, roleBox, roleNoteLbl, errLabel, btnRow);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    /**
     * Opens a styled in-app confirmation dialog before deleting a user account.
     * Uses the {@link ConfirmDialog} helper class — no native OS dialogs.
     *
     * @param user the user selected for deletion
     */
    private void handleDeleteUser(User user) {
        boolean deletingSelf = user.getEmail().equals(UserSession.getEmail());
        new ConfirmDialog(
            (Stage) usersTable.getScene().getWindow(),
            "Delete User",
            "Are you sure you want to delete \"" + user.getName() + "\"?\n" +
            "This action cannot be undone.",
            "Delete",
            () -> {
                // Delete products first if the user is a product manager or admin
                if (user.getUserType() == 2 || user.getUserType() == 3) {
                    dbController.deleteProductsByCreator(user.getEmail());
                }
                dbController.deleteAccount(user.getEmail());
            },
            () -> {
                if (deletingSelf) {
                    UserSession.clear();
                    try {
                        Scene scene = usersTable.getScene();
                        scene.setFill(Color.web("#162535"));
                        Parent navRoot = FXMLLoader.load(getClass().getResource("/Start.fxml"));
                        scene.setRoot(navRoot);
                    } catch (Exception ex) {
                        showSectionError(usersErrorLabel, "Deleted. Failed to navigate: " + ex.getMessage());
                    }
                } else {
                    loadUsers();
                }
            }
        ).show();
    }

    // ── Import ─────────────────────────────────────────────────────────────────

    private void handleChooseImportFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Import File");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Spreadsheet Files (CSV, XLSX)", "*.csv", "*.xlsx"));
        File file = fc.showOpenDialog(importDropZone.getScene().getWindow());
        if (file != null) processImportFile(file);
    }

    private void processImportFile(File file) {
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".csv") && !name.endsWith(".xlsx")) {
            showImportResults(0, List.of("Unsupported file type. Please upload a .csv or .xlsx file."));
            return;
        }
        try {
            List<String[]> rows = name.endsWith(".xlsx") ? readXlsxRows(file) : readCsvRows(file);
            if (rows.size() < 2) {
                showImportResults(0, List.of("The file contains no data rows (only a header or is empty)."));
                return;
            }
            processImportRows(rows);
        } catch (Exception e) {
            showImportResults(0, List.of("Failed to read file: " + e.getMessage()));
        }
    }

    private void processImportRows(List<String[]> rows) {
        int imported = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length == 0 || (row.length == 1 && row[0].isBlank())) continue; // skip blank rows

            String name        = get(row, 0).trim();
            String categories  = get(row, 1).trim();
            String description = get(row, 2).trim();
            String priceStr    = get(row, 3).trim();
            String stockStr    = get(row, 4).trim();
            String status      = get(row, 5).trim();

            if (name.isEmpty()) {
                errors.add("Row " + (i + 1) + ": Name is required — row skipped.");
                continue;
            }

            double price;
            try { price = priceStr.isEmpty() ? 0 : Double.parseDouble(priceStr); }
            catch (NumberFormatException e) {
                errors.add("Row " + (i + 1) + ": Invalid price \"" + priceStr + "\" — row skipped.");
                continue;
            }

            int stock;
            try { stock = stockStr.isEmpty() ? 0 : (int) Double.parseDouble(stockStr); }
            catch (NumberFormatException e) {
                errors.add("Row " + (i + 1) + ": Invalid stock \"" + stockStr + "\" — row skipped.");
                continue;
            }

            if (!status.equalsIgnoreCase("active") && !status.equalsIgnoreCase("inactive"))
                status = "active";

            try {
                dbController.addProduct(name, categories, description, price, stock,
                        status.toLowerCase(), "", UserSession.getEmail());
                imported++;
            } catch (Exception e) {
                errors.add("Row " + (i + 1) + ": Database error — " + e.getMessage());
            }
        }

        showImportResults(imported, errors);
        if (imported > 0) loadProducts();
    }

    private void showImportResults(int imported, List<String> errors) {
        importResultsBox.getChildren().clear();
        importResultsBox.setVisible(true);
        importResultsBox.setManaged(true);

        String bannerBg, bannerFg, icon;
        if (errors.isEmpty() && imported > 0) {
            bannerBg = "#f0fdf4"; bannerFg = "#16a34a"; icon = "✓";
        } else if (imported > 0) {
            bannerBg = "#fffbeb"; bannerFg = "#d97706"; icon = "⚠";
        } else {
            bannerBg = "#fef2f2"; bannerFg = "#d4183d"; icon = "✕";
        }

        String summary = icon + "  " + imported + " product" + (imported == 1 ? "" : "s") +
                " imported successfully" +
                (errors.isEmpty() ? "." : ",  " + errors.size() + " row" + (errors.size() == 1 ? "" : "s") + " skipped.");

        VBox banner = new VBox();
        banner.setStyle("-fx-background-color: " + bannerBg + "; -fx-background-radius: 8; -fx-padding: 14 16;");
        Label summaryLbl = new Label(summary);
        summaryLbl.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: " + bannerFg + ";");
        banner.getChildren().add(summaryLbl);
        importResultsBox.getChildren().add(banner);

        if (!errors.isEmpty()) {
            VBox errBox = new VBox(6);
            errBox.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 8; -fx-padding: 14 16;");
            Label errTitle = new Label("Skipped rows:");
            errTitle.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #d4183d;");
            errBox.getChildren().add(errTitle);
            for (String err : errors) {
                Label lbl = new Label("• " + err);
                lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #b91c1c;");
                lbl.setWrapText(true);
                errBox.getChildren().add(lbl);
            }
            importResultsBox.getChildren().add(errBox);
        }
    }

    // ── Static import readers ──────────────────────────────────────────────────

    private static List<String[]> readCsvRows(File file) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }

    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') { sb.append('"'); i++; }
                    else inQuotes = false;
                } else sb.append(c);
            } else if (c == '"') { inQuotes = true; }
            else if (c == ',') { fields.add(sb.toString()); sb.setLength(0); }
            else sb.append(c);
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private static List<String[]> readXlsxRows(File file) throws Exception {
        byte[] sharedStrBytes = null, sheetBytes = null;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                String n = ze.getName();
                if ("xl/sharedStrings.xml".equals(n))               sharedStrBytes = zis.readAllBytes();
                else if (n.startsWith("xl/worksheets/sheet") && sheetBytes == null) sheetBytes = zis.readAllBytes();
            }
        }
        if (sheetBytes == null) throw new Exception("No worksheet found in the XLSX file.");

        // Parse shared strings table
        List<String> ss = new ArrayList<>();
        if (sharedStrBytes != null) {
            NodeList siList = parseXmlBytes(sharedStrBytes).getElementsByTagName("si");
            for (int i = 0; i < siList.getLength(); i++) {
                NodeList ts = ((Element) siList.item(i)).getElementsByTagName("t");
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < ts.getLength(); j++) sb.append(ts.item(j).getTextContent());
                ss.add(sb.toString());
            }
        }

        // Parse sheet rows
        List<String[]> result = new ArrayList<>();
        NodeList rowList = parseXmlBytes(sheetBytes).getElementsByTagName("row");
        for (int r = 0; r < rowList.getLength(); r++) {
            Element rowEl = (Element) rowList.item(r);
            NodeList cells = rowEl.getElementsByTagName("c");
            int maxCol = 0;
            for (int ci = 0; ci < cells.getLength(); ci++)
                maxCol = Math.max(maxCol, xlColIndex(((Element) cells.item(ci)).getAttribute("r")));

            String[] row = new String[maxCol + 1];
            Arrays.fill(row, "");
            for (int ci = 0; ci < cells.getLength(); ci++) {
                Element cell = (Element) cells.item(ci);
                int col = xlColIndex(cell.getAttribute("r"));
                String type = cell.getAttribute("t");
                String val  = "";
                if ("s".equals(type)) {
                    NodeList v = cell.getElementsByTagName("v");
                    if (v.getLength() > 0) {
                        int idx = Integer.parseInt(v.item(0).getTextContent().trim());
                        val = idx < ss.size() ? ss.get(idx) : "";
                    }
                } else if ("inlineStr".equals(type)) {
                    NodeList ts = cell.getElementsByTagName("t");
                    StringBuilder sb = new StringBuilder();
                    for (int t = 0; t < ts.getLength(); t++) sb.append(ts.item(t).getTextContent());
                    val = sb.toString();
                } else {
                    NodeList v = cell.getElementsByTagName("v");
                    if (v.getLength() > 0) val = v.item(0).getTextContent().trim();
                }
                row[col] = val;
            }
            result.add(row);
        }
        return result;
    }

    private static int xlColIndex(String cellRef) {
        int col = 0;
        for (char c : cellRef.toCharArray()) {
            if (!Character.isLetter(c)) break;
            col = col * 26 + (Character.toUpperCase(c) - 'A' + 1);
        }
        return col - 1;
    }

    private static Document parseXmlBytes(byte[] data) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(data));
    }

    private static String get(String[] arr, int idx) {
        return (arr != null && idx < arr.length && arr[idx] != null) ? arr[idx] : "";
    }

    private static String importDropZoneStyle(boolean active) {
        return active
            ? "-fx-border-color: #3b82f6; -fx-border-style: dashed; -fx-border-width: 2;" +
              " -fx-border-radius: 10; -fx-background-color: #eff6ff; -fx-background-radius: 10;" +
              " -fx-cursor: hand; -fx-padding: 20;"
            : "-fx-border-color: #d1d5db; -fx-border-style: dashed; -fx-border-width: 2;" +
              " -fx-border-radius: 10; -fx-background-color: white; -fx-background-radius: 10;" +
              " -fx-cursor: hand; -fx-padding: 20;";
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
            List<Product> products = dbController.getAllProducts();
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

    /** Parses a String to double; returns {@code def} if the value is blank or unparseable. */
    private double parseDouble(String s, double def) {
        try { return (s == null || s.isBlank()) ? def : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    /** Returns {@code s} unchanged, or an empty String if {@code s} is null. */
    private String nvl(String s) { return s == null ? "" : s; }

    // ── Inline error-banner helpers ────────────────────────────────────────────

    /**
     * Displays an error message inside the given section's dedicated Label.
     * Keeps all feedback within the app UI — no native OS alert dialogs.
     *
     * @param errorLabel the Label to populate
     * @param message    the error text to display
     */
    private void showSectionError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        HoverUtils.shake(errorLabel);
    }

    /**
     * Hides and clears a section error label.
     * Called at the start of every data-load to reset the previous feedback state.
     *
     * @param errorLabel the Label to clear
     */
    private void clearSectionError(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    // ── Delivery / Logistics ───────────────────────────────────────────────────

    // ── Admin Orders ───────────────────────────────────────────────────────────

    private void loadAllOrders() {
        try {
            clearSectionError(ordersErrorLabel);
            List<Order> orders = dbController.getAllOrders();
            String filter = adminOrderStatusFilter.getValue();
            if (filter != null && !"All Statuses".equals(filter)) {
                orders = orders.stream().filter(o -> filter.equals(o.getStatus())).toList();
            }
            allAdminOrders.setAll(orders);
            adminOrdersTable.setItems(allAdminOrders);
            int count = allAdminOrders.size();
            adminOrderCountLabel.setText(count + " order" + (count == 1 ? "" : "s"));
        } catch (Exception e) {
            showSectionError(ordersErrorLabel, "Failed to load orders: " + e.getMessage());
        }
    }

    private void setupAdminOrdersTable() {
        adminOrdersTable.getStylesheets().add(
                getClass().getResource("/dashboard-table.css").toExternalForm());
        adminOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final String ROW_NORMAL = "-fx-background-color: white;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String ROW_HOVER  = "-fx-background-color: #f0f4f8;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        adminOrdersTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) ->
                    row.setStyle(item == null ? "" : ROW_NORMAL));
            row.hoverProperty().addListener((obs, was, isNow) -> {
                if (!row.isEmpty()) row.setStyle(isNow ? ROW_HOVER : ROW_NORMAL);
            });
            return row;
        });

        // Order Code
        colAdmOrderCode.setCellValueFactory(c -> c.getValue().orderCodeProperty());
        colAdmOrderCode.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);
                if (empty || code == null) { setGraphic(null); return; }
                Label lbl = new Label(code);
                lbl.setStyle("-fx-font-size: 13; -fx-font-weight: bold;" +
                        " -fx-text-fill: #030213; -fx-font-family: Poppins;");
                setGraphic(lbl);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 10));
            }
        });

        // Date
        colAdmOrderDate.setCellValueFactory(c -> c.getValue().orderDateProperty());
        colAdmOrderDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) { setText(null); return; }
                setText(date);
                setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
                setAlignment(Pos.CENTER);
            }
        });

        // Customer email
        colAdmOrderEmail.setCellValueFactory(c -> c.getValue().userEmailProperty());
        colAdmOrderEmail.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null) { setText(null); return; }
                setText(email);
                setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 6));
            }
        });

        // Items
        colAdmOrderItems.setCellValueFactory(c -> c.getValue().itemCountProperty().asObject());
        colAdmOrderItems.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer cnt, boolean empty) {
                super.updateItem(cnt, empty);
                if (empty || cnt == null) { setText(null); return; }
                setText(String.valueOf(cnt));
                setStyle("-fx-font-size: 13; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER);
            }
        });

        // Total
        colAdmOrderTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());
        colAdmOrderTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) { setText(null); return; }
                setText(String.format("$%.2f", total));
                setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER);
            }
        });

        // Status badge
        colAdmOrderStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colAdmOrderStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setStyle(adminOrderStatusBadge(status));
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // Actions — update status dropdown
        colAdmOrderActions.setCellValueFactory(c -> c.getValue().statusProperty());
        colAdmOrderActions.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                Order o = getTableView().getItems().get(idx);

                // Build context-appropriate action buttons
                HBox box = new HBox(4);
                box.setAlignment(Pos.CENTER);

                // Next status button (context-sensitive)
                String nextStatus = switch (status) {
                    case "Processing" -> "Shipped";
                    case "Shipped"    -> "Delivered";
                    default           -> null;
                };
                if (nextStatus != null) {
                    Button advBtn = new Button("→ " + nextStatus);
                    advBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                            " -fx-font-size: 11; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                            " -fx-padding: 5 10; -fx-background-radius: 6; -fx-cursor: hand;");
                    final String ns = nextStatus;
                    advBtn.setOnAction(e -> {
                        try {
                            dbController.updateOrderStatus(o.getOrderID(), ns);
                            loadAllOrders();
                        } catch (Exception ex) {
                            showSectionError(ordersErrorLabel, "Update failed: " + ex.getMessage());
                        }
                    });
                    box.getChildren().add(advBtn);
                }

                // Cancel button (only for Processing)
                if ("Processing".equals(status)) {
                    Button cancelBtn = new Button("Cancel");
                    cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d4183d;" +
                            " -fx-font-size: 11; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                            " -fx-border-color: #fecaca; -fx-border-radius: 6;" +
                            " -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");
                    cancelBtn.setOnAction(e -> {
                        try {
                            dbController.updateOrderStatus(o.getOrderID(), "Cancelled");
                            loadAllOrders();
                        } catch (Exception ex) {
                            showSectionError(ordersErrorLabel, "Cancel failed: " + ex.getMessage());
                        }
                    });
                    box.getChildren().add(cancelBtn);
                }

                setGraphic(box.getChildren().isEmpty() ? null : box);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private String adminOrderStatusBadge(String status) {
        return switch (status) {
            case "Processing" -> "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Shipped"    -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Delivered"  -> "-fx-background-color: #030213; -fx-text-fill: white;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Returned"   -> "-fx-background-color: #ede9fe; -fx-text-fill: #5b21b6;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Cancelled"  -> "-fx-background-color: transparent; -fx-text-fill: #717182;" +
                                 " -fx-border-color: #e6e6e6; -fx-border-radius: 20;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11;";
            case "Failed"     -> "-fx-background-color: #fef2f2; -fx-text-fill: #d4183d;" +
                                 " -fx-border-color: #fecaca; -fx-border-radius: 20;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            default           -> "-fx-background-color: #f3f4f6; -fx-text-fill: #717182;" +
                                 " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11;";
        };
    }

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

        // Delivery ID
        colDelId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDeliveryId()));
        colDelId.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); return; }
                setText(id);
                setStyle("-fx-font-size: 11; -fx-text-fill: #717182; -fx-font-family: Poppins;");
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 8));
            }
        });

        // Order Ref
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

        // Address
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

        // Driver
        colDelDriver.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getAssignedDriver()));
        colDelDriver.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String driver, boolean empty) {
                super.updateItem(driver, empty);
                if (empty) { setGraphic(null); return; }
                if (driver == null || driver.isBlank()) {
                    Label unassigned = new Label("Unassigned");
                    unassigned.setStyle("-fx-font-size: 11; -fx-text-fill: #717182; -fx-font-style: italic;");
                    setGraphic(unassigned);
                } else {
                    Label lbl = new Label(driver);
                    lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #030213;");
                    setGraphic(lbl);
                }
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 8));
            }
        });

        // Status badge
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

        // Actions
        colDelActions.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDeliveryId()));
        colDelActions.setCellFactory(col -> new TableCell<>() {
            final Button advanceBtn = new Button();
            final Button assignBtn  = new Button("Assign Driver");
            final Button failBtn    = new Button("Fail");
            final HBox   box        = new HBox(4);
            {
                advanceBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                        " -fx-font-size: 11; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                        " -fx-padding: 5 10; -fx-background-radius: 6; -fx-cursor: hand;");
                assignBtn.setStyle("-fx-background-color: white; -fx-text-fill: #030213;" +
                        " -fx-font-size: 11; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                        " -fx-border-color: #e6e6e6; -fx-border-radius: 6;" +
                        " -fx-background-radius: 6; -fx-padding: 5 10; -fx-cursor: hand;");
                failBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d4183d;" +
                        " -fx-font-size: 11; -fx-font-weight: bold; -fx-font-family: Poppins;" +
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

                String status = d.getStatus();
                switch (status) {
                    case "Pending" -> {
                        // Assign driver button (if no driver assigned)
                        if (d.getAssignedDriver().isBlank()) {
                            assignBtn.setOnAction(e -> handleAssignDriver(d));
                            box.getChildren().add(assignBtn);
                        }
                        // Advance to In Transit
                        advanceBtn.setText("Start");
                        advanceBtn.setOnAction(e -> handleAdvanceStatus(d));
                        box.getChildren().add(advanceBtn);
                    }
                    case "In Transit" -> {
                        advanceBtn.setText("Out for Delivery");
                        advanceBtn.setOnAction(e -> handleAdvanceStatus(d));
                        failBtn.setOnAction(e -> handleFailDelivery(d));
                        box.getChildren().addAll(advanceBtn, failBtn);
                    }
                    case "Out for Delivery" -> {
                        advanceBtn.setText("Mark Delivered");
                        advanceBtn.setOnAction(e -> handleAdvanceStatus(d));
                        failBtn.setOnAction(e -> handleFailDelivery(d));
                        box.getChildren().addAll(advanceBtn, failBtn);
                    }
                    default -> {
                        // Delivered or Failed — no actions
                    }
                }
                setGraphic(box);
                setAlignment(Pos.CENTER);
            }
        });
    }

    private String deliveryStatusStyle(String status) {
        return switch (status) {
            case "Pending" ->
                "-fx-background-color: #f3f4f6; -fx-text-fill: #717182;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "In Transit" ->
                "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Out for Delivery" ->
                "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Delivered" ->
                "-fx-background-color: #030213; -fx-text-fill: white;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Failed" ->
                "-fx-background-color: #fef2f2; -fx-text-fill: #d4183d;" +
                " -fx-border-color: #fecaca; -fx-border-radius: 20;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            default ->
                "-fx-background-color: #f3f4f6; -fx-text-fill: #717182;" +
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

    /**
     * Starts an auto-refresh Timeline that reloads the delivery table every 10 seconds
     * while the Logistics section is visible, reflecting live timer-driven status changes.
     */
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

    /**
     * Simulates delivery progression: for each non-terminal delivery, schedules a random
     * timer (3–12 s) to advance its status one step.  Drivers are checked for conflicts.
     */
    @FXML
    private void handleSimulateDeliveries() {
        List<Delivery> snapshot = new ArrayList<>(allDeliveries);
        for (Delivery d : snapshot) {
            String s = d.getStatus();
            if ("Delivered".equals(s) || "Failed".equals(s)) continue;
            int delaySec = 3 + (int) (Math.random() * 9); // 3–11 s
            javafx.animation.Timeline t = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.seconds(delaySec),
                    e -> {
                        try {
                            dbController.advanceDeliveryStatus(d.getDeliveryId());
                        } catch (Exception ex) { /* driver conflict or terminal — skip */ }
                        javafx.application.Platform.runLater(() -> {
                            if (logisticsSection.isVisible()) loadDeliveries();
                        });
                    }
                )
            );
            t.play();
        }
    }

    @FXML
    private void handleCreateDelivery() {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) productTable.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create Delivery");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(440);

        Label title = new Label("Create Delivery");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");

        Label orderLbl = new Label("Order Reference");
        orderLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField orderField = new TextField();
        orderField.setPromptText("e.g. LUX-0001");
        orderField.setPrefHeight(38);
        orderField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8;" +
                " -fx-padding: 0 12; -fx-font-size: 13;");

        Label addrLbl = new Label("Delivery Address");
        addrLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField addrField = new TextField();
        addrField.setPromptText("Street, City, Postcode");
        addrField.setPrefHeight(38);
        addrField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8;" +
                " -fx-padding: 0 12; -fx-font-size: 13;");

        Label driverLbl = new Label("Assign Driver (optional)");
        driverLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        ComboBox<String> driverBox = new ComboBox<>();
        driverBox.setPromptText("Select driver...");
        driverBox.setMaxWidth(Double.MAX_VALUE);
        driverBox.setPrefHeight(38);
        try {
            List<String> drivers = dbController.getAvailableDrivers();
            for (String d : drivers) {
                String[] parts = d.split("\\|");
                driverBox.getItems().add(parts.length > 1 ? parts[1] : d);
            }
        } catch (Exception ignored) {}

        Label errLbl = new Label();
        errLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;" +
                " -fx-background-color: #fef2f2; -fx-background-radius: 6; -fx-padding: 8 12;");
        errLbl.setWrapText(true);
        errLbl.setVisible(false);
        errLbl.setManaged(false);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #030213;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button createBtn = new Button("Create Delivery");
        createBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");

        // Store driver IDs for lookup
        List<String> driverIds = new java.util.ArrayList<>();
        try {
            List<String> driverRaw = dbController.getAvailableDrivers();
            for (String d : driverRaw) {
                String[] parts = d.split("\\|");
                driverIds.add(parts[0]);
            }
        } catch (Exception ignored) {}

        createBtn.setOnAction(e -> {
            String orderRef = orderField.getText().trim();
            String address  = addrField.getText().trim();
            if (orderRef.isEmpty() || address.isEmpty()) {
                errLbl.setText("Order Reference and Address are required.");
                errLbl.setVisible(true); errLbl.setManaged(true);
                HoverUtils.shake(errLbl);
                return;
            }
            String driverId = null;
            int selIdx = driverBox.getSelectionModel().getSelectedIndex();
            if (selIdx >= 0 && selIdx < driverIds.size()) {
                driverId = driverIds.get(selIdx);
            }
            try {
                dbController.createDelivery(orderRef, address, driverId);
                dialog.close();
                loadDeliveries();
            } catch (Exception ex) {
                errLbl.setText("Failed to create delivery: " + ex.getMessage());
                errLbl.setVisible(true); errLbl.setManaged(true);
                HoverUtils.shake(errLbl);
            }
        });

        HBox btnRow = new HBox(10, cancelBtn, createBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(6, 0, 0, 0));

        root.getChildren().addAll(title, orderLbl, orderField, addrLbl, addrField,
                driverLbl, driverBox, errLbl, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void handleAssignDriver(Delivery delivery) {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) productTable.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Assign Driver");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(380);

        Label title = new Label("Assign Driver");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");

        Label body = new Label("Assign a driver to delivery: " + delivery.getDeliveryId() +
                "\nOrder: " + delivery.getOrderRef());
        body.setStyle("-fx-font-size: 13; -fx-text-fill: #717182;");
        body.setWrapText(true);

        Label driverLbl = new Label("Select Driver");
        driverLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        ComboBox<String> driverBox = new ComboBox<>();
        driverBox.setMaxWidth(Double.MAX_VALUE);
        driverBox.setPrefHeight(38);

        List<String> driverIds = new java.util.ArrayList<>();
        try {
            List<String> drivers = dbController.getAvailableDrivers();
            for (String d : drivers) {
                String[] parts = d.split("\\|");
                driverIds.add(parts[0]);
                driverBox.getItems().add(parts.length > 1 ? parts[1] : d);
            }
        } catch (Exception ignored) {}

        Label errLbl = new Label();
        errLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;");
        errLbl.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #030213;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button assignBtn = new Button("Assign");
        assignBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        assignBtn.setOnAction(e -> {
            int selIdx = driverBox.getSelectionModel().getSelectedIndex();
            if (selIdx < 0) {
                errLbl.setText("Please select a driver.");
                return;
            }
            try {
                dbController.assignDeliveryDriver(delivery.getDeliveryId(), driverIds.get(selIdx));
                dialog.close();
                loadDeliveries();
            } catch (Exception ex) {
                errLbl.setText("Failed: " + ex.getMessage());
            }
        });

        HBox btnRow = new HBox(10, cancelBtn, assignBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, body, driverLbl, driverBox, errLbl, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void handleAdvanceStatus(Delivery delivery) {
        try {
            boolean advanced = dbController.advanceDeliveryStatus(delivery.getDeliveryId());
            if (advanced) loadDeliveries();
            else showSectionError(logisticsErrorLabel, "Cannot advance: delivery is already in a terminal state.");
        } catch (Exception ex) {
            showSectionError(logisticsErrorLabel, "Failed to advance delivery: " + ex.getMessage());
        }
    }

    private void handleFailDelivery(Delivery delivery) {
        new ConfirmDialog(
            (Stage) productTable.getScene().getWindow(),
            "Mark as Failed",
            "Mark delivery " + delivery.getDeliveryId() + " as failed?\nThis cannot be undone.",
            "Mark Failed",
            () -> dbController.failDelivery(delivery.getDeliveryId()),
            this::loadDeliveries
        ).show();
    }

    // ── Inventory Section ──────────────────────────────────────────────────────

    private void setupInventorySection() {
        // Tab button handlers
        btnWarehousesTab.setOnAction(e -> showInventoryTab("warehouses"));
        btnStockTab.setOnAction(e -> showInventoryTab("stock"));
        btnReportsTab.setOnAction(e -> showInventoryTab("reports"));
        btnAddWarehouse.setOnAction(e -> showAddWarehouseDialog(null));
        btnAddStock.setOnAction(e -> showAddStockDialog());

        // Warehouses table
        warehousesTable.getStylesheets().add(getClass().getResource("/dashboard-table.css").toExternalForm());
        warehousesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final String RN = "-fx-background-color: white; -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String RH = "-fx-background-color: #f0f4f8; -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        warehousesTable.setRowFactory(tv -> { TableRow<Warehouse> row = new TableRow<>();
            row.itemProperty().addListener((o,x,i) -> row.setStyle(i==null?"":RN));
            row.hoverProperty().addListener((o,x,h) -> { if(!row.isEmpty()) row.setStyle(h?RH:RN); }); return row; });
        colWhName.setCellValueFactory(c -> c.getValue().nameProperty());
        colWhLocation.setCellValueFactory(c -> c.getValue().locationProperty());
        colWhContact.setCellValueFactory(c -> c.getValue().contactInfoProperty());
        colWhCapacity.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCapacityDisplay()));
        colWhCapacity.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(v); setAlignment(javafx.geometry.Pos.CENTER); setStyle("-fx-font-size: 13; -fx-text-fill: #030213;");
            }
        });
        colWhUsed.setCellValueFactory(c -> {
            // Compute used from stock
            return new javafx.beans.property.SimpleStringProperty("—");
        });
        colWhActions.setCellValueFactory(c -> c.getValue().nameProperty());
        colWhActions.setCellFactory(col -> new TableCell<>() {
            final Button editBtn = makeOutlineBtn(); final Button delBtn = makeOutlineBtn();
            final HBox box = new HBox(4, editBtn, delBtn); { box.setAlignment(javafx.geometry.Pos.CENTER);
                editBtn.setGraphic(makeSvgIcon(EDIT_PATH, "#717182")); delBtn.setGraphic(makeSvgIcon(TRASH_PATH, "#d4183d")); }
            @Override protected void updateItem(String d, boolean empty) {
                super.updateItem(d, empty); if (empty) { setGraphic(null); return; }
                int idx = getIndex(); if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                Warehouse wh = getTableView().getItems().get(idx);
                editBtn.setOnAction(e -> showAddWarehouseDialog(wh));
                delBtn.setOnAction(e -> new ConfirmDialog((Stage) warehousesTable.getScene().getWindow(),
                    "Delete Warehouse", "Delete warehouse \"" + wh.getName() + "\"? All stock must be removed first.", "Delete",
                    () -> dbController.deleteWarehouse(wh.getWarehouseID()), () -> { loadWarehouses(); loadAllStock(); }).show());
                setGraphic(box); setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        // Stock table
        stockTable.getStylesheets().add(getClass().getResource("/dashboard-table.css").toExternalForm());
        stockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        stockTable.setRowFactory(tv -> { TableRow<WarehouseStock> row = new TableRow<>();
            row.itemProperty().addListener((o,x,i) -> row.setStyle(i==null?"":RN));
            row.hoverProperty().addListener((o,x,h) -> { if(!row.isEmpty()) row.setStyle(h?RH:RN); }); return row; });
        colStProduct.setCellValueFactory(c -> c.getValue().productNameProperty());
        colStWarehouse.setCellValueFactory(c -> c.getValue().warehouseNameProperty());
        colStQty.setCellValueFactory(c -> c.getValue().quantityProperty().asObject());
        colStQty.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty); if (empty || qty == null) { setText(null); setStyle(""); return; }
                int idx = getIndex(); if (idx < 0 || idx >= getTableView().getItems().size()) { setText(null); return; }
                WarehouseStock ws = getTableView().getItems().get(idx);
                setText(String.valueOf(qty)); setAlignment(javafx.geometry.Pos.CENTER);
                setStyle(ws.isLowStock() ? "-fx-font-size:13;-fx-text-fill:#d4183d;-fx-font-weight:bold;" : "-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });
        colStMin.setCellValueFactory(c -> c.getValue().minThresholdProperty().asObject());
        colStMin.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(String.valueOf(v)); setAlignment(javafx.geometry.Pos.CENTER); setStyle("-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });
        colStRestock.setCellValueFactory(c -> c.getValue().lastRestockDateProperty());
        colStActions.setCellValueFactory(c -> c.getValue().productNameProperty());
        colStActions.setCellFactory(col -> new TableCell<>() {
            final Button restockBtn = new Button("Restock"); final Button transferBtn = new Button("Transfer"); final Button removeBtn = makeOutlineBtn();
            final HBox box = new HBox(4, restockBtn, transferBtn, removeBtn);
            { box.setAlignment(javafx.geometry.Pos.CENTER);
              restockBtn.setStyle("-fx-background-color:#030213;-fx-text-fill:white;-fx-font-size:11;-fx-font-weight:bold;-fx-padding:5 10;-fx-background-radius:6;-fx-cursor:hand;");
              transferBtn.setStyle("-fx-background-color:white;-fx-text-fill:#030213;-fx-font-size:11;-fx-font-weight:bold;-fx-border-color:#e6e6e6;-fx-border-radius:6;-fx-background-radius:6;-fx-padding:5 10;-fx-cursor:hand;");
              removeBtn.setGraphic(makeSvgIcon(TRASH_PATH, "#d4183d")); }
            @Override protected void updateItem(String d, boolean empty) {
                super.updateItem(d, empty); if (empty) { setGraphic(null); return; }
                int idx = getIndex(); if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                WarehouseStock ws = getTableView().getItems().get(idx);
                restockBtn.setOnAction(e -> showRestockDialog(ws));
                transferBtn.setOnAction(e -> showTransferDialog(ws));
                removeBtn.setOnAction(e -> new ConfirmDialog((Stage) stockTable.getScene().getWindow(),
                    "Remove Stock", "Remove stock record for \"" + ws.getProductName() + "\" from \"" + ws.getWarehouseName() + "\"?", "Remove",
                    () -> dbController.removeStockRecord(ws.getStockID()), () -> reloadStock()).show());
                setGraphic(box); setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        // Stock filters
        stockSearchField.textProperty().addListener((o, x, n) -> applyStockFilters());
        stockWarehouseFilter.valueProperty().addListener((o, x, n) -> applyStockFilters());
        lowStockOnlyCheck.selectedProperty().addListener((o, x, n) -> applyStockFilters());

        // Reports tables
        totalStockTable.getStylesheets().add(getClass().getResource("/dashboard-table.css").toExternalForm());
        totalStockTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        totalStockTable.setItems(totalStockData);
        colRptProduct.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[1]));
        colRptCategory.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[2]));
        colRptWHQty.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[3]));
        colRptWHQty.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(v); setAlignment(javafx.geometry.Pos.CENTER); setStyle("-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });
        colRptCatQty.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[5]));
        colRptCatQty.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(v); setAlignment(javafx.geometry.Pos.CENTER); setStyle("-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });
        colRptWHCount.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[4]));
        colRptWHCount.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(v); setAlignment(javafx.geometry.Pos.CENTER); setStyle("-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });

        capacityTable.getStylesheets().add(getClass().getResource("/dashboard-table.css").toExternalForm());
        capacityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        capacityTable.setItems(capacityData);
        colCapName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[0]));
        colCapLocation.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[1]));
        colCapTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[2]));
        colCapTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(v); setAlignment(javafx.geometry.Pos.CENTER); setStyle("-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });
        colCapUsed.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[3]));
        colCapUsed.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(v); setAlignment(javafx.geometry.Pos.CENTER); setStyle("-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });
        colCapUtil.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue()[4]));
        colCapUtil.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(v); setAlignment(javafx.geometry.Pos.CENTER); setStyle("-fx-font-size:13;-fx-text-fill:#030213;");
            }
        });
    }

    private void showInventoryTab(String tab) {
        warehousesSubView.setVisible(false); warehousesSubView.setManaged(false);
        stockSubView.setVisible(false);      stockSubView.setManaged(false);
        reportsSubView.setVisible(false);    reportsSubView.setManaged(false);
        String activeStyle = "-fx-background-color: #030213; -fx-text-fill: white; -fx-padding: 8 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 12; -fx-font-weight: bold;";
        String inactiveStyle = "-fx-background-color: white; -fx-text-fill: #717182; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-size: 12; -fx-font-weight: bold;";
        btnWarehousesTab.setStyle(inactiveStyle); btnStockTab.setStyle(inactiveStyle); btnReportsTab.setStyle(inactiveStyle);
        switch (tab) {
            case "warehouses" -> { warehousesSubView.setVisible(true); warehousesSubView.setManaged(true); btnWarehousesTab.setStyle(activeStyle); loadWarehouses(); }
            case "stock"      -> { stockSubView.setVisible(true); stockSubView.setManaged(true); btnStockTab.setStyle(activeStyle); loadAllStock(); }
            case "reports"    -> { reportsSubView.setVisible(true); reportsSubView.setManaged(true); btnReportsTab.setStyle(activeStyle); loadStockReports(); }
        }
    }

    private void loadWarehouses() {
        try {
            clearSectionError(inventoryErrorLabel);
            List<Warehouse> warehouses = dbController.getAllWarehouses();
            allWarehouses.setAll(warehouses);
            warehousesTable.setItems(allWarehouses);
            // Refresh warehouse filter in stock view
            String prev = stockWarehouseFilter.getValue();
            stockWarehouseFilter.getItems().clear();
            stockWarehouseFilter.getItems().add("All Warehouses");
            for (Warehouse wh : warehouses) stockWarehouseFilter.getItems().add(wh.getName());
            if (prev != null && stockWarehouseFilter.getItems().contains(prev)) stockWarehouseFilter.setValue(prev);
            else stockWarehouseFilter.setValue("All Warehouses");
        } catch (Exception e) {
            showSectionError(inventoryErrorLabel, "Failed to load warehouses: " + e.getMessage());
        }
    }

    private void loadAllStock() {
        try {
            clearSectionError(inventoryErrorLabel);
            List<WarehouseStock> stock = dbController.getAllStock();
            allStock.setAll(stock);
            applyStockFilters();
        } catch (Exception e) {
            showSectionError(inventoryErrorLabel, "Failed to load stock: " + e.getMessage());
        }
    }

    private void applyStockFilters() {
        String search = stockSearchField.getText() == null ? "" : stockSearchField.getText().trim().toLowerCase();
        String whFilter = stockWarehouseFilter.getValue();
        boolean lowOnly = lowStockOnlyCheck.isSelected();
        filteredStock.setAll(allStock.stream().filter(ws -> {
            boolean ms = search.isEmpty() || ws.getProductName().toLowerCase().contains(search) || ws.getWarehouseName().toLowerCase().contains(search);
            boolean mw = whFilter == null || "All Warehouses".equals(whFilter) || ws.getWarehouseName().equals(whFilter);
            boolean ml = !lowOnly || ws.isLowStock();
            return ms && mw && ml;
        }).toList());
        stockTable.setItems(filteredStock);
    }

    private void reloadStock() { loadAllStock(); }

    private void loadStockReports() {
        try {
            clearSectionError(inventoryErrorLabel);
            totalStockData.setAll(dbController.getTotalStockReport());
            capacityData.setAll(dbController.getWarehouseCapacityReport());
        } catch (Exception e) {
            showSectionError(inventoryErrorLabel, "Failed to load reports: " + e.getMessage());
        }
    }

    private void showAddWarehouseDialog(Warehouse existing) {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) productTable.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "Add Warehouse" : "Edit Warehouse");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(420);

        Label title = new Label(existing == null ? "Add Warehouse" : "Edit Warehouse");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");

        Label nameLbl = new Label("Name *"); nameLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField nameField = new TextField(existing == null ? "" : existing.getName());
        nameField.setPrefHeight(38); nameField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label locLbl = new Label("Location"); locLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField locField = new TextField(existing == null ? "" : existing.getLocation());
        locField.setPrefHeight(38); locField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label contactLbl = new Label("Contact Info"); contactLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField contactField = new TextField(existing == null ? "" : existing.getContactInfo());
        contactField.setPrefHeight(38); contactField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label capLbl = new Label("Capacity (-1 = Unlimited)"); capLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField capField = new TextField(existing == null ? "-1" : String.valueOf(existing.getCapacity()));
        capField.setPrefHeight(38); capField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label errLbl = new Label(); errLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;"); errLbl.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #030213; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button(existing == null ? "Add Warehouse" : "Save Changes");
        saveBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String loc = locField.getText().trim();
            String contact = contactField.getText().trim();
            int cap;
            try { cap = Integer.parseInt(capField.getText().trim()); }
            catch (NumberFormatException ex) { errLbl.setText("Capacity must be an integer (-1 for unlimited)."); HoverUtils.shake(errLbl); return; }
            try {
                if (existing == null) dbController.createWarehouse(name, loc, contact, cap);
                else dbController.updateWarehouse(existing.getWarehouseID(), name, loc, contact, cap);
                dialog.close(); loadWarehouses(); loadAllStock();
            } catch (Exception ex) { errLbl.setText(ex.getMessage()); HoverUtils.shake(errLbl); }
        });

        HBox btnRow = new HBox(10, cancelBtn, saveBtn); btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT); btnRow.setPadding(new Insets(6,0,0,0));
        root.getChildren().addAll(title, nameLbl, nameField, locLbl, locField, contactLbl, contactField, capLbl, capField, errLbl, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void showAddStockDialog() {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) productTable.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Assign Stock to Warehouse");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(440);

        Label title = new Label("Assign Stock to Warehouse");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");

        Label whLbl = new Label("Warehouse *"); whLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        ComboBox<String> whBox = new ComboBox<>(); whBox.setMaxWidth(Double.MAX_VALUE); whBox.setPrefHeight(38);
        List<Warehouse> whs = new ArrayList<>();
        try { whs.addAll(dbController.getAllWarehouses()); for (Warehouse wh : whs) whBox.getItems().add(wh.getName()); } catch (Exception ignored) {}

        Label prodLbl = new Label("Product *"); prodLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        ComboBox<String> prodBox = new ComboBox<>(); prodBox.setMaxWidth(Double.MAX_VALUE); prodBox.setPrefHeight(38);
        List<Product> prods = new ArrayList<>();
        try { prods.addAll(dbController.getAllProducts()); for (Product p : prods) prodBox.getItems().add(p.getName() + " (ID:" + p.getProductID() + ")"); } catch (Exception ignored) {}

        Label qtyLbl = new Label("Initial Quantity"); qtyLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField qtyField = new TextField("0"); qtyField.setPrefHeight(38);
        qtyField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label minLbl = new Label("Min Threshold"); minLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField minField = new TextField("0"); minField.setPrefHeight(38);
        minField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label reorderLbl = new Label("Reorder Quantity"); reorderLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField reorderField = new TextField("0"); reorderField.setPrefHeight(38);
        reorderField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label errLbl = new Label(); errLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;"); errLbl.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #030213; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());
        Button saveBtn = new Button("Assign Stock");
        saveBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        final List<Warehouse> finalWhs = whs; final List<Product> finalProds = prods;
        saveBtn.setOnAction(e -> {
            int wi = whBox.getSelectionModel().getSelectedIndex();
            int pi = prodBox.getSelectionModel().getSelectedIndex();
            if (wi < 0 || pi < 0) { errLbl.setText("Please select a warehouse and product."); HoverUtils.shake(errLbl); return; }
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                int min = Integer.parseInt(minField.getText().trim());
                int reorder = Integer.parseInt(reorderField.getText().trim());
                dbController.assignStockToWarehouse(finalWhs.get(wi).getWarehouseID(), finalProds.get(pi).getProductID(), qty, min, reorder);
                dialog.close(); loadAllStock();
            } catch (NumberFormatException ex) { errLbl.setText("Quantity, threshold, and reorder must be integers."); HoverUtils.shake(errLbl);
            } catch (Exception ex) { errLbl.setText(ex.getMessage()); HoverUtils.shake(errLbl); }
        });

        HBox btnRow = new HBox(10, cancelBtn, saveBtn); btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT); btnRow.setPadding(new Insets(6,0,0,0));
        root.getChildren().addAll(title, whLbl, whBox, prodLbl, prodBox, qtyLbl, qtyField, minLbl, minField, reorderLbl, reorderField, errLbl, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void showRestockDialog(WarehouseStock ws) {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) productTable.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
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
                dialog.close(); loadAllStock();
            } catch (NumberFormatException ex) { errLbl.setText("Please enter a valid positive integer."); HoverUtils.shake(errLbl);
            } catch (Exception ex) { errLbl.setText(ex.getMessage()); HoverUtils.shake(errLbl); }
        });

        HBox btnRow = new HBox(10, cancelBtn, saveBtn); btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT); btnRow.setPadding(new Insets(6,0,0,0));
        root.getChildren().addAll(title, info, qtyLbl, qtyField, errLbl, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    private void showTransferDialog(WarehouseStock ws) {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) productTable.getScene().getWindow());
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Transfer Stock");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(400);

        Label title = new Label("Transfer Stock");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");
        Label info = new Label(ws.getProductName() + " from " + ws.getWarehouseName() + "\nAvailable: " + ws.getQuantity());
        info.setStyle("-fx-font-size: 13; -fx-text-fill: #717182;"); info.setWrapText(true);

        Label destLbl = new Label("Destination Warehouse *"); destLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        ComboBox<String> destBox = new ComboBox<>(); destBox.setMaxWidth(Double.MAX_VALUE); destBox.setPrefHeight(38);
        List<Warehouse> destWhs = new ArrayList<>();
        try {
            for (Warehouse wh : dbController.getAllWarehouses()) {
                if (wh.getWarehouseID() != ws.getWarehouseID()) { destWhs.add(wh); destBox.getItems().add(wh.getName()); }
            }
        } catch (Exception ignored) {}

        Label qtyLbl = new Label("Transfer Quantity *"); qtyLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField qtyField = new TextField("1"); qtyField.setPrefHeight(38);
        qtyField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label errLbl = new Label(); errLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;"); errLbl.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #030213; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());
        Button saveBtn = new Button("Transfer");
        saveBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white; -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        final List<Warehouse> finalDestWhs = destWhs;
        saveBtn.setOnAction(e -> {
            int di = destBox.getSelectionModel().getSelectedIndex();
            if (di < 0) { errLbl.setText("Please select a destination warehouse."); HoverUtils.shake(errLbl); return; }
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                dbController.transferStock(ws.getStockID(), finalDestWhs.get(di).getWarehouseID(), qty);
                dialog.close(); loadAllStock();
            } catch (NumberFormatException ex) { errLbl.setText("Please enter a valid positive integer."); HoverUtils.shake(errLbl);
            } catch (Exception ex) { errLbl.setText(ex.getMessage()); HoverUtils.shake(errLbl); }
        });

        HBox btnRow = new HBox(10, cancelBtn, saveBtn); btnRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT); btnRow.setPadding(new Insets(6,0,0,0));
        root.getChildren().addAll(title, info, destLbl, destBox, qtyLbl, qtyField, errLbl, btnRow);
        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    // ── Admin Reviews Section ──────────────────────────────────────────────────

    private void setupAdminReviewsSection() {
        adminReviewsTable.getStylesheets().add(getClass().getResource("/dashboard-table.css").toExternalForm());
        adminReviewsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        final String RN = "-fx-background-color: white; -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String RH = "-fx-background-color: #f0f4f8; -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        adminReviewsTable.setRowFactory(tv -> { TableRow<Review> row = new TableRow<>();
            row.itemProperty().addListener((o,x,i) -> row.setStyle(i==null?"":RN));
            row.hoverProperty().addListener((o,x,h) -> { if(!row.isEmpty()) row.setStyle(h?RH:RN); }); return row; });

        colRevProduct.setCellValueFactory(c -> c.getValue().productNameProperty());
        colRevCustomer.setCellValueFactory(c -> c.getValue().customerNameProperty());
        colRevRating.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().starsDisplay()));
        colRevRating.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                setText(v); setAlignment(javafx.geometry.Pos.CENTER); setStyle("-fx-font-size: 12; -fx-text-fill: #d97706;");
            }
        });
        colRevComment.setCellValueFactory(c -> c.getValue().commentProperty());
        colRevComment.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setText(null); return; }
                String truncated = v.length() > 60 ? v.substring(0, 57) + "..." : v;
                setText(truncated); setStyle("-fx-font-size: 12; -fx-text-fill: #030213;");
            }
        });
        colRevDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getCreatedAt().length() > 10 ? c.getValue().getCreatedAt().substring(0, 10) : c.getValue().getCreatedAt()));
        colRevFlagged.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().isFlagged() ? "Yes" : "No"));
        colRevFlagged.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty); if (empty || v == null) { setGraphic(null); return; }
                Label badge = new Label(v);
                if ("Yes".equals(v)) badge.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #d4183d; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;");
                else badge.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #717182; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 11;");
                setGraphic(badge); setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        colRevActions.setCellValueFactory(c -> c.getValue().productNameProperty());
        colRevActions.setCellFactory(col -> new TableCell<>() {
            final Button flagBtn = new Button(); final Button deleteBtn = makeOutlineBtn();
            final HBox box = new HBox(4, flagBtn, deleteBtn); { box.setAlignment(javafx.geometry.Pos.CENTER);
              deleteBtn.setGraphic(makeSvgIcon(TRASH_PATH, "#d4183d")); }
            @Override protected void updateItem(String d, boolean empty) {
                super.updateItem(d, empty); if (empty) { setGraphic(null); return; }
                int idx = getIndex(); if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                Review r = getTableView().getItems().get(idx);
                if (r.isFlagged()) {
                    flagBtn.setText("Unflag");
                    flagBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #717182; -fx-font-size: 11; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 6; -fx-cursor: hand;");
                    flagBtn.setOnAction(e -> { try { dbController.unflagReview(r.getReviewID()); loadAdminReviews(); } catch (Exception ex) { showSectionError(adminReviewsErrorLabel, ex.getMessage()); } });
                } else {
                    flagBtn.setText("Flag");
                    flagBtn.setStyle("-fx-background-color: #fef9c3; -fx-text-fill: #854d0e; -fx-font-size: 11; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 6; -fx-cursor: hand;");
                    flagBtn.setOnAction(e -> { try { dbController.flagReview(r.getReviewID()); loadAdminReviews(); } catch (Exception ex) { showSectionError(adminReviewsErrorLabel, ex.getMessage()); } });
                }
                deleteBtn.setOnAction(e -> new ConfirmDialog((Stage) adminReviewsTable.getScene().getWindow(),
                    "Delete Review", "Permanently delete this review?", "Delete",
                    () -> dbController.adminDeleteReview(r.getReviewID()), () -> loadAdminReviews()).show());
                setGraphic(box); setAlignment(javafx.geometry.Pos.CENTER);
            }
        });

        reviewRatingFilter.getItems().addAll("All Ratings", "5★", "4★", "3★", "2★", "1★");
        reviewRatingFilter.setValue("All Ratings");
        reviewSearchField.textProperty().addListener((o, x, n) -> applyAdminReviewFilters());
        reviewRatingFilter.valueProperty().addListener((o, x, n) -> applyAdminReviewFilters());
        flaggedOnlyCheck.selectedProperty().addListener((o, x, n) -> applyAdminReviewFilters());
    }

    private void loadAdminReviews() {
        try {
            clearSectionError(adminReviewsErrorLabel);
            allAdminReviews.setAll(dbController.getAllReviews());
            applyAdminReviewFilters();
        } catch (Exception e) {
            showSectionError(adminReviewsErrorLabel, "Failed to load reviews: " + e.getMessage());
        }
    }

    private void applyAdminReviewFilters() {
        String search = reviewSearchField.getText() == null ? "" : reviewSearchField.getText().trim().toLowerCase();
        String ratingFilter = reviewRatingFilter.getValue();
        boolean flaggedOnly = flaggedOnlyCheck.isSelected();
        filteredAdminReviews.setAll(allAdminReviews.stream().filter(r -> {
            boolean ms = search.isEmpty() || r.getProductName().toLowerCase().contains(search) || r.getCustomerName().toLowerCase().contains(search) || r.getCustomerEmail().toLowerCase().contains(search);
            boolean mr = ratingFilter == null || "All Ratings".equals(ratingFilter) || ratingFilter.startsWith(String.valueOf(r.getRating()));
            boolean mf = !flaggedOnly || r.isFlagged();
            return ms && mr && mf;
        }).toList());
        adminReviewsTable.setItems(filteredAdminReviews);
    }

    // ── ConfirmDialog inner class ──────────────────────────────────────────────

    /**
     * Reusable styled in-app confirmation dialog.
     *
     * <p>Applied OOP concepts demonstrated by this class:</p>
     * <ul>
     *   <li><b>Encapsulation</b>  — all internal state (Stage, Labels, Buttons) is
     *       private; only the {@code show()} method forms the public API.</li>
     *   <li><b>Abstraction</b>    — callers supply a {@link ThrowingRunnable} action
     *       without knowing how the dialog is built or displayed.</li>
     *   <li><b>User-defined type</b> — {@code ThrowingRunnable} is a custom
     *       functional interface that extends the Runnable concept to allow
     *       checked exceptions inside JavaFX event-handler lambdas.</li>
     *   <li><b>Single responsibility</b> — this class exclusively manages
     *       confirmation UI; all business logic lives in the supplied lambdas.</li>
     * </ul>
     */
    private static final class ConfirmDialog {

        /**
         * Functional interface for actions that may throw a checked exception.
         * Standard {@link Runnable} cannot declare checked exceptions, so this
         * user-defined type bridges that gap inside JavaFX event handlers.
         */
        @FunctionalInterface
        interface ThrowingRunnable {
            void run() throws Exception;
        }

        // Encapsulated fields — not accessible outside this class
        private final Stage            owner;
        private final String           heading;
        private final String           message;
        private final String           confirmLabel;
        private final ThrowingRunnable onConfirm;
        private final Runnable         onSuccess;

        /**
         * Constructs a new confirmation dialog.
         *
         * @param owner        the owning window (used for modality)
         * @param heading      title and header text for the dialog
         * @param message      descriptive body text shown to the user
         * @param confirmLabel text for the destructive/confirm button
         * @param onConfirm    action executed on confirmation — may throw a checked exception
         * @param onSuccess    callback invoked after a successful confirmation
         */
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

            // Inline error label — shown when onConfirm throws; no native Alert pop-up
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
                    onConfirm.run();   // execute the supplied action (e.g. DB delete)
                    dialog.close();
                    onSuccess.run();   // e.g. reload the table
                } catch (Exception ex) {
                    // Show the error inline — dialog stays open so the user sees it
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
}
