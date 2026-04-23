package Luxora.Luxora;
import javafx.beans.property.*;
public class Warehouse {
    private final IntegerProperty warehouseID;
    private final StringProperty  name;
    private final StringProperty  location;
    private final StringProperty  contactInfo;
    private final IntegerProperty capacity; // -1 = unlimited
    public Warehouse(int id, String name, String location, String contactInfo, int capacity) {
        this.warehouseID = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.location = new SimpleStringProperty(location);
        this.contactInfo = new SimpleStringProperty(contactInfo);
        this.capacity = new SimpleIntegerProperty(capacity);
    }
    public int    getWarehouseID()   { return warehouseID.get(); }
    public String getName()          { return name.get(); }
    public String getLocation()      { return location.get(); }
    public String getContactInfo()   { return contactInfo.get(); }
    public int    getCapacity()      { return capacity.get(); }
    public IntegerProperty warehouseIDProperty()  { return warehouseID; }
    public StringProperty  nameProperty()         { return name; }
    public StringProperty  locationProperty()     { return location; }
    public StringProperty  contactInfoProperty()  { return contactInfo; }
    public IntegerProperty capacityProperty()     { return capacity; }
    public String getCapacityDisplay() { return capacity.get() < 0 ? "Unlimited" : String.valueOf(capacity.get()); }
}
