package org.quadrate.curiosity.data.message;

public class MoveOn extends AbstractMessage {
    private final double direction;
    private final double intensity;

    public MoveOn(final double direction, final double intensity) {
        this.direction = direction;
        this.intensity = intensity;
    }

    public double getDirection() {
        return direction;
    }

    public double getIntensity() {
        return intensity;
    }
}
