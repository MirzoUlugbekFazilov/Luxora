package Luxora.Luxora;
import javafx.beans.property.*;
public class WarehouseStock {
    private final IntegerProperty stockID;
    private final IntegerProperty warehouseID;
    private final StringProperty  warehouseName;
    private final IntegerProperty productID;
    private final StringProperty  productName;
    private final StringProperty  category;
    private final IntegerProperty quantity;
    private final IntegerProperty minThreshold;
    private final StringProperty  lastRestockDate;
    private final IntegerProperty reorderQty;
    public WarehouseStock(int stockID, int warehouseID, String warehouseName,
                          int productID, String productName, String category,
                          int quantity, int minThreshold, String lastRestockDate, int reorderQty) {
        this.stockID = new SimpleIntegerProperty(stockID);
        this.warehouseID = new SimpleIntegerProperty(warehouseID);
        this.warehouseName = new SimpleStringProperty(warehouseName);
        this.productID = new SimpleIntegerProperty(productID);
        this.productName = new SimpleStringProperty(productName);
        this.category = new SimpleStringProperty(category == null ? "" : category);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.minThreshold = new SimpleIntegerProperty(minThreshold);
        this.lastRestockDate = new SimpleStringProperty(lastRestockDate == null ? "" : lastRestockDate);
        this.reorderQty = new SimpleIntegerProperty(reorderQty);
    }
    public int    getStockID()        { return stockID.get(); }
    public int    getWarehouseID()    { return warehouseID.get(); }
    public String getWarehouseName()  { return warehouseName.get(); }
    public int    getProductID()      { return productID.get(); }
    public String getProductName()    { return productName.get(); }
    public String getCategory()       { return category.get(); }
    public int    getQuantity()       { return quantity.get(); }
    public int    getMinThreshold()   { return minThreshold.get(); }
    public String getLastRestockDate(){ return lastRestockDate.get(); }
    public int    getReorderQty()     { return reorderQty.get(); }
    public boolean isLowStock()       { return quantity.get() < minThreshold.get(); }
    public IntegerProperty stockIDProperty()      { return stockID; }
    public IntegerProperty warehouseIDProperty()  { return warehouseID; }
    public StringProperty  warehouseNameProperty(){ return warehouseName; }
    public IntegerProperty productIDProperty()    { return productID; }
    public StringProperty  productNameProperty()  { return productName; }
    public StringProperty  categoryProperty()     { return category; }
    public IntegerProperty quantityProperty()     { return quantity; }
    public IntegerProperty minThresholdProperty() { return minThreshold; }
    public StringProperty  lastRestockDateProperty(){ return lastRestockDate; }
    public IntegerProperty reorderQtyProperty()   { return reorderQty; }
}
