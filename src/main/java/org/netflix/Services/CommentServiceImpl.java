package org.netflix.Services;

import org.netflix.DAO.CommentDAO;
import org.netflix.Models.Comment;
import java.util.List;

public class CommentServiceImpl implements ICommentService {

    private final CommentDAO commentDAO;

    public CommentServiceImpl(CommentDAO commentDAO) {
        this.commentDAO = commentDAO;
    }

    @Override
    public boolean addComment(int userId, int mediaId, String content) {
        if (content == null || content.isBlank()) return false;
        if (content.length() > 500) return false; // règle métier : max 500 chars
        Comment comment = new Comment(userId, mediaId, content);
        return commentDAO.insert(comment);
    }

    @Override
    public List<Comment> getCommentsByMedia(int mediaId) {
        return commentDAO.findByMedia(mediaId);
    }

    @Override
    public boolean deleteComment(int commentId) {
        return commentDAO.delete(commentId);
    }

    @Override
    public boolean reportComment(int commentId) {
        return commentDAO.markAsReported(commentId);
    }

    @Override
    public List<Comment> getReportedComments() {
        return commentDAO.findReported();
    }
}