package org.netflix.Models;

import java.sql.Timestamp;

public class WatchHistory {
    private int userId;
    private Integer mediaId;
    private Integer episodeId;
    private double stoppedAtTime;
    private Timestamp lastWatched;
    private  int completed;


    public WatchHistory(int userId, Integer mediaId, Integer episodeId, double stoppedAtTime, Timestamp lastWatched) {
        this.userId = userId;
        this.mediaId = mediaId;
        this.episodeId = episodeId;
        this.stoppedAtTime = stoppedAtTime;
        this.lastWatched = lastWatched;
    }
public WatchHistory(int userId, Integer mediaId, double stoppedAtTime, Timestamp lastWatched) {
    this.userId = userId;
    this.mediaId = mediaId;
    this.stoppedAtTime = stoppedAtTime;
    this.lastWatched = lastWatched;
}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getMediaId() {
        return mediaId;
    }

    public void setMediaId(Integer mediaId) {
        this.mediaId = mediaId;
    }

    public Integer getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(Integer episodeId) {
        this.episodeId = episodeId;
    }

    public double getStoppedAtTime() {
        return stoppedAtTime;
    }

    public void setStoppedAtTime(double stoppedAtTime) {
        this.stoppedAtTime = stoppedAtTime;
    }

    public Timestamp getLastWatched() {
        return lastWatched;
    }

    public void setLastWatched(Timestamp lastWatched) {
        this.lastWatched = lastWatched;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }
}