package org.netflix.Models;

import java.util.List;

public class Serie extends Media{
    private int nbrSaison;
    public Serie(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String backdropImageUrl, String director, String type,
                 int nbrSaison, List<Genre> genres,List<Acteur> casting,int views) {
        super( idMedia, title,  description,releaseYear,
                averageRating,  coverImageUrl,backdropImageUrl,director, genres, casting,views, type);
        this.nbrSaison = nbrSaison;
    }
    public int getNbrSaison() { return nbrSaison; }
    public void setNbrSaison(int nbrSaison) { this.nbrSaison = nbrSaison; }
}
