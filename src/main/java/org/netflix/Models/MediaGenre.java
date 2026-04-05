package org.netflix.Models;

public enum MediaGenre {
   Action,
    Thriller,
    Drame,
    Biopic,
    Comedie,
    Documentaire,
    Famille,
    Fantastique,
    Guerre,
    Historique,
    Horreur,
    Musical,
    Mystere,
    Policier,
    Romance,
    Science_Fiction,
    Sport,
    Western,
    Tragedy,
    Crime;

 @Override
 public String toString() {
  return name(); // or return a custom display name
 }

    public static MediaGenre fromString(String text) {
        for (MediaGenre g : MediaGenre.values()) {
            if (g.name().equalsIgnoreCase(text)) {
                return g;
            }
        }
        return null;
    }
}