package org.netflix.Models;

import java.util.List;

public class Serie extends Media{
    private int nbrSaison;
    public Serie(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String backdropImageUrl, String director,
                 int nbrSaison, List<Genre> genres) {
        super( idMedia, title, description,releaseYear,
                averageRating,  coverImageUrl, backdropImageUrl, director, genres);
        this.nbrSaison = nbrSaison;
    }
    public int getNbrSaison() { return nbrSaison; }
    public void setNbrSaison(int nbrSaison) { this.nbrSaison = nbrSaison; }
}
