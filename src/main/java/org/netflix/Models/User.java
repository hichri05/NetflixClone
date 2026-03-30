package org.netflix.Models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String role;
    private Timestamp createdAt;
    private List<Media> favorites = new ArrayList<>();

    public User(int id, String username, String email,String role, Timestamp createdAt, List<Media> favorites) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.role = role;
        this.favorites = favorites;
    }

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User() {

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

    public String  getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
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

    public void setPassword(String password) {
        this.password = password;
    }
}
