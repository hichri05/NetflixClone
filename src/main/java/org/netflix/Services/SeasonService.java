package org.netflix.Services;

import org.netflix.DAO.SeasonDAO;
import org.netflix.Models.Season;
import java.util.List;

public class SeasonService {
    public List<Season> getSeasonsForSerie(int idSerie) {
        return SeasonDAO.getSeasonsBySerie(idSerie);
    }

    public boolean createSeason(Season season) {
        return SeasonDAO.addSeason(season);
    }

    public boolean deleteSeason(int idSaison) {
        return SeasonDAO.deleteSeason(idSaison);
    }
}