package org.netflix.Services;

import org.netflix.DAO.MediaDAO;
import org.netflix.Models.Media;

import java.util.List;

public class MediaService {

    public List<Media> getAllMedia() {
        return MediaDAO.getAllMedia();
    }

    public List<Media> searchMedia(String query) {
        return MediaDAO.searchMedia(query);
    }

    public List<Media> getMediaByType(String type) {
        return MediaDAO.getAllMedia().stream()
                .filter(m -> m.getType().equalsIgnoreCase(type))
                .collect(java.util.stream.Collectors.toList());
    }

    public boolean addMedia(Media media) {
        return MediaDAO.addMedia(media);
    }

    public boolean updateMedia(Media media) {
        return MediaDAO.updateMedia(media);
    }

    public boolean deleteMedia(int idMedia) {
        return MediaDAO.deleteMedia(idMedia);
    }
}

