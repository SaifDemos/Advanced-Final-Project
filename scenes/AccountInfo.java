package scenes;

import java.sql.*;
import javafx.stage.*;
import utility.Assistor;
import utility.DBUtil;
import utility.SceneAnimator;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

public class AccountInfo {
    private String userEmail;
    private String userId;
    private ImageView eyeIcon;
    private PasswordField passField;
    private TextField passTextField;
    private boolean passwordVisible = false;

    public AccountInfo(String email, String id) {
        this.userEmail = email;
        this.userId = id;
    }

    public Scene getScene(Stage stage) {
        Label AccountInfoLabel = new Label("Account Information");
        AccountInfoLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label id = new Label("ID :");
        Label email = new Label("Email :");
        Label PhoneNumber = new Label("Phone Number :");

        Label idlLabel = new Label(userId);
        Label emaillLabel = new Label(userEmail);
        Label PasswordlLabel = new Label("?");
        Label PhoneNumberLabel = new Label("?");

        // Load current data from DB
        loadData(PasswordlLabel, PhoneNumberLabel);

        Button edit = new Button("Edit");
        Button Back = new Button("Back");

        edit.setGraphic(Assistor.createIcon("./images/edit.png", 24));

        Back.setGraphic(Assistor.createIcon("./images/back.png", 24));

        GridPane g7 = new GridPane();
        g7.add(AccountInfoLabel, 0, 0);
        GridPane.setColumnSpan(AccountInfoLabel, 2);
        GridPane.setHalignment(AccountInfoLabel, HPos.CENTER);

        g7.add(id, 0, 1);
        g7.add(email, 0, 2);
        g7.add(PhoneNumber, 0, 3);

        g7.add(idlLabel, 1, 1);
        g7.add(emaillLabel, 1, 2);
        g7.add(PhoneNumberLabel, 1, 3);

        g7.add(edit, 0, 5);
        g7.add(Back, 1, 5);

        g7.setVgap(20);
        g7.setHgap(20);

        g7.setAlignment(Pos.CENTER);

        edit.setOnAction(e -> {
            showEditDialog(stage, idlLabel, emaillLabel, PasswordlLabel, PhoneNumberLabel);
        });

        Back.setOnAction(e -> {
            StudentProfile studentprofile = new StudentProfile(userEmail);
            SceneAnimator.transition(stage, studentprofile.getScene(stage),
                    () -> stage.setTitle("AcademiX | My Profile"));
        });

        Scene scene = new Scene(Assistor.createWithBackground(g7, 0.2, 815, 665), 815, 600);
        scene.getStylesheets().add("file:./assets/style.css");
        return scene;
    }

    private void loadData(Label pass, Label phone) {
        String sql = "SELECT PASSWORD, PHONE FROM STUDENT WHERE EMAIL = ?";
        try (Connection con = DBUtil.dbConnect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, userEmail);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                pass.setText(rs.getString("PASSWORD"));
                phone.setText(rs.getString("PHONE"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEditDialog(Stage ownerStage, Label idLabel, Label emailLabel, Label passwordLabel,
            Label phoneLabel) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);
        dialog.setTitle("Edit Account Information");

        Label id = new Label("ID :");
        Label email = new Label("Email :");
        Label Password = new Label("Password :");
        Label PhoneNumber = new Label("Phone Number :");

        // ID field is NOT editable (same as email)
        TextField idField = new TextField(idLabel.getText());
        idField.setEditable(false);
        idField.setDisable(true);

        // Email field is NOT editable as requested
        TextField emailField = new TextField(emailLabel.getText());
        emailField.setEditable(false);
        emailField.setDisable(true); // Visually indicates it's locked

        passField = new PasswordField();
        passField.setPromptText("Enter Your new Password");

        passTextField = new TextField();
        passTextField.setPromptText("Enter Your new Password");
        passTextField.setVisible(false);
        passTextField.setManaged(false);

        eyeIcon = new ImageView(new Image("file:./images/visible.png"));
        eyeIcon.setFitHeight(20);
        eyeIcon.setFitWidth(20);
        Button toggleBtn = new Button();
        toggleBtn.setGraphic(eyeIcon);
        toggleBtn.setStyle(
                "-fx-background-color: transparent; -fx-border-color: transparent; -fx-pref-height: 25px; -fx-pref-width: 25px;");
        toggleBtn.setOnAction(e -> togglePasswordVisibility());

        HBox passBox = new HBox(5, passField, passTextField, toggleBtn);
        HBox.setHgrow(passField, Priority.ALWAYS);
        HBox.setHgrow(passTextField, Priority.ALWAYS);

        TextField phoneField = new TextField(phoneLabel.getText());

        // Real-time listener to restrict phone input to 11 digits
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (phoneField.getText().length() > 11) {
                phoneField.setText(phoneField.getText().substring(0, 11));
            }
        });

        Button confirm = new Button("Confirm");
        Button cancel = new Button("Cancel");

        Image icon = new Image("file:./images/AcademiX-Icon.png");
        ImageView iconView = new ImageView(icon);
        iconView.setFitHeight(48);
        iconView.setFitWidth(48);

        GridPane grid = new GridPane();
        grid.add(id, 0, 1);
        grid.add(email, 0, 2);
        grid.add(Password, 0, 3);
        grid.add(PhoneNumber, 0, 4);
        grid.add(idField, 1, 1);
        grid.add(emailField, 1, 2);
        grid.add(passBox, 1, 3);
        grid.add(phoneField, 1, 4);
        grid.add(confirm, 0, 6);
        grid.add(cancel, 1, 6);

        grid.setVgap(15);
        grid.setHgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(30));

        confirm.setOnAction(e -> {
            String newPass = passwordVisible ? passTextField.getText() : passField.getText();
            String newPhone = phoneField.getText();

            if (newPass.isEmpty() || newPhone.isEmpty()) {
                Assistor.showErrorAlert("Error", "Empty Fields", "Fields cannot be empty!");
            } else if (newPhone.length() != 11) {
                Assistor.showErrorAlert("Error", "Invalid Phone Number", "Phone number must be exactly 11 digits!");
            } else {
                // Database: Update Data in Oracle
                String updateSql = "UPDATE STUDENT SET PASSWORD = ?, PHONE = ? WHERE EMAIL = ?";
                try (Connection con = DBUtil.dbConnect();
                        PreparedStatement pstmt = con.prepareStatement(updateSql)) {

                    pstmt.setString(1, newPass);
                    pstmt.setString(2, newPhone);
                    pstmt.setString(3, userEmail);

                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        passwordLabel.setText(newPass);
                        phoneLabel.setText(newPhone);
                        dialog.close();
                    }
                } catch (SQLException ex) {
                    Assistor.showErrorAlert("Error", "Database Error", "Database Error: " + ex.getMessage());
                }
            }
        });

        cancel.setOnAction(e -> dialog.close());

        Scene dialogScene = new Scene(grid);
        dialogScene.getStylesheets().add("file:./assets/style.css");
        dialog.setScene(dialogScene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passTextField.setText(passField.getText());
            passField.setVisible(false);
            passField.setManaged(false);
            passTextField.setVisible(true);
            passTextField.setManaged(true);
            eyeIcon.setImage(new Image("file:./images/invisible.png"));
        } else {
            passField.setText(passTextField.getText());
            passField.setVisible(true);
            passField.setManaged(true);
            passTextField.setVisible(false);
            passTextField.setManaged(false);
            eyeIcon.setImage(new Image("file:./images/visible.png"));
        }
    }
}