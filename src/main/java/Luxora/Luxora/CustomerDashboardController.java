package Luxora.Luxora;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CustomerDashboardController {

    // ── Root ───────────────────────────────────────────────────────────────────
    @FXML private javafx.scene.layout.BorderPane rootPane;

    // ── Sidebar ────────────────────────────────────────────────────────────────
    @FXML private HBox  navBrowse, navCart, navOrders, navProfile, navReviews;
    @FXML private Label navBrowseLabel, navCartLabel, navOrdersLabel, navProfileLabel, navReviewsLabel;
    @FXML private Label cartBadge;

    // ── Sections ──────────────────────────────────────────────────────────────
    @FXML private StackPane mainStack;
    @FXML private VBox browseSection, productDetailSection, cartSection, ordersSection, profileSection, reviewsSection;

    // ── Customer Reviews ──────────────────────────────────────────────────────
    @FXML private TableView<Review>           custReviewsTable;
    @FXML private TableColumn<Review, String> colCustRevProduct, colCustRevRating, colCustRevComment, colCustRevDate, colCustRevHelpful, colCustRevActions;
    @FXML private TextField  custReviewSearchField;
    @FXML private ComboBox<String> custReviewRatingFilter;
    @FXML private Label reviewsErrorLabel;
    @FXML private Button btnSubmitReview;
    private final ObservableList<Review> allCustReviews      = FXCollections.observableArrayList();
    private final ObservableList<Review> filteredCustReviews = FXCollections.observableArrayList();

    // ── Orders ────────────────────────────────────────────────────────────────
    @FXML private TableView<Order>             ordersTable;
    @FXML private TableColumn<Order, String>   colOrderCode;
    @FXML private TableColumn<Order, String>   colOrderDate;
    @FXML private TableColumn<Order, Integer>  colOrderItems;
    @FXML private TableColumn<Order, Double>   colOrderTotal;
    @FXML private TableColumn<Order, String>   colOrderStatus;
    @FXML private TableColumn<Order, String>   colOrderActions;
    @FXML private Label                        orderCountLabel;
    @FXML private ComboBox<String>             orderStatusFilter;

    private final javafx.collections.ObservableList<Order> allOrders =
            FXCollections.observableArrayList();

    // ── Browse ────────────────────────────────────────────────────────────────
    @FXML private TextField        searchField, minPriceField, maxPriceField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label            productCountLabel;

    @FXML private TableView<Product>            productTable;
    @FXML private TableColumn<Product, String>  colImage;
    @FXML private TableColumn<Product, String>  colName;
    @FXML private TableColumn<Product, String>  colCategory;
    @FXML private TableColumn<Product, Double>  colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String>  colCartBtn;

    // ── Product detail ────────────────────────────────────────────────────────
    @FXML private Label     detailTitleLabel, detailNameLabel, detailPriceLabel;
    @FXML private Label     detailStockLabel, detailDescLabel;
    @FXML private FlowPane  detailCategoriesPane;
    @FXML private HBox      imageStrip;
    @FXML private Button    detailCartBtn;

    // ── Cart ──────────────────────────────────────────────────────────────────
    @FXML private VBox   cartItemsBox;
    @FXML private Label  cartCountLabel, cartTotalLabel;
    @FXML private HBox   cartFooter;
    @FXML private Button clearCartBtn;

    // ── Profile ───────────────────────────────────────────────────────────────
    @FXML private Label profileNameLabel, profileEmailLabel;

    private final ObservableList<Product> allProducts      = FXCollections.observableArrayList();
    private final ObservableList<Product> filteredProducts = FXCollections.observableArrayList();

    // Nav style constants — matching admin/manager
    private static final String NAV_BOX_ACTIVE   =
            "-fx-background-color: #ececf0; -fx-background-radius: 8; -fx-padding: 0 12; -fx-cursor: hand; " +
            "-fx-border-color: #030213 transparent transparent transparent; -fx-border-width: 0 0 0 3;";
    private static final String NAV_BOX_INACTIVE =
            "-fx-background-color: transparent; -fx-background-radius: 8; -fx-padding: 0 15; -fx-cursor: hand;";
    private static final String NAV_LBL_ACTIVE   =
            "-fx-font-size: 13; -fx-text-fill: #030213; -fx-font-weight: bold; -fx-font-family: Poppins;";
    private static final String NAV_LBL_INACTIVE =
            "-fx-font-size: 13; -fx-text-fill: #717182; -fx-font-weight: bold; -fx-font-family: Poppins;";

    // ── Initialisation ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        profileNameLabel.setText(UserSession.getName());
        profileEmailLabel.setText(UserSession.getEmail());

        navBrowse.setOnMouseClicked(e -> showSection("browse"));
        navCart.setOnMouseClicked(e -> { loadCart(); showSection("cart"); });
        navOrders.setOnMouseClicked(e -> { loadOrders(); showSection("orders"); });
        navProfile.setOnMouseClicked(e -> showSection("profile"));
        navReviews.setOnMouseClicked(e -> { loadCustomerReviews(); showSection("reviews"); });

        searchField.textProperty().addListener((o, old, nw) -> applyFilters());
        categoryFilter.valueProperty().addListener((o, old, nw) -> applyFilters());
        minPriceField.textProperty().addListener((o, old, nw) -> applyFilters());
        maxPriceField.textProperty().addListener((o, old, nw) -> applyFilters());

        // Order status filter
        orderStatusFilter.setItems(FXCollections.observableArrayList(
                "All Statuses", "Processing", "Shipped", "Delivered", "Cancelled", "Returned", "Failed"));
        orderStatusFilter.setValue("All Statuses");
        orderStatusFilter.valueProperty().addListener((o, old, nw) -> loadOrders());

        setupTableColumns();
        setupOrdersTable();
        loadProducts();
        updateCartBadge();

        // Nav icons
        SVGPath browseIcon = makeNavIcon(
            "M3 3h7v7H3V3zm11 0h7v7h-7V3zM3 14h7v7H3v-7zm11 0h7v7h-7v-7z", false);
        navBrowse.getChildren().add(0, browseIcon);
        HBox.setMargin(browseIcon, new Insets(0, 8, 0, 0));

        SVGPath cartIcon = makeNavIcon(
            "M1 1h4l2.68 13.39A2 2 0 0 0 9.64 16h9.72a2 2 0 0 0 1.96-1.61L23 6H6" +
            "M9 21a1 1 0 1 0 2 0 1 1 0 0 0-2 0zm10 0a1 1 0 1 0 2 0 1 1 0 0 0-2 0z", false);
        navCart.getChildren().add(0, cartIcon);
        HBox.setMargin(cartIcon, new Insets(0, 8, 0, 0));

        SVGPath ordersIcon = makeNavIcon(
            "M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" +
            "M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2" +
            "M9 12h6M9 16h4", false);
        navOrders.getChildren().add(0, ordersIcon);
        HBox.setMargin(ordersIcon, new Insets(0, 8, 0, 0));

        SVGPath reviewsIcon = makeNavIcon(
            "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z", false);
        navReviews.getChildren().add(0, reviewsIcon);
        HBox.setMargin(reviewsIcon, new Insets(0, 8, 0, 0));

        SVGPath profIcon = makeNavIcon(
            "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z", false);
        navProfile.getChildren().add(0, profIcon);
        HBox.setMargin(profIcon, new Insets(0, 8, 0, 0));

        setupCustomerReviewsSection();
        showSection("browse");
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

    private void showSection(String name) {
        browseSection.setVisible(false);        browseSection.setManaged(false);
        productDetailSection.setVisible(false); productDetailSection.setManaged(false);
        cartSection.setVisible(false);          cartSection.setManaged(false);
        ordersSection.setVisible(false);        ordersSection.setManaged(false);
        profileSection.setVisible(false);       profileSection.setManaged(false);
        reviewsSection.setVisible(false);       reviewsSection.setManaged(false);

        setNavActive(navBrowse,  navBrowseLabel,  false);
        setNavActive(navCart,    navCartLabel,    false);
        setNavActive(navOrders,  navOrdersLabel,  false);
        setNavActive(navProfile, navProfileLabel, false);
        setNavActive(navReviews, navReviewsLabel, false);

        switch (name) {
            case "browse" -> {
                browseSection.setVisible(true); browseSection.setManaged(true);
                setNavActive(navBrowse, navBrowseLabel, true);
            }
            case "detail" -> {
                productDetailSection.setVisible(true); productDetailSection.setManaged(true);
                setNavActive(navBrowse, navBrowseLabel, true);
            }
            case "cart" -> {
                cartSection.setVisible(true); cartSection.setManaged(true);
                setNavActive(navCart, navCartLabel, true);
            }
            case "orders" -> {
                ordersSection.setVisible(true); ordersSection.setManaged(true);
                setNavActive(navOrders, navOrdersLabel, true);
            }
            case "profile" -> {
                profileSection.setVisible(true); profileSection.setManaged(true);
                setNavActive(navProfile, navProfileLabel, true);
            }
            case "reviews" -> {
                reviewsSection.setVisible(true); reviewsSection.setManaged(true);
                setNavActive(navReviews, navReviewsLabel, true);
            }
        }
    }

    private void setNavActive(HBox hbox, Label label, boolean active) {
        hbox.setStyle(active ? NAV_BOX_ACTIVE : NAV_BOX_INACTIVE);
        label.setStyle(active ? NAV_LBL_ACTIVE : NAV_LBL_INACTIVE);
        hbox.getChildren().stream()
            .filter(n -> n instanceof SVGPath)
            .forEach(n -> ((SVGPath) n).setStroke(Color.web(active ? "#030213" : "#717182")));
    }

    // ── Browse: load + filter ─────────────────────────────────────────────────

    private void loadProducts() {
        try {
            List<Product> products = dbController.getActiveProducts();
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
            String prevCat = categoryFilter.getValue();
            categoryFilter.setItems(FXCollections.observableArrayList(cats));
            categoryFilter.setValue(cats.contains(prevCat) ? prevCat : "All Categories");
            applyFilters();
        } catch (Exception e) {
            showAlert("Failed to load products: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String search = nvl(searchField.getText()).trim().toLowerCase();
        String cat    = categoryFilter.getValue();
        double min    = parseDouble(minPriceField.getText(), 0);
        double max    = parseDouble(maxPriceField.getText(), Double.MAX_VALUE);

        filteredProducts.setAll(allProducts.stream().filter(p -> {
            boolean ms = search.isEmpty()
                    || p.getName().toLowerCase().contains(search)
                    || p.getCategory().toLowerCase().contains(search);
            boolean mc = cat == null || "All Categories".equals(cat)
                    || p.getCategory().toLowerCase().contains(cat.toLowerCase());
            boolean mp = p.getPrice() >= min && p.getPrice() <= max;
            return ms && mc && mp;
        }).toList());

        productTable.setItems(filteredProducts);
        int count = filteredProducts.size();
        productCountLabel.setText(count + " product" + (count == 1 ? "" : "s"));
    }

    // ── Table column setup ────────────────────────────────────────────────────

    private void setupTableColumns() {
        productTable.getStylesheets().add(
                getClass().getResource("/dashboard-table.css").toExternalForm());
        productTable.getStylesheets().add(
                getClass().getResource("/dashboard-filter.css").toExternalForm());
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
                if (!row.isEmpty()) row.setStyle(isNow ? ROW_HOVER : ROW_NORMAL);
            });
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1 && !row.isEmpty()) {
                    showProductDetail(row.getItem());
                }
            });
            return row;
        });

        // ── Image ──────────────────────────────────────────────────────────────
        colImage.setCellValueFactory(c -> c.getValue().imagesProperty());
        colImage.setCellFactory(col -> new TableCell<>() {
            final ImageView iv = new ImageView();
            final StackPane container = new StackPane(iv);
            {
                iv.setFitWidth(48); iv.setFitHeight(48); iv.setPreserveRatio(false);
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

        // ── Name + Price ───────────────────────────────────────────────────────
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
                lName.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #030213; -fx-font-family: Poppins;");
                Label lPrice = new Label(String.format("$%.2f", p.getPrice()));
                lPrice.setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
                vb.getChildren().addAll(lName, lPrice);
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
                setText(qty > 0 ? String.valueOf(qty) : "—");
                setAlignment(Pos.CENTER);
                if (qty == 0) {
                    setStyle("-fx-font-size: 13; -fx-text-fill: #717182;");
                } else if (qty < 10) {
                    setStyle("-fx-font-size: 13; -fx-text-fill: #d4183d; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-font-size: 13; -fx-text-fill: #030213;");
                }
            }
        });

        // ── Add to Cart button ─────────────────────────────────────────────────
        colCartBtn.setCellValueFactory(c -> c.getValue().nameProperty());
        colCartBtn.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button();
            { btn.setPrefWidth(116); setAlignment(Pos.CENTER); }

            @Override protected void updateItem(String dummy, boolean empty) {
                super.updateItem(dummy, empty);
                if (empty) { setGraphic(null); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                Product p = getTableView().getItems().get(idx);
                boolean inStock = p.getQuantity() > 0;
                btn.setText(inStock ? "+ Add to Cart" : "Out of Stock");
                btn.setDisable(!inStock);
                final String cartNormal = "-fx-background-color: #030213; -fx-text-fill: white; -fx-font-size: 11;" +
                        " -fx-font-weight: bold; -fx-font-family: Poppins;" +
                        " -fx-padding: 7 12; -fx-background-radius: 8; -fx-cursor: hand;";
                final String cartHover  = "-fx-background-color: #1e293b; -fx-text-fill: white; -fx-font-size: 11;" +
                        " -fx-font-weight: bold; -fx-font-family: Poppins;" +
                        " -fx-padding: 7 12; -fx-background-radius: 8; -fx-cursor: hand;";
                btn.setStyle(inStock ? cartNormal
                        : "-fx-background-color: #e6e6e6; -fx-text-fill: #717182; -fx-font-size: 11;" +
                          " -fx-font-family: Poppins; -fx-padding: 7 12; -fx-background-radius: 8;");
                if (inStock) {
                    btn.setOnMouseEntered(e -> btn.setStyle(cartHover));
                    btn.setOnMouseExited(e -> btn.setStyle(cartNormal));
                } else {
                    btn.setOnMouseEntered(null);
                    btn.setOnMouseExited(null);
                }
                btn.setOnAction(e -> {
                    e.consume();
                    try {
                        dbController.addToCart(UserSession.getEmail(), p.getProductID(), 1);
                        updateCartBadge();
                        btn.setOnMouseEntered(null);
                        btn.setOnMouseExited(null);
                        btn.setText("✓ Added!");
                        btn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-font-size: 11;" +
                                " -fx-font-weight: bold; -fx-font-family: Poppins;" +
                                " -fx-padding: 7 12; -fx-background-radius: 8;");
                        PauseTransition pt = new PauseTransition(Duration.seconds(1.5));
                        pt.setOnFinished(ev -> {
                            btn.setText("+ Add to Cart");
                            btn.setStyle(cartNormal);
                            btn.setOnMouseEntered(ev2 -> btn.setStyle(cartHover));
                            btn.setOnMouseExited(ev2 -> btn.setStyle(cartNormal));
                        });
                        pt.play();
                    } catch (Exception ex) {
                        showAlert("Could not add to cart: " + ex.getMessage());
                    }
                });
                setGraphic(btn);
            }
        });
    }

    // ── Product detail view ───────────────────────────────────────────────────

    private void showProductDetail(Product p) {
        detailTitleLabel.setText(p.getName());
        detailNameLabel.setText(p.getName());
        detailPriceLabel.setText(String.format("$%.2f", p.getPrice()));

        // Stock
        int qty = p.getQuantity();
        if (qty == 0) {
            detailStockLabel.setText("Out of stock");
            detailStockLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #717182; -fx-font-family: Poppins;");
        } else if (qty < 10) {
            detailStockLabel.setText(qty + " left (low stock)");
            detailStockLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #d4183d; -fx-font-weight: bold; -fx-font-family: Poppins;");
        } else {
            detailStockLabel.setText(String.valueOf(qty) + " in stock");
            detailStockLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #030213; -fx-font-family: Poppins;");
        }

        // Description
        String desc = p.getDescription();
        detailDescLabel.setText((desc == null || desc.isBlank()) ? "No description available." : desc);

        // Category chips
        detailCategoriesPane.getChildren().clear();
        if (p.getCategory() != null && !p.getCategory().isBlank()) {
            for (String c : p.getCategory().split(",")) {
                String t = c.trim();
                if (!t.isEmpty()) {
                    Label chip = new Label(t);
                    chip.setStyle("-fx-border-color: #e6e6e6; -fx-border-radius: 4;" +
                            " -fx-background-radius: 4; -fx-background-color: transparent;" +
                            " -fx-padding: 3 10; -fx-font-size: 12; -fx-text-fill: #030213; -fx-font-family: Poppins;");
                    detailCategoriesPane.getChildren().add(chip);
                }
            }
        }

        // Scrollable images
        imageStrip.getChildren().clear();
        List<String> validImgParts = new ArrayList<>();
        if (p.getImages() != null && !p.getImages().isBlank()) {
            for (String part : p.getImages().split("\\|", -1)) {
                if (!part.isBlank()) validImgParts.add(part.trim());
            }
        }
        for (int i = 0; i < validImgParts.size(); i++) {
            final int clickIdx = i;
            try {
                byte[] dec = Base64.getDecoder().decode(validImgParts.get(i));
                ImageView iv = new ImageView(new Image(new ByteArrayInputStream(dec)));
                iv.setFitWidth(180); iv.setFitHeight(180); iv.setPreserveRatio(true);
                StackPane frame = new StackPane(iv);
                frame.setMinSize(180, 180); frame.setMaxSize(180, 180);
                frame.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-cursor: hand;");
                frame.setOnMouseClicked(e -> showLightbox(validImgParts, clickIdx));
                imageStrip.getChildren().add(frame);
            } catch (Exception ignored) {}
        }
        if (imageStrip.getChildren().isEmpty()) {
            Label noImg = new Label("No images");
            noImg.setStyle("-fx-font-size: 13; -fx-text-fill: #717182; -fx-font-family: Poppins;");
            imageStrip.getChildren().add(noImg);
        }

        // Add to Cart button
        boolean inStock = p.getQuantity() > 0;
        detailCartBtn.setText(inStock ? "+ Add to Cart" : "Out of Stock");
        detailCartBtn.setDisable(!inStock);
        detailCartBtn.setStyle(inStock
                ? "-fx-background-color: #030213; -fx-text-fill: white; -fx-font-size: 13;" +
                  " -fx-font-weight: bold; -fx-font-family: Poppins; -fx-padding: 12 28;" +
                  " -fx-background-radius: 8; -fx-cursor: hand;"
                : "-fx-background-color: #e6e6e6; -fx-text-fill: #717182; -fx-font-size: 13;" +
                  " -fx-font-family: Poppins; -fx-padding: 12 28; -fx-background-radius: 8;");
        detailCartBtn.setOnAction(e -> {
            try {
                dbController.addToCart(UserSession.getEmail(), p.getProductID(), 1);
                updateCartBadge();
                detailCartBtn.setText("✓ Added!");
                detailCartBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white;" +
                        " -fx-font-size: 13; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                        " -fx-padding: 12 28; -fx-background-radius: 8;");
                PauseTransition pt = new PauseTransition(Duration.seconds(1.5));
                pt.setOnFinished(ev -> {
                    detailCartBtn.setText("+ Add to Cart");
                    detailCartBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                            " -fx-font-size: 13; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                            " -fx-padding: 12 28; -fx-background-radius: 8; -fx-cursor: hand;");
                });
                pt.play();
            } catch (Exception ex) {
                showAlert("Could not add to cart: " + ex.getMessage());
            }
        });

        showSection("detail");
    }

    @FXML
    private void handleBack() {
        showSection("browse");
    }

    // ── Image lightbox ────────────────────────────────────────────────────────

    private void showLightbox(List<String> parts, int startIndex) {
        if (parts == null || parts.isEmpty()) return;
        int[] idx = {Math.max(0, Math.min(startIndex, parts.size() - 1))};

        // Large image view
        ImageView bigIv = new ImageView();
        bigIv.setFitWidth(540); bigIv.setFitHeight(460); bigIv.setPreserveRatio(true);
        StackPane imgBox = new StackPane(bigIv);
        imgBox.setMinSize(540, 460); imgBox.setMaxSize(540, 460);
        imgBox.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 10;");

        // Counter
        Label counter = new Label();
        counter.setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");

        // Load helper
        Runnable loadImg = () -> {
            try {
                byte[] dec = Base64.getDecoder().decode(parts.get(idx[0]));
                bigIv.setImage(new Image(new ByteArrayInputStream(dec)));
            } catch (Exception ex) { bigIv.setImage(null); }
            counter.setText((idx[0] + 1) + " / " + parts.size());
        };
        loadImg.run();

        // Prev / next buttons
        Button prev = navBtn("‹");
        Button next = navBtn("›");
        boolean single = parts.size() <= 1;
        prev.setVisible(!single); prev.setManaged(!single);
        next.setVisible(!single); next.setManaged(!single);
        prev.setOnAction(e -> { idx[0] = (idx[0] - 1 + parts.size()) % parts.size(); loadImg.run(); });
        next.setOnAction(e -> { idx[0] = (idx[0] + 1) % parts.size(); loadImg.run(); });

        HBox imgRow = new HBox(16, prev, imgBox, next);
        imgRow.setAlignment(Pos.CENTER);

        VBox card = new VBox(12, counter, imgRow);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 24;");
        card.setMaxWidth(660); card.setMaxHeight(580);
        // Consume clicks so they don't propagate to overlay
        card.setOnMouseClicked(javafx.event.Event::consume);

        StackPane overlay = new StackPane(card);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.65);");
        overlay.setOnMouseClicked(e -> mainStack.getChildren().remove(overlay));

        mainStack.getChildren().add(overlay);
    }

    private Button navBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: white; -fx-background-radius: 50;" +
                " -fx-border-color: #e6e6e6; -fx-border-radius: 50;" +
                " -fx-font-size: 24; -fx-cursor: hand; -fx-padding: 4 16;" +
                " -fx-text-fill: #030213;");
        return b;
    }

    // ── Cart ──────────────────────────────────────────────────────────────────

    private void loadCart() {
        try {
            List<CartItem> items = dbController.getCartItems(UserSession.getEmail());
            cartItemsBox.getChildren().clear();

            if (items.isEmpty()) {
                cartCountLabel.setText("");
                cartFooter.setVisible(false);    cartFooter.setManaged(false);
                clearCartBtn.setVisible(false);  clearCartBtn.setManaged(false);

                VBox empty = new VBox(12);
                empty.setAlignment(Pos.CENTER);
                empty.setStyle("-fx-padding: 60 0;");
                Label icon  = new Label("\uD83D\uDED2"); icon.setStyle("-fx-font-size: 48;");
                Label title = new Label("Your cart is empty");
                title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213; -fx-font-family: Poppins;");
                Label sub   = new Label("Browse products and add them to your cart.");
                sub.setStyle("-fx-font-size: 13; -fx-text-fill: #717182; -fx-font-family: Poppins;");
                Button browse = new Button("Browse Products");
                browse.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                        " -fx-font-size: 13; -fx-font-family: Poppins;" +
                        " -fx-padding: 10 24; -fx-background-radius: 8; -fx-cursor: hand;");
                browse.setOnAction(e -> showSection("browse"));
                empty.getChildren().addAll(icon, title, sub, browse);
                cartItemsBox.getChildren().add(empty);
            } else {
                double total = items.stream().mapToDouble(CartItem::getSubtotal).sum();
                cartCountLabel.setText(items.size() + " item" + (items.size() == 1 ? "" : "s"));
                cartTotalLabel.setText("Total: " + String.format("$%.2f", total));
                cartFooter.setVisible(true);     cartFooter.setManaged(true);
                clearCartBtn.setVisible(true);   clearCartBtn.setManaged(true);
                for (CartItem ci : items) {
                    cartItemsBox.getChildren().add(buildCartRow(ci));
                }
            }
        } catch (Exception e) {
            showAlert("Failed to load cart: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearCart() {
        try {
            dbController.clearCart(UserSession.getEmail());
            updateCartBadge();
            loadCart();
        } catch (Exception e) {
            showAlert("Failed to clear cart: " + e.getMessage());
        }
    }

    private HBox buildCartRow(CartItem ci) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: white; -fx-background-radius: 12;" +
                " -fx-border-color: #e6e6e6; -fx-border-radius: 12; -fx-padding: 14 18;" +
                " -fx-effect: dropshadow(gaussian,rgba(0,0,0,0.04),6,0,0,1);");

        ImageView iv = new ImageView();
        iv.setFitWidth(70); iv.setFitHeight(70); iv.setPreserveRatio(false);
        if (ci.getProductImages() != null && !ci.getProductImages().isBlank()) {
            try {
                byte[] dec = Base64.getDecoder().decode(ci.getProductImages().split("\\|")[0].trim());
                iv.setImage(new Image(new ByteArrayInputStream(dec)));
            } catch (Exception ignored) {}
        }

        VBox info = new VBox(4);
        Label lName = new Label(ci.getProductName());
        lName.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #030213; -fx-font-family: Poppins;");
        Label lUnitPrice = new Label(String.format("$%.2f each", ci.getProductPrice()));
        lUnitPrice.setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
        info.getChildren().addAll(lName, lUnitPrice);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label qtyLabel = new Label(String.valueOf(ci.getQuantity()));
        qtyLabel.setMinWidth(28);
        qtyLabel.setAlignment(Pos.CENTER);
        qtyLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #030213; -fx-font-family: Poppins;");

        Button minus = qtyBtn("−");
        Button plus  = qtyBtn("+");

        Label lSub = new Label(String.format("$%.2f", ci.getSubtotal()));
        lSub.setMinWidth(70);
        lSub.setAlignment(Pos.CENTER_RIGHT);
        lSub.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #030213; -fx-font-family: Poppins;");

        minus.setOnAction(e -> {
            if (ci.getQuantity() <= 1) return;
            try {
                dbController.updateCartQuantity(ci.getCartID(), ci.getQuantity() - 1);
                ci.setQuantity(ci.getQuantity() - 1);
                qtyLabel.setText(String.valueOf(ci.getQuantity()));
                lSub.setText(String.format("$%.2f", ci.getSubtotal()));
                refreshCartTotal();
            } catch (Exception ex) { showAlert("Error: " + ex.getMessage()); }
        });

        plus.setOnAction(e -> {
            if (ci.getQuantity() >= ci.getProductStock()) return;
            try {
                dbController.updateCartQuantity(ci.getCartID(), ci.getQuantity() + 1);
                ci.setQuantity(ci.getQuantity() + 1);
                qtyLabel.setText(String.valueOf(ci.getQuantity()));
                lSub.setText(String.format("$%.2f", ci.getSubtotal()));
                refreshCartTotal();
            } catch (Exception ex) { showAlert("Error: " + ex.getMessage()); }
        });

        HBox qtyBox = new HBox(8, minus, qtyLabel, plus);
        qtyBox.setAlignment(Pos.CENTER);

        Button remove = new Button("✕");
        remove.setStyle("-fx-background-color: transparent; -fx-text-fill: #717182;" +
                " -fx-font-size: 15; -fx-cursor: hand; -fx-padding: 2 6;");
        remove.setOnMouseEntered(e -> remove.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #d4183d; -fx-font-size: 15; -fx-cursor: hand; -fx-padding: 2 6;"));
        remove.setOnMouseExited(e -> remove.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #717182; -fx-font-size: 15; -fx-cursor: hand; -fx-padding: 2 6;"));
        remove.setOnAction(e -> {
            try {
                dbController.removeFromCart(ci.getCartID());
                loadCart();
                updateCartBadge();
            } catch (Exception ex) { showAlert("Error: " + ex.getMessage()); }
        });

        row.getChildren().addAll(iv, info, spacer, qtyBox, lSub, remove);
        return row;
    }

    private Button qtyBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #ececf0; -fx-background-radius: 6;" +
                " -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 10; -fx-text-fill: #030213;");
        return b;
    }

    private void refreshCartTotal() {
        try {
            List<CartItem> items = dbController.getCartItems(UserSession.getEmail());
            double total = items.stream().mapToDouble(CartItem::getSubtotal).sum();
            cartTotalLabel.setText("Total: " + String.format("$%.2f", total));
            updateCartBadge();
        } catch (Exception ignored) {}
    }

    private void updateCartBadge() {
        try {
            int count = dbController.getCartItemCount(UserSession.getEmail());
            if (count > 0) {
                cartBadge.setText(String.valueOf(count));
                cartBadge.setVisible(true);
                cartBadge.setManaged(true);
            } else {
                cartBadge.setVisible(false);
                cartBadge.setManaged(false);
            }
        } catch (Exception e) {
            cartBadge.setVisible(false);
            cartBadge.setManaged(false);
        }
    }

    // ── Place Order ───────────────────────────────────────────────────────────

    @FXML
    private void handlePlaceOrder() {
        try {
            List<CartItem> items = dbController.getCartItems(UserSession.getEmail());
            if (items.isEmpty()) {
                showAlert("Your cart is empty.");
                return;
            }
            double total = items.stream().mapToDouble(CartItem::getSubtotal).sum();
            dbController.placeOrder(UserSession.getEmail(), items, total);
            // Reload products to reflect decremented stock
            loadProducts();
            updateCartBadge();
            loadOrders();
            showSection("orders");
            // Schedule automatic order status progression via timers
            List<Order> orders = dbController.getOrdersByEmail(UserSession.getEmail());
            if (!orders.isEmpty()) {
                Order latest = orders.get(0);
                scheduleOrderProgression(latest.getOrderID(), latest.getOrderCode());
            }
        } catch (Exception e) {
            showAlert("Failed to place order: " + e.getMessage());
        }
    }

    /**
     * Schedules timer-driven status transitions for a newly placed order:
     * Processing → Shipped after 25–45 s (auto-creates delivery),
     * Shipped → Delivered after another 40–70 s.
     */
    private void scheduleOrderProgression(int orderID, String orderCode) {
        int shippedDelaySec   = 25 + (int) (Math.random() * 20);   // 25-45 s
        int deliveredDelaySec = shippedDelaySec + 40 + (int) (Math.random() * 30); // +40-70 s

        javafx.animation.Timeline timer = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(shippedDelaySec),
                e -> {
                    try {
                        dbController.updateOrderStatus(orderID, "Shipped");
                        javafx.application.Platform.runLater(() -> {
                            if (ordersSection.isVisible()) loadOrders();
                        });
                    } catch (Exception ex) { /* silently skip if order was cancelled */ }
                }),
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(deliveredDelaySec),
                e -> {
                    try {
                        // Delivered may already be set by delivery propagation; only set if Shipped
                        List<Order> current = dbController.getOrdersByEmail(UserSession.getEmail());
                        boolean stillShipped = current.stream()
                                .anyMatch(o -> o.getOrderID() == orderID && "Shipped".equals(o.getStatus()));
                        if (stillShipped) dbController.updateOrderStatus(orderID, "Delivered");
                        javafx.application.Platform.runLater(() -> {
                            if (ordersSection.isVisible()) loadOrders();
                        });
                    } catch (Exception ex) { /* ignore */ }
                })
        );
        timer.play();
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    private void loadOrders() {
        try {
            List<Order> orders = dbController.getOrdersByEmail(UserSession.getEmail());
            String filter = (orderStatusFilter != null) ? orderStatusFilter.getValue() : null;
            if (filter != null && !"All Statuses".equals(filter)) {
                orders = orders.stream().filter(o -> filter.equals(o.getStatus())).toList();
            }
            allOrders.setAll(orders);
            ordersTable.setItems(allOrders);
            int count = allOrders.size();
            orderCountLabel.setText(count + " order" + (count == 1 ? "" : "s"));
        } catch (Exception e) {
            showAlert("Failed to load orders: " + e.getMessage());
        }
    }

    private void setupOrdersTable() {
        ordersTable.getStylesheets().add(
                getClass().getResource("/dashboard-table.css").toExternalForm());
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final String ROW_NORMAL = "-fx-background-color: white;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String ROW_HOVER  = "-fx-background-color: #f0f4f8;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        ordersTable.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) ->
                    row.setStyle(item == null ? "" : ROW_NORMAL));
            row.hoverProperty().addListener((obs, was, isNow) -> {
                if (!row.isEmpty()) row.setStyle(isNow ? ROW_HOVER : ROW_NORMAL);
            });
            return row;
        });

        // Order Code
        colOrderCode.setCellValueFactory(c -> c.getValue().orderCodeProperty());
        colOrderCode.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String code, boolean empty) {
                super.updateItem(code, empty);
                if (empty || code == null) { setGraphic(null); return; }
                Label lbl = new Label(code);
                lbl.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #030213; -fx-font-family: Poppins;");
                setGraphic(lbl);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 12));
            }
        });

        // Date
        colOrderDate.setCellValueFactory(c -> c.getValue().orderDateProperty());
        colOrderDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) { setText(null); return; }
                setText(date);
                setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
                setAlignment(Pos.CENTER);
            }
        });

        // Items count
        colOrderItems.setCellValueFactory(c -> c.getValue().itemCountProperty().asObject());
        colOrderItems.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer cnt, boolean empty) {
                super.updateItem(cnt, empty);
                if (empty || cnt == null) { setText(null); return; }
                setText(String.valueOf(cnt));
                setStyle("-fx-font-size: 13; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER);
            }
        });

        // Total
        colOrderTotal.setCellValueFactory(c -> c.getValue().totalProperty().asObject());
        colOrderTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) { setText(null); return; }
                setText(String.format("$%.2f", total));
                setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #030213;");
                setAlignment(Pos.CENTER);
            }
        });

        // Status badge
        colOrderStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colOrderStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setStyle(statusBadgeStyle(status));
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        // Actions (Cancel for Processing; Return for Delivered)
        colOrderActions.setCellValueFactory(c -> c.getValue().orderCodeProperty());
        colOrderActions.setCellFactory(col -> new TableCell<>() {
            final Button cancelBtn = new Button("Cancel");
            final Button returnBtn = new Button("Return");
            {
                cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d4183d;" +
                        " -fx-font-size: 12; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                        " -fx-border-color: #fecaca; -fx-border-radius: 6;" +
                        " -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;");
                returnBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #5b21b6;" +
                        " -fx-font-size: 12; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                        " -fx-border-color: #ddd6fe; -fx-border-radius: 6;" +
                        " -fx-background-radius: 6; -fx-padding: 5 12; -fx-cursor: hand;");
            }
            @Override protected void updateItem(String dummy, boolean empty) {
                super.updateItem(dummy, empty);
                if (empty) { setGraphic(null); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                Order o = getTableView().getItems().get(idx);
                switch (o.getStatus()) {
                    case "Processing" -> {
                        cancelBtn.setOnAction(e -> {
                            try {
                                dbController.updateOrderStatus(o.getOrderID(), "Cancelled");
                                loadOrders();
                            } catch (Exception ex) {
                                showAlert("Failed to cancel order: " + ex.getMessage());
                            }
                        });
                        setGraphic(cancelBtn);
                    }
                    case "Delivered" -> {
                        returnBtn.setOnAction(e -> handleReturnOrder(o));
                        setGraphic(returnBtn);
                    }
                    default -> setGraphic(null);
                }
                setAlignment(Pos.CENTER);
            }
        });
    }

    /** Opens a return-request dialog for a Delivered order. */
    private void handleReturnOrder(Order o) {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) ordersTable.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Return Order");
        dialog.setResizable(false);

        Label title = new Label("Return Order " + o.getOrderCode());
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");

        Label body = new Label("Please describe the reason for returning this order.");
        body.setStyle("-fx-font-size: 13; -fx-text-fill: #717182;");
        body.setWrapText(true);

        Label reasonLbl = new Label("Reason for Return");
        reasonLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182;");
        TextField reasonField = new TextField();
        reasonField.setPromptText("e.g. Item damaged, Wrong item received...");
        reasonField.setPrefHeight(38);
        reasonField.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6;" +
                " -fx-border-radius: 8; -fx-padding: 0 12; -fx-font-size: 13;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;" +
                " -fx-background-color: #fef2f2; -fx-background-radius: 6; -fx-padding: 8 12;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #030213;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button confirmBtn = new Button("Submit Return");
        confirmBtn.setStyle("-fx-background-color: #5b21b6; -fx-text-fill: white;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        confirmBtn.setOnAction(e -> {
            String reason = reasonField.getText().trim();
            if (reason.isEmpty()) {
                errorLabel.setText("Please enter a return reason.");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }
            try {
                dbController.submitReturnRequest(o.getOrderID(), reason);
                dialog.close();
                loadOrders();
            } catch (Exception ex) {
                errorLabel.setText("Failed to submit return: " + ex.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        });

        HBox buttons = new HBox(10, cancelBtn, confirmBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(4, 0, 0, 0));

        VBox root = new VBox(12, title, body, reasonLbl, reasonField, errorLabel, buttons);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(420);

        dialog.setScene(new javafx.scene.Scene(root));
        dialog.showAndWait();
    }

    private String statusBadgeStyle(String status) {
        return switch (status) {
            case "Processing" ->
                "-fx-background-color: #fef9c3; -fx-text-fill: #854d0e;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Shipped" ->
                "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Delivered" ->
                "-fx-background-color: #030213; -fx-text-fill: white;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Returned" ->
                "-fx-background-color: #ede9fe; -fx-text-fill: #5b21b6;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            case "Cancelled" ->
                "-fx-background-color: transparent; -fx-text-fill: #717182;" +
                " -fx-border-color: #e6e6e6; -fx-border-radius: 20;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11;";
            case "Failed" ->
                "-fx-background-color: #fef2f2; -fx-text-fill: #d4183d;" +
                " -fx-border-color: #fecaca; -fx-border-radius: 20;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11; -fx-font-weight: bold;";
            default ->
                "-fx-background-color: #f3f4f6; -fx-text-fill: #717182;" +
                " -fx-padding: 3 12; -fx-background-radius: 20; -fx-font-size: 11;";
        };
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    @FXML private void handleClearFilters() {
        searchField.clear();
        categoryFilter.setValue("All Categories");
        minPriceField.clear();
        maxPriceField.clear();
    }

    /**
     * Opens a styled in-app confirmation dialog for account deletion.
     * On confirmation, deletes the account and navigates to the Start screen.
     * All errors are displayed inline — no native OS dialogs.
     */
    @FXML
    private void handleDeleteAccount() {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) productTable.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Delete Account");
        dialog.setResizable(false);

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label("Delete Your Account");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213;");

        javafx.scene.control.Label bodyLabel = new javafx.scene.control.Label(
            "Are you sure you want to permanently delete your account?\n" +
            "This action cannot be undone.");
        bodyLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #717182;");
        bodyLabel.setWrapText(true);

        // Inline error label — shown only when a failure occurs
        javafx.scene.control.Label errorLabel = new javafx.scene.control.Label();
        errorLabel.setStyle(
            "-fx-font-size: 12; -fx-text-fill: #d4183d;" +
            " -fx-background-color: #fef2f2; -fx-background-radius: 6; -fx-padding: 8 12;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        javafx.scene.control.Button cancelButton = new javafx.scene.control.Button("Cancel");
        cancelButton.setStyle(
            "-fx-background-color: #f3f4f6; -fx-text-fill: #030213;" +
            " -fx-font-size: 13; -fx-font-weight: bold;" +
            " -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        cancelButton.setOnAction(e -> dialog.close());

        javafx.scene.control.Button confirmButton = new javafx.scene.control.Button("Delete Account");
        confirmButton.setStyle(
            "-fx-background-color: #d4183d; -fx-text-fill: white;" +
            " -fx-font-size: 13; -fx-font-weight: bold;" +
            " -fx-padding: 9 20; -fx-background-radius: 8; -fx-cursor: hand;");
        confirmButton.setOnAction(e -> {
            try {
                dbController.deleteAccount(UserSession.getEmail());
                UserSession.clear();
                dialog.close();
                // Navigate back to the start screen
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
        UserSession.clear();
        Scene scene = productTable.getScene();
        scene.setFill(Color.web("#162535"));
        Parent root = FXMLLoader.load(getClass().getResource("/Start.fxml"));
        scene.setRoot(root);
    }

    // ── Customer Reviews ──────────────────────────────────────────────────────

    private void setupCustomerReviewsSection() {
        custReviewsTable.getStylesheets().add(
                getClass().getResource("/dashboard-table.css").toExternalForm());
        custReviewsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        final String ROW_NORMAL = "-fx-background-color: white;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        final String ROW_HOVER  = "-fx-background-color: #f0f4f8;" +
                " -fx-border-color: transparent transparent #e6e6e6 transparent; -fx-border-width: 0 0 1 0;";
        custReviewsTable.setRowFactory(tv -> {
            TableRow<Review> row = new TableRow<>();
            row.itemProperty().addListener((obs, old, item) ->
                    row.setStyle(item == null ? "" : ROW_NORMAL));
            row.hoverProperty().addListener((obs, was, isNow) -> {
                if (!row.isEmpty()) row.setStyle(isNow ? ROW_HOVER : ROW_NORMAL);
            });
            return row;
        });

        // Product column
        colCustRevProduct.setCellValueFactory(c -> c.getValue().productNameProperty());
        colCustRevProduct.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setGraphic(null); return; }
                Label lbl = new Label(name);
                lbl.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #030213; -fx-font-family: Poppins;");
                lbl.setWrapText(true);
                setGraphic(lbl);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 0, 0, 8));
            }
        });

        // Rating (stars) column
        colCustRevRating.setCellValueFactory(c -> c.getValue().ratingProperty().asString());
        colCustRevRating.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String r, boolean empty) {
                super.updateItem(r, empty);
                if (empty || r == null) { setGraphic(null); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                Review rev = getTableView().getItems().get(idx);
                Label stars = new Label(rev.starsDisplay());
                stars.setStyle("-fx-font-size: 14; -fx-text-fill: #f59e0b;");
                setGraphic(stars);
                setAlignment(Pos.CENTER);
            }
        });

        // Comment column (truncated)
        colCustRevComment.setCellValueFactory(c -> c.getValue().commentProperty());
        colCustRevComment.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String comment, boolean empty) {
                super.updateItem(comment, empty);
                if (empty || comment == null) { setText(null); setGraphic(null); return; }
                String truncated = comment.length() > 60 ? comment.substring(0, 60) + "…" : comment;
                Label lbl = new Label(truncated);
                lbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
                lbl.setWrapText(true);
                setGraphic(lbl);
                setAlignment(Pos.CENTER_LEFT);
                setPadding(new Insets(0, 4, 0, 4));
            }
        });

        // Date column
        colCustRevDate.setCellValueFactory(c -> c.getValue().createdAtProperty());
        colCustRevDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) { setText(null); return; }
                String display = date.length() > 10 ? date.substring(0, 10) : date;
                setText(display);
                setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
                setAlignment(Pos.CENTER);
            }
        });

        // Helpful count column
        colCustRevHelpful.setCellValueFactory(c -> c.getValue().helpfulCountProperty().asString());
        colCustRevHelpful.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String cnt, boolean empty) {
                super.updateItem(cnt, empty);
                if (empty || cnt == null) { setText(null); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setText(null); return; }
                Review rev = getTableView().getItems().get(idx);
                setText("👍 " + rev.getHelpfulCount() + " / 👎 " + rev.getUnhelpfulCount());
                setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
                setAlignment(Pos.CENTER);
            }
        });

        // Actions column
        colCustRevActions.setCellValueFactory(c -> c.getValue().createdAtProperty());
        colCustRevActions.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String dummy, boolean empty) {
                super.updateItem(dummy, empty);
                if (empty) { setGraphic(null); return; }
                int idx = getIndex();
                if (idx < 0 || idx >= getTableView().getItems().size()) { setGraphic(null); return; }
                Review rev = getTableView().getItems().get(idx);

                // Determine time remaining in 5-minute window
                java.time.LocalDateTime createdTime = null;
                try {
                    createdTime = java.time.LocalDateTime.parse(
                        rev.getCreatedAt().replace(" ", "T"));
                } catch (Exception ignored) {}

                boolean withinWindow = false;
                String timeRemaining = "";
                if (createdTime != null) {
                    java.time.Duration elapsed = java.time.Duration.between(createdTime, java.time.LocalDateTime.now());
                    long secondsLeft = 300 - elapsed.getSeconds();
                    withinWindow = secondsLeft > 0;
                    if (withinWindow) {
                        long mins = secondsLeft / 60;
                        long secs = secondsLeft % 60;
                        timeRemaining = mins > 0 ? mins + "m " + secs + "s" : secs + "s";
                    }
                }

                HBox box = new HBox(6);
                box.setAlignment(Pos.CENTER);

                if (withinWindow) {
                    final String remaining = timeRemaining;
                    Button editBtn = new Button("Edit (" + remaining + ")");
                    editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1e40af;" +
                            " -fx-font-size: 11; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                            " -fx-border-color: #bfdbfe; -fx-border-radius: 6;" +
                            " -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
                    editBtn.setOnAction(e -> showEditReviewDialog(rev));

                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #d4183d;" +
                            " -fx-font-size: 11; -fx-font-weight: bold; -fx-font-family: Poppins;" +
                            " -fx-border-color: #fecaca; -fx-border-radius: 6;" +
                            " -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
                    deleteBtn.setOnAction(e -> {
                        try {
                            dbController.deleteReview(rev.getReviewID(), UserSession.getEmail());
                            loadCustomerReviews();
                        } catch (Exception ex) {
                            showReviewsError("Failed to delete review: " + ex.getMessage());
                        }
                    });
                    box.getChildren().addAll(editBtn, deleteBtn);
                } else {
                    // Mark helpful button
                    Button helpfulBtn = new Button("👍 Helpful");
                    helpfulBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #717182;" +
                            " -fx-font-size: 11; -fx-font-family: Poppins;" +
                            " -fx-border-color: #e6e6e6; -fx-border-radius: 6;" +
                            " -fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;");
                    helpfulBtn.setOnAction(e -> {
                        try {
                            dbController.voteHelpful(rev.getReviewID(), UserSession.getEmail(), true);
                            loadCustomerReviews();
                        } catch (Exception ex) {
                            showReviewsError("Could not vote: " + ex.getMessage());
                        }
                    });
                    box.getChildren().add(helpfulBtn);
                }

                setGraphic(box);
                setAlignment(Pos.CENTER);
            }
        });

        // Rating filter
        custReviewRatingFilter.setItems(FXCollections.observableArrayList(
                "All Ratings", "5 Stars", "4 Stars", "3 Stars", "2 Stars", "1 Star"));
        custReviewRatingFilter.setValue("All Ratings");
        custReviewRatingFilter.valueProperty().addListener((o, old, nw) -> applyCustomerReviewFilters());

        // Search filter
        if (custReviewSearchField != null) {
            custReviewSearchField.textProperty().addListener((o, old, nw) -> applyCustomerReviewFilters());
        }

        // Submit button
        if (btnSubmitReview != null) {
            btnSubmitReview.setOnAction(e -> showSubmitReviewDialog());
        }

        // Clear error label initially
        if (reviewsErrorLabel != null) {
            reviewsErrorLabel.setVisible(false);
            reviewsErrorLabel.setManaged(false);
        }

        custReviewsTable.setItems(filteredCustReviews);
    }

    private void loadCustomerReviews() {
        try {
            List<Review> reviews = dbController.getReviewsByCustomer(UserSession.getEmail());
            allCustReviews.setAll(reviews);
            applyCustomerReviewFilters();
            clearReviewsError();
        } catch (Exception e) {
            showReviewsError("Failed to load reviews: " + e.getMessage());
        }
    }

    private void applyCustomerReviewFilters() {
        String search = (custReviewSearchField != null) ?
                custReviewSearchField.getText().trim().toLowerCase() : "";
        String ratingVal = (custReviewRatingFilter != null) ?
                custReviewRatingFilter.getValue() : "All Ratings";

        filteredCustReviews.setAll(allCustReviews.stream().filter(r -> {
            boolean ms = search.isEmpty()
                    || r.getProductName().toLowerCase().contains(search)
                    || r.getComment().toLowerCase().contains(search);
            boolean mr = true;
            if (ratingVal != null && !ratingVal.equals("All Ratings")) {
                int starCount = switch (ratingVal) {
                    case "5 Stars" -> 5;
                    case "4 Stars" -> 4;
                    case "3 Stars" -> 3;
                    case "2 Stars" -> 2;
                    case "1 Star"  -> 1;
                    default -> -1;
                };
                mr = (starCount == -1 || r.getRating() == starCount);
            }
            return ms && mr;
        }).toList());
        custReviewsTable.setItems(filteredCustReviews);
    }

    private void showReviewsError(String msg) {
        if (reviewsErrorLabel == null) { showAlert(msg); return; }
        reviewsErrorLabel.setText(msg);
        reviewsErrorLabel.setVisible(true);
        reviewsErrorLabel.setManaged(true);
        HoverUtils.shake(reviewsErrorLabel);
        PauseTransition dismiss = new PauseTransition(Duration.seconds(5));
        dismiss.setOnFinished(e -> clearReviewsError());
        dismiss.play();
    }

    private void clearReviewsError() {
        if (reviewsErrorLabel == null) return;
        reviewsErrorLabel.setVisible(false);
        reviewsErrorLabel.setManaged(false);
    }

    private void showSubmitReviewDialog() {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) custReviewsTable.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Submit Review");
        dialog.setResizable(false);

        Label titleLbl = new Label("Submit a Review");
        titleLbl.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213; -fx-font-family: Poppins;");

        // Product ComboBox — only products that are purchased + delivered
        Label productLbl = new Label("Product");
        productLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
        ComboBox<Product> productCombo = new ComboBox<>();
        productCombo.setPrefWidth(360);
        productCombo.setPromptText("Select a product you purchased…");
        productCombo.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-font-size: 13;");

        // Load eligible products (delivered orders)
        List<Product> eligibleProducts = new ArrayList<>();
        try {
            List<Order> myOrders = dbController.getOrdersByEmail(UserSession.getEmail());
            Set<Integer> eligibleIDs = new java.util.LinkedHashSet<>();
            for (Order o : myOrders) {
                if ("Delivered".equals(o.getStatus())) {
                    for (Product p : allProducts) {
                        // Check if we've already added or if it's eligible
                        if (!eligibleIDs.contains(p.getProductID())) {
                            try {
                                if (dbController.hasCustomerPurchasedProduct(UserSession.getEmail(), p.getProductID())
                                        && !dbController.hasCustomerReviewedProduct(UserSession.getEmail(), p.getProductID())) {
                                    eligibleIDs.add(p.getProductID());
                                    eligibleProducts.add(p);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                    break; // avoid redundant loops per order; eligible check is per product
                }
            }
        } catch (Exception e) {
            showReviewsError("Could not load eligible products: " + e.getMessage());
        }

        productCombo.setItems(FXCollections.observableArrayList(eligibleProducts));
        productCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getName());
            }
        });
        productCombo.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : p.getName());
            }
        });

        // Rating toggle buttons (1–5)
        Label ratingLbl = new Label("Rating");
        ratingLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
        int[] selectedRating = {0};
        Button[] starBtns = new Button[5];
        HBox starBox = new HBox(8);
        starBox.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            Button b = new Button("★");
            b.setStyle("-fx-background-color: transparent; -fx-text-fill: #d1d5db; -fx-font-size: 24; -fx-cursor: hand; -fx-padding: 0;");
            b.setOnAction(e -> {
                selectedRating[0] = star;
                for (int j = 0; j < 5; j++) {
                    starBtns[j].setStyle(j < star
                            ? "-fx-background-color: transparent; -fx-text-fill: #f59e0b; -fx-font-size: 24; -fx-cursor: hand; -fx-padding: 0;"
                            : "-fx-background-color: transparent; -fx-text-fill: #d1d5db; -fx-font-size: 24; -fx-cursor: hand; -fx-padding: 0;");
                }
            });
            starBtns[i - 1] = b;
            starBox.getChildren().add(b);
        }

        // Comment field
        Label commentLbl = new Label("Comment (max 500 characters)");
        commentLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
        javafx.scene.control.TextArea commentArea = new javafx.scene.control.TextArea();
        commentArea.setPromptText("Share your experience with this product…");
        commentArea.setPrefRowCount(4);
        commentArea.setPrefWidth(360);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-font-size: 13; -fx-font-family: Poppins;");
        Label charCount = new Label("0 / 500");
        charCount.setStyle("-fx-font-size: 11; -fx-text-fill: #717182; -fx-font-family: Poppins;");
        commentArea.textProperty().addListener((obs, old, nw) -> {
            if (nw.length() > 500) {
                commentArea.setText(old);
            } else {
                charCount.setText(nw.length() + " / 500");
            }
        });

        // Error label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;" +
                " -fx-background-color: #fef2f2; -fx-background-radius: 6; -fx-padding: 8 12;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Buttons
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #030213;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button submitBtn = new Button("Submit Review");
        submitBtn.setStyle("-fx-background-color: #030213; -fx-text-fill: white;" +
                " -fx-font-size: 13; -fx-font-weight: bold; -fx-padding: 9 20;" +
                " -fx-background-radius: 8; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            Product selectedProduct = productCombo.getValue();
            if (selectedProduct == null) {
                errorLabel.setText("Please select a product.");
                errorLabel.setVisible(true); errorLabel.setManaged(true);
                return;
            }
            if (selectedRating[0] == 0) {
                errorLabel.setText("Please select a rating (1-5 stars).");
                errorLabel.setVisible(true); errorLabel.setManaged(true);
                return;
            }
            String comment = commentArea.getText().trim();
            if (comment.isEmpty()) {
                errorLabel.setText("Please enter a comment.");
                errorLabel.setVisible(true); errorLabel.setManaged(true);
                return;
            }
            try {
                dbController.submitReview(selectedProduct.getProductID(), UserSession.getEmail(), selectedRating[0], comment);
                dialog.close();
                loadCustomerReviews();
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true); errorLabel.setManaged(true);
                HoverUtils.shake(errorLabel);
            }
        });

        HBox buttons = new HBox(10, cancelBtn, submitBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(4, 0, 0, 0));

        VBox charRow = new VBox(2, commentArea, charCount);

        VBox root = new VBox(12, titleLbl, productLbl, productCombo,
                ratingLbl, starBox, commentLbl, charRow, errorLabel, buttons);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(420);

        dialog.setScene(new javafx.scene.Scene(root));
        dialog.showAndWait();
    }

    private void showEditReviewDialog(Review rev) {
        Stage dialog = new Stage();
        dialog.initOwner((Stage) custReviewsTable.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Review");
        dialog.setResizable(false);

        Label titleLbl = new Label("Edit Review — " + rev.getProductName());
        titleLbl.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #030213; -fx-font-family: Poppins;");

        // Rating toggle buttons (1–5)
        Label ratingLbl = new Label("Rating");
        ratingLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
        int[] selectedRating = {rev.getRating()};
        Button[] starBtns = new Button[5];
        HBox starBox = new HBox(8);
        starBox.setAlignment(Pos.CENTER_LEFT);
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            Button b = new Button("★");
            b.setStyle(i <= rev.getRating()
                    ? "-fx-background-color: transparent; -fx-text-fill: #f59e0b; -fx-font-size: 24; -fx-cursor: hand; -fx-padding: 0;"
                    : "-fx-background-color: transparent; -fx-text-fill: #d1d5db; -fx-font-size: 24; -fx-cursor: hand; -fx-padding: 0;");
            b.setOnAction(e -> {
                selectedRating[0] = star;
                for (int j = 0; j < 5; j++) {
                    starBtns[j].setStyle(j < star
                            ? "-fx-background-color: transparent; -fx-text-fill: #f59e0b; -fx-font-size: 24; -fx-cursor: hand; -fx-padding: 0;"
                            : "-fx-background-color: transparent; -fx-text-fill: #d1d5db; -fx-font-size: 24; -fx-cursor: hand; -fx-padding: 0;");
                }
            });
            starBtns[i - 1] = b;
            starBox.getChildren().add(b);
        }

        // Comment field
        Label commentLbl = new Label("Comment (max 500 characters)");
        commentLbl.setStyle("-fx-font-size: 12; -fx-text-fill: #717182; -fx-font-family: Poppins;");
        javafx.scene.control.TextArea commentArea = new javafx.scene.control.TextArea(rev.getComment());
        commentArea.setPrefRowCount(4);
        commentArea.setPrefWidth(360);
        commentArea.setWrapText(true);
        commentArea.setStyle("-fx-background-radius: 8; -fx-border-color: #e6e6e6; -fx-border-radius: 8; -fx-font-size: 13; -fx-font-family: Poppins;");
        Label charCount = new Label(rev.getComment().length() + " / 500");
        charCount.setStyle("-fx-font-size: 11; -fx-text-fill: #717182; -fx-font-family: Poppins;");
        commentArea.textProperty().addListener((obs, old, nw) -> {
            if (nw.length() > 500) {
                commentArea.setText(old);
            } else {
                charCount.setText(nw.length() + " / 500");
            }
        });

        // Error label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #d4183d;" +
                " -fx-background-color: #fef2f2; -fx-background-radius: 6; -fx-padding: 8 12;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

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
            String comment = commentArea.getText().trim();
            if (comment.isEmpty()) {
                errorLabel.setText("Please enter a comment.");
                errorLabel.setVisible(true); errorLabel.setManaged(true);
                return;
            }
            try {
                dbController.updateReview(rev.getReviewID(), UserSession.getEmail(), selectedRating[0], comment);
                dialog.close();
                loadCustomerReviews();
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true); errorLabel.setManaged(true);
                HoverUtils.shake(errorLabel);
            }
        });

        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(4, 0, 0, 0));

        VBox charRow = new VBox(2, commentArea, charCount);

        VBox root = new VBox(12, titleLbl, ratingLbl, starBox, commentLbl, charRow, errorLabel, buttons);
        root.setStyle("-fx-background-color: white; -fx-padding: 28;");
        root.setPrefWidth(420);

        dialog.setScene(new javafx.scene.Scene(root));
        dialog.showAndWait();
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private double parseDouble(String s, double def) {
        try { return (s == null || s.isBlank()) ? def : Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private String nvl(String s) { return s == null ? "" : s; }

    /**
     * Displays a temporary error banner at the bottom of the main content area.
     * The banner auto-dismisses after 4 seconds — no native OS alert dialogs.
     *
     * @param msg the error message to display
     */
    private void showAlert(String msg) {
        javafx.scene.control.Label banner = new javafx.scene.control.Label(msg);
        banner.setStyle(
            "-fx-background-color: #fef2f2; -fx-text-fill: #b91c1c;" +
            " -fx-background-radius: 8; -fx-padding: 12 20;" +
            " -fx-border-color: #fecaca; -fx-border-radius: 8; -fx-font-size: 13;");
        banner.setWrapText(true);
        banner.setMaxWidth(440);
        StackPane.setAlignment(banner, Pos.BOTTOM_CENTER);
        StackPane.setMargin(banner, new Insets(0, 0, 28, 0));
        mainStack.getChildren().add(banner);

        // Auto-remove the banner after 4 seconds
        PauseTransition dismiss = new PauseTransition(Duration.seconds(4));
        dismiss.setOnFinished(e -> mainStack.getChildren().remove(banner));
        dismiss.play();
    }
}
