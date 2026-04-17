package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.netflix.DAO.CommentDAO;
import org.netflix.Models.Comment;
import org.netflix.Utils.Session;

import java.util.List;
import java.util.stream.Collectors;

public class AdminCommentController {


    @FXML private Label adminNameLabel;
    @FXML private Label reportedCountLabel;
    @FXML private Label totalCountLabel;
    @FXML private Label statusLabel;

    @FXML private Button tabAllBtn;
    @FXML private Button tabReportedBtn;

    @FXML private TextField searchField;
    @FXML private VBox commentsRows;
    @FXML private VBox emptyState;


    private boolean showingReportedOnly = false;
    private List<CommentDAO.CommentDTO> allComments;
    private List<CommentDAO.CommentDTO> reportedComments;


    @FXML
    public void initialize() {
        if (Session.getUser() != null)
            adminNameLabel.setText(Session.getUser().getUsername());
        loadData();
        renderRows(allComments);
    }


    private void loadData() {
        allComments      = CommentDAO.getAllComments();
        reportedComments = CommentDAO.getReportedCommentsDTO();

        totalCountLabel.setText(String.valueOf(allComments.size()));
        reportedCountLabel.setText(String.valueOf(reportedComments.size()));
    }


    private void renderRows(List<CommentDAO.CommentDTO> list) {
        commentsRows.getChildren().clear();

        String query = searchField.getText().trim().toLowerCase();
        List<CommentDAO.CommentDTO> filtered = list.stream()
                .filter(dto -> query.isEmpty()
                        || dto.username.toLowerCase().contains(query)
                        || dto.mediaTitle.toLowerCase().contains(query)
                        || dto.comment.getContent().toLowerCase().contains(query))
                .collect(Collectors.toList());

        emptyState.setVisible(filtered.isEmpty());
        emptyState.setManaged(filtered.isEmpty());

        for (int i = 0; i < filtered.size(); i++) {
            CommentDAO.CommentDTO dto = filtered.get(i);
            HBox row = buildRow(dto, i);
            commentsRows.getChildren().add(row);
        }
    }


    private HBox buildRow(CommentDAO.CommentDTO dto, int index) {
        boolean isReported = dto.comment.getIs_reported() == 1;
        String rowBg = isReported ? "#1c1010" : (index % 2 == 0 ? "#181818" : "#1a1a1a");

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(0);
        row.setStyle("-fx-background-color: " + rowBg + ";" +
                "-fx-padding: 14 16 14 16;" +
                "-fx-border-color: #222; -fx-border-width: 0 0 1 0;");

        if (isReported) {
            row.setStyle(row.getStyle() + "-fx-border-color: #e50914 #222 #222 #e50914;" +
                    "-fx-border-width: 0 0 1 3;");
        }

        row.setOnMouseEntered(e -> row.setStyle(row.getStyle()
                .replace(rowBg, isReported ? "#2a1010" : "#222222")));
        row.setOnMouseExited(e -> row.setStyle(row.getStyle()
                .replace(isReported ? "#2a1010" : "#222222", rowBg)));

        VBox userCol = new VBox(2);
        userCol.setMinWidth(150);
        userCol.setMaxWidth(150);

        String initial = dto.username.isEmpty() ? "?" :
                String.valueOf(dto.username.charAt(0)).toUpperCase();
        Label avatar = new Label(initial);
        avatar.setStyle("-fx-background-color: #e50914; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 13px;" +
                "-fx-min-width: 32; -fx-min-height: 32;" +
                "-fx-max-width: 32; -fx-max-height: 32;" +
                "-fx-background-radius: 16; -fx-alignment: center;");

        Label userName = new Label(dto.username);
        userName.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label userId = new Label("ID #" + dto.comment.getId_User());
        userId.setStyle("-fx-text-fill: #555; -fx-font-size: 10px;");

        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER_LEFT);
        VBox userInfo = new VBox(1, userName, userId);
        userBox.getChildren().addAll(avatar, userInfo);
        userCol.getChildren().add(userBox);

        VBox mediaCol = new VBox(2);
        mediaCol.setMinWidth(180);
        mediaCol.setMaxWidth(180);

        Label mediaTitle = new Label(dto.mediaTitle);
        mediaTitle.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
        mediaTitle.setMaxWidth(165);
        mediaTitle.setWrapText(false);
        mediaTitle.setEllipsisString("...");

        Label mediaId = new Label("Media #" + dto.comment.getId_Media());
        mediaId.setStyle("-fx-text-fill: #444; -fx-font-size: 10px;");

