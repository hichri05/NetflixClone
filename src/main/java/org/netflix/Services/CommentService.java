// package org.netflix.Services;

package org.netflix.Services;

import org.netflix.DAO.CommentDAO;
import org.netflix.DAO.MovieDAO;
import org.netflix.DAO.SerieDAO;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.Comment;
import org.netflix.Models.User;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CommentService {

    private final CommentDAO commentDAO;
    private final MovieDAO movieDAO;
    private final SerieDAO serieDAO;
    private final UserDAO userDAO;

    public CommentService() {
        this.commentDAO = new CommentDAO();
        this.movieDAO = new MovieDAO();
        this.serieDAO = new SerieDAO();
        this.userDAO = new UserDAO();
    }

    // ==================== AJOUT ET SUPPRESSION DE COMMENTAIRES ====================

    /**
     * Ajoute un commentaire sur un média
     * @param userId ID de l'utilisateur
     * @param mediaId ID du média
     * @param content Contenu du commentaire
     * @return Optional contenant le commentaire créé ou vide si erreur
     */
    public Optional<Comment> addComment(int userId, int mediaId, String content) {
        // 1. Validation du contenu
        ValidationResult validation = validateCommentContent(content);
        if (!validation.isValid()) {
            System.err.println("Erreur validation: " + validation.getMessage());
            return Optional.empty();
        }

        // 2. Vérifier que le média existe
        if (!mediaExists(mediaId)) {
            System.err.println("Média non trouvé: " + mediaId);
            return Optional.empty();
        }

        // 3. Vérifier que l'utilisateur existe
        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            System.err.println("Utilisateur non trouvé: " + userId);
            return Optional.empty();
        }

        // 4. Créer le commentaire
        Comment comment = new Comment(
                0,
                userId,
                mediaId,
                content.trim(),
                false, // non signalé
                Timestamp.valueOf(LocalDateTime.now())
        );

        // 5. Sauvegarder en base
        boolean saved = commentDAO.insert(comment);

        if (saved) {
            System.out.println("Commentaire ajouté par: " + userOpt.get().getUsername());
            return Optional.of(comment);
        }

        return Optional.empty();
    }

    /**
     * Supprime un commentaire (par l'auteur ou par un admin)
     * @param commentId ID du commentaire
     * @param userId ID de l'utilisateur qui demande la suppression
     * @return true si suppression réussie
     */
    public boolean deleteComment(int commentId, int userId) {
        Optional<Comment> commentOpt = commentDAO.findById(commentId);

        if (commentOpt.isEmpty()) {
            System.err.println("Commentaire non trouvé: " + commentId);
            return false;
        }

        Comment comment = commentOpt.get();

        // Vérifier les droits : l'auteur ou un admin peuvent supprimer
        boolean isAuthor = comment.getUserId() == userId;
        boolean isAdmin = userDAO.isAdmin(userId);

        if (!isAuthor && !isAdmin) {
            System.err.println("Suppression non autorisée par l'utilisateur: " + userId);
            return false;
        }

        boolean deleted = commentDAO.delete(commentId);

        if (deleted) {
            System.out.println("Commentaire " + commentId + " supprimé par utilisateur " + userId);
        }

        return deleted;
    }

    /**
     * Supprime tous les commentaires d'un utilisateur
     * @param userId ID de l'utilisateur
     * @param adminId ID de l'admin qui effectue la suppression
     * @return Nombre de commentaires supprimés
     */
    public int deleteAllUserComments(int userId, int adminId) {
        // Vérifier que l'admin existe et a les droits
        if (!userDAO.isAdmin(adminId)) {
            System.err.println("Action non autorisée: utilisateur " + adminId + " n'est pas admin");
            return 0;
        }

        return commentDAO.deleteByUser(userId);
    }

    // ==================== RÉCUPÉRATION DES COMMENTAIRES ====================

    /**
     * Récupère tous les commentaires d'un média (non signalés)
     * @param mediaId ID du média
     * @return Liste des commentaires triés par date (plus récent d'abord)
     */
    public List<CommentWithUser> getCommentsByMedia(int mediaId) {
        List<Comment> comments = commentDAO.findByMedia(mediaId);

        // Filtrer les commentaires signalés (ne pas les afficher aux utilisateurs normaux)
        comments = comments.stream()
                .filter(c -> !c.isReported())
                .collect(Collectors.toList());

        return enrichCommentsWithUserInfo(comments);
    }

    /**
     * Récupère tous les commentaires d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des commentaires de l'utilisateur
     */
    public List<CommentWithUser> getCommentsByUser(int userId) {
        List<Comment> comments = commentDAO.findByUser(userId);
        return enrichCommentsWithUserInfo(comments);
    }

    /**
     * Récupère un commentaire par son ID
     * @param commentId ID du commentaire
     * @return Optional contenant le commentaire avec infos utilisateur
     */
    public Optional<CommentWithUser> getCommentById(int commentId) {
        Optional<Comment> commentOpt = commentDAO.findById(commentId);

        if (commentOpt.isEmpty()) {
            return Optional.empty();
        }

        Comment comment = commentOpt.get();
        Optional<User> userOpt = userDAO.findById(comment.getUserId());

        String username = userOpt.map(User::getUsername).orElse("Utilisateur inconnu");

        return Optional.of(new CommentWithUser(
                comment,
                username,
                getMediaTitle(comment.getMediaId())
        ));
    }

    /**
     * Récupère les derniers commentaires (pour la page d'accueil)
     * @param limit Nombre maximum de commentaires
     * @return Liste des derniers commentaires
     */
    public List<CommentWithUser> getRecentComments(int limit) {
        List<Comment> comments = commentDAO.findRecentComments(limit);

        // Filtrer les commentaires signalés
        comments = comments.stream()
                .filter(c -> !c.isReported())
                .collect(Collectors.toList());

        return enrichCommentsWithUserInfo(comments);
    }

    // ==================== MODÉRATION ====================

    /**
     * Signale un commentaire (par un utilisateur)
     * @param commentId ID du commentaire
     * @param reporterId ID de l'utilisateur qui signale
     * @return true si signalement réussi
     */
    public boolean reportComment(int commentId, int reporterId) {
        Optional<Comment> commentOpt = commentDAO.findById(commentId);

        if (commentOpt.isEmpty()) {
            System.err.println("Commentaire non trouvé: " + commentId);
            return false;
        }

        // Un utilisateur ne peut pas signaler son propre commentaire
        if (commentOpt.get().getUserId() == reporterId) {
            System.err.println("Un utilisateur ne peut pas signaler son propre commentaire");
            return false;
        }

        return commentDAO.markAsReported(commentId);
    }

    /**
     * Récupère tous les commentaires signalés (pour modération admin)
     * @return Liste des commentaires signalés
     */
    public List<CommentWithUser> getReportedComments() {
        List<Comment> reportedComments = commentDAO.findReportedComments();
        return enrichCommentsWithUserInfo(reportedComments);
    }

    /**
     * Approuve un commentaire signalé (le retire de la liste des signalés)
     * @param commentId ID du commentaire
     * @param adminId ID de l'admin qui approuve
     * @return true si approbation réussie
     */
    public boolean approveComment(int commentId, int adminId) {
        // Vérifier que l'utilisateur est admin
        if (!userDAO.isAdmin(adminId)) {
            System.err.println("Action non autorisée: utilisateur " + adminId + " n'est pas admin");
            return false;
        }

        return commentDAO.unmarkAsReported(commentId);
    }

    /**
     * Supprime un commentaire signalé (modération admin)
     * @param commentId ID du commentaire
     * @param adminId ID de l'admin qui supprime
     * @return true si suppression réussie
     */
    public boolean moderateComment(int commentId, int adminId) {
        // Vérifier que l'utilisateur est admin
        if (!userDAO.isAdmin(adminId)) {
            System.err.println("Action non autorisée: utilisateur " + adminId + " n'est pas admin");
            return false;
        }

        return commentDAO.delete(commentId);
    }

    /**
     * Supprime tous les commentaires signalés (modération massive)
     * @param adminId ID de l'admin qui effectue la suppression
     * @return Nombre de commentaires supprimés
     */
    public int deleteAllReportedComments(int adminId) {
        if (!userDAO.isAdmin(adminId)) {
            System.err.println("Action non autorisée: utilisateur " + adminId + " n'est pas admin");
            return 0;
        }

        return commentDAO.deleteReportedComments();
    }

    // ==================== STATISTIQUES ====================

    /**
     * Compte le nombre de commentaires pour un média
     * @param mediaId ID du média
     * @return Nombre de commentaires
     */
    public int getCommentCount(int mediaId) {
        return commentDAO.countByMedia(mediaId);
    }

    /**
     * Compte le nombre de commentaires signalés
     * @return Nombre de commentaires signalés
     */
    public int getReportedCommentCount() {
        return commentDAO.countReported();
    }

    /**
     * Récupère les statistiques de commentaires pour un média
     * @param mediaId ID du média
     * @return Statistiques des commentaires
     */
    public CommentStatistics getCommentStatistics(int mediaId) {
        int totalComments = commentDAO.countByMedia(mediaId);
        int reportedComments = commentDAO.countReportedByMedia(mediaId);

        return new CommentStatistics(
                mediaId,
                getMediaTitle(mediaId),
                totalComments,
                reportedComments
        );
    }

    // ==================== MÉTHODES DE VALIDATION ====================

    /**
     * Valide le contenu d'un commentaire
     * @param content Contenu du commentaire
     * @return Résultat de validation
     */
    private ValidationResult validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return ValidationResult.invalid("Le commentaire ne peut pas être vide");
        }

        if (content.length() < 3) {
            return ValidationResult.invalid("Le commentaire doit contenir au moins 3 caractères");
        }

        if (content.length() > 1000) {
            return ValidationResult.invalid("Le commentaire ne peut pas dépasser 1000 caractères");
        }

        // Vérifier les mots interdits (optionnel)
        List<String> forbiddenWords = getForbiddenWords();
        String lowerContent = content.toLowerCase();
        for (String word : forbiddenWords) {
            if (lowerContent.contains(word)) {
                return ValidationResult.invalid("Le commentaire contient des mots inappropriés");
            }
        }

        return ValidationResult.valid();
    }

    /**
     * Liste des mots interdits (à configurer selon besoin)
     */
    private List<String> getForbiddenWords() {
        return Arrays.asList(
                "insulte", "spam", "haine", "raciste"
        );
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Vérifie si un média existe (film ou série)
     */
    private boolean mediaExists(int mediaId) {
        return movieDAO.findById(mediaId).isPresent() ||
                serieDAO.findById(mediaId).isPresent();
    }

    /**
     * Récupère le titre d'un média
     */
    private String getMediaTitle(int mediaId) {
        Optional<Movie> movieOpt = movieDAO.findById(mediaId);
        if (movieOpt.isPresent()) {
            return movieOpt.get().getTitle();
        }

        Optional<Serie> serieOpt = serieDAO.findById(mediaId);
        if (serieOpt.isPresent()) {
            return serieOpt.get().getTitle();
        }

        return "Média inconnu";
    }

    /**
     * Enrichit une liste de commentaires avec les informations utilisateur
     */
    private List<CommentWithUser> enrichCommentsWithUserInfo(List<Comment> comments) {
        List<CommentWithUser> enrichedComments = new ArrayList<>();

        for (Comment comment : comments) {
            Optional<User> userOpt = userDAO.findById(comment.getUserId());
            String username = userOpt.map(User::getUsername).orElse("Utilisateur inconnu");

            enrichedComments.add(new CommentWithUser(
                    comment,
                    username,
                    getMediaTitle(comment.getMediaId())
            ));
        }

        // Trier par date (plus récent d'abord)
        enrichedComments.sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()));

        return enrichedComments;
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Classe pour les commentaires avec informations utilisateur
     */
    public static class CommentWithUser {
        private final int id;
        private final int userId;
        private final String username;
        private final int mediaId;
        private final String mediaTitle;
        private final String content;
        private final boolean reported;
        private final Timestamp createdAt;

        public CommentWithUser(Comment comment, String username, String mediaTitle) {
            this.id = comment.getId();
            this.userId = comment.getUserId();
            this.username = username;
            this.mediaId = comment.getMediaId();
            this.mediaTitle = mediaTitle;
            this.content = comment.getContent();
            this.reported = comment.isReported();
            this.createdAt = comment.getCreatedAt();
        }

        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public int getMediaId() { return mediaId; }
        public String getMediaTitle() { return mediaTitle; }
        public String getContent() { return content; }
        public boolean isReported() { return reported; }
        public Timestamp getCreatedAt() { return createdAt; }

        public String getTimeAgo() {
            long diff = System.currentTimeMillis() - createdAt.getTime();
            long minutes = diff / (60 * 1000);
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 0) return days + " jour" + (days > 1 ? "s" : "");
            if (hours > 0) return hours + " heure" + (hours > 1 ? "s" : "");
            if (minutes > 0) return minutes + " minute" + (minutes > 1 ? "s" : "");
            return "À l'instant";
        }

        public String getExcerpt(int maxLength) {
            if (content.length() <= maxLength) {
                return content;
            }
            return content.substring(0, maxLength) + "...";
        }

        @Override
        public String toString() {
            return username + ": " + content;
        }
    }

    /**
     * Classe pour les statistiques des commentaires
     */
    public static class CommentStatistics {
        private final int mediaId;
        private final String mediaTitle;
        private final int totalComments;
        private final int reportedComments;

        public CommentStatistics(int mediaId, String mediaTitle, int totalComments, int reportedComments) {
            this.mediaId = mediaId;
            this.mediaTitle = mediaTitle;
            this.totalComments = totalComments;
            this.reportedComments = reportedComments;
        }

        public int getMediaId() { return mediaId; }
        public String getMediaTitle() { return mediaTitle; }
        public int getTotalComments() { return totalComments; }
        public int getReportedComments() { return reportedComments; }
        public int getCleanComments() { return totalComments - reportedComments; }
        public double getReportedPercentage() {
            if (totalComments == 0) return 0;
            return (reportedComments * 100.0) / totalComments;
        }
    }

    /**
     * Classe interne pour les résultats de validation
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}