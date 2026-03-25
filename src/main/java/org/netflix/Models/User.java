package org.netflix.Models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String email;
    private Timestamp createdAt;
    private List<Media> favorites = new ArrayList<>();

    public User(int id, String username, String email, Timestamp createdAt, List<Media> favorites) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.favorites = favorites;
    }

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public List<Media> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Media> favorites) {
        this.favorites = favorites;
    }
    
    @Override
    public String toString() {
        return "User{" + "id=" + id + ", username=" + username + ", email=" + email + '}';
    }
}
