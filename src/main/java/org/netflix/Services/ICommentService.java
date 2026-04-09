package org.netflix.Services;

import org.netflix.Models.Comment;
import java.util.List;

public interface ICommentService {
    boolean addComment(int userId, int mediaId, String content);
    List<Comment> getCommentsByMedia(int mediaId);
    boolean deleteComment(int commentId);
    boolean reportComment(int commentId);
    List<Comment> getReportedComments(); // Admin modération
}