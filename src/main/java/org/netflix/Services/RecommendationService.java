/*package org.netflix.Services;

import org.netflix.DAO.WatchHistoryDAO;
import org.netflix.Models.Media;

import java.util.*;

public class RecommendationService {

    public static List<Media> getRecommendations(int userId, int limit) {
        List<Media> recommendations = new ArrayList<>();
        Set<Integer> addedIds = new HashSet<>();

        // 1. Build genre score map
        Map<Integer, Integer> scores = new HashMap<>();
        merge(scores, WatchHistoryDAO.getGenreScoresFromWatchHistory(userId));
        merge(scores, WatchHistoryDAO.getGenreScoresFromFavorites(userId));
        merge(scores, WatchHistoryDAO.getGenreScoresFromRatings(userId));

        // 2. Get already seen media to exclude
        Set<Integer> seen = WatchHistoryDAO.getSeenMediaIds(userId);

        // 3. Recommend by top genres
        if (!scores.isEmpty()) {
            List<Integer> topGenres = getTopGenres(scores, 3);
            for (Media m : WatchHistoryDAO.getMediaByGenres(topGenres, seen, limit)) {
                if (addedIds.add(m.getIdMedia())) recommendations.add(m);
            }
        }

        // 4. Fill with top rated unseen
        if (recommendations.size() < limit) {
            Set<Integer> exclude = new HashSet<>(seen);
            exclude.addAll(addedIds);
            for (Media m : WatchHistoryDAO.getTopRatedUnseen(exclude, limit - recommendations.size())) {
                if (addedIds.add(m.getIdMedia())) recommendations.add(m);
            }
        }

        // 5. Trending fallback
        if (recommendations.size() < limit) {
            Set<Integer> exclude = new HashSet<>(seen);
            exclude.addAll(addedIds);
            recommendations.addAll(WatchHistoryDAO.getTrending(exclude, limit - recommendations.size()));
        }

        return recommendations;
    }

    private static void merge(Map<Integer, Integer> base, Map<Integer, Integer> toAdd) {
        toAdd.forEach((k, v) -> base.merge(k, v, Integer::sum));
    }

    private static List<Integer> getTopGenres(Map<Integer, Integer> scores, int top) {
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(top)
                .map(Map.Entry::getKey)
                .toList();
    }
}*/