package com.softserve.itw2013.javafx.terminal.admin;

import com.softserve.itw2013.javafx.terminal.TerminalDispatcher;
import com.softserve.itw2013.javafx.terminal.controller.JFXController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 17.11.13
 * Time: 12:13
 * To change this template use File | Settings | File Templates.
 */
public class TerminalAdminApp extends Application {
    private AbstractApplicationContext applicationContext;

    @Override
    public void init() throws Exception {
        applicationContext = new ClassPathXmlApplicationContext("/application-context.xml");

        TerminalAdminContext.setTerminalDispatcher(applicationContext.getBean(TerminalDispatcher.class));
    }

    @Override
    public void start(final Stage stage) {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/main.fxml"));

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        final JFXController controller = fxmlLoader.getController();

        controller.assignStage(stage);

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        TerminalAdminContext.setTerminalDispatcher(null);

        applicationContext.close();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
