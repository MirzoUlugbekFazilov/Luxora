package Luxora.Luxora;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

public class ManagerRegisterController {

    @FXML private BorderPane    rootPane;
    @FXML private Label         headerIconLabel;
    @FXML private Label         nameIconLabel, emailIconLabel, passwordIconLabel, confirmPasswordIconLabel;
    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisibleField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField     confirmPasswordVisibleField;
    @FXML private Button        eyeToggleBtn;
    @FXML private Button        eyeToggleBtn2;
    @FXML private Label         messageLabel;

    private boolean passwordShown        = false;
    private boolean confirmPasswordShown = false;

    private static final String USER_PATH =
        "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2 " +
        "M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z";
    private static final String MAIL_PATH =
        "M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z " +
        "M22 6l-10 7L2 6";
    private static final String LOCK_PATH =
        "M19 11H5a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2z " +
        "M7 11V7a5 5 0 0 1 10 0v4";
    private static final String EYE_PATH =
        "M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z " +
        "M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z";
    private static final String EYE_OFF_PATH =
        "M9.88 9.88a3 3 0 1 0 4.24 4.24 " +
        "M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68 " +
        "M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61 " +
        "M2 2L22 22";

    @FXML
    public void initialize() {
        // Header circle — user/person icon
        SVGPath headerUser = new SVGPath();
        headerUser.setContent(USER_PATH);
        headerUser.setStroke(Color.web("#1e293b"));
        headerUser.setFill(Color.TRANSPARENT);
        headerUser.setStrokeWidth(1.5);
        double hs = 30.0 / 24.0;
        headerUser.setScaleX(hs); headerUser.setScaleY(hs);
        headerIconLabel.setGraphic(headerUser);
        headerIconLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        setupFieldIcon(nameIconLabel,            USER_PATH);
        setupFieldIcon(emailIconLabel,           MAIL_PATH);
        setupFieldIcon(passwordIconLabel,        LOCK_PATH);
        setupFieldIcon(confirmPasswordIconLabel, LOCK_PATH);
        setEyeIcon(eyeToggleBtn,  false);
        setEyeIcon(eyeToggleBtn2, false);
        HoverUtils.apply(rootPane);
    }

    private void setupFieldIcon(Label label, String svgPath) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setStroke(Color.web("#94a3b8"));
        icon.setFill(Color.TRANSPARENT);
        icon.setStrokeWidth(1.5);
        double s = 14.0 / 24.0;
        icon.setScaleX(s); icon.setScaleY(s);
        label.setGraphic(icon);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    private void setEyeIcon(Button btn, boolean eyeOff) {
        SVGPath eye = new SVGPath();
        eye.setContent(eyeOff ? EYE_OFF_PATH : EYE_PATH);
        eye.setStroke(Color.web("#94a3b8"));
        eye.setFill(Color.TRANSPARENT);
        eye.setStrokeWidth(1.8);
        double s = 14.0 / 24.0;
        eye.setScaleX(s); eye.setScaleY(s);
        btn.setGraphic(eye);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @FXML
    public void handleEyeToggle() {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisibleField.setText(passwordField.getText());
            passwordField.setVisible(false);        passwordField.setManaged(false);
            passwordVisibleField.setVisible(true);  passwordVisibleField.setManaged(true);
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordVisibleField.setVisible(false); passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);         passwordField.setManaged(true);
        }
        setEyeIcon(eyeToggleBtn, passwordShown);
    }

    @FXML
    public void handleEyeToggle2() {
        confirmPasswordShown = !confirmPasswordShown;
        if (confirmPasswordShown) {
            confirmPasswordVisibleField.setText(confirmPasswordField.getText());
            confirmPasswordField.setVisible(false);        confirmPasswordField.setManaged(false);
            confirmPasswordVisibleField.setVisible(true);  confirmPasswordVisibleField.setManaged(true);
        } else {
            confirmPasswordField.setText(confirmPasswordVisibleField.getText());
            confirmPasswordVisibleField.setVisible(false); confirmPasswordVisibleField.setManaged(false);
            confirmPasswordField.setVisible(true);         confirmPasswordField.setManaged(true);
        }
        setEyeIcon(eyeToggleBtn2, confirmPasswordShown);
    }

    @FXML
    public void handleRegister(ActionEvent event) throws Exception {
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordShown
                ? passwordVisibleField.getText().trim()
                : passwordField.getText().trim();
        String confirm  = confirmPasswordShown
                ? confirmPasswordVisibleField.getText().trim()
                : confirmPasswordField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        } else if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address.");
            return;
        } else if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        } else if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        } else {
            dbController.createUser(name, email, password, 2);
            Parent root = FXMLLoader.load(getClass().getResource("/ManagerLogin.fxml"));
            rootPane.getScene().setRoot(root);
        }
    }

    @FXML
    public void goBack(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ManagerLogin.fxml"));
        rootPane.getScene().setRoot(root);
    }

    @FXML
    public void goToLogin(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ManagerLogin.fxml"));
        rootPane.getScene().setRoot(root);
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12;");
        messageLabel.setText(msg);
        HoverUtils.shake(messageLabel);
    }
}
