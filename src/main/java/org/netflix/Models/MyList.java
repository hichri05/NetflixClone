package org.netflix.Models;

import java.sql.Timestamp;

public class MyList {
    private int userId;
    private Integer movieId;
    private Integer seriesId;
    private Timestamp addedAt;

    public MyList(int userId, Integer movieId, Integer seriesId, Timestamp addedAt) {
        this.userId = userId;
        this.movieId = movieId;
        this.seriesId = seriesId;
        this.addedAt = addedAt;
    }

    public int getUserId() { return userId; }
    public Integer getMovieId() { return movieId; }
    public Integer getSeriesId() { return seriesId; }
    public Timestamp getAddedAt() { return addedAt; }
}
