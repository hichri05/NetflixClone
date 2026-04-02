package org.netflix.Models;

import java.util.List;

public class Serie extends Media{
    private int nbrSaison;
    public Serie(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String backdropImageUrl, String director, String type, List<Genre> genres,List<Acteur> casting,
                 int nbrSaison) {
        super( idMedia, title, description,releaseYear,
                averageRating,  coverImageUrl, backdropImageUrl, director, type, genres,casting);
        this.nbrSaison = nbrSaison;
    }
    public int getNbrSaison() { return nbrSaison; }
    public void setNbrSaison(int nbrSaison) { this.nbrSaison = nbrSaison; }
}
