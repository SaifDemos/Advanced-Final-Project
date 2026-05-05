package classes;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.ArrayList;

public class Course {
    private final SimpleLongProperty courseId;
    private final SimpleStringProperty name;
    private final SimpleLongProperty recordHours;
    private final ArrayList<Lecturer> assignedLecturers;

    public Course(long id, String name, long recordHours) {
        this.courseId = new SimpleLongProperty(id);
        this.name = new SimpleStringProperty(name);
        this.recordHours = new SimpleLongProperty(recordHours);
        this.assignedLecturers = new ArrayList<>();
    }

    // Assign lecturer (add to list with bidirectional relationship)
    public void assignLecturer(Lecturer lecturer) {
        if (!assignedLecturers.contains(lecturer)) {
            assignedLecturers.add(lecturer);
            lecturer.addCourse(this);
        }
    }

    public ArrayList<Lecturer> getAssignedLecturers() {
        return assignedLecturers;
    }

    public Long getCourseId() {
        return courseId.get();
    }

    public String getName() {
        return name.get();
    }

    public Long getRecordHours() {
        return recordHours.get();
    }

    public SimpleLongProperty courseIdProperty() {
        return courseId;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleLongProperty recordHoursProperty() {
        return recordHours;
    }
}