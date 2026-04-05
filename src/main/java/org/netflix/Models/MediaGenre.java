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
}