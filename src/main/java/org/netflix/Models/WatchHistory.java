package org.netflix.Models;

import java.sql.Timestamp;

public class WatchHistory {
    private int userId;
    private Integer mediaId;
    private Integer episodeId;
    private double stoppedAtTime;
    private Timestamp lastWatched;
    private int completed;

    public WatchHistory(int userId, Integer mediaId, Integer episodeId, double stoppedAtTime, Timestamp lastWatched, int completed) {
        this.userId = userId;
        this.mediaId = mediaId;
        this.episodeId = episodeId;
        this.stoppedAtTime = stoppedAtTime;
        this.lastWatched = lastWatched;
        this.completed = completed;
    }
    public WatchHistory(int userId, Integer mediaId, double stoppedAtTime, Timestamp lastWatched, int completed) {
        this.userId = userId;
        this.mediaId = mediaId;
        this.stoppedAtTime = stoppedAtTime;
        this.lastWatched = lastWatched;
        this.completed = completed;
    }
    public int getUserId() { return userId; }
    public Integer getMediaId() { return mediaId; }
    public Integer getEpisodeId() { return episodeId; }
    public double getStoppedAtTime() { return stoppedAtTime; }
    public Timestamp getLastWatched() { return lastWatched; }
    public int getCompleted() { return completed; }

   
}
