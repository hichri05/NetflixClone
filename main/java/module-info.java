module org.netflix {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.netflix to javafx.fxml;
    exports org.netflix;
}
