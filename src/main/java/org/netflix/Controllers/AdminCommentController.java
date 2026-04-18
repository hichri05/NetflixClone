package org.netflix.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.netflix.DAO.CommentDAO;

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

    private boolean showingReportedOnly;
    private List<CommentDAO.CommentDTO> allComments;
    private List<CommentDAO.CommentDTO> reportedComments;

    @FXML
    public void initialize() {
        loadData();
        renderRows(allComments);
    }

    private void loadData() {
        allComments = CommentDAO.getAllComments();
        reportedComments = CommentDAO.getReportedCommentsDTO();

        totalCountLabel.setText(String.valueOf(allComments.size()));
        reportedCountLabel.setText(String.valueOf(reportedComments.size()));
    }

    private void renderRows(List<CommentDAO.CommentDTO> source) {
        commentsRows.getChildren().clear();

        String query = searchField.getText().trim().toLowerCase();

        List<CommentDAO.CommentDTO> filtered = source.stream()
                .filter(dto -> query.isEmpty()
                        || dto.username.toLowerCase().contains(query)
                        || dto.mediaTitle.toLowerCase().contains(query)
                        || dto.comment.getContent().toLowerCase().contains(query))
                .collect(Collectors.toList());

        boolean isEmpty = filtered.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);

        for (int i = 0; i < filtered.size(); i++) {
            commentsRows.getChildren().add(buildRow(filtered.get(i), i));
        }
    }

    private HBox buildRow(CommentDAO.CommentDTO dto, int index) {
        boolean reported = dto.comment.getIs_reported() == 1;

        String baseColor = reported
                ? "#1c1010"
                : (index % 2 == 0 ? "#181818" : "#1a1a1a");

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(buildRowStyle(baseColor, reported));

        row.setOnMouseEntered(e ->
                row.setStyle(buildRowStyle(reported ? "#2a1010" : "#222222", reported))
        );

        row.setOnMouseExited(e ->
                row.setStyle(buildRowStyle(baseColor, reported))
        );

        VBox userCol = buildUserColumn(dto);
        VBox mediaCol = buildMediaColumn(dto);

        Label content = new Label(dto.comment.getContent());
        content.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
        content.setWrapText(true);
        HBox.setHgrow(content, Priority.ALWAYS);

        Label date = new Label(dto.comment.getCreated_at() != null
                ? dto.comment.getCreated_at().toString()
                : "—");
        date.setMinWidth(110);
        date.setStyle("-fx-text-fill: #555; -fx-font-size: 12px;");

        Label status = buildStatusBadge(reported);
        HBox actions = buildActions(dto, row, reported);

        row.getChildren().addAll(userCol, mediaCol, content, date, status, actions);
        return row;
    }

    private VBox buildUserColumn(CommentDAO.CommentDTO dto) {
        VBox col = new VBox(2);
        col.setMinWidth(150);

        String initial = dto.username.isEmpty()
                ? "?"
                : String.valueOf(Character.toUpperCase(dto.username.charAt(0)));

        Label avatar = new Label(initial);
        avatar.setStyle("-fx-background-color: #e50914; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 13px;" +
                "-fx-min-width: 32; -fx-min-height: 32;" +
                "-fx-max-width: 32; -fx-max-height: 32;" +
                "-fx-background-radius: 16; -fx-alignment: center;");

        Label name = new Label(dto.username);
        name.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label id = new Label("ID #" + dto.comment.getId_User());
        id.setStyle("-fx-text-fill: #555; -fx-font-size: 10px;");

        VBox info = new VBox(1, name, id);
        HBox wrapper = new HBox(8, avatar, info);
        wrapper.setAlignment(Pos.CENTER_LEFT);

        col.getChildren().add(wrapper);
        return col;
    }

    private VBox buildMediaColumn(CommentDAO.CommentDTO dto) {
        VBox col = new VBox(2);
        col.setMinWidth(180);

        Label title = new Label(dto.mediaTitle);
        title.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
        title.setMaxWidth(165);
        title.setEllipsisString("...");

        Label id = new Label("Media #" + dto.comment.getId_Media());
        id.setStyle("-fx-text-fill: #444; -fx-font-size: 10px;");

        col.getChildren().addAll(title, id);
        return col;
    }

    private Label buildStatusBadge(boolean reported) {
        Label label = new Label(reported ? "🚨 Reported" : "✓ Clean");

        label.setStyle(reported
                ? "-fx-background-color: #3a0a0a; -fx-text-fill: #e50914;"
                : "-fx-background-color: #0a2a0a; -fx-text-fill: #2ecc71;");

        label.setStyle(label.getStyle() +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 3 8; -fx-background-radius: 4;");

        label.setMinWidth(100);
        return label;
    }

    private HBox buildActions(CommentDAO.CommentDTO dto, HBox row, boolean reported) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(100);

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #e50914; -fx-text-fill: white;" +
                "-fx-font-size: 11px; -fx-font-weight: bold;" +
                "-fx-padding: 5 12; -fx-background-radius: 4; -fx-cursor: hand;");

        deleteBtn.setOnMouseEntered(e ->
                deleteBtn.setStyle(deleteBtn.getStyle().replace("#e50914", "#b20710"))
        );
        deleteBtn.setOnMouseExited(e ->
                deleteBtn.setStyle(deleteBtn.getStyle().replace("#b20710", "#e50914"))
        );

        deleteBtn.setOnAction(e ->
                handleDelete(dto.comment.getId_Comment(), row)
        );

        box.getChildren().add(deleteBtn);

        if (reported) {
            Button dismiss = new Button("Dismiss");
            dismiss.setStyle("-fx-background-color: #2a2a2a; -fx-text-fill: #aaa;" +
                    "-fx-font-size: 11px; -fx-padding: 5 10;" +
                    "-fx-background-radius: 4; -fx-border-color: #444;");

            dismiss.setOnAction(e ->
                    handleDismissReport(dto.comment.getId_Comment())
            );

            box.getChildren().add(dismiss);
        }

        return box;
    }

    private String buildRowStyle(String color, boolean reported) {
        String style = "-fx-background-color: " + color + ";" +
                "-fx-padding: 14 16;" +
                "-fx-border-color: #222; -fx-border-width: 0 0 1 0;";

        if (reported) {
            style += "-fx-border-color: #e50914 #222 #222 #e50914;" +
                    "-fx-border-width: 0 0 1 3;";
        }

        return style;
    }

    private void handleDelete(int id, HBox row) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Comment");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This comment will be permanently deleted.");
        confirm.getDialogPane().setStyle("-fx-background-color: #1a1a1a;");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK && CommentDAO.deleteComment(id)) {
                commentsRows.getChildren().remove(row);
                loadData();
                flashStatus("Comment deleted.");
            } else {
                flashStatus("Failed to delete.");
            }
        });
    }

    private void handleDismissReport(int id) {
        if (CommentDAO.dismissReport(id)) {
            loadData();
            renderRows(showingReportedOnly ? reportedComments : allComments);
            flashStatus("Report dismissed.");
        } else {
            flashStatus("Failed to dismiss report.");
        }
    }

    @FXML
    private void handleTabAll(ActionEvent e) {
        showingReportedOnly = false;
        tabAllBtn.setStyle(tabStyle(true, "0"));
        tabReportedBtn.setStyle(tabStyle(false, "24"));
        renderRows(allComments);
    }

    @FXML
    private void handleTabReported(ActionEvent e) {
        showingReportedOnly = true;
        tabReportedBtn.setStyle(tabStyle(true, "24"));
        tabAllBtn.setStyle(tabStyle(false, "0"));
        renderRows(reportedComments);
    }

    @FXML
    private void handleSearch() {
        renderRows(showingReportedOnly ? reportedComments : allComments);
    }

    @FXML
    private void handleRefresh(ActionEvent e) {
        searchField.clear();
        loadData();
        renderRows(showingReportedOnly ? reportedComments : allComments);
        flashStatus("Refreshed.");
    }

    private void flashStatus(String msg) {
        statusLabel.setText(msg);

        new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException ignored) {}

            javafx.application.Platform.runLater(() ->
                    statusLabel.setText("")
            );
        }).start();
    }

    private String tabStyle(boolean active, String leftPad) {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: " + (active ? "white" : "#808080") + ";" +
                "-fx-font-size: 14px; -fx-font-weight: bold;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 12 24 12 " + leftPad + ";" +
                "-fx-border-color: " + (active ? "#e50914" : "transparent") + ";" +
                "-fx-border-width: 0 0 3 0;";
    }
}