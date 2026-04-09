package org.netflix.Services;

import org.netflix.Models.Media;
import java.util.List;

public interface IFavoriteService {
    boolean addToFavorites(int userId, int mediaId);
    boolean removeFromFavorites(int userId, int mediaId);
    List<Media> getFavorites(int userId);
    boolean isFavorite(int userId, int mediaId);
}