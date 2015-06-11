package org.quadrate.curiosity.raspy.system;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.util.Objects;

public class Radar extends AbstractDevice {
    private static final double DEFAULT_TEMPERATURE = 22.0;

    private static final Pin TRIGGER_PIN = RaspiPin.GPIO_04;
    private static final Pin ECHO_PIN = RaspiPin.GPIO_05;

    private static final int TRIGGER_PERIOD = 20; // microseconds
    private static final int MEASUREMENT_TIMEOUT = 20; // milliseconds
    private static final int MEASUREMENT_PROLONGATION = 50; // microseconds

    private final GpioPinDigitalOutput pdoTrigger;
    private final GpioPinDigitalInput pdiEcho;

    private volatile Double lastDistance;

    public Radar(final GpioController gpioController) {
        Objects.requireNonNull(gpioController);

        pdoTrigger = gpioController.provisionDigitalOutputPin(TRIGGER_PIN, "Trigger", PinState.LOW);
        pdiEcho = gpioController.provisionDigitalInputPin(ECHO_PIN, "Echo");
    }

    public Double getLastDistance() {
        return lastDistance;
    }

    public synchronized double readDistance(final Double temperature) throws Exception {
        double distance = Double.POSITIVE_INFINITY;

        pdoTrigger.setState(PinState.HIGH);
        Thread.sleep(0, TRIGGER_PERIOD * 1000);
        pdoTrigger.setState(PinState.LOW);
        long startTime = System.nanoTime();
        long currentTime;
        while(((currentTime = System.nanoTime()) - startTime) / 1000000 < MEASUREMENT_TIMEOUT) {
            if (pdiEcho.getState() == PinState.HIGH) {
                startTime = currentTime;
                while(((currentTime = System.nanoTime()) - startTime) / 1000000 < MEASUREMENT_TIMEOUT) {
                    if (pdiEcho.getState() == PinState.LOW) {
                        distance = (currentTime - startTime) * (331.4 + 0.6 * (temperature != null ? temperature : DEFAULT_TEMPERATURE)) / (2 * 1000000000.0);

                        break;
                    }

                    Thread.sleep(0, MEASUREMENT_PROLONGATION * 1000);
                }

                break;
            }

            Thread.sleep(0, MEASUREMENT_PROLONGATION * 1000);
        }

        lastDistance = distance;

        return distance;
    }
}
