package org.netflix.Models;

public class Episode {
    private int id;
    private int seasonId;
    private int episodeNumber;
    private String title;
    private String filePath;
    private String thumbnailPath;
    public Episode(int id, int seasonId, int episodeNumber, String title, String filePath, String thumbnailPath) {
        this.id = id;
        this.seasonId = seasonId;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.filePath = filePath;
        this.thumbnailPath = thumbnailPath;
    }
    public int getId() {
        return id;
    }
    public int getSeasonId() {
        return seasonId;
    }
    public int getEpisodeNumber() {
        return episodeNumber;
    }
    public String getTitle() {
        return title;
    }
    public String getFilePath() {
        return filePath;
    }
    public String getThumbnailPath() {
        return thumbnailPath;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }
    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }
}
