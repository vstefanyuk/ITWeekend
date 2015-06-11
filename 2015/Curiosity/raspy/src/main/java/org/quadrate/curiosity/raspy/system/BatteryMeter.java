package org.quadrate.curiosity.raspy.system;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class BatteryMeter extends AbstractDevice {
    public static enum Battery {
        BRAIN, ENGINE;
    }

    private static final int[][] BATTERIES_EDGE_LEVELS = {
        {600, 850},
        {400, 666}
    };

    private static final SpiChannel SPI_CHANNEL = SpiChannel.CS0;

    private final Map<Battery, Double> lastBatteryLevels = new ConcurrentHashMap<>();

    private final SpiDevice spiDevice;

    public BatteryMeter() throws Exception {
        spiDevice = SpiFactory.getInstance(SPI_CHANNEL, 500000);
    }

    public Double getLastBatteryLevel(final Battery battery) {
        return lastBatteryLevels.get(battery);
    }

    public synchronized double readBatteryLevel(final Battery battery) throws Exception {
        Objects.requireNonNull(battery);

        final int batteryOrdinal = battery.ordinal();

        final byte[] data;

        SPI_LOCK.lock();
        try {
            data = spiDevice.write((byte) (0b11000000 | (batteryOrdinal << 5)), (byte) 0x00);
        } finally {
            SPI_LOCK.unlock();
        }

        final int batteryLevelRaw = (((data[0] & 0x07) << 8) | (data[1] & 0xFE)) >> 1;

        double batteryLevel = Double.NaN;

        if (batteryLevelRaw != 0) {
            //System.out.println(battery + "=" + batteryLevelRaw);

            final int[] batteryEdgeLevels = BATTERIES_EDGE_LEVELS[batteryOrdinal];

            batteryLevel = ((double)batteryLevelRaw - batteryEdgeLevels[0]) / (batteryEdgeLevels[1] - batteryEdgeLevels[0]);

            if (batteryLevel < 0.0) {
                batteryLevel = 0.0;
            } else {
                if (batteryLevel > 1.0) {
                    batteryLevel = 1.0;
                }
            }

            lastBatteryLevels.put(battery, batteryLevel);
        }

        return batteryLevel;
    }
}
