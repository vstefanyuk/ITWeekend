package org.quadrate.curiosity.raspy.system;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;
import java.util.Objects;

import static org.quadrate.curiosity.raspy.RaspyUtils.ub2i;

public class AccelGyroThermoMeter extends AbstractDevice {
    public static class Measurement {
        private final double accelerationX;
        private final double accelerationY;
        private final double accelerationZ;

        private final double pitch;
        private final double roll;

        private final double temperature;

        private final double gyroX;
        private final double gyroY;
        private final double gyroZ;

        public Measurement(final double accelerationX, final double accelerationY, final double accelerationZ, final double temperature, final double gyroX, final double gyroY, final double gyroZ) {
            this.accelerationX = accelerationX;
            this.accelerationY = accelerationY;
            this.accelerationZ = accelerationZ;

            this.temperature = temperature;

            this.gyroX = gyroX;
            this.gyroY = gyroY;
            this.gyroZ = gyroZ;

            pitch = -Math.toDegrees(Math.atan2(accelerationY, Math.sqrt(accelerationX * accelerationX + accelerationZ * accelerationZ)));
            roll = -Math.toDegrees(Math.atan2(accelerationZ, Math.sqrt(accelerationX * accelerationX + accelerationY * accelerationY)));
        }

        public double getAccelerationX() {
            return accelerationX;
        }

        public double getAccelerationY() {
            return accelerationY;
        }

        public double getAccelerationZ() {
            return accelerationZ;
        }

        public double getTemperature() {
            return temperature;
        }

        public double getGyroX() {
            return gyroX;
        }

        public double getGyroY() {
            return gyroY;
        }

        public double getGyroZ() {
            return gyroZ;
        }

        public double getPitch() {
            return pitch;
        }

        public double getRoll() {
            return roll;
        }
    }

    private static final int I2C_DEVICE_ADDRESS = 0x68;

    private static final int POWER_MGMT1_REGISTER_ADDRESS = 0x6B;

    private static final int MEASUREMENT_DATA_ADDRESS = 0x3B;
    private static final int MEASUREMENT_DATA_SIZE = 14;

    private static final double ACCELERATION_SCALE = 16384.0;

    private static final double TEMPERATURE_OFFSET = 12412.0;
    private static final double TEMPERATURE_SCALE = 340.0;

    private static final double GYRO_SCALE = 131.0;

    private final byte[] buffer = new byte[14];

    private final I2CDevice i2cDevice;

    private volatile Measurement lastMeasurement;

    public AccelGyroThermoMeter(final I2CBus i2cBus) throws IOException {
        Objects.requireNonNull(i2cBus);

        i2cDevice = i2cBus.getDevice(I2C_DEVICE_ADDRESS);

        // Woke up
        i2cDevice.write(POWER_MGMT1_REGISTER_ADDRESS, (byte)0x00);
    }

    public Measurement getLastMeasurement() {
        return lastMeasurement;
    }

    public synchronized Measurement readMeasurement() throws Exception {
        I2C_LOCK.lock();
        try {
            i2cDevice.read(MEASUREMENT_DATA_ADDRESS, buffer, 0, MEASUREMENT_DATA_SIZE);
        } finally {
            I2C_LOCK.unlock();
        }

        final int accelerationXRaw = buffer[0] << 8 | ub2i(buffer[1]);
        final int accelerationYRaw = buffer[2] << 8 | ub2i(buffer[3]);
        final int accelerationZRaw = buffer[4] << 8 | ub2i(buffer[5]);

        final double accelerationX = accelerationXRaw / ACCELERATION_SCALE;
        final double accelerationY = accelerationYRaw / ACCELERATION_SCALE;
        final double accelerationZ = accelerationZRaw / ACCELERATION_SCALE;

        final int temperatureRaw = buffer[6] << 8 | ub2i(buffer[7]);

        final double temperature = (temperatureRaw + TEMPERATURE_OFFSET) / TEMPERATURE_SCALE;

        final int gyroXRaw = buffer[8] << 8 | ub2i(buffer[9]);
        final int gyroYRaw = buffer[10] << 8 | ub2i(buffer[11]);
        final int gyroZRaw = buffer[12] << 8 | ub2i(buffer[13]);

        final double gyroX = gyroXRaw / GYRO_SCALE;
        final double gyroY = gyroYRaw / GYRO_SCALE;
        final double gyroZ = gyroZRaw / GYRO_SCALE;

        final Measurement measurement = new Measurement(accelerationX, accelerationY, accelerationZ, temperature, gyroX, gyroY, gyroZ);

        lastMeasurement = measurement;

        return measurement;
    }
}
