package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.UserDAO;
import org.netflix.DAO.WatchHistoryDAO;
import org.netflix.Models.Media;
import org.netflix.Models.User;
import org.netflix.Models.WatchHistory;
import org.netflix.Utils.SceneSwitcher;
import org.netflix.Utils.Session;
import org.netflix.Utils.TransferData;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class ProfileController {

    // ── Hero ──────────────────────────────────────────────────────────────────
    @FXML private Label avatarLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label memberSinceLabel;

    // ── Stats ─────────────────────────────────────────────────────────────────
    @FXML private Label watchedCountLabel;
    @FXML private Label favoritesCountLabel;
    @FXML private Label ratingsCountLabel;

    // ── Password ──────────────────────────────────────────────────────────────
    @FXML private PasswordField currentPwdField;
    @FXML private PasswordField newPwdField;
    @FXML private PasswordField confirmPwdField;
    @FXML private Label         pwdStatusLabel;

    // ── Favorites ─────────────────────────────────────────────────────────────
    @FXML private FlowPane favoritesGrid;
    @FXML private Label    favCountChip;
    @FXML private VBox     emptyFavorites;

    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        User user = Session.getUser();
        if (user == null) return;

        loadUserInfo(user);
        loadStats(user);
        loadFavorites(user);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  USER INFO
    // ══════════════════════════════════════════════════════════════════════════

    private void loadUserInfo(User user) {
        String name = user.getUsername() != null ? user.getUsername() : "?";
        usernameLabel.setText(name);
        emailLabel.setText(user.getEmail() != null ? user.getEmail() : "");
        avatarLabel.setText(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());

        // Role badge
        String role = user.getRole() != null ? user.getRole().toUpperCase() : "USER";
        roleLabel.setText(role);
        if ("ADMIN".equals(role)) {
            roleLabel.setStyle("-fx-background-color: #3a0000; -fx-text-fill: #e50914;" +
                    "-fx-font-size: 10px; -fx-font-weight: bold;" +
                    "-fx-padding: 3 10 3 10; -fx-background-radius: 12;");
        }

        if (user.getCreatedAt() != null) {
            memberSinceLabel.setText("Member since " +
                    user.getCreatedAt().toLocalDateTime().toLocalDate().toString());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  STATS
    // ══════════════════════════════════════════════════════════════════════════

    private void loadStats(User user) {
        // Watch count: unique media watched
        List<WatchHistory> history = new WatchHistoryDAO().findByUser(user.getId());
        Set<Integer> watchedMediaIds = new HashSet<>();
        int ratingCount = 0;

        for (WatchHistory wh : history) {
            if (wh.getMediaId() != null) watchedMediaIds.add(wh.getMediaId());
        }

        watchedCountLabel.setText(String.valueOf(watchedMediaIds.size()));

        // Favorites count
        List<Media> favorites = UserDAO.getUserFavorites(user.getId());
        favoritesCountLabel.setText(String.valueOf(favorites != null ? favorites.size() : 0));

        // Rating count (approximate from MediaDAO)
        ratingsCountLabel.setText("—");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FAVORITES GRID
    // ══════════════════════════════════════════════════════════════════════════

    private void loadFavorites(User user) {
        favoritesGrid.getChildren().clear();
        List<Media> favorites = UserDAO.getUserFavorites(user.getId());

        if (favorites == null || favorites.isEmpty()) {
            emptyFavorites.setVisible(true);
            emptyFavorites.setManaged(true);
            favCountChip.setText("0 titles");
            return;
        }

        emptyFavorites.setVisible(false);
        emptyFavorites.setManaged(false);
        favCountChip.setText(favorites.size() + " title" + (favorites.size() == 1 ? "" : "s"));

        for (Media m : favorites) {
            favoritesGrid.getChildren().add(buildFavoriteCard(m, user));
        }
    }

    private VBox buildFavoriteCard(Media media, User user) {
        VBox card = new VBox(6);
        card.setCursor(Cursor.HAND);
        card.setPrefWidth(130);

        StackPane thumbBox = new StackPane();

        ImageView poster = new ImageView();
        poster.setFitWidth(130); poster.setFitHeight(190);
        poster.setPreserveRatio(false);
        try {
            String url = media.getCoverImageUrl();
            if (url != null && !url.isBlank()) poster.setImage(new Image(url, true));
        } catch (Exception ignored) {}

        Rectangle clip = new Rectangle(130, 190);
        clip.setArcWidth(6); clip.setArcHeight(6);
        poster.setClip(clip);

        // Remove (×) button overlay
        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white;" +
                "-fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 3 6;" +
                "-fx-background-radius: 0 0 0 4;");
        removeBtn.setOpacity(0);
        StackPane.setAlignment(removeBtn, Pos.TOP_RIGHT);

        thumbBox.setOnMouseEntered(e -> { poster.setOpacity(0.75); removeBtn.setOpacity(1); });
        thumbBox.setOnMouseExited(e  -> { poster.setOpacity(1.0);  removeBtn.setOpacity(0); });

        removeBtn.setOnAction(e -> {
            MediaDAO.removeFromFavorites(user.getId(), media.getIdMedia());
            loadFavorites(user); // refresh grid
        });

        thumbBox.getChildren().addAll(poster, removeBtn);

        Label titleLbl = new Label(media.getTitle());
        titleLbl.setStyle("-fx-text-fill: #ccc; -fx-font-size: 11px;");
        titleLbl.setMaxWidth(130);
        titleLbl.setWrapText(true);

        card.getChildren().addAll(thumbBox, titleLbl);

        card.setOnMouseClicked(e -> {
            TransferData.setMedia(media);
            SceneSwitcher.goTo(e, "/org/Views/MediaDetails.fxml");
        });

        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PASSWORD CHANGE
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleChangePassword(ActionEvent event) {
        User user = Session.getUser();
        if (user == null) return;

        String current = currentPwdField.getText();
        String newPwd  = newPwdField.getText();
        String confirm = confirmPwdField.getText();

        // Validate fields
        if (current.isBlank() || newPwd.isBlank() || confirm.isBlank()) {
            showPwdStatus("Please fill in all fields.", false);
            return;
        }

        // Verify current password against DB hash
        String storedHash = UserDAO.getHashedPass(user.getEmail());
        if (storedHash == null || !BCrypt.checkpw(current, storedHash)) {
            showPwdStatus("Current password is incorrect.", false);
            return;
        }

        // Validate new password strength
        if (newPwd.length() < 8) {
            showPwdStatus("New password must be at least 8 characters.", false);
            return;
        }
        if (!newPwd.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")) {
            showPwdStatus("Password must contain uppercase, lowercase, digit and special char (@#$%^&+=).", false);
            return;
        }
        if (!newPwd.equals(confirm)) {
            showPwdStatus("Passwords do not match.", false);
            return;
        }

        // Hash and update
        String newHash = BCrypt.hashpw(newPwd, BCrypt.gensalt(12));
        boolean updated = updatePasswordInDB(user.getId(), newHash);

        if (updated) {
            showPwdStatus("✓ Password updated successfully.", true);
            currentPwdField.clear();
            newPwdField.clear();
            confirmPwdField.clear();
        } else {
            showPwdStatus("Failed to update password. Please try again.", false);
        }
    }

    private boolean updatePasswordInDB(int userId, String newHash) {
        try (java.sql.Connection conn = org.netflix.Utils.ConxDB.getInstance();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                     "UPDATE user SET password = ? WHERE id_User = ?")) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showPwdStatus(String msg, boolean success) {
        pwdStatusLabel.setText(msg);
        pwdStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                (success ? "#46d369;" : "#e50914;"));
        pwdStatusLabel.setVisible(true);
        pwdStatusLabel.setManaged(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ══════════════════════════════════════════════════════════════════════════

    @FXML private void handleBack(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }

    @FXML private void handleViewHistory(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/WatchHistory.fxml");
    }

    @FXML private void handleViewMyList(ActionEvent event) {
        SceneSwitcher.goTo(event, "/org/Views/main.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        Session.logout();
        SceneSwitcher.goTo(event, "/org/Views/SignIn.fxml");
    }
}