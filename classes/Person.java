package classes;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public abstract class Person {
    private final SimpleStringProperty name;
    private final SimpleLongProperty id;

    public Person(String name, long id) {
        this.name = new SimpleStringProperty(name);
        this.id = new SimpleLongProperty(id);
    }

    public String getName() {
        return name.get();
    }

    public long getId() {
        return id.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleLongProperty idProperty() {
        return id;
    }

    public abstract String getRoleDescription();

    @Override
    public String toString() {
        return "Role: " + getRoleDescription() + "\nName: " + getName() + "\nID: " + getId();
    }
}