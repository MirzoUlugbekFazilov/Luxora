package Luxora.Luxora;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {

    private final StringProperty  name     = new SimpleStringProperty();
    private final StringProperty  email    = new SimpleStringProperty();
    private final IntegerProperty userType = new SimpleIntegerProperty();

    public User(String name, String email, int userType) {
        this.name.set(name);
        this.email.set(email);
        this.userType.set(userType);
    }

    public String getName()     { return name.get(); }
    public String getEmail()    { return email.get(); }
    public int    getUserType() { return userType.get(); }

    public StringProperty  nameProperty()     { return name; }
    public StringProperty  emailProperty()    { return email; }
    public IntegerProperty userTypeProperty() { return userType; }

    public String getRoleLabel() {
        return switch (userType.get()) {
            case 1  -> "Customer";
            case 2  -> "Product Manager";
            case 3  -> "Admin";
            default -> "Unknown";
        };
    }
}
