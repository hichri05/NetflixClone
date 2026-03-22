package org.netflix.Models;

public class Serie extends Media{
    private int nbrSaison;
    public Serie(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String director,
                 int nbrSaison) {
        super(idMedia, title, description, releaseYear, averageRating, coverImageUrl, director);
        this.nbrSaison = nbrSaison;
    }
    public int getNbrSaison() { return nbrSaison; }
    public void setNbrSaison(int nbrSaison) { this.nbrSaison = nbrSaison; }
}
