package com.servicesuite.flexibill;


public class Category {
    private String name;

    public Category() {
        // Required empty constructor for Firestore
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
