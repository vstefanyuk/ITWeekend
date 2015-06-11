package org.quadrate.curiosity.raspy.system;

import org.quadrate.curiosity.raspy.AbstractBackgroundService;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AutoPilot extends AbstractBackgroundService {
    private static final double MIN_COMPENSABLE_AZIMUTH_SHIFT = 0.05;
    private static final double MIN_RETARGABLE_DIRECTION_DIFF = 0.01;
    private static final double MIN_RETARGABLE_INTENSITY_DIFF = 0.01;
    private static final int FORWARD_MOVE_PERIOD = 50;
    private static final int COMPENSATE_MOVE_TICK = 10;

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    private final Engine engine;
    private final Compass compass;
    private final AccelGyroThermoMeter accelGyroThermoMeter;

    private volatile double direction;
    private volatile double intensity;
    private volatile double azimuth;

    private volatile double speed;

    private long startTime;

    public AutoPilot(final Engine engine, final Compass compass, final AccelGyroThermoMeter accelGyroThermoMeter) {
        Objects.requireNonNull(engine);
        Objects.requireNonNull(compass);
        Objects.requireNonNull(accelGyroThermoMeter);

        this.engine = engine;
        this.compass = compass;
        this.accelGyroThermoMeter = accelGyroThermoMeter;
    }

    public void moveOn(final double direction, final double intensity) {
        if (direction < 0.0 || direction > 1.0) {
            throw new IllegalArgumentException();
        }
        if (intensity < 0.0 || intensity > 1.0) {
            throw new IllegalArgumentException();
        }

        lock.lock();

        try {
            if (intensity != 0.0) {
                final boolean continued = this.intensity != 0;

                final double azimuth = (continued ? this.azimuth : getCurrentAzimuth()) + direction - (continued ? this.direction : 0.0);

                this.azimuth = azimuth >= 0.0 ? azimuth % 1.0 : 1.0 + azimuth;

                this.direction = direction;
            }

            this.intensity = intensity;

            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
            startTime = System.nanoTime();

            lock.lock();

            try {
                final double intensity = this.intensity;

                if (intensity != 0.0) {
                    final double azimuth = this.azimuth;

                    final double currentAzimuth = getCurrentAzimuth();

                    final double azimuthSignum = Math.signum(azimuth - currentAzimuth);

                    final double azimuthShift = getAzimuthShift(azimuth, currentAzimuth);

                    if (Math.abs(azimuthShift) > MIN_COMPENSABLE_AZIMUTH_SHIFT) {
                        final Engine.Direction shiftCompensateDirection = azimuthShift > 0.0 ? Engine.Direction.LEFT_FORWARD : Engine.Direction.RIGHT_FORWARD;

                        engine.drive(shiftCompensateDirection, intensity);

                        boolean retargeted = false;

                        final double direction = this.direction;

                        while(azimuthSignum == Math.signum(azimuth - getCurrentAzimuth())) {
                            if (condition.await(COMPENSATE_MOVE_TICK, TimeUnit.MILLISECONDS)) {
                                if (Math.abs(direction - this.direction) > MIN_RETARGABLE_DIRECTION_DIFF || this.intensity == 0.0 || Math.abs(intensity - this.intensity) > MIN_RETARGABLE_INTENSITY_DIFF) {
                                    retargeted = true;

                                    break;
                                }
                            }

                            updateSpeed();
                        }

                        if (retargeted) {
                            continue;
                        }
                    }

                    engine.drive(Engine.Direction.FORWARD, intensity);

                    Thread.sleep(FORWARD_MOVE_PERIOD);

                    updateSpeed();
                } else {
                    engine.drive(Engine.Direction.NEUTRAL, 0.0);

                    speed = 0.0;

                    condition.await();
                }
            } catch(final InterruptedException exception) {
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    protected void stopImpl() {
        super.stopImpl();

        engine.drive(Engine.Direction.NEUTRAL, 0.0);
    }

    private void updateSpeed() {
        final long currentTime = System.nanoTime();

        speed += getCurrentAcceleration() * 9.81 * (currentTime - startTime) / 1000000000.0;

        startTime = currentTime;
    }

    private double getCurrentAzimuth() {
        try {
            return compass.readAzimuth();
        } catch(final Exception exception) {
            return compass.getLastAzimuth();
        }
    }

    private double getCurrentAcceleration() {
        try {
            return accelGyroThermoMeter.readMeasurement().getAccelerationY();
        } catch(final Exception exception) {
            return accelGyroThermoMeter.getLastMeasurement().getAccelerationY();
        }
    }

    private static double getAzimuthShift(final double targetAzimuth, final double currentAzimuth) {
        double azimuthShift = targetAzimuth - currentAzimuth;

        if (Math.abs(azimuthShift) > 0.5) {
            azimuthShift = azimuthShift > 0.0 ? -(1.0 - targetAzimuth + currentAzimuth) : (1.0 - currentAzimuth + targetAzimuth);
        }

        return azimuthShift;
    }
}
