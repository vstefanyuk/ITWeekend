package org.quadrate.curiosity.raspy.system;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;

import java.io.IOException;
import java.util.Objects;

import static org.quadrate.curiosity.raspy.RaspyUtils.*;

public class Compass extends AbstractDevice {
    private static final int I2C_DEVICE_ADDRESS = 0x1E;

    private static final int MODE_REGISTER_ADDRESS = 0x02;
    private static final int MEASUREMENT_DATA_ADDRESS = 0x03;
    private static final int STATUS_REGISTER_ADDRESS = 0x09;

    private static final byte SINGLE_MEASUREMENT_MODE = (byte)0x81;

    private static final int MEASUREMENT_DATA_SIZE = 6;

    private static final int MEASUREMENT_PERIOD = 6; // milliseconds
    private static final int MEASUREMENT_PROLONGATION = 1; // milliseconds
    private static final int MEASUREMENT_TIMEOUT = 20; // milliseconds

    private static final byte STATUS_READY_BIT = 0x01;

    private final byte[] buffer = new byte[6];

    private final I2CDevice i2cDevice;

    private volatile Double lastAzimuth;

    public Compass(final I2CBus i2cBus) throws IOException {
        Objects.requireNonNull(i2cBus);

        i2cDevice = i2cBus.getDevice(I2C_DEVICE_ADDRESS);
    }

    public double getLastAzimuth() {
        return lastAzimuth;
    }

    public synchronized Double readAzimuth() throws Exception {
        I2C_LOCK.lock();
        try {
            i2cDevice.write(MODE_REGISTER_ADDRESS, SINGLE_MEASUREMENT_MODE);
        } finally {
            I2C_LOCK.unlock();
        }

        final long startTime = System.currentTimeMillis();

        Thread.sleep(MEASUREMENT_PERIOD);

        while(true) {
            I2C_LOCK.lock();
            try {
                if ((i2cDevice.read(STATUS_REGISTER_ADDRESS) & STATUS_READY_BIT) != 0) {
                    break;
                }
            } finally {
                I2C_LOCK.unlock();
            }

            if ((System.currentTimeMillis() - startTime) > MEASUREMENT_TIMEOUT) {
                throw new Exception("Timeout occured during reading compass azimuth");
            }

            Thread.sleep(MEASUREMENT_PROLONGATION);
        }

        I2C_LOCK.lock();
        try {
            i2cDevice.read(MEASUREMENT_DATA_ADDRESS, buffer, 0, MEASUREMENT_DATA_SIZE);
        } finally {
            I2C_LOCK.unlock();
        }

        final int x = buffer[0] << 8 | ub2i(buffer[1]);
        final int y = buffer[4] << 8 | ub2i(buffer[5]);

        final double azimuth = (Math.atan2(y, x) / Math.PI + 1.0) / 2.0;

        lastAzimuth = azimuth;

        return azimuth;
    }
}
