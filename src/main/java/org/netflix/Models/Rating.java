package org.netflix.Models;

import java.time.LocalDate;

public class Rating {
    public int id_Rating;
    public int id_User;
    public int id_Media;
    public float score;
    public LocalDate ratingDate;
    public Rating(int id_Rating, int id_User, int id_Media, float score, LocalDate ratingDate) {
        this.id_Rating = id_Rating;
        this.id_User = id_User;
        this.id_Media = id_Media;
        this.score = score;
        this.ratingDate = ratingDate;
    }
    public Rating(int id_User, int id_Media, float score, LocalDate ratingDate) {
        this.id_User = id_User;
        this.id_Media = id_Media;
        this.score = score;
        this.ratingDate = ratingDate;
    }
    public int getId_Rating() { return id_Rating; }
    public int getId_User() { return id_User; }
    public int getId_Media() { return id_Media; }
    public float getScore() { return score; }
    public LocalDate getRatingDate() { return ratingDate; }
    public void setId_Rating(int id_Rating) { this.id_Rating = id_Rating; }
    public void setId_User(int id_User) { this.id_User = id_User; }
    public void setId_Media(int id_Media) { this.id_Media = id_Media; }
    public void setScore(float score) { this.score = score; }
    public void setRatingDate(LocalDate ratingDate) { this.ratingDate = ratingDate; }
}