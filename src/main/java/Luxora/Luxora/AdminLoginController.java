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

public class AdminLoginController {

    @FXML private BorderPane    rootPane;
    @FXML private Label         emailIconLabel, passwordIconLabel;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisibleField;
    @FXML private Button        eyeToggleBtn;
    @FXML private Label         messageLabel;

    private boolean passwordShown = false;

    private static final String EYE_PATH =
        "M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z " +
        "M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z";
    private static final String EYE_OFF_PATH =
        "M9.88 9.88a3 3 0 1 0 4.24 4.24 " +
        "M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68 " +
        "M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61 " +
        "M2 2L22 22";

    private static final String MAIL_PATH =
        "M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z " +
        "M22 6l-10 7L2 6";
    private static final String LOCK_PATH =
        "M19 11H5a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2z " +
        "M7 11V7a5 5 0 0 1 10 0v4";

    @FXML
    public void initialize() {
        setupFieldIcon(emailIconLabel,    MAIL_PATH);
        setupFieldIcon(passwordIconLabel, LOCK_PATH);
        setEyeIcon(false);
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
        setEyeIcon(passwordShown);
    }

    private void setEyeIcon(boolean eyeOff) {
        SVGPath eye = new SVGPath();
        eye.setContent(eyeOff ? EYE_OFF_PATH : EYE_PATH);
        eye.setStroke(Color.web("#94a3b8"));
        eye.setFill(Color.TRANSPARENT);
        eye.setStrokeWidth(1.8);
        double s = 14.0 / 24.0;
        eye.setScaleX(s); eye.setScaleY(s);
        eyeToggleBtn.setGraphic(eye);
        eyeToggleBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @FXML
    public void handleLogin(ActionEvent event) throws Exception {
        String email    = emailField.getText().trim();
        String password = passwordShown
                ? passwordVisibleField.getText().trim()
                : passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password");
            return;
        } else if (dbController.emailchecker(email) == false) {
            showError("This email does not exist");
            return;
        } else if (!dbController.typechecker(email).equals("admin")) {
            showError("This is not admin account");
        } else if (dbController.loginchecker(email, password) == true && dbController.typechecker(email).equals("admin")) {

            // Save session
            UserSession.set(dbController.getUserName(email), email, "Administrator");

            // Login success → open dashboard
            javafx.scene.Scene scene = rootPane.getScene();
            javafx.stage.Stage stage = (javafx.stage.Stage) scene.getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/AdminDashboard.fxml"));
            scene.setRoot(root);
            stage.setMaximized(true);

        } else {
            showError("Incorrect password");
        }
    }

    @FXML
    public void goBack(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Start.fxml"));
        rootPane.getScene().setRoot(root);
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12;");
        messageLabel.setText(msg);
        HoverUtils.shake(messageLabel);
    }
}
