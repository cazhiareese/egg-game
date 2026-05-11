module com.eggame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;
    requires java.desktop;

    opens com.eggame to javafx.fxml;
    opens com.eggame.scene to javafx.fxml;
    opens com.eggame.map to javafx.fxml;
    opens com.eggame.entities to javafx.fxml;
    opens com.eggame.rules to javafx.fxml;

    exports com.eggame;
    exports com.eggame.scene;
    exports com.eggame.map;
    exports com.eggame.entities;
    exports com.eggame.rules;

    opens com.eggame.network to javafx.fxml;

    exports com.eggame.network;
}