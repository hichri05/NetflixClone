package org.netflix.Models;

public class Movie extends Media {
    private String videoUrl;
    private int durationMinutes;

    public Movie(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String director,
                 String videoUrl, int durationMinutes) {

        super(idMedia, title, description, releaseYear, averageRating, coverImageUrl, director);
        this.videoUrl = videoUrl;
        this.durationMinutes = durationMinutes;
    }

    public String getVideoUrl() { return videoUrl; }
    public int getDurationMinutes() { return durationMinutes; }
}