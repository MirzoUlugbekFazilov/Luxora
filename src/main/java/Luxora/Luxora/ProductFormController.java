package Luxora.Luxora;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ProductFormController {

    // ── FXML fields ────────────────────────────────────────────────────────────
    @FXML private VBox     rootVBox;
    @FXML private Label    formTitle;
    @FXML private TextField nameField;

    // Default category checkboxes
    @FXML private CheckBox cbElectronics, cbFashion, cbHomeLiving, cbBeauty;
    @FXML private CheckBox cbSports, cbToysKids, cbBooksMedia, cbGroceries;
    @FXML private CheckBox cbHealth, cbAutomotive, cbOffice, cbPetSupplies;
    @FXML private CheckBox cbTools, cbJewelry, cbTravel;

    // Custom category controls
    @FXML private TextField customCategoryField;
    @FXML private FlowPane  customCategoriesPane;

    @FXML private TextArea           descriptionField;
    @FXML private TextField          priceField;
    @FXML private TextField          quantityField;
    @FXML private ComboBox<String>   statusCombo;
    @FXML private VBox               dropZone;
    @FXML private FlowPane           thumbnailPane;
    @FXML private Label              imageErrorLabel;
    @FXML private Label              formErrorLabel;
    @FXML private Button             saveBtn;

    // ── State ──────────────────────────────────────────────────────────────────
    private Product  editingProduct;
    private Runnable onSave;
    private final List<String> imageBase64List = new ArrayList<>();

    // Default categories — order must match allCheckBoxes array
    private static final String[] DEFAULT_LABELS = {
        "Electronics", "Fashion", "Home & Living", "Beauty & Personal Care",
        "Sports & Outdoors", "Toys & Kids", "Books & Media", "Groceries & Food",
        "Health & Wellness", "Automotive", "Office & Stationery", "Pet Supplies",
        "Tools & Hardware", "Jewelry & Accessories", "Travel & Luggage"
    };

    // All default checkboxes in same order as DEFAULT_LABELS
    private CheckBox[] allCheckBoxes;

    // ── Initialisation ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        allCheckBoxes = new CheckBox[]{
            cbElectronics, cbFashion, cbHomeLiving, cbBeauty,
            cbSports, cbToysKids, cbBooksMedia, cbGroceries,
            cbHealth, cbAutomotive, cbOffice, cbPetSupplies,
            cbTools, cbJewelry, cbTravel
        };

        statusCombo.setItems(FXCollections.observableArrayList("active", "inactive"));
        statusCombo.setValue("active");

        HoverUtils.apply(rootVBox);

        // Drop zone – click to browse
        dropZone.setOnMouseClicked(e -> browseFiles());

        // Drop zone – drag from file system
        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
                dropZone.setStyle(dropZoneStyle(true));
            }
            event.consume();
        });
        dropZone.setOnDragExited(event -> dropZone.setStyle(dropZoneStyle(false)));
        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                for (File f : db.getFiles()) addImageFile(f);
            }
            dropZone.setStyle(dropZoneStyle(false));
            event.setDropCompleted(true);
            event.consume();
        });
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public void setProduct(Product p) {
        editingProduct = p;
        formTitle.setText("Edit Product");
        saveBtn.setText("Update Product");

        nameField.setText(p.getName());
        descriptionField.setText(p.getDescription());
        priceField.setText(String.valueOf(p.getPrice()));
        quantityField.setText(String.valueOf(p.getQuantity()));

        // Set status
        String status = p.getStatus();
        statusCombo.setValue("inactive".equalsIgnoreCase(status) ? "inactive" : "active");

        // Set category checkboxes — match against defaults; add unknowns as custom
        if (p.getCategory() != null && !p.getCategory().isBlank()) {
            for (String cat : p.getCategory().split(",")) {
                String trimmed = cat.trim();
                boolean found = false;
                for (int i = 0; i < DEFAULT_LABELS.length; i++) {
                    if (DEFAULT_LABELS[i].equalsIgnoreCase(trimmed)) {
                        allCheckBoxes[i].setSelected(true);
                        found = true;
                        break;
                    }
                }
                if (!found && !trimmed.isEmpty()) {
                    CheckBox customCb = new CheckBox(trimmed);
                    customCb.setSelected(true);
                    customCb.setStyle("-fx-font-size: 12; -fx-text-fill: #374151;");
                    customCategoriesPane.getChildren().add(customCb);
                }
            }
        }

        // Load existing images
        if (p.getImages() != null && !p.getImages().isBlank()) {
            for (String b64 : p.getImages().split("\\|", -1)) {
                if (!b64.isBlank()) imageBase64List.add(b64.trim());
            }
            refreshThumbnails();
        }
    }

    public void setOnSave(Runnable callback) { this.onSave = callback; }

    // ── Image handling ─────────────────────────────────────────────────────────

    private void browseFiles() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Product Images");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        List<File> files = fc.showOpenMultipleDialog(dropZone.getScene().getWindow());
        if (files != null) {
            for (File f : files) addImageFile(f);
        }
    }

    private void addImageFile(File file) {
        if (!isImageFile(file)) {
            showImageError("Unsupported file: " + file.getName() + ". Use PNG/JPG/JPEG.");
            return;
        }
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            Image img = new Image(new ByteArrayInputStream(bytes));
            if (img.isError() || img.getWidth() <= 0) {
                showImageError("Could not read image: " + file.getName());
                return;
            }
            // Enforce 1:1 square ratio (5% tolerance)
            double ratio = img.getWidth() / img.getHeight();
            if (Math.abs(ratio - 1.0) > 0.05) {
                showImageError("\"" + file.getName() + "\" is not square (" +
                        (int) img.getWidth() + "×" + (int) img.getHeight() +
                        "). Please use a 1:1 image.");
                return;
            }
            imageErrorLabel.setText("");
            imageBase64List.add(Base64.getEncoder().encodeToString(bytes));
            refreshThumbnails();
        } catch (Exception e) {
            showImageError("Error reading file: " + e.getMessage());
        }
    }

    private void refreshThumbnails() {
        thumbnailPane.getChildren().clear();
        for (int i = 0; i < imageBase64List.size(); i++) {
            final int idx = i;
            final String b64 = imageBase64List.get(i);

            ImageView iv = new ImageView();
            iv.setFitWidth(72); iv.setFitHeight(72); iv.setPreserveRatio(false);
            try {
                byte[] dec = Base64.getDecoder().decode(b64);
                iv.setImage(new Image(new ByteArrayInputStream(dec)));
            } catch (Exception ignored) {}

            Button removeBtn = new Button("✕");
            removeBtn.setStyle(
                    "-fx-background-color: rgba(239,68,68,0.85); -fx-text-fill: white;" +
                    " -fx-background-radius: 20; -fx-font-size: 10; -fx-padding: 0 5;" +
                    " -fx-cursor: hand; -fx-min-width: 18; -fx-min-height: 18;");
            removeBtn.setOnAction(e -> { imageBase64List.remove(idx); refreshThumbnails(); });

            Label dragHint = new Label("⠿");
            dragHint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");

            VBox thumb = new VBox(3);
            thumb.setAlignment(Pos.CENTER);
            thumb.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8;" +
                    " -fx-border-color: #e2e8f0; -fx-border-radius: 8;" +
                    " -fx-padding: 4; -fx-cursor: grab;");
            thumb.getChildren().addAll(iv, dragHint);

            StackPane wrapper = new StackPane(thumb, removeBtn);
            StackPane.setAlignment(removeBtn, Pos.TOP_RIGHT);

            wrapper.setOnDragDetected(event -> {
                Dragboard db = wrapper.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(String.valueOf(imageBase64List.indexOf(b64)));
                db.setContent(cc);
                db.setDragView(iv.snapshot(null, null));
                event.consume();
            });
            wrapper.setOnDragOver(event -> {
                if (event.getDragboard().hasString() && event.getGestureSource() != wrapper) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    wrapper.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 8;" +
                            " -fx-border-color: #3b82f6; -fx-border-radius: 8; -fx-border-width: 2;");
                }
                event.consume();
            });
            wrapper.setOnDragExited(event -> wrapper.setStyle(""));
            wrapper.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean ok = false;
                if (db.hasString()) {
                    try {
                        int fromIdx = Integer.parseInt(db.getString());
                        int toIdx   = imageBase64List.indexOf(b64);
                        if (fromIdx >= 0 && fromIdx < imageBase64List.size() && fromIdx != toIdx) {
                            String moved = imageBase64List.remove(fromIdx);
                            imageBase64List.add(toIdx, moved);
                            refreshThumbnails();
                        }
                        ok = true;
                    } catch (NumberFormatException ignored) {}
                }
                event.setDropCompleted(ok);
                event.consume();
            });
            wrapper.setOnDragDone(event -> event.consume());

            thumbnailPane.getChildren().add(wrapper);
        }
    }

    // ── Form save / cancel ─────────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        formErrorLabel.setText("");

        String name     = nameField.getText() == null ? "" : nameField.getText().trim();
        String desc     = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
        String priceStr = priceField.getText() == null ? "" : priceField.getText().trim();
        String qtyStr   = quantityField.getText() == null ? "" : quantityField.getText().trim();
        String status   = statusCombo.getValue() == null ? "active" : statusCombo.getValue();

        // Build category string from default checkboxes + custom ones
        List<String> selectedCats = new ArrayList<>();
        for (int i = 0; i < allCheckBoxes.length; i++) {
            if (allCheckBoxes[i].isSelected()) selectedCats.add(DEFAULT_LABELS[i]);
        }
        for (javafx.scene.Node node : customCategoriesPane.getChildren()) {
            if (node instanceof CheckBox cb && cb.isSelected()) {
                selectedCats.add(cb.getText());
            }
        }
        String category = String.join(", ", selectedCats);

        // Validation
        if (name.isEmpty())      { showFormError("Product name is required."); return; }
        if (category.isEmpty())  { showFormError("At least one category is required."); return; }
        if (priceStr.isEmpty())  { showFormError("Price is required."); return; }
        if (qtyStr.isEmpty())    { showFormError("Stock quantity is required."); return; }

        double price;
        try { price = Double.parseDouble(priceStr); if (price <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { showFormError("Price must be a positive number (e.g. 29.99)."); return; }

        int qty;
        try { qty = Integer.parseInt(qtyStr); if (qty < 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { showFormError("Stock quantity must be a non-negative integer (0 or more)."); return; }

        String images = String.join("|", imageBase64List);

        try {
            if (editingProduct == null) {
                dbController.addProduct(name, category, desc, price, qty, status, images,
                        UserSession.getEmail());
            } else {
                dbController.updateProduct(
                        editingProduct.getProductID(), name, category, desc, price, qty, status, images);
            }
            if (onSave != null) onSave.run();
            closeStage();
        } catch (Exception e) {
            showFormError("Save failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddCustomCategory() {
        String text = customCategoryField.getText() == null ? "" : customCategoryField.getText().trim();
        if (text.isEmpty()) return;

        // If it matches a default category, just check that box
        for (int i = 0; i < DEFAULT_LABELS.length; i++) {
            if (DEFAULT_LABELS[i].equalsIgnoreCase(text)) {
                allCheckBoxes[i].setSelected(true);
                customCategoryField.clear();
                return;
            }
        }

        // Avoid duplicates in custom pane
        for (javafx.scene.Node node : customCategoriesPane.getChildren()) {
            if (node instanceof CheckBox cb && cb.getText().equalsIgnoreCase(text)) {
                cb.setSelected(true);
                customCategoryField.clear();
                return;
            }
        }

        CheckBox newCb = new CheckBox(text);
        newCb.setSelected(true);
        newCb.setStyle("-fx-font-size: 12; -fx-text-fill: #374151;");
        customCategoriesPane.getChildren().add(newCb);
        customCategoryField.clear();
    }

    @FXML
    private void handleCancel() { closeStage(); }

    // ── Utilities ──────────────────────────────────────────────────────────────

    private void showFormError(String msg) {
        formErrorLabel.setText(msg);
        HoverUtils.shake(formErrorLabel);
    }

    private void showImageError(String msg) {
        imageErrorLabel.setText(msg);
        HoverUtils.shake(imageErrorLabel);
    }

    private boolean isImageFile(File f) {
        String n = f.getName().toLowerCase();
        return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")
                || n.endsWith(".gif") || n.endsWith(".bmp");
    }

    private void closeStage() {
        ((Stage) formTitle.getScene().getWindow()).close();
    }

    private String dropZoneStyle(boolean active) {
        return active
                ? "-fx-border-color: #3b82f6; -fx-border-style: dashed; -fx-border-width: 2;" +
                  " -fx-border-radius: 10; -fx-background-color: #eff6ff; -fx-background-radius: 10;" +
                  " -fx-cursor: hand; -fx-padding: 20;"
                : "-fx-border-color: #d1d5db; -fx-border-style: dashed; -fx-border-width: 2;" +
                  " -fx-border-radius: 10; -fx-background-color: white; -fx-background-radius: 10;" +
                  " -fx-cursor: hand; -fx-padding: 20;";
    }
}
