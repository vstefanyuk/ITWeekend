package org.quadrate.curiosity.raspy.system;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import org.quadrate.curiosity.State;
import org.quadrate.curiosity.raspy.AbstractBackgroundService;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SystemServiceImpl extends AbstractBackgroundService implements SystemService {
    private static final int ACCEL_GYRO_THERMO_MEASURING_PERIOD = 1000; // Milliseconds
    private static final int BATTERY_MEASURING_PERIOD = 5; // Seconds
    private static final int COMPASS_MEASURING_PERIOD = 1000; // Milliseconds
    private static final int RADAR_MEASURING_PERIOD = 500; // Milliseconds

    private static final int SCHEDULED_TASKS_TERMINATION_PERIOD = 10; // Seconds

    private int videoPort;
    private int audioPort;

    private volatile GpioController gpioController;
    private volatile I2CBus i2cBus;

    private volatile AccelGyroThermoMeter accelGyroThermoMeter;
    private volatile BatteryMeter batteryMeter;
    private volatile Compass compass;
    private volatile Radar radar;
    private volatile Leds leds;
    private volatile Engine engine;

    private volatile AutoPilot autoPilot;

    private volatile ScheduledExecutorService scheduledExecutorService;

    @Override
    public void setVideoPort(final int videoPort) {
        this.videoPort = videoPort;
    }

    @Override
    public void setAudioPort(final int audioPort) {
        this.audioPort = audioPort;
    }

    @Override
    public State getState() {
        final AccelGyroThermoMeter.Measurement accelGyroThermoMeterMeasurement = accelGyroThermoMeter.getLastMeasurement();

        final Engine.Direction engineDirection = engine.getDirection();

        return new State(
            leds.getLights(),
            false,
            radar.getLastDistance(),
            autoPilot.getSpeed(),
            accelGyroThermoMeterMeasurement != null ? accelGyroThermoMeterMeasurement.getTemperature() : null,
            batteryMeter.getLastBatteryLevel(BatteryMeter.Battery.BRAIN),
            batteryMeter.getLastBatteryLevel(BatteryMeter.Battery.ENGINE),
            compass.getLastAzimuth(),
            accelGyroThermoMeterMeasurement != null ? accelGyroThermoMeterMeasurement.getPitch() : null,
            accelGyroThermoMeterMeasurement != null ? accelGyroThermoMeterMeasurement.getRoll() : null,
            engineDirection == Engine.Direction.LEFT_FORWARD || engineDirection == Engine.Direction.FORWARD || engineDirection == Engine.Direction.LEFT_BACKWARD || engineDirection == Engine.Direction.BACKWARD,
            engineDirection == Engine.Direction.RIGHT_FORWARD || engineDirection == Engine.Direction.FORWARD || engineDirection == Engine.Direction.RIGHT_BACKWARD || engineDirection == Engine.Direction.BACKWARD
        );
    }

    @Override
    public void turnLights(final boolean on) {
        leds.turnLights(on);
    }

    @Override
    public void moveOn(double direction, double intensity) {
        autoPilot.moveOn(direction, intensity);
    }

    @Override
    public Integer runCamera() {
        try {
            Runtime.getRuntime().exec(new String[] {"/home/pi/Curiosity/raspivid.sh", String.valueOf(videoPort)});
        } catch (final IOException exception) {
            LOGGER.error("Could not run camera", exception);

            return null;
        }

        return videoPort;
    }

    @Override
    protected void startImpl() throws Exception {
        initDevices();

        initAutoPilot();

        initScheduledTasks();

        super.startImpl();
    }

    private void initDevices() throws Exception {
        gpioController = GpioFactory.getInstance();

        i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);

        accelGyroThermoMeter = new AccelGyroThermoMeter(i2cBus);

        batteryMeter = new BatteryMeter();

        compass = new Compass(i2cBus);

        radar = new Radar(gpioController);

        leds = new Leds(gpioController);

        engine = new Engine(gpioController);
    }

    private void initAutoPilot() throws Exception {
        autoPilot = new AutoPilot(engine, compass, accelGyroThermoMeter);

        autoPilot.start();
    }

    private void initScheduledTasks() {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                accelGyroThermoMeter.readMeasurement();
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 0, ACCEL_GYRO_THERMO_MEASURING_PERIOD, TimeUnit.MILLISECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            private BatteryMeter.Battery measuringBattery = BatteryMeter.Battery.BRAIN;

            @Override
            public void run() {
                try {
                    batteryMeter.readBatteryLevel(measuringBattery);

                    measuringBattery = BatteryMeter.Battery.values()[(measuringBattery.ordinal() + 1) % BatteryMeter.Battery.values().length];
                } catch (final Exception exception) {
                    throw new RuntimeException(exception);
                }
            }
        }, 0, BATTERY_MEASURING_PERIOD, TimeUnit.SECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                compass.readAzimuth();
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 0, COMPASS_MEASURING_PERIOD, TimeUnit.MILLISECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                final AccelGyroThermoMeter.Measurement tempMeasurement = accelGyroThermoMeter.getLastMeasurement();

                radar.readDistance(tempMeasurement != null ? tempMeasurement.getTemperature() : null);
            } catch (final Exception exception) {
                throw new RuntimeException(exception);
            }
        }, 0, RADAR_MEASURING_PERIOD, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void stopImpl() {
        super.stopImpl();

        releaseScheduledTasks();

        releaseAutoPilot();

        releaseDevices();
    }

    private void releaseScheduledTasks() {
        if (scheduledExecutorService != null) {
            try {
                scheduledExecutorService.shutdownNow();

                try {
                    scheduledExecutorService.awaitTermination(SCHEDULED_TASKS_TERMINATION_PERIOD, TimeUnit.SECONDS);
                } catch (final InterruptedException exception) {
                }
            } finally {
                scheduledExecutorService = null;
            }
        }
    }

    private void releaseAutoPilot() {
        if (autoPilot != null) {
            autoPilot.stop();

            autoPilot = null;
        }
    }

    private void releaseDevices() {
        engine = null;

        leds = null;

        radar = null;

        compass = null;

        batteryMeter = null;

        accelGyroThermoMeter = null;

        if (i2cBus != null) {
            try {
                i2cBus.close();
            } catch (final IOException exception) {
                LOGGER.error("Failure closing I2C bus", exception);
            }

            i2cBus = null;
        }

        if (gpioController != null) {
            gpioController.shutdown();

            gpioController = null;
        }
    }

    @Override
    public void run() {
    }
}
