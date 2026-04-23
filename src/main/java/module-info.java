module Luxora.Luxora {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires java.xml;
    requires org.xerial.sqlitejdbc;
    opens Luxora.Luxora to javafx.fxml;
    exports Luxora.Luxora;
}
