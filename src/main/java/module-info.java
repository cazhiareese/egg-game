module com.eggame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;

    opens com.eggame to javafx.fxml;

    exports com.eggame;
}