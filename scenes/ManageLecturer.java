package scenes;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utility.Assistor;
import utility.DBUtil;
import utility.SceneAnimator;
import javafx.scene.layout.Region;
import classes.*;
import java.sql.*;
import java.util.Optional;

public class ManageLecturer {

    private boolean isNumberRegisteredLec(String phone) {
        String sql = "SELECT COUNT(*) FROM LECTURER WHERE PHONE = ?";
        try (Connection con = DBUtil.dbConnect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isNumberRegisteredStud(String phone) {
        String sql = "SELECT COUNT(*) FROM STUDENT WHERE PHONE = ?";
        try (Connection con = DBUtil.dbConnect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private final TextField txtSearch = new TextField();
    private final TableView<Lecturer> lecturerTable = new TableView<>();
    private final ObservableList<Lecturer> lecturerData = FXCollections.observableArrayList();

    private final Button btnAdd = new Button("Add Lecturer");
    private final Button btnEdit = new Button("Edit Lecturer");
    private final Button btnDelete = new Button("Delete");
    private final Button btnRefresh = new Button("Refresh");
    private final Button Back = new Button("Back");

    public Scene getScene(Stage stage) {

        setupTableColumns();
        lecturerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Image iconImage = new Image("file:./images/AcademiX-Icon.png");
        ImageView icon = Assistor.createIcon("./images/AcademiX-Icon.png", 48);

        btnAdd.setGraphic(Assistor.createIcon("./images/add.png", 24));

        btnEdit.setGraphic(Assistor.createIcon("./images/edit.png", 24));

        btnDelete.setGraphic(Assistor.createIcon("./images/delete.png", 24));

        btnRefresh.setGraphic(Assistor.createIcon("./images/refresh.png", 24));

        Back.setGraphic(Assistor.createIcon("./images/back.png", 24));

        btnDelete.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-pref-width: 150px");
        btnDelete.disableProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                btnDelete.setStyle("-fx-pref-width: 150px");
            } else {
                btnDelete.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-pref-width: 150px");
            }
        });

        btnAdd.setOnAction(e -> showAddDialog(stage, icon, iconImage));
        btnDelete.setOnAction(e -> handleDeleteLecturer(icon, iconImage));
        btnEdit.setOnAction(e -> showEditDialog(stage, icon, iconImage));
        btnRefresh.setOnAction(e -> loadLecturers());

        Back.setOnAction(e -> {
            Dashboard dashboard = new Dashboard();
            SceneAnimator.transition(stage, dashboard.getScene(stage),
                    () -> stage.setTitle("AcademiX | Admin Dashboard"));
        });

        btnEdit.setDisable(true);
        btnDelete.setDisable(true);

        lecturerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            int selectedCount = lecturerTable.getSelectionModel().getSelectedItems().size();
            boolean isSingleSelected = selectedCount == 1;
            btnEdit.setDisable(!isSingleSelected);
            btnDelete.setDisable(selectedCount < 1);
        });

        txtSearch.setPromptText("Search by Name or ID");
        HBox searchBar = new HBox(10, new Label("Search:"), txtSearch);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        txtSearch.textProperty().addListener((obs, oldV, newV) -> handleRealTimeSearch(newV));

        Region spacer = new Region();
        HBox topBar = new HBox(20, searchBar, spacer, btnRefresh);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        btnRefresh.setStyle("-fx-pref-width: 120px");

        Region actionSpacer = new Region();
        HBox actionButtons = new HBox(10, btnAdd, btnEdit, btnDelete, actionSpacer, Back);
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        btnAdd.setStyle("-fx-pref-width: 150px");
        btnEdit.setStyle("-fx-pref-width: 150px");
        btnDelete.setStyle("-fx-pref-width: 150px");

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        VBox root = new VBox(10, topBar, sep1, lecturerTable, sep2, actionButtons);
        root.setPadding(new Insets(20, 20, 20, 20));
        VBox.setVgrow(lecturerTable, Priority.ALWAYS);

        StackPane stackRoot = Assistor.createWithBackground(root, 0.2, 815, 710);
        Scene primaryScene = new Scene(stackRoot, 815, 650);
        primaryScene.getStylesheets().add("file:./assets/style.css");

