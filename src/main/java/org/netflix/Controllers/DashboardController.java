package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.Genre;
import org.netflix.Models.Media;
import org.netflix.Models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML private Label movieCountLabel;
    @FXML private Label seriesCountLabel;
    @FXML private Label userCountLabel;
    @FXML private BarChart<Number, String> categoryChart;

    @FXML
    public void initialize() {
        formatChartAxis();
        refreshDashboard();
    }

    public void refreshDashboard() {
        List<Media> allMedia = MediaDAO.getAllMediaWithViews();
        List<User> allUsers = UserDAO.getAllUsers();

        long movieCount = allMedia.stream()
                .filter(m -> "Movie".equalsIgnoreCase(m.getType()))
                .count();
        long serieCount = allMedia.stream()
                .filter(m -> "Serie".equalsIgnoreCase(m.getType()))
                .count();

        movieCountLabel.setText(String.valueOf(movieCount));
        seriesCountLabel.setText(String.valueOf(serieCount));
        userCountLabel.setText(String.valueOf(allUsers != null ? allUsers.size() : 0));

        populateCategoryChart(allMedia);
    }

    private void formatChartAxis() {
        NumberAxis xAxis = (NumberAxis) categoryChart.getXAxis();
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                long value = object.longValue();
                if (value >= 1_000_000) return (value / 1_000_000) + "M";
                if (value >= 1_000) return (value / 1_000) + "K";
                return String.valueOf(value);
            }
            @Override
            public Number fromString(String string) { return 0; }
        });
    }

    private void populateCategoryChart(List<Media> allMedia) {
        categoryChart.getData().clear();
        Map<String, Long> genreViewMap = new HashMap<>();

        for (Media media : allMedia) {
            int views = media.getViews();
            List<Genre> genres = media.getGenres();

            if (genres != null) {
                for (Genre g : genres) {
                    if (g != null && g.getName() != null) {
                        String genreName = g.toString(); // handles Science_Fiction → "Science Fiction"
                        genreViewMap.put(genreName,
                                genreViewMap.getOrDefault(genreName, 0L) + views);
                    }
                }
            }
        }

        XYChart.Series<Number, String> seriesData = new XYChart.Series<>();
        seriesData.setName("Total Views");

        genreViewMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .forEach(entry -> seriesData.getData().add(
                        new XYChart.Data<>(entry.getValue(), entry.getKey())));

        categoryChart.getData().add(seriesData);
    }
}