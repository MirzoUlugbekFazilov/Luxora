package Luxora.Luxora;

import javafx.beans.property.*;

/**
 * Model class for a customer order.
 * Uses JavaFX properties for TableView binding (consistent with Product/User pattern).
 */
public class Order {

    private final IntegerProperty orderID;
    private final StringProperty  orderCode;
    private final StringProperty  userEmail;
    private final DoubleProperty  total;
    private final StringProperty  status;
    private final StringProperty  orderDate;
    private final IntegerProperty itemCount;

    public Order(int orderID, String orderCode, String userEmail,
                 double total, String status, String orderDate, int itemCount) {
        this.orderID   = new SimpleIntegerProperty(orderID);
        this.orderCode = new SimpleStringProperty(orderCode);
        this.userEmail = new SimpleStringProperty(userEmail);
        this.total     = new SimpleDoubleProperty(total);
        this.status    = new SimpleStringProperty(status);
        this.orderDate = new SimpleStringProperty(orderDate);
        this.itemCount = new SimpleIntegerProperty(itemCount);
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public int    getOrderID()   { return orderID.get(); }
    public String getOrderCode() { return orderCode.get(); }
    public String getUserEmail() { return userEmail.get(); }
    public double getTotal()     { return total.get(); }
    public String getStatus()    { return status.get(); }
    public String getOrderDate() { return orderDate.get(); }
    public int    getItemCount() { return itemCount.get(); }

    // ── Property accessors (for TableView binding) ────────────────────────────
    public IntegerProperty orderIDProperty()   { return orderID; }
    public StringProperty  orderCodeProperty() { return orderCode; }
    public StringProperty  userEmailProperty() { return userEmail; }
    public DoubleProperty  totalProperty()     { return total; }
    public StringProperty  statusProperty()    { return status; }
    public StringProperty  orderDateProperty() { return orderDate; }
    public IntegerProperty itemCountProperty() { return itemCount; }
}
