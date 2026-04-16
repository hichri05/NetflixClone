package org.netflix.Models;

public class Genre {
    private int id;
    private MediaGenre name;

    public Genre(int id, MediaGenre name) {
        this.id = id;
        this.name = name;
    }

    public Genre(int id){
        this.id = id;
    }

    public int getId() { return id; }
    public MediaGenre getName() { return this.name; }

    @Override
    public String toString() {
        if (name == null) return "";
        return name.toString().replace("_", " ");
    }

}