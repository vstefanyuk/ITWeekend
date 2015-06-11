package org.quadrate.curiosity.data.message;

public class TurnLights extends AbstractMessage {
    private final boolean on;

    public TurnLights(final boolean on) {
        this.on = on;
    }

    public boolean isOn() {
        return on;
    }
}
