package org.quadrate.curiosity;

import java.io.Serializable;

public class State implements Serializable {
    private final boolean beam;
    private final boolean speaker;
    private final Double radarDistance;
    private final Double speed;
    private final Double temperature;
    private final Double brainBattery;
    private final Double engineBattery;
    private final Double azimuth;
    private final Double pitch;
    private final Double roll;
    private final boolean leftEngine;
    private final boolean rightEngine;

    public State(final boolean beam, final boolean speaker, final Double radarDistance, final Double speed,
        final Double temperature, final Double brainBattery, final Double engineBattery, final Double azimuth,
        final Double pitch, final Double roll, final boolean leftEngine, final boolean rightEngine) {
        this.beam = beam;
        this.speaker = speaker;
        this.radarDistance = radarDistance;
        this.speed = speed;
        this.temperature = temperature;
        this.brainBattery = brainBattery;
        this.engineBattery = engineBattery;
        this.azimuth = azimuth;
        this.pitch = pitch;
        this.roll = roll;
        this.leftEngine = leftEngine;
        this.rightEngine = rightEngine;
    }

    public boolean isBeam() {
        return beam;
    }

    public boolean isSpeaker() {
        return speaker;
    }

    public Double getRadarDistance() {
        return radarDistance;
    }

    public Double getSpeed() {
        return speed;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getBrainBattery() {
        return brainBattery;
    }

    public Double getEngineBattery() {
        return engineBattery;
    }

    public Double getAzimuth() {
        return azimuth;
    }

    public Double getPitch() {
        return pitch;
    }

    public Double getRoll() {
        return roll;
    }

    public boolean isLeftEngine() {
        return leftEngine;
    }

    public boolean isRightEngine() {
        return rightEngine;
    }
}
