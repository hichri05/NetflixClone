module org.netflix {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.media;
    requires mysql.connector.j;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome6;

    opens org.Views to javafx.fxml;
    exports org.netflix.Main;
    opens org.netflix.Main to javafx.fxml;
    exports org.netflix.Controllers;
    opens org.netflix.Controllers to javafx.fxml;
}
