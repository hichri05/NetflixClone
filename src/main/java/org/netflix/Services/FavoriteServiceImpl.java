package org.netflix.Services;

import org.netflix.DAO.FavoriteDAO;
import org.netflix.Models.Media;
import java.util.List;

public class FavoriteServiceImpl implements IFavoriteService {

    private final FavoriteDAO favoriteDAO;

    public FavoriteServiceImpl(FavoriteDAO favoriteDAO) {
        this.favoriteDAO = favoriteDAO;
    }

    @Override
    public boolean addToFavorites(int userId, int mediaId) {
        if (isFavorite(userId, mediaId)) return false; // déjà dans favoris
        return favoriteDAO.insert(userId, mediaId);
    }

    @Override
    public boolean removeFromFavorites(int userId, int mediaId) {
        return favoriteDAO.delete(userId, mediaId);
    }

    @Override
    public List<Media> getFavorites(int userId) {
        return favoriteDAO.findByUser(userId);
    }

    @Override
    public boolean isFavorite(int userId, int mediaId) {
        return favoriteDAO.exists(userId, mediaId);
    }
}