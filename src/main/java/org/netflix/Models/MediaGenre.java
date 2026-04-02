package org.netflix.Models;

public enum MediaGenre {
    ACTION,
    COMEDY,
    HORROR,
    DRAMA,
    SCI_FI;

    public static MediaGenre fromString(String text) {
        for (MediaGenre g : MediaGenre.values()) {
            if (g.name().equalsIgnoreCase(text)) {
                return g;
            }
        }
        return null;
    }
}