package org.netflix.Models;

public class Genre {
    private int id;
    private MediaGenre name;


    // Constructor
    public Genre(int id, MediaGenre name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public int getId() { return id; }
    public MediaGenre getName() { return name; }

    // Helper method to display nicely in Scene Builder/UI
    @Override
    public String toString() {
        return name.toString().replace("_", " ");
    }
}