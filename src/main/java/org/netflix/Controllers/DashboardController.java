package org.netflix.Controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import org.netflix.DAO.MediaDAO;
import org.netflix.DAO.UserDAO;
import org.netflix.Models.Genre;
import org.netflix.Models.Media;
import org.netflix.Models.User;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label movieCountLabel;
    @FXML private Label seriesCountLabel;
    @FXML private Label userCountLabel;


    @FXML private BarChart<Number, String> categoryChart;

    @FXML private PieChart genrePieChart;

    @FXML private BarChart<Number, String> topFilmsChart;

    @FXML private LineChart<String, Number> registrationsChart;


    @FXML private ComboBox<String> dateFilter;



    @FXML
    public void initialize() {
        formatCategoryAxis();
        formatTopFilmsAxis();
        refreshDashboard();
    }

    public void refreshDashboard() {
        List<Media> allMedia = MediaDAO.getAllMediaWithViews();
        List<User>  allUsers = UserDAO.getAllUsers();

        long movieCount  = allMedia.stream().filter(m -> "Movie".equalsIgnoreCase(m.getType())).count();
        long seriesCount = allMedia.stream().filter(m -> "Serie".equalsIgnoreCase(m.getType())).count();

        movieCountLabel .setText(String.valueOf(movieCount));
        seriesCountLabel.setText(String.valueOf(seriesCount));
        userCountLabel  .setText(String.valueOf(allUsers != null ? allUsers.size() : 0));

        populateCategoryChart(allMedia);
        populateGenrePieChart(allMedia);
        populateTopFilmsChart(allMedia);
        populateRegistrationsChart();
    }

    private void formatCategoryAxis() {
        NumberAxis xAxis = (NumberAxis) categoryChart.getXAxis();
        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override public String toString(Number n) {
                long v = n.longValue();
                if (v >= 1_000_000) return (v / 1_000_000) + "M";
                if (v >= 1_000)     return (v / 1_000)     + "K";
                return String.valueOf(v);
            }
            @Override public Number fromString(String s) { return 0; }
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
                        String name = g.toString();
                        genreViewMap.merge(name, (long) views, Long::sum);
                    }
                }
            }
        }

        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Total Views");

        genreViewMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getValue(), e.getKey())));

        categoryChart.getData().add(series);
    }

    private void populateGenrePieChart(List<Media> allMedia) {
        genrePieChart.getData().clear();
        Map<String, Long> genreCountMap = new HashMap<>();

        for (Media media : allMedia) {
            List<Genre> genres = media.getGenres();
            if (genres != null) {
                for (Genre g : genres) {
                    if (g != null && g.getName() != null) {
                        genreCountMap.merge(g.toString(), 1L, Long::sum);
                    }
                }
            }
        }

        genreCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> genrePieChart.getData().add(
                        new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue())));
    }

    private void formatTopFilmsAxis() {
        NumberAxis xAxis = (NumberAxis) topFilmsChart.getXAxis();
        xAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override public String toString(Number n) {
                long v = n.longValue();
                if (v >= 1_000_000) return (v / 1_000_000) + "M";
                if (v >= 1_000)     return (v / 1_000)     + "K";
                return String.valueOf(v);
            }
            @Override public Number fromString(String s) { return 0; }
        });
    }

    private void populateTopFilmsChart(List<Media> allMedia) {
        topFilmsChart.getData().clear();
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        series.setName("Views");

        allMedia.stream()
                .filter(m -> m.getViews() > 0)
                .sorted(Comparator.comparingInt(Media::getViews).reversed())
                .limit(5)
                .forEach(m -> {
                    // Truncate long titles for readability
                    String title = m.getTitle().length() > 20
                            ? m.getTitle().substring(0, 18) + "…"
                            : m.getTitle();
                    series.getData().add(new XYChart.Data<>(m.getViews(), title));
                });

        topFilmsChart.getData().add(series);
    }

    private void populateRegistrationsChart() {
        registrationsChart.getData().clear();

        Map<String, Long> regsByDay = UserDAO.getUsersGroupedByDate();
        if (regsByDay == null || regsByDay.isEmpty()) return;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Registrations");

        regsByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue())));

        registrationsChart.getData().add(series);
    }
}
