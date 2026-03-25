// package org.netflix.Models;

package org.netflix.Models;

import java.sql.Timestamp;

public class WatchProgress {
    private int id;
    private int userId;
    private int episodeId;
    private double stoppedAtTime;
    private boolean completed;
    private Timestamp lastUpdated;

    // Constructeur par défaut
    public WatchProgress() {}

    // Constructeur avec tous les paramètres
    public WatchProgress(int id, int userId, int episodeId, double stoppedAtTime,
                         boolean completed, Timestamp lastUpdated) {
        this.id = id;
        this.userId = userId;
        this.episodeId = episodeId;
        this.stoppedAtTime = stoppedAtTime;
        this.completed = completed;
        this.lastUpdated = lastUpdated;
    }

    // Constructeur sans id (pour insertion)
    public WatchProgress(int userId, int episodeId, double stoppedAtTime,
                         boolean completed, Timestamp lastUpdated) {
        this.userId = userId;
        this.episodeId = episodeId;
        this.stoppedAtTime = stoppedAtTime;
        this.completed = completed;
        this.lastUpdated = lastUpdated;
    }

    // Constructeur simplifié (pour reprise)
    public WatchProgress(int userId, int episodeId, double stoppedAtTime) {
        this.userId = userId;
        this.episodeId = episodeId;
        this.stoppedAtTime = stoppedAtTime;
        this.completed = false;
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public double getStoppedAtTime() {
        return stoppedAtTime;
    }

    public void setStoppedAtTime(double stoppedAtTime) {
        this.stoppedAtTime = stoppedAtTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Méthodes utilitaires

    /**
     * Retourne la position formatée en HH:MM:SS
     */
    public String getFormattedPosition() {
        int hours = (int) (stoppedAtTime / 3600);
        int minutes = (int) ((stoppedAtTime % 3600) / 60);
        int seconds = (int) (stoppedAtTime % 60);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Retourne le pourcentage de progression
     * @param duration Durée totale en secondes
     * @return Pourcentage (0-100)
     */
    public double getProgressPercentage(double duration) {
        if (duration <= 0) return 0;
        double percentage = (stoppedAtTime / duration) * 100;
        return Math.min(percentage, 100);
    }

    /**
     * Vérifie si l'épisode est terminé (plus de 95%)
     * @param duration Durée totale en secondes
     * @return true si terminé
     */
    public boolean isEpisodeCompleted(double duration) {
        return completed || (duration > 0 && stoppedAtTime >= duration * 0.95);
    }

    /**
     * Marque l'épisode comme terminé
     */
    public void markAsCompleted() {
        this.completed = true;
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Met à jour la progression
     * @param position Nouvelle position
     * @param duration Durée totale
     */
    public void updateProgress(double position, double duration) {
        this.stoppedAtTime = position;
        this.completed = (duration > 0 && position >= duration * 0.95);
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "WatchProgress{" +
                "id=" + id +
                ", userId=" + userId +
                ", episodeId=" + episodeId +
                ", stoppedAtTime=" + stoppedAtTime +
                ", completed=" + completed +
                ", lastUpdated=" + lastUpdated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WatchProgress that = (WatchProgress) o;
        return userId == that.userId && episodeId == that.episodeId;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(userId, episodeId);
    }
}