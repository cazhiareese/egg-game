module com.eggame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;

    opens com.eggame to javafx.fxml;
    opens com.eggame.scene to javafx.fxml;
    opens com.eggame.map to javafx.fxml;

    exports com.eggame;
    exports com.eggame.scene;
    exports com.eggame.map;
}