package org.netflix.Models;

import java.util.List;

public class Media {
    protected int idMedia;
    protected String title;
    protected String description;
    protected int releaseYear;
    protected double averageRating;
    protected String coverImageUrl;
    protected String backdropImageUrl;
    protected String director;
    protected List<Genre> genres;

    public Media(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String backdropImageUrl,String director,List<Genre> genres) {
        this.idMedia = idMedia;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.averageRating = averageRating;
        this.coverImageUrl = coverImageUrl;
        this.backdropImageUrl = backdropImageUrl;
        this.director = director;
        this.genres = genres;
    }
    public  Media() {}
    public int getIdMedia() { return idMedia; }
    public String getTitle() { return title; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getBackdropImageUrl() {return backdropImageUrl;}
    public String getDescription() { return description; }
    public int getReleaseYear(){ return releaseYear;}
    public double getAverageRating(){ return averageRating;}
    public String getDirector() {return director;}
    public List<Genre> getGenres() { return genres; }
}