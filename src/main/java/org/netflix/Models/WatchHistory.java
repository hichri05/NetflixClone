package org.netflix.Models;

import java.sql.Timestamp;

public class WatchHistory {
    private int userId;
    private Integer movieId;
    private Integer episodeId;
    private double stoppedAtTime;
    private Timestamp lastWatched;

    public WatchHistory(int userId, Integer movieId, Integer episodeId, double stoppedAtTime, Timestamp lastWatched) {
        this.userId = userId;
        this.movieId = movieId;
        this.episodeId = episodeId;
        this.stoppedAtTime = stoppedAtTime;
        this.lastWatched = lastWatched;
    }
}
