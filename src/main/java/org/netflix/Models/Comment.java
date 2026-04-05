package org.netflix.Models;

import java.time.LocalDate;

public class Comment {
    public int id_Comment;
    public int id_User;
    public int id_Media;
    public String content;
    public LocalDate created_at;
    public int is_reported;
    public Comment(int id_Comment, int id_User, int id_Media, String content, LocalDate created_at, int is_reported) {
        this.id_Comment = id_Comment;
        this.id_User = id_User;
        this.id_Media = id_Media;
        this.content = content;
        this.created_at = created_at;
        this.is_reported = is_reported;
    }
    public Comment(int id_User, int id_Media, String content) {
        this.id_User = id_User;
        this.id_Media = id_Media;
        this.content = content;
        this.created_at = LocalDate.now();
        this.is_reported = 0; // Par défaut, un commentaire n'est pas signalé
    }
    public int getId_Comment() { return id_Comment; }
    public int getUserId() { return id_User; }
    public int getMediaId() { return id_Media; }
    public String getContent() { return content; }
    public LocalDate getCreated_at() { return created_at; }
    public int isReported() { return is_reported; }
    public void setId(int id_Comment) { this.id_Comment = id_Comment; }
    public void setId_User(int id_User) { this.id_User = id_User; }
    public void setId_Media(int id_Media) { this.id_Media = id_Media; }
    public void setContent(String content) { this.content = content; }
    public void getCreatedAt(LocalDate created_at) { this.created_at = created_at; }
    public void setIs_reported(int is_reported) { this.is_reported = is_reported; }
}
