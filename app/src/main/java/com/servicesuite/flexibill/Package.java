package com.servicesuite.flexibill;

import java.util.Map;

public class Package {
    private String id;  // Add this field
    private String name;
    private Map<String, Integer> items; // Key: item name, Value: quantity
    private double price;

    // Default constructor required for Firebase
    public Package() {}

    public Package(String id, String name, Map<String, Integer> items, double price) {
        this.id = id;
        this.name = name;
        this.items = items;
        this.price = price;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public void setItems(Map<String, Integer> items) {
        this.items = items;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
