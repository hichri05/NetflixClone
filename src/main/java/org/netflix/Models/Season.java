package org.netflix.Models;

public class Season  {
    private int idSeason;
    private int isSerie;
    private int seasonNumber;
    private String title;
    private String description;
    public Season(int idSeason, int idSerie, int saisonNumber, String title, String description) {
        this.idSeason = idSeason;
        this.isSerie = idSerie;
        this.seasonNumber = saisonNumber;
        this.title = title;
        this.description = description;
    }
    public int getIdSeason() {
        return idSeason;
    }

    public int getIsSerie() {
        return isSerie;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
    public void setIdSeason(int idSeason) {
        this.idSeason = idSeason;
    }

    public void setIsSerie(int isSerie) {
        this.isSerie = isSerie;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
