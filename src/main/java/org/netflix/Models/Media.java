package org.netflix.Models;

import java.util.ArrayList;
import java.util.List;

public class Media {
    protected int idMedia;
    protected String title;
    protected String description;
    protected int releaseYear;
    protected double averageRating;
    protected String coverImageUrl;
    protected String backdropImageUrl;
    protected String director;
    protected String type;
    protected List<Genre> genres;
    protected List<Acteur> casting = new ArrayList<>();
    protected int views;


    public Media(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String backdropImageUrl,String director,List<Genre> genres,List<Acteur> casting,int views, String type) {
        this.idMedia = idMedia;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.averageRating = averageRating;
        this.coverImageUrl = coverImageUrl;
        this.backdropImageUrl = backdropImageUrl;
        this.director = director;
        this.type = type;
        this.genres = genres;
        this.casting=casting;
        this.views=views;
        this.type=type;
    }
    public Media(int idMedia, String title, String description, int releaseYear,
                 double averageRating, String coverImageUrl, String backdropImageUrl,String director, String type, List<Genre> genres) {
        this.idMedia = idMedia;
        this.title = title;
        this.description = description;
        this.releaseYear = releaseYear;
        this.averageRating = averageRating;
        this.coverImageUrl = coverImageUrl;
        this.backdropImageUrl = backdropImageUrl;
        this.director = director;
        this.views=views;
        this.type=type;

        this.genres = genres;
    }
    public  Media() {}

    public Media(String title, String description, String coverImageUrl) {
        this.title = title;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
    }

    public int getIdMedia() { return idMedia; }
    public String getTitle() { return title; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public String getBackdropImageUrl() {return backdropImageUrl;}
    public String getDescription() { return description; }
    public int getReleaseYear(){ return releaseYear;}
    public double getAverageRating(){ return averageRating;}
    public String getDirector() {return director;}
    public List<Genre> getGenres() { return genres; }
    public List<Acteur> getCasting() { return casting; }
    public void setCasting(List<Acteur> casting) { this.casting = casting; }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIdMedia(int idMedia) {
        this.idMedia = idMedia;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setBackdropImageUrl(String backdropImageUrl) {
        this.backdropImageUrl = backdropImageUrl;
    }

    public String getCoverUrl() {
        return coverImageUrl;
    }
    public String getBackDropImageUrl(){
        return backdropImageUrl;
    }

    public int getViews() {
        return views;
    }


}