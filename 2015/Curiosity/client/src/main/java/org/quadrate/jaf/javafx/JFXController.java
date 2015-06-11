package org.quadrate.jaf.javafx;

import javafx.stage.Stage;

public interface JFXController {
    void assignStage(Stage stage);

    void open(Stage owner, boolean modal);

    Integer getReturnCode();
}
