package org.netflix.Services;

import org.netflix.DAO.WatchHistoryDAO;
import org.netflix.Models.WatchHistory;
import java.sql.Timestamp;
import java.util.List;

public class WatchHistoryServiceImpl implements IWatchHistoryService {

    private final WatchHistoryDAO watchHistoryDAO;

    public WatchHistoryServiceImpl(WatchHistoryDAO watchHistoryDAO) {
        this.watchHistoryDAO = watchHistoryDAO;
    }

    @Override
    public boolean saveProgress(int userId, int mediaId, Integer episodeId, double stoppedAt) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        WatchHistory existing = watchHistoryDAO.find(userId, mediaId, episodeId);
        if (existing != null) {
            return watchHistoryDAO.updateProgress(userId, mediaId, episodeId, stoppedAt, now);
        } else {
            WatchHistory wh = new WatchHistory(userId, mediaId, episodeId, stoppedAt, now, 0);
            return watchHistoryDAO.insert(wh);
        }
    }

    @Override
    public boolean markAsCompleted(int userId, int mediaId, Integer episodeId) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return watchHistoryDAO.updateCompleted(userId, mediaId, episodeId, now);
    }

    @Override
    public WatchHistory getProgress(int userId, int mediaId, Integer episodeId) {
        return watchHistoryDAO.find(userId, mediaId, episodeId);
    }

    @Override
    public List<WatchHistory> getUserHistory(int userId) {
        return watchHistoryDAO.findByUser(userId);
    }

    @Override
    public boolean isCompleted(int userId, Integer episodeId) {
        return watchHistoryDAO.isEpisodeCompleted(userId, episodeId);
    }
}