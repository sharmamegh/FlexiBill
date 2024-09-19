package com.servicesuite.flexibill;

public class Location {

    private String id;
    private String name;
    private int capacity; // Change to int
    private String address;

    // No-argument constructor (required for Firestore)
    public Location() {
        // Required for Firestore deserialization
    }

    // Constructor with parameters
    public Location(String name, int capacity, String address) {
        this.name = name;
        this.capacity = capacity;
        this.address = address;
    }

    public String getId() {
        return id;  // Add this getter method
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
