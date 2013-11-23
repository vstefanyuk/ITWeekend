package com.softserve.itw2013.javafx.hello;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import netscape.javascript.JSObject;

public class StarWars extends Application {
    private static final String MESSAGE =
        "A long time ago, in a galaxy far, far away...\n" +
        "\n" +
        "It is a period of civil war. Rebel " +
        "spaceships, striking from a hidden " +
        "base, have won their first victory " +
        "against the evil Galactic Empire.\n" +
        "\n" +
        "During the battle, Rebel spies managed " +
        "to steal secret plans to the Empire's " +
        "ultimate weapon, the Death Star, an " +
        "armored space station with enough " +
        "power to destroy an entire planet.\n" +
        "\n" +
        "Pursued by the Empire's sinister agents, " +
        "Princess Leia races home aboard her " +
        "starship, custodian of the stolen plans " +
        "that can save her people and restore " +
        "freedom to the galaxy...";

    private static final int DEFAULT_TRANSLATE = 100;
    private static final int DEFAULT_ROTATE = 0;
    private static final int DEFAULT_SCALE = 100;

    private BorderPane root;
    private Group content;
    private Text text;

    private Slider translateSlider;
    private Slider rotateSlider;
    private Slider scaleXSlider;
    private Slider scaleYSlider;

    private TranslateTransition translateTransition;
    private RotateTransition rotateTranslation;

    private JSObject jsObject;

    @Override
    public void start(final Stage stage) {
        root = new BorderPane();

        content = new Group();

        initSliders();

        final ImageView imageView = new ImageView("/StarWars.jpg");

        imageView.setFitWidth(imageView.getImage().getWidth() * 2);
        imageView.setPreserveRatio(true);

        content.getChildren().add(0, imageView);

        final Group textGroup = new Group();

        textGroup.setLayoutX(300);
        textGroup.setLayoutY(360);

        textGroup.setClip(new Rectangle(500, 200));

        text = new Text();

        text.setLayoutY(200);
        text.setTextOrigin(VPos.TOP);
        text.setTextAlignment(TextAlignment.JUSTIFY);
        text.setWrappingWidth(500);
        text.setText(MESSAGE);
        text.setFill(Color.rgb(187, 195, 107));
        text.setFont(Font.font("SansSerif", FontWeight.BOLD, 24));

        textGroup.getChildren().add(text);

        content.getChildren().add(textGroup);

        root.setCenter(content);

        initWebView();

        stage.setScene(new Scene(root));

        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Hello, World! Star Wars theme :)");

        stage.show();

        initTranslations();

        final Tooltip startTooltip;

        startTooltip = new Tooltip("Click mouse to start");

        startTooltip.show(stage);

        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                startTooltip.hide();

                translateTransition.play();
                rotateTranslation.play();

                root.setOnMouseClicked(null);
            }
        });
    }

    private void initWebView() {
        final WebView webView = new WebView();

        webView.setPrefHeight(200);

        final WebEngine engine = webView.getEngine();

        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    jsObject = (JSObject) engine.executeScript("window");

                    jsObject.setMember("app", StarWars.this);
                }
            }
        });

        engine.load(getClass().getResource("/JS.html").toExternalForm());

        root.setBottom(webView);
    }

    private void initSliders() {
        translateSlider = new Slider();

        translateSlider.setLayoutX(20);
        translateSlider.setLayoutY(40);
        translateSlider.setPrefWidth(450);
        translateSlider.setShowTickMarks(true);
        translateSlider.setMin(-100);
        translateSlider.setMax(100);
        translateSlider.setBlockIncrement(10);
        translateSlider.setValue(DEFAULT_TRANSLATE);
        translateSlider.setTooltip(new Tooltip("Translate"));
        translateSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                if(jsObject != null) {
                    jsObject.call("setTranslateValue", newNumber.intValue());
                }
            }
        });

        rotateSlider = new Slider();

        rotateSlider.setLayoutX(530);
        rotateSlider.setLayoutY(40);
        rotateSlider.setPrefWidth(450);
        rotateSlider.setShowTickMarks(true);
        rotateSlider.setMin(-100);
        rotateSlider.setMax(100);
        translateSlider.setBlockIncrement(1);
        rotateSlider.setValue(DEFAULT_ROTATE);
        rotateSlider.setTooltip(new Tooltip("Rotate"));
        rotateSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                if(jsObject != null) {
                    jsObject.call("setRotateValue", newNumber.intValue());
                }
            }
        });

        scaleXSlider = new Slider();

        scaleXSlider.setLayoutX(20);
        scaleXSlider.setLayoutY(80);
        scaleXSlider.setPrefWidth(450);
        scaleXSlider.setShowTickMarks(true);
        scaleXSlider.setMin(30);
        scaleXSlider.setMax(100);
        translateSlider.setBlockIncrement(10);
        scaleXSlider.setValue(DEFAULT_SCALE);
        scaleXSlider.setTooltip(new Tooltip("Scale X"));
        scaleXSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                if(jsObject != null) {
                    jsObject.call("setScaleXValue", newNumber.intValue());
                }
            }
        });
        root.scaleXProperty().bind(scaleXSlider.valueProperty().divide(100.0));

        scaleYSlider = new Slider();

        scaleYSlider.setLayoutX(530);
        scaleYSlider.setLayoutY(80);
        scaleYSlider.setPrefWidth(450);
        scaleYSlider.setShowTickMarks(true);
        scaleYSlider.setMin(30);
        scaleYSlider.setMax(100);
        translateSlider.setBlockIncrement(10);
        scaleYSlider.setValue(DEFAULT_SCALE);
        scaleYSlider.setTooltip(new Tooltip("Scale Y"));
        scaleYSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) {
                if(jsObject != null) {
                    jsObject.call("setScaleYValue", newNumber.intValue());
                }
            }
        });
        root.scaleYProperty().bind(scaleYSlider.valueProperty().divide(100));

        Button resetButton = new Button("Reset");

        resetButton.setLayoutX(480);
        resetButton.setLayoutY(60);

        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                reset();
            }
        });

        content.getChildren().addAll(translateSlider, rotateSlider, scaleXSlider, scaleYSlider, resetButton);
    }

    private void initTranslations() {
        translateTransition = new TranslateTransition(new Duration(6000), text);

        translateTransition.setToY(-600);
        translateTransition.setInterpolator(Interpolator.LINEAR);
        translateTransition.setCycleCount(Timeline.INDEFINITE);

        translateTransition.rateProperty().bind(translateSlider.valueProperty().divide(100.0));

        rotateTranslation = new RotateTransition(new Duration(6000), text);

        rotateTranslation.setByAngle(360);
        rotateTranslation.setCycleCount(Timeline.INDEFINITE);

        rotateTranslation.rateProperty().bind(rotateSlider.valueProperty().divide(100.0));
    }

    public void setTranslateValue(double value) {
        translateSlider.setValue(value);
    }

    public void setRotateValue(double value) {
        rotateSlider.setValue(value);
    }

    public void setScaleXValue(double value) {
        scaleXSlider.setValue(value);
    }

    public void setScaleYValue(double value) {
        scaleYSlider.setValue(value);
    }

    public void reset() {
        setTranslateValue(DEFAULT_TRANSLATE);
        setRotateValue(DEFAULT_ROTATE);
        text.setRotate(0);
        setScaleXValue(DEFAULT_SCALE);
        setScaleYValue(DEFAULT_SCALE);
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
