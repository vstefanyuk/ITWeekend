package org.quadrate.jaf.javafx;

import com.google.common.base.Preconditions;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;

import java.util.function.Consumer;

public abstract class AbstractJFXController implements JFXController {
    public static final Integer OK_RC = 0;
    public static final Integer CANCEL_RC = 1;

    private final boolean fullScreen;

    @FXML
    protected Parent root;

    private Stage stage;

    private Integer returnCode;

    protected AbstractJFXController() {
        this(false);
    }

    protected AbstractJFXController(final boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    @FXML
    protected void initialize() {
        final Consumer<Scene> onScene = (scene) -> {
            final Stage stage = (Stage)scene.getWindow();

            if (stage == null) {
                final ReadOnlyObjectProperty<Window> windowProperty = scene.windowProperty();

                windowProperty.addListener(new ChangeListener<Window>() {
                    @Override
                    public void changed(ObservableValue<? extends Window> observable, Window oldValue, Window newValue) {
                        windowProperty.removeListener(this);

                        postInitialize((Stage)newValue);
                    }
                });
            } else {
                postInitialize(stage);
            }
        };

        final Scene scene = root.getScene();

        if (scene == null) {
            final ReadOnlyObjectProperty<Scene> sceneProperty = root.sceneProperty();

            sceneProperty.addListener(new ChangeListener<Scene>() {
                @Override
                public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
                    sceneProperty.removeListener(this);

                    onScene.accept(newValue);
                }
            });
        } else {
            onScene.accept(scene);
        }
    }

    protected void postInitialize(final Stage stage) {
        if (this.stage != null) {
            if (this.stage != stage) {
                throw new IllegalStateException();
            }
        } else {
            this.stage = stage;
        }
    }

    public final boolean isFullScreen() {
        return fullScreen;
    }

    public final Parent getRoot() {
        return root;
    }

    public final Scene getScene() {
        return stage != null ? stage.getScene() : null;
    }

    public final Stage getStage() {
        return stage;
    }

    @Override
    public void assignStage(final Stage stage) {
        Preconditions.checkArgument(stage != null);

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
        Preconditions.checkArgument(owner != null);

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
        Preconditions.checkArgument(controller != null);

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
