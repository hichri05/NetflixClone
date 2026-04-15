package org.netflix.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.User;

public class UserManagementController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUserEmail;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserStatus;
    @FXML private TableColumn<User, String> colActions;
    @FXML private TextField userSearch;
    @FXML private Label totalUsersLabel;
    @FXML private Label bannedUsersLabel;
    @FXML private Label adminUsersLabel;

    // ── Role constants ────────────────────────────────────────────────────────
    private static final String ROLE_USER   = "user";
    private static final String ROLE_ADMIN  = "ADMIN";
    private static final String ROLE_BANNED = "BANNED";

    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    // ── Init ──────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupColumns();
        loadUsers();
        setupSearch();
    }

    // ── Column setup ──────────────────────────────────────────────────────────
    private void setupColumns() {
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Role pill badge
        colUserRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                User u = getTableRow().getItem();
                String role = u.getRole();
                Label pill = new Label(role == null ? "user" : role.toUpperCase());
                pill.setStyle(getRolePillStyle(role));
                setGraphic(pill);
            }
        });

        // Status dot + label
        colUserStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                User u = getTableRow().getItem();
                boolean banned = ROLE_BANNED.equalsIgnoreCase(u.getRole());

                Circle dot = new Circle(4);
                dot.setFill(Color.web(banned ? "#ff4444" : "#46d369"));

                Label lbl = new Label(banned ? "Banned" : "Active");
                lbl.setStyle("-fx-text-fill: " + (banned ? "#ff4444" : "#46d369") + ";"
                        + "-fx-font-weight: bold; -fx-font-size: 12px;");

                HBox box = new HBox(6, dot, lbl);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        // Action buttons
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button promoteBtn = new Button();
            private final Button banBtn     = new Button();

            {
                promoteBtn.setStyle(
                        "-fx-background-color: #2a2a2a; -fx-text-fill: white; -fx-cursor: hand;" +
                                "-fx-border-color: #555; -fx-border-radius: 4; -fx-background-radius: 4;" +
                                "-fx-padding: 4 12; -fx-font-size: 11px;");
                banBtn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #ff4444;" +
                                "-fx-border-color: #ff4444; -fx-border-radius: 4; -fx-background-radius: 4;" +
                                "-fx-cursor: hand; -fx-padding: 4 12; -fx-font-size: 11px;");

                promoteBtn.setOnAction(e -> {
                    User u = getTableRow().getItem();
                    if (u != null) handlePromote(u);
                });
                banBtn.setOnAction(e -> {
                    User u = getTableRow().getItem();
                    if (u != null) handleBan(u);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                User u = getTableRow().getItem();
                boolean isBanned = ROLE_BANNED.equalsIgnoreCase(u.getRole());
                boolean isAdmin  = ROLE_ADMIN.equalsIgnoreCase(u.getRole());

                promoteBtn.setText(isAdmin ? "Demote" : "Promote");
                banBtn.setText(isBanned ? "Unban" : "Ban");

                if (isBanned) {
                    banBtn.setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: #46d369;" +
                                    "-fx-border-color: #46d369; -fx-border-radius: 4; -fx-background-radius: 4;" +
                                    "-fx-cursor: hand; -fx-padding: 4 12; -fx-font-size: 11px;");
                } else {
                    banBtn.setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: #ff4444;" +
                                    "-fx-border-color: #ff4444; -fx-border-radius: 4; -fx-background-radius: 4;" +
                                    "-fx-cursor: hand; -fx-padding: 4 12; -fx-font-size: 11px;");
                }

                HBox box = new HBox(8, promoteBtn, banBtn);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });
    }

    // ── Data ──────────────────────────────────────────────────────────────────
    private void loadUsers() {
        allUsers.setAll(UserDAO.getAllUsers());
        userTable.setItems(allUsers);
        updateStats();
    }

    private void updateStats() {
        if (totalUsersLabel != null)
            totalUsersLabel.setText(String.valueOf(allUsers.size()));
        if (bannedUsersLabel != null)
            bannedUsersLabel.setText(String.valueOf(
                    allUsers.stream().filter(u -> ROLE_BANNED.equalsIgnoreCase(u.getRole())).count()));
        if (adminUsersLabel != null)
            adminUsersLabel.setText(String.valueOf(
                    allUsers.stream().filter(u -> ROLE_ADMIN.equalsIgnoreCase(u.getRole())).count()));
    }

    private void setupSearch() {
        userSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                userTable.setItems(allUsers);
            } else {
                String lower = newVal.toLowerCase();
                userTable.setItems(allUsers.filtered(u ->
                        (u.getEmail()    != null && u.getEmail().toLowerCase().contains(lower)) ||
                                (u.getUsername() != null && u.getUsername().toLowerCase().contains(lower))
                ));
            }
        });
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void handlePromote(User user) {
        // Toggle: ADMIN → user, anything else → ADMIN
        String newRole = ROLE_ADMIN.equalsIgnoreCase(user.getRole()) ? ROLE_USER : ROLE_ADMIN;
        String label   = ROLE_ADMIN.equalsIgnoreCase(user.getRole()) ? "demote to User" : "promote to Admin";

        showConfirm("Confirm", "Are you sure you want to " + label + " " + user.getUsername() + "?",
                () -> {
                    boolean ok = UserDAO.updateRole(user.getId(), newRole);
                    if (ok) {
                        showInfo("Role Updated", user.getUsername() + " is now " + newRole + ".");
                        loadUsers();
                    } else {
                        showError("Failed to update role. Check database connection.");
                    }
                });
    }

    private void handleBan(User user) {
        boolean isBanned = ROLE_BANNED.equalsIgnoreCase(user.getRole());

        if (isBanned) {
            showConfirm("Unban User", "Restore " + user.getUsername() + " to a regular user?",
                    () -> {
                        // FIX: use constant, consistent casing
                        boolean ok = UserDAO.updateRole(user.getId(), ROLE_USER);
                        if (ok) {
                            showInfo("User Unbanned", user.getUsername() + " has been unbanned.");
                            loadUsers();
                        } else {
                            showError("Failed to unban user. Check database connection.");
                        }
                    });
        } else {
            showConfirm("Ban User",
                    "Ban " + user.getUsername() + "? They will lose access immediately.",
                    () -> {
                        // FIX: "BANNED" is now 6 chars — make sure your DB column is VARCHAR(20)
                        boolean ok = UserDAO.updateRole(user.getId(), ROLE_BANNED);
                        if (ok) {
                            showInfo("User Banned", user.getUsername() + " has been banned.");
                            loadUsers();
                        } else {
                            showError("Failed to ban user. Check database connection.\n" +
                                    "Make sure your 'role' column is VARCHAR(20) or larger.");
                        }
                    });
        }
    }

    @FXML
    public void handleBanSelected(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a user first."); return; }
        handleBan(selected);
    }

    @FXML
    public void handlePromoteSelected(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a user first."); return; }
        handlePromote(selected);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String getRolePillStyle(String role) {
        String base = "-fx-padding: 2 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
        if (ROLE_ADMIN.equalsIgnoreCase(role))
            return base + "-fx-background-color: #e50914; -fx-text-fill: white;";
        if (ROLE_BANNED.equalsIgnoreCase(role))
            return base + "-fx-background-color: #333; -fx-text-fill: #ff4444;";
        return base + "-fx-background-color: #2a2a2a; -fx-text-fill: #aaa;";
    }

    private void showConfirm(String title, String msg, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(btn -> { if (btn == ButtonType.OK) onConfirm.run(); });
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.setHeaderText(null);
        alert.show();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setHeaderText(title);
        alert.show();
    }
}
