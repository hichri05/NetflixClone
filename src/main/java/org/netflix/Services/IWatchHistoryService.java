package org.netflix.Services;

import org.netflix.Models.WatchHistory;
import java.util.List;

public interface IWatchHistoryService {
    boolean saveProgress(int userId, int mediaId, Integer episodeId, double stoppedAt);
    boolean markAsCompleted(int userId, int mediaId, Integer episodeId);
    WatchHistory getProgress(int userId, int mediaId, Integer episodeId);
    List<WatchHistory> getUserHistory(int userId);
    boolean isCompleted(int userId, Integer episodeId);
}