package utility;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import java.util.Optional;

public class Assistor {

    // Create icon for buttons
    public static ImageView createIcon(String imagePath, double size) {
        Image img = new Image("file:" + imagePath);
        ImageView iconView = new ImageView(img);
        iconView.setFitHeight(size);
        iconView.setFitWidth(size);
        return iconView;
    }

    private static final double DEFAULT_WIDTH = 815;
    private static final double DEFAULT_HEIGHT = 665;
    private static final Image BG_IMAGE = new Image("file:./images/background.gif");

    public static StackPane createWithBackground(Node content, double opacity) {
        return createWithBackground(content, opacity, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static StackPane createWithBackground(Node content, double opacity, double width, double height) {
        StackPane root = new StackPane();

        ImageView bgView = new ImageView(BG_IMAGE);
        bgView.setFitWidth(width);
        bgView.setFitHeight(height);
        bgView.setPreserveRatio(false);
        bgView.setOpacity(opacity);

        root.getChildren().addAll(bgView, content);
        return root;
    }

    // Success - with AcademiX icon (matches ManageStudents.java pattern)
    public static void showSuccessAlert(String title, String header, String message) {
        Alert alert = new Alert(AlertType.INFORMATION, message);
        alert.setTitle(title);
        alert.setHeaderText(header);

        // Set AcademiX icon (matches ManageStudents.java)
        Image icon = new Image("file:./images/AcademiX-Icon.png");
        ImageView iconView = new ImageView(icon);
        iconView.setFitHeight(48);
        iconView.setFitWidth(48);
        alert.getDialogPane().setGraphic(iconView);

        // Add stylesheet
        alert.getDialogPane().getStylesheets().add("./assets/style.css");

        alert.show();
        SceneAnimator.animateAlert(alert);
    }

    // Warning - default (no icon)
    public static void showWarningAlert(String title, String header, String message) {
        Alert alert = new Alert(AlertType.WARNING, message);
        alert.setTitle(title);
        alert.setHeaderText(header);

        // Add stylesheet only
        alert.getDialogPane().getStylesheets().add("./assets/style.css");

        alert.show();
        SceneAnimator.animateAlert(alert);
    }

    // Error - default (no icon)
    public static void showErrorAlert(String title, String header, String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.setTitle(title);
        alert.setHeaderText(header);

        // Add stylesheet only
        alert.getDialogPane().getStylesheets().add("./assets/style.css");

        alert.show();
        SceneAnimator.animateAlert(alert);
    }

    // Confirmation - with AcademiX icon
    public static Optional<ButtonType> showConfirmationAlert(String title, String header, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION, message);
        alert.setTitle(title);
        alert.setHeaderText(header);

        Image icon = new Image("file:./images/AcademiX-Icon.png");
        ImageView iconView = new ImageView(icon);
        iconView.setFitHeight(48);
        iconView.setFitWidth(48);
        alert.getDialogPane().setGraphic(iconView);

        alert.getDialogPane().getStylesheets().add("./assets/style.css");

        SceneAnimator.animateAlert(alert);
        return alert.showAndWait();
    }
}
