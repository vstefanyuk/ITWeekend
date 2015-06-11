package org.quadrate.curiosity.data.message;

import org.quadrate.curiosity.State;

public class StateResponse extends AbstractMessage {
    private final String uuid;
    private final State state;

    public StateResponse(final String uuid, final State state) {
        this.uuid = uuid;
        this.state = state;
    }

    public String getUuid() {
        return uuid;
    }

    public State getState() {
        return state;
    }
}
