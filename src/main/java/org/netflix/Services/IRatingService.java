package org.netflix.Services;

import org.netflix.Models.Rating;

public interface IRatingService {
    boolean rateMedia(int userId, int mediaId, float score);
    float getUserRatingForMedia(int userId, int mediaId);
    double getAverageRating(int mediaId);
}