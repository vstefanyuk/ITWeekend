package org.quadrate.curiosity.raspy.system;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.util.Objects;

public class Engine extends AbstractDevice {
    public static enum Direction {NEUTRAL, LEFT_FORWARD, FORWARD, RIGHT_FORWARD, LEFT_BACKWARD, BACKWARD, RIGHT_BACKWARD}

    private static final Pin MOTOR_PWM_PIN = RaspiPin.GPIO_01;

    private static final Pin LEFT_ENGINE_FORWARD_PIN = RaspiPin.GPIO_19;
    private static final Pin LEFT_ENGINE_BACKWARD_PIN = RaspiPin.GPIO_17;
    private static final Pin RIGHT_ENGINE_FORWARD_PIN = RaspiPin.GPIO_20;
    private static final Pin RIGHT_ENGINE_BACKWARD_PIN = RaspiPin.GPIO_18;

    private static final int MIN_MOTOR_PWM = 512;
    private static final int MAX_MOTOR_PWM = 1023;

    private final GpioPinPwmOutput pwmMotor;

    private final GpioPinDigitalOutput pdoLeftEngineForward;
    private final GpioPinDigitalOutput pdoLeftEngineBackward;
    private final GpioPinDigitalOutput pdoRightEngineForward;
    private final GpioPinDigitalOutput pdoRightEngineBackward;

    private volatile Direction direction = Direction.NEUTRAL;
    private volatile double intensity;

    public Engine(final GpioController gpioController) {
        Objects.requireNonNull(gpioController);

        pwmMotor = gpioController.provisionPwmOutputPin(MOTOR_PWM_PIN, "Motor PWM", 0);

        pdoLeftEngineForward = gpioController.provisionDigitalOutputPin(LEFT_ENGINE_FORWARD_PIN, "Left Engine Forward", PinState.LOW);
        pdoLeftEngineBackward = gpioController.provisionDigitalOutputPin(LEFT_ENGINE_BACKWARD_PIN, "Left Engine Backward", PinState.LOW);
        pdoRightEngineForward = gpioController.provisionDigitalOutputPin(RIGHT_ENGINE_FORWARD_PIN, "Right Engine Forward", PinState.LOW);
        pdoRightEngineBackward = gpioController.provisionDigitalOutputPin(RIGHT_ENGINE_BACKWARD_PIN, "Right Engine Backward", PinState.LOW);
    }

    public Direction getDirection() {
        return direction;
    }

    public double getIntensity() {
        return intensity;
    }

    public synchronized void drive(final Direction direction, final double intensity) {
        Objects.requireNonNull(direction);
        if (intensity < 0.0 || intensity > 1.0) {
            throw new IllegalArgumentException();
        }

        this.direction = direction;
        this.intensity = intensity;

        pdoLeftEngineForward.setState(PinState.LOW);
        pdoLeftEngineBackward.setState(PinState.LOW);
        pdoRightEngineForward.setState(PinState.LOW);
        pdoRightEngineBackward.setState(PinState.LOW);

        pwmMotor.setPwm(MIN_MOTOR_PWM + (int) Math.round(intensity * (MAX_MOTOR_PWM - MIN_MOTOR_PWM)));

        if (direction == Direction.LEFT_FORWARD || direction == Direction.FORWARD) {
            pdoLeftEngineForward.setState(PinState.HIGH);
        }

        if (direction == Direction.RIGHT_FORWARD || direction == Direction.FORWARD) {
            pdoRightEngineForward.setState(PinState.HIGH);
        }
        if (direction == Direction.LEFT_BACKWARD || direction == Direction.BACKWARD) {
            pdoLeftEngineBackward.setState(PinState.HIGH);
        }

        if (direction == Direction.RIGHT_BACKWARD || direction == Direction.BACKWARD) {
            pdoRightEngineBackward.setState(PinState.HIGH);
        }
    }
}
