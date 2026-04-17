package org.netflix.Services;

import org.netflix.DAO.RatingDAO;
import org.netflix.Models.Rating;
import java.time.LocalDate;
import java.util.List;

public class RatingServiceImpl implements IRatingService {

    private final RatingDAO ratingDAO;
    private final IMediaService mediaService;

    public RatingServiceImpl(RatingDAO ratingDAO, IMediaService mediaService) {
        this.ratingDAO = ratingDAO;
        this.mediaService = mediaService;
    }

    @Override
    public boolean rateMedia(int userId, int mediaId, float score) {
        if (score < 1 || score > 5) return false;

        Rating existing = ratingDAO.findByUserAndMedia(userId, mediaId);
        boolean result;
        if (existing != null) {
            existing.setScore(score);
            existing.setRatingDate(LocalDate.now());
            result = ratingDAO.update(existing);
        } else {
            Rating rating = new Rating(userId, mediaId, score, LocalDate.now());
            result = ratingDAO.insert(rating);
        }

        if (result) mediaService.calculateAverageRating(mediaId);
        return result;
    }

    @Override
    public float getUserRatingForMedia(int userId, int mediaId) {
        Rating r = ratingDAO.findByUserAndMedia(userId, mediaId);
        return (r != null) ? r.getScore() : 0f;
    }

    @Override
    public double getAverageRating(int mediaId) {
        return mediaService.calculateAverageRating(mediaId);
    }
}