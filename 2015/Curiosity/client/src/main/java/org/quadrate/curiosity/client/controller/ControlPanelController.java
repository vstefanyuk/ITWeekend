package org.quadrate.curiosity.client.controller;

import eu.hansolo.steelseries.extras.*;
import eu.hansolo.steelseries.gauges.*;
import eu.hansolo.steelseries.tools.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.stage.*;
import org.controlsfx.dialog.Dialogs;
import org.quadrate.curiosity.CuriosityConstants;
import org.quadrate.curiosity.State;
import org.quadrate.curiosity.data.message.AbstractMessage;
import org.quadrate.curiosity.data.message.CameraRequest;
import org.quadrate.curiosity.data.message.CameraResponse;
import org.quadrate.curiosity.data.message.MoveOn;
import org.quadrate.curiosity.data.message.StateRequest;
import org.quadrate.curiosity.data.message.StateResponse;
import org.quadrate.curiosity.data.message.TurnLights;
import org.quadrate.jaf.javafx.AbstractJFXController;

import javax.swing.*;
import java.awt.*;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

public class ControlPanelController extends AbstractJFXController {
    private static final String RASPY_HOST = "192.168.1.2"; // TODO: take from config

    private static final long REFRESH_STATE_INTERVAL = 1000; // Milliseconds

    private static final double OBSTACLE_MAX_VALUE = 400.0;
    private static final double OBSTACLE_WARNING_VALUE = 50.0;
    private static final double OBSTACLE_CRITICAL_VALUE = 30.0;
    private static final double[] OBSTACLE_TRACK_VALUES = new double[] {0.0, 30.0, 200.0};

    private static final double SPEED_MAX_VALUE = 80.0;
    private static final double SPEED_WARNING_VALUE = 60.0;
    private static final double SPEED_CRITICAL_VALUE = 70.0;
    private static final double[] SPEED_TRACK_VALUES = new double[] {60.0, 65.0, 80.0};

    private static final double TEMPERATURE_MIN_VALUE = -20.0;
    private static final double TEMPERATURE_MAX_VALUE = 40.0;
    private static final double TEMPERATURE_WARNING_MIN_VALUE = 0.0;
    private static final double TEMPERATURE_WARNING_MAX_VALUE = 30.0;
    private static final double TEMPERATURE_CRITICAL_MAX_VALUE = 35.0;
    private static final double TEMPERATURE_CRITICAL_MIN_VALUE = -10.0;
    private static final double[] TEMPERATURE_TRACK_VALUES = new double[] {-20.0, 10.0, 40.0};

    private static final double TOUCH_JOYSTICK_SECTOR = 2.0 / 3.0;

    private static final Image NOISE = new Image("/org/quadrate/curiosity/client/img/noise.png");

    @FXML
    private SwingNode swnBeam;
    private Indicator indBeam;
    @FXML
    private SwingNode swnSpeaker;
    private Indicator indSpeaker;
    @FXML
    private SwingNode swnRadar;
    private Linear lnrRadar;
    @FXML
    private SwingNode swnCamera;
    private Indicator indCamera;
    @FXML
    private SwingNode swnMicrophone;
    private Indicator indMicrophone;
    @FXML
    private ImageView imvDisplay;
    @FXML
    private SwingNode swnSpeedometer;
    private Radial2Top r2tSpeedometer;
    @FXML
    private SwingNode swnBrainBattery;
    private Battery btrBrain;
    @FXML
    private SwingNode swnThermometer;
    private Radial2Top r2tThermometer;
    @FXML
    private SwingNode swnEngineBattery;
    private Battery btrEngine;
    @FXML
    private SwingNode swnCompass;
    private Compass cmpCompass;
    @FXML
    private SwingNode swnGyroscope;
    private Horizon hrzGyroscope;
    @FXML
    private SwingNode swnLeftEngine;
    private Indicator indLeftEngine;
    @FXML
    private SwingNode swnRightEngine;
    private Indicator indRightEngine;
    @FXML
    private Circle crlJoystick;
    @FXML
    private Sphere sphJoystick;
    @FXML
    private SwingNode swnStatusLed;
    private Led ledStatus;
    @FXML
    private SwingNode swnSettings;
    private Indicator indSettings;
    @FXML
    private SwingNode swnConnect;
    private Indicator indConnect;

