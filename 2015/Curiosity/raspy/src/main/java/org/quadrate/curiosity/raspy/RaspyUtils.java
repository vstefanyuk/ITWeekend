package org.quadrate.curiosity.raspy;

public class RaspyUtils {
    private RaspyUtils() {
    }

    public static int getSWord(final byte[] data, final int offset) {
        return data[offset] << 8 | ub2i(data[offset + 1]);
    }
    public static int getUWord(final byte[] buffer, final int offset) {
        return ub2i(buffer[offset]) << 8 | ub2i(buffer[offset + 1]);
    }

    public static int ub2i(final byte b) {
        return ((int)b) & 0xFF;
    }
}
