package Luxora.Luxora;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class StartController {

    @FXML private BorderPane rootPane;
    @FXML private VBox    customerCard;
    @FXML private VBox    managerCard;

    private static final String CARD_NORMAL =
            "-fx-background-color: white; -fx-background-radius: 12; " +
            "-fx-cursor: hand; -fx-padding: 36 28; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 24, 0, 0, 8);";

    private static final String CARD_HOVER =
            "-fx-background-color: #f1f5f9; -fx-background-radius: 12; " +
            "-fx-cursor: hand; -fx-padding: 36 28; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 24, 0, 0, 8);";

    @FXML
    public void customerLogin(MouseEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/CustomerLogin.fxml"));
        rootPane.getScene().setRoot(root);
    }

    @FXML
    public void managerLogin(MouseEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ManagerLogin.fxml"));
        rootPane.getScene().setRoot(root);
    }

    @FXML
    public void initialize() {
        HoverUtils.apply(rootPane);
    }

    @FXML
    public void onCustomerHover(MouseEvent event) {
        customerCard.setStyle(CARD_HOVER);
    }

    @FXML
    public void onCustomerExit(MouseEvent event) {
        customerCard.setStyle(CARD_NORMAL);
    }

    @FXML
    public void onManagerHover(MouseEvent event) {
        managerCard.setStyle(CARD_HOVER);
    }

    @FXML
    public void onManagerExit(MouseEvent event) {
        managerCard.setStyle(CARD_NORMAL);
    }

    @FXML
    public void goToAdmin(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/AdminLogin.fxml"));
        rootPane.getScene().setRoot(root);
    }
}
