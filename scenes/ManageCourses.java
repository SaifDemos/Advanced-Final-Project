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

public class ManageCourses {

    private boolean isNameRegistered(String name) {
        String sql = "SELECT COUNT(*) FROM COURSE WHERE UPPER(name) = UPPER(?)";
        try (Connection con = DBUtil.dbConnect();
                PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, name);
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
    private final TableView<Course> courseTable = new TableView<>();
    private final ObservableList<Course> courseData = FXCollections.observableArrayList();

    private final Button btnAdd = new Button("Add Course");
    private final Button btnEdit = new Button("Edit Course");
    private final Button btnDelete = new Button("Delete");
    private final Button btnRefresh = new Button("Refresh");
    private final Button Back = new Button("Back");

    public Scene getScene(Stage stage) {

        setupTableColumns();
        courseTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
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
        btnDelete.setOnAction(e -> handleDeleteCourse(icon, iconImage));
        btnEdit.setOnAction(e -> showEditDialog(stage, icon, iconImage));
        btnRefresh.setOnAction(e -> loadCourses());

        Back.setOnAction(e -> {
            Dashboard dashboard = new Dashboard();
            SceneAnimator.transition(stage, dashboard.getScene(stage),
                    () -> stage.setTitle("AcademiX | Admin Dashboard"));
        });

        btnEdit.setDisable(true);
        btnDelete.setDisable(true);

        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            int selectedCount = courseTable.getSelectionModel().getSelectedItems().size();
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

        VBox root = new VBox(10, topBar, sep1, courseTable, sep2, actionButtons);
        root.setPadding(new Insets(20, 20, 20, 20));
        VBox.setVgrow(courseTable, Priority.ALWAYS);

        StackPane stackRoot = Assistor.createWithBackground(root, 0.2, 815, 710);
        Scene primaryScene = new Scene(stackRoot, 815, 650);
        primaryScene.getStylesheets().add("file:./assets/style.css");

        loadCourses();
        return primaryScene;
    }

    private void showAddDialog(Stage ownerStage, ImageView iconView, Image icon) {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Course");
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);

        TextField fName = new TextField();
        TextField fRecordHours = new TextField();

        fName.setPromptText("Course Name");
        fRecordHours.setPromptText("Record Hours");

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


        fRecordHours.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.matches("[1-3]")) {
                fRecordHours.setText(newValue.replaceAll("[^1-3]", ""));
            }
            if (fRecordHours.getText().length() > 1) {
                fRecordHours.setText(fRecordHours.getText().substring(0, 1));
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.addRow(0, new Label("Course Name:"), fName);
        grid.addRow(1, new Label("Record Hours:"), fRecordHours);

        Button btnSave = new Button("Save Course");
        Button btnCancel = new Button("Cancel");
        HBox hb = new HBox(10, btnSave, btnCancel);
        grid.add(hb, 1, 3);
        GridPane.setHalignment(hb, HPos.RIGHT);

        btnCancel.setOnAction(e -> dialog.close());

        btnSave.setOnAction(e -> {
            String name = fName.getText().trim();
            String recordHours = fRecordHours.getText().trim();

            if (name.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Course Name", "No Course Name has been entered.");
                return;
            }
            if (!name.matches("^[a-zA-Z\\s'-]+$")) {
                Assistor.showErrorAlert("Error", "Invalid Course Name",
                        "Course name must contain only letters, spaces, hyphens, and apostrophes.");
                return;
            }
            if (recordHours.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Record Hours", "No Record Hours has been entered.");
                return;
            }
            if (isNameRegistered(name)) {
                Assistor.showErrorAlert("Error", "Course Already Exists",
                        "A course with this name already exists. Please use another name.");
                return;
            }

            try {
                long hoursValue = Long.parseLong(recordHours);

                String sql = "INSERT INTO COURSE (name, record_hours) VALUES (?, ?)";
                try (Connection con = DBUtil.dbConnect(); PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setLong(2, hoursValue);

                    if (pstmt.executeUpdate() > 0) {
                        loadCourses();
                        Assistor.showSuccessAlert("Success", "Course Added", "Course added successfully!");
                        dialog.close();
                    }
                }
            } catch (Exception ex) {
                Assistor.showErrorAlert("Error", "Database Error", "Error: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(grid);
        scene.getStylesheets().add("file:./assets/style.css");
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void setupTableColumns() {
        TableColumn<Course, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        TableColumn<Course, String> colName = new TableColumn<>("Course Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Course, Long> colHours = new TableColumn<>("Record Hours");
        colHours.setCellValueFactory(new PropertyValueFactory<>("recordHours"));

        courseTable.getColumns().setAll(colId, colName, colHours);
        courseTable.setItems(courseData);
    }

    private void loadCourses() {
        courseData.clear();
        try (Connection con = DBUtil.dbConnect();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM COURSE ORDER BY course_id DESC")) {
            while (rs.next()) {
                courseData.add(new Course(rs.getLong("course_id"), rs.getString("name"), rs.getLong("record_hours")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteCourse(ImageView iconView, Image icon) {
        ObservableList<Course> selectedCourses = courseTable.getSelectionModel().getSelectedItems();
        if (selectedCourses.isEmpty())
            return;

        String message;
        if (selectedCourses.size() == 1) {
            message = "Delete course " + selectedCourses.get(0).getName() + "?";
        } else {
            StringBuilder sb = new StringBuilder("Delete ");
            sb.append(selectedCourses.size()).append(" courses?\n\nSelected:\n");
            for (Course c : selectedCourses) {
                sb.append("- ").append(c.getName()).append("\n");
            }
            message = sb.toString();
        }

        Optional<ButtonType> result = Assistor.showConfirmationAlert("Confirmation", "Delete Course?", message);

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection con = DBUtil.dbConnect()) {
                // First: remove from COURSE_LECTURER
                PreparedStatement removeLecturers = con.prepareStatement(
                        "DELETE FROM COURSE_LECTURER WHERE course_id = ?");
                // Second: remove from STUDENT_COURSE
                PreparedStatement removeStudents = con.prepareStatement(
                        "DELETE FROM STUDENT_COURSE WHERE course_id = ?");
                // Then: delete course
                PreparedStatement pstmt = con.prepareStatement("DELETE FROM COURSE WHERE course_id = ?");

                for (Course c : selectedCourses) {
                    removeLecturers.setLong(1, c.getCourseId());
                    removeLecturers.executeUpdate();

                    removeStudents.setLong(1, c.getCourseId());
                    removeStudents.executeUpdate();

                    pstmt.setLong(1, c.getCourseId());
                    pstmt.executeUpdate();
                }
                loadCourses();
            } catch (SQLException e) {
                Assistor.showErrorAlert("Database Error", "Cannot Delete", "Cannot delete course: " + e.getMessage());
            }
        }
    }

    private void handleRealTimeSearch(String query) {
        courseData.clear();
        String sql = "SELECT * FROM COURSE WHERE UPPER(NAME) LIKE ? OR CAST(course_id AS TEXT) LIKE ?";
        try (Connection con = DBUtil.dbConnect(); PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query.toUpperCase() + "%");
            pstmt.setString(2, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                courseData.add(new Course(rs.getLong("course_id"), rs.getString("name"), rs.getLong("record_hours")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showEditDialog(Stage ownerStage, ImageView iconView, Image icon) {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Stage dialog = new Stage();
        dialog.setTitle("Edit Course");
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(ownerStage);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label lblId = new Label("Course ID: " + selected.getCourseId());
        lblId.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        GridPane.setColumnSpan(lblId, 2);
        GridPane.setHalignment(lblId, HPos.CENTER);

        TextField fName = new TextField(selected.getName());
        TextField fRecordHours = new TextField(String.valueOf(selected.getRecordHours()));

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

        fRecordHours.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[1-3]")) {
                fRecordHours.setText(newValue.replaceAll("[^1-3]", ""));
            }
            if (fRecordHours.getText().length() > 1) {
                fRecordHours.setText(fRecordHours.getText().substring(0, 1));
            }
        });

        grid.add(lblId, 0, 0, 2, 1);
        grid.addRow(1, new Label("Course Name:"), fName);
        grid.addRow(2, new Label("Record Hours:"), fRecordHours);

        Button btnUpdate = new Button("Update");
        ImageView updateIconView = new ImageView(new Image("file:./images/edit.png"));
        updateIconView.setFitHeight(20);
        updateIconView.setFitWidth(20);
        btnUpdate.setGraphic(updateIconView);
        Button btnCancel = new Button("Cancel");
        HBox hb = new HBox(10, btnUpdate, btnCancel);
        grid.add(hb, 1, 4);
        GridPane.setHalignment(hb, HPos.RIGHT);

        btnUpdate.setDisable(true);

        String origName = selected.getName();
        String origHours = String.valueOf(selected.getRecordHours());

        javafx.beans.property.ReadOnlyBooleanWrapper isChanged = new javafx.beans.property.ReadOnlyBooleanWrapper(
                false);

        fName.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(!newV.equals(origName) || !fRecordHours.getText().equals(origHours));
            btnUpdate.setDisable(!isChanged.get());
        });

        fRecordHours.textProperty().addListener((obs, oldV, newV) -> {
            isChanged.set(!newV.equals(origHours) || !fName.getText().equals(origName));
            btnUpdate.setDisable(!isChanged.get());
        });

        btnCancel.setOnAction(e -> dialog.close());

        btnUpdate.setOnAction(e -> {
            String name = fName.getText().trim();
            String recordHours = fRecordHours.getText().trim();

            if (name.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Course Name", "No Course Name has been entered.");
                return;
            }
            if (!name.matches("^[a-zA-Z\\s'-]+$")) {
                Assistor.showErrorAlert("Error", "Invalid Course Name",
                        "Course name must contain only letters, spaces, hyphens, and apostrophes.");
                return;
            }
            if (recordHours.isEmpty()) {
                Assistor.showErrorAlert("Error", "No Record Hours", "No Record Hours has been entered.");
                return;
            }
            if (!fName.getText().equals(origName) && isNameRegistered(fName.getText())) {
                Assistor.showErrorAlert("Error", "Course Already Exists",
                        "A course with this name already exists. Please use another name.");
                return;
            }

            try {
                long hoursValue = Long.parseLong(recordHours);

                StringBuilder changed = new StringBuilder();
                if (!fName.getText().equals(origName))
                    changed.append("Name, ");
                if (!fRecordHours.getText().equals(origHours))
                    changed.append("Record Hours, ");

                String sql = "UPDATE COURSE SET name = ?, record_hours = ? WHERE course_id = ?";
                try (Connection con = DBUtil.dbConnect(); PreparedStatement pstmt = con.prepareStatement(sql)) {
                    pstmt.setString(1, fName.getText());
                    pstmt.setLong(2, hoursValue);
                    pstmt.setLong(3, selected.getCourseId());

                    if (pstmt.executeUpdate() > 0) {
                        loadCourses();
                        String changedStr = changed.toString();
                        if (changedStr.endsWith(", ")) {
                            changedStr = changedStr.substring(0, changedStr.length() - 2);
                        }
                        Assistor.showSuccessAlert("Success", "Update Complete",
                                "Course updated successfully!\n\nChanged: " + changedStr);
                        dialog.close();
                    }
                }
            } catch (Exception ex) {
                Assistor.showErrorAlert("Error", "Database Error", "Error: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(grid);
        scene.getStylesheets().add("file:./assets/style.css");
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}