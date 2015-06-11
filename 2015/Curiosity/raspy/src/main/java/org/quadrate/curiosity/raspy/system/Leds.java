package org.quadrate.curiosity.raspy.system;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.util.Objects;

public class Leds extends AbstractDevice {
    public static enum Status {OFF, RED, GREEN, BLUE}

    private static final Pin LIGHTS_PIN = RaspiPin.GPIO_06;

    private static final Pin STATUS_RED_PIN = RaspiPin.GPIO_03;
    private static final Pin STATUS_GREEN_PIN = RaspiPin.GPIO_02;
    private static final Pin STATUS_BLUE_PIN = RaspiPin.GPIO_00;

    private final GpioPinDigitalOutput pdoLights;

    private final GpioPinDigitalOutput pdoStatusRed;
    private final GpioPinDigitalOutput pdoStatusGreen;
    private final GpioPinDigitalOutput pdoStatusBlue;

    private volatile boolean lights;
    private volatile Status status = Status.OFF;

    public Leds(final GpioController gpioController) {
        Objects.requireNonNull(gpioController);

        pdoLights = gpioController.provisionDigitalOutputPin(LIGHTS_PIN, "Lights", PinState.LOW);

        pdoStatusRed = gpioController.provisionDigitalOutputPin(STATUS_RED_PIN, "Status RED", PinState.HIGH);
        pdoStatusGreen = gpioController.provisionDigitalOutputPin(STATUS_GREEN_PIN, "Status GREEN", PinState.HIGH);
        pdoStatusBlue = gpioController.provisionDigitalOutputPin(STATUS_BLUE_PIN, "Status BLUE", PinState.HIGH);
    }

    public boolean getLights() {
        return lights;
    }

    public synchronized void turnLights(final boolean on) {
        this.lights = on;

        pdoLights.setState(on ? PinState.HIGH : PinState.LOW);
    }

    public Status getStatus() {
        return status;
    }

    public synchronized void showStatus(final Status status) {
        Objects.requireNonNull(status);

        this.status = status;

        pdoStatusRed.setState(status == Status.RED ? PinState.LOW : PinState.HIGH);
        pdoStatusGreen.setState(status == Status.GREEN ? PinState.LOW : PinState.HIGH);
        pdoStatusBlue.setState(status == Status.BLUE ? PinState.LOW : PinState.HIGH);
    }
}
