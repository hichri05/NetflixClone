package org.netflix.Models;

public class Movie {
    private int id;
    private String title;
    private String description;
    private int releaseYear;
    private double rating;
    private int durationMinutes;
    private String posterImage;
    private String filePath;

    public Movie(int id, String title, String description, int releaseYear,
                 double rating, int durationMinutes, String posterImage, String filePath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.rating = rating;
        this.durationMinutes = durationMinutes;
        this.posterImage = posterImage;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(String posterImage) {
        this.posterImage = posterImage;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
