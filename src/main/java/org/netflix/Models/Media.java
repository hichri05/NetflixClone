package org.netflix.Models;

import java.util.ArrayList;
import java.util.List;

public class Media {
    protected int idMedia;
    protected String title;
    protected String description;
    protected int releaseYear;
    protected double averageRating;
    protected String coverImageUrl;
    protected String director;
    protected List<Genre> genres;
    private List<Acteur> casting = new ArrayList<>();

    public Media(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String director,List<Genre> genres,List<Acteur> casting) {
        this.idMedia = idMedia;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.averageRating = averageRating;
        this.coverImageUrl = coverImageUrl;
        this.director = director;
        this.genres = genres;
        this.casting=casting;
    }
    public  Media() {}
    public int getIdMedia() { return idMedia; }
    public String getTitle() { return title; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getDescription() { return description; }
    public int getReleaseYear(){ return releaseYear;}
    public double getAverageRating(){ return averageRating;}
    public String getDirector() {return director;}
    public List<Genre> getGenres() { return genres; }
    public List<Acteur> getCasting() { return casting; }
    public void setCasting(List<Acteur> casting) { this.casting = casting; }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdMedia(int idMedia) {
        this.idMedia = idMedia;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}