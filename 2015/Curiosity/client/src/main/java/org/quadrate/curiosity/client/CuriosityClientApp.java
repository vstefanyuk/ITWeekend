package org.quadrate.curiosity.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.quadrate.curiosity.client.controller.ControlPanelController;
import java.io.IOException;

public class CuriosityClientApp extends Application {
	@Override
	public void start(final Stage stage) throws Exception {
		stage.setTitle("Curiosity");
        stage.setResizable(false);

        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/ControlPanel.fxml"));

        try {
            fxmlLoader.load();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }

        final ControlPanelController controlPanelController = fxmlLoader.getController();

        stage.setScene(new Scene(controlPanelController.getRoot()));

        stage.show();
	}

	public static void main(final String[] args) {
		launch(args);
	}
}
