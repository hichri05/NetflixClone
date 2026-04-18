package org.netflix.Services;

import org.netflix.Models.Episode;
import org.netflix.Models.Season;
import java.util.List;

public interface ISerieService {
    List<Season> getSeasonsBySerie(int serieId);
    boolean addSeason(Season season);
    boolean updateSeason(Season season);
    boolean deleteSeason(int seasonId);

    List<Episode> getEpisodesBySeason(int seasonId);
    boolean addEpisode(Episode episode);
    boolean updateEpisode(Episode episode);
    boolean deleteEpisode(int episodeId);

    Episode getNextEpisode(int currentEpisodeId, int seasonId);
    Episode getFirstUnwatchedEpisode(int serieId, int userId);
}