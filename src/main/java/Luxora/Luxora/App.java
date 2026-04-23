package Luxora.Luxora;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Ensure DB tables exist before any screen is shown
        dbController.createUsersTable();
        dbController.createProductsTable();
        dbController.migrateAddCreatedBy();
        dbController.createCartTable();
        dbController.createOrdersTable();
        dbController.createDeliveryTables();
        dbController.createInventoryTables();
        dbController.createReviewsTables();
        dbController.seedDefaultUsers();
        dbController.seedDefaultDrivers();

        Parent root = FXMLLoader.load(getClass().getResource("/Start.fxml"));
        Scene scene = new Scene(root);
        scene.setFill(Color.web("#162535"));
        stage.setScene(scene);
        stage.setTitle("Luxora");
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}