        loadLecturers();
        return primaryScene;
    }

    private void showAddDialog(Stage ownerStage, ImageView iconView, Image icon) {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Lecturer");
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);

        TextField fName = new TextField();
        TextField fPhone = new TextField();
        TextField fSalary = new TextField();
        DatePicker fBirth = new DatePicker();

        ToggleGroup tg = new ToggleGroup();
        RadioButton rMale = new RadioButton("Male");
        rMale.setToggleGroup(tg);
        RadioButton rFemale = new RadioButton("Female");
        rFemale.setToggleGroup(tg);

        fName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\- ]*")) {
                fName.setText(newValue.replaceAll("[^a-zA-Z\\- ]", ""));
            }
            if (newValue.startsWith(" ")) {
                fName.setText(newValue.replaceFirst(" ", ""));
            }
            if (newValue.startsWith("-")) {
                fName.setText(newValue.replaceFirst("-", ""));
            }
        });

        fPhone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                fPhone.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (fPhone.getText().length() > 11) {
                fPhone.setText(fPhone.getText().substring(0, 11));
            }
        });

        fSalary.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                fSalary.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });

        fName.setPromptText("Full Name");
        fPhone.setPromptText("Phone (11 digits)");
        fSalary.setPromptText("Salary");
        fBirth.setPromptText("MM/DD/YYYY");

        HBox genderBox = new HBox(10, rMale, rFemale);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.addRow(0, new Label("Full Name:"), fName);
        grid.addRow(1, new Label("Phone:"), fPhone);
        grid.addRow(2, new Label("Birth Date:"), fBirth);
        grid.addRow(3, new Label("Gender:"), genderBox);
        grid.addRow(4, new Label("Salary:"), fSalary);

        Button btnSave = new Button("Save Lecturer");
        Button btnCancel = new Button("Cancel");

        btnSave.setStyle("-fx-pref-width: 130px");
        btnCancel.setStyle("-fx-pref-width: 100px");

        HBox hb = new HBox(10, btnSave, btnCancel);
        grid.add(hb, 1, 5);

        btnSave.setOnAction(e -> {
            String name = fName.getText();
            String phone = fPhone.getText();
            String salaryStr = fSalary.getText();
            String gender = (tg.getSelectedToggle() != null) ? ((RadioButton) tg.getSelectedToggle()).getText() : "";
            String birthDate = (fBirth.getValue() != null) ? fBirth.getValue().toString() : "";

            if (name.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Name", "No Name has been entered.");
                return;
            } else if (phone.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Phone Number", "No Phone Number has been entered.");
                return;
            } else if (!phone.matches("\\d+")) {
                Assistor.showErrorAlert("Error", "Invalid Phone Number", "Phone number must contain only numbers.");
                return;
            } else if (phone.length() != 11) {
                Assistor.showErrorAlert("Error", "Invalid Phone Number", "Phone number must be 11 digits.");
                return;
            } else if (isNumberRegisteredLec(phone) || isNumberRegisteredStud(phone)) {
                Assistor.showErrorAlert("Error", "Phone Already Registered",
                        "This phone number is already registered. Please use another one.");
                return;
            } else if (gender == null || gender.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Gender", "No Gender has been selected.");
                return;
            } else if (birthDate.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Birth Date", "No Birth Date has been entered.");
                return;
            } else if (salaryStr.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Salary", "No Salary has been entered.");
                return;
            }

            try {
                float salaryValue = Float.parseFloat(salaryStr);
                if (salaryValue < 0) {
                    Assistor.showErrorAlert("Error", "Invalid Salary", "Salary must be a positive number");
                    return;
                }

                String autoEmail = "";
                long generatedId = 0;
                String firstName = name.trim().split("\\s+")[0].toLowerCase();
                String sql = "INSERT INTO LECTURER (NAME, EMAIL, PHONE, PASSWORD, GENDER, BIRTH_DATE, SALARY) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (Connection con = DBUtil.dbConnect();
                        PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, name);
                    pstmt.setString(2, autoEmail);
                    pstmt.setString(3, phone);
                    pstmt.setString(4, "lecturer123");
                    pstmt.setString(5, gender);
                    pstmt.setDate(6, java.sql.Date.valueOf(birthDate));
                    pstmt.setFloat(7, salaryValue);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        ResultSet rs = pstmt.getGeneratedKeys();
                        if (rs.next()) {
                            generatedId = rs.getLong(1);
                            autoEmail = firstName + "." + generatedId + "@academix.edu";

                            try (PreparedStatement updateStmt = con
                                    .prepareStatement("UPDATE LECTURER SET EMAIL = ? WHERE lecturer_id = ?")) {
                                updateStmt.setString(1, autoEmail);
                                updateStmt.setLong(2, generatedId);
                                updateStmt.executeUpdate();
                            }
                        }
                        loadLecturers();
                        dialog.close();

                        Assistor.showSuccessAlert("Success", "Lecturer Created",
                                "Lecturer added successfully!\n\nLecturer ID: " + generatedId
                                        + "\nEmail: " + autoEmail
                                        + "\nDefault Password: lecturer123");
                    }
                }
            } catch (NumberFormatException ex) {
                Assistor.showErrorAlert("Error", "Invalid Salary format", "Salary must be a valid number");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        btnCancel.setOnAction(e -> dialog.close());

        Scene scene = new Scene(grid);
        scene.getStylesheets().add("file:./assets/style.css");
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showEditDialog(Stage ownerStage, ImageView iconView, Image icon) {
        Lecturer selected = lecturerTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Stage dialog = new Stage();
        dialog.setTitle("Edit Lecturer: " + selected.getName());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label lblId = new Label("Lecturer ID: " + selected.getLecturerId());
        lblId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        GridPane.setColumnSpan(lblId, 2);
        GridPane.setHalignment(lblId, HPos.CENTER);

        TextField fName = new TextField(selected.getName());
        TextField fSalary = new TextField(String.valueOf(selected.getSalary()));

        TextField fEmail = new TextField();
        fEmail.setEditable(false);
        fEmail.setDisable(true);
        TextField fPhone = new TextField();

        fName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[a-zA-Z\\- ]*")) {
                fName.setText(newValue.replaceAll("[^a-zA-Z\\- ]", ""));
            }
            if (newValue.startsWith(" ")) {
                fName.setText(newValue.replaceFirst(" ", ""));
            }
            if (newValue.startsWith("-")) {
                fName.setText(newValue.replaceFirst("-", ""));
            }
        });

        fPhone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                fPhone.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (fPhone.getText().length() > 11) {
                fPhone.setText(fPhone.getText().substring(0, 11));
            }
        });

        fSalary.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                fSalary.setText(newValue.replaceAll("[^\\d.]", ""));
            }
        });

        TextField fPass = new TextField();
        DatePicker fBirth = new DatePicker();

        ToggleGroup tg = new ToggleGroup();
        RadioButton rMale = new RadioButton("Male");
        rMale.setToggleGroup(tg);
        RadioButton rFemale = new RadioButton("Female");
        rFemale.setToggleGroup(tg);

        try (Connection con = DBUtil.dbConnect();
                PreparedStatement pstmt = con.prepareStatement(
                        "SELECT EMAIL, PHONE, PASSWORD, GENDER, BIRTH_DATE FROM LECTURER WHERE lecturer_id = ?")) {
            pstmt.setLong(1, selected.getLecturerId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                fEmail.setText(rs.getString("email"));
                fPhone.setText(rs.getString("phone"));
                fPass.setText(rs.getString("password"));
                String gender = rs.getString("gender");
                if ("Male".equals(gender)) {
                    rMale.setSelected(true);
                } else if ("Female".equals(gender)) {
                    rFemale.setSelected(true);
                }
                java.sql.Date birthDate = rs.getDate("birth_date");
                if (birthDate != null) {
                    fBirth.setValue(birthDate.toLocalDate());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HBox genderBox = new HBox(10, rMale, rFemale);

        grid.add(lblId, 0, 0, 2, 1);
        grid.addRow(1, new Label("Full Name:"), fName);
        grid.addRow(2, new Label("Email Address:"), fEmail);
        grid.addRow(3, new Label("Phone Number:"), fPhone);
        grid.addRow(4, new Label("Login Password:"), fPass);
        grid.addRow(5, new Label("Birth Date:"), fBirth);
        grid.addRow(6, new Label("Gender:"), genderBox);
        grid.addRow(7, new Label("Salary:"), fSalary);

        Button btnUpdate = new Button("Update");
        ImageView updateIconView = new ImageView(new Image("file:./images/update.png"));
        updateIconView.setFitHeight(20);
        updateIconView.setFitWidth(20);
        btnUpdate.setGraphic(updateIconView);
        Button btnCancel = new Button("Cancel");
        HBox hb = new HBox(10, btnUpdate, btnCancel);
        grid.add(hb, 1, 8);

        btnUpdate.setDisable(true);

        String origName = selected.getName();
        String origPhone = fPhone.getText();
        String origPass = fPass.getText();
        String origGender = selected.getGender();
        String origBirth = selected.getBirthDate();
        String origSalary = String.valueOf(selected.getSalary());

        javafx.beans.property.ReadOnlyBooleanWrapper isChanged = new javafx.beans.property.ReadOnlyBooleanWrapper(
                false);

        fName.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(
                    !newV.equals(origName) || !fPhone.getText().equals(origPhone) || !fPass.getText().equals(origPass)
                            || (tg.getSelectedToggle() != null
                                    && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))
                            || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth))
                            || !fSalary.getText().equals(origSalary));
            btnUpdate.setDisable(!isChanged.get());
        });
        fPhone.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(
                    !newV.equals(origPhone) || !fName.getText().equals(origName) || !fPass.getText().equals(origPass)
                            || (tg.getSelectedToggle() != null
                                    && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))
                            || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth))
                            || !fSalary.getText().equals(origSalary));
            btnUpdate.setDisable(!isChanged.get());
        });
        fPass.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(
                    !newV.equals(origPass) || !fName.getText().equals(origName) || !fPhone.getText().equals(origPhone)
                            || (tg.getSelectedToggle() != null
                                    && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))
                            || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth))
                            || !fSalary.getText().equals(origSalary));
            btnUpdate.setDisable(!isChanged.get());
        });
        fSalary.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(
                    !newV.equals(origSalary) || !fName.getText().equals(origName) || !fPhone.getText().equals(origPhone)
                            || !fPass.getText().equals(origPass)
                            || (tg.getSelectedToggle() != null
                                    && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))
                            || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth)));
            btnUpdate.setDisable(!isChanged.get());
        });
        tg.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            String newGender = (newV != null) ? ((RadioButton) newV).getText() : "";
            isChanged.set((newV != null && !newGender.equals(origGender)) || !fName.getText().equals(origName)
                    || !fPhone.getText().equals(origPhone)
                    || !fPass.getText().equals(origPass) || !fSalary.getText().equals(origSalary)
                    || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth)));
            btnUpdate.setDisable(!isChanged.get());
        });
        fBirth.valueProperty().addListener((obs, oldV, newV) -> {
            String newBirth = (newV != null) ? newV.toString() : "";
            isChanged.set(!newBirth.equals(origBirth) || !fName.getText().equals(origName)
                    || !fPhone.getText().equals(origPhone)
                    || !fPass.getText().equals(origPass)
                    || (tg.getSelectedToggle() != null
                            && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))
                    || !fSalary.getText().equals(origSalary));
            btnUpdate.setDisable(!isChanged.get());
        });

        btnUpdate.setOnAction(e -> {
            try {
                if (!fName.getText().matches("^[a-zA-Z\\s'-]+$")) {
                    Assistor.showErrorAlert("Error", "Invalid Name", "Invalid name format");
                    return;
                }
                if (fPhone.getText().isEmpty()) {
                    Assistor.showErrorAlert("Error", "No Phone Number", "No Phone Number");
                    return;
                }
                if (!fPhone.getText().matches("\\d+")) {
                    Assistor.showErrorAlert("Error", "Invalid Phone Number", "Invalid Phone Number");
                    return;
                }
                if (fPhone.getText().length() != 11) {
                    Assistor.showErrorAlert("Error", "Invalid Phone Number", "Phone number must be 11 digits.");
                    return;
                }
                if (!fPhone.getText().equals(origPhone)
                        && (isNumberRegisteredLec(fPhone.getText()) || isNumberRegisteredStud(fPhone.getText()))) {
                    Assistor.showErrorAlert("Error", "Phone Already Registered",
                            "This phone number is already registered.");
                    return;
                }
                float checkSalary = Float.parseFloat(fSalary.getText());
                if (checkSalary < 0) {
                    Assistor.showErrorAlert("Error", "Invalid Salary", "Salary must be a positive number");
                    return;
                }
                String gender = (tg.getSelectedToggle() != null) ? ((RadioButton) tg.getSelectedToggle()).getText()
                        : "";
                if (gender.isEmpty()) {
                    Assistor.showErrorAlert("Error", "No Gender", "No Gender selected");
                    return;
                }
                String birthDate = (fBirth.getValue() != null) ? fBirth.getValue().toString() : "";
                if (birthDate.isEmpty()) {
                    Assistor.showErrorAlert("Error", "No Birth Date", "No Birth Date selected");
                    return;
                }

                String currentGender = (tg.getSelectedToggle() != null)
                        ? ((RadioButton) tg.getSelectedToggle()).getText()
                        : "";
                String currentBirth = (fBirth.getValue() != null) ? fBirth.getValue().toString() : "";

                StringBuilder changed = new StringBuilder();
                if (!fName.getText().equals(origName))
                    changed.append("Name, ");
                if (!fPhone.getText().equals(origPhone))
                    changed.append("Phone, ");
                if (!fPass.getText().equals(origPass))
                    changed.append("Password, ");
                if (!currentGender.equals(origGender))
                    changed.append("Gender, ");
                if (!currentBirth.equals(origBirth))
                    changed.append("Birth Date, ");
                if (!fSalary.getText().equals(origSalary))
                    changed.append("Salary, ");

                String sql = "UPDATE LECTURER SET NAME=?, EMAIL=?, PHONE=?, PASSWORD=?, GENDER=?, BIRTH_DATE=?, SALARY=? WHERE lecturer_id=?";
                try (Connection con = DBUtil.dbConnect(); PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, fName.getText());
                    pstmt.setString(2, fEmail.getText());
                    pstmt.setString(3, fPhone.getText());
                    pstmt.setString(4, fPass.getText());
                    pstmt.setString(5, gender);
                    pstmt.setDate(6, java.sql.Date.valueOf(birthDate));
                    pstmt.setFloat(7, checkSalary);
                    pstmt.setLong(8, selected.getLecturerId());

                    if (pstmt.executeUpdate() > 0) {
                        loadLecturers();
                        String changedStr = changed.toString();
                        if (changedStr.endsWith(", ")) {
                            changedStr = changedStr.substring(0, changedStr.length() - 2);
                        }
                        Assistor.showSuccessAlert("Success", "Update Complete",
                                "Lecturer updated successfully!\n\nChanged: " + changedStr);
                        dialog.close();
                    }
                }
            } catch (Exception ex) {
                Assistor.showErrorAlert("Error", "Database Error", "Error: " + ex.getMessage());
            }
        });

        btnCancel.setOnAction(e -> dialog.close());

        Scene scene = new Scene(grid);
        scene.getStylesheets().add("file:./assets/style.css");
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        TableColumn<Lecturer, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("lecturerId"));
        TableColumn<Lecturer, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(250);
        TableColumn<Lecturer, Float> colSalary = new TableColumn<>("Salary");
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colSalary.setPrefWidth(150);

        lecturerTable.getColumns().setAll(colId, colName, colSalary);
        lecturerTable.setItems(lecturerData);
    }

    private void loadLecturers() {
        lecturerData.clear();
        try (Connection con = DBUtil.dbConnect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM LECTURER ORDER BY lecturer_id DESC")) {
            while (rs.next()) {
                lecturerData.add(new Lecturer(rs.getLong("lecturer_id"), rs.getString("name"), rs.getString("phone"),
                        rs.getString("birth_date"), rs.getString("gender"), rs.getFloat("salary")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteLecturer(ImageView iconView, Image icon) {
        ObservableList<Lecturer> selectedLecturers = lecturerTable.getSelectionModel().getSelectedItems();
        if (selectedLecturers.isEmpty())
            return;

        String message;
        if (selectedLecturers.size() == 1) {
            message = "Delete lecturer " + selectedLecturers.get(0).getName() + "?";
        } else {
            StringBuilder sb = new StringBuilder("Delete ");
            sb.append(selectedLecturers.size()).append(" lecturers?\n\nSelected:\n");
            for (Lecturer l : selectedLecturers) {
                sb.append("- ").append(l.getName()).append("\n");
            }
            message = sb.toString();
        }

        Optional<ButtonType> result = Assistor.showConfirmationAlert("Confirmation", "Delete Lecturer?", message);

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection con = DBUtil.dbConnect()) {
                // First: remove from junction table
                PreparedStatement removeAssignments = con.prepareStatement(
                        "DELETE FROM COURSE_LECTURER WHERE lecturer_id = ?");
                // Then: delete lecturer
                PreparedStatement pstmt = con.prepareStatement("DELETE FROM LECTURER WHERE lecturer_id = ?");

                for (Lecturer l : selectedLecturers) {
                    removeAssignments.setLong(1, l.getLecturerId());
                    removeAssignments.executeUpdate();

                    pstmt.setLong(1, l.getLecturerId());
                    pstmt.executeUpdate();
                }
                loadLecturers();
            } catch (SQLException e) {
                Assistor.showErrorAlert("Database Error", "Cannot Delete", "Cannot delete lecturer: " + e.getMessage());
            }
        }
    }

    private void handleRealTimeSearch(String query) {
        lecturerData.clear();
        String sql = "SELECT * FROM LECTURER WHERE UPPER(NAME) LIKE ? OR CAST(lecturer_id AS TEXT) LIKE ?";
        try (Connection con = DBUtil.dbConnect(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query.toUpperCase() + "%");
            pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lecturerData.add(new Lecturer(rs.getLong("lecturer_id"), rs.getString("name"), rs.getString("phone"),
                        rs.getString("birth_date"), rs.getString("gender"), rs.getFloat("salary")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}