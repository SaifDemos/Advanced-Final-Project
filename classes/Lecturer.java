package classes;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.ArrayList;

public class Lecturer extends Person {
    private final SimpleStringProperty phone;
    private final SimpleStringProperty birthDate;
    private final SimpleStringProperty gender;
    private final SimpleFloatProperty salary;
    private final ArrayList<Course> coursesTeaching;

    public Lecturer(long id, String name, String phone, String birthDate, String gender, float salary) {
        super(name, id);
        this.phone = new SimpleStringProperty(phone);
        this.birthDate = new SimpleStringProperty(birthDate);
        this.gender = new SimpleStringProperty(gender);
        this.salary = new SimpleFloatProperty(salary);
        this.coursesTeaching = new ArrayList<>();
    }

    @Override
    public String getRoleDescription() {
        return "Lecturer";
    }

    // Course teaching methods
    public void addCourse(Course course) {
        if (!coursesTeaching.contains(course)) {
            coursesTeaching.add(course);
        }
    }

    public ArrayList<Course> getCoursesTeaching() {
        return coursesTeaching;
    }

    public Long getLecturerId() {
        return getId();
    }

    public String getPhone() {
        return phone.get();
    }

    public String getBirthDate() {
        return birthDate.get();
    }

    public String getGender() {
        return gender.get();
    }

    public Float getSalary() {
        return salary.get();
    }

    public SimpleLongProperty lecturerIdProperty() {
        return idProperty();
    }

    public SimpleStringProperty nameProperty() {
        return super.nameProperty();
    }

    public SimpleStringProperty phoneProperty() {
        return phone;
    }

    public SimpleStringProperty birthDateProperty() {
        return birthDate;
    }

    public SimpleStringProperty genderProperty() {
        return gender;
    }

    public SimpleFloatProperty salaryProperty() {
        return salary;
    }
}
