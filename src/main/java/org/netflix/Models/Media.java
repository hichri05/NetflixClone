package org.netflix.Models;

public class Media {
    private int idMedia;
    private String title;
    private String description;
    private int releaseYear;
    private double averageRating;
    private String coverImageUrl;
    private String director;
    private String type;

    public Media(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String director,String type) {
        this.idMedia = idMedia;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.averageRating = averageRating;
        this.coverImageUrl = coverImageUrl;
        this.director = director;
        this.type=type;
    }

    public int getIdMedia() { return idMedia; }
    public String getTitle() { return title; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getDescription() { return description; }
    public int getReleaseYear(){ return releaseYear;}
    public double getAverageRating(){ return averageRating;}
    public String getDirector() {return director;}
    public String getType() {return type;}
}