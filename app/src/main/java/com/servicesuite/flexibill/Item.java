package com.servicesuite.flexibill;

public class Item {
    private String name;
    private String category;
    private String type;
    private double price;

    // Default constructor required for Firebase
    public Item() {}

    public Item(String name, String category, String type, double price) {
        this.name = name;
        this.category = category;
        this.type = type;
        this.price = price;
    }

    // Getters and Setters
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
