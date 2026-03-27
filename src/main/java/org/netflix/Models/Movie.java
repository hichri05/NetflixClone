package org.netflix.Models;

import java.util.List;

public class Movie extends Media {
    private String videoUrl;
    private int durationMinutes;

    public Movie(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String backdropImageUrl, String director,
                 String videoUrl, int durationMinutes, List<Genre> genres) {

        super( idMedia, title, description,releaseYear,
        averageRating,  coverImageUrl, backdropImageUrl, director, genres);
        this.videoUrl = videoUrl;
        this.durationMinutes = durationMinutes;
    }
    public Movie(){}

    public String getVideoUrl() { return videoUrl; }
    public int getDurationMinutes() { return durationMinutes; }
    @Override
    public String toString() {
        return String.format(
                "MOVIE DETAILS\n" +
                        "--------------------------------------------------\n" +
                        "ID:          %d\n" +
                        "Title:       %s (%d)\n" +
                        "Director:    %s\n" +
                        "Rating:      ★ %.1f / 10\n" +
                        "Duration:    %d min\n" +
                        "Cover:       %s\n" +
                        "Video URL:   %s\n" +
                        "Description: %s\n" +
                        "--------------------------------------------------",
                idMedia, title, releaseYear, director, averageRating,
                durationMinutes, coverImageUrl, videoUrl,
                (description != null && description.length() > 60) ? description.substring(0, 60) + "..." : description
        );
    }
}