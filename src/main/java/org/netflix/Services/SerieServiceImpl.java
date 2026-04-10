/*package org.netflix.Services;

import org.netflix.DAO.EpisodeDAO;
import org.netflix.DAO.SeasonDAO;
import org.netflix.DAO.WatchHistoryDAO;
import org.netflix.Models.Episode;
import org.netflix.Models.Season;
import java.util.List;

public class SerieServiceImpl implements ISerieService {

    private final SeasonDAO seasonDAO;
    private final EpisodeDAO episodeDAO;
    private final WatchHistoryDAO watchHistoryDAO;

    public SerieServiceImpl(SeasonDAO seasonDAO, EpisodeDAO episodeDAO, WatchHistoryDAO watchHistoryDAO) {
        this.seasonDAO = seasonDAO;
        this.episodeDAO = episodeDAO;
        this.watchHistoryDAO = watchHistoryDAO;
    }

    @Override
    public List<Season> getSeasonsBySerie(int serieId) {
        return seasonDAO.findBySerieId(serieId);
    }

    @Override
    public boolean addSeason(Season season) {
        if (season == null) return false;
        return seasonDAO.insert(season);
    }

    @Override
    public boolean updateSeason(Season season) {
        if (season == null) return false;
        return seasonDAO.update(season);
    }

    @Override
    public boolean deleteSeason(int seasonId) {
        return seasonDAO.delete(seasonId);
    }

    @Override
    public List<Episode> getEpisodesBySeason(int seasonId) {
        return episodeDAO.findBySeasonId(seasonId);
    }

    @Override
    public boolean addEpisode(Episode episode) {
        if (episode == null || episode.getTitle() == null) return false;
        return episodeDAO.insert(episode);
    }

    @Override
    public boolean updateEpisode(Episode episode) {
        if (episode == null) return false;
        return episodeDAO.update(episode);
    }

    @Override
    public boolean deleteEpisode(int episodeId) {
        return episodeDAO.delete(episodeId);
    }

    @Override
    public Episode getNextEpisode(int currentEpisodeId, int seasonId) {
        // Règle métier : chercher l'épisode avec episodeNumber + 1
        Episode current = episodeDAO.findById(currentEpisodeId);
        if (current == null) return null;
        return episodeDAO.findBySeasonAndNumber(seasonId, current.getEpisodeNumber() + 1);
    }

    @Override
    public Episode getFirstUnwatchedEpisode(int serieId, int userId) {
        // Règle métier : parcourir les saisons et épisodes dans l'ordre
        List<Season> seasons = seasonDAO.findBySerieId(serieId);
        for (Season season : seasons) {
            List<Episode> episodes = episodeDAO.findBySeasonId(season.getIdSeason());
            for (Episode ep : episodes) {
                boolean watched = watchHistoryDAO.isEpisodeCompleted(userId, ep.getId());
                if (!watched) return ep;
            }
        }
        return null; // Tout vu
    }
}*/