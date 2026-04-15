package org.netflix.Services;

import org.netflix.DAO.EpisodeDAO;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.MovieDAO;
import org.netflix.DAO.SerieDAO;
import org.netflix.Models.Episode;
import org.netflix.Models.Media;
import org.netflix.Models.Movie;
import org.netflix.Models.Serie;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CatalogService {


    public Movie getTrendingMedia() {
        Movie trend = MovieDAO.getTrendMovie();
        if (trend == null) {
            System.err.println("Aucun film tendance trouvé.");
        }
        return trend;
    }

    public List<Media> getTopTenMostWatched() {
        return MediaDAO.getTopViews();
    }

    public List<Media> getMediasByGenre(String genreName) {
        List<Media> allMedias = new ArrayList<>();
        allMedias.addAll(MovieDAO.findbyGenre(genreName));
        allMedias.addAll(SerieDAO.findbyGenre(genreName));
        return allMedias;
    }

    public List<Media> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return MediaDAO.searchMedia(query);
    }
    public List<Serie> getAllSeriesByGenre(String genreName) {
        return SerieDAO.findbyGenre(genreName);
    }


        private final EpisodeDAO episodeDAO = new EpisodeDAO();

        public List<Episode> fetchEpisodes(int idseason) {
            return episodeDAO.getEpisodesBySeason( idseason);
        }

        public void createEpisode(int mediaId, int seasonNum, int epNum, String title, int duration, String url) throws SQLException {
            if (title == null || title.isEmpty()) throw new IllegalArgumentException("Title cannot be empty");

            Episode ep = new Episode();
            ep.setSeasonId(seasonNum);
            ep.setEpisodeNumber(epNum);
            ep.setTitle(title);
            ep.setDuration(duration);
            ep.setFilePath(url);

            episodeDAO.addEpisode(ep);
        }

        public void removeEpisode(int episodeId) throws SQLException {
            episodeDAO.deleteEpisode(episodeId);
        }
    }