        mediaCol.getChildren().addAll(mediaTitle, mediaId);

        Label content = new Label(dto.comment.getContent());
        content.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        content.setWrapText(true);
        content.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label date = new Label(dto.comment.getCreated_at() != null
                ? dto.comment.getCreated_at().toString() : "—");
        date.setMinWidth(110);
        date.setMaxWidth(110);
        date.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");

        Label statusBadge;
        if (isReported) {
            statusBadge = new Label("🚨 Reported");
            statusBadge.setStyle("-fx-background-color: #3a0a0a; -fx-text-fill: #e50914;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-padding: 3 8 3 8; -fx-background-radius: 4;");
        } else {
            statusBadge = new Label("✓ Clean");
            statusBadge.setStyle("-fx-background-color: #0a2a0a; -fx-text-fill: #2ecc71;" +
                    "-fx-font-size: 11px; -fx-font-weight: bold;" +
                    "-fx-padding: 3 8 3 8; -fx-background-radius: 4;");
        }
        statusBadge.setMinWidth(100);
        statusBadge.setMaxWidth(100);


        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setMinWidth(100);

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 5 12 5 12; -fx-background-radius: 4; -fx-cursor: hand;" +
                "-fx-border-color: transparent;");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(deleteBtn.getStyle()
                .replace("#e50914", "#b20710")));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(deleteBtn.getStyle()
                .replace("#b20710", "#e50914")));
        deleteBtn.setOnAction(e -> handleDelete(dto.comment.getId_Comment(), row));

        if (isReported) {
            Button dismissBtn = new Button("Dismiss");
            dismissBtn.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #aaa;" +
                    "-fx-font-size: 11px; -fx-padding: 5 10 5 10;" +
                    "-fx-background-radius: 4; -fx-cursor: hand;" +
                    "-fx-border-color: #444;");
            dismissBtn.setOnAction(e -> handleDismissReport(dto.comment.getId_Comment(), row));
            actions.getChildren().addAll(deleteBtn, dismissBtn);
        } else {
            actions.getChildren().add(deleteBtn);
        }

        row.getChildren().addAll(userCol, mediaCol, content, date, statusBadge, actions);
        return row;
    }

    private void handleDelete(int commentId, HBox row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Comment");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This comment will be permanently deleted.");
        confirm.getDialogPane().setStyle("-fx-background-color: #1a1a1a;");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (CommentDAO.deleteComment(commentId)) {
                    commentsRows.getChildren().remove(row);
                    loadData();
                    flashStatus("Comment deleted.");
                } else {
                    flashStatus("Failed to delete.");
                }
            }
        });
    }

    private void handleDismissReport(int commentId, HBox row) {
        if (CommentDAO.dismissReport(commentId)) {
            loadData();
            List<CommentDAO.CommentDTO> current = showingReportedOnly ? reportedComments : allComments;
            renderRows(current);
            flashStatus("Report dismissed.");
        } else {
            flashStatus("Failed to dismiss report.");
        }
    }

    @FXML
    private void handleTabAll(ActionEvent event) {
        showingReportedOnly = false;
        tabAllBtn.setStyle(activeTabStyle("0"));
        tabReportedBtn.setStyle(inactiveTabStyle("24"));
        renderRows(allComments);
    }

    @FXML
    private void handleTabReported(ActionEvent event) {
        showingReportedOnly = true;
        tabReportedBtn.setStyle(activeTabStyle("24"));
        tabAllBtn.setStyle(inactiveTabStyle("0"));
        renderRows(reportedComments);
    }

    @FXML
    private void handleSearch() {
        renderRows(showingReportedOnly ? reportedComments : allComments);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        searchField.clear();
        loadData();
        renderRows(showingReportedOnly ? reportedComments : allComments);
        flashStatus("Refreshed.");
    }

    private void flashStatus(String msg) {
        statusLabel.setText(msg);
        new Thread(() -> {
            try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> statusLabel.setText(""));
        }).start();
    }

    private String activeTabStyle(String leftPad) {
        return "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;" +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 24 12 " + leftPad + ";" +
                "-fx-border-color: #e50914; -fx-border-width: 0 0 3 0;";
    }

    private String inactiveTabStyle(String leftPad) {
        return "-fx-background-color: transparent; -fx-text-fill: #808080; -fx-font-size: 14px;" +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 12 24 12 " + leftPad + ";" +
                "-fx-border-color: transparent; -fx-border-width: 0 0 3 0;";
    }
}
