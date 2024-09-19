package com.servicesuite.flexibill;

// MenuItem.java
public class MenuItem {
    private String name;
    private String category;

    public MenuItem() {
        // Default constructor required for Firestore
    }

    public MenuItem(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
