package classes;

public class Department {
    private String name;
    private long code;

    public Department(String name, long code) {
        this.name = name;
        this.code = code;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
