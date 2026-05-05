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

public class ManageStudents {

    private static final ObservableList<String> FIELDS_OF_STUDY = FXCollections.observableArrayList(
            "Computer Science", "Statistics", "Mathematics", "Physics", "Chemistry", "Biophysics",
            "Physical Marine Sciences", "Microbiology", "Biochemistry", "Ecology", "Botany",
            "Biotechnology", "Zoology", "Entomology", "Molecular Biology", "General Marine Sciences",
            "Geology", "NONE");

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

        String sql2 = "SELECT COUNT(*) FROM LECTURER WHERE PHONE = ?";
        try (Connection con = DBUtil.dbConnect();
                PreparedStatement pstmt = con.prepareStatement(sql2)) {

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

    private final TextField txtSearch = new TextField();
    private final TableView<Student> studentTable = new TableView<>();
    private final ObservableList<Student> studentData = FXCollections.observableArrayList();

    private final Button btnAdd = new Button("Add Student");
    private final Button btnEdit = new Button("Edit Student");
    private final Button btnDelete = new Button("Delete");
    private final Button btnRefresh = new Button("Refresh");
    private final Button Back = new Button("Back");

    public Scene getScene(Stage stage) {

        setupTableColumns();
        studentTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
        btnDelete.setOnAction(e -> handleDeleteStudent(icon, iconImage));
        btnEdit.setOnAction(e -> showFullEditDialog(stage, icon, iconImage));
        btnRefresh.setOnAction(e -> loadStudents());

        Back.setOnAction(e -> {
            Dashboard dashboard = new Dashboard();
            SceneAnimator.transition(stage, dashboard.getScene(stage),
                    () -> stage.setTitle("AcademiX | Admin Dashboard"));
        });

        btnEdit.setDisable(true);
        btnDelete.setDisable(true);

        studentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            int selectedCount = studentTable.getSelectionModel().getSelectedItems().size();
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

        VBox root = new VBox(10, topBar, sep1, studentTable, sep2, actionButtons);
        root.setPadding(new Insets(20, 20, 20, 20));
        VBox.setVgrow(studentTable, Priority.ALWAYS);

        StackPane stackRoot = Assistor.createWithBackground(root, 0.2, 815, 710);
        Scene primaryScene = new Scene(stackRoot, 815, 650);
        primaryScene.getStylesheets().add("file:./assets/style.css");

        loadStudents();
        return primaryScene;
    }

    private void showAddDialog(Stage ownerStage, ImageView iconView, Image icon) {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Student");
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);

        TextField fName = new TextField();
        TextField fPhone = new TextField();
        TextField fCgpa = new TextField();
        ComboBox<String> fMajor = new ComboBox<>(FIELDS_OF_STUDY);
        fMajor.setPrefWidth(400);
        GridPane.setColumnSpan(fMajor, 2);
        ComboBox<String> fMinor = new ComboBox<>(FIELDS_OF_STUDY);
        fMinor.setPrefWidth(400);
        GridPane.setColumnSpan(fMinor, 2);

        ToggleGroup tg = new ToggleGroup();
        RadioButton rMale = new RadioButton("Male");
        rMale.setToggleGroup(tg);
        RadioButton rFemale = new RadioButton("Female");
        rFemale.setToggleGroup(tg);

        DatePicker fBirth = new DatePicker();

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

        fCgpa.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                fCgpa.setText(newValue.replaceAll("[^\\d*(\\.\\d*)?]", ""));
            }
        });

        fName.setPromptText("Name");
        fPhone.setPromptText("Phone (11 digits)");
        fCgpa.setPromptText("CGPA");
        fMajor.setPromptText("Select Major");
        fMinor.setPromptText("Select Minor");
        fBirth.setPromptText("MM/DD/YYYY");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        HBox genderBox = new HBox(10, rMale, rFemale);

        grid.addRow(0, new Label("Full Name:"), fName);
        grid.addRow(1, new Label("Phone:"), fPhone);
        grid.addRow(2, new Label("Birth Date:"), fBirth);
        grid.addRow(3, new Label("Gender:"), genderBox);
        grid.addRow(4, new Label("CGPA:"), fCgpa);
        grid.addRow(5, new Label("Major:"), fMajor);
        grid.addRow(6, new Label("Minor:"), fMinor);

        Button btnSave = new Button("Save Student");
        Button btnCancel = new Button("Cancel");

        btnSave.setStyle("-fx-pref-width: 120px");
        btnCancel.setStyle("-fx-pref-width: 100px");

        HBox hb = new HBox(10, btnSave, btnCancel);
        grid.add(hb, 1, 7);

        btnSave.setOnAction(e -> {
            String name = fName.getText();
            String phone = fPhone.getText();
            String cgpaStr = fCgpa.getText();
            String major = fMajor.getValue();
            String minor = fMinor.getValue();
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
            } else if (isNumberRegisteredStud(phone) || isNumberRegisteredLec(phone)) {
                Assistor.showErrorAlert("Error", "Phone Already Registered",
                        "This phone number is already registered. Please use another one.");
                return;
            } else if (gender.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Gender", "No Gender has been selected.");
                return;
            } else if (birthDate.isEmpty()) {
                Assistor.showErrorAlert("Error", "No BirthDate", "No BirthDate has been entered.");
                return;
            }

            try {
                float cgpaValue = Float.parseFloat(cgpaStr);
                if (cgpaValue < 0.0f || cgpaValue > 4.0f) {
                    Assistor.showErrorAlert("Error", "Invalid CGPA", "CGPA must be from 0.0 to 4.0");
                    return;
                }

                String autoEmail = "";
                long generatedId = 0;
                String firstName = name.trim().split("\\s+")[0].toLowerCase();
                String sql = "INSERT INTO STUDENT (NAME, CGPA, MAJOR, MINOR, EMAIL, PHONE, PASSWORD, GENDER, BIRTH_DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (Connection con = DBUtil.dbConnect();
                        PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, name);
                    pstmt.setFloat(2, cgpaValue);
                    pstmt.setString(3, major);
                    pstmt.setString(4, minor);
                    pstmt.setString(5, autoEmail);
                    pstmt.setString(6, phone);
                    pstmt.setString(7, "student123");
                    pstmt.setString(8, gender);
                    pstmt.setDate(9, java.sql.Date.valueOf(birthDate));

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        ResultSet rs = pstmt.getGeneratedKeys();
                        if (rs.next()) {
                            generatedId = rs.getLong(1);
                            autoEmail = firstName + "." + generatedId + "@academix.edu";

                            try (PreparedStatement updateStmt = con
                                    .prepareStatement("UPDATE STUDENT SET EMAIL = ? WHERE student_id = ?")) {
                                updateStmt.setString(1, autoEmail);
                                updateStmt.setLong(2, generatedId);
                                updateStmt.executeUpdate();
                            }
                        }
                        loadStudents();
                        dialog.close();

                        Assistor.showSuccessAlert("Success", "Student Created",
                                "Student added successfully!\n\nStudent ID: " + generatedId +
                                        "\nEmail: " + autoEmail +
                                        "\nDefault Password: student123");
                    }
                }
            } catch (NumberFormatException ex) {
                Assistor.showErrorAlert("Error", "Invalid CGPA format", "CGPA must be a valid number");
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

    private void showFullEditDialog(Stage ownerStage, ImageView iconView, Image icon) {
        Student selected = studentTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Stage dialog = new Stage();
        dialog.setTitle("Edit Student: " + selected.getName());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label lblId = new Label("Student ID: " + selected.getStudentId());
        lblId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        GridPane.setColumnSpan(lblId, 2);
        GridPane.setHalignment(lblId, HPos.CENTER);

        TextField fName = new TextField(selected.getName());
        TextField fCgpa = new TextField(String.valueOf(selected.getCgpa()));
        ComboBox<String> fMajor = new ComboBox<>(FIELDS_OF_STUDY);
        fMajor.setValue(selected.getMajor());
        GridPane.setColumnSpan(fMajor, 2);
        ComboBox<String> fMinor = new ComboBox<>(FIELDS_OF_STUDY);
        fMinor.setValue(selected.getMinor());
        GridPane.setColumnSpan(fMinor, 2);

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

        fCgpa.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d\\.?\\d*")) {
                fCgpa.setText(newValue.replaceAll("[^\\d.]", ""));
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
                        "SELECT EMAIL, PHONE, PASSWORD, GENDER, BIRTH_DATE FROM STUDENT WHERE student_id = ?")) {
            pstmt.setLong(1, selected.getStudentId());
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
        grid.addRow(7, new Label("Current CGPA:"), fCgpa);
        grid.addRow(8, new Label("Major:"), fMajor);
        grid.addRow(9, new Label("Minor:"), fMinor);

        Button btnUpdate = new Button("Update");
        ImageView updateIconView = new ImageView(new Image("file:./images/update.png"));
        updateIconView.setFitHeight(20);
        updateIconView.setFitWidth(20);
        btnUpdate.setGraphic(updateIconView);
        Button btnCancel = new Button("Cancel");
        HBox hb = new HBox(10, btnUpdate, btnCancel);
        grid.add(hb, 1, 10);

        btnUpdate.setDisable(true);

        String origName = selected.getName();
        String origPhone = fPhone.getText();
        String origPass = fPass.getText();
        String origGender = (rMale.isSelected()) ? "Male" : (rFemale.isSelected()) ? "Female" : "";
        String origBirth = (fBirth.getValue() != null) ? fBirth.getValue().toString() : "";
        String origCgpa = String.valueOf(selected.getCgpa());
        String origMajor = selected.getMajor();
        String origMinor = selected.getMinor();

        javafx.beans.property.ReadOnlyBooleanWrapper isChanged = new javafx.beans.property.ReadOnlyBooleanWrapper(
                false);

        fName.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(
                    !newV.equals(origName) || !fPhone.getText().equals(origPhone) || !fPass.getText().equals(origPass)
                            || (fMajor.getValue() != null && !fMajor.getValue().equals(origMajor))
                            || (fMinor.getValue() != null && !fMinor.getValue().equals(origMinor))
                            || !fCgpa.getText().equals(origCgpa)
                            || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth))
                            || ((tg.getSelectedToggle() != null
                                    && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))));
            btnUpdate.setDisable(!isChanged.get());
        });
        fPhone.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(
                    !newV.equals(origPhone) || !fName.getText().equals(origName) || !fPass.getText().equals(origPass)
                            || (fMajor.getValue() != null && !fMajor.getValue().equals(origMajor))
                            || (fMinor.getValue() != null && !fMinor.getValue().equals(origMinor))
                            || !fCgpa.getText().equals(origCgpa)
                            || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth))
                            || ((tg.getSelectedToggle() != null
                                    && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))));
            btnUpdate.setDisable(!isChanged.get());
        });
        fPass.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(
                    !newV.equals(origPass) || !fName.getText().equals(origName) || !fPhone.getText().equals(origPhone)
                            || (fMajor.getValue() != null && !fMajor.getValue().equals(origMajor))
                            || (fMinor.getValue() != null && !fMinor.getValue().equals(origMinor))
                            || !fCgpa.getText().equals(origCgpa)
                            || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth))
                            || ((tg.getSelectedToggle() != null
                                    && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))));
            btnUpdate.setDisable(!isChanged.get());
        });
        fCgpa.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(
                    !newV.equals(origCgpa) || !fName.getText().equals(origName) || !fPhone.getText().equals(origPhone)
                            || !fPass.getText().equals(origPass)
                            || (fMajor.getValue() != null && !fMajor.getValue().equals(origMajor))
                            || (fMinor.getValue() != null && !fMinor.getValue().equals(origMinor))
                            || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth))
                            || ((tg.getSelectedToggle() != null
                                    && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))));
            btnUpdate.setDisable(!isChanged.get());
        });
        fMajor.valueProperty().addListener((obs, oldV, newV) -> {
            isChanged.set((newV != null && !newV.equals(origMajor)) || !fName.getText().equals(origName)
                    || !fPhone.getText().equals(origPhone)
                    || !fPass.getText().equals(origPass) || !fCgpa.getText().equals(origCgpa)
                    || (fMinor.getValue() != null && !fMinor.getValue().equals(origMinor))
                    || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth))
                    || ((tg.getSelectedToggle() != null
                            && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))));
            btnUpdate.setDisable(!isChanged.get());
        });
        fMinor.valueProperty().addListener((obs, oldV, newV) -> {
            isChanged.set((newV != null && !newV.equals(origMinor)) || !fName.getText().equals(origName)
                    || !fPhone.getText().equals(origPhone)
                    || !fPass.getText().equals(origPass)
                    || (fMajor.getValue() != null && !fMajor.getValue().equals(origMajor))
                    || !fCgpa.getText().equals(origCgpa)
                    || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth))
                    || ((tg.getSelectedToggle() != null
                            && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))));
            btnUpdate.setDisable(!isChanged.get());
        });
        fBirth.valueProperty().addListener((obs, oldV, newV) -> {
            String newBirth = (newV != null) ? newV.toString() : "";
            isChanged.set(!newBirth.equals(origBirth) || !fName.getText().equals(origName)
                    || !fPhone.getText().equals(origPhone)
                    || !fPass.getText().equals(origPass)
                    || (fMajor.getValue() != null && !fMajor.getValue().equals(origMajor))
                    || (fMinor.getValue() != null && !fMinor.getValue().equals(origMinor))
                    || !fCgpa.getText().equals(origCgpa)
                    || ((tg.getSelectedToggle() != null
                            && !((RadioButton) tg.getSelectedToggle()).getText().equals(origGender))));
            btnUpdate.setDisable(!isChanged.get());
        });
        tg.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            String newGender = (newV != null) ? ((RadioButton) newV).getText() : "";
            isChanged.set(!newGender.equals(origGender) || !fName.getText().equals(origName)
                    || !fPhone.getText().equals(origPhone)
                    || !fPass.getText().equals(origPass)
                    || (fMajor.getValue() != null && !fMajor.getValue().equals(origMajor))
                    || (fMinor.getValue() != null && !fMinor.getValue().equals(origMinor))
                    || !fCgpa.getText().equals(origCgpa)
                    || (fBirth.getValue() != null && !fBirth.getValue().toString().equals(origBirth)));
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
                        && (isNumberRegisteredStud(fPhone.getText()) || isNumberRegisteredLec(fPhone.getText()))) {
                    Assistor.showErrorAlert("Error", "Phone Already Registered",
                            "This phone number is already registered.");
                    return;
                }
                float checkCgpa = Float.parseFloat(fCgpa.getText());
                if (checkCgpa < 0.0f || checkCgpa > 4.0f) {
                    Assistor.showErrorAlert("Error", "Invalid CGPA", "CGPA must be from 0.0 to 4.0");
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
                if (!fCgpa.getText().equals(origCgpa))
                    changed.append("CGPA, ");
                if (fMajor.getValue() != null && !fMajor.getValue().equals(origMajor))
                    changed.append("Major, ");
                if (fMinor.getValue() != null && !fMinor.getValue().equals(origMinor))
                    changed.append("Minor, ");

                String sql = "UPDATE STUDENT SET NAME=?, EMAIL=?, PHONE=?, PASSWORD=?, GENDER=?, BIRTH_DATE=?, CGPA=?, MAJOR=?, MINOR=? WHERE student_id=?";
                try (Connection con = DBUtil.dbConnect(); PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, fName.getText());
                    pstmt.setString(2, fEmail.getText());
                    pstmt.setString(3, fPhone.getText());
                    pstmt.setString(4, fPass.getText());
                    pstmt.setString(5, gender);
                    pstmt.setDate(6, java.sql.Date.valueOf(birthDate));
                    pstmt.setFloat(7, checkCgpa);
                    pstmt.setString(8, fMajor.getValue());
                    pstmt.setString(9, fMinor.getValue());
                    pstmt.setLong(10, selected.getStudentId());

                    if (pstmt.executeUpdate() > 0) {
                        loadStudents();
                        String changedStr = changed.toString();
                        if (changedStr.endsWith(", ")) {
                            changedStr = changedStr.substring(0, changedStr.length() - 2);
                        }
                        Assistor.showSuccessAlert("Success", "Update Complete",
                                "Student updated successfully!\n\nChanged: " + changedStr);
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
        TableColumn<Student, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        TableColumn<Student, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Student, Float> colCGPA = new TableColumn<>("CGPA");
        colCGPA.setCellValueFactory(new PropertyValueFactory<>("cgpa"));
        TableColumn<Student, String> colMajor = new TableColumn<>("Major");
        colMajor.setCellValueFactory(new PropertyValueFactory<>("major"));
        TableColumn<Student, String> colMinor = new TableColumn<>("Minor");
        colMinor.setCellValueFactory(new PropertyValueFactory<>("minor"));

        studentTable.getColumns().setAll(colId, colName, colCGPA, colMajor, colMinor);
        studentTable.setItems(studentData);
    }

    private void loadStudents() {
        studentData.clear();
        try (Connection con = DBUtil.dbConnect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM STUDENT ORDER BY student_id DESC")) {
            while (rs.next()) {
                studentData.add(new Student(rs.getLong("student_id"), rs.getString("name"), rs.getFloat("cgpa"),
                        rs.getString("major"), rs.getString("minor")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteStudent(ImageView iconView, Image icon) {
        ObservableList<Student> selectedStudents = studentTable.getSelectionModel().getSelectedItems();
        if (selectedStudents.isEmpty())
            return;

        String message;
        if (selectedStudents.size() == 1) {
            message = "Delete student " + selectedStudents.get(0).getName() + "?";
        } else {
            StringBuilder sb = new StringBuilder("Delete ");
            sb.append(selectedStudents.size()).append(" students?\n\nSelected:\n");
            for (Student s : selectedStudents) {
                sb.append("- ").append(s.getName()).append("\n");
            }
            message = sb.toString();
        }

        Optional<ButtonType> result = Assistor.showConfirmationAlert("Confirmation", "Delete Student?", message);

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection con = DBUtil.dbConnect()) {
                // First: remove from STUDENT_COURSE junction table
                PreparedStatement removeCourses = con.prepareStatement(
                        "DELETE FROM STUDENT_COURSE WHERE student_id = ?");
                // Then: delete student
                PreparedStatement pstmt = con.prepareStatement("DELETE FROM STUDENT WHERE student_id = ?");

                for (Student s : selectedStudents) {
                    removeCourses.setLong(1, s.getStudentId());
                    removeCourses.executeUpdate();

                    pstmt.setLong(1, s.getStudentId());
                    pstmt.executeUpdate();
                }
                loadStudents();
            } catch (SQLException e) {
                Assistor.showErrorAlert("Database Error", "Cannot Delete", "Cannot delete student: " + e.getMessage());
            }
        }
    }

    private void handleRealTimeSearch(String query) {
        studentData.clear();
        String sql = "SELECT * FROM STUDENT WHERE UPPER(NAME) LIKE ? OR CAST(student_id AS TEXT) LIKE ?";
        try (Connection con = DBUtil.dbConnect(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query.toUpperCase() + "%");
            pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                studentData.add(new Student(rs.getLong("student_id"), rs.getString("name"), rs.getFloat("cgpa"),
                        rs.getString("major"), rs.getString("minor")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}