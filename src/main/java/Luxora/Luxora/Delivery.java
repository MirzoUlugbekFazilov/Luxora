package Luxora.Luxora;

/**
 * Model class for a delivery record.
 * Represents a shipment associated with an order, managed by the admin/logistics team.
 */
public class Delivery {

    private final String deliveryId;
    private final String orderRef;
    private final String deliveryAddress;
    private final String assignedDriver;
    private final String status;
    private final String createdTime;

    public Delivery(String deliveryId, String orderRef, String deliveryAddress,
                    String assignedDriver, String status, String createdTime) {
        this.deliveryId      = deliveryId;
        this.orderRef        = orderRef;
        this.deliveryAddress = deliveryAddress;
        this.assignedDriver  = assignedDriver;
        this.status          = status;
        this.createdTime     = createdTime;
    }

    public String getDeliveryId()      { return deliveryId; }
    public String getOrderRef()        { return orderRef; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getAssignedDriver()  { return assignedDriver == null ? "" : assignedDriver; }
    public String getStatus()          { return status; }
    public String getCreatedTime()     { return createdTime; }
}
