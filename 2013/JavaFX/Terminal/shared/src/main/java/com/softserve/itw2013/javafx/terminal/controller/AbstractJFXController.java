package com.softserve.itw2013.javafx.terminal.controller;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.Validate;

/**
 * Created with IntelliJ IDEA.
 * User: V.Stefanyuk
 * Date: 16.11.13
 * Time: 23:13
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractJFXController implements JFXController {
    public static final Integer OK_RC = 0;
    public static final Integer CANCEL_RC = 1;

    private final boolean fullScreen;

    @FXML
    protected Parent root;

    private Stage stage;

    private Integer returnCode;

    protected AbstractJFXController(final boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public final boolean isFullScreen() {
        return fullScreen;
    }

    protected final Stage getStage() {
        return stage;
    }

    @Override
    public void assignStage(final Stage stage) {
        Validate.notNull(stage);

        if (this.stage == null) {
            this.stage = stage;

            configureStage(stage);
        } else {
            if (this.stage != stage) {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public void open(final Stage owner, final boolean modal) {
        Validate.notNull(owner);

        if (stage == null) {
            stage = new Stage();

            stage.initOwner(owner);
            if (modal) {
                stage.initModality(Modality.WINDOW_MODAL);
            }

            configureStage(stage);
        } else {
            if (stage.getOwner() != owner) {
                throw new IllegalStateException();
            }
        }

        returnCode = null;

        if (modal) {
            stage.showAndWait();
        } else {
            stage.show();
        }
    }

    public final Integer getReturnCode() {
        return returnCode;
    }

    protected final void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    protected final void open(final JFXController controller, final boolean modal) {
        Validate.notNull(controller);

        controller.open(getStage(), modal);
    }

    protected final void hide() {
        stage.hide();
    }

    protected void configureStage(final Stage stage) {
        stage.setScene(new Scene(root));

        if (fullScreen) {
            stage.initStyle(StageStyle.UNDECORATED);

            final Rectangle2D screenVisualBounds = Screen.getPrimary().getBounds();

            stage.setX(screenVisualBounds.getMinX());
            stage.setY(screenVisualBounds.getMinY());
            stage.setWidth(screenVisualBounds.getWidth());
            stage.setHeight(screenVisualBounds.getHeight());
        }
    }
}
