package classes;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.ArrayList;

/**
 * Data Model class for a Student record.
 */

public class Student extends Person {
    private final SimpleFloatProperty cgpa;
    private final SimpleStringProperty major;
    private final SimpleStringProperty minor;
    private final ArrayList<Course> enrolledCourses;

    public Student(long id, String name, float cgpa, String major, String minor) {
        super(name, id);
        this.cgpa = new SimpleFloatProperty(cgpa);
        this.major = new SimpleStringProperty(major);
        this.minor = new SimpleStringProperty(minor);
        this.enrolledCourses = new ArrayList<>();
    }

    @Override
    public String getRoleDescription() {
        return "Student";
    }

    // Enrollment methods
    public void enrollInCourse(Course course) {
        if (!enrolledCourses.contains(course)) {
            enrolledCourses.add(course);
        }
    }

    public ArrayList<Course> getEnrolledCourses() {
        return enrolledCourses;
    }

    // Getters for TableView Mapping (The TableView uses these methods)
    public Long getStudentId() {
        return getId();
    }

    public Float getCgpa() {
        return cgpa.get();
    }

    public String getMajor() {
        return major.get();
    }

    public String getMinor() {
        return minor.get();
    }

    // Property methods for JavaFX binding
    public SimpleLongProperty studentIdProperty() {
        return idProperty();
    }

    public SimpleFloatProperty cgpaProperty() {
        return cgpa;
    }

    public SimpleStringProperty majorProperty() {
        return major;
    }

    public SimpleStringProperty minorProperty() {
        return minor;
    }
}
