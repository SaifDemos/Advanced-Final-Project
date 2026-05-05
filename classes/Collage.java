package classes;

import java.util.ArrayList;
import java.util.List;

public class Collage {

    // Fixed data
    private final String collageName = "Faculty of Science";
    private final String deanName = "Prof. Dr. Amany Abdel-Hamid";
    private final String universityName = "Alexandria University";
    private final String address = "Alexandria, Egypt";
    private final int establishedYear = 1942;
    private List<Department> departments;

    // Dynamic data
    private int numberOfCourses;
    private int numberOfStudents;
    private int numberOfDoctors;

    public Collage() {
        this.departments = new ArrayList<>();
        this.departments.add(new Department("Physics Department", 101));
        this.departments.add(new Department("Chemistry Department", 102));
        this.departments.add(new Department("Computer Science & Mathematics Department", 103));
        this.departments.add(new Department("Botany and Microbiology", 104));
        this.departments.add(new Department("Zoology", 105));
        this.departments.add(new Department("Geology", 106));
        this.departments.add(new Department("Oceangraphy", 107));
        this.departments.add(new Department("Biochemisrty", 108));
        this.departments.add(new Department("Enviromental Sciences", 109));
    }

    public void setNumberOfCourses(int count) {
        this.numberOfCourses = count;
    }

    public void setNumberOfStudents(int count) {
        this.numberOfStudents = count;
    }

    public void setNumberOfDoctors(int count) {
        this.numberOfDoctors = count;
    }

    public String getCollageName() {
        return collageName;
    }

    public String getDeanName() {
        return deanName;
    }

    public String getUniversityName() {
        return universityName;
    }

    public String getAddress() {
        return address;
    }

    public int getEstablishedYear() {
        return establishedYear;
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public int getNumberOfCourses() {
        return numberOfCourses;
    }

    public int getNumberOfDoctors() {
        return numberOfDoctors;
    }

    public List<Department> getDepartments() {
        return departments;
    }
}
