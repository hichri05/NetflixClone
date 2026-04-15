package org.netflix.Services;

import org.netflix.DAO.EpisodeDAO;
import org.netflix.Models.Episode;
import java.util.List;

public class EpisodeService {
    public List<Episode> getEpisodes(int idSaison) {
        return EpisodeDAO.getEpisodesBySeason(idSaison);
    }

    public boolean saveEpisode(Episode ep) {
        return EpisodeDAO.addEpisode(ep);
    }

    public boolean removeEpisode(int id) {
        return EpisodeDAO.deleteEpisode(id);
    }
}