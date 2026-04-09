package org.netflix.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.User;

public class UserManagementController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUserEmail;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserStatus;
    @FXML private TableColumn<User, String> colActions;
    @FXML private TextField userSearch;

    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));


        colUserStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                User u = getTableRow().getItem();
                boolean banned = "BANNED".equalsIgnoreCase(u.getRole());
                setText(banned ? "Banned" : "Active");
                setStyle(banned
                        ? "-fx-text-fill: #ff4444; -fx-font-weight: bold;"
                        : "-fx-text-fill: #46d369; -fx-font-weight: bold;");
            }
        });


        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button promoteBtn = new Button("Promote");
            private final Button banBtn = new Button("Ban");
            {
                promoteBtn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-cursor: hand;");
                banBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-border-color: #ff4444; -fx-cursor: hand;");

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
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, promoteBtn, banBtn);
                    setGraphic(box);
                }
            }
        });

        loadUsers();


        userSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                userTable.setItems(allUsers);
            } else {
                String lower = newVal.toLowerCase();
                ObservableList<User> filtered = allUsers.filtered(u ->
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(lower)) ||
                                (u.getUsername() != null && u.getUsername().toLowerCase().contains(lower))
                );
                userTable.setItems(filtered);
            }
        });
    }

    private void loadUsers() {
        allUsers.setAll(UserDAO.getAllUsers());
        userTable.setItems(allUsers);
    }

    private void handlePromote(User user) {
        String newRole = "ADMIN".equalsIgnoreCase(user.getRole()) ? "user" : "ADMIN";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Change role of " + user.getUsername() + " to " + newRole + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean ok = UserDAO.updateRole(user.getId(), newRole);
                if (ok) {
                    showInfo("Role updated", user.getUsername() + " is now " + newRole + ".");
                    loadUsers();
                } else {
                    showError("Failed to update role.");
                }
            }
        });
    }

    private void handleBan(User user) {
        boolean isBanned = "BANNED".equalsIgnoreCase(user.getRole());
        if (isBanned) {

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Unban " + user.getUsername() + "?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    UserDAO.updateRole(user.getId(), "user");
                    loadUsers();
                }
            });
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Ban " + user.getUsername() + "? This will set their role to BANNED.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean ok = UserDAO.updateRole(user.getId(), "BANNED");
                    if (ok) {
                        showInfo("User banned", user.getUsername() + " has been banned.");
                        loadUsers();
                    } else {
                        showError("Failed to ban user.");
                    }
                }
            });
        }
    }


    @FXML
    public void handleBanSelected(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a user first.");
            return;
        }
        handleBan(selected);
    }

    @FXML
    public void handlePromoteSelected(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a user first.");
            return;
        }
        handlePromote(selected);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.setHeaderText(title);
        alert.show();
    }
}