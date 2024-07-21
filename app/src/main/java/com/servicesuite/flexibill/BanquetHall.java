package com.servicesuite.flexibill;

public class BanquetHall {
    private String name;
    private int capacity;

    // Default constructor (required for Firestore)
    public BanquetHall() {}

    public BanquetHall(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }
}
