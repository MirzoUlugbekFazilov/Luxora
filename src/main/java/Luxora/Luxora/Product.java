package Luxora.Luxora;

import javafx.beans.property.*;

public class Product {

    private final IntegerProperty productID   = new SimpleIntegerProperty();
    private final StringProperty  name        = new SimpleStringProperty();
    private final StringProperty  category    = new SimpleStringProperty();
    private final StringProperty  description = new SimpleStringProperty();
    private final DoubleProperty  price       = new SimpleDoubleProperty();
    private final IntegerProperty quantity    = new SimpleIntegerProperty();
    private final StringProperty  status      = new SimpleStringProperty();
    private final StringProperty  images      = new SimpleStringProperty();
    private final StringProperty  createdBy   = new SimpleStringProperty();

    public Product() {}

    public Product(int productID, String name, String category, String description,
                   double price, int quantity, String status, String images, String createdBy) {
        this.productID.set(productID);
        this.name.set(name == null ? "" : name);
        this.category.set(category == null ? "" : category);
        this.description.set(description == null ? "" : description);
        this.price.set(price);
        this.quantity.set(quantity);
        this.status.set(status == null ? "active" : status);
        this.images.set(images == null ? "" : images);
        this.createdBy.set(createdBy == null ? "" : createdBy);
    }

    // ── productID ──────────────────────────────────────────────────────────────
    public int getProductID() { return productID.get(); }
    public void setProductID(int v) { productID.set(v); }
    public IntegerProperty productIDProperty() { return productID; }

    // ── name ───────────────────────────────────────────────────────────────────
    public String getName() { return name.get(); }
    public void setName(String v) { name.set(v == null ? "" : v); }
    public StringProperty nameProperty() { return name; }

    // ── category ───────────────────────────────────────────────────────────────
    public String getCategory() { return category.get(); }
    public void setCategory(String v) { category.set(v == null ? "" : v); }
    public StringProperty categoryProperty() { return category; }

    // ── description ────────────────────────────────────────────────────────────
    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v == null ? "" : v); }
    public StringProperty descriptionProperty() { return description; }

    // ── price ──────────────────────────────────────────────────────────────────
    public double getPrice() { return price.get(); }
    public void setPrice(double v) { price.set(v); }
    public DoubleProperty priceProperty() { return price; }

    // ── quantity ───────────────────────────────────────────────────────────────
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int v) { quantity.set(v); }
    public IntegerProperty quantityProperty() { return quantity; }

    // ── status ─────────────────────────────────────────────────────────────────
    public String getStatus() { return status.get(); }
    public void setStatus(String v) { status.set(v == null ? "active" : v); }
    public StringProperty statusProperty() { return status; }

    // ── images (pipe-separated base64 strings) ─────────────────────────────────
    public String getImages() { return images.get(); }
    public void setImages(String v) { images.set(v == null ? "" : v); }
    public StringProperty imagesProperty() { return images; }

    // ── createdBy ──────────────────────────────────────────────────────────────
    public String getCreatedBy() { return createdBy.get(); }
    public void setCreatedBy(String v) { createdBy.set(v == null ? "" : v); }
    public StringProperty createdByProperty() { return createdBy; }
}