    private double sphJoystickRadius;
    private double shiftJoystickRadius;
    private double touchJoystickRadius;
    private double origSphJoystickLeft;
    private double origSphJoystickTop;

    private boolean joystickMouseCaptured;
    private double prevJoystickMouseX;
    private double prevJoystickMouseY;

    private volatile Socket raspySocket;
    private volatile ObjectOutputStream raspyOutputStream;
    private volatile Thread processMessagesThread;
    private volatile Thread refreshStateThread;

    private Stage stgVideo;
    private Long hWndVideo;

    @FXML
    @Override
    protected void initialize() {
        super.initialize();

        sphJoystickRadius = sphJoystick.getRadius();
        shiftJoystickRadius = crlJoystick.getRadius() - sphJoystickRadius;
        touchJoystickRadius = sphJoystickRadius * TOUCH_JOYSTICK_SECTOR;
        origSphJoystickLeft = AnchorPane.getLeftAnchor(sphJoystick);
        origSphJoystickTop = AnchorPane.getTopAnchor(sphJoystick);

        initSwingControls();
    }

    @Override
    protected void postInitialize(final Stage stage) {
        super.postInitialize(stage);

        final ChangeListener<Number> positionListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                refreshVideoPosition();
            }
        };

        stage.xProperty().addListener(positionListener);
        stage.yProperty().addListener(positionListener);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                releaseVideoWindow();
            }
        });
    }

    public void onJoystickMouseMoved(javafx.scene.input.MouseEvent e) {
        final Point2D joystickPos = sphJoystick.sceneToLocal(e.getSceneX(), e.getSceneY());

        final boolean touched = Math.abs(joystickPos.getX()) < touchJoystickRadius && Math.abs(joystickPos.getY()) < touchJoystickRadius;

        getScene().setCursor(touched ? javafx.scene.Cursor.OPEN_HAND : javafx.scene.Cursor.DEFAULT);
    }

    public void onJoystickMouseExited(javafx.scene.input.MouseEvent e) {
        if (!joystickMouseCaptured) {
            getScene().setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    public void onJoystickMousePressed(javafx.scene.input.MouseEvent e) {
        if (e.isPrimaryButtonDown() && !joystickMouseCaptured) {
            prevJoystickMouseX = e.getSceneX();
            prevJoystickMouseY = e.getSceneY();

            final Point2D joystickPoint = sphJoystick.sceneToLocal(prevJoystickMouseX, prevJoystickMouseY);

            if (Math.abs(joystickPoint.getX()) < touchJoystickRadius && Math.abs(joystickPoint.getY()) < touchJoystickRadius) {
                getScene().setCursor(javafx.scene.Cursor.CLOSED_HAND);

                joystickMouseCaptured = true;
            }

        }
    }

    public void onJoystickMouseDragged(javafx.scene.input.MouseEvent e) {
        if (joystickMouseCaptured) {
            final double joystickMouseX = e.getSceneX();
            final double joystickMouseY = e.getSceneY();

            if (joystickMouseX != prevJoystickMouseX || joystickMouseY != prevJoystickMouseY) {
                final Point2D joystickPos = sphJoystick.sceneToLocal(joystickMouseX, joystickMouseY);

                if (Math.abs(joystickPos.getX()) < sphJoystickRadius && Math.abs(joystickPos.getY()) < sphJoystickRadius) {
                    final boolean touched = Math.abs(joystickPos.getX()) < touchJoystickRadius && Math.abs(joystickPos.getY()) < touchJoystickRadius;

                    if (touched) {
                        final double joystickLeft = AnchorPane.getLeftAnchor(sphJoystick) + joystickMouseX - prevJoystickMouseX;
                        final double joystickTop = AnchorPane.getTopAnchor(sphJoystick) + joystickMouseY - prevJoystickMouseY;

                        final double joystickShiftX = joystickLeft - origSphJoystickLeft;
                        final double joystickShiftY = joystickTop - origSphJoystickTop;

                        if (Math.sqrt(joystickShiftX * joystickShiftX + joystickShiftY * joystickShiftY) < shiftJoystickRadius) {
                            setJoystickPosition(joystickLeft, joystickTop);

                            updateMovement(joystickShiftX, joystickShiftY);
                        }
                    }

                    getScene().setCursor(touched ? javafx.scene.Cursor.CLOSED_HAND : javafx.scene.Cursor.OPEN_HAND);

                    prevJoystickMouseX = joystickMouseX;
                    prevJoystickMouseY = joystickMouseY;
                } else {
                    releaseMouse(e);
                }
            }
        }
    }

    public void onJoystickMouseReleased(javafx.scene.input.MouseEvent e) {
        if (joystickMouseCaptured) {
            releaseMouse(e);
        }
    }

    private void releaseMouse(javafx.scene.input.MouseEvent e) {
        setJoystickPosition(origSphJoystickLeft, origSphJoystickTop);

        final Point2D point = sphJoystick.sceneToLocal(e.getSceneX(), e.getSceneY());

        final boolean touched = Math.abs(point.getX()) < touchJoystickRadius && Math.abs(point.getY()) < touchJoystickRadius;

        getScene().setCursor(touched ? javafx.scene.Cursor.OPEN_HAND : javafx.scene.Cursor.DEFAULT);

        moveOn(0.0, 0.0);

        joystickMouseCaptured = false;
    }

    private void setJoystickPosition(final double leftAnchor, final double topAnchor) {
        AnchorPane.setLeftAnchor(sphJoystick, leftAnchor);
        AnchorPane.setTopAnchor(sphJoystick, topAnchor);
    }

    private void updateMovement(final double joystickShiftX, final double joystickShiftY) {
        final double direction;
        if (joystickShiftY != 0.0) {
            final double angle = Math.atan(Math.abs(joystickShiftX / joystickShiftY)) / Math.PI / 2;

            if (joystickShiftY < 0.0) {
                direction = joystickShiftX > 0.0 ? angle : 1.0 - angle;
            } else {
                direction = joystickShiftX < 0.0 ? (angle + 0.5) : (0.5 - angle);
            }
        } else {
            direction = joystickShiftX <= 0.0 ? 0.75 : 0.25;
        }

        final double intensity = Math.sqrt(joystickShiftX * joystickShiftX + joystickShiftY * joystickShiftY) / shiftJoystickRadius;

        moveOn(direction, intensity);
    }

    private void moveOn(final double direction, final double intensity) {
        sendMessage(new MoveOn(direction, intensity));
    }

    private void initSwingControls() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                indBeam = new Indicator();
                indBeam.setOpaque(true);
                indBeam.setFrameDesign(FrameDesign.CHROME);
                indBeam.setSymbolType(SymbolType.FULL_BEAM);
                indBeam.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                indBeam.setEnabled(false);
                setSwingNodeContent(swnBeam, indBeam);
                indBeam.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        final boolean lightsOn = !indBeam.isOn();

                        sendMessage(new TurnLights(lightsOn));

                        indBeam.setOn(lightsOn);
                    }
                });

                indSpeaker = new Indicator();
                indSpeaker.setOpaque(true);
                indSpeaker.setFrameDesign(FrameDesign.CHROME);
                indSpeaker.setSymbolType(SymbolType.SPEAKER);
                indSpeaker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                indSpeaker.setEnabled(false);
                setSwingNodeContent(swnSpeaker, indSpeaker);

                lnrRadar = new Linear();
                lnrRadar.setOpaque(true);
                lnrRadar.setFrameVisible(false);
                lnrRadar.setTitle("Radar");
                lnrRadar.setUnitString("mm");
                lnrRadar.setMaxValue(OBSTACLE_MAX_VALUE);
                lnrRadar.setValueColor(ColorDef.JUG_GREEN);
                setTrackValues(lnrRadar, OBSTACLE_TRACK_VALUES);
                lnrRadar.setTrackStartColor(Color.RED);
                lnrRadar.setTrackSectionColor(Color.RED);
                lnrRadar.setTrackStopColor(Color.YELLOW);
                lnrRadar.setTrackVisible(true);
                lnrRadar.addPropertyChangeListener("value", new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        updateRadarLed();
                    }
                });
                setSwingNodeContent(swnRadar, lnrRadar);

                indCamera = new Indicator();
                indCamera.setOpaque(true);
                indCamera.setFrameDesign(FrameDesign.CHROME);
                indCamera.setSymbolType(SymbolType.CAMERA);
                indCamera.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                indCamera.setEnabled(false);
                setSwingNodeContent(swnCamera, indCamera);
                indCamera.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        Platform.runLater(() -> {
                            if (stgVideo == null) {
                                createVideoWindow();

                                sendMessage(new CameraRequest(null));
                            } else {
                                releaseVideoWindow();
                            }
                        });
                    }
                });

                indMicrophone = new Indicator();
                indMicrophone.setOpaque(true);
                indMicrophone.setFrameDesign(FrameDesign.CHROME);
                indMicrophone.setSymbolType(SymbolType.MICROPHONE);
                indMicrophone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                indMicrophone.setEnabled(false);
                setSwingNodeContent(swnMicrophone, indMicrophone);

                r2tSpeedometer = new Radial2Top();
                r2tSpeedometer.setOpaque(true);
                r2tSpeedometer.setFrameVisible(false);
                r2tSpeedometer.setTitle("Speed");
                r2tSpeedometer.setUnitString("cm/s");
                r2tSpeedometer.setMaxValue(SPEED_MAX_VALUE);
                setTrackValues(r2tSpeedometer, SPEED_TRACK_VALUES);
                r2tSpeedometer.setTrackStartColor(Color.YELLOW);
                r2tSpeedometer.setTrackSectionColor(Color.YELLOW);
                r2tSpeedometer.setTrackStopColor(Color.RED);
                r2tSpeedometer.setTrackVisible(true);
                r2tSpeedometer.addPropertyChangeListener("value", new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        updateSpeedometerLed();
                    }
                });
                setSwingNodeContent(swnSpeedometer, r2tSpeedometer);

                btrBrain = new Battery();
                btrBrain.setOpaque(true);
                setSwingNodeContent(swnBrainBattery, btrBrain);

                r2tThermometer = new Radial2Top();
                r2tThermometer.setOpaque(true);
                r2tThermometer.setFrameVisible(false);
                r2tThermometer.setTitle("Temperature");
                r2tThermometer.setUnitString("\u00B0C");
                r2tThermometer.setMinValue(TEMPERATURE_MIN_VALUE);
                r2tThermometer.setMaxValue(TEMPERATURE_MAX_VALUE);
                setTrackValues(r2tThermometer, TEMPERATURE_TRACK_VALUES);
                r2tThermometer.setTrackStartColor(Color.BLUE);
                r2tThermometer.setTrackSectionColor(Color.GREEN);
                r2tThermometer.setTrackStopColor(Color.RED);
                r2tThermometer.setTrackVisible(true);
                r2tThermometer.addPropertyChangeListener("value", new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        updateThermometerLed();
                    }
                });
                setSwingNodeContent(swnThermometer, r2tThermometer);

                btrEngine = new Battery();
                btrEngine.setOpaque(true);
                setSwingNodeContent(swnEngineBattery, btrEngine);

                cmpCompass = new Compass();
                cmpCompass.setOpaque(true);
                cmpCompass.setFrameDesign(FrameDesign.BLACK_METAL);
                setSwingNodeContent(swnCompass, cmpCompass);

                hrzGyroscope = new Horizon();
                hrzGyroscope.setOpaque(true);
                hrzGyroscope.setFrameDesign(FrameDesign.BLACK_METAL);
                setSwingNodeContent(swnGyroscope, hrzGyroscope);

                indLeftEngine = new Indicator();
                indLeftEngine.setOpaque(true);
                indLeftEngine.setSymbolType(SymbolType.ENGINE);
                indLeftEngine.setFrameDesign(FrameDesign.BRASS);
                setSwingNodeContent(swnLeftEngine, indLeftEngine);

                indRightEngine = new Indicator();
                indRightEngine.setOpaque(true);
                indRightEngine.setSymbolType(SymbolType.ENGINE);
                indRightEngine.setFrameDesign(FrameDesign.BRASS);
                setSwingNodeContent(swnRightEngine, indRightEngine);

                ledStatus = new Led();
                ledStatus.setOpaque(true);
                setSwingNodeContent(swnStatusLed, ledStatus);

                indSettings = new Indicator();
                indSettings.setOpaque(true);
                indSettings.setFrameDesign(FrameDesign.SHINY_METAL);
                indSettings.setSymbolType(SymbolType.SETTINGS);
                indSettings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setSwingNodeContent(swnSettings, indSettings);
                indSettings.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        sendMessage(new MoveOn(0.0, 1.0));
                    }
                });

                indConnect = new Indicator();
                indConnect.setOpaque(true);
                indConnect.setFrameDesign(FrameDesign.CHROME);
                indConnect.setSymbolType(SymbolType.CONNECT);
                indConnect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setSwingNodeContent(swnConnect, indConnect);
                indConnect.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (!indConnect.isOn()) {
                            connect();
                        } else {
                            disconnect();
                        }
                    }
                });
            });
        } catch (final Exception exception) {
        }
    }

    private void connect() {
        try {
            raspySocket = new Socket(RASPY_HOST, CuriosityConstants.DEFAULT_CONTROL_PORT_NUMBER);

            raspyOutputStream = new ObjectOutputStream(raspySocket.getOutputStream());

            processMessagesThread = new Thread(() -> {
                processIncomeMessages();
            });
            processMessagesThread.start();

            refreshStateThread = new Thread(() -> {
                while(!Thread.interrupted()) {
                    sendMessage(new StateRequest(null));

                    try {
                        Thread.sleep(REFRESH_STATE_INTERVAL);
                    } catch (final InterruptedException exception) {
                        break;
                    }
                }
            });
            refreshStateThread.start();

            indBeam.setEnabled(true);
            indCamera.setEnabled(true);

            indConnect.setOn(true);
        } catch(final Exception exception) {
            Platform.runLater(() -> {
                Dialogs.create()
                        .owner(getStage())
                        .title("Failure")
                        .masthead("Could not connect")
                        .message(exception.getMessage())
                        .showError();
            });
        }
    }

    private void processIncomeMessages() {
        try {
            final ObjectInputStream raspyInputStream = new ObjectInputStream(raspySocket.getInputStream());

            try {
                while(!Thread.currentThread().isInterrupted()) {
                    final AbstractMessage message = (AbstractMessage)raspyInputStream.readObject();

                    SwingUtilities.invokeAndWait(() -> {
                        ledStatus.setLedOn(true);
                    });

                    try {
                        if (message instanceof StateResponse) {
                            SwingUtilities.invokeLater(() -> {
                                refreshState(((StateResponse)message).getState());
                            });
                        }

                        if (message instanceof CameraResponse) {
                            final CameraResponse cameraResponse = (CameraResponse)message;

                            try {
                                final Integer videoPort = cameraResponse.getPort();

                                if (videoPort != null) {
                                    Runtime.getRuntime().exec(new String[] {"cmd", "/C", "Start", "D:\\Projects\\Curiosity\\vmplayer.bat", RASPY_HOST, String.valueOf(videoPort), hWndVideo.toString()});
                                } else {
                                    throw new Exception("Camera is not available");
                                }
                            } catch (final IOException exception) {
                                Platform.runLater(() -> {
                                    releaseVideoWindow();

                                });

                                if (!Thread.currentThread().isInterrupted()) {
                                    Platform.runLater(() -> {
                                        Dialogs.create()
                                                .owner(getStage())
                                                .title("Failure")
                                                .masthead("Could not open camera")
                                                .message(exception.getMessage())
                                                .showError();
                                    });
                                }
                            }
                        }
                    } finally {
                        SwingUtilities.invokeLater(() -> {
                            ledStatus.setLedOn(false);
                        });
                    }
                }
            } finally {
                raspyInputStream.close();
            }
        } catch(final Exception exception) {
            if (!Thread.currentThread().isInterrupted()) {
                Platform.runLater(() -> {
                    Dialogs.create()
                            .owner(getStage())
                            .title("Failure")
                            .masthead("Process message")
                            .message(exception.getMessage())
                            .showError();
                });
            }
        } finally {
            if (!Thread.currentThread().isInterrupted()) {
                processMessagesThread = null;

                disconnect();
            }
        }
    }

    private synchronized void sendMessage(final AbstractMessage message) {
        if (raspyOutputStream != null) {
            try {
                raspyOutputStream.writeObject(message);

                raspyOutputStream.flush();
            } catch(final Exception exception) {
                disconnect();

                Platform.runLater(() -> {
                    Dialogs.create()
                            .owner(getStage())
                            .title("Failure")
                            .masthead("Could send message")
                            .message(exception.getMessage())
                            .showError();
                });
            }
        }
    }

    private void disconnect() {
        if (refreshStateThread != null) {
            refreshStateThread.interrupt();

            try {
                refreshStateThread.join(3000);
            } catch (final InterruptedException exception) {
                refreshStateThread.destroy();
            } finally {
                refreshStateThread = null;
            }
        }

        if (processMessagesThread != null) {
            processMessagesThread.interrupt();

            try {
                processMessagesThread.join(3000);
            } catch (final InterruptedException exception) {
                processMessagesThread.destroy();
            } finally {
                processMessagesThread = null;
            }
        }

        if (raspyOutputStream != null) {
            try {
                raspyOutputStream.close();
            } catch (final IOException exception) {
            }

            raspyOutputStream = null;
        }

        if (raspySocket != null) {
            try {
                raspySocket.close();
            } catch (final IOException exception) {
            }

            raspySocket = null;
        }

        SwingUtilities.invokeLater(() -> {
            indBeam.setEnabled(false);
            indCamera.setEnabled(false);
            ledStatus.setLedOn(false);
            indConnect.setOn(false);
        });
    }

    private void refreshState(final State state) {
        indBeam.setOn(state.isBeam());
        indSpeaker.setOn(state.isSpeaker());
        r2tSpeedometer.setValue(Math.abs(state.getSpeed() * 100.0));
        final Double radarDistance = state.getRadarDistance();
        if (radarDistance != null) {
            lnrRadar.setValue(radarDistance != Double.POSITIVE_INFINITY ? radarDistance * 100.0 : lnrRadar.getMaxValue());
        }
        final Double temperature = state.getTemperature();
        if (temperature != null) {
            r2tThermometer.setValue(temperature);
        }
        final Double brainBattery = state.getBrainBattery();
        if (brainBattery != null) {
            btrBrain.setValue((int)Math.round(brainBattery * 100.0));
        }
        final Double engineBattery = state.getEngineBattery();
        if (engineBattery != null) {
            btrEngine.setValue((int)Math.round(engineBattery * 100.0));
        }
        final Double azimuth = state.getAzimuth();
        if (azimuth != null) {
            cmpCompass.setValue(azimuth * 360.0);
        }
        final Double pitch = state.getPitch();
        if (pitch != null) {
            hrzGyroscope.setPitch(pitch);
        }
        final Double roll = state.getRoll();
        if (roll != null) {
            hrzGyroscope.setRoll(roll);
        }
        indLeftEngine.setOn(state.isLeftEngine());
        indRightEngine.setOn(state.isRightEngine());
    }

    private void setSwingNodeContent(final SwingNode swingNode, final JComponent content) {
        final Pane pane = (Pane)swingNode.getParent();

        final int prefWidth = (int) Math.round(pane.getPrefWidth());
        final int prefHeight = content instanceof Radial2Top ? prefWidth : (int) Math.round(pane.getPrefHeight());

        content.setPreferredSize(new Dimension(prefWidth, prefHeight));

        swingNode.setContent(content);
    }

    private void createVideoWindow() {
        stgVideo = new Stage(StageStyle.UNDECORATED);

        final ImageView imvVideo = new ImageView(NOISE);

        stgVideo.setScene(new Scene(new Pane(imvVideo), imvDisplay.getFitWidth(), imvDisplay.getFitHeight()));

        final Stage stage = (Stage) imvDisplay.getScene().getWindow();

        stgVideo.initOwner(stage);

        refreshVideoPosition();

        stgVideo.show();

        stage.toFront();

        try {
            final Object tkStage = stgVideo.impl_getPeer();

            final Method getPlatformWindow = tkStage.getClass().getDeclaredMethod("getPlatformWindow");
            getPlatformWindow.setAccessible(true);

            final Object platformWindow = getPlatformWindow.invoke(tkStage);

            final Method getNativeHandle = platformWindow.getClass().getMethod("getNativeHandle");
            getNativeHandle.setAccessible(true);

            hWndVideo = (Long)getNativeHandle.invoke(platformWindow);

            SwingUtilities.invokeLater(() -> {
                indCamera.setOn(true);
            });
        } catch (final Throwable exception) {
            throw new RuntimeException("Could not obtain handle of video-window", exception);
        }
    }

    private void refreshVideoPosition() {
        if (stgVideo != null) {
            final Point2D videoPosition = imvDisplay.localToScreen(0, 0);

            stgVideo.setX(videoPosition.getX());
            stgVideo.setY(videoPosition.getY());
        }
    }

    private void releaseVideoWindow() {
        if (stgVideo != null) {
            try {
                stgVideo.close();
            } finally {
                stgVideo = null;
                hWndVideo = null;
            }
        }

        SwingUtilities.invokeLater(() -> {
            indCamera.setOn(false);
        });
    }

    private void updateRadarLed() {
        final double value = lnrRadar.getValue();

        lnrRadar.setLedColor(value < OBSTACLE_WARNING_VALUE ? LedColor.RED : LedColor.RED_LED);
        lnrRadar.setLedBlinking(value < OBSTACLE_CRITICAL_VALUE);
    }

    private void updateSpeedometerLed() {
        final double value = r2tSpeedometer.getValue();

        r2tSpeedometer.setLedColor(value > SPEED_WARNING_VALUE ? LedColor.RED : LedColor.RED_LED);
        r2tSpeedometer.setLedBlinking(value > SPEED_CRITICAL_VALUE);
    }

    private void updateThermometerLed() {
        final double value = r2tThermometer.getValue();

        r2tThermometer.setLedColor(value < TEMPERATURE_WARNING_MIN_VALUE || value > TEMPERATURE_WARNING_MAX_VALUE ? LedColor.RED : LedColor.RED_LED);
        r2tThermometer.setLedBlinking(value < TEMPERATURE_CRITICAL_MIN_VALUE || value > TEMPERATURE_CRITICAL_MAX_VALUE);
    }

    private void setTrackValues(final AbstractGauge gauge, final double[] trackValues) {
        gauge.setTrackStart(trackValues[0]);
        gauge.setTrackSection(trackValues[1]);
        gauge.setTrackStop(trackValues[2]);
    }
}
