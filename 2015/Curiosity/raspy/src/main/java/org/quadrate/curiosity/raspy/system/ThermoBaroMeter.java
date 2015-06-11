package org.quadrate.curiosity.raspy.system;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;
import java.util.Objects;

import static org.quadrate.curiosity.raspy.RaspyUtils.*;

public class ThermoBaroMeter extends AbstractDevice {
    public static class Measurement {
        private final double temperature;
        private final double pressure;
        private final double altitude;

        public Measurement(final double temperature, final double pressure) {
            this.temperature = temperature;
            this.pressure = pressure;

            altitude = 44330.0 * (1.0 - Math.pow(pressure / 1013.25, 1.0 / 5.255));
        }

        public double getTemperature() {
            return temperature;
        }

        public double getPressure() {
            return pressure;
        }

        public double getAltitude() {
            return altitude;
        }
    }

    private static final int I2C_DEVICE_ADDRESS = 0x77;

    private static final int CALIBRATION_DATA_ADDRESS = 0xAA;
    private static final int CALIBRATION_DATA_SIZE = 11 * 2;

    private static final int MODE_REGISTER_ADDRESS = 0xF4;
    private static final int MEASUREMENT_DATA_ADDRESS = 0xF6;

    private static final byte MEASURE_TEMERATURE_MODE = (byte)0x2E;
    private static final byte MEASURE_PRESSURE_MODE = 0x34;

    private static final int TEMPERATURE_MEASUREMENT_PERIOD = 5; // milliseconds
    private static final int PRESSURE_MEASUREMENT_PERIOD = 5; // milliseconds

    private static final int TEMPERATURE_MEASUREMENT_DATA_SIZE = 2;
    private static final int PRESSURE_MEASUREMENT_DATA_SIZE = 2;

    private final byte[] buffer = new byte[22];

    private final I2CDevice i2cReadDevice;
    private final I2CDevice i2cWriteDevice;

    // Calibration data
    private final int ac1; // 0x00
    private final int ac2; // 0x01
    private final int ac3; // 0x02
    private final int ac4; // 0x03
    private final int ac5; // 0x04
    private final int ac6; // 0x05
    private final int b1; // 0x06
    private final int b2; // 0x07
    private final int mb; // 0x08
    private final int mc; // 0x09
    private final int md; // 0x0A

    private volatile Measurement lastMeasurement;

    public ThermoBaroMeter(final I2CBus i2cBus) throws IOException {
        Objects.requireNonNull(i2cBus);

        i2cReadDevice = i2cBus.getDevice(I2C_DEVICE_ADDRESS);
        i2cWriteDevice = i2cBus.getDevice(I2C_DEVICE_ADDRESS);

        i2cReadDevice.read(CALIBRATION_DATA_ADDRESS, buffer, 0, CALIBRATION_DATA_SIZE);

        ac1 = getSWord(buffer, 0x00 * 2);
        ac2 = getSWord(buffer, 0x01 * 2);
        ac3 = getSWord(buffer, 0x02 * 2);
        ac4 = getUWord(buffer, 0x03 * 2);
        ac5 = getUWord(buffer, 0x04 * 2);
        ac6 = getUWord(buffer, 0x05 * 2);
        b1 = getSWord(buffer, 0x06 * 2);
        b2 = getSWord(buffer, 0x07 * 2);
        mb = getSWord(buffer, 0x08 * 2);
        mc = getSWord(buffer, 0x09 * 2);
        md = getSWord(buffer, 0x0A * 2);
    }

    public Measurement getLastMeasurement() {
        return lastMeasurement;
    }

    public synchronized Measurement readMeasurement() throws Exception {
        I2C_LOCK.lock();
        try {
            i2cWriteDevice.write(MODE_REGISTER_ADDRESS, MEASURE_TEMERATURE_MODE);
        } finally {
            I2C_LOCK.unlock();
        }

        Thread.sleep(TEMPERATURE_MEASUREMENT_PERIOD);

        I2C_LOCK.lock();
        try {
            i2cReadDevice.read(MEASUREMENT_DATA_ADDRESS, buffer, 0, TEMPERATURE_MEASUREMENT_DATA_SIZE);
        } finally {
            I2C_LOCK.unlock();
        }

        final int ut = getSWord(buffer, 0);

        I2C_LOCK.lock();
        try {
            i2cWriteDevice.write(MODE_REGISTER_ADDRESS, MEASURE_PRESSURE_MODE);
        } finally {
            I2C_LOCK.unlock();
        }

        Thread.sleep(PRESSURE_MEASUREMENT_PERIOD);

        I2C_LOCK.lock();
        try {
            i2cReadDevice.read(MEASUREMENT_DATA_ADDRESS, buffer, 0, PRESSURE_MEASUREMENT_DATA_SIZE);
        } finally {
            I2C_LOCK.unlock();
        }

        final int up = getUWord(buffer, 0);

        int x1 = ((ut - ac6) * ac5) >> 15;

        int x2 = (mc << 11) / (x1 + md);

        final int b5 = x1 + x2;

        final int t = (b5 + 8) >> 4;

        final int b6 = b5 - 4000;

        x1 = (b2 * ((b6 * b6) >> 12)) >> 11;

        x2 = (ac2 * b6) >> 11;

        int x3 = x1 + x2;

        final int b3 = ((ac1 << 2) + x3 + 2) >> 2;

        x1 = (ac3 * b6) >> 13;

        x2 = (b1 * ((b6 * b6) >> 12)) >> 16;

        x3 = (x1 + x2 + 2) >> 2;

        final int b4 = (ac4 * (x3 + 32768)) >> 15;

        final long b7 = (up - b3) * 50000l;

        int p = (int)(b7 < 0x80000000 ? ((b7 << 1) / b4) : ((b7 / b4) << 1));

        x1 = (p >> 8) * (p >> 8);

        x1 = (x1 * 3038) >> 16;

        x2 = (-7357 * p) >> 16;

        p = p + ((x1 + x2 + 3791) >> 4);

        final Measurement measurement = new Measurement(t / 10.0, p / 100.0);

        lastMeasurement = measurement;

        return measurement;
    }
}
