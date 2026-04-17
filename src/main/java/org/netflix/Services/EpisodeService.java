package org.netflix.Services;

import org.netflix.DAO.EpisodeDAO;
import org.netflix.Models.Episode;
import java.util.List;

public class EpisodeService {

    /**
     * Retrieves all episodes for a specific season.
     * This now includes the thumbnail_path needed for the photos.
     */
    public List<Episode> getEpisodes(int idSaison) {
        return EpisodeDAO.getEpisodesBySeason(idSaison);
    }

    /**
     * Saves a new episode to the database.
     */
    public boolean saveEpisode(Episode ep) {
        if (ep == null) return false;
        return EpisodeDAO.addEpisode(ep);
    }

    /**
     * Deletes an episode by its unique ID.
     */
    public boolean removeEpisode(int id) {
        return EpisodeDAO.deleteEpisode(id);
    }
}